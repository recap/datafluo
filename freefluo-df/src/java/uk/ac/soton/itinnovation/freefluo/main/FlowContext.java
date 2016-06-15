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
////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.main;

import java.io.*;
import java.util.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

import org.apache.log4j.*;

import uk.ac.soton.itinnovation.freefluo.lang.*;

/**
 * Represents a particular context for a workflow instance.
 * Arbitrary properties can be associated with
 * a context by adding name value pairs.
 * <p>
 * This class supports serialisation and 
 * deserialisation to and from XML.
 * <p>
 * Property names and values must be of type 
 * <code>java.lang.String</code>.
 */
public class FlowContext {
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";
  public static final String USER_PRINCIPAL = "user-principal";
  public static final String ISSUER_PRINCIPAL = "issuer-principal";

  private static Logger logger = Logger.getLogger(FlowContext.class);
  private HashMap properties = new HashMap();
  
  public FlowContext() {}

  /**
   * Construct a <code>FlowContext</code> from serialised
   * XML representation.
   */
  public FlowContext(String flowContextXML) throws BadlyFormedDocumentException, InvalidDocumentException {
    SAXBuilder builder = new SAXBuilder();
    Document doc = null;
    
    try {
      doc = builder.build(new StringReader(flowContextXML));
    }
    catch(JDOMException e) {
      String msg = "Error parsing flow context XML. Document not well formed.";
      logger.warn(msg, e);
      throw new BadlyFormedDocumentException(msg, e);
    }
    catch(IOException e) {
      String msg = "Error parsing flow context XML. Document not well formed.";
      logger.warn(msg, e);
      throw new BadlyFormedDocumentException(msg, e);
    }
    
    try {
      Element flowContextEl = doc.getRootElement();
      Collection propertyEls = flowContextEl.getChildren();
      for(Iterator i = propertyEls.iterator(); i.hasNext();) {
        Element propertyEl = (Element) i.next();
        Element keyEl = propertyEl.getChild("key");
        Element valueEl = propertyEl.getChild("value");

        String key = keyEl.getText();
        String value = valueEl.getText();

        properties.put(key, value);
      }
    }
    catch(Exception e) {
      String msg = "Error parsing flow context XML. Document not valid.";
      logger.warn(msg, e);
      throw new InvalidDocumentException(msg, e);
    }
  }

  public void put(String name, String value) {
    properties.put(name, value);
  }

  public void remove(String name) {
    properties.remove(name);
  }

  public String get(String name) {
    return(String) properties.get(name);
  }

  HashMap getProperties() {
    return properties;
  }

  void setProperties(HashMap properties) {
    this.properties = properties;
  }

  /**
   * Serialises to an XML document this <code>FlowContext</code> 
   */
  public String toXmlString() {
    Document doc = new Document();
    Element flowContextEl = new Element("flowContext");
    doc.setRootElement(flowContextEl);
    
    for(Iterator i = properties.keySet().iterator(); i.hasNext();) {
      String key = (String) i.next();
      String value = (String) properties.get(key);

      Element propertyEl = new Element("property");
      Element keyEl = new Element("key");
      Element valueEl = new Element("value");
      flowContextEl.addContent(propertyEl);
      propertyEl.addContent(keyEl);
      propertyEl.addContent(valueEl);

      keyEl.setText(key);
      valueEl.setText(value);
    }

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String strFlowContext = outputter.outputString(doc);
    return strFlowContext;
  }
}
