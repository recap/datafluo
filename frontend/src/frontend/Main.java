/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend;

import com.predic8.wsdl.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.FileWriter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Rule;
import nl.wtcw.vle.biojava.UniProtBankStub;
import nl.wtcw.vle.biojava.UniProtBankStub.TestArrayResponse;
import nl.wtcw.vle.biojava.UniProtBankStub.TestResponse;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.biojava3.alignment.Alignments;
import org.biojava3.alignment.Alignments.PairwiseSequenceAlignerType;
import org.biojava3.alignment.SimpleGapPenalty;
import org.biojava3.alignment.SimpleSubstitutionMatrix;
import org.biojava3.alignment.template.SequencePair;
import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.io.FastaReaderHelper;

/**
 *
 * @author reggie
 */


public class Main implements MessageListener{

	public static String[] getSequenceForId(String[] ids) throws Exception{
		List<String> lst = new ArrayList<String>();
		for(int i = 0; i < ids.length; i++){
			URL uniprotFasta = new URL(String.format("http://www.uniprot.org/uniprot/%s.fasta", ids[i]));
			//System.out.println("URL: "+uniprotFasta.toString());
			BufferedReader in = new BufferedReader(	new InputStreamReader(uniprotFasta.openStream()));
			String contents = new String();
			String line;
			while ((line = in.readLine()) != null)
					contents = contents.concat(line+"\n");
			in.close();
			lst.add(contents);
			//System.out.println("CONTENTS: " +contents);
		}



		String[] result = (String[])lst.toArray(new String[lst.size()]);
		return result;
	}

	public static String pairSequenceAlignment(String[] sequence) throws Exception {
		if(sequence.length < 2)
			return "Expected Sequence List Size 2 But Got " + Integer.toString(sequence.length)+"\n";

        List<ProteinSequence> lst = new ArrayList<ProteinSequence>();
		ByteArrayInputStream seqStream1 = new ByteArrayInputStream(sequence[0].getBytes());
		ByteArrayInputStream seqStream2 = new ByteArrayInputStream(sequence[1].getBytes());
		LinkedHashMap<String, ProteinSequence> a  = FastaReaderHelper.readFastaProteinSequence(seqStream1);
		LinkedHashMap<String, ProteinSequence> b  = FastaReaderHelper.readFastaProteinSequence(seqStream2);
		ProteinSequence protSeq1 = null;
		ProteinSequence protSeq2 = null;
		for (  Entry<String, ProteinSequence> entry : a.entrySet() ) {
			protSeq1 = entry.getValue();
			break;
			//System.out.println( entry.getValue().getOriginalHeader() + "=" + entry.getValue().getSequenceAsString() );
		}
		for (  Entry<String, ProteinSequence> entry : b.entrySet() ) {
			protSeq2 = entry.getValue();
			break;
			//System.out.println( entry.getValue().getOriginalHeader() + "=" + entry.getValue().getSequenceAsString() );
		}
		SubstitutionMatrix<AminoAcidCompound> matrix = new SimpleSubstitutionMatrix<AminoAcidCompound>();
        SequencePair<ProteinSequence, AminoAcidCompound> pair = Alignments.getPairwiseAlignment(protSeq1, protSeq2,
                PairwiseSequenceAlignerType.LOCAL, new SimpleGapPenalty(), matrix);

		String result = String.format("%n%s vs %s%n%s", pair.getQuery().getAccession(), pair.getTarget().getAccession(), pair);

		return result;


		/*lst.add(protSeq1);
		lst.add(protSeq2);

        Profile<ProteinSequence, AminoAcidCompound> profile = Alignments.getMultipleSequenceAlignment(lst);

		return profile.toString();*/
        //System.out.printf("Clustalw:%n%s%n", profile);
        //ConcurrencyTools.shutdown();
    }


