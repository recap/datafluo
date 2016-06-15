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
////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.main;

import java.util.*;

import uk.ac.soton.itinnovation.freefluo.lang.*;
import uk.ac.soton.itinnovation.freefluo.core.flow.*;
import uk.ac.soton.itinnovation.freefluo.core.port.*;
import uk.ac.soton.itinnovation.freefluo.event.*;
import uk.ac.soton.itinnovation.freefluo.conf.*;

public interface Engine {

  EngineConfiguration getEngineConfiguration();

  /**
   * Get information on all the workflows that are deployed and available from
   * this engine.
   * <p>
   * A collection of <code>WorkflowDescription</code> objects is returned. Each
   * object describes a deployed workflow.
   */
  Collection getWorkflowDescriptions();

  /**
   * Create a new workflow instance.
   * @param workflowDefinitionId specified a workflow definition from which to create the new instance.
   * @param accountId the account to charge the workflow use to.
   * @return workflowInstanceId locally unique identifier for the workflow instance
   */
  String newWorkflowInstance(String accountId, String workflowDefinitionId) throws UnknownWorkflowDefinitionException;

  /**
   * Compiles a workflow from <code>workflowDefinition</code>.
   * The method returns the unique identifier for the workflow
   * instance immediately after the worklow intstance has been
   * created.
   * @param workflowDefinition an XML representation of the workflow
   * @return workflowInstanceId
   */
  String compile(String workflowDefinition) throws BadlyFormedDocumentException, ParsingException, PortConnectionException;
  
  /** 
   * Set workflow input. Takes a Map input data,
   * with the keys in the map being String objects corresponding
   * to the named workflow inputs within the workflow that
   * this instance represents the state of.
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   * @throws InvalidInputException if the input is null, it doesn't map correctly onto inputs in the workflow, or if
   * it is otherwise formatted incorrectly.
   */
  void setInput(String workflowInstanceId, Map inputMap) throws UnknownWorkflowInstanceException, InvalidInputException;

  /**
   * Sets context information for a workflow instance.
   */
  void setFlowContext(String workflowInstanceId, FlowContext flowContext) throws UnknownWorkflowInstanceException, InvalidFlowContextException;

  /**
   * Get the <code>FlowContext</code> associated with the workflow instance identified by <code>workflowInstanceId</code>
   */
  FlowContext getFlowContext(String workflowInstanceId) throws UnknownWorkflowInstanceException;

  WorkflowInstance getWorkflowInstance(String workflowInstanceId) throws UnknownWorkflowInstanceException;

  /**
   * Run a workflow instance that's already been compiled and been assigned
   * <code>workflowInstanceId</code> as it's unique identifier.
   * If required, inputs should be set before calling this method.
   * @param workflowInstanceId the unique identifier for the compiled workflow instance you wish to run.
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   */
  void run(String workflowInstanceId) throws UnknownWorkflowInstanceException;

  /**
   * This method can be used to get a simple String that describes
   * the current state of a particular workflow insntance, identifieid
   * by <code>workflowInstanceId</code>.  The possible values for the return
   * value are enumerated in <code>WorkflowState</code>.
   * @return simple workflow status string
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   * @see uk.ac.soton.itinnovation.freefluo.main.WorkflowState
   */
  String getStatus(String workflowInstanceId) throws UnknownWorkflowInstanceException;

  /**
   * Register an observer of the state of a workflow instance.  The <code>WorkflowStateListener</code>
   * will be sent events when the state of the workflow instance changes.
   * @param workflowInstanceId the unique identifier of the workflow instance to observe
   * @param workflowStateListener the observer to notify when the workflow instance state changes
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   * @see uk.ac.soton.itinnovation.freefluo.event.WorkflowStateListener
   */
  void addWorkflowStateListener(String workflowInstanceId, WorkflowStateListener workflowStateListener) throws UnknownWorkflowInstanceException;
  
  /**
   * Remove an observer of the state of a workflow instance.  The <code>WorkflowStateListener</code>
   * will no longer be sent notifications when the worklow instance with id <code>workflowInstanceId</code>
   * changes state.
   * @param workflowInstanceId the unique identifier of the workflow instance being observed
   * @param workflowStateListener the observer to remove
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   * @see uk.ac.soton.itinnovation.freefluo.event.WorkflowStateListener
   */
  void removeWorkflowStateListener(String workflowInstanceId, WorkflowStateListener workflowStateListener) throws UnknownWorkflowInstanceException;

