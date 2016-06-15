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
//      Created By          :   Darren Marvin
//      Created Date        :   2002/7/27
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/08/23 06:24:47 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////
package nl.wtcw.vle.datafluo.core.flow;

import nl.wtcw.vle.datafluo.core.engine.Engine;
import java.util.*;

import nl.wtcw.vle.datafluo.main.*;
import nl.wtcw.vle.datafluo.core.event.*;
import nl.wtcw.vle.datafluo.core.task.*;
import nl.wtcw.vle.datafluo.core.util.*;
import nl.wtcw.vle.datafluo.util.event.*;

import org.apache.log4j.Logger;

public class Flow implements TaskStateListener, FlowStateListener, RunEventSource {

  private static Logger logger = Logger.getLogger(Flow.class);
  private String id;
    
  private TimePoint startTime;
  private TimePoint endTime;
    
  private FlowState state = FlowState.NEW;

  private final Object stateThreadSync = new Object();
  private final Object iterationSync = new Object();
		
  private Topic taskStateTopic = new Topic();
  private Topic flowStateTopic = new Topic();

  
  private HashSet /* runningTask:Task */ runningTasks = new HashSet();

  private HashSet /* PausedTask:Task */ pausedTasks = new HashSet();

  private HashSet /* PausedTask:Task */ IRUNTasks = new HashSet();
 
  // for debugging only
  private HashMap /* taskName:String -> taskState:String */ taskStateMap = new HashMap();

  private HashSet /* allTasksContainedByTheFlow:Task */ allTasks = new HashSet();
  private HashMap /* taskId:String -> containedTask:Task */ taskIdMap = new HashMap();
		
  private HashMap /* taskId:String -> taskWithNoParents:Task */ startTaskMap = new HashMap();
  private HashMap /* taskId:String -> taskWithNoChildren:Task */ endTaskMap = new HashMap();

  /** Lists errors that are encountered as tasks execute and fail */
  protected StringBuffer errorMessage = new StringBuffer();

  private ThreadGroup threadGroup = null;

  private Engine engine = null;

  // temp. ID generator is placed in the flow.
  protected static int flowIdCounter = -1;
  private static Object idCounterLock = new Object();

  /** 
   * Flag to indicate whether to prevent from running any Tasks that haven't yet run but otherwise 
   * would.
   */
  private boolean isCancelTasks = false;

  /** 
   * Flag to indicate whether to prevent from running any Tasks that haven't yet run but otherwise 
   * would.
   */
  private boolean isPauseTasks = false;

  /* Flag for the tasks to pause*/
  private boolean isPause = false;

  /**
   * implements the sub states notpausing and pausing.
   * a false value corresponds to the notpausing sub-state.
   */
  private boolean Pausing = false;

  /**
   * implements the sub states notpausing and pausing.
   * returns the value of the pausing variable.
   */
  public boolean isPausing() {
    return Pausing;
  }

  /**
   * implements the sub state pausing.
   * sets the value of the pausing variable to true.
   */
  public void setPausing() {
    Pausing = true;
  }
   
  /**
   * implements the sub state notpausing.
   * sets the value of the pausing variable to false.
   */
  public void unsetPausing() {
    Pausing = false;
  }

  public Flow(String id, Engine engine) {
    this.id = id;
    threadGroup = new ThreadGroup(id);
    this.engine = engine;
  }

  public Flow(String id){
	  this.id = id;
  }

  public Flow(Engine engine) {
    this(getUniqueFlowId(), engine);
  }

  public String getFlowId() {
    return id;
  }

  public long getTimeSinceFinalise() {
    if(endTime == null || startTime == null)
      return 0;
    else
      return System.currentTimeMillis() - endTime.getMillisecs();

  }

  public FlowState getState() {
    synchronized(stateThreadSync) {
      return state;
    }
  }

  public String getStateString() {
    synchronized(stateThreadSync) {
      return state.getStateString();
    }
  }

  public ThreadGroup getThreadGroup() {
    return threadGroup;
  }

