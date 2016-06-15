/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.submission;

import java.util.Iterator;
import vlam.datafluo.utils.GlobalConfiguration;
import vlam.datafluo.wsengine.VlamDatafluoTask;
import uk.ac.soton.itinnovation.freefluo.core.flow.Flow;
import uk.ac.soton.itinnovation.freefluo.core.task.TaskState;

/**
 *
 * @author reggie
 */
public class HeartBeatMonitor implements Runnable {

	private Flow flow;

	public HeartBeatMonitor() {
		flow = GlobalConfiguration.getFlow();
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(60000);
				flow = GlobalConfiguration.getFlow();
				if (flow != null) {
					long ctime = (System.currentTimeMillis() / 1000);
					for (Iterator itr = flow.getTasks().iterator(); itr.hasNext();) {
						VlamDatafluoTask task = (VlamDatafluoTask) itr.next();

						if ((task.getState() == TaskState.RUNNING) && (task.isWaiting() == false) && (task.getLastHeartBeat() != 0)) {
							if ((ctime - task.getLastHeartBeat()) > 60) {
								//GlobalConfiguration.logging.debug(task.getName() +" last heart beat: " + (ctime - task.getLastHeartBeat()) +"\n" );
								task.resubmit();
							}//if
						}//if
					}//for
				}//if flow = null
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}//while
	}
}
