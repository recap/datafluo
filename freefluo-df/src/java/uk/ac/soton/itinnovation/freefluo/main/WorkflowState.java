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
//      Created Date        :   2004/01/19
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/08/23 06:24:48 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.main;

import java.io.*;
import java.util.*;

import uk.ac.soton.itinnovation.freefluo.core.flow.FlowState;

/**
 * This is a deliberate duplication of the String definitions of the possible
 * states that a flow can have; as copied from core.flow.FlowState
 * class.  External clients of Freefluo should rely only on this interface and
 * not uk.ac.soton.itinnovation.freefluo.core.flow.FlowState for a stable interface.
 * Task extensions to freefluo should use uk.ac.soton.itinnovation.freefluo.core.flow.FlowState
 */
public class WorkflowState implements Serializable {
  public static final String NEW_STATE = FlowState.NEW.getStateString();
  public static final String RUNNING_STATE = FlowState.RUNNING.getStateString();
  public static final String COMPLETE_STATE = FlowState.COMPLETE.getStateString();
  public static final String FAILING_STATE = FlowState.FAILING.getStateString();
  public static final String FAILED_STATE = FlowState.FAILED.getStateString();
  public static final String CANCELLING_STATE = FlowState.CANCELLING.getStateString();
  public static final String CANCELLED_STATE = FlowState.CANCELLED.getStateString();
  public static final String DESTROYED_STATE = FlowState.DESTROYED.getStateString();
  public static final String PAUSED_STATE = FlowState.PAUSED.getStateString();

  public static final WorkflowState NEW = new WorkflowState(NEW_STATE);
  public static final WorkflowState RUNNING = new WorkflowState(RUNNING_STATE);
  public static final WorkflowState COMPLETE = new WorkflowState(COMPLETE_STATE);
  public static final WorkflowState FAILING = new WorkflowState(FAILING_STATE);
  public static final WorkflowState FAILED = new WorkflowState(FAILED_STATE);
  public static final WorkflowState CANCELLING = new WorkflowState(CANCELLING_STATE);
  public static final WorkflowState CANCELLED = new WorkflowState(CANCELLED_STATE);
  public static final WorkflowState DESTROYED = new WorkflowState(DESTROYED_STATE);
  public static final WorkflowState PAUSED = new WorkflowState(PAUSED_STATE);

  private static HashMap /* flowStateId:Integer -> workflowState:WorkflowState */ stateMap = new HashMap();
  private static HashMap /* flowState:String -> workflowState:WorkflowState */ stateStringMap = new HashMap();
  
  // a map used to translate between interal represenation of flow state and the
  // external representation i.e. WorkflowState.  Some internal states are 
  // hidden from the outside world.
  static {
    stateMap.put(new Integer(FlowState.NEW.getState()), NEW);
    stateMap.put(new Integer(FlowState.RUNNING.getState()), RUNNING);
    stateMap.put(new Integer(FlowState.COMPLETE.getState()), COMPLETE);
    stateMap.put(new Integer(FlowState.FAILING.getState()), FAILING);
    stateMap.put(new Integer(FlowState.FAILED.getState()), FAILED);
    stateMap.put(new Integer(FlowState.CANCELLING.getState()), CANCELLING);
    stateMap.put(new Integer(FlowState.CANCELLED.getState()), CANCELLED);
    stateMap.put(new Integer(FlowState.DESTROYED.getState()), DESTROYED);
    stateMap.put(new Integer(FlowState.PAUSED.getState()), PAUSED);

    stateStringMap.put(NEW_STATE, NEW);
    stateStringMap.put(RUNNING_STATE, RUNNING);
    stateStringMap.put(COMPLETE_STATE, COMPLETE);
    stateStringMap.put(FAILING_STATE, FAILING);
    stateStringMap.put(FAILED_STATE, FAILED);
    stateStringMap.put(CANCELLING_STATE, CANCELLING);
    stateStringMap.put(CANCELLED_STATE, CANCELLED);
    stateStringMap.put(DESTROYED_STATE, DESTROYED);
    stateStringMap.put(PAUSED_STATE, PAUSED);
  }

  private String stateString = null;

  private WorkflowState(String stateString) {
    this.stateString = stateString;
  }

  public boolean equals(WorkflowState rhs) {
    return this.stateString.equals(rhs.stateString);
  }

  public String getStateString() {
    return stateString;
  }

  public boolean isFinal() {
    if(equals(COMPLETE) || equals(FAILED) || equals(CANCELLED)) {
      return true;
    }
    else {
      return false;
    }
  }

  public String toString() {
    return stateString;
  }

  /**
   * Get a workflow state from the integer representation of the
   * FlowState.  This method translates from the internal representation
   * of flow state to the external represenation of a <code>WorkflowState</code><br/>
   * The <code>flowState</code> parameter should
   * have value of one of the constants enumerated in <code>FlowState</code>
   * @param flowState an integer representation of the FlowState
   * @return a WorkflowState to represent the flow state.
   */
  public static WorkflowState getState(int flowState) {
    return(WorkflowState) stateMap.get(new Integer(flowState));
  }

  /**
   * Get a workflow state from the string representation of the workflow state.
   */
  public static WorkflowState getState(String stateString) {
    return(WorkflowState) stateStringMap.get(stateString);
  }
}
