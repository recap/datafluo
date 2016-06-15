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
//      Created Date        :   2004/07/29
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
import java.net.*;

import org.apache.log4j.*;

import org.apache.axis.transport.http.*;
import org.apache.axis.client.*;
import org.apache.axis.utils.*;

/**
 * A simple HttpServer
 */
public class HttpServer {
  private static Logger logger = Logger.getLogger(HttpServer.class);
  private static SimpleAxisServer axisServer = null;
  
  /** The path independent location of the axis deployment descriptor for freefluo */
  private static final String WSDD_RESOURCE = "conf/deploy.wsdd";

  private static int listenPort;
  private static int stopPort;

  /**
   * Main method to start the simple HTTP server.
   * Two arguments should be passed in <code>args</code>.
   * The first is the port to listen on for HTTP/SOAP
   * requests. The second is the port to listen on for 
   * a signal to shut down the server. The server is 
   * shutdown by simply connecting to this port
   * and then immediately closing the connection.
   */
  public static void main(String[] args) {   
    try {
      if(args.length == 1) {
        shutdownServerOnPort(Integer.parseInt(args[0]));
      }
      else if(args.length == 2) {
        listenPort = Integer.parseInt(args[0]);
        stopPort = Integer.parseInt(args[1]);
        startUp();
      }
      else {
        printUsage();
        System.exit(1);
      }
    }
    catch(Exception e) {
      String msg = "Serious error in HttpServer.";
      logger.fatal(msg, e);
      e.printStackTrace();
    }
  }

  private static void startUp() throws Exception {
    // start axis server for listening for HTTP/SOAP
    // requests
    ServerSocket serverSocket = new ServerSocket(listenPort);
    axisServer = new SimpleAxisServer();
    axisServer.setServerSocket(serverSocket);
    axisServer.start();

    // deploy freefluo to the axis server
    AdminClient adminClient = new AdminClient();
    Options opts = new Options(new String[] {"-l http://localhost:" + listenPort + "/axis/servlet/AxisServlet"}
      );
    InputStream wsdd = HttpServer.class.getClassLoader().getResourceAsStream(WSDD_RESOURCE);

    adminClient.process(opts, wsdd);

    // bind and listen on a server socket for a shutdown command
    final ServerSocket stopSocket = new ServerSocket(stopPort);

    Thread stopThread = new Thread() {
        public void run() {
          try {        
            Socket remoteSocket = stopSocket.accept();

            InputStream sockStream = remoteSocket.getInputStream();
            while(sockStream.read() != -1) {}
            // clean up
            axisServer.stop();

            sockStream.close();
            remoteSocket.close();
            stopSocket.close();
          }
          catch(Exception e) {
            logger.error("Error in shutdown thread", e);
            throw new RuntimeException("Error in shutdown thread", e);
          }
        }
      };

    stopThread.start();
  }

  private static void shutdownServerOnPort(int stopPort) throws Exception {
    Socket sock = new Socket(InetAddress.getLocalHost(), stopPort);
    sock.close();
  }

  private static void printUsage() {
    System.out.println();
    System.out.println("Usage: HttpServer [listenPort] stopPort");
    System.out.println();
    System.out.println("If both a listenPort and stopPort are specified, a simple HTTP Server that " +
      "supports the Freefluo web application is started. The server listens for SOAP/HTTP " +
      "requests on listenPort. Connecting to stopPort and closing the connection signals " +
      "for the server to shutdown.");
    System.out.println();
    System.out.println("In contrast, if only stopPort is specified, an attempt is made to shutdown an HttpServer " +
      "that was started previously and told to listen for stop signal on stopPort.");
    System.out.println();
    System.out.println("  listenPort\tThe port on which the server should listen for HTTP requests");
    System.out.println("  stopPort\tThe shutdown port. Opening and closing a connection to this port shuts down the server.");
    System.out.println();
  }
}
