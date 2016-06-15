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
//      Created Date        :   2004/11/29
//      Created for Project :   MYGRID
//      Dependencies        :
//
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.core.flow;

import uk.ac.soton.itinnovation.freefluo.core.task.*;
import java.util.*;

public class PausedState extends FlowState {
  protected PausedState(String stateString, int state) {
    super(stateString, state);
  }

  public void pause(Flow flow) {}

  //Cancels flow's execution.
  public void cancel(Flow flow) {
    flow.setState(FlowState.CANCELLING);
    flowStateChanged(flow);
    flow.cancelTasks();
  }
		
  //Resumes flow from paused state.
  public void resume(Flow flow) {
    //flow.setState(FlowState.RUNNING);
    //flowStateChanged(flow);
    flow.resumeTasks();
  }

  //Resumes a specific task from paused state.
  public void resumeTask(Flow flow, String taskId) {
    flow.resumeTaskById(taskId);
  }

  /** 
   * Tasks resuming while the flow is in paused state need to be handled.
   * This method handles the taskRunning event they send. 
   * Since one task is resumed and running after the execution of this method,
   * the flow state goes to running.
   */
  public void taskRunning(Task task) {
    Flow flow = task.getFlow();
    flow.removePausedTask(task);
    flow.addRunningTask(task);
    flow.setState(FlowState.RUNNING);
    flowStateChanged(flow);
  }

  /*// Handles the cancel signal on paused tasks, received in flow's paused state.
   public void taskCancelled(Task task) {
   Flow flow = task.getFlow();
   flow.removePausedTask(task);
   if (task.isCancelable())flow.removeRunningTask(task); //if was in running tasks list will be removed. 
   if (flow.getRunningTasksSize()+flow.getPausedTasksSize()==0){
   flow.setState(FlowState.CANCELLED);
   flowStateChanged(flow);
   }
   }*/


  /** 
   * Tasks pausing while the flow is in paused state need to be handled.
   * Matskan Note: This shouldn't be happening. But since it does because of the pull mechanism
   * in pausing tasks, should be handled. 
   * The number of paused tasks is increased. 
   public void taskPaused(Task task){
   Flow flow = task.getFlow();
   if (flow.getStartTasks().contains(task) || task.isPauseable())flow.removeRunningTask(task); //if was in running tasks list will be removed. 
   flow.addPausedTask(task);
   }
   */


}
