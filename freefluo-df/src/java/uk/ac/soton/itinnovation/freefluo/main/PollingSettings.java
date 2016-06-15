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
//      Created By          :   Justin Ferris
//      Created Date        :   2005/12/06
//      Created for Project :   SIMDAT
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/12/07 14:49:19 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.main;

import java.util.*;

public class PollingSettings {
  // polling defaults
  private int maxRetries = 5;
  private int minDelay = 1000;
  private int maxDelay = 1000;
  private float exponent = 1.0f;
  
  public PollingSettings() {
  }

  public void setMaxRetries(int maxRetries) {
    this.maxRetries = maxRetries;
  }

  public int getMaxRetries() {
    return maxRetries;
  }

  public void setMinDelay(int minDelay) {
    this.minDelay = minDelay;
  }

  public int getMinDelay() {
    return minDelay;
  }

  public void setMaxDelay(int maxDelay) {
    this.maxDelay = maxDelay;
  }

  public int getMaxDelay() {
    return maxDelay;
  }

  public void setExponent(float exponent) {
    this.exponent = exponent;
  }

  public float getExponent() {
    return exponent;
  }
}