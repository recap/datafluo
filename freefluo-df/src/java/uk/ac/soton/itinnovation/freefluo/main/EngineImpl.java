////////////////////////////////////////////////////////////////////////////////
//
//  University of Southampton IT Innovation Centre, 2002
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

import org.apache.log4j.Logger;

import uk.ac.soton.itinnovation.freefluo.lang.*;
import uk.ac.soton.itinnovation.freefluo.lang.scufl.*;
import uk.ac.soton.itinnovation.freefluo.event.*;
import uk.ac.soton.itinnovation.freefluo.conf.*;
import uk.ac.soton.itinnovation.freefluo.util.xml.*;
import uk.ac.soton.itinnovation.freefluo.util.*;
import uk.ac.soton.itinnovation.freefluo.core.task.*;
import uk.ac.soton.itinnovation.freefluo.core.flow.*;

public class EngineImpl implements Engine {
  private static Logger logger = Logger.getLogger(EngineImpl.class);
  private static String PROXY_SETTINGS_FILE = "proxy-settings";
  
  static {
    try {
      ResourceBundle rb = ResourceBundle.getBundle(PROXY_SETTINGS_FILE);
      logger.info("Initialising proxy settings from config file at " + rb.getLocale().toString());
      Properties sysProps = System.getProperties();
      Enumeration keys = rb.getKeys();
      while(keys.hasMoreElements()) {
        String key = (String) keys.nextElement();
        String value = (String) rb.getString(key);
        sysProps.put(key, value);
      }	
    }
    catch(MissingResourceException e) {
      // if the config file is absent, we dont set any proxy configuration
      logger.info("Proxy configuration file absent.");
    }
  }

  private EngineConfiguration engineConfiguration;

  protected HashMap /* workflowInstanceId:String -> workflowInstance:WorkflowInstance */ workflowInstanceMap = new HashMap();

  public EngineImpl(EngineConfiguration engineConfiguration) {
    logger.debug("EngineImpl.<init>");
    this.engineConfiguration = engineConfiguration;
  }
  
