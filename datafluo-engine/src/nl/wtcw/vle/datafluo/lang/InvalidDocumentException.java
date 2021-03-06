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
//      Created Date        :   2004/12/15
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
 * Thrown when an XML document is invalid with
 * respect to its XSD schema or DTD.
 */
public class InvalidDocumentException extends ParsingException {
  private ArrayList errors = new ArrayList();

  public InvalidDocumentException(String msg, Exception e) {
    super(msg, e);
  }

  public InvalidDocumentException(String msg) {
    super(msg);
  }

  public InvalidDocumentException(String msg, ArrayList errors) {
    super(msg);
    this.errors = errors;
  }

  /**
   * @return ArrayList of String descriptions of errors encountered when parsing 
   * the XML document.
   */
  public ArrayList getErrors() {
    return errors;
  }
}
