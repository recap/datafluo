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
//	Created Date :        2005/08/09
//	Created for Project : Simdat
//
/////////////////////////////////////////////////////////////////////////
//
//	Dependencies : none
//
/////////////////////////////////////////////////////////////////////////
//
//	Last commit info:	$Author: ferris $
//                    $Date: 2005/09/05 15:31:56 $
//                    $Revision: 1.3 $
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.main;

import java.io.*;
import java.util.*;
import java.security.*;
import java.security.cert.*;

import org.apache.log4j.*;

import org.apache.axis.MessageContext;
import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;

import uk.ac.soton.itinnovation.grid.utils.*;
import uk.ac.soton.itinnovation.grid.gridservit.context.ServiceContext;
import uk.ac.soton.itinnovation.grid.gridservit.EScienceService;
import uk.ac.soton.itinnovation.grid.gridservit.GridConstants;
import uk.ac.soton.itinnovation.grid.gridservit.context.infrastructure.security.authorisation.pbac.PBACAuthoriser;

import uk.ac.soton.itinnovation.freefluo.lang.*;
import uk.ac.soton.itinnovation.freefluo.util.*;
import uk.ac.soton.itinnovation.freefluo.util.xml.*;
import uk.ac.soton.itinnovation.freefluo.conf.*;
import uk.ac.soton.itinnovation.freefluo.data.*;

/**
 * Decorator for the <code>EngineService</code> that provides
 * message-level authentication and message integrity as well
 * as enforces authorisation constraints.
 */
public class SecureEngineService extends EScienceService {
  // operations as stored in Auth
  private static final String NEW_WORKFLOW_INSTANCE = "http://it-innovation.soton.ac.uk/grid/service/account/initresourceallocation";
  private static final String ACCESS_WORKFLOW_INSTANCE = "read-and-modify-workflow-instance";

  private static Logger logger = Logger.getLogger(SecureEngineService.class);
  private EngineService service;

  private String[] serviceProviderId;

