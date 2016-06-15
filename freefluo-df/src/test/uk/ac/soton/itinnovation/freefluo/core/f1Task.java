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
//      Created Date        :   2004/03/23
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/08/23 06:24:48 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.core;

import org.apache.log4j.*;

import uk.ac.soton.itinnovation.freefluo.core.flow.*;
import uk.ac.soton.itinnovation.freefluo.core.task.*;
import uk.ac.soton.itinnovation.freefluo.core.event.*;

public class f1Task extends FlowTask {
  //private static Logger logger = Logger.getLogger(f1Task.class);
  private DataTask inputA = null;
  private DataTask inputB = null;
  private DataTask output = null;
  private DataTask tmp = null;
  private DataTask tmp2 = null;
  private DataTask tmp3 = null;
  private SubModelTask S1 = null;
  private MultModelTask M1 = null;
  public Flow iflow = null;

  public f1Task(String name, Flow flow, DataTask inputA, DataTask inputB, DataTask output) {
    super(name, flow, true);
    this.inputA = inputA;
    this.inputB = inputB;
    this.output = output;

    iflow = new Flow("iflow", null);
    tmp = new DataTask("tmp", iflow);
    tmp2 = new DataTask("tmp2", iflow);
    tmp3 = new DataTask("tmp3", iflow);
    M1 = new MultModelTask("M1", iflow, inputB, tmp, tmp2);
    S1 = new SubModelTask("S1", iflow, inputA, tmp2, tmp3);
    tmp.setDoubleValue(10.0);
    tmp.linkTo(M1);
    M1.linkTo(tmp2);

    tmp2.linkTo(S1);
    S1.linkTo(tmp3);
     
    setTaskFlow(iflow);
    
  }

  protected void execute() { 
    output.setDoubleValue(tmp3.getDoubleValue());
  }

  protected void taskPaused() {}

  protected void taskComplete() {}

  protected void taskCancelled() {}

  protected void taskResumed() {}
  
}
