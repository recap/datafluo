/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.submission;

import java.util.Vector;
import java.util.concurrent.Semaphore;
import nl.wtcw.vle.datafluo.context.Context;
import nl.wtcw.vle.datafluo.util.GlobalConfiguration;
import nl.wtcw.vle.datafluo.core.task.Task;
import org.apache.log4j.Logger;

/**
 *
 * @author reggie
 */
public class Scheduler implements Runnable {

	private static Logger logger = Logger.getLogger(Scheduler.class);
	protected final Vector runnableTasks = new Vector();
	protected Vector submitters;
	protected Context context;
	protected final Semaphore sem = new Semaphore(0, true);
	protected final Object threadSync = new Object();

	public Scheduler(Context context){
		this.context = context;
	}

	public void run() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	public void submitterAdded(ISubmitter submitter){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	public void addRunnableTask(Task task) {
		logger.debug("ADDING TASK "+task.getName());
		synchronized (this.runnableTasks) {
			this.runnableTasks.add(task);
		}

		sem.release();
		logger.debug("ADDED TASK "+task.getName());
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
