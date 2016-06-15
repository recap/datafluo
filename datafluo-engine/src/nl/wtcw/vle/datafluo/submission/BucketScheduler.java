/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.submission;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import nl.wtcw.vle.datafluo.context.Context;
import nl.wtcw.vle.datafluo.submission.SimpleGramSubmitter;
import nl.wtcw.vle.datafluo.util.GlobalConfiguration;
import nl.wtcw.vle.datafluo.core.task.VLETask;
import org.apache.log4j.Logger;

/**
 *
 * @author reggie
 */
public class BucketScheduler extends Scheduler {
	private static Logger logger = Logger.getLogger(BucketScheduler.class);
	private int alt = 0;
	private int serverListSize = 0;
	private QueueMonitorServer monitorServer = new QueueMonitorServer();

	public BucketScheduler(Context context) {
		super(context);
		Thread t = new Thread(monitorServer);
		t.start();
		this.submitters = (Vector) GlobalConfiguration.getSubmitters();
		Collections.sort(submitters);
		this.serverListSize = this.submitters.size();
	}

	private ISubmitter getSubmitterByName(String submitterName) {
		for (Iterator itr = submitters.iterator(); itr.hasNext();) {
			ISubmitter is = (ISubmitter) itr.next();
			if (is.getName().contains(submitterName)) {
				return is;
			}
		}
		return (ISubmitter) submitters.firstElement();
	}

	private ISubmitter getBestSubmitter() {
		for (Iterator itr = submitters.iterator(); itr.hasNext();) {
			ISubmitter is = (ISubmitter) itr.next();
			if (is.getAvailableSlots() > 0) {
				is.decSlot();
				return is;
			}
		}
		return (ISubmitter) submitters.firstElement();
	}

	@Override
	public void run() {
		boolean allSubmittersReady = true;
		try {
			//wait untill all submitters have reported stats
			while (true) {
				allSubmittersReady = true;
				for (Iterator itr = submitters.iterator(); itr.hasNext();) {
					ISubmitter is = (ISubmitter) itr.next();
					if (is.isReady() == false) {
						allSubmittersReady = false;
					}
				}
				if ((allSubmittersReady == true) && (submitters.size() > 0)) {
					break;
				}
				Thread.sleep(1000);
			}

			Collections.sort(submitters);

			while (true) {

				sem.acquire();
				VLETask task = null;

				synchronized (this.runnableTasks) {
					task = (VLETask) this.runnableTasks.firstElement();
					this.runnableTasks.remove(task);
				}
				//Collections.sort(submitters);
				//SimpleGramSubmitter sgs = (SimpleGramSubmitter)submitters.elementAt(alt);
				ISubmitter is = null;


				if ((task.getHost().contentEquals("any")) || (task.getHost().isEmpty())) {
					is = (ISubmitter) getBestSubmitter();
				} else {

					String[] spl_temp = task.getHost().toString().split("\\.");
					//System.err.print(spl_temp.length);
					is = (ISubmitter) getSubmitterByName(spl_temp[0].toString());
				}

				System.err.println("submitting task: " + task.getName() + " to " + is.getName());
				//logger.log("HOST "+ is.getName()+ " " + task.getName());
				task.setHost(is.getName());
				logger.debug(task.getName() + " " + task.getTaskId() + " START_TIME: " + task.getStartTime().getShortString() + " " + task.getHost());
				is.submit(task);

			}//while
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

	}
}
