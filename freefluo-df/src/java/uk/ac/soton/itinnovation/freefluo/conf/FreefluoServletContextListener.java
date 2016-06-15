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
//      Created Date        :   2005/01/10
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

import javax.servlet.*;

public class FreefluoServletContextListener implements ServletContextListener {
  public void contextDestroyed(ServletContextEvent sce) {}
          
  public void contextInitialized(ServletContextEvent sce) {
    ServletContext ctx = sce.getServletContext();
    String realPath = ctx.getRealPath("/");
 
    if(realPath != null) {
      File root = new File(realPath);
      File exts = new File(root, "WEB-INF/exts");
      ConfigurationLocator.setExtensionsDir(exts.getAbsolutePath());
      File workflowDatabaseDir = new File(root, "WEB-INF/workflows");
      ServiceConfiguration.setWorkflowDatabaseDir(workflowDatabaseDir);

      File uploadsDir = new File(root, "WEB-INF/uploads");
      ServiceConfiguration.setUploadsDir(uploadsDir);
    }
    else {
      System.err.println("Fatal error on web application initialisation. ServletContext.getRealPath(\"/\")" +
        " returned null");
    }
  }
}
