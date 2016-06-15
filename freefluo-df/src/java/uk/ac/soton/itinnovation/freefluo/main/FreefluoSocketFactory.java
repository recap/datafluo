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
//      Created Date        :   2005/01/19
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/08/23 06:24:48 $
//                              $Revision: 1.1 $
//
////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.main;

import java.util.*;
import java.io.*;
import java.net.*;

import javax.xml.namespace.*;

import org.apache.axis.*;
import org.apache.axis.client.*;
import org.apache.axis.session.*;
import org.apache.axis.components.net.*;

/**
 * This socket factory has been implemented to overcome a 
 * current shortcoming with Axis SSL support. Currently (Axis1.2RC2)
 * it doesn't seem possible in Axis to configure SSL on a per-Call
 * basis. For example we may wish for some connections to authenticate
 * a server and others to trust all servers, say because authentication
 * is being carried out by a higher level protocol than SSL. Another
 * example would be where we wish to use different client-side certificates/private keys
 * depending on the current user. Axis configuration of SSL is currently system wide
 * and can be achieved by specifying a <code>org.apache.axis.components.net.SecureSocketFactory</code>
 * implementation as a value of the <code>axis.socketSecureFactory</code> System property.
 * <p>
 * To use this class, set the <code>axis.socketSecureFactory</code> System property to have the
 * value of the fully qualified name of this class. The default behaviour then is to delegate
 * SSL configuration and SSLSocket creation to the Axis default, 
 * <code>org.apache.axis.components.net.JSSESocketFactory</code>.
 * However, before creating a Socket, the create(...) method of this class examines properties
 * in the Session associated with the current MessageContext. At present the behaviour is simply
 * to extract the value of the FACTORY_KEY key from the Session and delegate Socket 
 * creation depending on the value. A value of SUN_JSSE_FACTORY will result in the
 * axis default Socket factory being used. Whereas a value of TRUST_ALL_FACTORY will
 * result in the use of a Socket factory that creates sockets that trust all servers
 * and therefore do not require a Trust store.
 * <p>
 * Possible future enhancements for this class would be to use different client certificates
 * for client-authentication in SSL, depending on the user that's using the Call.
 * <p>
 * An example of client client code to setup Axis and SSL so that all servers are trusted follows.
 * <p>
 * <pre>
 *   System.setProperty("axis.socketSecureFactory", 
 *       "uk.ac.soton.itinnovation.freefluo.main.FreefluoSocketFactory"); 
 *
 *   Service service = new Service();
 *   Call call = (Call) service.createCall();
 *   Session session = new SimpleSession();
 *   session.set(FreefluoSocketFactory.FACTORY_KEY, FreefluoSocketFactory.TRUST_ALL_FACTORY);
 *   MessageContext ctx = call.getMessageContext();
 *   ctx.setSession(session);
 * </pre>
 */
public class FreefluoSocketFactory extends JSSESocketFactory {
  public static final String FACTORY_KEY = "FreefluoSocketFactory.factory";
  public static final String SUN_JSSE_FACTORY = "sun";
  public static final String TRUST_ALL_FACTORY = "trustall";

  private SunFakeTrustSocketFactory fakeFactory;
  private SunJSSESocketFactory sunFactory;

  public FreefluoSocketFactory(Hashtable attributes) {
    super(attributes);
    fakeFactory = new SunFakeTrustSocketFactory(attributes);
    sunFactory = new SunJSSESocketFactory(attributes);
  }

  public Socket create(java.lang.String host, int port, 
    StringBuffer otherHeaders, BooleanHolder useFullURL) throws Exception {
    MessageContext ctx = MessageContext.getCurrentContext();
    Session session = ctx.getSession();

    if(session == null) {
      return super.create(host, port, otherHeaders, useFullURL);
    }

    String factory = (String) session.get(FACTORY_KEY);

    if(factory == null) {
      return super.create(host, port, otherHeaders, useFullURL);
    }

    if(factory.equals(SUN_JSSE_FACTORY)) {
      return sunFactory.create(host, port, otherHeaders, useFullURL);
    }
    else if(factory.equals(TRUST_ALL_FACTORY)) {
      return fakeFactory.create(host, port, otherHeaders, useFullURL);
    }
    else {
      return super.create(host, port, otherHeaders, useFullURL);
    }
  }
}
