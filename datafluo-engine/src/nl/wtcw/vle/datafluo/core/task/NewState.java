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
//      Created By          :   Darren Marvin
//      Created Date        :   2002/7/27
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/08/23 06:24:47 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////
package nl.wtcw.vle.datafluo.core.task;

import java.util.*;

import org.apache.log4j.*;

import nl.wtcw.vle.datafluo.core.event.*;
import nl.wtcw.vle.datafluo.core.flow.*;
import nl.wtcw.vle.datafluo.core.port.*;

public class NewState extends TaskState {
  //private Logger logger = Logger.getLogger(NewState.class);
  
  protected NewState(String stateString, int state) {
    super(stateString, state);
  }


  /* matskan: Modified to support pausing of tasks.
   *
   */
  @Override
  public void run(AbstractTask task, RunEvent runEvent) {
    RunEventSource runEventSource = runEvent.getSource();
    //Collection parents = task.getParents();
	Collection inputPorts = task.getInputPorts();
    Set runEvents = task.getRunEvents();
    Flow flow = task.getFlow();


    
    if(inputPorts.size() > 0 && runEventSource instanceof Port) {
      // not a start task in the workflow. The run event is from
      // a parent task.
      Port remotePort = (Port) runEventSource;
	  //System.err.println("Task " + task.getTaskId() + " received RunEvent from Task " + remotePort.getTask().getTaskId() );
	  
      runEvents.add(remotePort);
	  //System.err.println("Task " + task.getTaskId() + " input port size: " + task.getInputPorts().size() + " run event size: " + runEvents.size() );
    }
    
    final AbstractTask finalTask = task;
    final RunEvent finalRunEvent = runEvent;

  /*  Thread runThread = new Thread() {
		@Override
        public void run() {
          finalTask.handleRun(finalRunEvent);
        }
      };*/
    
    if(runEvents.size() == inputPorts.size() && !flow.isCancelTasks()) {

		Thread runThread = new Thread() {
		@Override
        public void run() {		  
          finalTask.handleRun(finalRunEvent);
        }
      };
      // runEvents.size() == parents.size() if 
      // the Task is a start Task - i.e. has no parents-
      // and the Flow requested that it run; or if
      // the Task has received run events from all
      // of its parents
      //runEvents.clear();
      //
      //Change the state according to isPausable and isCancelable attributes.
      if(task.isPauseable() || task.isCancelable()) {
        //if (!finalTask.isPause()){
        runEvents.clear();
        task.setState(TaskState.IRUN);
        taskStateChanged(task); 
        runThread.start();

        /*} else {
         task.setState(TaskState.PAUSED);
         task.taskPaused();
         taskStateChanged(task); 
         }*/
      }
      else { 
        runEvents.clear();
        task.setState(TaskState.RUNNING); 
        taskStateChanged(task); 
        runThread.start(); 
      }
      //if (!finalTask.isPause())runThread.start();
    }
  }

  public void destroy(AbstractTask task) {
    task.handleDestroy();
    task.setState(TaskState.DESTROYED);
    taskStateChanged(task);
  }
}
