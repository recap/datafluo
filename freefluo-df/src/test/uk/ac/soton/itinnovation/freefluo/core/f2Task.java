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

public class f2Task extends FlowTask {
  //private static Logger logger = Logger.getLogger(f2Task.class);
  private DataTask inputE = null;
  private DataTask input = null;
  private DataTask output = null;
  private DataTask tmp4 = null;
  private DataTask tmp5 = null;
  public DataTaskX tmp6 = null;
  private DataTask tmp7 = null;
  private DataTask c = null;
  private DataTask d = null;
  private SubModelTask S2 = null;
  private MultModelTask M2 = null;
  private MultModelTask M3 = null;
  public AddModelTask A = null;
  public Flow iflow2 = null;

  public f2Task(String name, Flow flow, DataTask input, DataTask c, DataTask d, DataTask inputE, DataTask output) {
    super(name, flow, true);
    this.input = input;
    this.inputE = inputE;
    this.output = output;
    this.c = c;
    this.c = d;

    iflow2 = new Flow("iflow2", null);
    tmp4 = new DataTask("tmp4", iflow2);
    tmp5 = new DataTask("tmp5", iflow2);
    tmp6 = new DataTaskX("tmp6", iflow2);
    tmp7 = new DataTask("tmp7", iflow2);
    M2 = new MultModelTask("M2", iflow2, c, d, tmp5);
    S2 = new SubModelTask("S2", iflow2, input, c, tmp4);
    M3 = new MultModelTask("M3", iflow2, tmp4, inputE, tmp6);
    A = new AddModelTask("A", iflow2, tmp6, tmp5, output);

    //c.linkTo(M2);
    //d.linkTo(M2);
    M2.linkTo(tmp5);
    
    //input.linkTo(S2);
    //c.linkTo(S2);
    S2.linkTo(tmp4);

    tmp4.linkTo(M3);
    //inputE.linkTo(M3);
    M3.linkTo(tmp6);

    tmp5.linkTo(A);
    tmp6.linkTo(A);

    setTaskFlow(iflow2);
  }

  protected void execute() {//output.setDoubleValue(tmp7.getDoubleValue());
    //iflow2.destroy();
  }

  protected void taskPaused() {}

  protected void taskCancelled() {}

  protected void taskComplete() {}

  protected void taskResumed() {}

}
