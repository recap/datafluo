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

package nl.wtcw.vle.datafluo.core.event;

import java.util.*;

import nl.wtcw.vle.datafluo.core.flow.*;

public class FlowStateChangedEvent {
  private Flow flow = null;
  private String message = null;
  private boolean isStateFinal = false;
  private int state;
  private String stateString;

  public FlowStateChangedEvent(Flow flow) {
    this(flow, "");
  }

  public FlowStateChangedEvent(Flow flow, String message) {
    this.flow = flow;
    this.message = message;
    this.isStateFinal = flow.isStateFinal();
    stateString = flow.getStateString();
    state = flow.getState().getState();
  }

  /**
   * Get the Flow that generated this event.  Note that operations invoked on the returned Flow
   * may not be thread safe.  In particular, do not examine the Flows's state through this
   * reference as other threads may have changed the state since this event
   * was generated.
   */
  public Flow getFlow() {
    return flow;
  }

  public String getMessage() {
    return message;
  }

  public boolean isStateFinal() {
    return isStateFinal;
  }

  public int getState() {
    return state;
  }

  public String getStateString() {
    return stateString;
  }

  public String toString() {
    String nl = System.getProperty("line.separator");
    StringBuffer sb = new StringBuffer();

    sb.append("FlowStateChangedEvent: " + nl);
    sb.append("\tflow: " + flow.toString() + nl);
    sb.append("\tmessage: " + message);
    return sb.toString();
  }
}
