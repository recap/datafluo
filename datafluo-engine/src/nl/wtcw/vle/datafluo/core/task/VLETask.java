package nl.wtcw.vle.datafluo.core.task;

import nl.wtcw.vle.datafluo.core.port.VLEPort;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;
import nl.wtcw.vle.datafluo.context.Context;

import nl.wtcw.vle.wfd.ModuleI;
import nl.wtcw.vle.datafluo.core.event.RunEvent;
import nl.wtcw.vle.datafluo.core.event.DataEvent;
import nl.wtcw.vle.datafluo.core.event.TaskStateChangedEvent;
import nl.wtcw.vle.datafluo.core.flow.Flow;
import nl.wtcw.vle.datafluo.messaging.*;
import nl.wtcw.vle.datafluo.submission.*;
import nl.wtcw.vle.datafluo.util.GlobalConfiguration;
import nl.wtcw.vle.datafluo.core.event.PortStateChangedEvent;
import nl.wtcw.vle.datafluo.core.port.Port;
import nl.wtcw.vle.datafluo.core.port.PortState;
import org.apache.log4j.Logger;
import nl.wtcw.vle.datafluo.event.IStateMonitor;

public class VLETask extends AbstractTask implements Cloneable {

	public final static int AUTOFARM = 0;
	public final static int FIXEDFARM = 1;
	public final static int ONE2ONEFARM = 2;
	public final static int NONEFARM = 3;

	public final static String ANYHOST = "any";

	public final static String NODESERVICE_CAT = "NodeService";
	public final static String VLEHARNESS_CAT = "VLEharness";
	public final static String DEFAULT_CAT = "VLEharness";


	private static Logger logger = Logger.getLogger(VLETask.class);

	@Deprecated
	private ModuleI module;
	@Deprecated
	private String TaskVersion = "0.1";
	@Deprecated
	private String SoLibUrl = "";
	@Deprecated
	private String HOME = GlobalConfiguration.home;
	@Deprecated
	private String instanceID;

	private String category = VLETask.DEFAULT_CAT;
	private long budget = 0;
	private int farm = -1;
	private VLEPort farmedPort = null;
	private int farmType =   VLETask.NONEFARM;
	private String host = VLETask.ANYHOST;
	private boolean fixedHost = false;
	private int runCounter = 0;	
	private LinkedList clones = new LinkedList();
	protected VLETask parent = null;
	private long lastBalanceAttempt = 0;
	private long lastHeatBeat = 0;
	private long submitTime = 0;
	private long endTime = 0;
	private long waitTime = 0;
	private int waitingClones = 0;
	private int cloneSizeShadow = 0;
	
	private int cloneNumber = 0;
	private final Object syncMe = new Object();

	private Context context;

	@Deprecated
	public VLETask(String taskId, Flow flow, ModuleI module) throws URISyntaxException {
		super(taskId, flow);
		this.module = module;
		this.SoLibUrl = module.getName() + ".so";

	}
	public VLETask(String taskId, String name, Flow flow, Context context, boolean isCritical) {
		super(taskId, name, flow, isCritical, false, false);
		setContext(context);
	}

	private void setContext(Context context){
		this.context = context;

		for(Iterator itr = context.getStateMonitors().iterator(); itr.hasNext();){
			IStateMonitor sm = (IStateMonitor)itr.next();
			this.addTaskStateListener(sm);
		}
	}

	public void setCategory(String category){
		this.category = category;
	}

	public String getCategory(){
		return this.category;
	}
	
	public void setBudget(long budget){
		this.budget = budget;
	}

	public long getBudget(){
		return this.budget;
	}

	public Context getContext(){
		return this.context;
	}

	public synchronized long getLastHeartBeat() {
		return this.lastHeatBeat;
	}

