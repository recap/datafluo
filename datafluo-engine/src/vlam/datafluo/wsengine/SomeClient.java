/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vlam.datafluo.wsengine;

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

//Rename this to EngineInstance
public class SomeClient implements IStateMonitor{
	public VlamEngine engine;

	public void flowStateChanged(FlowStateChangedEvent flowStateChangedEvent) {
		switch(flowStateChangedEvent.getState()){
			case FlowState.RUNNING_STATE:
			//	debug("FLOW: RUNNING");
				break;
			case FlowState.COMPLETE_STATE:
			//	debug("FLOW: COMPLETE");
				break;
			case FlowState.FAILED_STATE:
			//	debug("FLOW: FAILED");
				break;
			default:
			//	debug("FLOW: STATE UNSUPORTED");

		}
	}

	public void taskStateChanged(TaskStateChangedEvent taskStateChangedEvent) {
		VlamDatafluoTask task = (VlamDatafluoTask)taskStateChangedEvent.getTask();
		task.getInstanceID();
		switch(taskStateChangedEvent.getState()){
			case TaskState.NEW_STATE:
			//	debug("TASK: "+taskStateChangedEvent.getTask().getName()+":NEW" );
				break;
			case TaskState.COMPLETE_STATE:
			//	debug("TASK: "+taskStateChangedEvent.getTask().getName()+":COMPLETE" );
				break;
			case TaskState.FAILED_STATE:
			//	debug("TASK: "+taskStateChangedEvent.getTask().getName()+":FAILED" );
				break;
			case TaskState.RUNNING_STATE:
			//	debug("TASK: "+taskStateChangedEvent.getTask().getName()+":RUNNING" );
				break;
			default:
			//	debug("TASK: "+taskStateChangedEvent.getTask().getName()+":UNSUPPORTED" );
		}
	}

	public void portStateChanged(PortStateChangedEvent portStateChangedEvent) {

		switch(portStateChangedEvent.getState()){
			case PortState.NEW_STATE:
			//	debug("PORT: "+portStateChangedEvent.getPort().getName()+":NEW");
				break;
			case PortState.ACTIVE_STATE:
			//	debug("PORT: "+portStateChangedEvent.getPort().getName()+":ACTIVE");
				break;
			case PortState.DESTROYED_STATE:
			//	debug("PORT: "+portStateChangedEvent.getPort().getName()+":DESTROYED");
				break;
			case PortState.ENABLED_STATE:
			//	debug("PORT: "+portStateChangedEvent.getPort().getName()+":ENABLED");
				break;
			default:
			//	debug("PORT: "+portStateChangedEvent.getPort().getName()+":UNSUPPORTED" );
		}
	}

	private void debug(String msg) {
        System.err.println(this.getClass().getName() +  ": " + msg);
    }

}
