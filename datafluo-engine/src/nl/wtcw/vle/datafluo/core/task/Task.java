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
//      Created Date        :   2003/05/28
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
import nl.wtcw.vle.datafluo.core.event.*;
import nl.wtcw.vle.datafluo.core.port.*;


/**
 * A Task represents a unit of work.  The engine calls the handleRun()
 * method of task to allow it to do its work.  The engine determines when
 * to call the handleRun() command method according to a task's parent dependencies.
 * Dependencies are established with the linkTo() method of this class.  To make
 * a child task depend on a parent task, link them as follows:
 * <p>
 * parentTask.linkTo(childTask)
 * <p>
 * With this dependency, the childTask will have its handleRun method called only after
 * successful completion of the handleRun() method of the parentTask (and any other parents).
 * <p>
 * Subclass <a href="AbstractTask.html">AbstractTask</a> and implement the handleRun() method to
 * provide a custom task.  Sublcass implementations of handleRun() must indicate the success
 * or failure of a Task by calling one of the fail() or complete() methods of this class.
 * <p>
 */
public interface Task extends RunEventSource {

  /**
   * Get the id of this task
   *
   * @return the tasks unique id 
   */
  public String getTaskId();

  /** 
   * Get the human readable name for this task
   */
  public String getName();

  public void setName(String name);
  /**
   * Get a human readable description of this task
   */
  public String getDescription();

  /**
   * If this task has failed this method retrieves the error message.
   * If the task hasn't failed, an empty String is returned.
   */
  public String getErrorMessage();

  /**
   * Get the task's state
   *
   * @return the present state
   */
  public TaskState getState();
	
  /**
   * Get a simple string represenation of the tasks present state. e.g. NEW, SCHEDULED, 
   * SCHEDULING, SCHEDULED, RUNNING, COMPLETE, CANCELLED, FAILED
   *
   * @return the tasks state as a string
   */
  public String getStateString();
	
  /**
   * If the tasks state is in a final state (complete, failed or cancelled
   * this method returns true
   *
   * @return true if the state is final or false if the task is yet to reach its final state
   */
  public boolean isStateFinal();
	
  /**
   * Get the flow that contains this task
   *
   * @return the tasks flow
   */
  public Flow getFlow();
	
  /**
   * Add a task as a child of this task in the graph
   * This method is typically called by the framework and shouldn't be
   * used by application developers.
   * Use thisTask.linkTo(Task childTask) instead.
   */
  @Deprecated
  public void addChild(Task task);

  /**
   * Add a port to the task. Ports implement a dataflow mechanisim
   * @param port
   */
  public void addInputPort(Port port);
  public void addOutputPort(Port port);
  public Collection getInputPorts();
  public Collection getOutputPorts();
  public boolean containsInputPort(String portId);
  public boolean containsOutputPort(String portId);
	
  /**
   * Get the child tasks of this task in the graph
   *
   * @return a Collection<Task> of child tasks
   */
  @Deprecated
  public Collection getChildren();
	
  /**
   * Get a child task by its taskId
   * 
   * @return the child task with the specified task id or null if 
   * there is no such child
   */
  @Deprecated
  public Task getChild(String parentTaskId);
	
  /**
   * Add a task as a parent of this task in the graph
   * This method is typically called by the framework and shouldn't be
   * used by application developers.
   * Use parentTask.linkTo(Task thisTask) instead.
   */
  @Deprecated
  public void addParent(Task task);
	
  /**
   * Get the parent tasks for this task in the graph
   *
   * @return a Collection<Task> of parent tasks 
   */
  @Deprecated
  public Collection getParents();

  /**
   * Get a parent task by its taskId
   * 
   * @return the parent task with the specified task id or null if 
   * there is no such parent
   */
  @Deprecated
  public Task getParent(String parentTaskId);
	
  /**
   * Make a dependency link (a data link typically) between this task
   * and a child.
   * This is a convenience method.  The implementation should call addChild() 
   * and addParent() appropriately
   * 
   */
  @Deprecated
  public void linkTo(Task childTask);
  
