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
//      Created Date        :   2004/11/24
//      Created for Project :   MYGRID
//      Dependencies        :
//
//
///////////////////////////////////////////////////////////////////////////////////////
package nl.wtcw.vle.datafluo.core.task;



import nl.wtcw.vle.datafluo.core.event.*;

public class PausedState extends TaskState {
  //private Logger logger = Logger.getLogger(PausedState.class);
  
  protected PausedState(String stateString, int state) {
    super(stateString, state);
  }

  /**
   * Resumes a paused task.
   * Changes state to RUNNING.
   */
  public void resume(final AbstractTask task, final RunEvent runEvent) {

    //first Reset task.runEvents
    task.runEvents.clear();
	
    // A thread is created(here) and started(later) for the task to run
    // since it has not been started for the run event.
    Thread resumeThread = new Thread() {
        public void run() {
          task.handleRun(runEvent);
        }
      };

    //This thread is for interruptible tasks only,
    //since they can be paused while they are running.
    Thread iResumeThread = new Thread() {
        public void run() {
          task.handleResume(runEvent);
        }
      };

    if (!task.isPause()) //Ok flow is resuming but task may have a breakpoint!
    if(task.isCompleted()) {
      task.handleComplete();
      task.setState(TaskState.COMPLETE); 
      taskStateChanged(task);
      task.taskComplete();
    }
    else if(task.isCancelable() || task.isPauseable()) {
      task.taskResumed();
      task.setState(TaskState.IRUN);
      taskStateChanged(task);
      if(task.getState() != TaskState.NEW) iResumeThread.start();
      else {
        resumeThread.start();
      }
    }

    /*else {
     task.taskResumed();
     task.setState(TaskState.RUNNING);
     taskStateChanged(task);
     resumeThread.start(); 
     }*/
    //}
  }

  /**
   * Cancels a paused task.
   * Changes state to CANCELLED.
   */
  public void cancel(AbstractTask task) {
    if(task.isPauseable()) task.handleCancel(); 
    //matskan-NOTE: Pause-able tasks are those who can be paused while running 
    else { 
      task.stopTimer();
      task.setState(TaskState.CANCELLED);
      taskStateChanged(task);
      task.taskCancelled();
    }
  }
	
  /**
   * Interruptible task is now cancelled.
   * Changes state to CANCELLED.
   * Used only by interruptible tasks.
   */
  public void cancelled(AbstractTask task) {
    task.stopTimer();
    task.setState(TaskState.CANCELLED);
    taskStateChanged(task);
    task.taskCancelled();
  }

  /**
   * Interruptible task is failed while being cancelled.
   * Changes state to FAILED.
   * Added for interruptible tasks.
   */
  public void fail(AbstractTask task, String message) {
    task.stopTimer();

    task.handleFail();

    task.setErrorMessage(message);
    task.setState(TaskState.FAILED);
    taskStateChanged(task, message);
  }

  public boolean isDataNonVolatile(AbstractTask task) {
    return task.isCompleted();
  }

}
