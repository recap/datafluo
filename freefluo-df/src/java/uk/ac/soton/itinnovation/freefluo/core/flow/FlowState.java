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
//      Created Date        :   2003/05/29
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/08/23 06:24:47 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.freefluo.core.flow;

import java.util.*;

import org.apache.log4j.*;

import uk.ac.soton.itinnovation.freefluo.core.task.*;
import uk.ac.soton.itinnovation.freefluo.core.event.*;

/**
 * Defines the states of a flow.
 */
public abstract class FlowState implements TaskStateListener {
  private static Logger logger = Logger.getLogger(FlowState.class);

  public static final int NEW_STATE = 0;
  public static final int RUNNING_STATE = 1;
  public static final int COMPLETE_STATE = 2;
  public static final int PAUSED_STATE = 3;
  public static final int FAILING_STATE = 4;
  public static final int FAILED_STATE = 5;
  public static final int CANCELLING_STATE = 6;
  public static final int CANCELLED_STATE = 7;
  public static final int DESTROYED_STATE = 8;

  public static final FlowState NEW = new NewState("NEW", NEW_STATE);
  public static final FlowState RUNNING = new RunningState("RUNNING", RUNNING_STATE);
  public static final FlowState COMPLETE = new CompleteState("COMPLETE", COMPLETE_STATE);
  public static final FlowState PAUSED = new PausedState("PAUSED", PAUSED_STATE);
  public static final FlowState FAILING = new FailingState("FAILING", FAILING_STATE);
  public static final FlowState FAILED = new FailedState("FAILED", FAILED_STATE);
  public static final FlowState CANCELLING = new CancellingState("CANCELLING", CANCELLING_STATE);
  public static final FlowState CANCELLED = new CancelledState("CANCELLED", CANCELLED_STATE);
  public static final FlowState DESTROYED = new DestroyedState("DESTROYED", DESTROYED_STATE);

  private String stateString = null;
  private int state;

  protected FlowState(String stateString, int state) {
    this.stateString = stateString;
    this.state = state;
  }

  public void taskStateChanged(TaskStateChangedEvent taskStateChangedEvent) {
    Task task = taskStateChangedEvent.getTask();
    int taskState = taskStateChangedEvent.getState();
    Flow flow = task.getFlow();

    if(logger.isDebugEnabled()) {
      Map taskStateMap = flow.getTaskStateMap();

      taskStateMap.put(task.getTaskId(), taskStateChangedEvent.getStateString());
    }

    // JF 16/11/2004: Flow state depends on the combination of states of the composed
    // tasks.
    logger.debug("flow state: " + flow.getState() + " Running: " + flow.getRunningTasksSize() + " PAUSED: " + flow.getPausedTasksSize() + " All: " + flow.getTasks().size());
    if(taskState == TaskState.RUNNING_STATE || taskState == TaskState.IRUN_STATE) {			 
      logger.debug("task RUNNING: " + task.getDescription());
      taskRunning(task);
    }
    else if(taskState == TaskState.PAUSED_STATE) {			 
      logger.debug("task PAUSED: " + task.getDescription());
      taskPaused(task);
    }
    else if(taskState == TaskState.CANCELLED_STATE) {			 
      logger.debug("task CANCELLED: " + task.getDescription());
      taskCancelled(task);
    }
    else if(taskState == TaskState.COMPLETE_STATE) {
      logger.debug("task COMPLETE: " + task.getDescription());
      taskComplete(task);
    }
    else if(taskState == TaskState.FAILED_STATE) {
      logger.debug("task FAILED: " + task.getDescription());
      taskFailed(task);
    }
    else if(taskState == TaskState.DESTROYED_STATE) {}
    else {
      throw new IllegalStateException("Unrecognised task state " + taskStateChangedEvent.getStateString() + " for task " + task.toString());
    }
  }

  public String getStateString() {
    return stateString;
  }

  public String toString() {
    return stateString;
  }

  public int getState() {
    return state;
  }

  public void run(Flow flow) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage("run" + ". flowId: " + flow.getFlowId());

    logger.error(msg);
    throw new IllegalStateException(msg);
  }

  public void cancel(Flow flow) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage("cancel");

    logger.error(msg);
    throw new IllegalStateException(msg);
  }

  public void destroy(Flow flow) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage("destroy");

    logger.error(msg);
    throw new IllegalStateException(msg);
  }

  public void pause(Flow flow) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage("pause");

    logger.error(msg);
    throw new IllegalStateException(msg);
  }

  public void resume(Flow flow) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage("resume");

    logger.error(msg);
    throw new IllegalStateException(msg);
  }

  public void resumeTask(Flow flow, String taskId) {}

  public void pauseTask(Flow flow, String taskId) {}

  public void taskRunning(Task task) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage("taskRunning" + ". flowId: " + task.getFlow().getFlowId());

    logger.error(msg);
    throw new IllegalStateException(msg);
  }

  public void taskPaused(Task task) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage("taskPaused");

    logger.error(msg);
    throw new IllegalStateException(msg);
  }

  public void taskCancelled(Task task) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage("taskCancelled");

    logger.error(msg);
    throw new IllegalStateException(msg);
  }

  public void taskComplete(Task task) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage("taskComplete");

    logger.error(msg, new RuntimeException());
    throw new IllegalStateException(msg);
  }
  
  public void taskFailed(Task task) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage("taskFailed");

    logger.error(msg);
    throw new IllegalStateException(msg);
  }

  protected void flowStateChanged(Flow flow) {
    String msg = "Flow " + flow.getFlowId() + " has changed state to " + flow.getState();

    flow.flowStateChanged(new FlowStateChangedEvent(flow, msg));
  }

  protected void flowStateChanged(Flow flow, String message) {
    flow.flowStateChanged(new FlowStateChangedEvent(flow, message));
  }

  protected void recordTaskFailureMessage(Flow flow, Task task) {
    String nl = System.getProperty("line.separator");
    
    synchronized(flow.errorMessage) {
      flow.errorMessage.append(task.getErrorMessage() + nl);
    }
  }

  private String getStateTransitionErrorMessage(String message) {
    return "Illegal state transition.  Received: " + message + " while in state: " + toString();
  }

  private void debugLogStates(Flow flow) {
    Map taskStateMap = flow.getTaskStateMap();

    for(Iterator i = taskStateMap.keySet().iterator(); i.hasNext();) {
      String name = (String) i.next();
      String state = (String) taskStateMap.get(name);

      logger.debug(name + "\t" + state);
    }

    logger.debug("runningTaskSet.size(): " + flow.getRunningTasks().size());
  }
}
