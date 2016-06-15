////////////////////////////////////////////////////////////////////////////////
//
// University of Southampton IT Innovation Centre, 2002
//
// Copyright in this library belongs to the IT Innovation Centre of
// 2 Venture Road, Chilworth Science Park, Southampton SO16 7NP, UK.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1
// of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation Inc, 59 Temple Place, Suite 330, Boston MA 02111-1307 USA.
//
//      Created By          :   Justin Ferris
//      Created Date        :   2003/05
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/08/23 06:24:47 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////
package nl.wtcw.vle.datafluo.core.task;

import java.util.*;

import nl.wtcw.vle.datafluo.core.flow.*;
import nl.wtcw.vle.datafluo.core.port.*;
import nl.wtcw.vle.datafluo.core.event.*;
import nl.wtcw.vle.datafluo.util.event.*;
import nl.wtcw.vle.datafluo.core.util.*;

import org.apache.log4j.Logger;

public abstract class AbstractTask implements Task, TaskStateListener, PortStateListener, RunEventSource {
  private static Logger logger = Logger.getLogger(AbstractTask.class);
  
  /** Whether or not this task has breakpoint*/
  private boolean hasBreakpoint = false;

  /** Whether or not this task has breakpoint*/
  protected boolean hasCompleted = false;

  protected String taskId = null;
  protected String name = null;

  /** The internal state of this task. The task state begins as NEW */
  protected TaskState state = TaskState.NEW;

  private Topic portStateTopic = new Topic();

  /** Whether or not this task is critical */
  private boolean isCritical;

  /** Whether or not this task can be cancelled*/
  private boolean isCancelable;

  /** Whether or not this task can be paused*/
  private boolean isPauseable;

  private TimePoint startTime;
  private TimePoint endTime;
  protected Flow flow = null;

  @Deprecated
  private HashMap /* parentTaskId:String -> parentTask:Task */ parentTaskMap = new HashMap();
  @Deprecated
  protected HashMap /* childTaskId:String -> childTask:Task */ childTaskMap = new HashMap();

  protected HashMap /* portId:String -> Port:Port */			   taskInputPorts = new HashMap();
  protected HashMap /* portId:String -> Port:Port */			   taskOutputPorts = new HashMap();
  /** 
   * Set of parent tasks that have sent a run event to this task. When all parents
   * have sent a run event this task will start to run and clear the set 
   */
  protected HashSet /* parentTask:Task */ runEvents = new HashSet();

  protected final Object stateThreadSync = new Object();

  /**
   * Collection of listeners of events for task state changes
   */
  private ArrayList /* of TaskStateListener */ taskStateListeners = new ArrayList();

  /**
   * Collection of listeners of arbitrary events related to tasks.
   * For example during iteration over a data set a subclass may decide
   * to notify listeners of completion of processing of a data item
   * in the set.
   */
  private ArrayList /* of TaskEventListener */ taskEventListeners = new ArrayList();  

  private String errorMessage = "";
    
  /**
   * Construct an AbstractTask with a unique id for <code>Task</code> (within the namespace of the <code>Flow</code>),
   * a human readable name for the <code>Task</code>, the containing <code>Flow</code>; and finally, a flag to indicate
   * whether the containing <code>Flow</code> should fail or conitinue should this <code>Task</code> fail.
   * @param taskId unique identifier within the namespace of the Flow that contains this Task.
   * @param name human readable name for the Task (unique amongst Tasks within the namespace of the Flow).
   * @param flow the containing Flow.
   * @param isCritical flag to indicate whether the containing Flow should continue or fail should
   * this Task fail at workflow runtime. If this is set to true, the Flow will fail, else it will continue to
   * run other Tasks that don't depend on this one.
   */
  public AbstractTask(String taskId, String name, Flow flow, boolean isCritical) {
    this.taskId = taskId;
    this.name = name;
    this.flow = flow;
    this.isCritical = isCritical;

    flow.addTask(this);
    flow.addStartTask(this);
    flow.addEndTask(this);
  }

  /**
   * Construct an AbstractTask that is interruptable. 
   * Interruptible tasks can be paused and cancelled while running.
   * Interruptible tasks must implement handlePause and handleCancel methods.
   * This constructor has additional parameter: isInterruptible
   */
  public AbstractTask(String taskId, String name, Flow flow, boolean isCritical, boolean isPauseable, boolean isCancelable) {
    this.taskId = taskId;
    this.name = name;
    this.flow = flow;
    this.isCritical = isCritical;
    this.isPauseable = isPauseable; 
    this.isCancelable = isCancelable; 

    flow.addTask(this);
    flow.addStartTask(this);
    flow.addEndTask(this);
  }

