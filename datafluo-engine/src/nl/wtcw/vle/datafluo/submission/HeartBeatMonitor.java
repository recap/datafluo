/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.submission;

import java.util.Iterator;
import nl.wtcw.vle.datafluo.util.GlobalConfiguration;
import nl.wtcw.vle.datafluo.core.task.VLETask;
import nl.wtcw.vle.datafluo.core.flow.Flow;
import nl.wtcw.vle.datafluo.core.task.TaskState;

/**
 *
 * @author reggie
 */
public class HeartBeatMonitor implements Runnable {

	private Flow flow;
	private boolean running;
	private static HeartBeatMonitor instance = null;

	public HeartBeatMonitor() {
		running = false;
		flow = GlobalConfiguration.getFlow();
	}

	public static HeartBeatMonitor getInstance(){
		if(instance == null){
			instance = new HeartBeatMonitor();
		}
		return instance;
	}

	public boolean isRunning(){
		return this.running;
	}

	public void run() {
		running = true;
		while (true) {
			try {
				Thread.sleep(60000);
				flow = GlobalConfiguration.getFlow();
				if (flow != null) {
					long ctime = (System.currentTimeMillis() / 1000);
					for (Iterator itr = flow.getTasks().iterator(); itr.hasNext();) {
						VLETask task = (VLETask) itr.next();

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