	public static void addConection(Session session, String id) throws JMSException{
		/*String connectionTemplate = "<?xml version='1.0' encoding='utf-8'?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+"<soapenv:Body>"
				+"<ns:doubleInt xmlns:ns=\"http://calcws\">"
				+"<ns:p>#VLEPARAM:p#</ns:p>"
				+"</ns:doubleInt>"
				+"</soapenv:Body>"
				+"</soapenv:Envelope>";*/

		String connectionTemplate = "<?xml version='1.0' encoding='utf-8'?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+"<soapenv:Body>"
				+"<ns:pairSequenceAlignment xmlns:ns=\"http://biojava.vle.wtcw.nl\">"
				+"<ns:sequence>VLEPARAM</ns:sequence>"
				+"</ns:pairSequenceAlignment>"
				+"</soapenv:Body>"
				+"</soapenv:Envelope>";


		Destination destination = session.createQueue("Utils.getSequenceForId.connections");
		MessageProducer producer = session.createProducer(destination);
		javax.jms.Message message = session.createTextMessage(connectionTemplate);
		message.setStringProperty("epr", "axis2/services/UniProtBank");
		message.setStringProperty("service", "UniProtBank");
		message.setStringProperty("method", "pairSequenceAlignment");
		message.setStringProperty("prefix", id);
		message.setStringProperty("destparam", "sequence");
		producer.send(message);
	}

	private static void deployService(Session session, String serviceName) throws JMSException{
		//Destination destination = session.createQueue("global.services");
		Destination destination = session.createQueue("global.runqueue");
		MessageProducer producer = session.createProducer(destination);
		MapMessage mmsg = session.createMapMessage();
		mmsg.setString("serviceURL", "http://elab.science.uva.nl:8080/~reggie/"+serviceName+".aar");
		mmsg.setString("serviceName", serviceName);
		producer.send(mmsg);

	}