  /**
   * Retrive the <code>Engine</code> in which this <code>Flow</code> is
   * contained.
   */
  public Engine getEngine() {
    return engine;
  }

  protected void setState(FlowState state) {
    this.state = state;
  }

 
  protected HashSet getRunningTasks() {
    return runningTasks;
  }

  protected HashSet getPausedTasks() {
    synchronized(iterationSync) {
      return pausedTasks;
    }
  }
  
  //Return the isPause flag for the task to pause or not.
  public boolean isFlowPausing() {
    return isPause;
  }

  public void addTask(Task task) {
    allTasks.add(task);
    taskIdMap.put(task.getTaskId(), task);

    //if it can be cancelled or paused while running then runs in interruptible state
    if(task.isPauseable() || task.isCancelable())IRUNTasks.add(task); 
  }
 
  public void addStartTask(Task task) {
    startTaskMap.put(task.getTaskId(), task);
  }

  public boolean removeStartTask(Task task) {
    return startTaskMap.remove(task.getTaskId()) != null ? true : false;
  }
 
  public void addEndTask(Task task) {
    endTaskMap.put(task.getTaskId(), task);
  }

  public boolean removeEndTask(Task task) {
    return endTaskMap.remove(task.getTaskId()) != null ? true : false;
  }

  protected void addRunningTask(Task task) {
    synchronized(iterationSync) {
      runningTasks.add(task);
    }
  }


  protected boolean removeRunningTask(Task task) {
    synchronized(iterationSync) {
      return runningTasks.remove(task);
    }
  }


  protected int getRunningTasksSize() {
    synchronized(iterationSync) {
      return runningTasks.size();
    }
  }

  protected void addPausedTask(Task task) {
    synchronized(iterationSync) {
      pausedTasks.add(task);
    }
  }

  protected boolean removePausedTask(Task task) {
    synchronized(iterationSync) {
      return pausedTasks.remove(task);
    }
  }
 
  protected int getPausedTasksSize() {
    synchronized(iterationSync) {
      return pausedTasks.size();
    }
  }

  public Task getTask(String taskId) {
    return(Task) taskIdMap.get(taskId);
  }

  public Collection getTasks() {
    return allTasks;
  }

  public Collection getStartTasks() {
    return startTaskMap.values();
  }
		
  public Task getStartTask(String taskId) {
    return(Task) startTaskMap.get(taskId);
  }
 		
  public Collection getEndTasks() {
    return endTaskMap.values();
  }

  public Task getEndTask(String taskId) { 
    return(Task) endTaskMap.get(taskId);
  }
		
  // degugging only
  public Map getTaskStateMap() {
    return taskStateMap;
  }
			
  public boolean isStateFinal() {
    synchronized(stateThreadSync) {
      return state == FlowState.COMPLETE || state == FlowState.CANCELLED || state == FlowState.FAILED;
    }
  }
		
  public final void run() {
    synchronized(stateThreadSync) {
      state.run(this);
    }
  }

  public final void cancel() {
    synchronized(stateThreadSync) {
      state.cancel(this);
    }
  }

  public final void destroy() {
    synchronized(stateThreadSync) {
      state.destroy(this);
    }
  }
 
  public final boolean pause() {
    isPause = true;
    synchronized(stateThreadSync) {
      state.pause(this);
    }
    return true; 
  }
 
  public final boolean resume() {
    isPause = false;
    synchronized(stateThreadSync) {
      state.resume(this);
    }
    return true; 
  }

  /**
   * Pauses a specific task.
   * The task is specified by the taskId parameter.
   * Pausing of the task depends on the flow's state.
   * 
   * @param taskId. The unique identifier of the task. 
   */
  public final void pauseTask(String taskId) {
    synchronized(stateThreadSync) {
      state.pauseTask(this, taskId);
    }
  }
 
  /**
   * Resumes a specific task.
   * The task is specified by the taskId parameter.
   * Resuming of the task depends on the flow's state.
   * 
   * @param taskId. The unique identifier of the task. 
   */
  public final void resumeTask(String taskId) {
    synchronized(stateThreadSync) {
      state.resumeTask(this, taskId);
    }
  }

