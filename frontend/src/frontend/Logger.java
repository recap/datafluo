/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.DestinationSource;

/**
 *
 * @author reggie
 */
public class Logger implements MessageListener{

	FileWriter fstream;
	BufferedWriter out;
	
	public Logger() throws JMSException, IOException{
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("failover://tcp://elab.science.uva.nl:61616");
		ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection();
		connection.start();
		//DestinationSource ds = new DestinationSource(connection);
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination dest = session.createQueue("global.events");
		MessageConsumer consumer = session.createConsumer(dest);
		fstream = new FileWriter("events.log");
		out = new BufferedWriter(fstream);
		consumer.setMessageListener(this);
	}
	public void onMessage(Message msg) {
		try {

			TextMessage tmsg = (TextMessage) msg;
			out.write("jmstimestamp:"+Long.toString(msg.getJMSTimestamp())+" "+tmsg.getText()+"\n");
			out.flush();
		} catch (IOException ex) {
			java.util.logging.Logger.getLogger(Logger.class.getName()).log(Level.SEVERE, null, ex);
		} catch (JMSException ex) {
			java.util.logging.Logger.getLogger(Logger.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
