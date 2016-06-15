/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.core.port;

import java.util.Iterator;
import nl.wtcw.vle.datafluo.context.Context;
import nl.wtcw.vle.datafluo.core.task.*;
import nl.wtcw.vle.datafluo.messaging.*;
import nl.wtcw.vle.datafluo.util.GlobalConfiguration;
import nl.wtcw.vle.datafluo.event.IStateMonitor;
import nl.wtcw.vle.datafluo.core.task.VLETask;
import org.apache.log4j.Logger;

/**
 *
 * @author reggie
 */
public class VLEPort extends Port implements IQueueEventListener, Cloneable {

	private static Logger logger = Logger.getLogger(VLEPort.class);

	private Queue portQueue = null;
	private Queue shadowQueue = null;
	private Message lastMessage = null;
	private String Parameters = "null?";
	public final Object Sync = new Object();
	@Deprecated
	private String instanceID;
	private Context context = null;

	public VLEPort(String portId, String name, int direction, Task task) {
		super(portId, name, direction, task);
		VLETask vleTask = (VLETask) task;
		context = vleTask.getContext();
		
		for (Iterator itr = context.getStateMonitors().iterator(); itr.hasNext();) {
			IStateMonitor sm = (IStateMonitor) itr.next();
			this.addPortStateListener(sm);
		}
	}

	public VLEPort(String portId, int direction, Task task) {
		super(portId, "", direction, task);
		VLETask vleTask = (VLETask) task;
		context = vleTask.getContext();

		for (Iterator itr = context.getStateMonitors().iterator(); itr.hasNext();) {
			IStateMonitor sm = (IStateMonitor) itr.next();
			this.addPortStateListener(sm);
		}
	}

	@Deprecated
	public String getInstanceID() {
		return this.instanceID;
	}
	
	@Deprecated
	public void setInstanceID(String instanceID) {
		this.instanceID = instanceID;
	}

	@Override
	public String getName() {
		return this.portId;

	}

	public VLEPort cloneMe() throws CloneNotSupportedException {
		VLEPort clone = (VLEPort) this.clone();
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
		VLETask ltask = (VLETask) this.task;
		if ((m.getAux() == ltask.getCloneNumber()) || (m.getAux() == -1)) {
			if (queueRaisedEvent.getEvent() == Queue.MESSAGE_RECEIVED_EVENT) {
				ltask.portReceiveMessage(this);
			}
			if (queueRaisedEvent.getEvent() == Queue.MESSAGE_CONSUMED_EVENT) {
				if (ltask.getName().contains("Histogram") == true)
					logger.debug("AUX: "+ltask.getTaskId()+"@"
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
