package vlam.datafluo.wsengine;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import nl.wtcw.vle.wfd.ModuleI;
import nl.wtcw.vle.wfd.ParameterI;
import uk.ac.soton.itinnovation.freefluo.core.event.RunEvent;
import uk.ac.soton.itinnovation.freefluo.core.event.DataEvent;
import uk.ac.soton.itinnovation.freefluo.core.event.TaskStateChangedEvent;
import uk.ac.soton.itinnovation.freefluo.core.flow.Flow;
import uk.ac.soton.itinnovation.freefluo.core.task.AbstractTask;
import vlam.datafluo.messaging.*;
import vlam.datafluo.submission.*;
import vlam.datafluo.utils.GlobalConfiguration;
import uk.ac.soton.itinnovation.freefluo.core.event.PortStateChangedEvent;
import uk.ac.soton.itinnovation.freefluo.core.port.Port;
import uk.ac.soton.itinnovation.freefluo.core.port.PortState;
import uk.ac.soton.itinnovation.freefluo.core.task.TaskState;

public class VlamDatafluoTask extends AbstractTask implements Cloneable {

	private ModuleI module;
	private String TaskVersion = "0.1";
	private String SoLibUrl = "";
	private int farm = -1;
	private VlamDatafluoPort farmedPort = null;
	private int farmType = 3; //0-auto, 1-fixed, 2-one-to-one, 3-no-farming
	private String host = "any";
	private boolean fixedHost = false;
	private int runCounter = 0;	
	private String HOME = GlobalConfiguration.home;
	private LinkedList clones = new LinkedList();
	protected VlamDatafluoTask parent = null;
	private long lastBalanceAttempt = 0;
	private long lastHeatBeat = 0;
	private long submitTime = 0;
	private long endTime = 0;
	private long waitTime = 0;
	private int waitingClones = 0;
	private int cloneSizeShadow = 0;
	private String instanceID;
	private int cloneNumber = 0;
	private final Object syncMe = new Object();


	public VlamDatafluoTask(String taskId, Flow flow, ModuleI module) throws URISyntaxException {
		super(taskId, flow);
		this.module = module;
		this.SoLibUrl = module.getName() + ".so";
		this.addTaskStateListener(GlobalConfiguration.stateMonitor);

		for(Iterator itr = GlobalConfiguration.getStateMonitors().iterator(); itr.hasNext();){
			IStateMonitor sm = (IStateMonitor)itr.next();
			this.addTaskStateListener(sm);
		}
	}

	public String getInstanceID() {
		return this.instanceID;
	}

	public void setInstanceID(String instanceID) {
		this.instanceID = instanceID;
	}

	public synchronized long getLastHeartBeat() {
		return this.lastHeatBeat;
	}

	public synchronized void heartBeat() {
		if (this.lastHeatBeat == 0) {
			this.waitTime = (System.currentTimeMillis() - this.submitTime) / 1000;
			GlobalConfiguration.logging.log(this.getName() + " " + this.taskId + " WAIT: " + this.waitTime);
		}
		this.lastHeatBeat = (System.currentTimeMillis() / 1000);

	}

	public boolean isWaiting() {
		if (this.waitingClones == 0) {
			return false;
		} else {
			return true;
		}
	}

	public int getCloneNumber(){
		return this.cloneNumber;
	}

