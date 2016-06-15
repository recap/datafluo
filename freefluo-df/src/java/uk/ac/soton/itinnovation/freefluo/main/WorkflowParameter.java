/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2005
//
// Copyright in this software belongs to the IT Innovation Centre of
// 2 Venture Road, Chilworth Science Park, Southampton SO16 7NP, UK.
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//	Created By :          Justin Ferris
//	Created Date :        2005/08/08
//	Created for Project : Simdat
//
/////////////////////////////////////////////////////////////////////////
//
//	Dependencies : none
//
/////////////////////////////////////////////////////////////////////////
//
//	Last commit info:	$Author: ferris $
//                    $Date: 2005/08/23 06:24:48 $
//                    $Revision: 1.1 $
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.main;

import java.io.*;
import java.util.*;

import org.jdom.*;

/**
 * Description of a workflow input or output paramater
 */
public class WorkflowParameter {
  private String name;
  private String syntacticType;
  private List mimeTypes = new ArrayList();

  public WorkflowParameter(String name, String syntacticType) {
    this.name = name;
    this.syntacticType = syntacticType;
  }

  public WorkflowParameter(Element el) {
    name = el.getAttributeValue("name");
    syntacticType = el.getAttributeValue("type");

    Element mimeTypesEl = el.getChild("mime-types");
    List types = mimeTypesEl.getChildren();
    
    for(Iterator i = types.iterator(); i.hasNext();) {
      Element typeEl = (Element) i.next();
      addMimeType(typeEl.getText().trim());
    }
  }

  public String getName() {
    return name;
  }

  public String getSyntacticType() {
    return syntacticType;
  }

  public List getMimeTypes() {
    return mimeTypes;
  }

  public void addMimeType(String mimeType) {
    if(!mimeTypes.contains(mimeType)) {
      mimeTypes.add(mimeType);
    }
  }

  public boolean removeMimeType(String mimeType) {
    return mimeTypes.remove(mimeType);
  }

  public Element toElement() {
    Element e = new Element("param");
    e.setAttribute("name", name);
    e.setAttribute("type", syntacticType);

    Element mimeTypesEl = new Element("mime-types");
    for(Iterator i = mimeTypes.iterator(); i.hasNext();) {
      String mimeType = (String) i.next();
      Element type = new Element("type");
      type.addContent(mimeType);
      mimeTypesEl.addContent(type); 
    }
    e.addContent(mimeTypesEl);

    return e;
  }
}