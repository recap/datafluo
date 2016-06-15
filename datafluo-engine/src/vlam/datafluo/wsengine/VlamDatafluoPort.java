/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.wsengine;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.soton.itinnovation.freefluo.core.port.*;
import uk.ac.soton.itinnovation.freefluo.core.task.*;
import vlam.datafluo.messaging.*;
import vlam.datafluo.utils.GlobalConfiguration;

/**
 *
 * @author reggie
 */
public class VlamDatafluoPort extends Port implements IQueueEventListener, Cloneable {

	private Queue portQueue = null;
	private Queue shadowQueue = null;
	private Message lastMessage = null;
	private String Parameters = "null?";
	public final Object Sync = new Object();
	private String instanceID;

	public VlamDatafluoPort(String portId, String name, int direction, Task task) {
		super(portId, name, direction, task);
		VlamDatafluoTask ltask = (VlamDatafluoTask) task;
		//this.portId = portId + "." + Integer.toString(ltask.getCloneNumber());
		//GlobalConfiguration.logging.debug("PORTID: "+ this.portId);
		this.addPortStateListener(GlobalConfiguration.stateMonitor);
		for (Iterator itr = GlobalConfiguration.getStateMonitors().iterator(); itr.hasNext();) {
			IStateMonitor sm = (IStateMonitor) itr.next();
			this.addPortStateListener(sm);
		}
	}

	public VlamDatafluoPort(String portId, int direction, Task task) {
		super(portId, "", direction, task);
		VlamDatafluoTask ltask = (VlamDatafluoTask) task;
		//this.portId = portId + "." + Integer.toString(ltask.getCloneNumber());
		//GlobalConfiguration.logging.debug("PORTID: "+ this.portId);
		this.addPortStateListener(GlobalConfiguration.stateMonitor);
		for (Iterator itr = GlobalConfiguration.getStateMonitors().iterator(); itr.hasNext();) {
			IStateMonitor sm = (IStateMonitor) itr.next();
			this.addPortStateListener(sm);
		}
	}

	public String getInstanceID() {
		return this.instanceID;
	}

	public void setInstanceID(String instanceID) {
		this.instanceID = instanceID;
	}

	@Override
	public String getName() {
		return this.portId;

	}

	public VlamDatafluoPort cloneMe() throws CloneNotSupportedException {
		VlamDatafluoPort clone = (VlamDatafluoPort) this.clone();
		clone.lastMessage = null;
		return clone;
	}

	public Message getLastMessage() {
		return this.lastMessage;
	}

	public void setShadowQueue(Queue queue) {
		this.shadowQueue = queue;
	}

	public Queue getShadowQueue() {
		return this.shadowQueue;
	}

	public void setQueue(Queue queue) {
		queue.addEventListener(this);
		this.portQueue = queue;
	}

	public Queue getQueue() {
		return this.portQueue;
	}

	public void setParameters(String params) {
		this.Parameters = params;
	}

	public String getParameters() {
		return this.Parameters;

	}

	public synchronized void queueRaisedEvent(QueueRaisedEvent queueRaisedEvent) {
		Message m = queueRaisedEvent.getMessageObject();
		VlamDatafluoTask ltask = (VlamDatafluoTask) this.task;
		if ((m.getAux() == ltask.getCloneNumber()) || (m.getAux() == -1)) {
			if (queueRaisedEvent.getEvent() == Queue.MESSAGE_RECEIVED_EVENT) {
				ltask.portReceiveMessage(this);
			}
			if (queueRaisedEvent.getEvent() == Queue.MESSAGE_CONSUMED_EVENT) {
				if (ltask.getName().contains("Histogram") == true)
					GlobalConfiguration.logging.debug("AUX: "+ltask.getTaskId()+"@"
							+Integer.toString(ltask.getCloneNumber())+" "+Integer.toString(m.getAux()));

				if (this.direction == Port.DIRECTION_IN) {
					//GlobalConfiguration.logging.debug("AUX: "+ Integer.toString(m.getAux())
					//		+" "+Integer.toString(ltask.getCloneNumber())+" "+ltask.getName());
					this.lastMessage = queueRaisedEvent.getMessageObject();
				}
				ltask.portConsumedMessage(this);
			}
		}
	}
}
