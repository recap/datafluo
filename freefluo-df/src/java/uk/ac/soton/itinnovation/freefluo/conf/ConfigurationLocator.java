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
//      Created Date        :   2005/01/07
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
import java.net.*;

import org.apache.log4j.*;

import uk.ac.soton.itinnovation.freefluo.util.clazz.*;

/**
 * Service locator for <code>EngineConfiguration</code>s
 */
public class ConfigurationLocator {
  private static Logger logger = Logger.getLogger(ConfigurationLocator.class);
  public static final String DEFAULT_CONFIGURATION = "taverna";
  private static HashMap configs = new HashMap();
  private static final String CONFIG_RESOURCE_BUNDLE = "config";
  private static final String NAME_KEY = "name";
  private static final String PARSER_KEY = "workflow.parser";
  private static final String DATA_HANDLER_KEY = "data.handler";

  /** 
   * system property key. The value associated with the key 
   * identifies the absolute path to the extensions root directory */
  private static String EXT_DIR_KEY = "freefluo.ext.dir";

  /**
   * The extensions directory in which different EngineConfigurations
   * and their dependencies are deployed.
   */
  private static File extDir;

  /**
   * String representation of the extensions directory
   */
  private static String strExtDir;

  /**
   * Whether or not the initialisation of available engine configurations
   * has occurred yet.
   */
  private static boolean isInitialised = false;

  /**
   * This method intialises the available configurations.
   * Note that inorder to initialise available configurations
   * this method requires the absolute path to the directory
   * in which the configurations are deployed (the extensions directory). 
   * The algorithm for determining the extensions directory is simple
   * and as follows.
   * <p>
   * 1. Use the value that's the result of a call to ConfigurationLocator.getExtensionsDir()
   * if this is not <code>null</code>
   * 2. Get the value from the system property identified by <code>EXT_DIR_KEY</code>
   *
   * This simple mechanism is designed to allow the workflow engine to work seamlessly in 
   * a web service container or stand alone. In the case of a web service container deployment,
   * a <code>ServletContextListener</code> calls ConfigurationLocator.setExtensionDir()
   * when the web application is initialised and before the first call to <code>getEngineConfiguration()</code>. 
   * For standalone deployment, a startup script can pass the extensions directory as a system property.
   */ 
  private static void init() {
    try {
      logger.debug("CongifurationLocator.init(...)");

      if(strExtDir == null) {
        strExtDir = System.getProperty(EXT_DIR_KEY);
      }
      logger.debug("Extensions directory is: " + strExtDir);
      extDir = new File(strExtDir);
      
      File[] contents = extDir.listFiles();

      for(int i = 0; i < contents.length; i++) {
        if(contents[i].isDirectory()) {
          addConfiguration(contents[i]);  
        }
      }
    }
    catch(Exception e) {
      logger.fatal("Fatal error loading deployed configurations.", e);
    }
  }

  public static String getExtensionsDir(String strExtDir) {
    return strExtDir;
  }

  public static void setExtensionsDir(String strExtDir) {
    ConfigurationLocator.strExtDir = strExtDir;
  }

  /**
   * Get the <code>EngineConfiguration</code> named <code>name</code>.
   * Note that initialisation of available configurations will be done
   * lazily the first time this method is called.
   */
  public static EngineConfiguration getConfiguration(String name) throws NoSuchConfigurationException {
    if(!isInitialised) {
      init();
      isInitialised = true;
    }
    
    EngineConfiguration config = (EngineConfiguration) configs.get(name);
    if(config == null) {
      throw new NoSuchConfigurationException("No configuration called " + name);
    }

    return config;
  }

  public static EngineConfiguration getDefaultConfiguration() throws NoSuchConfigurationException {
    return getConfiguration(DEFAULT_CONFIGURATION);    
  }

  private static void addConfiguration(File dir) throws Exception {
    logger.debug("Adding configuration from directory: " + dir.toString());
    ArrayList urls = new ArrayList();

    File confDir = new File(dir, "conf");
    if(confDir.isDirectory()) {
      URL url = confDir.toURI().toURL();
      urls.add(url);
    }
    
    File classesDir = new File(dir, "classes");
    if(classesDir.isDirectory()) {
      URL url = classesDir.toURI().toURL();
      urls.add(url);
    }

    File libDir = new File(dir, "lib");
    if(libDir.isDirectory()) {
      File[] jarFiles = libDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
              if(pathname.getPath().endsWith(".jar")) {
                return true;
              }
              else {
                return false;
              }
            }
          }
        );

      for(int i = 0; i < jarFiles.length; i++) {
        URL url = jarFiles[i].toURI().toURL();
        urls.add(url);
      }  
    }
    URL[] arUrls = (URL[]) urls.toArray(new URL[0]);
    
    if(logger.isDebugEnabled()) {
      logger.debug("Adding the following URLs to the ClassLoader for the configuration: ");
      for(int i = 0; i < arUrls.length; i++) {
        logger.debug(arUrls[i].toExternalForm());
      }
    }

    ReverseDelegationClassLoader classLoader = new ReverseDelegationClassLoader(arUrls, ConfigurationLocator.class.getClassLoader());
    //ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    URL urlToConfig = classLoader.getResource("config.properties");
    
    // load the config properties file
    ResourceBundle rb = ResourceBundle.getBundle(CONFIG_RESOURCE_BUNDLE, Locale.getDefault(), classLoader);
    String name = rb.getString(NAME_KEY);
    String parser = rb.getString(PARSER_KEY);
    String dataHandler = rb.getString(DATA_HANDLER_KEY);

    ConfigurationDescription configDescription = new ConfigurationDescription(name, parser, dataHandler);
    EngineConfiguration configuration = new EngineConfigurationImpl(configDescription, classLoader);

    configs.put(configuration.getName(), configuration);
    logger.debug("Added configuration: " + configuration.getName());
  }
}
