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
//      Created By          :   Nikolaos Matskanis
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

public class TestSimpleGraph3 extends TestCase implements FlowStateListener, TaskStateListener {
  //private static Logger logger = Logger.getLogger(TestSimpleGraph3.class);

  FlowRegistry flowRegistry = FlowRegistry.getInstance();
  Flow flow = null;

  DataTask a = null;
  DataTask b = null;
  DataTask c = null;
  DataTask d = null;
  DataTask e = null;
  DataTask temp = null;
  DataTask res = null;
  DataTask exp = null;

  f1Task f1 = null;
  f2Task f2 = null;
  
  Object stopObject = new Object();
  Object taskStopObject = new Object();
  Object delayObject = new Object();

  public TestSimpleGraph3(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(TestSimpleGraph3.class);
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
    res = new DataTask("res", flow);
    exp = new DataTask("exp", flow);
    f1 = new f1Task("f1", flow, a, b, temp);
    f2 = new f2Task("f2", flow, temp, c, d, e, res);
    
    exp.setDoubleValue(4359.5);	
    //temp.setDoubleValue(20863.0);	
    a.setDoubleValue(25633.0);
    b.setDoubleValue(477.0);
    c.setDoubleValue(1920.0);
    d.setDoubleValue(0.10);
    e.setDoubleValue(0.22);
     
    a.linkTo(f1);
    b.linkTo(f1);
    f1.linkTo(temp);

    temp.linkTo(f2);
    e.linkTo(f2);
    c.linkTo(f2);
    d.linkTo(f2);
    f2.linkTo(res);

    flow.addFlowStateListener(this);
  }

  protected void tearDown() {}

  public void testSimpleRun() throws Exception {
    setUp();
    System.out.println("<test name=testSimpleRun>");
    flow.run();
    f2.tmp6.finish();
    //assertEquals(FlowState.RUNNING, flow.getState());

    synchronized(stopObject) {
      stopObject.wait();
    }
    assertEquals(20863.0, temp.getDoubleValue(), 05);
    assertEquals(exp.getDoubleValue(), res.getDoubleValue(), 05);

    assertEquals(FlowState.COMPLETE, flow.getState());
    flow.destroy();
    System.out.println("</test>");
  }

  public void testPauseOnf1() throws Exception {
    setUp();
    System.out.println("<test name=testPauseOnf1>");
    a.addTaskStateListener(this);
    flow.run();
    
    //synchronized(taskStopObject) {
    //	taskStopObject.wait();
    //}
    flow.pause();
    f2.tmp6.finish();
    //  synchronized(delayObject) {
    //delayObject.wait(3000);
    // }
    assertTrue("Processor f1", f1.getState().getStateString() == "PAUSED" || f1.getState().getStateString() == "NEW"); 
    assertEquals("Processor f2", "NEW", f2.getState().getStateString()); 
    assertEquals(0.0, temp.getDoubleValue(), 05);
    flow.resume();
    assertEquals(FlowState.RUNNING, flow.getState());
    synchronized(stopObject) {
      stopObject.wait(5000);
    }

    assertEquals(20863.0, temp.getDoubleValue(), 05);
    assertEquals(exp.getDoubleValue(), res.getDoubleValue(), 05);
    assertEquals(FlowState.COMPLETE, flow.getState());
    flow.destroy();
    System.out.println("</test>");
  }

  public void testBreakpointOnf1() throws Exception {
    setUp();
    System.out.println("<test name=testBreakpointOnf1>");
    a.addTaskStateListener(this);
    f1.addBreakpoint();
    System.out.println("Added breakpoint on f1 " + f1.isBreakpoint());

    flow.run();
    
    //synchronized(taskStopObject) {
    //	taskStopObject.wait();
    //}
    synchronized(delayObject) {
      delayObject.wait(3000);
    }
    assertEquals("Processor f1", "PAUSED", f1.getState().getStateString()); 
    assertEquals("Processor f2", "NEW", f2.getState().getStateString()); 
    assertEquals(20863.0, temp.getDoubleValue(), 05);
    System.out.println("Nested Flow Processor f1 has output " + temp.getDoubleValue());
		f1.removeBreakpoint();

    System.out.println("Resuming  f1");
    flow.resumeTask("f1");
    f2.tmp6.finish();
    assertEquals(FlowState.RUNNING, flow.getState());
    synchronized(stopObject) {
      stopObject.wait(5000);
    }

    assertEquals(exp.getDoubleValue(), res.getDoubleValue(), 05);
    assertEquals(FlowState.COMPLETE, flow.getState());
    flow.destroy();
    System.out.println("</test>");
  }

