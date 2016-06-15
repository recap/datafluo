/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.submission;

import vlam.datafluo.wsengine.*;

/**
 *
 * @author reggie
 */
public class SubmissionScheduler extends Scheduler {

	
	public SubmissionScheduler() {
	}

	@Override
	public void run() {

		try {

			while (true) {
				sem.acquire();
				synchronized (this.threadSync) {
					VlamDatafluoTask task = (VlamDatafluoTask) this.runnableTasks.firstElement();
					this.runnableTasks.remove(task);
					System.err.println("Removed task: " + task.getName());
					gMinionSubmitter gs = new gMinionSubmitter();
					gs.submit("");
				}
			}//while
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}


	}
}
