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
//      Created Date        :   2004/03/31
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/08/23 06:24:48 $
//                              $Revision: 1.1 $
//
////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.main;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.jdom.*;
import org.jdom.input.*;

import org.apache.axis.MessageContext;

import uk.ac.soton.itinnovation.freefluo.lang.*;
import uk.ac.soton.itinnovation.freefluo.util.*;
import uk.ac.soton.itinnovation.freefluo.util.xml.*;
import uk.ac.soton.itinnovation.freefluo.conf.*;
import uk.ac.soton.itinnovation.freefluo.data.*;

/**
 * The freefluo webservice facade.  This class wraps and
 * delegates to the standard EngineImpl.  It's necessary
 * to do this to present a simple interface to clients
 * and to avoid writing complex deserializers.  For example,
 * an axis Deserializer for DataThing that's implemented 
 * with Axis' SAX model would need to be written to be
 * able to use EngineImpl as the web service class.  It
 * doesn't seem worth the effort to do this as it's very
 * simple to pass a map of data things as an XML document
 * carried by an xsd:string.
 */
public class EngineService {
  public static Logger logger = Logger.getLogger(EngineService.class);
  private EngineImpl engine;
  private DataHandler dataHandler;

  public EngineService() {
    EngineConfiguration config = null;
    ConfigurationDescription configDescription = new ConfigurationDescription("taverna", 
        "uk.ac.soton.itinnovation.freefluo.exts.taverna.TavernaScuflModelParser", 
        "uk.ac.soton.itinnovation.freefluo.exts.taverna.TavernaDataHandler"); 
    try {
      config = new EngineConfigurationImpl(configDescription, getClass().getClassLoader());
    }
    catch(Exception e) {
      logger.error("Serious error configuring engine", e);
      throw new RuntimeException("Serious error configuring engine", e);
    }
    
    init(config);
  }

  public EngineService(EngineConfiguration config) {
    init(config);
  }

  private void init(EngineConfiguration config) {
    engine = new EngineImpl(config);
    dataHandler = engine.getEngineConfiguration().getDataHandler();
  }

  // not a web service method. Currently used by SecureEngineService
  // to register the authenticated user with the workflow.
  EngineImpl getEngine() {
    return engine;
  }

  public Collection getWorkflowDescriptions() {
    Collection objects = engine.getWorkflowDescriptions();
    Collection strings = new ArrayList();
    for(Iterator i = objects.iterator(); i.hasNext();) {
      WorkflowDescription wd = (WorkflowDescription) i.next();
      strings.add(wd.toElementString());
    }
    return strings;
  }

  public String newWorkflowInstance(String accountId, String workflowDefinitionId) throws UnknownWorkflowDefinitionException {
    return engine.newWorkflowInstance(accountId, workflowDefinitionId);
  }

  public String compile(String workflowDefinition) throws BadlyFormedDocumentException, ParsingException {
    String flowId = engine.compile(workflowDefinition);

    // place user's authentication details in the FlowContext for the workflow
    // instance, if the user's been authenticated.
    MessageContext messageContext = MessageContext.getCurrentContext();
    String username = messageContext.getUsername();
    String password = messageContext.getPassword();

    logger.info("Compiled workflow with newly created id " + flowId + " for user " + username);
    if(username != null && password != null) {
      FlowContext flowContext = null;
      
      try {
        flowContext = engine.getFlowContext(flowId);
      }
      catch(Exception e) {
        String msg = "Fatal error. Can't find recently compiled worklow instance";
        logger.fatal(msg, e);
        throw new RuntimeException(msg, e);
      }
      
      flowContext.put(FlowContext.USERNAME, username);
      flowContext.put(FlowContext.PASSWORD, password);
    }
    return flowId;
  }

