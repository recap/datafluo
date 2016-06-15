/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package submitter;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author reggie
 */
public class LoadCalculator implements Runnable {

	ResourceManager resourceManager = null;
	private ActiveMQConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private Destination destination;
	private MessageProducer producer;
	private int pri = 0;

	public LoadCalculator(String brokerURL) throws JMSException{

		this.resourceManager = ResourceManager.getClassInstance();
		connectionFactory = new ActiveMQConnectionFactory(brokerURL);
		connection = connectionFactory.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		destination = session.createQueue("global.resources");
		producer = session.createProducer(destination);
		//producer.setPriority(0);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		producer.setTimeToLive(10000);
	}



	public void run() {
		

		while(true){
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ex) {
				Logger.getLogger(LoadCalculator.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			Iterator itr = resourceManager.getSubmitters().iterator();
			int free = 0;
			int slots = 0;
			while(itr.hasNext()){
				ISubmitter is = (ISubmitter)itr.next();
				slots += is.getTotalSlots();
				free += is.getAvailableSlots();

			}
			//System.out.println("Things: "+Integer.toString(free)+" "+Integer.toString(slots));

			double load = 0;
			
			if((free != 0) &&  (slots != 0))
				load =  ((double)slots - (double)free)/(double)slots;
			else
				continue;

			System.out.println("Load: "+Double.toString(load));
			try {		
				
				MapMessage mmsg = session.createMapMessage();
				mmsg.setDouble("load", load);
				//producer.setPriority(pri);
				producer.send(mmsg);
				//pri++;
				//if(pri > 9)
				//pri = 0;
				
			} catch (JMSException ex) {
				Logger.getLogger(LoadCalculator.class.getName()).log(Level.SEVERE, null, ex);
			}

		}
	}

}