  public AbstractTask(String taskId, String name, Flow flow) {
    this(taskId, name, flow, true);
  }

  public AbstractTask(String taskId, Flow flow) {
    this(taskId, "", flow);    
  }

  // Task interface implementation
  public String getTaskId() {
    return taskId;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name){
	  this.name = name;
  }
  
  public String getDescription() {
    return name + ":" + taskId;
  }

  /**
   * @deprecated
   */
  public boolean isFailFlowOnTaskFailure() {
    return isCritical;
  }
   
  public boolean isCritical() {
    return isCritical;
  }

  /**
   * @deprecated
   */
  public void setFailFlowOnTaskFailure(boolean failFlow) {
    this.isCritical = failFlow;
  }
  
  public void setCritical(boolean isCritical) {
    this.isCritical = isCritical;
  }

  /**
   * Return if this task can be paused while running.
   * If so then it will run in interruptible run state.
   */
  public boolean isPauseable() {
    return isPauseable;
  }

  /**
   * Return if this task can be cancelled while running.
   * If so then it will run in interruptible run state.
   */
  public boolean isCancelable() {
    return isCancelable;
  }

  /**
   * Set this task to interruptible.
   * Interruptible tasks can be paused and cancelled while executing.
   * There is a constructor to create them(see above).
   public void setInterruptible(boolean isInterruptible) {
   this.isInterruptible=isInterruptible;
   }
   */

  public void setErrorMessage(String errorMessage) {
    synchronized(errorMessage) {
      this.errorMessage = errorMessage;
    }
  }

  public String getErrorMessage() {
    synchronized(errorMessage) {
      return errorMessage;
    }
  }

  public TaskState getState() {
    synchronized(stateThreadSync) {
      return state;
    }
  }

  public String getStateString() {
    synchronized(stateThreadSync) {
      return state.getStateString();
    }
  }

  public TimePoint getStartTime() {
    return startTime;
  }

  public TimePoint getEndTime() {
    return endTime;
  }

  public boolean isStateFinal() {
    synchronized(stateThreadSync) {
      return state == TaskState.COMPLETE || state == TaskState.FAILED;
    }
  }
    
  public Flow getFlow() {
    return flow;
  }

@Deprecated
  public void addChild(Task task) {
    childTaskMap.put(task.getTaskId(), task);
  }



  public void addInputPort(Port port){
	  taskInputPorts.put(port.getPortId(), port);
  }

  public Collection getInputPorts(){
	  return taskInputPorts.values();
  }

  public Port getInputPort(String portId) {
	  return(Port) taskInputPorts.get(portId);
  }

  public boolean containsInputPort(String portId){
	  if(this.taskInputPorts.containsKey(portId))
		  return true;
	  else
		  return false;
  }

  public boolean containsOutputPort(String portId){
	  if(this.taskOutputPorts.containsKey(portId))
		  return true;
	  else
		  return false;
  }

  public void addOutputPort(Port port){
	  taskOutputPorts.put(port.getPortId(), port);
  }

  public Collection getOutputPorts(){
	  return taskOutputPorts.values();
  }

  public Port getOutputPort(String portId) {
	  return(Port) taskOutputPorts.get(portId);
  }
  
  @Deprecated
  public Collection getChildren() {
    return childTaskMap.values();
  }
  @Deprecated
  public Task getChild(String childTaskId) {
    return(Task) childTaskMap.get(childTaskId);
  }

  @Deprecated
  public void addParent(Task task) {
    parentTaskMap.put(task.getTaskId(), task);
  }
  @Deprecated
  public Collection getParents() {
    return parentTaskMap.values();
  }
  @Deprecated
  public Task getParent(String parentTaskId) {
    return(Task) parentTaskMap.get(parentTaskId);
  }

  public Set getRunEvents() {
    return runEvents;
  }
@Deprecated
  public void linkTo(Task childTask) {
    this.addChild(childTask);
    childTask.addParent(this);
    flow.removeEndTask(this);
    flow.removeStartTask(childTask);
  }
  
