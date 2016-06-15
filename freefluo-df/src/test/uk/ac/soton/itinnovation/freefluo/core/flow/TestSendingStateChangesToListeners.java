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
//      Created By          :   Ahmed Saleh
//      Created Date        :   2005/03/10
//      Created for Project :   MyIB
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/08/23 06:24:49 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.core.flow;

import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;

import junit.framework.*;

import uk.ac.soton.itinnovation.freefluo.core.registry.*;
import uk.ac.soton.itinnovation.freefluo.core.flow.*;
import uk.ac.soton.itinnovation.freefluo.core.*;
import uk.ac.soton.itinnovation.freefluo.core.task.*;
import uk.ac.soton.itinnovation.freefluo.core.event.*;
import uk.ac.soton.itinnovation.freefluo.util.*;

public class TestSendingStateChangesToListeners extends TestCase implements FlowStateListener, TaskStateListener {
  private static Logger logger = Logger.getLogger(TestSendingStateChangesToListeners.class);

  FlowRegistry flowRegistry = FlowRegistry.getInstance();
  Flow flow = null;

  SimpleTask task1 = null;
  SimpleTask task2 = null;
  SimpleTask task3 = null;
  SimpleTask task4 = null;
  SimpleTask task5 = null;
  SimpleTask task6 = null;
  SimpleTask task7 = null;
  SimpleTask task8 = null;
  SimpleTask task9 = null;

  Object stopObject = new Object();

  public TestSendingStateChangesToListeners(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(TestSendingStateChangesToListeners.class);
    return suite;
  }

  protected void setUp() {
		
    flow = new Flow("flow", null);

    task1 = new SimpleTask("task1", flow);
    task2 = new SimpleTask("task2", flow);
    task3 = new SimpleTask("task3", flow);
    task4 = new SimpleTask("task4", flow);
    task5 = new SimpleTask("task5", flow);
    task6 = new SimpleTask("task6", flow);
    task7 = new SimpleTask("task7", flow);
    task8 = new SimpleTask("task8", flow);
    task9 = new SimpleTask("task9", flow);

    task1.linkTo(task2);
    task2.linkTo(task3);
    task3.linkTo(task4);
    task4.linkTo(task5);
    task5.linkTo(task6);
    task6.linkTo(task7);
    task7.linkTo(task8);
    task8.linkTo(task9);

    flow.addFlowStateListener(this);
    flow.addTaskStateListener(this);
    task1.addTaskStateListener(this);
    task2.addTaskStateListener(this);
    task3.addTaskStateListener(this);
    task4.addTaskStateListener(this);
    task5.addTaskStateListener(this);
    task6.addTaskStateListener(this);
    task7.addTaskStateListener(this);
    task8.addTaskStateListener(this);
    task9.addTaskStateListener(this);
   
  }

  public void testSendingStateChanges() throws Exception {
    flow.run();

    synchronized(stopObject) {
      stopObject.wait(1000);
    }
    flow.destroy();
  }

  public void flowStateChanged(FlowStateChangedEvent flowStateChangedEvent) {
    FlowState flowState = flowStateChangedEvent.getFlow().getState();
    System.out.println("FlowState: " + flowState);
 
  }

  public void taskStateChanged(TaskStateChangedEvent taskStateChangedEvent) {
    TaskState taskState = taskStateChangedEvent.getTask().getState();
    System.out.println("TaskState: " + taskStateChangedEvent.getTask().getTaskId() + " = " + taskState);
  }
}
