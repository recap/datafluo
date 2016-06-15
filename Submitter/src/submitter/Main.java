/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package submitter;

import javax.jms.JMSException;

/**
 *
 * @author reggie
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws JMSException {
        ResourceManager rm = ResourceManager.getClassInstance();

		Thread t = new Thread(new QueueMonitorServer());
		t.start();

		LoadCalculator c = new LoadCalculator("failover://tcp://elab.science.uva.nl:61616");
		Thread t2 = new Thread(c);
		t2.start();
		/*GramSubmitter fs2 = new GramSubmitter("fs2","fs2.das3.science.uva.nl",0,null);
		GramSubmitter fs4 = new GramSubmitter("fs4","fs4.das3.science.uva.nl",0,null);
		GramSubmitter fs0 = new GramSubmitter("fs0","fs0.das3.science.uva.nl",0,null);
		GramSubmitter fs1 = new GramSubmitter("fs1","fs1.das3.science.uva.nl",0,null);
		GramSubmitter fs3 = new GramSubmitter("fs3","fs3.das3.science.uva.nl",0,null);

		
		//rm.addSubmitter(fs0);
		//rm.addSubmitter(fs1);
		rm.addSubmitter(fs2);
		//rm.addSubmitter(fs3);
		rm.addSubmitter(fs4);*/

		RunQueueListener rl = new RunQueueListener("failover://tcp://elab.science.uva.nl:61616","global.runqueue");

    }

}
