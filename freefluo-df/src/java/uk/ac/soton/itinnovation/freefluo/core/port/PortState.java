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
package uk.ac.soton.itinnovation.freefluo.core.port;

import uk.ac.soton.itinnovation.freefluo.core.event.*;

/**
 * Superclass for all the possible states for the core.
 *
 */
public abstract class PortState {
  public static final int NEW_STATE = 0;
  public static final int ENABLED_STATE = 1;
  public static final int DISABLED_STATE = 3;
  public static final int ACTIVE_STATE = 4;
  public static final int DESTROYED_STATE = 6;
  
  
		
  public static PortState NEW = new NewState("NEW", NEW_STATE);
  public static PortState ENABLED = new EnabledState("ENABLED", ENABLED_STATE);
  public static PortState DISABLED = new DisabledState("DISABLED", DISABLED_STATE);
  public static PortState ACTIVE = new ActiveState("ACTIVE", ACTIVE_STATE);
  public static PortState DESTROYED = new DestroyedState("DESTROY", DESTROYED_STATE);
  		
  //private static Logger logger = Logger.getLogger(PortState.class);
  private String stateString = null;
  private int state;

  protected PortState(String stateString, int state) {
    this.stateString = stateString;
    this.state = state;
  }

  public void enable(Port port) throws IllegalStateException{
	  String msg = getStateTransitionErrorMessage(port, "enable");

    throw new IllegalStateException(msg);
  }
  public void disable(Port port) throws IllegalStateException{
	  String msg = getStateTransitionErrorMessage(port, "disable");

    throw new IllegalStateException(msg);
  }
  public void activate(Port port, DataEvent dataEvent) throws IllegalStateException{
	  String msg = getStateTransitionErrorMessage(port, "active");

    throw new IllegalStateException(msg);
  }
  
  public void destroy(Port port, DataEvent dataEvent) throws IllegalStateException {
    String msg = getStateTransitionErrorMessage(port, "destroy");

    //logger.error(msg);
    throw new IllegalStateException(msg);
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


  protected void portStateChanged(Port port) {
    PortStateChangedEvent portStateChangedEvent =
      new PortStateChangedEvent(port, "Port " + port.getDescription() + " has changed state to " + port.getState());

    port.portStateChanged(portStateChangedEvent);
  }

  protected void portStateChanged(Port port, String message) {
    PortStateChangedEvent portStateChangedEvent = new PortStateChangedEvent(port, message);

    port.portStateChanged(portStateChangedEvent);
  }

  private String getStateTransitionErrorMessage(Port port, String message) {
    return "Illegal state transition.  Port " + port.getDescription() + ".  Received: " + message + " while in state: " + toString();
  }
}
