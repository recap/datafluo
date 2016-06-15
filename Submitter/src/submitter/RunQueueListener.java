/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package submitter;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author reggie
 */
public class RunQueueListener implements MessageListener {

	private ActiveMQConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private MessageConsumer messageConsumer;
	private Destination inputDestination;
	private String brokerURL;
	private String runQueue;
	private ArrayList<Message> mLst = new ArrayList();

	public RunQueueListener(String brokerURL, String queue){
		try {
			this.brokerURL = brokerURL;
			this.runQueue = queue;
			connectionFactory = new ActiveMQConnectionFactory(this.brokerURL);
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			inputDestination = session.createQueue(runQueue);
			messageConsumer = session.createConsumer(inputDestination);
			messageConsumer.setMessageListener(this);
		} catch (JMSException ex) {
			Logger.getLogger(RunQueueListener.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	public void onMessage(Message message) {
		try {

			//submit harness
			ResourceManager rm = ResourceManager.getClassInstance();



				ISubmitter sub = rm.getASubmitter();

				while(sub == null){
					sub = rm.getASubmitter();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					Logger.getLogger(RunQueueListener.class.getName()).log(Level.SEVERE, null, ex);
				}
				}


				sub.submit("");
				//submit service
				Destination dest = session.createQueue("global.services");
				MessageProducer producer = session.createProducer(dest);
				producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				producer.setTimeToLive(10000);
				producer.send(message);
				producer.close();

			
		} catch (JMSException ex) {
			Logger.getLogger(RunQueueListener.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
