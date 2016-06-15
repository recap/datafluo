/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.datafluo.main.ws;

import javax.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnection;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * An example class for demonstrating some of the basics behind Camel. This
 * example sends some text messages on to a JMS Queue, consumes them and
 * persists them to disk
 *
 * @version
 */
public final class CamelJmsToFileExample {

    private CamelJmsToFileExample() {
    }

    public static void main(String args[]) throws Exception {
        // START SNIPPET: e1
        CamelContext context = new DefaultCamelContext();
        // END SNIPPET: e1
        // Set up the ActiveMQ JMS Components
        // START SNIPPET: e2
        //ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
		//ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistant=true");
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        // Note we can explicity name the component

        context.addComponent("test-jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
        // END SNIPPET: e2
        // Add some configuration by hand ...
        // START SNIPPET: e3
        context.addRoutes(new RouteBuilder() {

            public void configure() {
                from("test-jms:queue:test.queue.in").to("file:///tmp/test_4");
                // set up a listener on the file component
               // from("file:///tmp/test_4").process(new Processor() {

                //    public void process(Exchange e) {
                ///        System.out.println("Received exchange: " + e.getIn());
                //    }
               // });
            }
        });
        // END SNIPPET: e3
        // Camel template - a handy class for kicking off exchanges
        // START SNIPPET: e4
        ProducerTemplate template = context.createProducerTemplate();
        // END SNIPPET: e4
        // Now everything is set up - lets start the context
        context.start();
        // Now send some test text to a component - for this case a JMS Queue
        // The text get converted to JMS messages - and sent to the Queue
        // test.queue
        // The file component is listening for messages from the Queue
        // test.queue, consumes
        // them and stores them to disk. The content of each file will be the
        // test we sent here.
        // The listener on the file component gets notified when new files are
        // found ... that's it!
        // START SNIPPET: e5
        for (int i = 0; i < 10; i++) {
            template.sendBody("test-jms:queue:test.queue.in", "Test Message: " + i);
        }
        // END SNIPPET: e5
        Thread.sleep(1000);
        context.stop();
    }
}
