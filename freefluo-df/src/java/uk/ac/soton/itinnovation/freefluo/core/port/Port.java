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
//      Created Date        :   2003/05
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

import uk.ac.soton.itinnovation.freefluo.core.*;
import uk.ac.soton.itinnovation.freefluo.core.task.*;
import uk.ac.soton.itinnovation.freefluo.core.flow.*;
import uk.ac.soton.itinnovation.freefluo.core.event.*;
import uk.ac.soton.itinnovation.freefluo.core.util.*;

import org.apache.log4j.Logger;

public class Port implements PortStateListener, DataEventSource, RunEventSource {
  public static final int DIRECTION_INVALID = 0;
  public static final int DIRECTION_OUT = 1;
  public static final int DIRECTION_IN = 2;
  public static final int DIRECTION_BOTH = 3;
  
  private static Logger logger = Logger.getLogger(Port.class);

  /** Whether or not this task has breakpoint*/
  private boolean hasBreakpoint = false;

  /** Whether or not this task has breakpoint*/
  private boolean hasCompleted = false;

  protected String portId = null;
  protected String name = null;

  /** The internal state of this task. The task state begins as NEW */
  private PortState state = PortState.NEW;

  protected int direction = Port.DIRECTION_INVALID;

  private TimePoint startTime;
  private TimePoint endTime;
  protected Task task = null;


  protected HashMap /* connectedPortId:String -> connectedPort:Port */ connectionPortMap = new HashMap();

  /**
   * List of Ports that have sent a dataAvailableEvent. Typically ports are PTP but
   * still we can model many-to-one port mapping. In this case when all ports send
   * the event the ports signals the Task.
   */
  protected HashSet /* connectedPort:Port */ dataAvailableEvents = new HashSet();

  /**
   * Set of parent tasks that have sent a run event to this task. When all parents
   * have sent a run event this task will start to run and clear the set
   */
  //protected HashSet /* parentTask:Task */ runEvents = new HashSet();

  protected final Object stateThreadSync = new Object();

  /**
   * Collection of listeners of events for task state changes
   */
  private ArrayList /* of TaskStateListener */ portStateListeners = new ArrayList();

  /**
   * Collection of listeners of arbitrary events related to tasks.
   * For example during iteration over a data set a subclass may decide
   * to notify listeners of completion of processing of a data item
   * in the set.
   */
  private ArrayList /* of TaskEventListener */ portEventListeners = new ArrayList();

  private String errorMessage = "";

  /**
   * Construct an AbstractTask with a unique id for <code>Task</code> (within the namespace of the <code>Flow</code>),
   * a human readable name for the <code>Task</code>, the containing <code>Flow</code>; and finally, a flag to indicate
   * whether the containing <code>Flow</code> should fail or conitinue should this <code>Task</code> fail.
   * @param taskId unique identifier within the namespace of the Flow that contains this Task.
   * @param name human readable name for the Task (unique amongst Tasks within the namespace of the Flow).
   * @param flow the containing Flow.
   * @param isCritical flag to indicate whether the containing Flow should continue or fail should
   * this Task fail at workflow runtime. If this is set to true, the Flow will fail, else it will continue to
   * run other Tasks that don't depend on this one.
   */
  public Port(String portId, String name, int direction, Task task) {
    this.portId = portId;
    this.name = name;
    this.task = task;
	this.direction = direction;

	if(direction == Port.DIRECTION_IN)
		task.addInputPort(this);
	if(direction == Port.DIRECTION_OUT)
		task.addOutputPort(this);
	
	this.enable();
 }


  public Port(String portId, int direction, Task task) {
    this(portId, "", direction, task);
  }


  // Task interface implementation
  public String getTaskId() {
    return portId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return name + ":" + portId;
  }


  public void setErrorMessage(String errorMessage) {
    synchronized(errorMessage) {
      this.errorMessage = errorMessage;
    }
  }

  public String getErrorMessage() {
    synchronized(errorMessage) {
      return errorMessage;
    }
  }

  public PortState getState() {
    synchronized(stateThreadSync) {
      return state;
    }
  }

  public Set getDataAvailableEvents() {
	  return dataAvailableEvents;
  }

  public String getStateString() {
    synchronized(stateThreadSync) {
      return state.getStateString();
    }
  }

  public TimePoint getStartTime() {
    return startTime;
  }

  public TimePoint getEndTime() {
    return endTime;
  }

