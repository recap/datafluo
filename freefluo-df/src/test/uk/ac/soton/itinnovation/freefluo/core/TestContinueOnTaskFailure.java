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

public class TestContinueOnTaskFailure extends TestCase implements FlowStateListener, TaskStateListener {
  private static Logger logger = Logger.getLogger(TestContinueOnTaskFailure.class);

  FlowRegistry flowRegistry = FlowRegistry.getInstance();
  Flow flow = null;

  DataTask a = null;
  DataTask b = null;
  DataTask c = null;
  DataTask d = null;
  DataTask e = null;
  DataTask f = null;

  AddModelTask addOne = null;
  AddModelTask addTwo = null;
  AddModelTask addThree = null;
  
  Object stopObject = new Object();

  public TestContinueOnTaskFailure(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(TestContinueOnTaskFailure.class);
    return suite;
  }

  protected void setUp() {
    flow = new Flow("flow", null);

    a = new DataTask("a", flow);
    b = new DataTask("b", flow);
    c = new DataTask("c", flow);
    d = new DataTask("d", flow);
    e = new DataTask("e", flow);
    f = new DataTask("f", flow);

    addOne = new AddModelTask("addOne", flow, a, b, d);
    addTwo = new AddModelTask("addTwo", flow, a, b, e);
    addThree = new AddModelTask("addThree", flow, d, e, f);
		
    a.setIntValue(1);
    b.setIntValue(2);
    addOne.setFail(true);
    c.setFail(true);
    addTwo.setDelay(2000L);
		
    a.linkTo(addOne);
    a.linkTo(addTwo);
    b.linkTo(addOne);
    b.linkTo(addTwo);

    addOne.linkTo(d);
    addTwo.linkTo(e);

    d.linkTo(addThree);
    e.linkTo(addThree);

    addThree.linkTo(f);

    flow.addFlowStateListener(this);
    //flow.addTaskStateListener(this);
  }

  protected void tearDown() {}

  public void testFailOnTaskFailure() throws Exception {
    flow.run();

    synchronized(stopObject) {
      stopObject.wait();
    }

    assertEquals(FlowState.FAILED, flow.getState());
  }

  public void testContinueOnFailure() throws Exception {
    c.setCritical(false);
    addOne.setCritical(false);
    flow.run();

    synchronized(stopObject) {
      stopObject.wait();
    }

    assertEquals(FlowState.COMPLETE, flow.getState());
    
    assertEquals(TaskState.COMPLETE, a.getState());
    assertEquals(TaskState.COMPLETE, b.getState());
    assertEquals(TaskState.COMPLETE, addTwo.getState());
    assertEquals(TaskState.COMPLETE, e.getState());
    assertEquals(TaskState.FAILED, c.getState());
    assertEquals(TaskState.FAILED, addOne.getState());
    assertEquals(TaskState.NEW, d.getState());
    assertEquals(TaskState.NEW, addThree.getState());
    assertEquals(TaskState.NEW, f.getState());

    assertEquals(0, f.getIntValue());
  }

  public void flowStateChanged(FlowStateChangedEvent flowStateChangedEvent) {
    FlowState flowState = flowStateChangedEvent.getFlow().getState();
    //System.out.println("FlowState: " + flowState);
    if(flowState == FlowState.COMPLETE || flowState == FlowState.FAILED) {
      synchronized(stopObject) {
        stopObject.notify();
      }
    }
  }

  public void taskStateChanged(TaskStateChangedEvent taskStateChangedEvent) {
    TaskState taskState = taskStateChangedEvent.getTask().getState();
    //System.out.println("TaskState: " + taskState);
  }
}
