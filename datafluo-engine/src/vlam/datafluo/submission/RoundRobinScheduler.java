/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.submission;

import java.util.Iterator;
import java.util.Vector;
import vlam.datafluo.utils.GlobalConfiguration;
import vlam.datafluo.wsengine.VlamDatafluoTask;

/**
 *
 * @author reggie
 */
public class RoundRobinScheduler extends Scheduler {

	private Vector ratios;
	private int alt = 0;
	private int serverListSize = 0;
	private int ratioCounter = 0;
	private QueueMonitorServer monitorServer = new QueueMonitorServer();

	public RoundRobinScheduler() {
		super();
		Thread t = new Thread(monitorServer);
		t.start();
		this.submitters = (Vector) GlobalConfiguration.getSubmitters();
		this.ratios = new Vector();
		//Collections.sort(submitters);
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

	private ISubmitter getNextSubmitter() {
		double ratio = Double.parseDouble((String) this.ratios.elementAt(alt));
		if (ratioCounter < ratio) {
			ratioCounter++;
			return (ISubmitter) submitters.elementAt(alt);
		} else {
			ratioCounter = 0;
			int slots = 0;
			int counter = 0;
			do {
				alt++;
				alt = alt % serverListSize;
				ISubmitter is = (ISubmitter) submitters.elementAt(alt);
				slots = is.getAvailableSlots();
				GlobalConfiguration.logging.log("SLOTS: " + is.getName() + " " + is.getAvailableSlots());
				counter++;
			} while (slots <= 0);
			ratioCounter++;
			return (ISubmitter) submitters.elementAt(alt);
		}

	}

	private void calcRatios() {
		ISubmitter temp_is = null;
		for (Iterator itr = submitters.iterator(); itr.hasNext();) {
			ISubmitter is = (ISubmitter) itr.next();
			if (is.getAvailableSlots() > 0) {
				if (temp_is == null) {
					temp_is = is;
				} else {
					if (is.getAvailableSlots() < temp_is.getAvailableSlots()) {
						temp_is = is;
					}
				}//else
			}//if > 0
		}//for

		for (Iterator itr = submitters.iterator(); itr.hasNext();) {
			ISubmitter is = (ISubmitter) itr.next();
			double ratio = Math.ceil(is.getAvailableSlots() / temp_is.getAvailableSlots());
			this.ratios.add(Double.toString(ratio));
			GlobalConfiguration.logging.log("RATIO: " + is.getName() + " " + Double.toString(ratio));
		}//for

	}

	@Override
	public void run() {
		boolean allSubmittersReady = true;
		try {
			Thread.sleep(30000);
			//wait until all submitters have reported stats
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

			this.serverListSize = this.submitters.size();
			
			this.calcRatios();

			while (true) {

				sem.acquire();
				//synchronized (this.threadSync) {
				VlamDatafluoTask task;
				synchronized (this.runnableTasks) {
					task = (VlamDatafluoTask) this.runnableTasks.firstElement();
					GlobalConfiguration.logging.log("scheduling task: " + task.getName());
					this.runnableTasks.remove(task);
				}
					ISubmitter is = null;

					if ((task.getHost().contentEquals("any")) || (task.getHost().isEmpty())) {
						GlobalConfiguration.logging.log("submitter binding to any: "+task.getName());
						is = (ISubmitter) getNextSubmitter();
					} else {

						String[] spl_temp = task.getHost().toString().split("\\.");
						System.err.print(spl_temp.length);
						is = (ISubmitter) getSubmitterByName(spl_temp[0].toString());
					}


					System.err.println("submitting task: " + task.getName() + " to " + is.getName());
					GlobalConfiguration.logging.log("submitting task: " + task.getName() + " to " + is.getName());
					//GlobalConfiguration.logging.log("HOST "+ is.getName()+ " " + task.getName());
					task.setHost(is.getName());
					GlobalConfiguration.logging.log(task.getName() + " " + task.getTaskId() + " START_TIME: " + task.getStartTime().getShortString() + " " + task.getHost());
					is.decSlot();
					is.submit(task);

				//}//sync
			}//while

		//} catch (InterruptedException ex) {
		//	ex.printStackTrace();
		//}

	}catch (Exception ex) {
			ex.printStackTrace();
		}

}
}
