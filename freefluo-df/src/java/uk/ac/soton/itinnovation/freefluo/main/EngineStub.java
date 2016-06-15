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
//      Created Date        :   2004/04/28
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/12/06 15:04:29 $
//                              $Revision: 1.4 $
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.main;

import java.io.*;
import java.net.*;
import java.util.*;
import java.rmi.*;
import java.lang.reflect.*;

import javax.xml.namespace.*;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

import org.jdom.*;
import org.jdom.output.*;

import org.apache.axis.session.*;
import org.apache.axis.client.*;
import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;

import uk.ac.soton.itinnovation.freefluo.conf.*;
import uk.ac.soton.itinnovation.freefluo.task.*;
import uk.ac.soton.itinnovation.freefluo.lang.*;
import uk.ac.soton.itinnovation.freefluo.event.*;
import uk.ac.soton.itinnovation.freefluo.util.xml.*;
import uk.ac.soton.itinnovation.freefluo.util.*;
import uk.ac.soton.itinnovation.freefluo.util.event.*;
import uk.ac.soton.itinnovation.freefluo.data.*;

/**
 * This class is provided as a convenience for Java developers
 * that wish to access the Freefluo enactment engine web service
 * with as little effort as possible.  It's a client-side stub
 * that's instantiated with the URL of a remote engine instance.
 */
public class EngineStub implements Engine, SecureEngine {
  public static final String OPERATION_NS = "http://itinnovation.soton.ac.uk/freefluo";
  public static final String SOAP_ACTION = "freefluo";
  private static Logger logger = Logger.getLogger(EngineStub.class);
  private URL serviceEndpoint = null;

  private EngineConfiguration engineConfiguration;
  private DataHandler dataHandler;

  private String username;
  private String password;

  private PollingSettings pollingSettings = new PollingSettings();

  static {
    //System.setProperty("axis.socketSecureFactory", "uk.ac.soton.itinnovation.freefluo.main.FreefluoSocketFactory"); 
  
    try {
      InputStream is = EngineStub.class.getClassLoader().getResourceAsStream("crypto.properties");
      Properties props = new Properties();
      props.load(is);
      is.close();

      String keystoreLocation = props.getProperty("org.apache.ws.security.crypto.merlin.file");
      String keystorePassword = props.getProperty("org.apache.ws.security.crypto.merlin.keystore.password");

      System.setProperty("javax.net.ssl.keyStore", keystoreLocation);
      System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
      System.setProperty("javax.net.ssl.trustStore", keystoreLocation);
      System.setProperty("javax.net.ssl.trustStorePassword", keystorePassword);

      logger.info("Loaded keystore details from crypto.properties");
    }
    catch(Exception e) {
      logger.info("Keystore couldn't be loaded. Assuming insecure use.", e);
    }
  }

  /** 
   * For SSL connections this indicates whether all servers should
   * be trusted (value of <code>true</code>) or alternatively whether
   * a trust store should be used to authenticate the server (value
   * of <code>false</code>
   */
  private boolean trustAllServers = true;

  /** 
   * Map of <code>workflowInstanceId:String</code> to <code>ArrayList</code> of 
   * <code>WorkflowStateListeners</code> for the workflow.
   */
  private Map listenersMap = new HashMap();