  public SecureEngineService() {
    logger.debug("SecureEngineService.<init>");
    EngineConfiguration config = null;
    ConfigurationDescription configDescription = new ConfigurationDescription("taverna", 
        "uk.ac.soton.itinnovation.freefluo.exts.taverna.TavernaScuflModelParser", 
        "uk.ac.soton.itinnovation.freefluo.exts.taverna.TavernaDataHandler",
        "uk.ac.soton.itinnovation.freefluo.util.id.AuthIdGenerator"); 
    try {
      config = new EngineConfigurationImpl(configDescription, getClass().getClassLoader());
    }
    catch(Exception e) {
      logger.error("Serious error configuring secure engine service", e);
      throw new RuntimeException("Serious error configuring secure engine service");
    }
    service = new EngineService(config);

    try {
      // load the keyststore so that the service provider ID can be determine
      InputStream is = getClass().getClassLoader().getResourceAsStream("crypto.properties");
      Properties props = new Properties();
      props.load(is);
      is.close();

      String strStore = props.getProperty("org.apache.ws.security.crypto.merlin.file");
      File store = new File(strStore);
      is = new BufferedInputStream(new FileInputStream(store));
      KeyStore keystore = KeyStore.getInstance("JKS");
      keystore.load(is, null);
      is.close();

      String privateKeyAlias = props.getProperty("org.apache.ws.security.crypto.merlin.keystore.alias");
      X509Certificate cert = (X509Certificate) keystore.getCertificate(privateKeyAlias);

      Principal issuer = cert.getIssuerDN();
			DNParser issuerParser = new DNParser(issuer.toString());
      String strIssuer = issuerParser.getStandardStringDN();

			Principal subject = cert.getSubjectDN();
			DNParser subjectParser = new DNParser(subject.toString());
      String strSubject = subjectParser.getStandardStringDN();
      
      this.serviceProviderId = new String[] {strIssuer, strSubject};
    }
    catch(Exception e) {
      String msg = "Error loading keystore";
      logger.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public Collection getWorkflowDescriptions() {
    Collection descriptions = service.getWorkflowDescriptions();
    return descriptions;
  }

  public String newWorkflowInstance(String accountId, String workflowDefinitionId) throws UnknownWorkflowDefinitionException {
    setUserContext();

    // jf: 19/08/2005
    // Be tolerant of account id format.. Clients can use a full URL or just an id number
    accountId = accountId.substring(accountId.lastIndexOf("#") + 1);

    if(!isAuthorised(NEW_WORKFLOW_INSTANCE, accountId)) {
      throw new OperationNotAuthorisedException("New workflow instance creation not authorised for " + UserContext.getString());
    }
   
    String instanceId = service.newWorkflowInstance(accountId, workflowDefinitionId);
    
    // open authorisations for the client user to interact with the workflow instance.
    try {
      ServiceContext ctx = getContext();
      PBACAuthoriser auth = getPBAC(ctx);
      auth.OpenAuthorisation(instanceId, UserContext.getIssuer(), UserContext.getUser(), ACCESS_WORKFLOW_INSTANCE);
    }
    catch(Exception e) {
      String msg = "Error opening authorisations to workflow instance " + instanceId + ". " + UserContext.getString();
      logger.error(msg, e);
      throw new RuntimeException(msg);
    }

    // set the user context that the workflow will be run as.
    try {
      EngineImpl engine = service.getEngine();
      WorkflowInstance workflow = engine.getWorkflowInstance(instanceId);
      FlowContext flowContext = workflow.getFlowContext();
      flowContext.put(FlowContext.USER_PRINCIPAL, UserContext.getUser());
      flowContext.put(FlowContext.ISSUER_PRINCIPAL, UserContext.getIssuer());
    }
    catch(Exception e) {
      String msg = "Serious error setting user context for workflow " + instanceId;
      logger.error(msg, e);
      throw new RuntimeException(msg);
    }
    return instanceId;
  }

  public void setInput(String workflowInstanceId, Map input) throws UnknownWorkflowInstanceException, BadlyFormedDocumentException, InvalidInputException {
    setUserContext();
    checkAccessAuthorised(workflowInstanceId);
    service.setInput(workflowInstanceId, input);
  }

  public void run(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    setUserContext();
    checkAccessAuthorised(workflowInstanceId);
    service.run(workflowInstanceId);
  }

  public String getStatus(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    setUserContext();
    checkAccessAuthorised(workflowInstanceId);
    String status = service.getStatus(workflowInstanceId);
    return status;
  }

  public String getProgressReportXML(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    setUserContext();
    checkAccessAuthorised(workflowInstanceId);
    String progress = service.getProgressReportXML(workflowInstanceId);
    return progress;
  }

  public Map getOutput(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    setUserContext();
    checkAccessAuthorised(workflowInstanceId);
    Map output = service.getOutput(workflowInstanceId);
    return output;
  }

  public String getErrorMessage(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    setUserContext();
    checkAccessAuthorised(workflowInstanceId);
    String errorMsg = service.getErrorMessage(workflowInstanceId);
    return errorMsg;
  }

  public String getProvenanceXML(String workflowInstanceId) throws UnknownWorkflowInstanceException /*, UnknownProcessorException*/  {
    setUserContext();
    checkAccessAuthorised(workflowInstanceId);
    String provReport = service.getProvenanceXML(workflowInstanceId);
    return provReport;
  }

  public String getVersion() {
    String version = service.getVersion();
    return version;
  }

  public void cancelExecution(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    setUserContext();
    checkAccessAuthorised(workflowInstanceId);
    service.cancelExecution(workflowInstanceId);
  }

  public void destroy(String workflowInstanceId) throws UnknownWorkflowInstanceException {
    setUserContext();
    checkAccessAuthorised(workflowInstanceId);
    service.destroy(workflowInstanceId);
  }

  public String[] getServiceProviderId() {
    return serviceProviderId;
  }

  private ServiceContext getContext() {
    ServiceContext serviceContext = null;

    try {
      serviceContext = getServiceContext();
    }
    catch(Exception e) {
      String msg = "Fatal error getting GridServIT ServiceContext";
      logger.fatal(msg, e);
      throw new RuntimeException(msg);
    }

    return serviceContext;
  }

  private PBACAuthoriser getPBAC(ServiceContext serviceContext) {
    PBACAuthoriser auth = null;

    try {
      auth = getAuth(serviceContext);
    }
    catch(Exception e) {
      String msg = "Fatal error getting Auth from ServiceContext";
      logger.fatal(msg, e);
      throw new RuntimeException(msg);
    }
    return auth;
  }

  private void setUserContext() {
    try {
      ServiceContext ctx = getContext();
      PBACAuthoriser auth = getPBAC(ctx);
  
      Principal user = ctx.getEScienceContext().getSecurityContext().getAuthenticatedSubjectPrincipal();
      Principal issuer = ctx.getEScienceContext().getSecurityContext().getAuthenticatedIssuerPrincipal();

      String strUser = new DNParser(user.toString()).getStandardStringDN();
      String strIssuer = new DNParser(issuer.toString()).getStandardStringDN();
      UserContext.setUser(strUser);
      UserContext.setIssuer(strIssuer); 
    }
    catch(Exception e) {
      throw new RuntimeException("Error getting user context.");
    }
  }

  // TODO.  multithreading issues in pbac, gria services.
  // this needs further thought!!!
  private boolean isAuthorised(String operation, String conversation) {
    ServiceContext ctx = getContext();
    PBACAuthoriser auth = getPBAC(ctx);

    String strUser = UserContext.getUser();
    String strIssuer = UserContext.getIssuer();
    
    boolean isAuthorised = false;
    boolean isConversationLocked = false;

    String contextMsg = "Conversation: " + conversation + "\tuser: " + strUser + 
                        "\tissuer: " + strIssuer + "\toperation: " + operation;
    
    try {
      String result = auth.CheckAuthorisationAndHoldConversation(conversation, strIssuer, strUser, operation);
     
      if(result.equals("SUCCESS")) {
        // strange as it seems, the conversation is locked only if the user is authorised.
        isAuthorised = true;
        isConversationLocked = true;
      }
    }
    catch (Exception e) {
      logger.error("Error checking authorisation. " + contextMsg, e);
      throw new RuntimeException("Error checking authorisation. " + contextMsg);
    }
    finally {
      if(isConversationLocked) {
        try {
          auth.ReleaseConversation(conversation);
        }
        catch(Exception e) {
          String msg = "Error releasing conversation after checking authorisation. " +
                       contextMsg;
          logger.error(msg, e);
          throw new RuntimeException(msg);
        }
      }
    }

    return isAuthorised;
  }

  private void checkAccessAuthorised(String workflowInstanceId) {
    if(!isAuthorised(ACCESS_WORKFLOW_INSTANCE, workflowInstanceId)) {
      throw new OperationNotAuthorisedException("Access to workflow instance " + workflowInstanceId +
                                                " isn't authorised for " + UserContext.getString());
    }
  }
}
