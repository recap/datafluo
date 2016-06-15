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

import java.util.*;
import uk.ac.soton.itinnovation.freefluo.core.event.*;
import uk.ac.soton.itinnovation.freefluo.core.task.*;


public class EnabledState extends PortState {
  //private Logger logger = Logger.getLogger(NewState.class);
  
  protected EnabledState(String stateString, int state) {
    super(stateString, state);
  }

  /**
   * Activate connected ports.
   * @param port
   */
	@Override
  public void activate(Port port, DataEvent dataEvent){

	  Port tmpPort = null;
	  if(port.getDirection() == Port.DIRECTION_OUT){
		for(Iterator i = port.getConnections().iterator(); i.hasNext();) {
			tmpPort = (Port) i.next();
			if(tmpPort.getDirection() == Port.DIRECTION_IN){
				tmpPort.activate(new DataEvent(port));
			}//if
		
		}//for
	  }//if

	  if(port.getDirection() == Port.DIRECTION_IN) {
		  tmpPort = (Port)dataEvent.getSource();
		  //Task task = tmpPort.getTask();
		  port.getTask().run(new RunEvent(tmpPort));
	  }//if

	  port.setState(PortState.ACTIVE);	  
	  portStateChanged(port);
  }
	@Override
  public void disable(Port port){
	  port.setState(PortState.DISABLED);
	  portStateChanged(port);
  }

  @Override
  public void destroy(Port port, DataEvent dataEvent) {
    //port.handleDestroy();
    port.setState(PortState.DESTROYED);
    portStateChanged(port);
  }
}
