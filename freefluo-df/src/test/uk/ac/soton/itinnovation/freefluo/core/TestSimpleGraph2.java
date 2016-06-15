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

public class TestSimpleGraph2 extends TestCase implements FlowStateListener, TaskStateListener {
  private static Logger logger = Logger.getLogger(TestSimpleGraph2.class);

  FlowRegistry flowRegistry = FlowRegistry.getInstance();
  Flow flow = null;

  DataTask a = null;
  DataTaskX b = null;
  DataTask c = null;
  DataTask d = null;
  DataTask e = null;
  DataTaskX tmp = null;
  DataTask tmp2 = null;
  DataTask tmp3 = null;
  DataTask tmp4 = null;
  DataTask tmp5 = null;
  DataTask tmp6 = null;
  DataTask res = null;
  DataTask exp = null;

  SubModelTask S1 = null;
  SubModelTask S2 = null;
  MultModelTask M1 = null;
  MultModelTask M2 = null;
  MultModelTask M3 = null;
  AddModelTask A = null;
  
  Object stopObject = new Object();
  Object taskStopObject = new Object();
  Object delayObject = new Object();
  Object sleeping = new Object();

  public TestSimpleGraph2(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(TestSimpleGraph2.class);
    return suite;
  }

  protected void setUp() {
    flow = new Flow("flow", null);

    a = new DataTask("a", flow);
    b = new DataTaskX("b", flow);
    c = new DataTask("c", flow);
    d = new DataTask("d", flow);
    e = new DataTask("e", flow);
    tmp = new DataTaskX("tmp", flow);
    tmp2 = new DataTask("tmp2", flow);
    tmp3 = new DataTask("tmp3", flow);
    tmp4 = new DataTask("tmp4", flow);
    tmp5 = new DataTask("tmp5", flow);
    tmp6 = new DataTask("tmp6", flow);
    res = new DataTask("res", flow);
    exp = new DataTask("exp", flow);
    M1 = new MultModelTask("M1", flow, b, tmp, tmp2);
    S1 = new SubModelTask("S1", flow, a, tmp2, tmp3);
    S2 = new SubModelTask("S2", flow, tmp3, c, tmp4);
    M2 = new MultModelTask("M2", flow, c, d, tmp5);
    M3 = new MultModelTask("M3", flow, tmp4, e, tmp6);
    A = new AddModelTask("A", flow, tmp5, tmp6, res);
    
    exp.setDoubleValue(4359.5);	
    tmp.setDoubleValue(10.0);	
    a.setDoubleValue(25633.0);
    b.setDoubleValue(477.0);
    c.setDoubleValue(1920.0);
    d.setDoubleValue(0.1);
    e.setDoubleValue(0.22);
     
    b.linkTo(M1);
    tmp.linkTo(M1);
    M1.linkTo(tmp2);

    tmp2.linkTo(S1);
    a.linkTo(S1);
    S1.linkTo(tmp3);
    
    tmp3.linkTo(S2);
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
    tmp.addTaskStateListener(this);
  }

  protected void tearDown() {}

  public void testSimpleRun() throws Exception {

    flow.run();
    assertEquals(FlowState.RUNNING, flow.getState());

    synchronized(delayObject) {
      delayObject.wait(200);
    }
    tmp.finish();
    b.finish();
    synchronized(stopObject) {
      stopObject.wait();
    }

    assertEquals(exp.getDoubleValue(), res.getDoubleValue(), 05);

    assertEquals(FlowState.COMPLETE, flow.getState());
  }