  /**
   * Make a dependency link between tasks by connecting ports
   * @param localPort
   * @param remotePort
   */

  public void linkTo(Port localPort, Port remotePort) throws PortConnectionException ;

  /**
   * Ready this task to run.
   */
  //public void ready();

	
  /**
   * Run this task
   */
  public void run(RunEvent event);

  /**
   * Destroy this task.  Implementing classes provide their own
   * custom clean up code.
   * destroy() typically will be called after a Flow is in a final
   * state and its tasks should destruct.
   */
  public void destroy();

  //protected void complete();

  /**
   * Pause this task. Tasks typically don't support pausing. 
   * This method marks (places a breakpoint) the task no to start running.
   *
   * @return true on success of marking 
   */
  public boolean pause();
  
  /**
   * Resume this task. Tasks typically don't support pausing. 
   * This method removes a breakpoint and starts running the task.
   *
   * @return true on success 
   */
  public boolean resume(RunEvent event);
	
  /**
   * Returns if the data of this task can be edited.  
   *
   * @return true on non-volatile data. 
   */
  public boolean isDataNonVolatile();

  /**
   * Add a breakpoint. 
   * This method adds a breakpoint to the task.
   */
  public void addBreakpoint();

  /**
   * Remove a breakpoint. 
   * This method Remove a breakpoint to the task.
   */
  public void removeBreakpoint();
 
  /**
   * Check this task for breakpoints. 
   * This method checks if this task was marked to pause or
   *  contains a breakpoint
   *
   * @return true if it does
   */
  public boolean isPause();

  /**
   * Cancel this task. Tasks typically don't support cancelling while running. 
   * This method cancels the task if it is paused.
   */
  public void cancel();

  /**
   * Add an observer of this task's state
   */
  public void addTaskStateListener(TaskStateListener taskStateListener);

  /**
   * Remove an observer of this task's state
   */
  public void removeTaskStateListener(TaskStateListener taskStateListener);
	
  /**
   * Add an observer of arbitrary (application specifice) events generated by this 
   * Task.
   */
  public void addTaskEventListener(TaskEventListener taskEventListener);

  /**
   * Remove an observer of arbitrary events generated by this Task.
   */
  public void removeTaskEventListener(TaskEventListener taskEventListener);

  public void portStateChanged(PortStateChangedEvent portStateChangedEvent);

  /**
   * This method is used by the containing <code>Flow</code> when this <code>Task</code>
   * fails. If this method returns <code>true</code> the <code>Flow</code> will fail itself.
   * If the call to this method returns false the <code>Flow</code> will continue to allow
   * other (non-dependent) tasks to run.
   */
  public boolean isCritical();

  /**
   * This method is used to set the task to interruptible state.
   * Interruptible tasks can be cancelled and paused while running.
   * Tasks must support this function by overriding the hanldePause and 
   * handleCancel methods.
   public void setInterruptible(boolean isInterruptible);
   */

  /**
   * This method is used to define if the task can be cancelled while running.
   * If Tasks support this function, they are interruptible and must override handleCancel method.
   */
  public boolean isCancelable();

  /**
   * This method is used to define if the task can be paused while running.
   * If Tasks support this function, they are interruptible and must override handlePause method.
   */
  public boolean isPauseable();

  /**
   * Set whether or not this task is critical to the workflow. If 
   * <code>isCritical</code> is <code>true</code> and this <code>Task</code>
   * fails during workflow execution, the containing <code>Flow</code> will fail also.
   * Otherwise, if <code>isCritical</code> is <code>false</code> and this
   * <code>Task</code> 1fails during workflow execution, the containing <code>Flow</code> will 
   * continue execution.
   */
  public void setCritical(boolean isCritical);

  /**
   * Debugging method.  Provides a string representation of the tasks members.
   */
  public String toString();
	
}
