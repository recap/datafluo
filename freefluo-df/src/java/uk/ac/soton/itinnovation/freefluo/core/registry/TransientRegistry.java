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

package uk.ac.soton.itinnovation.freefluo.core.registry;

import java.util.*;

import uk.ac.soton.itinnovation.freefluo.core.flow.*;

/**
 * This class is a concrete implementation for the FlowRegistry interface. It
 * provides a transient registry that is held purely in memory and has no
 * persistent back-end, you have been warned.
 */
public class TransientRegistry extends FlowRegistry {

  private HashMap flowsMap = new HashMap();
  private ArrayList listeners = new ArrayList();

  public synchronized String getUniqueId(String description) {
    StringBuffer sb = new StringBuffer("FlowID:" + description + ":");
 
    try {
      Thread.sleep(1);	
    }
    catch(InterruptedException ex) {}
       
    sb.append(Long.toString(System.currentTimeMillis()));
    return sb.toString();
  }

  public synchronized void addFlow(Flow flow) {
    flowsMap.put(flow.getFlowId(), flow);
  }

  public synchronized Flow removeFlow(String flowID) {
    return(Flow) flowsMap.remove(flowID);
  }

  public Flow getFlow(String flowId) throws IllegalArgumentException {
    return(Flow) flowsMap.get(flowId);
  }

  public Flow[] getFlows() {
    Collection flowsList = flowsMap.values();

    return(Flow[]) flowsList.toArray(new Flow[flowsList.size()]);
  }
}
