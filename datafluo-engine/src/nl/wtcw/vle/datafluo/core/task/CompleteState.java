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

public class CompleteState extends TaskState {
  //private static Logger logger = Logger.getLogger(CompleteState.class);
  protected CompleteState(String stateString, int state) {
    super(stateString, state);
  }

  public void destroy(AbstractTask task) {
    task.handleDestroy();
    task.setState(TaskState.DESTROYED);
    taskStateChanged(task);
  }
 
  //The following are needed to handle these calls for interruptible tasks.
  public void pause(AbstractTask task) {}

  public void paused(AbstractTask task) {}

  public void cancel(AbstractTask task) {}

  public void cancelled(AbstractTask task) {}

  public void resume(final AbstractTask task, final RunEvent runEvent) {}

}
