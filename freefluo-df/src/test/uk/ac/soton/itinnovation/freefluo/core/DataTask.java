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

public class DataTask extends BaseTask {
  //private static Logger logger = Logger.getLogger(DataTask.class);
  private boolean booleanValue = false;
  private int intValue = 0;
  private double doubleValue = 0;

  public DataTask(String name, Flow flow) {
    super(name, flow);
  }

  public int getIntValue() {
    return intValue;
  }

  public double getDoubleValue() {
    return doubleValue;
  }

  public void setIntValue(int i) {
    intValue = i;
  }

  public void setDoubleValue(double i) {
    doubleValue = i;
  }

  public boolean getBooleanValue() {
    return booleanValue;
  }

  public void setBooleanValue(boolean b) {
    booleanValue = b;
  }

  protected void execute() {   
    complete();
  }
}
