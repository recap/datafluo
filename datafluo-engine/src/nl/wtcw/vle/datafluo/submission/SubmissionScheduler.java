/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.submission;

import nl.wtcw.vle.datafluo.context.Context;
import nl.wtcw.vle.datafluo.core.task.VLETask;

/**
 *
 * @author reggie
 */
public class SubmissionScheduler extends Scheduler {

	
	public SubmissionScheduler(Context context) {
		super(context);
	}

	@Override
	public void run() {

		try {

			while (true) {
				sem.acquire();
				synchronized (this.threadSync) {
					VLETask task = (VLETask) this.runnableTasks.firstElement();
					this.runnableTasks.remove(task);
					System.err.println("Removed task: " + task.getName());
				//	gMinionSubmitter gs = new gMinionSubmitter();
				//	gs.submit("");
				}
			}//while
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}


	}
}
