////////////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2002
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
//      Created Date        :   2004/06/29
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/08/23 06:24:48 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.core;

import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;

import junit.framework.*;

import uk.ac.soton.itinnovation.freefluo.core.registry.*;
import uk.ac.soton.itinnovation.freefluo.core.flow.*;
import uk.ac.soton.itinnovation.freefluo.core.task.*;
import uk.ac.soton.itinnovation.freefluo.core.event.*;
import uk.ac.soton.itinnovation.freefluo.util.*;

public class TestSimpleGraph extends TestCase implements FlowStateListener, TaskStateListener {
  private static Logger logger = Logger.getLogger(TestSimpleGraph.class);

  FlowRegistry flowRegistry = FlowRegistry.getInstance();
  Flow flow = null;

  DataTask a = null;
  DataTask b = null;
  
  CopyTask copyTask = null;
  
  Object stopObject = new Object();
  Object delayObject = new Object();
  Object finishObject = new Object();

  public TestSimpleGraph(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(TestSimpleGraph.class);
    return suite;
  }

  protected void setUp() {
    flow = new Flow("flow", null);

    a = new DataTask("a", flow);
    b = new DataTask("b", flow);
    copyTask = new CopyTask("copy", flow, a, b);

    a.setIntValue(0);
    b.setIntValue(1);

    a.linkTo(copyTask);
    copyTask.linkTo(b);

    flow.addFlowStateListener(this);
    copyTask.addTaskStateListener(this);
  }

  protected void tearDown() {}

  public void testPauseOnB() throws Exception {

    flow.run();
    
    synchronized(stopObject) {
      stopObject.wait(2222);
    }
    flow.pause();

    assertEquals("COMPLETE", a.getState().getStateString());
    assertEquals("RUNNING", copyTask.getState().getStateString());
    assertEquals("NEW", b.getState().getStateString());
    copyTask.finish();
    synchronized(delayObject) {
      delayObject.wait();
    }

    assertEquals("PAUSED", copyTask.getState().getStateString());
    assertEquals("NEW", b.getState().getStateString());
    assertEquals(FlowState.PAUSED, flow.getState());
    //assertEquals(1,flow.getPausedTasksSize());
    flow.resume();

    synchronized(finishObject) {
      finishObject.wait(3000);
    }

    //assertEquals("Paused tasks not as expected", 0,flow.getPausedTasksSize());
    //assertEquals("Running tasks not right", 0,flow.getRunningTasksSize());

    assertEquals(FlowState.COMPLETE, flow.getState());
  }

  public void flowStateChanged(FlowStateChangedEvent flowStateChangedEvent) {
    FlowState flowState = flowStateChangedEvent.getFlow().getState();
    //System.out.println("FlowState: " + flowState);
    if(flowState == FlowState.PAUSED) 
      synchronized(delayObject) {
        delayObject.notify();
      }
    //if(flowState == FlowState.COMPLETE || flowState==FlowState.FAILED) {
    //synchronized(stopObject) {
    //stopObject.notify();
    //}
    //}
  }

  public void taskStateChanged(TaskStateChangedEvent taskStateChangedEvent) {
    TaskState taskState = taskStateChangedEvent.getTask().getState();
    if(taskState == TaskState.RUNNING) {
      synchronized(stopObject) {
        stopObject.notify();
      }
    }
  }
}
