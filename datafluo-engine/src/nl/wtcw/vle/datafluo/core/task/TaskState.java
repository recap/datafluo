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
package nl.wtcw.vle.datafluo.core.task;


import nl.wtcw.vle.datafluo.core.event.*;

/**
 * Superclass for all the possible states for the core.
 *
 */
public abstract class TaskState implements PortStateListener {
  public static final int NEW_STATE = 0;
 // public static final int READY_STATE = 8;
  public static final int RUNNING_STATE = 1;
  public static final int COMPLETE_STATE = 3;
  public static final int FAILED_STATE = 4;
  public static final int CANCELLED_STATE = 5;
  public static final int DESTROYED_STATE = 6;
  public static final int PAUSED_STATE = 2;
  public static final int IRUN_STATE = 7;
  public static final int WAIT_STATE = 8;
		
  public static TaskState NEW = new NewState("NEW", NEW_STATE);
  
  public static TaskState RUNNING = new RunningState("RUNNING", RUNNING_STATE);
  public static TaskState IRUN = new InterruptibleRunState("IRUN", IRUN_STATE);
  public static TaskState COMPLETE = new CompleteState("COMPLETE", COMPLETE_STATE);
  public static TaskState FAILED = new FailedState("FAILED", FAILED_STATE);
  public static TaskState CANCELLED = new CancelledState("CANCELLED", CANCELLED_STATE);
  public static TaskState DESTROYED = new DestroyedState("DESTROYED", DESTROYED_STATE);
  public static TaskState PAUSED = new PausedState("PAUSED", PAUSED_STATE);
  public static TaskState WAITING = new WaitingState("WAITING", WAIT_STATE);
		
  //private static Logger logger = Logger.getLogger(TaskState.class);
  private String stateString = null;
  private int state;

  protected TaskState(String stateString, int state) {
    this.stateString = stateString;
    this.state = state;
  }

  public void portStateChanged(PortStateChangedEvent portStateChangedEvent) {
	// TODO
  }

  
  /*matskan: Modified to implement pausing of tasks*/
  public void run(AbstractTask task, RunEvent runEvent) throws IllegalStateException {
    String msg = null;
    if(!task.isPause())
      msg = getStateTransitionErrorMessage(task, "run");
    else msg = getStateTransitionErrorMessage(task, "run while isPause is true");

    //logger.error(msg);
    throw new IllegalStateException(msg);
  }

  /*matskan:Added to implement resuming of tasks from paused state*/
  public void resume(AbstractTask task, RunEvent runEvent) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage(task, "resume");

    //logger.error(msg);
    throw new IllegalStateException(msg);
  }

  /*matskan:Added to implement pausing of tasks */
  public void pause(AbstractTask task) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage(task, "pause !!!");

    //logger.error(msg);
    throw new IllegalStateException(msg);
  }

  public void complete(AbstractTask task) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage(task, "complete");

    //logger.error(msg);
    throw new IllegalStateException(msg);
  }

  /*matskan: Added to implement pausing of interruptable tasks*/
  public void paused(AbstractTask task) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage(task, "Paused");

    //logger.error(msg);
    throw new IllegalStateException(msg);
  }

  /*matskan: Added to implement pausing of interruptable tasks*/
  public void cancelled(AbstractTask task) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage(task, "Cancelled");

    //logger.error(msg);
    throw new IllegalStateException(msg);
  }

  public void wait(AbstractTask task) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage(task, "Wait");

    //logger.error(msg);
    throw new IllegalStateException(msg);
  }

  /*matskan: Added to implement cancelling of tasks from paused state*/
  public void cancel(AbstractTask task) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage(task, "cancel");

    //logger.error(msg);
    throw new IllegalStateException(msg);
  }

  public void fail(AbstractTask task, String message) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage(task, "fail");

    //logger.error(msg);
    throw new IllegalStateException(msg);
  }
  
  public void destroy(AbstractTask task) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage(task, "destroy");

    //logger.error(msg);
    throw new IllegalStateException(msg);
  }

  public boolean isDataNonVolatile(AbstractTask task) {
    return false;
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

  protected void taskStateChanged(AbstractTask task) {
    TaskStateChangedEvent taskStateChangedEvent = 
      new TaskStateChangedEvent(task, "AbstractTask " + task.getDescription() + " has changed state to " + task.getState()); 	

    task.taskStateChanged(taskStateChangedEvent);
  }

  protected void taskStateChanged(AbstractTask task, String message) {
    TaskStateChangedEvent taskStateChangedEvent = new TaskStateChangedEvent(task, message); 	

    task.taskStateChanged(taskStateChangedEvent);
  }

  private String getStateTransitionErrorMessage(AbstractTask task, String message) {
    return "Illegal state transition.  AbstractTask " + task.getDescription() + ".  Received: " + message + " while in state: " + toString();
  }
}
