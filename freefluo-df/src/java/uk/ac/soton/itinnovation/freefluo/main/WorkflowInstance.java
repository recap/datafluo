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
//      Created Date        :   2004/01/22
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/08/23 06:24:48 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.main;

import java.util.*;

import org.apache.log4j.*;

import org.jdom.*;
import org.jdom.output.*;

import uk.ac.soton.itinnovation.freefluo.core.flow.*;
import uk.ac.soton.itinnovation.freefluo.core.task.*;
import uk.ac.soton.itinnovation.freefluo.core.event.*;
import uk.ac.soton.itinnovation.freefluo.lang.scufl.*;
import uk.ac.soton.itinnovation.freefluo.task.*;
import uk.ac.soton.itinnovation.freefluo.event.*;

/**
 * Represents a client workflow instance.  Information
 * related to the clients executable workflow isntance is gathered here, 
 * including the executable workflow, input data, output data (if the
 * instance has been run) and provenance information, generated during workflow 
 * execution etc.
 */
public interface WorkflowInstance {
  Flow getFlow();
  
  FlowContext getFlowContext();
  void setFlowContext(FlowContext flowContext);
  
  void setInput(Map input) throws InvalidInputException;

  void addWorkflowStateListener(WorkflowStateListener workflowStateListener);
  void removeWorkflowStateListener(WorkflowStateListener workflowStateListener);
  
  void run();
  String getStatus();
  
  public Map[] getIntermediateResultsForProcessor(String processorName) throws UnknownProcessorException;
  
  Map getOutput();  
  
  public String getProvenanceXML();
  public String getProgressReportXML();
  public String getErrorMessage();

  public void cancel();
  public boolean pause();
  public boolean resume();
  public void destroy();

	public boolean isDataNonVolatile(String processorName);
	public boolean changeOutputPortTaskData(String processorId,String portId, Object newData);
}
