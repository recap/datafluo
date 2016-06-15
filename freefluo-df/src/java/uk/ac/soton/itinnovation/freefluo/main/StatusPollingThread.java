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
//      Created Date        :   2005/12/06
//      Created for Project :   SIMDAT
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/12/07 14:49:19 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.main;

import java.util.*;

import org.apache.log4j.*;

import uk.ac.soton.itinnovation.freefluo.event.*;
import uk.ac.soton.itinnovation.freefluo.util.event.*;

class StatusPollingThread extends Thread {
    private static Logger logger = Logger.getLogger(StatusPollingThread.class);

    private Engine engine;
    private String workflowInstanceId;
    private PollingSettings pollingSettings;
    private List listeners;
    private Topic topic;

    public StatusPollingThread(Engine engine, String workflowInstanceId, 
                               PollingSettings pollingSettings, List listeners) {
      this.engine = engine;
      this.workflowInstanceId = workflowInstanceId;
      this.pollingSettings = pollingSettings;
      this.listeners = listeners;
      
      this.topic = new Topic();
    
      for(Iterator i = listeners.iterator(); i.hasNext();) {
        final WorkflowStateListener listener = (WorkflowStateListener) i.next();
        
        Consumer consumer = new Consumer() {
          public void newEvent(Object event) {
            listener.workflowStateChanged((WorkflowStateChangedEvent) event);
          }
        };

        topic.addConsumer(consumer);
      }
    }

    public void run() {
      int statusRetries = 0;
      int pollingDelay = pollingSettings.getMinDelay();
      String strStatus = null;
      String errorMsg = null;
      WorkflowState lastState = null;
      WorkflowState state = null;
      WorkflowStateChangedEvent event = null;

      topic.start();

      if(logger.isDebugEnabled()) {
        logger.debug("minPollingDelay: " + pollingSettings.getMinDelay() + 
                     "\tmaxPollingDelay: " + pollingSettings.getMaxDelay() + 
                     "\tpollingExponent: " + pollingSettings.getExponent());
      }
      while(true) {
        try {
          if(logger.isDebugEnabled()) {
            logger.debug("Remote workflow: " + workflowInstanceId + 
                         ". Sleeping for: " + pollingDelay);
            
          }
          Thread.sleep(pollingDelay);
        }
        catch(InterruptedException e) {
        }

        int temp = (int) (pollingDelay * pollingSettings.getExponent());
      
        // a maxPollingDelay of 0 indicates an unbounded maximum
        if(pollingSettings.getMaxDelay() == 0 || temp <= pollingSettings.getMaxDelay()) {
          pollingDelay = temp;
        }
        else if (pollingSettings.getMaxDelay() > pollingSettings.getMinDelay()) {
          // the maximum has been exceeded, so set the polling delay
          // to the maximum
          pollingDelay = pollingSettings.getMaxDelay();
        }

        try {
          strStatus = engine.getStatus(workflowInstanceId);
          state = WorkflowState.getState(strStatus);  
        } 
        catch (Exception e) {
          if(statusRetries == pollingSettings.getMaxRetries()) {
            String msg = "Error polling status of remote workflow " + workflowInstanceId +
                         " and no retries remaining (retry=" + statusRetries + 
                         ", maxPollingRetries=" + pollingSettings.getMaxRetries() + "). ";
            logger.error(msg, e);
            state = WorkflowState.FAILED;
            errorMsg = msg + e.getMessage();
          }
          else {
            logger.warn("Error getting job status of remote workflow " + workflowInstanceId +
                        " but going to retry (retry=" + statusRetries + ", maxPollingRetries=" + pollingSettings.getMaxRetries() + ").", e);
            statusRetries += 1;
            continue;
          }
        }

        if(state == WorkflowState.FAILED && errorMsg == null) {
          // The workflow failed and the status was polled successfully.
          try {
            errorMsg = engine.getErrorMessage(workflowInstanceId);
          }
          catch(Exception e) {
            errorMsg = "Workflow " + workflowInstanceId + " failed, but unable to retrieve " +
                       "error message from server: " + e.getMessage();
            logger.error(errorMsg, e);
          }
        }

        if(lastState == null || state != lastState) {
          lastState = state;
          event = new WorkflowStateChangedEvent(workflowInstanceId, state, errorMsg);
          topic.put(event);
        }
        
        if(state.isFinal()) {
          topic.stop();
          break;
        }   
      } // while
    } // method run
  } // class PollingThread