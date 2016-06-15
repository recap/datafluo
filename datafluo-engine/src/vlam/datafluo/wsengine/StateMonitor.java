/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vlam.datafluo.wsengine;

import vlam.datafluo.utils.GlobalConfiguration;
import uk.ac.soton.itinnovation.freefluo.core.event.FlowStateChangedEvent;
import uk.ac.soton.itinnovation.freefluo.core.event.PortStateChangedEvent;
import uk.ac.soton.itinnovation.freefluo.core.event.TaskStateChangedEvent;
import uk.ac.soton.itinnovation.freefluo.core.flow.FlowState;
import uk.ac.soton.itinnovation.freefluo.core.port.PortState;
import uk.ac.soton.itinnovation.freefluo.core.task.TaskState;

/**
 *
 * @author reggie
 */
public class StateMonitor implements IStateMonitor{

	public void flowStateChanged(FlowStateChangedEvent flowStateChangedEvent) {
		switch(flowStateChangedEvent.getState()){
			case FlowState.RUNNING_STATE:
				GlobalConfiguration.logging.debug("FLOW: RUNNING");
				break;
			case FlowState.COMPLETE_STATE:
				GlobalConfiguration.logging.debug("FLOW: COMPLETE");
				//System.exit(0);
				break;
			case FlowState.FAILED_STATE:
				GlobalConfiguration.logging.debug("FLOW: FAILED");
				break;
			default:
				GlobalConfiguration.logging.debug("FLOW: STATE UNSUPORTED");

		}
	}

	public void taskStateChanged(TaskStateChangedEvent taskStateChangedEvent) {
		VlamDatafluoTask task = (VlamDatafluoTask)taskStateChangedEvent.getTask();
		task.getInstanceID();
		switch(taskStateChangedEvent.getState()){
			case TaskState.NEW_STATE:
				GlobalConfiguration.logging.debug("TASK: "+taskStateChangedEvent.getTask().getName()+":NEW" );
				break;
			case TaskState.COMPLETE_STATE:
				GlobalConfiguration.logging.debug("TASK: "+taskStateChangedEvent.getTask().getName()+":COMPLETE" );
				break;
			case TaskState.FAILED_STATE:
				GlobalConfiguration.logging.debug("TASK: "+taskStateChangedEvent.getTask().getName()+":FAILED" );
				break;
			case TaskState.RUNNING_STATE:
				GlobalConfiguration.logging.debug("TASK: "+taskStateChangedEvent.getTask().getName()+":RUNNING" );
				break;
			default:
				GlobalConfiguration.logging.debug("TASK: "+taskStateChangedEvent.getTask().getName()+":UNSUPPORTED" );
		}
	}

	public void portStateChanged(PortStateChangedEvent portStateChangedEvent) {

		switch(portStateChangedEvent.getState()){
			case PortState.NEW_STATE:
				GlobalConfiguration.logging.debug("PORT: "+portStateChangedEvent.getPort().getName()+":NEW");
				break;
			case PortState.ACTIVE_STATE:
				GlobalConfiguration.logging.debug("PORT: "+portStateChangedEvent.getPort().getName()+":ACTIVE");
				break;
			case PortState.DESTROYED_STATE:
				GlobalConfiguration.logging.debug("PORT: "+portStateChangedEvent.getPort().getName()+":DESTROYED");
				break;
			case PortState.ENABLED_STATE:
				GlobalConfiguration.logging.debug("PORT: "+portStateChangedEvent.getPort().getName()+":ENABLED");
				break;
			default:
				GlobalConfiguration.logging.debug("PORT: "+portStateChangedEvent.getPort().getName()+":UNSUPPORTED" );
		}
	}
}