	private static void deployService2(Session session, String serviceName) throws JMSException{
		//Destination destination = session.createQueue("global.services");
		Destination destination = session.createQueue("global.services");
		MessageProducer producer = session.createProducer(destination);
		MapMessage mmsg = session.createMapMessage();
		mmsg.setString("serviceURL", "http://elab.science.uva.nl:8080/~reggie/"+serviceName+".aar");
		mmsg.setString("serviceName", serviceName);
		producer.send(mmsg);

	}


	
	public static void writeToMQ(Session session, String id) throws JMSException{
		/*String soapMessage = "<?xml version='1.0' encoding='utf-8'?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+"<soapenv:Body>"
				+"<ns:add xmlns:ns=\"http://calcws\">"
				+ "<ns:j>10</ns:j><ns:i>1</ns:i>"
				+"</ns:add>"
				+"</soapenv:Body>"
				+"</soapenv:Envelope>";*/

		/*String soapMessage = "<?xml version='1.0' encoding='utf-8'?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+"<soapenv:Body>"
				+"<ns:getStringSequenceForId xmlns:ns=\"http://biojava.vle.wtcw.nl\">"
				+ "<ns:uniProtId>"+id+"</ns:uniProtId>"
				+"</ns:getStringSequenceForId>"
				+"</soapenv:Body>"
				+"</soapenv:Envelope>";*/

		/*String soapMessage = "<?xml version='1.0' encoding='utf-8'?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+"<soapenv:Body>"
				+"<ns:alignPairGlobal xmlns:ns=\"http://biojava.vle.wtcw.nl\">"
				+ "<ns:id1>Q21691</ns:id1>"
				+ "<ns:id2>Q21495</ns:id2>"
				+ "<ns:sl>20000</ns:sl>"
				+"</ns:alignPairGlobal>"
				+"</soapenv:Body>"
				+"</soapenv:Envelope>";*/

		/*String soapMessage = "<?xml version='1.0' encoding='utf-8'?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+"<soapenv:Body>"
				+"<ns:Test xmlns:ns=\"http://biojava.vle.wtcw.nl\">"
				+"</ns:Test>"
				+"</soapenv:Body>"
				+"</soapenv:Envelope>";*/

		/*String soapMessage = "<?xml version='1.0' encoding='utf-8'?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+"<soapenv:Body>"
				+"<ns:mul xmlns:ns=\"http://calcws\">"
				+ "<ns:j>9</ns:j><ns:i>9</ns:i>"
				+"</ns:mul>"
				+"</soapenv:Body>"
				+"</soapenv:Envelope>";*/

		/*String soapMessage = "<?xml version='1.0' encoding='utf-8'?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+"<soapenv:Body>"
				+"<ns:getVersion xmlns:ns=\"http://axisversion.sample\">"
				+"</ns:getVersion>"
				+"</soapenv:Body>"
				+"</soapenv:Envelope>";*/

		/*String soapMessage = "<?xml version='1.0' encoding='utf-8'?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+"<soapenv:Body>"
				+"<ns:doubleMatrix xmlns:ns=\"http://calcws\">"
				+"</ns:doubleMatrix>"
				+"</soapenv:Body>"
				+"</soapenv:Envelope>";*/

		/*String soapMessage = "<?xml version='1.0' encoding='utf-8'?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+"<soapenv:Body>"
				+"<ns:pairSequenceAlignment xmlns:ns=\"http://biojava.vle.wtcw.nl\">"
				+ "<ns:sequence1>>sp|Q91G85|009R_IIV6 Uncharacterized protein 009R OS=Invertebrate iridescent virus 6 GN=IIV6-009R PE=4 SV=1\n"
				+ "MIKLFCVLAAFISINSACQSSHQQREEFTVATYHSSSICTTYCYSNCVVASQHKGLNVES\n"
				+ "YTCDKPDPYGRETVCKCTLIKCHDI</ns:sequence1>"
				+ "<ns:sequence2>>sp|Q197C8|032R_IIV3 Uncharacterized protein 032R OS=Invertebrate iridescent virus 3 GN=IIV3-032R PE=4 SV=1\n"
				+ "MKLMLEIVKNISEPVGKLAIWFNETYQVDVSETINKWNELTGMNITVQENAVSADDTTAE\n"
				+ "ETEYSVVVNENPTRTAARTRKESKTAAKPRKMQIPKTKDVCQHIFKSGSRAGEQCTTKPK\n"
				+ "NNALFCSAHRVRNSVTSNATEASEKTVAKTNGTAAPQKRGVKSKSPTVIPSDFDDSDSSS\n"
				+ "SATRGLRKAPTLSPRKPPPTTTTASSAQEEEDEQQAHFSGSSSPPPKNNGNGAVYSDSSS\n"
				+ "DEDDDDAHHTTVIPLLKKGARKPLDENVQFTSDSSDEED</ns:sequence2>"
				+"</ns:pairSequenceAlignment>"
				+"</soapenv:Body>"
				+"</soapenv:Envelope>";*/

		/*String soapMessage = "<?xml version='1.0' encoding='UTF-8'?>"
				+"<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">"
				+ "<soapenv:Body><ns:testArray xmlns:ns=\"http://biojava.vle.wtcw.nl\">"
				+ "<ns:a>flippy</ns:a>"
				+ "<ns:a>refty</ns:a>"
				+ "</ns:testArray>"
				+ "</soapenv:Body>"
				+ "</soapenv:Envelope>";*/

		/*String soapMessage = "<?xml version='1.0' encoding='UTF-8'?>"
				+"<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">"
				+ "<soapenv:Body><ns:getSequenceForId xmlns:ns=\"http://biojava.vle.wtcw.nl\">"
				+ "<ns:ids>Q91G85</ns:ids>"
				+ "<ns:ids>Q197E9</ns:ids>"
				+ "</ns:getSequenceForId>"
				+ "</soapenv:Body>"
				+ "</soapenv:Envelope>";*/

		/*String soapMessage = "<?xml version='1.0' encoding='utf-8'?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+ "<soapenv:Body>"
				+ "<ns:doubleMatrix xmlns:ns=\"http://calcws\">"
				+ "<ns:matrix xmlns:ax21=\"http://type.octave.ange.dk/xsd\" "
				+ "xmlns:ax22=\"http://matrix.type.octave.ange.dk/xsd\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:type=\"ax21:OctaveDouble\">"
				+ "<ax22:data>"
				+ "<ax22:array>2.0</ax22:array>"
				+ "<ax22:array>4.0</ax22:array>"
				+ "<ax22:array>6.0</ax22:array>"
				+ "<ax22:array>8.0</ax22:array>"
				+ "</ax22:data>"
				+ "<ax22:size>2</ax22:size>"
				+ "<ax22:size>2</ax22:size>"
				+ "</ns:matrix>"
				+ "</ns:doubleMatrix>"
				//+ "<ns1:doubleMatrix xmlns:ns1=\"http://ws.apache.org/axis2/xsd\" />"
				+ "</soapenv:Body></soapenv:Envelope>";*/


		String soapMessage = "<?xml version='1.0' encoding='UTF-8'?>"
				+"<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">"
				+ "<soapenv:Body><ns:HtmlPrint xmlns:ns=\"http://html.vle.wtcw.nl\">"
				+ "<ns:print>Q91G85\nstring 1\nstring 2\n</ns:print>"
				+ "</ns:HtmlPrint>"
				+ "</soapenv:Body>"
				+ "</soapenv:Envelope>";

		//ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
		
		
		//Destination destination = session.createQueue("UniProtBank.alignPairGlobal.input");
		//Destination destination = session.createQueue("UniProtBank.pairSequenceAlignment.input");
		//Destination destination = session.createQueue("UniProtBank.getSequenceForId.input");
		Destination destination = session.createQueue("HtmlRender.HtmlPrint.input");
		//Destination destination = session.createQueue("UniProtBank.testArray.input");
		//Destination destination = session.createQueue("calcWS.add.input");
		//Destination destination = session.createQueue("calcWS.doubleMatrix.input");
		MessageProducer producer = session.createProducer(destination);
		javax.jms.Message message = session.createTextMessage(soapMessage);
		message.setStringProperty("epr", "axis2/services/HtmlRender");
		//message.setStringProperty("epr", "axis2/services/calcWS");
		message.setStringProperty("prefix", id);
		producer.send(message);

		

	}


