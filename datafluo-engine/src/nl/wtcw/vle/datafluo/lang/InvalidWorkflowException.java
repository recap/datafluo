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
//      Created By          :   jf
//      Created Date        :   2002/04/30
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/08/23 06:24:48 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////
package nl.wtcw.vle.datafluo.lang;

import java.io.*;
import java.util.*;

/**
 * Thrown when a workflow specification is invalid with
 * respect to its XSD schema or DTD; or other constraints
 * that determine if the workflow specification is executable.
 */
public class InvalidWorkflowException extends InvalidDocumentException {
  public InvalidWorkflowException(String msg, Exception e) {
    super(msg, e);
  }

  public InvalidWorkflowException(String msg) {
    super(msg);
  }

  public InvalidWorkflowException(String msg, ArrayList errors) {
    super(msg, errors);
  }
}
