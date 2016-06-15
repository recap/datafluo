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
//      Created Date        :   2005/01/05
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/08/23 06:24:47 $
//                              $Revision: 1.1 $
//
////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.conf;

import java.io.*;
import java.util.*;

import org.apache.log4j.*;

import uk.ac.soton.itinnovation.freefluo.main.WorkflowInstance;
import uk.ac.soton.itinnovation.freefluo.lang.WorkflowParser;
import uk.ac.soton.itinnovation.freefluo.data.DataHandler;
import uk.ac.soton.itinnovation.freefluo.util.id.IdGenerator;

public class EngineConfigurationImpl implements EngineConfiguration {
  private static Logger logger = Logger.getLogger(EngineConfigurationImpl.class);
  private ConfigurationDescription configurationDescription;
  private Class workflowParserClass;
  private DataHandler dataHandler;
  private ClassLoader classLoader;
  private IdGenerator idGenerator;

  /**
   * Construct the configuration with a description of the configuration
   * and a <code>ClassLoader</code> to use to load classes in the configuration.
   * Providing <code>EngineConfiguration</code>s with different <code>ClassLoader</code>s
   * makes possible class loader isolation of classes in the configuration (and the classes
   * they depend upon) from the classes that make up Freefluo.
   */
  public EngineConfigurationImpl(ConfigurationDescription configurationDescription, ClassLoader classLoader) throws ConfigurationException {

    this.configurationDescription = configurationDescription;
    this.classLoader = classLoader;

    init();
  }
  
  public String getName() {
    return configurationDescription.getName();
  }

  public WorkflowParser getWorkflowParser() {
    try {
      return createWorkflowParser();
    }
    catch(Exception e) {
      String msg = "Couldn't create an instance of workflow parser for configuration " +
        configurationDescription.getName() + " using parser class " +
        configurationDescription.getWorkflowParser() + ". EngineConfigurationImpl.init() " +
        " should have caught this error previously.";
      logger.fatal(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  public DataHandler getDataHandler() {
    return dataHandler;
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public IdGenerator getIdGenerator() {
    return idGenerator;
  }

  private void init() throws ConfigurationException {
    Class clazz = null;
    Object obj = null;
    
    try {
      workflowParserClass = classLoader.loadClass(configurationDescription.getWorkflowParser());
    }
    catch(ClassNotFoundException e) {
      String msg = "Error with configuration named " + configurationDescription.getName() + 
        "ClassNotFound for WorkflowParser implementation: " + configurationDescription.getWorkflowParser();
      logger.warn(msg, e);
      throw new ConfigurationException(msg, e);
    }

    // workflow parser is stateful and so we create a new instance every time
    // a request is made for an instance.
    // Here, we're just checking the validity of the class that's been specified.
    createWorkflowParser();

    try {
      clazz = classLoader.loadClass(configurationDescription.getDataHandler());
    }
    catch(ClassNotFoundException e) {
      String msg = "Error with configuration named " + configurationDescription.getName() + 
        "ClassNotFound for DataHandler implementation: " + configurationDescription.getDataHandler();
      logger.warn(msg, e);
      throw new ConfigurationException(msg, e);
    }
    
    try {
      obj = clazz.newInstance();
      dataHandler = (DataHandler) obj;
    }
    catch(Exception e) {
      String msg = "Error with configuration named " + configurationDescription.getName() + 
        "Invalid class for DataHandler implementation: " + configurationDescription.getDataHandler();
      logger.warn(msg, e);
      throw new ConfigurationException(msg, e);
    }

    // load the id generator
    try {
      clazz = classLoader.loadClass(configurationDescription.getIdGenerator());
    }
    catch(ClassNotFoundException e) {
      String msg = "Error with configuration named " + configurationDescription.getName() + 
        "ClassNotFound for IdGenerator implementation: " + configurationDescription.getIdGenerator();
      logger.warn(msg, e);
      throw new ConfigurationException(msg, e);
    }
    
    try {
      obj = clazz.newInstance();
      idGenerator = (IdGenerator) obj;
    }
    catch(Exception e) {
      String msg = "Error with configuration named " + configurationDescription.getName() + 
        "Invalid class for IdGenerator implementation: " + configurationDescription.getIdGenerator();
      logger.warn(msg, e);
      throw new ConfigurationException(msg, e);
    }
  }

  private WorkflowParser createWorkflowParser() throws ConfigurationException {
    Object obj = null;
    WorkflowParser workflowParser = null;
    
    try {
      obj = workflowParserClass.newInstance();
      workflowParser = (WorkflowParser) obj;
      return workflowParser;
    }
    catch(Exception e) {
      String msg = "Error with configuration named " + configurationDescription.getName() + 
        "Invalid class for WorkflowParser implementation: " + configurationDescription.getWorkflowParser();
      logger.warn(msg, e);
      throw new ConfigurationException(msg, e);
    }
  }
}
