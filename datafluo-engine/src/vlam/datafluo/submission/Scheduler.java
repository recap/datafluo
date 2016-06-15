/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.submission;

import java.util.Vector;
import java.util.concurrent.Semaphore;
import vlam.datafluo.utils.GlobalConfiguration;
import uk.ac.soton.itinnovation.freefluo.core.task.Task;

/**
 *
 * @author reggie
 */
public class Scheduler implements Runnable {

	protected final Vector runnableTasks = new Vector();
	protected Vector submitters;
	protected final Semaphore sem = new Semaphore(0, true);
	protected final Object threadSync = new Object();

	public void run() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	public void submitterAdded(ISubmitter submitter){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	public void addRunnableTask(Task task) {
		GlobalConfiguration.logging.log("ADDING TASK "+task.getName());
		synchronized (this.runnableTasks) {
			this.runnableTasks.add(task);
		}

		sem.release();
		GlobalConfiguration.logging.log("ADDED TASK "+task.getName());
	}
	public void requeueRunnableTask(Task task){
		//System.err.println("REQUEUE TASK "+task.getName());
		synchronized (this.runnableTasks) {
			this.runnableTasks.add(task);
			//this.runnableTasks.add(this.runnableTasks.indexOf(this.runnableTasks.lastElement()), task);
		}
		sem.release();
		//System.err.println("REQUEUED TASK "+task.getName());
	}
}
