/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.datafluo.main.ws;


import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.memory.buffer.MessageQueue;

/**
 *
 * @author reggie
 */
public class HarnessListener implements MessageListener {

	public HarnessListener(String serverURL, String topic) throws JMSException, Exception{
		ActiveMQConnectionFactory connectionFactory =  new ActiveMQConnectionFactory(serverURL);
        ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection();
        connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createQueue("global.config");
		MessageProducer producer = session.createProducer(destination);
		TextMessage message = session.createTextMessage("some message");
		producer.send(message);
		//consumer.setMessageListener(this);
	}

	public void onMessage(Message msg) {
		TextMessage txt = (TextMessage)msg;
		try {
			System.out.println(txt.getText());
		} catch (JMSException ex) {
			Logger.getLogger(HarnessListener.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void main(String[] args) throws JMSException, Exception{
		HarnessListener hs = new HarnessListener(ActiveMQConnection.DEFAULT_BROKER_URL, "default.config");
	}

}
