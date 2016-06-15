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
import org.jdom.output.*;

public class WorkflowDescription {
  private String id;
  private String name;
  private List inputs = new ArrayList();
  private List outputs = new ArrayList();

  public WorkflowDescription(Element el) {
    id = el.getAttributeValue("id");
    name = el.getAttributeValue("name");

    Element inputsEl = el.getChild("inputs");
    List children = inputsEl.getChildren();

    for(Iterator i = children.iterator(); i.hasNext();) {
      Element paramEl = (Element) i.next();
      WorkflowParameter wp = new WorkflowParameter(paramEl);
      addInput(wp);
    }

    Element outputsEl = el.getChild("outputs");
    children = outputsEl.getChildren();

    for(Iterator i = children.iterator(); i.hasNext();) {
      Element paramEl = (Element) i.next();
      WorkflowParameter wp = new WorkflowParameter(paramEl);
      addOutput(wp);
    }
  }

  public WorkflowDescription(String name) {
    this.name = name;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void addInput(WorkflowParameter input) {
    inputs.add(input);
  }

  public List getInputs() {
    return inputs;
  }

  public void addOutput(WorkflowParameter output) {
    outputs.add(output);
  }

  public List getOutputs() {
    return outputs;
  }

  public Element toElement() {
    Element e = new Element("workflow");
    e.setAttribute("id", id);
    e.setAttribute("name", name);
    
    Element inputsEl = new Element("inputs");
    e.addContent(inputsEl);
    for(Iterator i = inputs.iterator(); i.hasNext();) {
      WorkflowParameter wp = (WorkflowParameter) i.next();
      Element wpe = wp.toElement();
      inputsEl.addContent(wpe);
    }

    Element outputsEl = new Element("outputs");
    e.addContent(outputsEl);
    for(Iterator i = outputs.iterator(); i.hasNext();) {
      WorkflowParameter wp = (WorkflowParameter) i.next();
      Element wpe = wp.toElement();
      outputsEl.addContent(wpe);
    }

    return e;
  }

  public String toElementString() {
    Element el = toElement();
    StringWriter writer = null;

    try {
      writer = new StringWriter();
      XMLOutputter outputter = new XMLOutputter();
      outputter.output(el, writer);
    }
    catch(Exception e) {
      throw new RuntimeException(e);
    }

    return writer.toString();
  }
}