  /*
   public void testPauseOnM1() throws Exception {
   setUp();
   //M1.pause();
   flow.run();
   
   tmp.finish();
   b.finish();
   while(M1.getState().getStateString().equals("NEW") );
   flow.pause();
   synchronized(delayObject) {
   delayObject.wait(2000);
   }

   assertEquals("Processor M1","PAUSED",M1.getState().getStateString());
   assertEquals("Processor M3","NEW",M3.getState().getStateString());
   assertEquals("Processor S1","NEW",S1.getState().getStateString());
   assertEquals("Processor S2","NEW",S2.getState().getStateString());
   //synchronized(sleeping) {
   //			sleeping.wait(2000);
   //}
   for (Iterator i=flow.getTasks().iterator(); i.hasNext();){
   Task task=(Task)i.next();
   logger.debug(flow.getFlowId() + " flow  has task "+task.getTaskId()+" in state" + task.getState());
   }
   assertEquals(FlowState.PAUSED, flow.getState());
   flow.resume();
   
   assertEquals(FlowState.RUNNING, flow.getState());
   

   synchronized(stopObject) {
   stopObject.wait(3000);
   }

   assertEquals(FlowState.COMPLETE, flow.getState());
   assertEquals( exp.getDoubleValue(),res.getDoubleValue(),05);
   flow.destroy();
   }
   */
  public void testPauseOnS1() throws Exception {

    setUp();
    flow.run();
    synchronized(delayObject) {
      delayObject.wait(1000);
    }
    tmp.finish();
    b.finish();
    
    while(S1.getState().getStateString().equals("NEW")/*&& (M1.getState().getStateString().equals("NEW") ||  M2.getState().getStateString().equals("NEW"))*/);
    flow.pause();

    String M1State = M1.getState().getStateString();
    //String M2State=M2.getState().getStateString();

    assertTrue("Processor M1", M1State.equals("COMPLETE"));
    assertEquals("Processor M3", "NEW", M3.getState().getStateString());
    assertEquals("Processor S2", "NEW", S2.getState().getStateString());
    synchronized(delayObject) {
      delayObject.wait(10000);
    }

    String S1State = S1.getState().getStateString();
    assertEquals("Processor M1", "COMPLETE", M1.getState().getStateString());
    assertEquals("Processor M2", "COMPLETE", M2.getState().getStateString());
    assertEquals("Processor M3", "NEW", M3.getState().getStateString());
    assertTrue("Processor S1", S1State.equals("PAUSED"));
    assertEquals("Processor S2", "NEW", S2.getState().getStateString());
    assertEquals(FlowState.PAUSED, flow.getState());
    flow.resume();

    synchronized(stopObject) {
      stopObject.wait(3000);
    }

    assertEquals(exp.getDoubleValue(), res.getDoubleValue(), 05);
    assertEquals(FlowState.COMPLETE, flow.getState());
    flow.destroy();
  }

  public void testPauseOnS2() throws Exception {

    setUp();
    flow.run();
    synchronized(delayObject) {
      delayObject.wait(1000);
    }
    tmp.finish();
    b.finish();
    
    while(S2.getState().getStateString().equals("NEW"));
    flow.pause();

    synchronized(delayObject) {
      delayObject.wait(10000);
    }

    assertEquals("Processor S2", "PAUSED", S2.getState().getStateString());
    assertEquals("Processor M3", "NEW", M3.getState().getStateString());
    assertEquals("Processor A", "NEW", A.getState().getStateString());
    flow.resume();

    assertEquals(FlowState.RUNNING, flow.getState());

    synchronized(stopObject) {
      stopObject.wait(3000);
    }

    assertEquals(exp.getDoubleValue(), res.getDoubleValue(), 05);
    assertEquals(FlowState.COMPLETE, flow.getState());
    flow.destroy();
  }
 
  public void testPauseOnA() throws Exception {

    setUp();
    flow.run();
    synchronized(delayObject) {
      delayObject.wait(1000);
    }
    tmp.finish();
    b.finish();
    
    while(A.getState().getStateString().equals("NEW"));
    flow.pause();

    synchronized(delayObject) {
      delayObject.wait(10000);
    }
    String M1State = M1.getState().getStateString();
    String M3State = M3.getState().getStateString();
    String S2State = S2.getState().getStateString();

    assertTrue("Processor M1", M1State.equals("COMPLETE"));

    assertEquals("Processor tmp5", "COMPLETE", tmp5.getState().getStateString());
    assertEquals("Processor A", "PAUSED", A.getState().getStateString());
    assertEquals("Processor res", "NEW", res.getState().getStateString());
    flow.resume();

    synchronized(stopObject) {
      stopObject.wait(3000);
    }

    assertEquals(exp.getDoubleValue(), res.getDoubleValue(), 05);
    assertEquals(FlowState.COMPLETE, flow.getState());
    flow.destroy();
  }
	
  public void testBreakpoint() throws Exception {

    setUp();
    flow.run();
    M1.addBreakpoint();
    System.out.println("Adding breakpoint on processor M1 ");
    tmp.finish();
    b.finish();
    synchronized(delayObject) {
      delayObject.wait(1000);
    }
    String M1State = M1.getState().getStateString();
    String M2State = M2.getState().getStateString();
    String S1State = S1.getState().getStateString();

    assertTrue("Processor M1", M1State.equals("PAUSED"));
    assertTrue("Processor M2", M2State.equals("COMPLETE"));
    assertTrue("Processor S1", S1State.equals("NEW"));
    assertEquals("Processor A", "NEW", A.getState().getStateString());

    assertEquals(FlowState.PAUSED, flow.getState());
    System.out.println("Flow is Paused.");
    System.out.println("Processor M1 has output " + tmp2.getDoubleValue());
    M1.removeBreakpoint();
    System.out.println("Processor M1 is resumed");
    flow.resumeTask("M1");

    synchronized(stopObject) {
      stopObject.wait(3000);
    }

    assertEquals(exp.getDoubleValue(), res.getDoubleValue(), 05);
    assertEquals(FlowState.COMPLETE, flow.getState());
    flow.destroy();
  }

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
    if(taskState == TaskState.RUNNING) {
      synchronized(taskStopObject) {
        taskStopObject.notify();
      }
    }
  }
}