  public EngineImpl() {
    logger.debug("EngineImpl.<init>");
    try {
      this.engineConfiguration = ConfigurationLocator.getDefaultConfiguration();
    }
    catch(NoSuchConfigurationException e) {
      String msg = "Fatal Error initialising EngineImpl. No such configuration " + ConfigurationLocator.DEFAULT_CONFIGURATION;
      logger.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
    catch(Exception e) {
      String msg = "Fatal Error initialising EngineImpl.";
      logger.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public EngineConfiguration getEngineConfiguration() {
    return engineConfiguration;
  }

  public Collection getWorkflowDescriptions() {
    WorkflowDeployer deployer = WorkflowDeployer.getInstance();
    Collection descriptions = deployer.getDeployedWorkflows();
    return descriptions;
  }

  public String newWorkflowInstance(String accountId, String workflowDefinitionId) throws UnknownWorkflowDefinitionException {
    String instanceId = null;

    WorkflowDeployer deployer = WorkflowDeployer.getInstance();
    String workflowDef = deployer.getWorkflowDefinition(workflowDefinitionId);

    try {
      instanceId = compile(workflowDef);
    }
    catch(Exception e) {
      String msg = "Serious error compiling workflow with id: " + workflowDefinitionId +
                   ". The workflow had been previoulsy deployed so should have compiled without error.";
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }

    return instanceId;
  }
  
  public String compile(String workflowDefinition) throws BadlyFormedDocumentException, ParsingException {
    try {
      WorkflowParser parser = engineConfiguration.getWorkflowParser(); 
      WorkflowInstance workflowInstance = parser.parse(this, workflowDefinition);
      String flowId = workflowInstance.getFlow().getFlowId();
      workflowInstanceMap.put(flowId, workflowInstance);

      logger.info("Compilation of workflow completed successfully. Compiled workflow has been assigned an id of " + flowId);
      return flowId;
    }
    catch(BadlyFormedDocumentException e) {
      String msg = "Compilation of workfow failed.  The workflow definition is badly formed.";
      logger.warn(msg, e);
      throw e;
    }
    catch(ParsingException e) {
      String msg = "Compilation of workflow failed.  The workflow definition is invalid.";
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error compiling workflow.";
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public void setFlowContext(String workflowInstanceId, FlowContext flowContext) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;

    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      workflowInstance.setFlowContext(flowContext);
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Setting flow context failed.  Unknown workflow instance with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error setting flow context for workflow instance with id " + workflowInstanceId;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public FlowContext getFlowContext(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;

    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      return workflowInstance.getFlowContext();
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Getting flow context failed.  Unknown workflow instance with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error getting flow context for workflow instance with id " + workflowInstanceId;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

 

  public void run(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    try {
      final WorkflowInstance workflowInstance = getWorkflowInstance(workflowInstanceId);
      Thread thread = new Thread() {
          public void run() {
            workflowInstance.run();
          }
        };

      thread.start();
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Running workflow failed.  There isn't a workflow instance with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error running workflow instance with id " + workflowInstanceId;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public String getStatus(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;
    
    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      return workflowInstance.getStatus();
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Getting workflow status failed.  There isn't a workflow instance with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error getting status for workflow instance with id " + workflowInstanceId;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public void addWorkflowStateListener(String workflowInstanceId, WorkflowStateListener workflowStateListener) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;
    
    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      workflowInstance.addWorkflowStateListener(workflowStateListener);
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Adding workflow state listener failed.  There isn't a workflow instance with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error adding workflow state listener to workflow instance with id " + workflowInstanceId;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public void removeWorkflowStateListener(String workflowInstanceId, WorkflowStateListener workflowStateListener) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;
    
    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      workflowInstance.removeWorkflowStateListener(workflowStateListener);
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Removing workflow state listener failed.  There isn't a workflow instance with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error removing workflow state listener to workflow instance with id " + workflowInstanceId;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public String getProgressReportXML(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;

    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      return workflowInstance.getProgressReportXML();
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Getting progress report xml failed.  There isn't a workflow instance with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error getting progress report xml for workflow instance with id " + workflowInstanceId;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public Map[] getIntermediateResultsForProcessor(String workflowInstanceId, String processorName) throws UnknownWorkflowInstanceException, UnknownProcessorException {
    WorkflowInstance workflowInstance = null;
    Map[] inputAndOutputResults = null;

    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      inputAndOutputResults = workflowInstance.getIntermediateResultsForProcessor(processorName);
      return inputAndOutputResults;
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Getting intermediate results for processor failed.  Unknown workflow instance with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }
    catch(UnknownProcessorException e) {
      String msg = "Getting intermediate results for processor failed.  " + 
        "Unknown processor with name " + processorName + " in workflow instance with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error getting intermediate results for processor.  Workflow instance id: " + workflowInstanceId +
        ".  processorName: " + processorName;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

	public boolean changeOutputPortTaskData(String workflowInstanceId, String processorName,String OutputPortName, Object newData) throws UnknownWorkflowInstanceException/*, UnknownProcessorException*/ {
    WorkflowInstance workflowInstance = null;

    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      return workflowInstance.changeOutputPortTaskData(processorName, OutputPortName, newData);
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Changing intermediate results for processor failed.  Unknown workflow instance with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }
    /*catch(UnknownProcessorException e) {
      String msg = "Changing intermediate results for processor failed.  " + 
        "Unknown processor with name " + processorName + " in workflow instance with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }*/
    catch(Exception e) {
      String msg = "Serious error changing intermediate results for processor.  Workflow instance id: " + workflowInstanceId +
        ".  processorName: " + processorName;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  
  public Map getOutput(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;
    
    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      return workflowInstance.getOutput();
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Getting output failed.  Unknown workflow instance with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error getting output for workflow instance with id " + workflowInstanceId;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public String getErrorMessage(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;
    
    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      return workflowInstance.getErrorMessage();
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Getting error message failed.  Unknown workflow instance with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error getting error message for workflow instance with id " + workflowInstanceId;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }
  
  public String getProvenanceXML(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;
    
    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      return workflowInstance.getProvenanceXML();
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Getting provenance failed.  There's isn't a workflow instance with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error getting provenance xml for workflow instance with id " + workflowInstanceId;
      logger.error(msg);
      throw new RuntimeException(msg, e);
    }
  }
  
  /* matskan: Added to support pausing of flow.*/
  public void pauseExecution(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;
    
    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      workflowInstance.pause();
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Pausing workflow instance failed.  Unknown workflow instance with id " + workflowInstanceId;

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error pausing workflow instance with id " + workflowInstanceId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  /* matskan: Added to support breakpoints on tasks.*/
  public void pause(String workflowInstanceId, String processorId) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;
    
    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      Task task = workflowInstance.getFlow().getTask(processorId);
      task.addBreakpoint();
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Placing Breakpoint on a processor failed.  Unknown workflow instance with id " + workflowInstanceId;

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error on placing breakpoint on " + workflowInstanceId + " " + processorId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  /* matskan: Added to check if data of tasks are non volatile.*/
  public boolean isDataNonVolatile(String workflowInstanceId, String processorId) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;
    
    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      Task task = workflowInstance.getFlow().getTask(processorId);
      return task.isDataNonVolatile();
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Checking for non volatile data on a processor failed.  Unknown workflow instance with id " + workflowInstanceId;

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error on checking for non volatile data on " + workflowInstanceId + " " + processorId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  /* matskan: Added to support resuming  of a paused flow.*/
  public void resumeExecution(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;
    
    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      workflowInstance.resume();
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Resuming workflow instance failed.  Unknown workflow instance with id " + workflowInstanceId;

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error resuming workflow instance with id " + workflowInstanceId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  /* matskan: Added to support resuming of tasks from paused state.*/
  public void resume(String workflowInstanceId, String processorId) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;
    
    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      Flow flow = workflowInstance.getFlow();
      Task task = flow.getTask(processorId);
      task.removeBreakpoint();
      flow.resumeTask(processorId);
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Removing Breakpoint on a processor failed.  Unknown workflow instance with id " + workflowInstanceId;

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error on resuming from breakpoint on " + workflowInstanceId + " " + processorId + e.getMessage();

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  /*matskan: Added to support cancelling of paused tasks.*/
  public void cancel(String workflowInstanceId, String processorId) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;
    
    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      Task task = workflowInstance.getFlow().getTask(processorId);
      task.cancel();
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Cancelling a processor failed.  Unknown workflow instance with id " + workflowInstanceId;

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error on cancelling a processor on " + workflowInstanceId + " " + processorId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  /* matskan: Added to support quering of flow's state.*/
  public boolean isPaused(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;
    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      return workflowInstance.getFlow().getState() == FlowState.PAUSED;
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Quering flow state failed.  Unknown workflow instance with id " + workflowInstanceId;

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error on quering flow state " + workflowInstanceId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public void cancelExecution(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;
    
    try {
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      workflowInstance.cancel();
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Cancelling workflow instance failed.  Unknown workflow instance with id " + workflowInstanceId;

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error cancelling workflow instance with id " + workflowInstanceId;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }
  
  public void setInput(String workflowInstanceId, Map inputMap) throws UnknownWorkflowInstanceException, InvalidInputException {
    WorkflowInstance workflowInstance = null;
    
    try {
      logger.debug("workflowInstanceMap.size(): " + workflowInstanceMap.size());
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      workflowInstance.setInput(inputMap);
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Setting input failed.  Unknown workflow instance with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }
    catch(InvalidInputException e) {
      String msg = "Setting input failed.  The input was invalid for workflow with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error setting input for workflow instance with id " + workflowInstanceId;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public void destroy(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = null;

    try {
      logger.info("Destroying workflow instance with id " + workflowInstanceId);
      workflowInstance = getWorkflowInstance(workflowInstanceId);
      workflowInstance.destroy();
      workflowInstanceMap.remove(workflowInstanceId);
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Destroying workflow instance failed.  Unknown workflow instance with id " + workflowInstanceId;
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error destroying workflow instance with id " + workflowInstanceId;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public String getVersion() {
    logger.debug("Request to getVersion. Version is " + Version.getVersion());
    return Version.getVersion();
  }

  public WorkflowInstance getWorkflowInstance(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    WorkflowInstance workflowInstance = (WorkflowInstance) workflowInstanceMap.get(workflowInstanceId);

    if(workflowInstance == null) {
      String msg = "Can't find workflow instance with id " + workflowInstanceId;
      logger.warn(msg);
      throw new UnknownWorkflowInstanceException(msg);
    }

    return workflowInstance;
  }
}
