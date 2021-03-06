/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.submission;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import nl.wtcw.vle.datafluo.context.Context;
import nl.wtcw.vle.datafluo.util.GlobalConfiguration;
import nl.wtcw.vle.datafluo.core.task.VLETask;
import org.apache.log4j.Logger;

/**
 *
 * @author reggie
 */
public class InteractiveScheduler extends Scheduler {

	private static Logger logger = Logger.getLogger(InteractiveScheduler.class);
	private QueueMonitorServer monitorServer = new QueueMonitorServer();
	private int serverListSize = 0;
	private Vector localQueue = new Vector();

	public InteractiveScheduler(Context context) {
		super(context);
		Thread t = new Thread(monitorServer);
		t.start();
		this.submitters = (Vector) GlobalConfiguration.getSubmitters();
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

		Collections.sort(submitters);
		for (Iterator itr = submitters.iterator(); itr.hasNext();) {
			ISubmitter is = (ISubmitter) itr.next();
			if ((is.getAvailableSlots() > 0) && (is.getMetric() > 0)) {
				return is;
			}
		}
		return null;

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
				if (allSubmittersReady == true) {
					break;
				}
				Thread.sleep(1000);
			}

			while (true) {
				sem.acquire();
				VLETask task = null;
				ISubmitter is = null;
				synchronized (this.threadSync) {
					task = (VLETask) this.runnableTasks.firstElement();
					this.runnableTasks.remove(task);
				}//sync
				do {

					if ((task.getHost().contentEquals("any")) || (task.getHost().isEmpty())) {
						is = (ISubmitter) getBestSubmitter();
					} else {
						String[] spl_temp = task.getHost().toString().split("\\.");
						is = (ISubmitter) getSubmitterByName(spl_temp[0].toString());
					}
					if (is == null) {
						Thread.sleep(1000);
					}

				} while (is == null);


				task.setHost(is.getName());
				logger.debug(task.getName() + " " + task.getTaskId() + " START_TIME: " + task.getStartTime().getShortString() + " " + task.getHost());
				is.decSlot();
				is.submit(task);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