    /**
     * @param args the command line arguments
     */
    /*public static void main(String[] args) throws IOException, JMSException, ServiceException, Exception {


		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("failover://tcp://elab.science.uva.nl:61616");
		ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection();
		//connection.start();
		DestinationSource ds = new DestinationSource(connection);

		String fileName = "replication.fcl";

		FIS fis = FIS.load(fileName,true);
        // Error while loading?
        if( fis == null ) {
            System.err.println("Can't load file: '"
                                   + fileName + "'");
            return;
        }

        // Show
        fis.chart();

        // Set inputs
       // fis.setVariable("taskLoad", 1.1);
       // fis.setVariable("resourceLoad", 0.6);

        // Evaluate
       // fis.evaluate();

        // Show output variable's chart
        //fis.getVariable("tip").chartDefuzzifier(true);
		//double f = fis.getVariable("replication").getValue();

        // Print ruleSet
        //System.out.println(Double.toString(f));
		
		/*BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(true){
			System.out.print("Enter taskLoad: ");
		double d1 = Double.parseDouble(br.readLine());
		System.out.print("Enter resourceLoad: ");
		double d2 = Double.parseDouble(br.readLine());
		fis.setVariable("taskLoad", d1);
        fis.setVariable("resourceLoad", d2);*/
	/*	{
		double tl = 0;
		double rl = 0;
		FileWriter fw = new FileWriter("fuzzy_plot.plt");
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("# taskLoad resourceLoad replication\n");
		int k = 0;
		for(tl=0;tl<=2;tl += 0.1){
			for(rl=0;rl<=3;rl+=0.1){
				k++;
				fis.setVariable("taskLoad", tl);
				fis.setVariable("resourceLoad", rl);
				fis.evaluate();
				double r = Math.round(fis.getVariable("replication").getValue());
				bw.write(Double.toString(tl)+" "
						+Double.toString(rl)+" "
						+Double.toString(r)+"\n");
				//if(k >= 10){
				//	k = 0;
					//bw.write("\n");
				//}
			}
			bw.write("\n");
		}
		bw.flush();
		bw.close();
		fw.close();
		System.out.println("Ready");
		//System.exit(0);

		fis.evaluate();
        // Show output variable's chart
        //fis.getVariable("tip").chartDefuzzifier(true);
		double f = Math.round(fis.getVariable("replication").getValue());

        // Print ruleSet
        System.out.println(Double.toString(f));
		for( Rule r : fis.getFunctionBlock("replication").getFuzzyRuleBlock("No1").getRules() )
                       System.out.println(r);
		Thread.sleep(30000);
		}

		

		/*ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("failover://tcp://elab.science.uva.nl:61616");
		ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection();
		connection.start();
		DestinationSource ds = new DestinationSource(connection);
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		frontend.Logger l = new frontend.Logger();
		

		//deployService2(session, "Utils");
		//deployService2(session, "UniProtBank");
		//writeToMQ(session,null);
		//writeToMQ(session,null);
		//writeToMQ(session,null);
		//writeToMQ(session,null);

		/*FileWriter fstream = new FileWriter("results.html");
		BufferedWriter out = new BufferedWriter(fstream);
		out.write("<html>\n<table>");


		Destination dest = session.createQueue("HtmlRender.HtmlPrint.output");
		MessageConsumer consumer = session.createConsumer(dest);

		javax.jms.Message msg = consumer.receive();

		TextMessage tmsg = (TextMessage)msg;

		String smsg = tmsg.getText();

		System.out.println(smsg);
		
		Pattern p = Pattern.compile("<ns([0-9]*):return>([a-zA-Z0-9></|_=#&;\"\\-\\s]+)</ns([0-9]*):return>");
		Matcher mtch = p.matcher(tmsg.getText());
		ArrayList<String> returns = new ArrayList();
		while(mtch.find() == true){
				//returns.add(m.group(2));
				System.out.println(mtch.group(2));
				String conv = mtch.group(2).replace("&lt;", "<");
				out.write(conv);
		}

		out.write("</table>\n</html>");
		 //Close the output stream
		out.close();

		System.exit(0);
		
		FileReader dbfile = new FileReader("db.txt");
		BufferedReader dbreader = new BufferedReader(dbfile);
		String line;
		ArrayList<String> dblst = new ArrayList();

		line = dbreader.readLine();

		while(line != null){
			dblst.add(line);
			line = dbreader.readLine();
		}
		dbfile.close();
		dbreader.close();

		FileReader slfile = new FileReader("sl.txt");
		BufferedReader slreader = new BufferedReader(slfile);

		ArrayList<String> sllst = new ArrayList();

		line = slreader.readLine();

		while(line != null){
			sllst.add(line);
			line = slreader.readLine();
		}
		slfile.close();
		slreader.close();

		String soapMessage_T = "<?xml version='1.0' encoding='UTF-8'?>\n"
				+"<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">\n"
				+ "<soapenv:Body><ns:getSequenceForId xmlns:ns=\"http://utils.vle.wtcw.nl\">\n"
				+ "<ns:ids>#1111#</ns:ids>\n"
				+ "<ns:ids>#2222#</ns:ids>\n"
				+ "</ns:getSequenceForId>\n"
				+ "</soapenv:Body>\n"
				+ "</soapenv:Envelope>\n";


		Iterator dbitr = dblst.iterator();
		Iterator slitr = sllst.iterator();

		Destination destination = session.createQueue("Utils.getSequenceForId.input");
		MessageProducer producer = session.createProducer(destination);

		//addConection(session,null);
		//for(int i=0; i < 20; i++)
			deployService2(session, "Utils");
		ArrayList<TextMessage> mlst = new ArrayList();
		
		while(slitr.hasNext()){
			String ids = (String)slitr.next();
			dbitr = dblst.iterator();
			while(dbitr.hasNext()){
				String sid = (String)dbitr.next();
				String newS = soapMessage_T.replace("#1111#", ids);
				newS = newS.replace("#2222#", sid);
				//String newS = soapMessage_T.replace("#1111#", "Q91G57");
				//newS = newS.replace("#2222#", "Q91G85");


				//System.out.println("OUT: "+newS);
				javax.jms.TextMessage message = session.createTextMessage();
				message.setText(newS);
				message.setStringProperty("epr", "axis2/services/Utils");
				message.setStringProperty("prefix", "test");
				mlst.add(message);
				//break;
				
			}
			//break;
		}

		Iterator mitr = mlst.iterator();
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		while(mitr.hasNext()){
			TextMessage m = (TextMessage)mitr.next();			
			producer.send(m);
		}

		producer.close();
		connection.close();
		

		//System.out.println("OUT: "+(String)alst.get(5));

		//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		//br.read();
		//br.read();

		System.exit(0);


		/*ArrayList<String> alst = new ArrayList();
		alst.add("Q197F5");
		alst.add("Q197F7");

		String[] par = (String[])alst.toArray(new String[alst.size()]);

		String[] res = getSequenceForId(par);

		String out = pairSequenceAlignment(res);

		System.out.print(out);

		System.exit(0);*/