  /**
   * Link ports togather
   */
  public void linkTo(Port localPort,Port remotePort) throws PortConnectionException {
	if( (localPort.getDirection() == Port.DIRECTION_OUT)
		&& remotePort.getDirection() == Port.DIRECTION_IN) {
		localPort.addConnection(remotePort);
		
		//System.err.print("LINKTO: " + this.name +"->" +remotePort.getTask().getName() + ":" + remotePort.getPortId() + "\n" );
		
		flow.removeEndTask(this);
		flow.removeStartTask(remotePort.getTask());
	}
	else
		throw new PortConnectionException("Task " + this.getTaskId() + ": Error in port direction for connecting "
				+ localPort.getPortId()+ " to " + remotePort.getPortId() );
    //this.addChild(childTask);
    //childTask.addParent(this);
    //flow.removeEndTask(this);
    //flow.removeStartTask(childTask);
  }
		
  public final void run(RunEvent runEvent) {
    //RunEventSource source = runEvent.getSource();
			
    /*if(logger.isDebugEnabled()) {
     String description = "flow";


     if(source instanceof AbstractTask) {
     description = ((AbstractTask) source).getDescription();
     }

     logger.debug(getDescription() + " received run from " + description + ", state=" + getStateString());
     }*/
      
    synchronized(stateThreadSync) {			
      state.run(this, runEvent);
    }
  }

  /*Added to pause tasks.*/
  public final boolean pause() {
    synchronized(stateThreadSync) {			
      state.pause(this);
    }
    return true;
  }

  /**
   * Checks if task should be paused.
   * matskan
   */
  public final boolean isPause() {
    Flow flow = getFlow();
    return(flow.isFlowPausing() || hasBreakpoint);
  }

  /**
   * Checks if task has a breakpoint.
   * Only for debugging purposes.
   * matskan
   */
  public final boolean isBreakpoint() {
    return hasBreakpoint;
  }

  /**
   * Checks if task has completed.
   * This function is used with breakpoints to define
   * if the resume from paused state should be to complete or running state. 
   * Also is used to define wheather or not the data are volatile.
   * matskan
   */
  public final boolean isCompleted() {
    return hasCompleted;
  }

  /**
   * Sets task's hasCompleted flag.
   * matskan
   */
  public final void setCompleted() {
    hasCompleted = true;
	
  }

  /**
   * Checks if task's output is allowed to be editied.
   * Currently only checks if the processor has completed
   * More to be added.
   * matskan
   */
  public final boolean isDataNonVolatile() {
    return state.isDataNonVolatile(this);
  }

  /*Resumes paused tasks*/
  public final boolean resume(RunEvent runEvent) {
    synchronized(stateThreadSync) {			
      state.resume(this, runEvent);
    }
    return true;

  }

  /*Added to cancel paused tasks*/
  public final void cancel() {
    synchronized(stateThreadSync) {			
      state.cancel(this);
    }
  }

  /**
   * Adds a breakpoint. 
   * Should be called by the Engine class.
   */
  public final void addBreakpoint() {
    hasBreakpoint = true;
  }

  /**
   * Removes a breakpoint.
   * Should be called by the Engine class.
   */
  public final void removeBreakpoint() {
    hasBreakpoint = false;
  }

  public final void destroy() {
    synchronized(stateThreadSync) {
      state.destroy(this);
    }
  }

  protected final void complete() {
    synchronized(stateThreadSync) {
      state.complete(this);
    }
  }

  /* A task that is interruptable notifies that has been paused*/
  protected final void paused() {
    synchronized(stateThreadSync) {
      state.paused(this);
    }
  }

  /* A task that is interruptable notifies that has been cancelled*/
  protected final void cancelled() {
    synchronized(stateThreadSync) {
      state.cancelled(this);
    }
  }

  protected final void fail(String message) {
    synchronized(stateThreadSync) {
      state.fail(this, message);
    }
  }

  protected final void fail(Exception e) {
    String message = e.getMessage();

    synchronized(stateThreadSync) {
      state.fail(this, message);
    }
  }	

  public void addTaskStateListener(TaskStateListener taskStateListener) {
    taskStateListeners.add(taskStateListener);
  }

  public void removeTaskStateListener(TaskStateListener taskStateListener) {
    taskStateListeners.remove(taskStateListener);
  }

  public void addTaskEventListener(TaskEventListener taskEventListener) {
    taskEventListeners.add(taskEventListener);
  }

  public void removeTaskEventListener(TaskEventListener taskEventListener) {
    taskEventListeners.remove(taskEventListener);
  }

  public String toString() {

    /* String nl = System.getProperty("line.separator");

     StringBuffer sb = new StringBuffer();

     sb.append("Task:" + getTaskId() + nl);
     sb.append("\tstate: " + state.getStateString() + nl);
     sb.append("\tchildren:" + nl);
     
     for(Iterator i = childTaskMap.iterator(); i.hasNext(); sb.append(nl)) {
     sb.append("\t\t" + ((Task) i.next()).toString());
     }
     
     return sb.toString();
     */
    return getDescription();
  }

