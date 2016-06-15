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
//      Created Date        :   2004/11/16
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

import uk.ac.soton.itinnovation.freefluo.core.task.*;

public class FailingState extends FlowState {
  protected FailingState(String stateString, int state) {
    super(stateString, state);
  }

  public void cancel(Flow flow) {}

  public void pause(Flow flow) {}

  //Resumes a specific task from paused state.
  public void resumeTask(Flow flow, String taskId) {
    flow.resumeTaskById(taskId);
  }

  public void taskFailed(Task task) {
    Flow flow = task.getFlow();
    flow.removeRunningTask(task);

    if(flow.getRunningTasksSize() + flow.getPausedTasksSize() == 0) {
      flow.handleFail();
      flow.setState(FlowState.FAILED);
      flowStateChanged(flow);
    }
  }

  public void taskComplete(Task task) {
    Flow flow = task.getFlow();
    flow.removeRunningTask(task);

    if(flow.getRunningTasksSize() + flow.getPausedTasksSize() == 0) {
      flow.handleFail();
      flow.setState(FlowState.FAILED);
      flowStateChanged(flow);
    }
  }

  /**
   * Handles task's cancelled event.
   * matskan
   */
  public void taskCancelled(Task task) {
    Flow flow = task.getFlow();
    flow.removeRunningTask(task);
    flow.removePausedTask(task);

    if(flow.getRunningTasksSize() + flow.getPausedTasksSize() == 0) {
      flow.handleFail();
      flow.setState(FlowState.FAILED);
      flowStateChanged(flow);
    }
  }

  /** 
   * Tasks pausing while the flow is in failing state need to be handled.
   * The number of paused tasks is increased. 
   */
  public void taskPaused(Task task) {
    Flow flow = task.getFlow();
    flow.removeRunningTask(task); //if was in running tasks list will be removed. 
    flow.addPausedTask(task);
  }

  public void taskRunning(Task task) {
    Flow flow = task.getFlow();
    flow.removePausedTask(task);
    flow.addRunningTask(task);
  }

}