		//Pattern p = Pattern.compile("[a-zA-Z]+([0-9]+)[a-zA-Z]+");
		/*Pattern p = Pattern.compile("<ns([0-9]*):return>([a-zA-Z0-9>|_=&;\\-\\s]+)</ns([0-9]*):return>");
		//Pattern p = Pattern.compile("<ns([0-9]*):return>([.*?]+)</ns([0-9]*):return>");
		Matcher m = p.matcher("<ns:return>&gt;sp|Q197E9|011L_IIV3 Uncharacterized protein 011L OS=Invertebrate iridescent virus 3 GN=IIV3-011L PE=4 SV=1\n"
				+ "MMESPKYKKSTCSVTNLGGTCILPQKGATAPKAKDVSPELLVNKMDNLCQDWARTRNEYN\n"
				+ "KVHIEQAPTDSYFGVVHSHTPKKKYTSRDSDSEPEATSTRRSATAQRAANLKSSPVDQWS\n"
				+ "TTPPQPQPQPAAPTVKKTCASSPPAALSVKRTCTSPPPPPVLIDDDTGEDAFYDTNDPDI\n"
				+ "FYDIENGVSELETEGPKRPVYYQRNIRYPIDGSVPQESEQWYDPIDDEFLASSGDVVSLE\n"
				+ "PSPIAAFQPTPPKTVQFVPMPEEIIVPPPPPPKTVVDEGVQAMPYTVDQMIQTDFEESPL\n"
				+ "LANVNLRTIPIEEVNPNFSPVLMQDMVRDSFVFGTVAQRVMASQRVKQFFKELIEQDVSL\n"
				+ "AGRMCMDSGSPQLNLYNSLMGVKLLYRWRSSTTFYRAIVPEIDEPVQVMQDVLSSSEWAK\n"
				+ "FDSQAGIPPKMVYIHYKLLNDLVKTLICPNFQLTHAALVCVDCRPEAVGSDGLQDGRQRR\n"
				+ "CSNLVSEYHEMTLEDLFNTIKPADLNAKNIILSVLFQMLYAVATVQKQFGMGGLFANADS\n"
				+ "VHVRRIQPGGFWHYTVNGLRYSVPNYGYLVILTNFTDVVNYRPDFATTRYFGRRQAKVVP\n"
				+ "TRNWYKFVPFTTRYRPFVTVDPITQAKTTAYAPNPPTEGITINEFYKDSSDLRPSVPVDL\n"
				+ "NDMITFPVPEFHLTICRLFSFFSKFYDSNFIGNDPFVRNLVDRYSQPFEFPDVYWPEDGV\n"
				+ "SRVLACYTIEEIYPNWVDGDTDYVIESYNLD</ns:return>");

		ArrayList<String> lst = new ArrayList();
		while (m.find() == true) {
			lst.add(m.group(2));
			//System.out.println(m.group(2));
			//System.out.println(m.groupCount());
		}

		String dest = "p";
		String connectionTemplate = "<?xml version='1.0' encoding='utf-8'?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
				+"<soapenv:Body>\n"
				+"<ns:doubleInt xmlns:ns=\"http://calcws\">\n"
				+"<ns1:p>VLEPARAM</ns1:p>\n"
				+"</ns:doubleInt>\n"
				+"</soapenv:Body>\n"
				+"</soapenv:Envelope>\n";

		Pattern p2 = Pattern.compile("<ns([0-9]*):"+dest+">([a-zA-Z0-9]+)</ns([0-9]*):"+dest+">");
		Matcher m2 = p2.matcher(connectionTemplate);

		while(m2.find() == true){
			//System.out.println(m2.group(2));
			String f = m2.group();
			String r = m2.group(2);
			Iterator itr = lst.iterator();
			String toChange = "";
			while(itr.hasNext()){
				String s = (String)itr.next();
				String k = f.replace(r, s);
				toChange = toChange.concat(k);
			}
			String tmp = m2.replaceAll(toChange);
			System.out.println(tmp);

			//String x = m2.group();
			//x = "sss";
		}

		//System.out.println(m2.toString());


		System.exit(0);*/
		