  public void testPauseOnf2() throws Exception {
    setUp();
    System.out.println("<test name=testPauseOnf2>");
    temp.addTaskStateListener(this);
    //f2.A.addBreakpoint();
    
    Thread t = new Thread() {
        public void run() {
          try {
            Thread.sleep(5000);

            for(Iterator i = f2.iflow2.getTasks().iterator(); i.hasNext();) {
              Task task = (Task) i.next();
              System.out.println("f2 sub task: " + task.getTaskId() + " state: " + task.getState());
            }

            System.out.println("flow.getTasks().getSize(): " + flow.getTasks().size());
            for(Iterator i = flow.getTasks().iterator(); i.hasNext();) {
              Task task = (Task) i.next();
              System.out.print("flow sub task: " + task.getTaskId());
              System.out.println();
              //System.out.print(" state: " + task.getState() + "\n");
            }
          }
          catch(Exception e) {
            e.printStackTrace();
          }
        }
      };
    //t.start();	
    flow.run();
    
    synchronized(taskStopObject) {
      taskStopObject.wait(1000);
    }

    System.out.println("Task tmp6 will finish in one sec!");
    synchronized(delayObject) {
      delayObject.wait(1000);
    }
    flow.pause();
    f2.tmp6.finish();
    System.out.println("Waiting for f2 to finish ... ");
    synchronized(delayObject) {
      delayObject.wait(1000);
    }
    System.out.println("Tasks are paused ... ");
    assertEquals("Processor f2", "PAUSED", f2.getState().getStateString()); 
    assertEquals("Processor iflow2.tmp6", "PAUSED", f2.tmp6.getState().getStateString()); 
    assertEquals("Processor f1", "COMPLETE", f1.getState().getStateString()); 
    assertEquals(20863.0, temp.getDoubleValue(), 05);
    assertEquals(0.0, res.getDoubleValue(), 0.5);
    flow.resume();
    synchronized(stopObject) {
      stopObject.wait(1000);
    }

    assertEquals(FlowState.COMPLETE, flow.getState());
    assertEquals(exp.getDoubleValue(), res.getDoubleValue(), 05);
    flow.destroy();
    System.out.println("</test>");
  }

  public void testCancelOnPausedf2() throws Exception {
    setUp();
    System.out.println("<test name=testCancelOnPausedf2>");
    temp.addTaskStateListener(this);
    //f2.A.addBreakpoint();
    
    Thread t = new Thread() {
        public void run() {
          try {
            Thread.sleep(5000);

            for(Iterator i = f2.iflow2.getTasks().iterator(); i.hasNext();) {
              Task task = (Task) i.next();
              System.out.println("f2 sub task: " + task.getTaskId() + " state: " + task.getState());
            }

            System.out.println("flow.getTasks().getSize(): " + flow.getTasks().size());
            for(Iterator i = flow.getTasks().iterator(); i.hasNext();) {
              Task task = (Task) i.next();
              System.out.print("flow sub task: " + task.getTaskId());
              System.out.println();
              //System.out.print(" state: " + task.getState() + "\n");
            }
          }
          catch(Exception e) {
            e.printStackTrace();
          }
        }
      };
    //t.start();	
    flow.run();
    
    synchronized(taskStopObject) {
      taskStopObject.wait(1000);
    }
    flow.pause();

    System.out.println("Task tmp6 will finish in one sec!");
    synchronized(delayObject) {
      delayObject.wait(1000);
    }
    f2.tmp6.finish();
    System.out.println("Waiting for f2 to finish ... ");
    synchronized(delayObject) {
      delayObject.wait(1000);
    }
    System.out.println("Tasks are paused ... ");
    assertEquals("Processor f2", "PAUSED", f2.getState().getStateString()); 
    assertEquals("Processor f1", "COMPLETE", f1.getState().getStateString()); 
    assertEquals(20863.0, temp.getDoubleValue(), 05);
    assertEquals(0.0, res.getDoubleValue(), 0.5);
    flow.cancel();
    synchronized(stopObject) {
      stopObject.wait(2000);
    }

    assertEquals(FlowState.CANCELLED, flow.getState());
    flow.destroy();
    System.out.println("</test>");
  }

  public void testCancelOnf2() throws Exception {
    setUp();
    System.out.println("<test name=testCancelOnf2>");
    temp.addTaskStateListener(this);
    //f2.A.addBreakpoint();
    
    Thread t = new Thread() {
        public void run() {
          try {
            Thread.sleep(5000);

            for(Iterator i = f2.iflow2.getTasks().iterator(); i.hasNext();) {
              Task task = (Task) i.next();
              System.out.println("f2 sub task: " + task.getTaskId() + " state: " + task.getState());
            }

            System.out.println("flow.getTasks().getSize(): " + flow.getTasks().size());
            for(Iterator i = flow.getTasks().iterator(); i.hasNext();) {
              Task task = (Task) i.next();
              System.out.print("flow sub task: " + task.getTaskId());
              System.out.println();
              //System.out.print(" state: " + task.getState() + "\n");
            }
          }
          catch(Exception e) {
            e.printStackTrace();
          }
        }
      };
    //t.start();	
    flow.run();
    
    synchronized(taskStopObject) {
      taskStopObject.wait(1000);
    }
    flow.cancel();

    System.out.println("Task tmp6 will finish in one sec!");
    synchronized(delayObject) {
      delayObject.wait(1000);
    }
    f2.tmp6.finish();
    System.out.println("Waiting for f2 to finish ... ");
    synchronized(stopObject) {
      stopObject.wait(1000);
    }
    assertEquals("Processor f2", "CANCELLED", f2.getState().getStateString()); 
    assertEquals("Processor f1", "COMPLETE", f1.getState().getStateString()); 
    assertEquals(FlowState.CANCELLED, flow.getState());
    flow.destroy();
    System.out.println("</test>");
  }

  public void flowStateChanged(FlowStateChangedEvent flowStateChangedEvent) {
    FlowState flowState = flowStateChangedEvent.getFlow().getState();
    //if(flowState == FlowState.PAUSED) 
    // synchronized(delayObject) {
    //  delayObject.notify();
    //}
    if(flowState == FlowState.COMPLETE || flowState == FlowState.CANCELLED || flowState == FlowState.FAILED) {
      synchronized(stopObject) {
        stopObject.notify();
      }
    }
  }

  public void taskStateChanged(TaskStateChangedEvent taskStateChangedEvent) {
    TaskState taskState = taskStateChangedEvent.getTask().getState();
    if(taskState == TaskState.COMPLETE) {
      synchronized(taskStopObject) {
        taskStopObject.notify();
      }
    }
  }
}