  /**
   * Return the XML string of the status document
   * @return XML progress report document
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   */
  String getProgressReportXML(String workflowInstanceId) throws UnknownWorkflowInstanceException;

  /**
   * Return an array of Maps containing intermediate results
   * for the named processor. The array is always exactly two 
   * items long, and each item is a Map containing port
   * names as keys (String) and data objects as the values present
   * on those ports. The Map at position 0 in the array contains
   * the input values to the processor, that at position 1
   * the output values. If the processor has no output or input
   * values populated then these maps will be empty but will
   * still be returned.
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance 
   * with id <code>workflowInstanceId</code>
   * @exception UnknownProcessorException thrown if the named processor is not 
   * present in the workflow instance.
   */
  Map[] getIntermediateResultsForProcessor(String workflowInstanceId, String processorName)  throws UnknownWorkflowInstanceException, UnknownProcessorException;
  
  /**
   * Return a Map &lt;sinkName:String->data:Object&gt;
   * containing all the set of results in the form of the baclava data model defined by
   * the org.embl.ebi.escience.baclava package. 
   * Outputs that have not yet been populated with data will not appear in
   * the output document.
   * @return Map of data outputs
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   */
  Map getOutput(String workflowInstanceId) throws UnknownWorkflowInstanceException;

  /**
   * Returns a human readable string containing details of errors 
   * that occurred during execution of the workflow
   * identified by <code>workflowInstanceId</code>
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   */
  String getErrorMessage(String workflowInstanceId) throws UnknownWorkflowInstanceException;
  
  /**
   * Return the XML string containing the provenance report,
   * this document is currently poorly defined but will in
   * the future contain the set of RDF statements generated
   * by the knowledge collection code.
   * @return XML provenance doument
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   */
  String getProvenanceXML(String workflowInstanceId) throws UnknownWorkflowInstanceException;
  
  /**
   * Pause the workflow enactment. This consists of setting the
   * paused boolean flag, then cancelling all running workflow
   * processes. Be aware therefore that calling this is not always
   * safe as it may interrupt processes that maintain some kind
   * of external state.
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   */
  void pauseExecution(String workflowInstanceId) throws UnknownWorkflowInstanceException;
  
  /**
   * Resume the workflow enactment.
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   */
  void resumeExecution(String workflowInstanceId) throws UnknownWorkflowInstanceException;

  /**
   * Return whether the workflow is currently paused
   * @return boolean true if the workflow is paused
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   */
  boolean isPaused(String workflowInstanceId) throws UnknownWorkflowInstanceException; 

  /**
   * Add breakoint to the specific processor. 
   * This consists of setting the hasBreakpoint boolean flag.
   * The procesor will pause when it is about to run.
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   */
  public void pause(String workflowInstanceId, String processorId) throws UnknownWorkflowInstanceException;

  /**
   * Checks if task's output is allowed to be editied.
   *
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   */
  public boolean isDataNonVolatile(String workflowInstanceId, String processorId) throws UnknownWorkflowInstanceException;


	/**
	 * Changes intermediate output data on the output porttask.
	 * @return true if data is non volatile and 
	 * the changes can be performed.
	 */
	public boolean changeOutputPortTaskData(String workflowInstanceId, String processorId, String OutputPortName, Object newData) throws  UnknownWorkflowInstanceException/*, UnknownProcessorException*/;

  /**
   * Remove breakoint to the specific processor and resume it. This consists of setting the
   * hasBreakpoint boolean flag.
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   */
  public void resume(String workflowInstanceId, String processorId) throws UnknownWorkflowInstanceException;

  /** 
   * Cancel the currently running workflow, freeing
   * any resources used
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   */
  void cancelExecution(String workflowInstanceId) throws UnknownWorkflowInstanceException;

  /** 
   * Cancel a paused processor or currently running one that supports cancelling at run time. 
   * Frees any resources used
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   */
  void cancel(String workflowInstanceId, String processorId) throws UnknownWorkflowInstanceException;

  /**
   * This method is used to signal to the workflow enactment engine that
   * resources associated with the workflow instance identified by 
   * <code>workflowInstanceId</code> can be destroyed.
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   */
  void destroy(String workflowInstanceId) throws UnknownWorkflowInstanceException;

  /**
   * Get version information for the software
   */
  String getVersion();
}
