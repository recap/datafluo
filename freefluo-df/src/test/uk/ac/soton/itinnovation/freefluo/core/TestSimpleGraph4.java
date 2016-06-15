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

public class TestSimpleGraph4 extends TestCase implements FlowStateListener, TaskStateListener {
  private static Logger logger = Logger.getLogger(TestSimpleGraph4.class);

  FlowRegistry flowRegistry = FlowRegistry.getInstance();
  Flow flow = null;

  DataTask a = null;
  DataTask b = null;
  DataTask c = null;
  DataTask d = null;
  DataTask e = null;
  DataTask temp = null;
  DataTask tmp4 = null;
  DataTask tmp5 = null;
  DataTask tmp6 = null;
  DataTask res = null;
  DataTask exp = null;

  f1Task f1 = null;
  SubModelTask S2 = null;
  MultModelTask M2 = null;
  MultModelTask M3 = null;
  AddModelTask A = null;

  Object stopObject = new Object();
  Object taskStopObject = new Object();
  Object delayObject = new Object();

  public TestSimpleGraph4(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(TestSimpleGraph4.class);
    return suite;
  }

  protected void setUp() {
    flow = new Flow("flow", null);

    a = new DataTask("a", flow);
    b = new DataTask("b", flow);
    c = new DataTask("c", flow);
    d = new DataTask("d", flow);
    e = new DataTask("e", flow);
    temp = new DataTask("temp", flow);
    tmp4 = new DataTask("tmp4", flow);
    tmp5 = new DataTask("tmp5", flow);
    tmp6 = new DataTask("tmp6", flow);
    res = new DataTask("res", flow);
    exp = new DataTask("exp", flow);
    f1 = new f1Task("f1", flow, a, b, temp);
    S2 = new SubModelTask("S2", flow, temp, c, tmp4);
    M2 = new MultModelTask("M2", flow, c, d, tmp5);
    M3 = new MultModelTask("M3", flow, tmp4, e, tmp6);
    A = new AddModelTask("A", flow, tmp5, tmp6, res);

    exp.setDoubleValue(4359.5);	
    //temp.setDoubleValue(20863.0);	
    a.setDoubleValue(25633.0);
    b.setDoubleValue(477.0);
    c.setDoubleValue(1920.0);
    d.setDoubleValue(0.1);
    e.setDoubleValue(0.22);
     
    a.linkTo(f1);
    b.linkTo(f1);
    f1.linkTo(temp);

    temp.linkTo(S2);
    c.linkTo(S2);
    S2.linkTo(tmp4);

    c.linkTo(M2);
    d.linkTo(M2);
    M2.linkTo(tmp5);

    tmp4.linkTo(M3);
    e.linkTo(M3);
    M3.linkTo(tmp6);
 
    tmp5.linkTo(A);
    tmp6.linkTo(A);
    A.linkTo(res);

    flow.addFlowStateListener(this);
    d.addTaskStateListener(this);
    c.addTaskStateListener(this);
    b.addTaskStateListener(this);
    temp.addTaskStateListener(this);
  }

  protected void tearDown() {}

  public void testSimpleRun() throws Exception {
    setUp();

    flow.run();
    assertEquals(FlowState.RUNNING, flow.getState());

    synchronized(stopObject) {
      stopObject.wait();
    }
    assertEquals(20863.0, temp.getDoubleValue(), 05);
    assertEquals(exp.getDoubleValue(), res.getDoubleValue(), 05);

    assertEquals(FlowState.COMPLETE, flow.getState());
    flow.destroy();
  }

  /*
   public void testPauseOnM1() throws Exception {
   setUp();
   //M1.pause();
   flow.run();
   
   while(tmp.getState().getStateString().equals("NEW") && (tmp.getState().getStateString().equals("NEW") || d.getState().getStateString().equals("NEW")) );
   flow.pause();

   assertEquals("Processor M1","NEW",M1.getState().getStateString());
   assertEquals("Processor M2","NEW",M2.getState().getStateString());
   assertEquals("Processor M3","NEW",M3.getState().getStateString());
   assertEquals("Processor S1","NEW",S1.getState().getStateString());
   assertEquals("Processor S2","NEW",S2.getState().getStateString());
   synchronized(delayObject) {
   delayObject.wait(10000);
   }

   String M2State=M2.getState().getStateString();

   assertEquals("Processor M1","PAUSED",M1.getState().getStateString()); 
   assertTrue("Processor M2",M2State.equals("PAUSED")|| M2State.equals("NEW"));
   assertEquals("Processor M3","NEW",M3.getState().getStateString());
   assertEquals("Processor S1","NEW",S1.getState().getStateString());
   assertEquals("Processor S2","NEW",S2.getState().getStateString());
   flow.resume();

   
   assertEquals(FlowState.RUNNING, flow.getState());

   synchronized(stopObject) {
   stopObject.wait(3000);
   }

   assertEquals( exp.getDoubleValue(),res.getDoubleValue(),05);
   assertEquals(FlowState.COMPLETE, flow.getState());
   flow.destroy();
   }

   */
  public void flowStateChanged(FlowStateChangedEvent flowStateChangedEvent) {
    FlowState flowState = flowStateChangedEvent.getFlow().getState();
    //System.out.println("FlowState: " + flowState);
    if(flowState == FlowState.PAUSED) 
      synchronized(delayObject) {
        delayObject.notify();
      }
    if(flowState == FlowState.COMPLETE || flowState == FlowState.FAILED) {
      synchronized(stopObject) {
        stopObject.notify();
      }
    }
  }

  public void taskStateChanged(TaskStateChangedEvent taskStateChangedEvent) {
    TaskState taskState = taskStateChangedEvent.getTask().getState();
    if(taskState != TaskState.NEW) {
      synchronized(taskStopObject) {
        taskStopObject.notify();
      }
    }
  }
}