  public void setInput(String workflowInstanceId, Map input) throws UnknownWorkflowInstanceException, BadlyFormedDocumentException, InvalidInputException {
    try {
      input = dataHandler.xmlToObjectMap(input);
    }
    catch(BadlyFormedDocumentException e) {
      String msg = "Error setting input for workflow instance " + workflowInstanceId +
        "The input is badly formed XML.";
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error setting input for workflow instance " + workflowInstanceId;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
    engine.setInput(workflowInstanceId, input);
  }

  public void setFlowContext(String workflowInstanceId, String strFlowContext)  throws UnknownWorkflowInstanceException, InvalidFlowContextException {
    FlowContext flowContext = null;
    
    try {
      flowContext = new FlowContext(strFlowContext);;
    }
    catch(Exception e) {
      String msg = "Error setting flow context for workflow instance " + workflowInstanceId;
      logger.warn(msg, e);
      throw new InvalidFlowContextException(msg, e);
    }

    engine.setFlowContext(workflowInstanceId, flowContext);
  }

  public String getFlowContext(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    FlowContext flowContext = null;

    try {
      flowContext = engine.getFlowContext(workflowInstanceId);
      String strFlowContext = flowContext.toXmlString();
      return strFlowContext;
    }
    catch(Exception e) {
      String msg = "Serious error getting flow context for workflow instance " + workflowInstanceId;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public void run(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    engine.run(workflowInstanceId);
  }

  public String getStatus(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    return engine.getStatus(workflowInstanceId);
  }

  public String getProgressReportXML(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    try {
      return engine.getProgressReportXML(workflowInstanceId);
    }
    catch(Throwable e) {
      String msg = "Serious error getting progress report xml.";
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public Map[] getIntermediateResultsForProcessor(String workflowInstanceId, String processorName) throws UnknownWorkflowInstanceException, UnknownProcessorException {
    try {
      Map[] inputsAndOutputs = engine.getIntermediateResultsForProcessor(workflowInstanceId, processorName);
      inputsAndOutputs[0] = dataHandler.objectToXmlMap(inputsAndOutputs[0]);
      inputsAndOutputs[1] = dataHandler.objectToXmlMap(inputsAndOutputs[1]);
      return inputsAndOutputs;
    }
    catch(Exception e) {
      String msg = "Serious error getting intermediate results for processor " + processorName + " in " +
        " workflow instance with id " + workflowInstanceId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public Map getOutput(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    String outputString = null;
    Map output = engine.getOutput(workflowInstanceId);
    output = dataHandler.objectToXmlMap(output);
    return output;
  }

  public String getErrorMessage(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    return engine.getErrorMessage(workflowInstanceId);
  }

  public String getProvenanceXML(String workflowInstanceId) throws UnknownWorkflowInstanceException /*, UnknownProcessorException*/  {
    return engine.getProvenanceXML(workflowInstanceId);
  }

  public String getVersion() {
    return engine.getVersion();
  }

  public boolean isDataNonVolatile(String workflowInstanceId, String processorId) throws UnknownWorkflowInstanceException {
    return engine.isDataNonVolatile(workflowInstanceId, processorId);
  }

	 
  public boolean changeOutputPortTaskData(String workflowInstanceId, String processorId, String OutputPortName, Object newData) throws UnknownWorkflowInstanceException {
    return engine.changeOutputPortTaskData(workflowInstanceId, processorId,OutputPortName,newData);
  }

  public boolean isPaused(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    return engine.isPaused(workflowInstanceId);
  }

  public void resume(String workflowInstanceId, String processorId) throws UnknownWorkflowInstanceException {
    engine.resume(workflowInstanceId, processorId);
  }

  public void resumeExecution(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    engine.resumeExecution(workflowInstanceId);
  }

  public void pause(String workflowInstanceId, String processorId) throws UnknownWorkflowInstanceException {
    engine.pause(workflowInstanceId, processorId);
  }

  public void pauseExecution(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    engine.pauseExecution(workflowInstanceId);
  }

  public void cancel(String workflowInstanceId, String processorId) throws UnknownWorkflowInstanceException {
    engine.cancel(workflowInstanceId, processorId);
  }

  public void cancelExecution(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    engine.cancelExecution(workflowInstanceId);
  }

  public void destroy(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    engine.destroy(workflowInstanceId);
  }
}