  // end Task interface implementation

  /**
   * Provides the time spent so far executing.
   * @return executing time in millisecs
   */
  public long getExecutionTimeSoFar() {
    if(startTime == null) {
      return 0;
    }
    else if(endTime == null) {
      return System.currentTimeMillis() - startTime.getMillisecs();
    }
    else {
      return endTime.getMillisecs() - startTime.getMillisecs();
    }
  }

  protected void setState(TaskState state) {
    this.state = state;
  }

  public void addPortStateListener(final PortStateListener portStateListener) {
    portStateTopic.addConsumer(new Consumer() {
      public void newEvent(Object eventObj) {
        portStateListener.portStateChanged((PortStateChangedEvent) eventObj);
      }
    });
  }

  public void portStateChanged(PortStateChangedEvent portStateChangedEvent) {
	  // TODO

	final PortStateChangedEvent finalPortStateChangedEvent = portStateChangedEvent;

    logger.debug(getTaskId() + " task received port state changed event for " +
                 portStateChangedEvent.getPort().getDescription() + " to " +
                 portStateChangedEvent.getPort().getState());

   // synchronized(stateThreadSync) {
     // state.portStateChanged(portStateChangedEvent);
   // }

   // portStateTopic.put(portStateChangedEvent);


  }
  public void taskStateChanged(TaskStateChangedEvent taskStateChangedEvent) {
    final TaskStateChangedEvent finalTaskStateChangedEvent = taskStateChangedEvent;
		
    logger.debug(getDescription() + " is in state " + state);

				
    // note: notify the flow of this tasks change in state
    // this has to be synchronous as the order in which events are received
    // is significant
    flow.taskStateChanged(taskStateChangedEvent);

     //HERE STATE
    for(Iterator i = taskStateListeners.iterator(); i.hasNext();) {
     TaskStateListener taskStateListener = (TaskStateListener) i.next();	
     taskStateListener.taskStateChanged(finalTaskStateChangedEvent);
		

     /*Thread thread = new Thread() {
     public void run() {
     taskStateListener.taskStateChanged(finalTaskStateChangedEvent);
     }
     };

     thread.start();*/
     }
  }

  protected void startTimer() {
    startTime = new TimePoint();
  }

  protected void stopTimer() {
    endTime = new TimePoint();
  }

  protected abstract void handleRun(RunEvent runEvent);
  
  protected abstract void taskPaused();

  protected abstract void taskCancelled();

  protected abstract void taskResumed();

  protected abstract void taskComplete();

  protected void handleComplete() {
	  for(Iterator itr = this.getOutputPorts().iterator(); itr.hasNext();){
			Port port = (Port)itr.next();
			port.destroy(new DataEvent(port));
		}
    //runTasks(getChildren());
  }

  /**
   * Must be implemented by the classes of interruptible tasks.
   * Otherwise, if called, throws exception
   */
  protected void handleResume(RunEvent runEvent) {
    throw new UnsupportedOperationException("handleResume is not supported by interruptible task.");
  }

  /**
   * Must be implemented by the classes of interruptible tasks.
   * Otherwise, if called, throws exception
   */
  protected void handlePause() {
    throw new UnsupportedOperationException("handlePause is not supported by interruptible task.");
  }

  /**
   * Must be implemented by the classes of interruptible tasks.
   */
  protected void handleCancel() {
    throw new UnsupportedOperationException("handleCancel is not supported by interruptible task.");
  }

  protected void handleFail() {}

  /**
   * <code>handleDestroy()</code> is called to clean up resources that may have been
   * allocated for the <code>Task</code>
   */
  protected void handleDestroy() {}
		
  protected void runTasks(Collection tasks) {
    RunEvent runEvent = new RunEvent(this);
    Task task = null;

    for(Iterator i = tasks.iterator(); i.hasNext();) {
      task = (Task) i.next();
      task.run((RunEvent) runEvent.clone());
    }
  }

  /**
   * Utility method for subclasses to call in order to
   * send a TaskEvent to all registered TaskEventListeners.
   */
  protected void fireTaskEvent(TaskEvent taskEvent) {

    final TaskEvent finalTaskEvent = taskEvent;
    
    for(Iterator i = taskEventListeners.iterator(); i.hasNext();) {
      final TaskEventListener taskEventListener = (TaskEventListener) i.next();
        	
      Thread thread = new Thread() {
          public void run() {
            taskEventListener.processEvent(finalTaskEvent);
          }
        };

      thread.start();
    }
  }
}
