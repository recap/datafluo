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
//      Created Date        :   2003/09/29
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/08/23 06:24:48 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.lang.scufl;

import org.jdom.Namespace;

public class ScuflConstants {
  public static final String SCUFL_INTERNAL_SOURCE_PROCESSOR_NAME = "SCUFL_INTERNAL_SOURCEPORTS";
  public static final String SCUFL_INTERNAL_SINK_PROCESSOR_NAME = "SCUFL_INTERNAL_SINKPORTS";
  public static final String SUPPORTED_SCUFL_VERSION = "0.1";
  public static final String SCUFL_NS_URI = "http://org.embl.ebi.escience/xscufl/0.1alpha";
  public static final String VERSION_ATTRIBUTE = "version";
  public static final Namespace SCUFL_NAMESPACE = Namespace.getNamespace(SCUFL_NS_URI);
}
