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

import org.apache.log4j.*;

import org.jdom.*;

import uk.ac.soton.itinnovation.freefluo.conf.*;
import uk.ac.soton.itinnovation.freefluo.lang.*;
import uk.ac.soton.itinnovation.freefluo.util.xml.*;

/**
 * Todo, if we really wanted to we could make access far more concurrent.
 * For example, we could have read and write locks per workflow description
 * file ho hum.
 */
public class WorkflowDeployer {
  private static Logger logger = Logger.getLogger(WorkflowDeployer.class);
  private static WorkflowDeployer instance;
  private static final String INDEX_FILE_NAME = "index.dat";
  private File dbDir;

  /** maintains a sequence for workflow ids */
  private File nextIndexFile;

  private WorkflowParser parser;

  private WorkflowDeployer() {
    dbDir = ServiceConfiguration.getWorkflowDatabaseDir();
    nextIndexFile = new File(dbDir, INDEX_FILE_NAME);
    if(!nextIndexFile.exists()) {
      try {
        RandomAccessFile raf = new RandomAccessFile(nextIndexFile, "rw");
        raf.writeInt(0);
        raf.close();
      }
      catch(IOException e) {
        logger.fatal("", e);
        throw new RuntimeException(e);
      }
    }

    EngineConfiguration config = null;
    ConfigurationDescription configDescription = new ConfigurationDescription("taverna", 
        "uk.ac.soton.itinnovation.freefluo.exts.taverna.TavernaScuflModelParser", 
        "uk.ac.soton.itinnovation.freefluo.exts.taverna.TavernaDataHandler"); 
    try {
      config = new EngineConfigurationImpl(configDescription, getClass().getClassLoader());
    }
    catch(Exception e) {
      logger.fatal("Serious error configuring engine to get parser", e);
      throw new RuntimeException("Serious error configuring engine to get parser", e);
    }

    parser = config.getWorkflowParser();
  }

  public synchronized static WorkflowDeployer getInstance() {
    if(instance == null) {
      instance = new WorkflowDeployer();
    }

    return instance;
  }

  public synchronized WorkflowDescription deploy(String workflow) throws ParsingException {
    WorkflowDescription description = null;
    
    try {
      description = parser.describe(workflow);
    }
    catch(ParsingException e) {
      logger.error("Error parsing workflow", e);
      throw e;
    }
    
    // parse the workflow to generate a model for inspection
    int id = nextWorkflowId();
    description.setId("" + id);

    // save the workflow & workflow description
    try {
      File workflowFile = new File(dbDir, "_" + id + "-workflow");
      BufferedWriter bw = new BufferedWriter(new FileWriter(workflowFile));
      bw.write(workflow);
      bw.close();

      // save the workflow description
      File descFile = new File(dbDir, "_" + id + "-description");
      bw = new BufferedWriter(new FileWriter(descFile));
      String strDesc = description.toElementString();
      bw.write(strDesc);
      bw.close();
    }
    catch(Exception e) {
      String msg = "Error saving files when deploying workflow";
      logger.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }

    return description;
  }

  public synchronized void undeployAll() {
    Collection descriptions = getDeployedWorkflows();

    for(Iterator i = descriptions.iterator(); i.hasNext();) {
      WorkflowDescription wd = (WorkflowDescription) i.next();
      undeploy(wd.getId());
    }
  }

  public synchronized void undeploy(String id) {
    File workflowFile = new File(dbDir, "_" + id + "-workflow");
    File descFile = new File(dbDir, "_" + id + "-description");

    workflowFile.delete();
    descFile.delete();
  }

  /**
   * Returns a collection of WorkflowDesription
   */
  public synchronized Collection getDeployedWorkflows() {
    Collection deployedWorkflows = new ArrayList();

    FileFilter filter = new FileFilter() {
      public boolean accept(File file) {
        if(!file.isFile()) {
          return false;
        }

        if(!file.getName().endsWith("-description")) {
          return false;
        }

        return true;
      }
    };

    File[] files = dbDir.listFiles(filter);
    
    for(int i = 0; i < files.length; i++) {
      try {
        Document doc = XmlUtils.jdomDocumentFromFile(files[i]);
        WorkflowDescription description = new WorkflowDescription(doc.getRootElement());
        deployedWorkflows.add(description);
      }
      catch(Exception e) {
        throw new RuntimeException(e);
      }
    }

    return deployedWorkflows;
  }

  public synchronized String getWorkflowDefinition(String workflowDefinitionId) 
          throws UnknownWorkflowDefinitionException  {
    File workflowFile = new File(dbDir, "_" + workflowDefinitionId + "-workflow");
    if(!workflowFile.isFile()) {
      throw new UnknownWorkflowDefinitionException("No such workflow definition with id " + 
                                                   workflowDefinitionId);
    }

    StringBuffer workflowDefinition = new StringBuffer();
 
    try {
      BufferedReader br = new BufferedReader(new FileReader(workflowFile));
      String s = null;
      while((s = br.readLine()) != null) {
        workflowDefinition.append(s);
      }
      br.close();
    }
    catch(IOException e) {
      String msg = "Problem reading workflow definition file";
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
    }

    return workflowDefinition.toString();
  }

  private synchronized int nextWorkflowId() {
    int index = -1;

    try {
      RandomAccessFile raf = new RandomAccessFile(nextIndexFile, "rw");
      index = raf.readInt();
      raf.seek(0);
      raf.writeInt(index + 1);
      raf.close();
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }

    return index;
  }
}