  public String getPortId(){
	  return this.portId;
  }
  /**
   * An active state is a ports which has data on its port
   * @return
   */
  public boolean isStateActive(){
	  synchronized(stateThreadSync) {
      return state == PortState.ACTIVE;
    }
  }


  public Task getTask() {
    return task;
  }
  
  public void setTask(Task task){
		 this.task = task;
  }

  public void addConnection(Port port) {
	  //String s = port.getTask().getName() + port.getPortId();
	  String s = port.getTask().getTaskId() +":"+ port.getPortId();


    //connectionPortMap.put(port.getPortId(), port);
	  connectionPortMap.put(s, port);
  }

  public Collection getConnections() {
    return connectionPortMap.values();
  }

  public Port getConnection(String portTaskId) {
    return(Port) connectionPortMap.get(portTaskId);
  }

  public int getDirection(){
	  return direction;
  }



  //public Set getRunEvents() {
  //  return runEvents;
 // }

  //public void linkTo(Port remotePort) {
  //  this.addConnection(remotePort);
  //  childTask.addParent(this);
  //  flow.removeEndTask(this);
  //  flow.removeStartTask(childTask);
  //}

  //public final void run(RunEvent runEvent) {
   // RunEventSource source = runEvent.getSource();

    /*if(logger.isDebugEnabled()) {
     String description = "flow";


     if(source instanceof AbstractTask) {
     description = ((AbstractTask) source).getDescription();
     }

     logger.debug(getDescription() + " received run from " + description + ", state=" + getStateString());
     }*/

    //synchronized(stateThreadSync) {
     // state.run(this, runEvent);
   // }
  //}


 public final void activate(DataEvent dataEvent) {
	 synchronized(stateThreadSync) {
      state.activate(this,dataEvent);
    }
 }

  public final void enable(){
	  synchronized(stateThreadSync) {
      state.enable(this);
    }
  }
  public final void disable(){
	  synchronized(stateThreadSync) {
      state.disable(this);
    }
  }
  public final void destroy(DataEvent dataEvent) {
    synchronized(stateThreadSync) {
		
      state.destroy(this, dataEvent);
    }
  }

  public void addPortStateListener(PortStateListener portStateListener) {
    portStateListeners.add(portStateListener);
  }

  public void removePortStateListener(PortStateListener portStateListener) {
    portStateListeners.remove(portStateListener);
  }

  public void addPortEventListener(PortEventListener portEventListener) {
    portEventListeners.add(portEventListener);
  }

  public void removePortEventListener(PortEventListener portEventListener) {
    portEventListeners.remove(portEventListener);
  }

  public String toString() {

    /* String nl = System.getProperty("line.separator");

     StringBuffer sb = new StringBuffer();

     sb.append("Task:" + getTaskId() + nl);
     sb.append("\tstate: " + state.getStateString() + nl);
     sb.append("\tchildren:" + nl);

     for(Iterator i = childTaskMap.iterator(); i.hasNext(); sb.append(nl)) {
     sb.append("\t\t" + ((Task) i.next()).toString());
     }

     return sb.toString();
     */
    return getDescription();
  }

  // end Task interface implementation


  protected void setState(PortState state) {
    this.state = state;
  }

  public void portStateChanged(PortStateChangedEvent portStateChangedEvent) {
    final PortStateChangedEvent finalPortStateChangedEvent = portStateChangedEvent;

    logger.debug(getDescription() + " is in state " + state);

    // note: notify the flow of this ports change in state
    // this has to be synchronous as the order in which events are received
    // is significant
    task.portStateChanged(portStateChangedEvent);

	//HERE STATE
    for(Iterator i = portStateListeners.iterator(); i.hasNext();) {
     final PortStateListener portStateListener = (PortStateListener) i.next();
	 portStateListener.portStateChanged(finalPortStateChangedEvent);
     /*Thread thread = new Thread() {
     public void run() {
     portStateListener.portStateChanged(finalTaskStateChangedEvent);
     }
     };

     thread.start();*/
     }
  }


  /**
   * Utility method for subclasses to call in order to
   * send a TaskEvent to all registered TaskEventListeners.
   */
  protected void firePortEvent(PortEvent portEvent) {
    final PortEvent finalPortEvent = portEvent;

    for(Iterator i = portEventListeners.iterator(); i.hasNext();) {
      final PortEventListener portEventListener = (PortEventListener) i.next();

      Thread thread = new Thread() {
          public void run() {
            portEventListener.processEvent(finalPortEvent);
          }
        };

      thread.start();
    }
  }
}
