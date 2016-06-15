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

import nl.wtcw.vle.datafluo.core.event.*;

public class RunningState extends TaskState {
  protected RunningState(String stateString, int state) {
    super(stateString, state);
  }
		
	@Override
  public void complete(AbstractTask task) {
    task.stopTimer();
    task.setCompleted();
    if(task.isPause()) {
      task.setState(TaskState.PAUSED);
      task.taskPaused();
      taskStateChanged(task);
    }
    else { 
      task.handleComplete();
      task.setState(TaskState.COMPLETE);
      taskStateChanged(task);
    }
  }
	
 @Override
  public void run(AbstractTask task, RunEvent runEvent) {}
		
	@Override
  public void fail(AbstractTask task, String message) {
    task.stopTimer();

    task.handleFail();

    task.setErrorMessage(message);
    task.setState(TaskState.FAILED);
    taskStateChanged(task, message);
  }

}

