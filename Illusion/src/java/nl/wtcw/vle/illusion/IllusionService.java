/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.illusion;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import com.predic8.wsdl.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

/**
 *
 * @author reggie
 */
@WebService()
public class IllusionService {

	private HashMap<String, AbstractService> serviceMap = new HashMap();
	/**
	 * Web service operation
	 */

	private ConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;

	public IllusionService(){
		try {
			connectionFactory = new ActiveMQConnectionFactory("failover://tcp://elab.science.uva.nl:61616");
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			connection.start();
		} catch (JMSException ex) {
			Logger.getLogger(IllusionService.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@WebMethod(operationName = "registerWebservice")
	public String registerWebservice(@WebParam(name = "wsdl")
	String wsdl) {
		WSDLParser parser = new WSDLParser();
		Definitions defs = parser.parse(wsdl);
		//System.out.println(defs.getDocumentation().getContent());
		AbstractService abstractService = new AbstractService(defs.getDocumentation().getContent());
		for (PortType pt : defs.getPortTypes()) {
			//System.out.println(pt.getName());
			for (Operation op : pt.getOperations()) {
				abstractService.addOperation(op.getName());
				//System.out.println(" -" + op.getName());
			}
		}
		serviceMap.put(abstractService.getName(), abstractService);

		return null;
	}

	/**
	 * Web service operation
	 */ @WebMethod(operationName = "callOperation")
	public String callOperation(@WebParam(name = "serviceName")
	String serviceName, @WebParam(name = "soapMessage")
	String soapMessage) {
		InputStream ins = null;
		try {
			Destination servDest = session.createQueue(serviceName+".config");
			MessageConsumer servCons = session.createConsumer(servDest);
			javax.jms.Message servMesg = servCons.receiveNoWait();
			boolean serviceActive = false;
			if(servMesg != null){
				long curTime = System.currentTimeMillis();
				if((curTime - servMesg.getJMSTimestamp()) < 15000)
					serviceActive = true;
			}

			if(serviceActive == false){
				GramSubmitter gsubmitter = new GramSubmitter("fs2","fs2.das3.science.uva.nl",0,null);
				gsubmitter.submit("calcWS");
				servCons.receive();
			}
				


			ins = new ByteArrayInputStream(soapMessage.getBytes("UTF-8"));
			XMLStreamReader parser = StAXUtils.createXMLStreamReader(ins);
			OMXMLParserWrapper builder = new StAXSOAPModelBuilder(parser, null);
			SOAPEnvelope envelope = (SOAPEnvelope) builder.getDocumentElement();
			SOAPBody body = envelope.getBody();
			Iterator itr = body.getChildElements();
			String ret = "nothing";
			while(itr.hasNext()){

				OMElement e = (OMElement)itr.next();
				String operation = e.getLocalName();

				Destination dest = session.createQueue(serviceName+"."+operation+".input");
				MessageProducer producer = session.createProducer(dest);
				TextMessage message = session.createTextMessage(soapMessage);
				message.setStringProperty("epr", "axis2/services/"+serviceName);
				String prefix = UUID.randomUUID().toString();
				prefix = prefix.substring(prefix.lastIndexOf('-'), prefix.length());
				message.setStringProperty("prefix", prefix);
				producer.send(message);

				Destination retDest = session.createQueue(prefix+"."+serviceName+"."+operation+".output");
				MessageConsumer consumer = session.createConsumer(retDest);
				javax.jms.Message msg = consumer.receive();
				TextMessage textMessage = (TextMessage) msg;

				return textMessage.getText();

				
			}
			return ret;
		} catch (JMSException ex) {
			Logger.getLogger(IllusionService.class.getName()).log(Level.SEVERE, null, ex);
		} catch (XMLStreamException ex) {
			Logger.getLogger(IllusionService.class.getName()).log(Level.SEVERE, null, ex);
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(IllusionService.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			//try {
			//	ins.close();
			//} catch (IOException ex) {
			//	Logger.getLogger(IllusionService.class.getName()).log(Level.SEVERE, null, ex);
			//}
		}
		return null;
	}
	
	



}
