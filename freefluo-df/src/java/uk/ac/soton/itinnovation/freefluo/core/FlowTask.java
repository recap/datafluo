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
//   Created By          :   Nikolaos Matskanis
//   Created Date        :   2004/12/09
//   Created for Project :   MYGRID
//
//
///////////////////////////////////////////////////////////////////////////////


package uk.ac.soton.itinnovation.freefluo.core;

import uk.ac.soton.itinnovation.freefluo.core.flow.*;
import uk.ac.soton.itinnovation.freefluo.core.task.*;
import uk.ac.soton.itinnovation.freefluo.core.event.*;

public abstract class FlowTask extends AbstractTask implements FlowStateListener {

  private Flow taskFlow = null;
  private Object stopObject = new Object();
  private Object pauseObject = new Object();

  public FlowTask(String id, Flow flow, boolean isCritical) {
    //call the constractor of abstract class that creates an interruptible task
    //Nested flows can be both cancelled and paused so are interruptible tasks.
    super(id, null, flow, isCritical, true, true); 
  }
  
  /**
   * Sets the nested flow.
   * Classes that extend this class should set the nested flow by using this method.
   */
  public void setTaskFlow(Flow _taskFlow) {
    taskFlow = _taskFlow;
    taskFlow.addFlowStateListener(this);
  }

  public Flow getTaskFlow() {
    return taskFlow;
  }

  /**
   * Pauses the nested flow.
   * handlePause should be implemented by all interruprtible tasks.
   */
  public void handlePause() { 
    try {
      taskFlow.pause();
    }
    catch(Exception e) {
      fail(e.toString());
    }
  }

  /**
   * Cancels the nested flow.
   * handleCancel should be implemented by all interruprtible tasks.
   */
  public void handleCancel() { 
    try {
      taskFlow.cancel();
    }
    catch(Exception e) {
      fail(e.toString());
    }
  }

  /**
   * Runs the nested flow.
   */
  protected void handleRun(RunEvent runEvent) {
    try {
      taskFlow.run();
    }
    catch(Exception e) {
      fail(e.toString());
    }
  }

  /**
   * Resumes the nested flow.
   * handleResume should be implemented by all interruprtible tasks.
   */
  protected void handleResume(RunEvent runEvent) {
    try {
      if(taskFlow.getState() == FlowState.NEW)handleRun(runEvent);
      else taskFlow.resume();
    }
    catch(Exception e) {
      fail(e.toString());
    }
  }

  /**
   * FlowTask is an abstract task.
   * Classes tha extend this class should put the code related to the specific implementation
   * in this method.
   */
  protected abstract void execute();

  public void flowStateChanged(FlowStateChangedEvent flowStateChangedEvent) {
    FlowState flowState = flowStateChangedEvent.getFlow().getState();

    if(flowState == FlowState.COMPLETE) {
      execute();
      complete();
    }
    if(flowState == FlowState.PAUSED) paused();
    if(flowState == FlowState.FAILED)fail("Task failed!!!");
    if(flowState == FlowState.CANCELLED) {
      System.out.println("Cancelling succedded");
      cancelled();
    }
  }
}