  /**
   * @param serviceLocation the URL for the Freefluo web service endpoint
   */
  public EngineStub(URL serviceEndpoint) {
    this.serviceEndpoint = serviceEndpoint;
    
    try {
      this.engineConfiguration = ConfigurationLocator.getDefaultConfiguration();
    }
    catch(NoSuchConfigurationException e) {
      String msg = "Fatal Error initialising EngineStub. No such configuration " + ConfigurationLocator.DEFAULT_CONFIGURATION;
      logger.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
    catch(Exception e) {
      String msg = "Fatal Error initialising EngineStub.";
      logger.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }

    this.dataHandler = engineConfiguration.getDataHandler();
  }

  /**
   * @param serviceLocation the URL for the Freefluo web service endpoint
   * @param the configuration to use.
   */
  public EngineStub(EngineConfiguration engineConfiguration, URL serviceEndpoint) {
    this.serviceEndpoint = serviceEndpoint;
    this.engineConfiguration = engineConfiguration;
    this.dataHandler = engineConfiguration.getDataHandler();
  }

  /**
   * Create an engine stub that authenticates itself with the remote
   * Freefluo web service using HTTP basic authentication. Note that 
   * The <code>username</code> and <code>password</code> provided
   * will be sent in all HTTP/SOAP requests made by this instance
   * of <code>EngineStub</code>.
   */
  public EngineStub(URL serviceEndpoint, String username, String password) {
    this(serviceEndpoint);
    setUsername(username);
    setPassword(password);
  }

  /**
   * Create an engine stub that authenticates itself with the remote
   * Freefluo web service using HTTP basic authentication. Note that 
   * The <code>username</code> and <code>password</code> provided
   * will be sent in all HTTP/SOAP requests made by this instance
   * of <code>EngineStub</code>.
   */
  public EngineStub(EngineConfiguration engineConfiguration, URL serviceEndpoint, String username, String password) {
    this(engineConfiguration, serviceEndpoint);
    setUsername(username);
    setPassword(password);
  }

  /**
   * Set the username used by this stub in HTTP basic authentication.
   */
  public void setUsername(String username) {
    if(username == null) {
      String msg = "Can't set username. Username was null";
      logger.error(msg);
      throw new RuntimeException(msg);
    }
    this.username = username.trim();
  }

  /**
   * Set the password used by this stub in HTTP basic authentication.
   */
  public void setPassword(String password) {
    if(password == null) {
      String msg = "Can't set password. Password was null";
      logger.error(msg);
      throw new RuntimeException(msg);
    }
    
    this.password = password.trim();
  }

  /**
   * Set whether, for SSL connections, all servers should be trusted 
   * (<code>true</code>) or alternatively whether a trust store should be used to 
   * authenticate servers certificates (<code>false</code>).
   */
  public void setTrustAllServers(boolean isTrustAllServers) {
    this.trustAllServers = isTrustAllServers;
  }

  public PollingSettings getPollingSettings() {
    return pollingSettings;
  }

  public void setPollingSettings(PollingSettings pollingSettings) {
    this.pollingSettings = pollingSettings;
  }

  public EngineConfiguration getEngineConfiguration() {
    return engineConfiguration;
  }

  public String[] getServiceProviderId() {
    try {
      Object output = null;
      Call call = createCall("getServiceProviderId");
      
      try {
        output = call.invoke(new Object[] {});
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }

      String[] id = (String[]) output;

      return id;
    }
    catch(Exception e) {
      String msg = "Serious error getting service provider id of remote enactor";

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }    
  }

  public Collection getWorkflowDescriptions() {
    Call call = createCall("getWorkflowDescriptions");
    Object output = null;
    Collection strings = new ArrayList();
    Collection objects = new ArrayList();

    try {
      Object[] arObj = (Object[]) call.invoke(new Object[]{});
      strings = Arrays.asList(arObj);
      
      for(Iterator i = strings.iterator(); i.hasNext();) {
        String s = (String) i.next();

        Document doc = XmlUtils.jdomDocumentFromString(s);
        Element el = doc.getRootElement();
        WorkflowDescription wd = new WorkflowDescription(el);
        objects.add(wd);
      }
    }
    catch(Exception e) {
      String msg = "Serious error getting workflow descriptions from : " + serviceEndpoint;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }

    return objects;
  }

  public String newWorkflowInstance(String accountId, String workflowDefinitionId) throws UnknownWorkflowDefinitionException {
    String instanceId = null;
   
    Call call = createCall("newWorkflowInstance");

    try {
      try {
        Object obj = call.invoke(new Object[]{accountId, workflowDefinitionId});
        instanceId = (String) obj;
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
    }
    catch(UnknownWorkflowDefinitionException e) {
      String msg = "No workflow definition with id " + workflowDefinitionId;
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error getting workflow definition with id " + workflowDefinitionId;
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }

    return instanceId;
  }

  public String compile(String workflowDefinition) throws BadlyFormedDocumentException, ParsingException {
    String flowId = null;
    Object output = null;
    
    Call call = createCall("compile");
    Object[] arguments = new Object[] {workflowDefinition};
    
    try {
      try {
        output = call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
      flowId = (String) output;
      return flowId;
    }
    catch(BadlyFormedDocumentException e) {
      String msg = "Compilation of workfow failed.  The workflow definition is badly formed.";

      logger.warn(msg, e);
      throw e;
    }
    catch(ParsingException e) {
      String msg = "Compilation of workflow failed.  The workflow definition is invalid.  " + e.getMessage();

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error compiling workflow.";

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public void setInput(String workflowInstanceId, Map inputData) throws UnknownWorkflowInstanceException, InvalidInputException {
    try {
      try {
        inputData = dataHandler.objectToXmlMap(inputData);
        Call call = createCall("setInput");
        Object[] arguments = new Object[] {workflowInstanceId, inputData};

        call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Setting input failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

      logger.warn(msg, e);
      throw e;
    }
    catch(InvalidInputException e) {
      String msg = "Setting input failed.  Invalid input for workflow " + workflowInstanceId + ".  " + e.getMessage();

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error setting input for workflow instance with id " + workflowInstanceId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public void setFlowContext(String workflowInstanceId, FlowContext flowContext) throws UnknownWorkflowInstanceException, InvalidFlowContextException {
    try {
      Call call = createCall("setFlowContext");

      if(flowContext == null) {
        throw new InvalidFlowContextException("Client error.  The client has sent a flow context of NULL");
      }
  
      Object[] arguments = new Object[] {workflowInstanceId, flowContext.toXmlString()};

      try {
        call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
    }
    catch(InvalidFlowContextException e) {
      String msg = "Setting flow context failed.  Invalid flow context data for workflow instance " + workflowInstanceId + ".";
      logger.warn(msg, e);
      throw e;
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Setting flow context failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error setting flow context.";
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public FlowContext getFlowContext(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    try {
      FlowContext flowContext = null;
      Call call = createCall("getFlowContext");

      Object[] arguments = new Object[] {workflowInstanceId};

      try {
        String strFlowContext = (String) call.invoke(arguments);
        flowContext = new FlowContext(strFlowContext);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }

      return flowContext;
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Getting flow context failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();
      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error getting flow context.";
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }
  
  public WorkflowInstance getWorkflowInstance(String workflowInstanceId) throws UnknownWorkflowInstanceException{
	   throw new UnsupportedOperationException("Not supported yet.");
  }

  public void run(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    try {
      Call call = createCall("run");
      Object[] arguments = new Object[] {workflowInstanceId};

      try {
        call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Running workflow failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error running workflow.";

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }

    List listeners = null;
    List listenersCopied = new ArrayList();

    synchronized(listenersMap) {
      listeners = (List) listenersMap.get(workflowInstanceId);
    }

    if(listeners != null) {
      synchronized(listeners) {
        listenersCopied.addAll(listeners);
      }
    }
    
    if(listenersCopied.size() == 0) {
      return;
    }

    StatusPollingThread pollingThread = new StatusPollingThread(this, workflowInstanceId, 
              pollingSettings, listenersCopied);
    pollingThread.start();
  }
  
  public String getStatus(String workflowInstanceId) throws UnknownWorkflowInstanceException { 
    try {
      Object output = null;
      Call call = createCall("getStatus");
      Object[] arguments = new Object[] {workflowInstanceId};
      
      try {
        output = call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }

      String status = (String) output;

      return status;
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Running workflow failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error getting status for workflow instance with id " + workflowInstanceId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  /**
   * A a listener of the state of a workflow. Note that under the hood
   * polling is used to get status of workflow instance in the
   * remote web service enactor. Listeners should be added/removed before 
   * the workflow instance is started running. The behaviour is undefined
   * for listeners added or removed after execution of the workfow instance
   * has started.
   */
  public void addWorkflowStateListener(String workflowInstanceId, WorkflowStateListener workflowStateListener) throws UnknownWorkflowInstanceException {
    synchronized(listenersMap) {
      ArrayList listeners = null;
      listeners = (ArrayList) listenersMap.get(workflowInstanceId);
      if(listeners == null) {
        listeners = new ArrayList();
        listenersMap.put(workflowInstanceId, listeners);
      }
      
      synchronized(listeners) {
        if(!listeners.contains(workflowStateListener)) {
          listeners.add(workflowStateListener);
        }
      }
    }
  }

  /**
   * Note that removing a <code>WorkflowStateListener</code> has no effect if workflow execution
   * has started.
   */
  public void removeWorkflowStateListener(String workflowInstanceId, WorkflowStateListener workflowStateListener) throws UnknownWorkflowInstanceException {
    synchronized(listenersMap) {
      ArrayList listeners = null;  
      listeners = (ArrayList) listenersMap.get(workflowInstanceId);
      if(listeners == null) {
        logger.warn("EngineStub.removeWorkflowStateListener(...) called for unknown workflow instance with id " + workflowInstanceId);
        return;
      }

      synchronized(listeners) {
        listeners.remove(workflowStateListener);
        if(listeners.size() == 0) {
          listenersMap.remove(workflowInstanceId);
        }
      }
    }
  }

  public String getProgressReportXML(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    try {
      Object output = null;
      Call call = createCall("getProgressReportXML");
      Object[] arguments = new Object[] {workflowInstanceId};
      
      try {
        output = call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
      String progressReport = (String) output;

      return progressReport;
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Getting progress report XML failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

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
    try {
      Object output = null;
      Call call = createCall("getIntermediateResultsForProcessor");
      Object[] arguments = new Object[] {workflowInstanceId, processorName};
      
      try {
        output = call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
      Map[] inputsAndOutputs = (Map[]) output;
      inputsAndOutputs[0] = dataHandler.xmlToObjectMap(inputsAndOutputs[0]);
      inputsAndOutputs[1] = dataHandler.xmlToObjectMap(inputsAndOutputs[1]);
      return inputsAndOutputs;
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Getting intermediate results for processor failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

      logger.warn(msg, e);
      throw e;
    }
    catch(UnknownProcessorException e) {
      String msg = "Getting intermediate results for processor failed.  " + 
        "Unknown processor with name " + processorName + " in workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

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
  
  public Map getOutput(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    try {
      Object output = null;
      Call call = createCall("getOutput");
      Object[] arguments = new Object[] {workflowInstanceId};
      
      try {
        output = call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }

      Map outputMap = (Map) output;
      outputMap = dataHandler.xmlToObjectMap(outputMap);
      return outputMap;
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Getting output failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

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
    
    try {
      Object output = null;
      Call call = createCall("getErrorMessage");
      Object[] arguments = new Object[] {workflowInstanceId};
      
      try {
        output = call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }

      String errorMessage = (String) output;

      return errorMessage;
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Getting error message failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

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
    try {
      Object output = null;
      Call call = createCall("getProvenanceXML");
      Object[] arguments = new Object[] {workflowInstanceId};
      
      try {
        output = call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
      String provXML = (String) output;

      return provXML;
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Getting provenance XML failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error getting provenance xml for workflow instance with id " + workflowInstanceId;

      logger.error(msg);
      throw new RuntimeException(msg, e);
    }
  }
  
  public void pauseExecution(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    try {
      Call call = createCall("pause");
      Object[] arguments = new Object[] {workflowInstanceId};
      
      try {
        call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Pausing workflow failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error pausing workflow instance with id " + workflowInstanceId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  /* Added by matskan to support pausing of tasks.*/
  public void pause(String workflowInstanceId, String processorId) throws UnknownWorkflowInstanceException {
    try {
      Call call = createCall("pause");
      Object[] arguments = new Object[] {workflowInstanceId, processorId};
      
      try {
        call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Pausing of processor in the workflow failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error pausing processor" + processorId + " in the workflow instance with id " + workflowInstanceId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public boolean isDataNonVolatile(String workflowInstanceId, String processorId) throws UnknownWorkflowInstanceException {
    try {
      Call call = createCall("isDataNonVolatile");
      Object[] arguments = new Object[] {workflowInstanceId, processorId};
      
      try {
        return((Boolean) call.invoke(arguments)).booleanValue();
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Checking for non volatile data in the workflow failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error on checking processor " + processorId + " of the workflow instance with id " + workflowInstanceId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
		return false;
  }

  public boolean changeOutputPortTaskData(String workflowInstanceId, String processorName, String OutputPortName, Object newData) throws UnknownWorkflowInstanceException {
    try {
      Call call = createCall("changeOutputPortTaskData");
      Object[] arguments = new Object[] {workflowInstanceId, processorName, OutputPortName, newData};
      
      try {
        return ((Boolean)call.invoke(arguments)).booleanValue();
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Changing intermediate data in the workflow failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error on changing intermediate data on processor "+processorName+" of the workflow instance with id " + workflowInstanceId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
		return false;
  }

  
  public void resumeExecution(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    try {
      Call call = createCall("resume");
      Object[] arguments = new Object[] {workflowInstanceId};
      
      try {
        call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Resuming workflow failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error resuming workflow instance with id " + workflowInstanceId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  /* Added by matskan to support resuming of tasks from paused state.*/
  public void resume(String workflowInstanceId, String processorId) throws UnknownWorkflowInstanceException {
    try {
      Call call = createCall("resume");
      Object[] arguments = new Object[] {workflowInstanceId, processorId};
      
      try {
        call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Resuming of processor workflow failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error resuming  " + processorId + " workflow instance with id " + workflowInstanceId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  /* Added by matskan to support cancelling of paused tasks.*/
  public void cancel(String workflowInstanceId, String processorId) throws UnknownWorkflowInstanceException {
    try {
      Call call = createCall("cancel");
      Object[] arguments = new Object[] {workflowInstanceId, processorId};
      
      try {
        call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Cancelling processor failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error cancelling processor of workflow instance with id " + workflowInstanceId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public boolean isPaused(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    try {
      Call call = createCall("isPaused");
      Object[] arguments = new Object[] {workflowInstanceId};
      
      try {
        return((Boolean) call.invoke(arguments)).booleanValue();
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Check if workflow is paused failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error on checking workflow instance with id " + workflowInstanceId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
    return true; //EXTEMELY TEMP TO CORRECT THIS

  }

  public void cancelExecution(String workflowInstanceId) throws UnknownWorkflowInstanceException {  
    try {
      Call call = createCall("cancelExecution");
      Object[] arguments = new Object[] {workflowInstanceId};
      
      try {
        call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Cancelling workflow failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

      logger.warn(msg, e);
      throw e;
    }
    catch(Exception e) {
      String msg = "Serious error cancelling workflow instance with id " + workflowInstanceId;

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public void destroy(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    try {
      Call call = createCall("destroy");
      Object[] arguments = new Object[] {workflowInstanceId};
      
      try {
        call.invoke(arguments);
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }
    }
    catch(UnknownWorkflowInstanceException e) {
      String msg = "Destroying workflow failed.  Unknown workflow instance with id " + workflowInstanceId + ".  " + e.getMessage();

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
    try {
      Object output = null;
      Call call = createCall("getVersion");
      
      try {
        output = call.invoke(new Object[] {}
            );
      }
      catch(AxisFault fault) {
        translateFaultAndThrowException(fault);
      }

      String version = (String) output;

      return version;
    }
    catch(Exception e) {
      String msg = "Serious error getting version of remote enactor";

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  private Call createCall(String operationName) {
    Call call = null; 
    
    try {
      Service  service = new Service();
      call = (Call) service.createCall();

      if(username != null && !username.equals("") &&
        password != null && !password.equals("")) {
        call.setUsername(username);
        call.setPassword(password);
      }

      if(trustAllServers) {
        Session session = new SimpleSession();
        session.set(FreefluoSocketFactory.FACTORY_KEY, FreefluoSocketFactory.TRUST_ALL_FACTORY);
        MessageContext ctx = call.getMessageContext();
        ctx.setSession(session);
      }
    }
    catch(ServiceException e) {
      String msg = "Serious error compiling workflow.  Error encountered creating the Call object.";

      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }
    
    call.setTargetEndpointAddress(serviceEndpoint);
    call.setOperationName(new QName(OPERATION_NS, operationName));
    call.setSOAPActionURI(SOAP_ACTION);
    return call;
  }

  /**
   * Todo, see if Axis' built in mechanisms for de/serialising exceptions
   * is better than this.
   */
  private void translateFaultAndThrowException(AxisFault fault) throws BadlyFormedDocumentException, ParsingException, UnknownWorkflowInstanceException, InvalidInputException, InvalidFlowContextException, UnknownWorkflowDefinitionException {
    String faultMessage = fault.getFaultString();
    int index = faultMessage.indexOf(":");
    Class clazz = null;
    
    String className = null;
    String message = null;

    if(index != -1) {
      className = faultMessage.substring(0, index);
      if(faultMessage.length() - 1 != index) {
        message = faultMessage.substring(index + 1);
      }
      else {
        message = "";
      }
    }
    else {
      className = faultMessage;
      message = "";
    }

    className = className.trim();
    message = message.trim();

    logger.debug("SOAP fault encountered.  message: " + message + ".  exception class: " + className);
    
    try {
      clazz = getClass().getClassLoader().loadClass(className);
      Constructor constructor = clazz.getConstructor(new Class[] {String.class});
      Object exception = constructor.newInstance(new Object[] {message});
    
      if(exception instanceof BadlyFormedDocumentException) {
        throw(BadlyFormedDocumentException) exception;
      }
      else if(exception instanceof ParsingException) {
        throw(ParsingException) exception;
      }
      else if(exception instanceof UnknownWorkflowInstanceException) {
        throw(UnknownWorkflowInstanceException) exception;
      }
      else if(exception instanceof InvalidInputException) {
        throw(InvalidInputException) exception;
      }
      else if(exception instanceof InvalidFlowContextException) {
        throw(InvalidFlowContextException) exception;
      }
      else if(exception instanceof UnknownWorkflowDefinitionException) {
        throw(UnknownWorkflowDefinitionException) exception;
      }
      else {
        throw new RuntimeException(message);
      }
    }
    catch(ClassNotFoundException e) {
      logger.error("", e);
      throw new RuntimeException(faultMessage);
    }
    catch(SecurityException e) {
      logger.error("", e);
      throw new RuntimeException(faultMessage);
    }
    catch(NoSuchMethodException e) {
      logger.error("", e);
      throw new RuntimeException(faultMessage);
    }
    catch(InstantiationException e) {
      logger.error("", e);
      throw new RuntimeException(faultMessage);
    }
    catch(IllegalAccessException e) {
      logger.error("", e);
      throw new RuntimeException(faultMessage);
    }
    catch(InvocationTargetException e) {
      logger.error("", e);
      throw new RuntimeException(faultMessage);
    }
  }
}