	public VlamDatafluoTask getClone(int number){
		if( (number == 0) || (this.clones == null) )
			return this;
		for(Iterator itr = this.clones.iterator(); itr.hasNext();){
			VlamDatafluoTask ltask = (VlamDatafluoTask)itr.next();
			if(ltask.getCloneNumber() == number){
				return ltask;
			}
		}
		return this;
	}
	public synchronized VlamDatafluoTask cloneMe() throws CloneNotSupportedException {
		
		VlamDatafluoTask task = (VlamDatafluoTask) this.clone();
		task.farm = -1;
		task.farmType = 3;
		task.clones = null;
		task.parent = this;
		task.runCounter = -1;
		task.lastHeatBeat = 0;
		task.submitTime = 0;
		task.taskId = UUID.randomUUID().toString();
		task.taskInputPorts = new HashMap();
		task.taskOutputPorts = new HashMap();
		task.cloneNumber = this.clones.size() + 1;
		if (this.fixedHost == false) {
			task.host = "any";
		}
		for (Iterator itr = this.taskInputPorts.values().iterator(); itr.hasNext();) {
			VlamDatafluoPort oport = (VlamDatafluoPort) itr.next();
			VlamDatafluoPort port = oport.cloneMe();
			port.setShadowQueue(null);
			port.setTask(task);
			if (oport.equals(this.farmedPort) == false) {
				Queue queue = GlobalConfiguration.getEngine().messageExchange.createMessageQueue(port);
				GlobalConfiguration.getMessageExchange().createLink(this.getInputPort(oport.getName()).getShadowQueue(), queue);
			}else{
				this.farmedPort.getQueue().addEventListener(port);
				task.farmedPort = port;
			}
			task.addInputPort(port);
			//Effects the load calculation;
			this.farmedPort.getQueue().incAttachedConsumers();

		}

		for (Iterator itr = this.taskOutputPorts.values().iterator(); itr.hasNext();) {
			VlamDatafluoPort oport = (VlamDatafluoPort) itr.next();
			VlamDatafluoPort port = oport.cloneMe();
			port.setTask(task);
			task.addOutputPort(port);
		}

		if (this.state.getState() != TaskState.COMPLETE_STATE) {
			this.clones.add(task);
			this.cloneSizeShadow++;
			//GlobalConfiguration.logging.debug("CLONED "+ this.getName()+" NEW TASKID: "+ task.getTaskId()+"\n");
			GlobalConfiguration.getFlow().addTask(task);

			if (this.state.getState() == TaskState.RUNNING_STATE) {
				try {
					task.setState(TaskState.RUNNING);
					task.invoke();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}//if		
		}//if

		return task;
		
	}

	public void setFarmType(int type) {
		this.farmType = type;
	}

	public int getFarmType() {
		return this.farmType;
	}

	public boolean isFarmed() {
		if (this.farm >= 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isParent() {
		if (this.parent == null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasDanglingClones() {
		if (this.clones != null) {
			for (Iterator itr = this.clones.iterator(); itr.hasNext();) {
				VlamDatafluoTask task = (VlamDatafluoTask) itr.next();
				if (task.getLastHeartBeat() == 0) {
					return true;
				}
			}
		}
		return false;
	}

	public void setFarmedPort(VlamDatafluoPort farmedPort) {
		this.farmedPort = farmedPort;
	}

	public void setFarmedPort(String farmedPort) {
		VlamDatafluoPort port = this.getInputPort(farmedPort);
		this.farmedPort = port;
	}

	public VlamDatafluoPort getFarmedPort() {
		return this.farmedPort;
	}

	public synchronized void incRunCounter() {
		this.runCounter++;
	}

	public synchronized void decRunCounter() {
		this.runCounter--;
	}

	public synchronized void incParentRunCounter() {
		if (this.parent != null) {
			this.parent.incRunCounter();
		} else {
			this.runCounter++;
		}
	}

	public synchronized void decParentRunCounter() {
		if (this.parent != null) {
			this.parent.decRunCounter();
		} else {
			this.runCounter--;
		}
	}

	public synchronized int getRunCounter() {
		return this.runCounter;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setHost(String host, boolean fixed) {
		this.host = host;
		this.fixedHost = true;
	}

	public String getHost() {
		return this.host;
	}

	public void setFarmCount(int farm) {
		this.farm = farm;
	}

	public int getFarmCount() {
		return this.farm;
	}

	@Override
	public VlamDatafluoPort getInputPort(String portId) {
		return (VlamDatafluoPort) taskInputPorts.get(portId);
	}

	@Override
	public VlamDatafluoPort getOutputPort(String portId) {
		return (VlamDatafluoPort) taskOutputPorts.get(portId);
	}

	public String getSoLibURL() {
		return this.SoLibUrl;
	}

	public ModuleI getModule() {
		return this.module;
	}

	public String getTaskVersion() {
		return this.TaskVersion;
	}

	/**
	 * This is where everything happends.
	 * @param runevent
	 */
	@Override
	protected void handleRun(RunEvent runevent) {
		try {
			flow.taskStateChanged(new TaskStateChangedEvent(this, "Start running"));


			try {				
				invoke();
				if (clones != null) {
					for (Iterator itr = this.clones.iterator(); itr.hasNext();) {
						VlamDatafluoTask task = (VlamDatafluoTask) itr.next();
						if (task.state.getState() != TaskState.RUNNING_STATE) {
							task.setState(TaskState.RUNNING);
						}
						task.invoke();
					}//for
				}//if
			} catch (Exception ex) {
				ex.printStackTrace();
			}//catch			
		} catch (Exception ex) {
			ex.printStackTrace();
		}//catch
	}//handleRun

	public synchronized void portReceiveMessage(VlamDatafluoPort port) {

		if ((GlobalConfiguration.farming > 0)) {
			switch (this.farmType) {
				case 0: //Auto
					break;
				case 1: //Fixed
					//GlobalConfiguration.logging.debug(this.getName()+": FIX FARMING: "+ this.farm);
					for (int i = 1; i < this.farm; i++) {
						try {
							VlamDatafluoTask task = this.cloneMe();
							//task.invoke();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					//Disable furthur farming
					this.farmType = 3;
					break;
				case 2: //One-2-one
					//GlobalConfiguration.logging.debug(this.getName()+": ONE-2-ONE FARMING: "+this.clones.size()+" "+this.farmedPort.getQueue().getMaxCounter() );					
					for (int i = this.cloneSizeShadow + 1; i < this.farmedPort.getQueue().getMaxCounter(); i++) {
						try {
							this.cloneMe();							
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}					
					break;
				case 3: //None
					break;
				default:

			}//switch
		}//if

		//TODO check if this works moved from CommandHandler as it makes more sense here.
		if (port.getDirection() == VlamDatafluoPort.DIRECTION_OUT) {
			port.activate(new DataEvent(port));
		}
	}

	public synchronized void portConsumedMessage(VlamDatafluoPort port) {
		if ((GlobalConfiguration.farming > 0) && (this.state.getState() == TaskState.RUNNING_STATE)
				&& (this.isParent() == true) && (this.hasDanglingClones() == false) && (this.farmedPort != null)) {
			double load = this.farmedPort.getQueue().calcLoad();
			GlobalConfiguration.logging.log(this.getName() + " " + this.taskId + " LOAD: " + load + " " + this.farmedPort.getQueue().getSize());
			switch (this.farmType) {
				case 0: //Auto
					//GlobalConfiguration.logging.debug(this.getName()+": LOAD: "+load + " "+this.farmedPort.getQueue().getSize());
					long time = System.currentTimeMillis();					
					long diff = (time - this.lastBalanceAttempt) / 1000;
					GlobalConfiguration.logging.log("DIFF: " + diff);
					if ((diff >= GlobalConfiguration.balanceFrequency) && (load > GlobalConfiguration.loadThreshold)) {
						int clones = (int) Math.ceil(load / GlobalConfiguration.loadThreshold);
						if (clones > GlobalConfiguration.maxCloneBurst) {
							clones = GlobalConfiguration.maxCloneBurst;
						}
						//GlobalConfiguration.logging.debug(this.getName()+": LOAD: "+load+" CLONES: "+clones);
						for (int i = 0; i < clones; i++) {
							try {
								VlamDatafluoTask task = this.cloneMe();
								//task.invoke();
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						this.lastBalanceAttempt = time;
					}

					break;
				case 1:
				case 2:
				case 3:
			}
		}//if
	}

	@Deprecated
	public void tryFarmMe() throws Exception {		
	}
	@Deprecated
	private synchronized void invoke__() throws Exception {
		for (Iterator itr = getOutputPorts().iterator(); itr.hasNext();) {
			VlamDatafluoPort port = (VlamDatafluoPort) itr.next();
			port.activate(new DataEvent(port));
		}//for*/
		this.tryComplete();
	}
	
	public void resubmit() {

		if (this.state.getState() == TaskState.RUNNING_STATE) {

			GlobalConfiguration.logging.log(this.getName() + " " + this.taskId + " RE-SUBMITTING: " + GlobalConfiguration.logging.getTime());
			GlobalConfiguration.logging.debug("RE-SUBMITTING: " + this.getModule().getName() + "\n");
			//Add last message back to queue
			for (Iterator itr = this.getInputPorts().iterator(); itr.hasNext();) {
				VlamDatafluoPort port = (VlamDatafluoPort) itr.next();
				Message message = port.getLastMessage();
				if (message != null) {
					port.getQueue().rePut(message);
				}
			}
			Scheduler sched = GlobalConfiguration.submissionScheduler;
			sched.addRunnableTask(this);
		}
	}	

	private synchronized void invoke() throws Exception {		
		this.startTimer();
		//GlobalConfiguration.logging.log(this.getName()+" "+this.taskId+ " START_TIME: " + this.getStartTime().getShortString() + " "+this.host);
		GlobalConfiguration.logging.log("FIRING: " + this.getModule().getName() + "\n");
		this.submitTime = System.currentTimeMillis();
		this.incParentRunCounter();
		boolean fired = false;

		
		/**
		 * Tasks with input ports
		 */
		if (fired == false) {
			fired = true;
			Scheduler sched = GlobalConfiguration.submissionScheduler;
			sched.addRunnableTask(this);
		
		}		
	}
	
	public int getWaitingClones() {
		return this.waitingClones;
	}

	public synchronized void tryComplete() {

		this.endTime = (System.currentTimeMillis() - this.submitTime) / 1000;
		GlobalConfiguration.logging.log(this.getName() + " " + this.taskId + " COMPLETE: " + this.endTime);
		/*if(this.getName().contains("Histogram") == true)
			GlobalConfiguration.logging.debug(this.getName() + " " + this.taskId + " LAST MSG: "
					+ this.farmedPort.getLastMessage().getMessage());*/
		this.stopTimer();
		GlobalConfiguration.logging.log(this.getName() + " " + this.taskId + " STOP_TIME: " + this.getEndTime().getShortString());

		if (this.parent != null) {
			parent.clones.remove(this);
			//GlobalConfiguration.logging.debug("REMOVING TASK: "+ this.getName() + ":" + this.getTaskId()+"\n");
			//GlobalConfiguration.logging.debug("CLONE SIZE: "+ parent.clones.size()+"\n");
			if ((parent.clones.size() == 0) && (parent.getWaitingClones() == 1)) {
				parent.complete();
			}
		} else {
			this.waitingClones = 1;			
			//GlobalConfiguration.logging.debug("REMOVING TASK: "+ this.getName() + ":" + this.getTaskId()+"\n");
			//GlobalConfiguration.logging.debug("CLONE SIZE: "+ this.clones.size()+"\n");
			if (this.clones.size() == 0) {
				this.complete();
			}
		}
		this.waitingClones = 1;
	}

	@Override
	public void portStateChanged(PortStateChangedEvent portStateChangedEvent) {
		super.portStateChanged(portStateChangedEvent);

		//Destroy cloned ports
		VlamDatafluoPort port = (VlamDatafluoPort) portStateChangedEvent.getPort();
		if ((portStateChangedEvent.getState() == PortState.DESTROYED_STATE) && (this.clones != null) && (port.getDirection() == Port.DIRECTION_IN)) {
			for (Iterator itr = this.clones.iterator(); itr.hasNext();) {
				VlamDatafluoTask task = (VlamDatafluoTask) itr.next();
				VlamDatafluoPort cport = task.getInputPort(port.getName());
				if (cport != null) {
					cport.destroy(new DataEvent(cport));
				} else {
					//	GlobalConfiguration.logging.debug("NO CLONED PORT "+ port.getName()+"\n");
				}


			}//for
		}//if

	}

	@Override
	protected void taskCancelled() {
		throw new UnsupportedOperationException("Not supported yet.");

	}

	@Override
	protected void taskComplete() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void taskPaused() {
		throw new UnsupportedOperationException("Not supported yet.");

	}

	@Override
	protected void taskResumed() {
		throw new UnsupportedOperationException("Not supported yet.");

	}
}

    