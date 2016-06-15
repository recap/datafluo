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

public class CopyTask extends BaseTask {
  private static Logger logger = Logger.getLogger(CopyTask.class);
  private boolean booleanValue = false;
  private DataTask input = null;
  private DataTask output = null;
  private boolean X = true;
	
  int intValue = 0;

  public CopyTask(String name, Flow flow, DataTask input, DataTask output) {
    super(name, flow);
    this.input = input;
    this.output = output;
  }

  protected void execute() { 
    while(X);
    output.setIntValue(input.getIntValue());
    output.setDoubleValue(input.getDoubleValue());
    output.setBooleanValue(input.getBooleanValue());
    complete();
  }
  
  public void finish() {
    X = false;
  }
}