  /**
   * Returns the time spent executing so far
   * @return milliseconds run
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

  public String getErrorMessage() {
    synchronized(errorMessage) {
      return errorMessage.toString();
    }
  }

  public void addTaskStateListener(final TaskStateListener taskStateListener) {
    taskStateTopic.addConsumer(new Consumer() {
      public void newEvent(Object eventObj) {
        taskStateListener.taskStateChanged((TaskStateChangedEvent) eventObj);
      }
    });
  }

  public void addFlowStateListener(final FlowStateListener flowStateListener) {
    flowStateTopic.addConsumer(new Consumer() {
      public void newEvent(Object eventObj) {
        flowStateListener.flowStateChanged((FlowStateChangedEvent) eventObj);
      }
    });
  }

  public void taskStateChanged(TaskStateChangedEvent taskStateChangedEvent) {
    final TaskStateChangedEvent finalTaskStateChangedEvent = taskStateChangedEvent;
				
    logger.debug(getFlowId() + " flow received task state changed event for " + 
                 taskStateChangedEvent.getTask().getDescription() + " to " + 
                 taskStateChangedEvent.getTask().getState());
   
    synchronized(stateThreadSync) {
      state.taskStateChanged(taskStateChangedEvent);
    }

    taskStateTopic.put(taskStateChangedEvent);
  }
 
  public void flowStateChanged(FlowStateChangedEvent flowStateChangedEvent) {
    logger.debug("Flow has state: " + getState());
    flowStateTopic.put(flowStateChangedEvent);

    if(flowStateChangedEvent.getState() == FlowState.DESTROYED_STATE) {
      taskStateTopic.stop();
      flowStateTopic.stop();
    }
  }

  public String toString() {
    return getFlowId();
  }

  protected void handleRun() {
    taskStateTopic.start();
    flowStateTopic.start();
	
    runningTasks.clear(); // should be clear anyway
    logger.debug("Flow '" + getFlowId() + "' started RUNNING.");

    // the not paused start tasks are added to the runningTasks set.
    // the paused start tasks are added to the pausedTasks set.
    // This is a special case. Ordinarily, tasks are added
    // to the set when they send taskRunning() to the Flow.
    // However, we must start with some tasks 'running' to 
    // be in a consistent state.
    // note: a new thread is started for every start task
    synchronized(iterationSync) {
      for(Iterator i = getStartTasks().iterator(); i.hasNext();) {
        final Task task = (Task) i.next();
        if(!task.isPause()) runningTasks.add(task);
        else pausedTasks.add(task);
        final RunEvent runEvent = new RunEvent(this);

        Thread runThread = new Thread() {
            public void run() {
              task.run(runEvent);
            }
          };
     
        runThread.start();
      }
    }
  }
  
  protected void handleComplete() {
    logger.debug(getFlowId() + " is complete"); 
  }

  /**
   * Calling this method with an argument of <code>true</code>prevents 
   * any Tasks that haven't yet run, but otherwise would run, from starting 
   * to run. Calling with an argument of <code>false</code> will allow
   * any Tasks, that are otherwise able to run, to begin execution. 
   */
  protected void setCancelTasks(boolean isCancelTasks) {
    this.isCancelTasks = isCancelTasks;
  }

  public boolean isCancelTasks() {
    return isCancelTasks;
  }

  /**
   * Calling this method with an argument of <code>true</code>prevents 
   * any Tasks that haven't yet run, but otherwise would run, from starting 
   * to run. Calling with an argument of <code>false</code> will allow
   * any Tasks, that are otherwise able to run, to begin execution. 
   */
  protected void setPauseTasks(boolean isPauseTasks) {
    this.isPauseTasks = isPauseTasks;
  }

  public boolean isPauseTasks() {
    return isPauseTasks;
  }

  /**
   * Pauses a specific task.
   * The task is specified by the taskId parameter.
   *
   * @param taskId. The unique identifier of the task. 
   */
  public void pauseTaskById(String taskId) {
    //Note: Used only for IRUN tasks.  
    for(Iterator i = IRUNTasks.iterator(); i.hasNext();) {
      Task task = (Task) i.next();
      if(task.getTaskId() == taskId && task.getState() == TaskState.IRUN) {
        task.pause();
        break;
      }
    }
  }
    
