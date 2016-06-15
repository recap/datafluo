/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.datafluo.submission;

import java.util.Iterator;
import org.apache.log4j.Logger;
import nl.wtcw.vle.datafluo.context.Context;
import nl.wtcw.vle.datafluo.core.event.DataEvent;
import nl.wtcw.vle.datafluo.core.event.DataEventSource;
import nl.wtcw.vle.datafluo.core.port.Port;
import nl.wtcw.vle.datafluo.core.port.VLEPort;
import nl.wtcw.vle.datafluo.core.task.AbstractTask;
import nl.wtcw.vle.datafluo.core.task.Task;
import nl.wtcw.vle.datafluo.core.task.VLETask;

/**
 *
 * @author reggie
 */
public class FifoLocalScheduler extends Scheduler{

	private static Logger logger = Logger.getLogger(FifoLocalScheduler.class);

	public FifoLocalScheduler(Context context){
		super(context);
		logger.debug("FifoLocalScheduler.<init>");
	}

	@Override
	public void run(){
		LocalSubmitter submitter = new LocalSubmitter();
		try {
		while(true){
			sem.acquire();
			Task task = null;

			synchronized (this.runnableTasks) {
				task = (Task) this.runnableTasks.firstElement();
				this.runnableTasks.remove(task);
			}
			submitter.submit((VLETask)task);
			
		}
		} catch (InterruptedException ex) {
				logger.error(ex);
		}
	}
}