		/*UniProtBankStub stublist = new UniProtBankStub();
		UniProtBankStub.TestArray  reql = new UniProtBankStub.TestArray();
		ArrayList a = new ArrayList();
		a.add("flippy");
		a.add("refty");
		reql.setA((String[])a.toArray(new String[a.size()]));

		TestArrayResponse responselist = stublist.testArray(reql);
		System.out.println("Response : " + responselist.get_return());
		//CalcWSStub ws;

		

		System.exit(0);*/
		//dk.ange.octave.type.xsd.OctaveDouble a = new dk.ange.octave.type.xsd.OctaveDouble();
		//dk.ange.octave.type.OctaveDouble ab = new dk.ange.octave.type.OctaveDouble(new double[] { 1, 2, 3, 4 }, 2, 2);
		//a.se

		//a.setData((JAXBElement<dk.ange.octave.type.xsd.OctaveDouble>)ab);
		//int i = add(10,12);
		//System.out.println("Output: "+Integer.toString(i));
	/*	ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("failover://tcp://elab.science.uva.nl:61616");
		ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection();
		connection.start();
		DestinationSource ds = new DestinationSource(connection);
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);*/

		//ds.start();
		//Set s = ds.getQueues();
		//Iterator it = s.iterator();
		//while(it.hasNext()){
		//	ActiveMQQueue q = (ActiveMQQueue) it.next();
//
		//	System.out.println("Queue Name: "+q.getQueueName());