	public synchronized void heartBeat() {
		if (this.lastHeatBeat == 0) {
			this.waitTime = (System.currentTimeMillis() - this.submitTime) / 1000;
			logger.debug(this.getName() + " " + this.taskId + " WAIT: " + this.waitTime);
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

	public VLETask getClone(int number){
		if( (number == 0) || (this.clones == null) )
			return this;
		for(Iterator itr = this.clones.iterator(); itr.hasNext();){
			VLETask ltask = (VLETask)itr.next();
			if(ltask.getCloneNumber() == number){
				return ltask;
			}
		}
		return this;
	}
	public synchronized VLETask cloneMe() throws CloneNotSupportedException {
		
		VLETask task = (VLETask) this.clone();
		task.farm = -1;
		task.farmType = 3;
		task.clones = null;
		task.parent = this;
		task.runCounter = -1;
		task.lastHeatBeat = 0;
		task.submitTime = 0;
		task.taskId = UUID.randomUUID().toString().replace("-","0").substring(0, 10);
		task.taskInputPorts = new HashMap();
		task.taskOutputPorts = new HashMap();
		task.cloneNumber = this.clones.size() + 1;
		if (this.fixedHost == false) {
			task.host = VLETask.ANYHOST;
		}
		for (Iterator itr = this.taskInputPorts.values().iterator(); itr.hasNext();) {
			VLEPort oport = (VLEPort) itr.next();
			VLEPort port = oport.cloneMe();
			port.setShadowQueue(null);
			port.setTask(task);
			if (oport.equals(this.farmedPort) == false) {
				Queue queue = context.getMessageExchange().createMessageQueue(port);
				//Queue queue = GlobalConfiguration.getEngine().messageExchange.createMessageQueue(port);
				context.getMessageExchange().createLink(this.getInputPort(oport.getName()).getShadowQueue(), queue);
			}else{
				this.farmedPort.getQueue().addEventListener(port);
				task.farmedPort = port;
			}
			task.addInputPort(port);
			//Effects the load calculation;
			this.farmedPort.getQueue().incAttachedConsumers();

		}

		for (Iterator itr = this.taskOutputPorts.values().iterator(); itr.hasNext();) {
			VLEPort oport = (VLEPort) itr.next();
			VLEPort port = oport.cloneMe();
			port.setTask(task);
			task.addOutputPort(port);
		}

		if (this.state.getState() != TaskState.COMPLETE_STATE) {
			this.clones.add(task);
			this.cloneSizeShadow++;
			//logger.debug("CLONED "+ this.getName()+" NEW TASKID: "+ task.getTaskId()+"\n");
			context.getEngine().getFlow().addTask(task);

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
				VLETask task = (VLETask) itr.next();
				if (task.getLastHeartBeat() == 0) {
					return true;
				}
			}
		}
		return false;
	}

	public void setDesignatedDataPartitionPort(VLEPort farmedPort) {
		this.farmedPort = farmedPort;
	}
	
	@Deprecated
	public void setDesignatedDataPartitionPort(String farmedPort) {
		VLEPort port = this.getInputPort(farmedPort);
		this.farmedPort = port;
	}

	public VLEPort getDesignatedDataPartitionPort() {
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
	public VLEPort getInputPort(String portId) {
		return (VLEPort) taskInputPorts.get(portId);
	}

	@Override
	public VLEPort getOutputPort(String portId) {
		return (VLEPort) taskOutputPorts.get(portId);
	}

	@Deprecated
	public String getSoLibURL() {
		return this.SoLibUrl;
	}

	@Deprecated
	public ModuleI getModule() {
		return this.module;
	}

	@Deprecated
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
						VLETask task = (VLETask) itr.next();
						if (task.state.getState() != TaskState.RUNNING_STATE) {
							task.setState(TaskState.RUNNING);
						}
						task.invoke();
					}//for
				}//if
			} catch (Exception ex) {
				logger.error(ex);
			}//catch			
		} catch (Exception ex) {
			logger.error(ex);
		}//catch
	}//handleRun

	public synchronized void portReceiveMessage(VLEPort port) {

		if ((GlobalConfiguration.farming > 0)) {
			switch (this.farmType) {
				case 0: //Auto
					break;
				case 1: //Fixed
					//logger.debug(this.getName()+": FIX FARMING: "+ this.farm);
					for (int i = 1; i < this.farm; i++) {
						try {
							VLETask task = this.cloneMe();
							//task.invoke();
						} catch (Exception ex) {
							logger.error(ex);
						}
					}
					//Disable furthur farming
					this.farmType = 3;
					break;
				case 2: //One-2-one
					//logger.debug(this.getName()+": ONE-2-ONE FARMING: "+this.clones.size()+" "+this.farmedPort.getQueue().getMaxCounter() );
					for (int i = this.cloneSizeShadow + 1; i < this.farmedPort.getQueue().getMaxCounter(); i++) {
						try {
							this.cloneMe();							
						} catch (Exception ex) {
							logger.error(ex);
						}
					}					
					break;
				case 3: //None
					break;
				default:

			}//switch
		}//if

		//TODO check if this works moved from CommandHandler as it makes more sense here.
		if (port.getDirection() == VLEPort.DIRECTION_OUT) {
			port.activate(new DataEvent(port));
		}
	}

	public synchronized void portConsumedMessage(VLEPort port) {
		if ((GlobalConfiguration.farming > 0) && (this.state.getState() == TaskState.RUNNING_STATE)
				&& (this.isParent() == true) && (this.hasDanglingClones() == false) && (this.farmedPort != null)) {
			double load = this.farmedPort.getQueue().calcLoad();
			logger.debug(this.getName() + " " + this.taskId + " LOAD: " + load + " " + this.farmedPort.getQueue().getSize());
			switch (this.farmType) {
				case 0: //Auto
					//logger.debug(this.getName()+": LOAD: "+load + " "+this.farmedPort.getQueue().getSize());
					long time = System.currentTimeMillis();					
					long diff = (time - this.lastBalanceAttempt) / 1000;
					logger.debug("DIFF: " + diff);
					if ((diff >= GlobalConfiguration.balanceFrequency) && (load > GlobalConfiguration.loadThreshold)) {
						int clones = (int) Math.ceil(load / GlobalConfiguration.loadThreshold);
						if (clones > GlobalConfiguration.maxCloneBurst) {
							clones = GlobalConfiguration.maxCloneBurst;
						}
						//logger.debug(this.getName()+": LOAD: "+load+" CLONES: "+clones);
						for (int i = 0; i < clones; i++) {
							try {
								VLETask task = this.cloneMe();
								//task.invoke();
							} catch (Exception ex) {
								logger.error(ex);
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
			VLEPort port = (VLEPort) itr.next();
			port.activate(new DataEvent(port));
		}//for*/
		this.tryComplete();
	}
	
	public void resubmit() {

		if (this.state.getState() == TaskState.RUNNING_STATE) {
			
			logger.debug("RE-SUBMITTING: " + this.getName()+":"+this.getTaskId() + "\n");
			//Add last message back to queue
			for (Iterator itr = this.getInputPorts().iterator(); itr.hasNext();) {
				VLEPort port = (VLEPort) itr.next();
				Message message = port.getLastMessage();
				if (message != null) {
					port.getQueue().rePut(message);
				}
			}
			Scheduler sched = context.getScheduler();
			sched.addRunnableTask(this);
		}
	}	

	private synchronized void invoke() throws Exception {		
		this.startTimer();		
		logger.debug("FIRING: " + this.getName()+":"+this.getTaskId() + "\n");
		this.submitTime = System.currentTimeMillis();
		this.incParentRunCounter();
		boolean fired = false;

		/**
		 * Tasks with input ports
		 */
		if (fired == false) {
			fired = true;
			Scheduler sched = context.getScheduler();
			sched.addRunnableTask(this);
		
		}		
	}
	
	public int getWaitingClones() {
		return this.waitingClones;
	}

	public synchronized void tryComplete() {

		this.endTime = (System.currentTimeMillis() - this.submitTime) / 1000;
		logger.debug(this.getName()+":"+this.getTaskId() + " COMPLETE: " + this.endTime);
		/*if(this.getName().contains("Histogram") == true)
			logger.debug(this.getName() + " " + this.taskId + " LAST MSG: "
					+ this.farmedPort.getLastMessage().getMessage());*/
		this.stopTimer();
		logger.debug(this.getName()+":"+this.getTaskId() + " STOP_TIME: " + this.getEndTime().getShortString());

		if (this.parent != null) {
			parent.clones.remove(this);
			//logger.debug("REMOVING TASK: "+ this.getName() + ":" + this.getTaskId()+"\n");
			//logger.debug("CLONE SIZE: "+ parent.clones.size()+"\n");
			if ((parent.clones.size() == 0) && (parent.getWaitingClones() == 1)) {
				parent.complete();
			}
		} else {
			this.waitingClones = 1;			
			//logger.debug("REMOVING TASK: "+ this.getName() + ":" + this.getTaskId()+"\n");
			//logger.debug("CLONE SIZE: "+ this.clones.size()+"\n");
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
		VLEPort port = (VLEPort) portStateChangedEvent.getPort();
		if ((portStateChangedEvent.getState() == PortState.DESTROYED_STATE) && (this.clones != null) && (port.getDirection() == Port.DIRECTION_IN)) {
			for (Iterator itr = this.clones.iterator(); itr.hasNext();) {
				VLETask task = (VLETask) itr.next();
				VLEPort cport = task.getInputPort(port.getName());
				if (cport != null) {
					cport.destroy(new DataEvent(cport));
				} else {
					//	logger.debug("NO CLONED PORT "+ port.getName()+"\n");
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

    