  /**
   * Resumes a specific task.
   * The task is specified by the taskId parameter.
   * 
   * @param taskId. The unique identifier of the task. 
   */
  protected void resumeTaskById(String taskId) {
    final RunEvent runEvent = new RunEvent(this);
    HashSet temp = new HashSet();
    synchronized(iterationSync) {
      temp.addAll(pausedTasks);
      //temp.addAll(allTasks);
      for(Iterator i = temp.iterator(); i.hasNext();) {
        Task task = (Task) i.next();
        if(task.getTaskId().equals(taskId)) {
          task.resume(runEvent);
          break;
        }
      }
    }
  }

  protected void handleFail() {}

  protected void destroyTasks() {
    synchronized(iterationSync) {
      for(Iterator i = allTasks.iterator(); i.hasNext();) {
        Task task = (Task) i.next();
        task.destroy();
      }
    }
  }

  /**
   * Pauses all tasks.
   * Is invoked by the pause event handling method.
   */
  protected void pauseTasks() {

    //matskan NOTE: 2 implementations pull, push.
    //Pull:
    //isPause flag indicates tasks that they should pause.
    //Tasks check this flag before running. So pull technique is used in this implementation.
    //Interruptible tasks that are running are pushed to paused state:

    //synchronized(iterationSync){
    for(Iterator i = IRUNTasks.iterator(); i.hasNext();) {
      Task task = (Task) i.next();
      if(task.getState() == TaskState.IRUN)task.pause();
    }
    //}


    /*OR... push implementation.
     //Checks for all tasks. If they are start-tasks and are in new state pause them!
     //Else If they have parent tasks that are running and they are in NEW state, pause them!
     synchronized(iterationSync){
     for(Iterator i = allTasks.iterator(); i.hasNext();) {
     Task task = (Task) i.next();
     if(task.getState()==TaskState.IRUN)task.pause();
     else if(task.getState()==TaskState.NEW){
     if (getStartTasks().contains(task))task.pause();
     else
     for(Iterator j=task.getParents().iterator();j.hasNext();){
     Task parentTask = (Task) j.next();
     if(parentTask.getState()==TaskState.RUNNING) {task.pause();break;}
     }
     }
     }
     }
     */
  }

  /**
   * Resumes all tasks.
   * Is invoked by the resume event handling method.
   */
  protected void resumeTasks() {
    final RunEvent runEvent = new RunEvent(this);
    HashSet temp = new HashSet();
    synchronized(iterationSync) {
      temp.addAll(pausedTasks);
    }
    for(Iterator i = temp.iterator(); i.hasNext();) {
      Task task = (Task) i.next();
      if(task.getState() == TaskState.PAUSED)task.resume(runEvent);
    }
  }

  /**
   * Cancels all tasks.
   * Is invoked by the cancel event handling method.
   */
  protected void cancelTasks() {
    synchronized(iterationSync) {
      HashSet temp = new HashSet();
      HashSet temp2 = new HashSet();
      temp.addAll(IRUNTasks); //add all IRUN tasks regardless if they arerunning or not
      temp2.addAll(IRUNTasks); //add all IRUN tasks regardless if they arerunning or not
      temp2.removeAll(runningTasks); //remove from temp2 those IRUNs who are running. 
      //So only non-running remain in temp2. 
      temp.removeAll(temp2);  //remove from temp the non-running IRUNs.
      temp.addAll(pausedTasks);
      //temp is a collection of paused tasks and tasks that are running and can be paused.
    
      for(Iterator i = temp.iterator(); i.hasNext();) {
        Task task = (Task) i.next();
        task.cancel();
      }
    }
  }
  
  private static String getUniqueFlowId() {
    String flowId = null;
    synchronized(idCounterLock) {
      flowIdCounter++;
      flowId = "" + flowIdCounter;
    }

    return flowId;
  }
}
