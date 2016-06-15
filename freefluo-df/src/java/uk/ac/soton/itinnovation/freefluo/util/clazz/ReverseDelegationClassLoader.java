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
//                              $Date: 2005/08/23 06:24:48 $
//                              $Revision: 1.1 $
//
////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.util.clazz;

import java.io.*;
import java.util.*;
import java.net.*;

import org.apache.log4j.*;

/**
 * A <code>ReverseDelegationClassLoader</code> loads classes first from
 * a particular set of directories and JAR files , and
 * secondly from it's parent <code>ClassLoader</code>.
 * Thus, a <code>ReverseDelegationClassLoader</code> doesn't 
 * follow the J2SE delegation pattern of first delegating
 * to it's parent.
 * <p>
 * The JAR files and directories from which classes are loaded are identified 
 * by an array of <code>URL</code>s, with which an instance of this class
 * is constructed.
 */
public class ReverseDelegationClassLoader extends URLClassLoader {
  private ClassLoader parent;
  private static Logger logger = Logger.getLogger(ReverseDelegationClassLoader.class);

  /**
   * Create a class loader that loads classes and resources
   * first from the list of <code>urls</code> and next 
   * by delegation to the <code>ClassLoader</code> provided
   * as the <code>parent</code> argument.
   */
  public ReverseDelegationClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);

    this.parent = parent;
  }

  public URL getResource(String name) {
    URL url = findResource(name);
    
    if(url == null && parent != null) {
      url = parent.getResource(name);
    }

    return url;
  }

  protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
    logger.debug("Trying to load class: " + name);
    Class c = findLoadedClass(name);
    if(c == null) {
      try {
        c = findClass(name);
      }
      catch(ClassNotFoundException e) {}
    } 
     
    if(c == null && parent != null) {
      c = parent.loadClass(name);
    }

    if(c == null) {
      throw new ClassNotFoundException(name);
    }

    if(resolve) {
      resolveClass(c);
    }
    return c;
  }
}
