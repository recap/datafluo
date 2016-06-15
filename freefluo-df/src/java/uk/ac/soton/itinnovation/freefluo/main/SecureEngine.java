/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2005
//
// Copyright in this software belongs to the IT Innovation Centre of
// 2 Venture Road, Chilworth Science Park, Southampton SO16 7NP, UK.
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//	Created By :          Justin Ferris
//	Created Date :        2005/08/08
//	Created for Project : Simdat
//
/////////////////////////////////////////////////////////////////////////
//
//	Dependencies : none
//
/////////////////////////////////////////////////////////////////////////
//
//	Last commit info:	$Author: ferris $
//                    $Date: 2005/08/24 09:47:05 $
//                    $Revision: 1.2 $
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.main;

import java.util.*;

import uk.ac.soton.itinnovation.freefluo.lang.*;
import uk.ac.soton.itinnovation.freefluo.core.flow.*;
import uk.ac.soton.itinnovation.freefluo.event.*;
import uk.ac.soton.itinnovation.freefluo.conf.*;

public interface SecureEngine {

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
   * Cancel the currently running workflow, freeing
   * any resources used
   * @throws UnknownWorkflowInstanceException if there isn't a workflow instance with id <code>workflowInstanceId</code>
   */
  void cancelExecution(String workflowInstanceId) throws UnknownWorkflowInstanceException;

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

  String[] getServiceProviderId();
}
