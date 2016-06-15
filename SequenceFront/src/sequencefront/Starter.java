/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sequencefront;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.DestinationSource;

/**
 *
 * @author reggie
 */
public class Starter implements MessageListener{
	private BufferedWriter out;
	private FileWriter fstream;
	private boolean firstMessage = false;
	private int counter = 0;
	private int fileId = 1;

	private void connectSeqToAli(Session session) throws JMSException{
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
		message.setStringProperty("epr", "axis2/services/UniProtBank2");
		message.setStringProperty("service", "UniProtBank2");
		message.setStringProperty("method", "pairSequenceAlignment");
		message.setStringProperty("prefix", "test");
		message.setStringProperty("destparam", "sequence");
		producer.send(message);
	}

	private void connectSeqToAliG(Session session) throws JMSException{
		String connectionTemplate = "<?xml version='1.0' encoding='utf-8'?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+"<soapenv:Body>"
				+"<ns:pairSequenceAlignmentGlobal xmlns:ns=\"http://biojava.vle.wtcw.nl\">"
				+"<ns:sequence>VLEPARAM</ns:sequence>"
				+"</ns:pairSequenceAlignmentGlobal>"
				+"</soapenv:Body>"
				+"</soapenv:Envelope>";


		Destination destination = session.createQueue("Utils.getSequenceForId.connections");
		MessageProducer producer = session.createProducer(destination);
		javax.jms.Message message = session.createTextMessage(connectionTemplate);
		message.setStringProperty("epr", "axis2/services/UniProtBank");
		message.setStringProperty("service", "UniProtBank");
		message.setStringProperty("method", "pairSequenceAlignmentGlobal");
		message.setStringProperty("prefix", "test");
		message.setStringProperty("destparam", "sequence");
		producer.send(message);
	}

	private void connectAliToHtml(Session session) throws JMSException{
		String connectionTemplate = "<?xml version='1.0' encoding='utf-8'?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+"<soapenv:Body>"
				+"<ns:HtmlPrint xmlns:ns=\"http://html.vle.wtcw.nl\">"
				+"<ns:print>VLEPARAM</ns:print>"
				+"</ns:HtmlPrint>"
				+"</soapenv:Body>"
				+"</soapenv:Envelope>";


		Destination destination = session.createQueue("UniProtBank2.pairSequenceAlignment.connections");
		MessageProducer producer = session.createProducer(destination);
		javax.jms.Message message = session.createTextMessage(connectionTemplate);
		message.setStringProperty("epr", "axis2/services/HtmlRender");
		message.setStringProperty("service", "HtmlRender");
		message.setStringProperty("method", "HtmlPrint");
		message.setStringProperty("prefix", "test");
		message.setStringProperty("destparam", "print");
		producer.send(message);
	}

	private void connectAliGToHtml(Session session) throws JMSException{
		String connectionTemplate = "<?xml version='1.0' encoding='utf-8'?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+"<soapenv:Body>"
				+"<ns:HtmlPrint xmlns:ns=\"http://html.vle.wtcw.nl\">"
				+"<ns:print>VLEPARAM</ns:print>"
				+"</ns:HtmlPrint>"
				+"</soapenv:Body>"
				+"</soapenv:Envelope>";


		Destination destination = session.createQueue("UniProtBank.pairSequenceAlignmentGlobal.connections");
		MessageProducer producer = session.createProducer(destination);
		javax.jms.Message message = session.createTextMessage(connectionTemplate);
		message.setStringProperty("epr", "axis2/services/HtmlRender");
		message.setStringProperty("service", "HtmlRender");
		message.setStringProperty("method", "HtmlPrint");
		message.setStringProperty("prefix", "test");
		message.setStringProperty("destparam", "print");
		producer.send(message);
	}
	
