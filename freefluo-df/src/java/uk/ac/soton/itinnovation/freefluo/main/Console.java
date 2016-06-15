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
//      Created Date        :   2004/03/24
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/08/23 06:24:48 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.main;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.commons.cli.*;

import org.jdom.*;
import org.jdom.output.*;

import uk.ac.soton.itinnovation.freefluo.conf.*;
import uk.ac.soton.itinnovation.freefluo.util.*;
import uk.ac.soton.itinnovation.freefluo.util.xml.*;
import uk.ac.soton.itinnovation.freefluo.event.*;
import uk.ac.soton.itinnovation.freefluo.data.*;

/**
 * Command line interface to Freefluo
 */
public class Console implements WorkflowStateListener {
  private static Logger logger = Logger.getLogger(Console.class);  
  private String flowId = null;
  private Engine engine = null;
  private Object completionLock = new Object();
  private WorkflowState workflowState = null;
  private boolean isQuietOnErrors = false;
  private String inputFilename;
  private String outputFilename;
  private String workflowFile;

  private static PrintStream out;
  private static PrintStream err;

  public static void main(String[] args) throws Exception {
    redirectStandardStreams();

    Console console = new Console();
    console.run(args);
  }

  private Console() {
    EngineConfiguration config = null;
    ConfigurationDescription configDescription = new ConfigurationDescription("taverna", 
        "uk.ac.soton.itinnovation.freefluo.exts.taverna.TavernaScuflModelParser", 
        "uk.ac.soton.itinnovation.freefluo.exts.taverna.TavernaDataHandler"); 
    try {
      config = new EngineConfigurationImpl(configDescription, getClass().getClassLoader());
    }
    catch(Exception e) {
      logger.error("Serious error configuring engine", e);
      throw new RuntimeException("Serious error configuring engine", e);
    }
    
    engine = new EngineImpl(config);
  }

  public void workflowStateChanged(WorkflowStateChangedEvent event) {
    synchronized(completionLock) {
      workflowState = event.getWorkflowState();
      if(workflowState.isFinal()) {
        completionLock.notifyAll();
      }
    }
  }

  private void run(String[] args) throws Exception {
    Options options = new Options();
    Option option = null;

    option = new Option("h", false, "print this message");
    option.setLongOpt("help");
    options.addOption(option);

    option = new Option("v", false, "output version information and exit");
    option.setLongOpt("version");
    options.addOption(option); 

    option = new Option("q", false, "be quiet on errors");
    option.setLongOpt("quiet");
    options.addOption(option);

    option = new Option("s", false, "print output summary to the console");
    option.setLongOpt("summary");
    options.addOption(option);

    option = OptionBuilder.withArgName("OUTPUTFILE").hasArg().withDescription("save results to OUTPUTFILE").create("o");
    options.addOption(option);

    option = OptionBuilder.withArgName("INPUTFILE").hasArg().withDescription("read inputs from INPUTFILE").create("i");
    options.addOption(option);
    
    CommandLineParser parser = new BasicParser();
    CommandLine commandLine = null;
    
    try {
      commandLine = parser.parse(options, args);
    }
    catch(ParseException e) {
      printUsage();
      return;
    }

    try {
      if(commandLine.hasOption("q")) {
        isQuietOnErrors = true;
      }

      if(commandLine.hasOption("h")) {
        printUsage();
        return;
      }
      
      if(commandLine.hasOption("v")) {
        printVersion();
        return;
      }

      inputFilename = commandLine.getOptionValue("i");
      outputFilename = commandLine.getOptionValue("o");
      
      List argList = commandLine.getArgList();

      if(argList.size() != 1) {
        printUsage();
        return;
      }

      workflowFile = (String) argList.get(0);

      // run workflow and block until it terminates
      runWorkflowAndWait();

      if(workflowState.equals(WorkflowState.FAILED)) {
        throw new RuntimeException("FAILED. details: " + engine.getErrorMessage(flowId));
      }
      
      // output results
      DataHandler dataHandler = engine.getEngineConfiguration().getDataHandler();
      Map outputMap = engine.getOutput(flowId);
      Document outputDocument = dataHandler.getDataDocument(outputMap); 
      
      if(outputFilename != null) {
        FileOutputStream fos = new FileOutputStream(outputFilename);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(outputDocument, bos);
        bos.close();
      }

      if(commandLine.hasOption("s")) {
        out.println("Status: " + workflowState);
        out.println("Results:");
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(outputDocument, out);

      }
    }
    catch(Exception e) {
      resetStandardStreams();

      if(!isQuietOnErrors) {
        throw e;
      }
      else {
        System.err.println(e.getMessage());
      }
    }
  }

  private void printUsage() {
    out.println("Usage: freefluo [OPTION]... [WORKFLOW]");
    out.println("Run Scufl WORKFLOW file");
    out.println();
    out.println("  -h, --help\tprint this message");
    out.println("  -v, --version\toutput version information and exit");
    out.println("  -q, --quite\tbe quite on errors");
    out.println("  -s, --summary\tprint output summary to the console");
    out.println("  -i INPUTFILE\tread inputs from INPUTFILE");
    out.println("  -o OUTPUTFILE\tsave results to OUTPUTFILE");
    out.println();
  }

  private void printVersion() {
    out.println("Freefluo version " + Version.getVersion());
  }

  private void runWorkflowAndWait() throws Exception {
    String workflow = getFileAsString(workflowFile);

    flowId = engine.compile(workflow);
    engine.addWorkflowStateListener(flowId, this);
  
    if(inputFilename != null) {
      String input = getFileAsString(inputFilename);
      Document inputDocument = XmlUtils.jdomDocumentFromString(input);
      DataHandler dataHandler = engine.getEngineConfiguration().getDataHandler();
      Map inputMap = dataHandler.parseDataDocument(inputDocument);
      engine.setInput(flowId, inputMap);
    }

    synchronized(completionLock) {
      engine.run(flowId);
      completionLock.wait();
    }  
  }

  private String getFileAsString(String fileName) throws IOException {
    String line = null;
    File file = new File(fileName);

    if(!file.exists() || !file.isFile()) {
      throw new IOException("No such file: " + fileName);
    }

    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    StringWriter sw = new StringWriter();

    while((line = br.readLine()) != null) {
      sw.write(line);
    }

    br.close();
    return sw.toString();
  }

  private static void redirectStandardStreams() throws Exception {
    out = System.out;
    err = System.err;

    PrintStream outPs = new PrintStream(new FileOutputStream(new File("log", "std.out.log")), true);
    System.setOut(outPs);

    PrintStream errPs = new PrintStream(new FileOutputStream(new File("log", "std.err.log")), true);
    System.setErr(errPs);
  }

  private static void resetStandardStreams() throws Exception {
    System.setOut(out);
    System.setErr(err);
  }
}
