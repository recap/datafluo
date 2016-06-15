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
//      Created By          :   Nikolaos Matskanis
//      Created Date        :   2004/12/07
//      Created for Project :   Freefluo
//      Dependencies        :
//
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.core.task;

import uk.ac.soton.itinnovation.freefluo.core.event.*;

public class InterruptibleRunState extends TaskState {
  protected InterruptibleRunState(String stateString, int state) {
    super(stateString, state);
  }

  /**
   * Pauses a running task.
   * The handlePause method is called. 
   * It should be implemented by all interruptible tasks.
   */
  public void pause(AbstractTask task) {
    task.handlePause();	  
  }

  /**
   * Indicates that interruptible task is now paused.
   * Changes state to PAUSED.
   */
  public void paused(AbstractTask task) {
    task.stopTimer();
    task.setState(TaskState.PAUSED);
    taskStateChanged(task);
    task.taskPaused();
  }

  /**
   * Cancels a running task.
   * The handleCancel method is called. 
   * It should be implemented by all interruptible tasks.
   */
  public void cancel(AbstractTask task) {
    task.handleCancel();
  }

  /**
   * Interruptible task is now cancelled.
   * Changes state to CANCELLED.
   */
  public void cancelled(AbstractTask task) {
    task.stopTimer();
    task.setState(TaskState.CANCELLED);
    taskStateChanged(task);
    task.taskCancelled();
  }

  public void complete(AbstractTask task) {
    task.stopTimer();
    task.setCompleted();
    if(task.isPause()) { //i.e. has breakpoint
      task.setState(TaskState.PAUSED);
      task.taskPaused();
      taskStateChanged(task);
    }
    else { 
      task.handleComplete();
      task.setState(TaskState.COMPLETE);
      taskStateChanged(task);
    }
  }
		
  public void fail(AbstractTask task, String message) {
    task.stopTimer();

    task.handleFail();

    task.setErrorMessage(message);
    task.setState(TaskState.FAILED);
    taskStateChanged(task, message);
  }

}

