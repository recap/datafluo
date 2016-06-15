/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.event;

import nl.wtcw.vle.datafluo.event.IStateMonitor;
import nl.wtcw.vle.datafluo.core.task.VLETask;
import nl.wtcw.vle.datafluo.util.GlobalConfiguration;
import nl.wtcw.vle.datafluo.core.event.FlowStateChangedEvent;
import nl.wtcw.vle.datafluo.core.event.PortStateChangedEvent;
import nl.wtcw.vle.datafluo.core.event.TaskStateChangedEvent;
import nl.wtcw.vle.datafluo.core.flow.FlowState;
import nl.wtcw.vle.datafluo.core.port.PortState;
import nl.wtcw.vle.datafluo.core.task.TaskState;
import org.apache.log4j.Logger;

/**
 *
 * @author reggie
 */
public class DefaultStateMonitor implements IStateMonitor {

	private static Logger logger = Logger.getLogger(DefaultStateMonitor.class);

	public DefaultStateMonitor(){
		logger.debug("DefaultStateMonitor.<init>");
	}

	public void flowStateChanged(FlowStateChangedEvent flowStateChangedEvent) {

		switch (flowStateChangedEvent.getState()) {
			case FlowState.RUNNING_STATE:
				logger.debug("FLOW: RUNNING");
				GlobalConfiguration.startTime = System.currentTimeMillis();
				break;
			case FlowState.COMPLETE_STATE:
				//logger.debug("FLOW: COMPLETE");
				GlobalConfiguration.stopTime = (System.currentTimeMillis() - GlobalConfiguration.startTime) / 1000;
				logger.debug("FLOW COMPLETED IN: " + Long.toString(GlobalConfiguration.stopTime)
						+ "s");	
				break;
			case FlowState.FAILED_STATE:
				logger.debug("FLOW: FAILED");
				break;
			default:
				logger.debug("FLOW: STATE UNSUPORTED");

		}
	}

	public void taskStateChanged(TaskStateChangedEvent taskStateChangedEvent) {
		VLETask task = (VLETask) taskStateChangedEvent.getTask();		
		switch (taskStateChangedEvent.getState()) {
			case TaskState.NEW_STATE:
				logger.debug("TASK: " + taskStateChangedEvent.getTask().getName() + ":NEW");
				break;
			case TaskState.COMPLETE_STATE:
				logger.debug("TASK: " + taskStateChangedEvent.getTask().getName() + ":COMPLETE");
				break;
			case TaskState.FAILED_STATE:
				logger.debug("TASK: " + taskStateChangedEvent.getTask().getName() + ":FAILED");
				break;
			case TaskState.RUNNING_STATE:
				logger.debug("TASK: " + taskStateChangedEvent.getTask().getName() + ":RUNNING");
				break;
			default:
				logger.debug("TASK: " + taskStateChangedEvent.getTask().getName() + ":UNSUPPORTED");
		}
	}

	public void portStateChanged(PortStateChangedEvent portStateChangedEvent) {

		switch (portStateChangedEvent.getState()) {
			case PortState.NEW_STATE:
				//logger.debug("PORT: " + portStateChangedEvent.getPort().getName() + ":NEW");
				break;
			case PortState.ACTIVE_STATE:
				//logger.debug("PORT: " + portStateChangedEvent.getPort().getName() + ":ACTIVE");
				break;
			case PortState.DESTROYED_STATE:
				//logger.debug("PORT: " + portStateChangedEvent.getPort().getName() + ":DESTROYED");
				break;
			case PortState.ENABLED_STATE:
				//logger.debug("PORT: " + portStateChangedEvent.getPort().getName() + ":ENABLED");
				break;
			default:
				//logger.debug("PORT: " + portStateChangedEvent.getPort().getName() + ":UNSUPPORTED");
		}
	}		 
}