		//}

		//System.exit(0);
		
		//for(int i = 1; i <100; i++){
			//deployService(session,"calcWS");
		//	deployService(session,"UniProtBank");
			//writeToMQ(session, "test-run-1");
			//addConecction(session,"test-run-1");
		//}

		//Destination destination = session.createQueue("global.runqueue");
		//MessageProducer producer = session.createProducer(destination);
		//javax.jms.Message message = session.createTextMessage("");
		//message.setStringProperty("epr", "axis2/services/UniProtBank");
		//message.setStringProperty("prefix", id);
		//for(int j=1;j<21;j++){
			//producer.send(message);
		//}

		//writeToMQ(session, "");

		/*connection.close();
		System.exit(0);
        // TODO code application logic here
		//String wsdlFile = readFile("/home/reggie/TRUNK/frontend/calcWS.wsdl");
		WSDLParser parser = new WSDLParser();
		//Definitions defs = parser.parse("http://localhost:8080/axis2/services/calcWS?wsdl");
		Definitions defs = parser.parse("/home/reggie/TRUNK/frontend/calcWS.wsdl");
		//System.out.println(defs.ELEMENTNAME.getNamespaceURI());
		System.out.println(defs.getDocumentation().getContent());
		for (PortType pt : defs.getPortTypes()) {
			System.out.println(pt.getName());
			for (Operation op : pt.getOperations()) {
				System.out.println(" -" + op.getName());
			}

		}*/
  //  }*/

	public void onMessage(javax.jms.Message msg) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	



}
