//////////////////////////////////////////////////////////////////////////////////
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
//      Created Date        :   2003/5/16
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

public class RunningState extends FlowState {

  //private Logger logger = Logger.getLogger(RunningState.class);
  
  protected RunningState(String stateString, int state) {
    super(stateString, state);
  }

  public void cancel(Flow flow) {
    flow.setCancelTasks(true);
    flow.setState(FlowState.CANCELLING);
    flowStateChanged(flow);
    flow.cancelTasks();
  }

  /**
   * Implemetns the pausing of the flow.
   */
  public void pause(Flow flow) {
    if(!flow.isPausing()) {
      flow.setPausing();
      flow.pauseTasks();
    }
  }

  public void resume(Flow flow) {
    if(flow.isPausing()) {
      flow.unsetPausing();
      flow.resumeTasks();
    }
  }

  //Pauses a specific task.
  public void pauseTask(Flow flow, String taskId) {
    flow.pauseTaskById(taskId);
  }

  //Resumes a specific task from paused state.
  public void resumeTask(Flow flow, String taskId) {
    flow.resumeTaskById(taskId);
  }

  public void taskRunning(Task task) {
    Flow flow = task.getFlow();
    flow.removePausedTask(task);
    flow.addRunningTask(task);
  }

  /* Handles pause evens for paused tasks*/
  public void taskPaused(Task task) {
    Flow flow = task.getFlow();
    flow.removeRunningTask(task);
    flow.addPausedTask(task);
    if(flow.getRunningTasksSize() == 0) {
      flow.unsetPausing(); // set Pausing to false;
      flow.setState(FlowState.PAUSED);	
      flowStateChanged(flow);
    }
  }

  /* Handles cancel evens for paused tasks*/
  public void taskCancelled(Task task) {
    Flow flow = task.getFlow();
    flow.removePausedTask(task);
    if(task.isCancelable())flow.removeRunningTask(task);
  }

  /**
   * Handles complete events.
   * Changes flow's state to complete or paused is there are no running tasks.
   */
  public void taskComplete(Task task) {
    Flow flow = task.getFlow();
    flow.removeRunningTask(task);
    flow.removePausedTask(task);

    if(flow.getRunningTasksSize() + flow.getPausedTasksSize() == 0) {
      flow.handleComplete();
      flow.setState(FlowState.COMPLETE);
	  //System.err.println("FLOW COMPLETE");
      flowStateChanged(flow);
    }
    else if(flow.getRunningTasksSize() == 0 && flow.getPausedTasksSize() > 0) {
      flow.unsetPausing(); // set Pausing to false;
      flow.setState(FlowState.PAUSED);	
      flowStateChanged(flow);
    }
  }

  public void taskFailed(Task task) {
    Flow flow = task.getFlow();
    super.recordTaskFailureMessage(flow, task);
    //Set runningTasks = flow.getRunningTasks(); 
    //Set pausedTasks=flow.getPausedTasks();
    flow.removeRunningTask(task);
    //runningTasks.remove(task);    
    
    if(!task.isCritical() && flow.getRunningTasksSize() + flow.getPausedTasksSize() > 0) {
      // The task failed but it wasn't critical.
      // There are some tasks either paused or running.
      // If both do nothing, the state is RUNNINING with sub-state Pausing
      // If only runninng do nuthing, the state is RUNNINING with sub-state NotPausing
      if(flow.isPausing() && flow.getRunningTasksSize() == 0) {
        // If there aren't any tasks running, the flow is PAUSED.
        flow.unsetPausing(); // set Pausing to false;
        flow.setState(FlowState.PAUSED);
        flowStateChanged(flow);
      }
    }
    else if(!task.isCritical() && 
      flow.getRunningTasksSize() + flow.getPausedTasksSize() == 0) {
      // The task failed but it wasn't critical.
      // Futhermore, there aren't any other tasks running or are paused.
      // Therefore, the flow is COMPLETE.
      flow.handleComplete();
      flow.setState(FlowState.COMPLETE);	
      flowStateChanged(flow);
    }
    else if(task.isCritical() && 
      flow.getRunningTasksSize() + flow.getPausedTasksSize() > 0) {
      // The task failed and it is critical. Futhermore, 
      // there are other tasks running/are paused. Therefore, the flow
      // cancels the remaining Tasks and moves to the FAILING 
      // state.
      flow.setCancelTasks(true);
      flow.setState(FlowState.FAILING);
      flowStateChanged(flow);
    }
    else if(task.isCritical() &&
      flow.getRunningTasksSize() + flow.getPausedTasksSize() == 0) {
      // The task failed and it is critical. Futhermore, 
      // there aren't any other Tasks running or are paused. Therefore, the
      // flow proceeds immediately to the FAILED state.
      flow.handleFail();
      flow.setState(FlowState.FAILED);
      flowStateChanged(flow);
    }
  }

}
