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

public class UserContext {
  private static ThreadLocal userLocal = new ThreadLocal();
  private static ThreadLocal issuerLocal = new ThreadLocal();

  public static void setUser(String user) {
    userLocal.set(user);
  }

  public static String getUser() {
    return (String) userLocal.get();
  }

  public static void setIssuer(String issuer) {
    issuerLocal.set(issuer);
  }

  public static String getIssuer() {
    return (String) issuerLocal.get();
  }

  public static String getString() {
    return "User: " + userLocal.get() + "\tIssuer: " + issuerLocal.get();
  }
}