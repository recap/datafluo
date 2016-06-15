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
public class DummyScheduler extends Scheduler{

	private static Logger logger = Logger.getLogger(DummyScheduler.class);

	public DummyScheduler(Context context){
		super(context);
		logger.debug("DummyScheduler.<init>");
	}

	@Override
	public void run(){
		try {
		while(true){
			sem.acquire();
			Task task = null;

			synchronized (this.runnableTasks) {
				task = (Task) this.runnableTasks.firstElement();
				this.runnableTasks.remove(task);
			}
			/*logger.debug("Activating output ports for: "+task.getName());
			for(Iterator itr = task.getOutputPorts().iterator(); itr.hasNext();){
				Port port = (Port)itr.next();
				port.activate(new DataEvent(port));
			}
			VLETask vtask = (VLETask)task;
			vtask.tryComplete();*/
		}
		} catch (InterruptedException ex) {
				logger.error(ex);
		}
	}
}