	private void deployService(Session session, String serviceName) throws JMSException{
		//Destination destination = session.createQueue("global.services");
		Destination destination = session.createQueue("global.runqueue");
		MessageProducer producer = session.createProducer(destination);
		MapMessage mmsg = session.createMapMessage();
		mmsg.setString("serviceURL", "http://elab.science.uva.nl:8080/~reggie/"+serviceName+".aar");
		mmsg.setString("serviceName", serviceName);
		producer.send(mmsg);

	}
	private void sendKillMsg(Session session) throws JMSException{
		//Destination destination = session.createQueue("global.services");
		Destination destination = session.createQueue("Utils.getSequenceForId.input.cmd");
		MessageProducer producer = session.createProducer(destination);
		MapMessage mmsg = session.createMapMessage();
		mmsg.setString("cmd", "kill");
		producer.send(mmsg);

	}
	private void sendMasterToken(Session session, String queueName) throws JMSException{
		//Destination destination = session.createQueue("global.services");
		Destination destination = session.createQueue(queueName+".cmd");
		MessageProducer producer = session.createProducer(destination);
		MapMessage mmsg = session.createMapMessage();
		mmsg.setString("cmd", "master");
		producer.send(mmsg);

	}

	private void induceInput(Session session) throws FileNotFoundException, IOException, JMSException{
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


		deployService(session, "Utils");

		ArrayList<TextMessage> mlst = new ArrayList();

		while(slitr.hasNext()){
			String ids = (String)slitr.next();
			dbitr = dblst.iterator();
			while(dbitr.hasNext()){
				String sid = (String)dbitr.next();
				String newS = soapMessage_T.replace("#1111#", ids);
				newS = newS.replace("#2222#", sid);
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

	}

	public Starter() throws JMSException, IOException{
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("failover://tcp://elab.science.uva.nl:61616");
		ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection();
		connection.start();
		DestinationSource ds = new DestinationSource(connection);
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		//sendMasterToken(session,"UniProtBank.pairSequenceAlignmentGlobal.input");
		//sendMasterToken(session,"UniProtBank.pairSequenceAlignment.input");
		sendMasterToken(session,"HtmlRender.HtmlPrint.input");
		connectSeqToAli(session);
		connectAliToHtml(session);
		connectAliGToHtml(session);
		connectSeqToAliG(session);
		//sendKillMsg(session);
		//sendKillMsg(session);
		//sendKillMsg(session);
		induceInput(session);

		/*String soapMessage = "<?xml version='1.0' encoding='UTF-8'?>"
				+"<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">"
				+ "<soapenv:Body><ns:HtmlPrint xmlns:ns=\"http://html.vle.wtcw.nl\">"
				+ "<ns:print>Q91G85\nstring 1\nstring 2\n</ns:print>"
				+ "</ns:HtmlPrint>"
				+ "</soapenv:Body>"
				+ "</soapenv:Envelope>";*/


		/*Destination destination = session.createQueue("HtmlRender.HtmlPrint.input");
		MessageProducer producer = session.createProducer(destination);
		javax.jms.Message message = session.createTextMessage(soapMessage);
		message.setStringProperty("epr", "axis2/services/HtmlRender");
		message.setStringProperty("prefix", "test");
		for(int i =0; i< 100;i++)
			producer.send(message);*/
		

		Destination destIn = session.createQueue("HtmlRender.HtmlPrint.output");
		MessageConsumer consumer = session.createConsumer(destIn);
		consumer.setMessageListener(this);

	}

	public void onMessage(Message msg) {
		try {
			if(firstMessage == false){
				firstMessage = true;
				fstream = new FileWriter("results_"+Integer.toString(fileId)+".html");
				out = new BufferedWriter(fstream);
				out.write("<html>\n<table border=\"1\">");
			}

			TextMessage tmsg = (TextMessage) msg;
			Pattern p = Pattern.compile("<ns([0-9]*):return>([a-zA-Z0-9></|_=#&;:\"\\-\\s]+)</ns([0-9]*):return>");
			Matcher mtch = p.matcher(tmsg.getText());
			while (mtch.find() == true) {
				//returns.add(m.group(2));
				//System.out.println(mtch.group(2));
				String conv = mtch.group(2).replace("&lt;", "<");
				conv = conv.replace("&gt;", ">");
				out.write(conv);
				out.flush();
				counter++;
			}
			if(counter > 1000){
				out.write("</table></html>");
				out.flush();
				counter = 0;
				fileId++;
				firstMessage = false;
			}
			//out.write("</table>\n</html>");
			//Close the output stream

		} catch (IOException ex) {
			Logger.getLogger(Starter.class.getName()).log(Level.SEVERE, null, ex);
		} catch (JMSException ex) {
			Logger.getLogger(Starter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
