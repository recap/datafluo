/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vlam.datafluo.submission;

/**
 * @author Terry Fleury (tfleury@ncsa.uiuc.edu)
 * @version 1.0 2006-05-18
 *
 * This is a simple client demonstrating how to delegate a credential to a
 * web service using a MyProxy server.
 *
 * To compile and run this program you will need:
 *     Java 1.4.2 or higher (java.sun.com/javase/downloads)
 *     Axis 1.2.1 or higher (ws.apache.org/axis)
 *     GT4 Java WS Core 4.0.1 or higher (www.globus.org/toolkit/downloads)
 *
 * To compile, you will need the following jars in your CLASSPATH:
 *    axis.jar               - Axis
 *    cog-jglobus.jar        - GT4 Java WS Core
 *    jaxrpc.jar             - Axis
 *    log4j.jar              - Axis
 *
 * To run, you will need these ADDITIONAL jars in your CLASSPATH:
 *    commons-discovery.jar  - Axis
 *    commons-logging.jar    - Axis
 *    cryptix32.jar          - GT4 Java WS Core
 *    cryptix-asn1.jar       - GT4 Java WS Core
 *    puretls.jar            - GT4 Java WS Core
 *    saaj.jar               - Axis
 *
 * Run "java MyProxyDelegator -?" for a complete description of the program.
 */



// In standard Java
import java.io.File;
import java.io.FileInputStream;
import java.util.Random;
import java.util.Hashtable;
import java.security.Security;
import org.ietf.jgss.GSSCredential;

// In Axis jaxrpc.jar

// In axis.jar
import org.apache.axis.client.Call;
import org.apache.axis.utils.Options;

// In cog-jglobus.jar
import org.globus.common.CoGProperties;
import org.globus.myproxy.MyProxy;
import org.gridforum.jgss.ExtendedGSSManager;
import org.gridforum.jgss.ExtendedGSSCredential;

// In log4j.jar
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

/**
 * This class demonstrates a sample client for delegating a credential using
 * a MyProxy server.  This program uses a MyProxy server to delegate a
 * credential from a Java client to a web service.  The credential can be
 * read from a local file or fetched from a MyProxy server.  The credential
 * is then delegated to the (same) MyProxy server using a temporary username
 * and passphrase, plus a short lifetime.  The username/passphrase is then
 * sent to a web service which can use this information to get the delegated
 * credential.  At this point, the web service can use the delegated
 * credential to act on behalf of this Java client (to do a GridFTP command,
 * for example).
 */

public class MyProxyDelegator {

    // Declare some constants
    private static final String MYPROXYHOST  = "ve.nikhef.nl";
    private static final int    MYPROXYPORT  = 7512;
    private static final int    MYPROXYSHORT = 300;      // 5 minutes
    private static final int    MYPROXYLONG  = 14400;    // 4 hours

    static Random rn = new Random();        // Generate random strings
    static Logger logger = Logger.getLogger(MyProxyDelegator.class);
    static CoGProperties cogprops = CoGProperties.getDefault();

    // Variables to hold options specified on command line
    static String  myProxyHost = "";        // MyProxy host:port for both
    static int     myProxyPort = 0;         //     getting and putting creds
    static boolean myProxyGetCred = false;  // Get a cred from MyProxy server
    static String  myProxyUsername = "";    // Username & password to get a
    static String  myProxyPassword = "";    //     cred from MyProxy server
    static String  credFileName = "";       // Get a cred from local file


    /**
     * Main application to demonstrate delegation of a credential using a
     * MyProxy server.  We first read any arguments from the command line,
     * using default values where required.  Then we set up a connection
     * from this Java client to an Axis based web service (e.g. running
     * within Tomcat).  We get a credential, either from local file or from
     * a MyProxy server.  We generate a random username/passphrase and put
     * the credential back to the MyProxy server with this info.  Using the
     * connection we set up eariler, we notify the web service that we have
     * stored a credential.  The web service then gets this delegated
     * credential and responds with a success/failure message.  The client
     * then prints out this message.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {

        // Set up the logger for outputting 'info' messages
        logger.setLevel((Level)Level.INFO);

        // Set up SSL support for URLs (i.e. https)
        System.setProperty("java.protocol.handler.pkgs",
                           "com.sun.net.ssl.internal.www.protocol");
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

        // Read command line options and arguments
        getCommandLineOptions(args);

        // Get a credential, either from file or from a MyProxy server
        GSSCredential gssCred = null;
        if (myProxyGetCred) {  // Get cred from a MyProxy server
            gssCred = getCredFromMyProxy(myProxyHost,myProxyPort,
                                         myProxyUsername,myProxyPassword);
        } else {  // Read cred from local file
            gssCred = getCredFromFile(credFileName);
        }

        // If we successfully got a credential, then put it to MyProxy server
        if (gssCred != null) {

            Call call = null;

            try {
                logger.info("Got Credential with DN = " + gssCred.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            /* Store the GSSCredential we fetched earlier to a      *
             * MyProxy server.  The returned Hashtable ht contains  *
             * the host:port of the MyProxy server used, the random *
             * username and passphrase used, and the lifetime       *
             * specified for the newly stored credential.  This     *
             * information is then sent to the web service which    *
             * can fetch the short lived credential for it's own    *
             * use, thus allowing the web service to act on our     *
             * behalf.                                              */

            Hashtable ht = putCredToMyProxy(gssCred);

        }
    }


    /**
     * Scan the command line for arguments and option flags.  This scans the
     * command line for any flags which can specify how the credential is
     * read in (from local file or from MyProxy server).  It also assumes that
     * the first non-flag command line argument is the URL of the web service.
     * Note that all of the command line arguments are set to global variables
     * to make the code easy.  Yeah, I'm lazy.
     * NOTE: If you specify both a username and password on the command
     * line, then it is assumed that you want to get the initial credential
     * from a MyProxy server, and any credential file name will be ignored.
     * @param args The command line arguments of the 'java' command.
     */
    public static void getCommandLineOptions(String [] args) {

        String tmp;

        try {
            Options options = new Options(args);
            if (options.isFlagSet('?') > 0) { // Print out help message
                printHelpMessage();
                System.exit(0);
            }
            tmp = options.isValueSet('h');    // MyProxy host name
            if (tmp != null) {
                myProxyHost = tmp;
            }
            tmp = options.isValueSet('p');    // MyProxy port number
            if (tmp != null) {
                myProxyPort = Integer.valueOf(tmp).intValue();
            }
            tmp = options.isValueSet('u');    // MyProxy user name
            if (tmp != null) {                //     for getting a cred
                myProxyUsername = tmp;
            }
            tmp = options.isValueSet('w');    // MyProxy user password
            if (tmp != null) {                //     for getting a cred
                myProxyPassword = tmp;
            }
            tmp = options.isValueSet('n');    // Local filename to load cred
            if (tmp != null) {
                credFileName = tmp;
            }
            
            // If both username and password were specified on the command
            // line, then get a credential from the MyProxy server.  In this
            // case, we ignore any filename specified with the "-n" option.
            if ((myProxyUsername.length() > 0)
                    && (myProxyPassword.length() > 0)) {
                myProxyGetCred = true;
                if (myProxyHost.length() == 0)
                    myProxyHost = MYPROXYHOST;
                if (myProxyPort == 0)
                    myProxyPort = MYPROXYPORT;
            } else { // Otherwise, read the credential from local file on disk
                if (credFileName.length() == 0)
                    credFileName = cogprops.getProxyFile();
            }

            // Report how we plan on getting the initial credential.
            if (myProxyGetCred) {
                logger.info("Getting credential from MyProxy Server.");
                logger.info("MyProxy host = " + myProxyHost);
                logger.info("MyProxy port = " + myProxyPort);
                logger.info("MyProxy user = " + myProxyUsername);
                logger.info("MyProxy pass = " + myProxyPassword);
            } else {
                logger.info("Getting credential from Local File.");
                logger.info("Local credential file name = " + credFileName);
            }
        } catch (Exception e) {
            logger.error("Caught exception " + e.getMessage());
        }
    }


    /**
     * Print out a help message.  This prints out detailed information on the
     * command line arguments and options for this sample program.
     */
    public static void printHelpMessage() {

        System.out.println("\n"+
"Usage: java MyProxyDelegator [options] [serviceURL]\n"+
"\n"+
"This program demonstrates how to delegate a credential from a Java client\n"+
"to a web service using a MyProxy server.\n"+
"\n"+
"A credential will be read from a default local file unless overridden\n"+
"by command line options. The default proxy credential file corresponds\n"+
"to the standard Globus file name "+cogprops.getProxyFile()+". You can\n"+
"specify an alternate file name OR you can specify options to get a\n"+
"credential from a MyProxy server. If you want to get the credential from\n"+
"a MyProxy server, you MUST specify at least the username and password\n"+
"for the credential. Default values will be used for the MyProxy server\n"+
"and port if you do not specify them. The local credential file will NOT\n"+
"be read if you specify username/password for getting the credential from\n"+
"a MyProxy server.\n"+
"\n"+
"Available [options] are:\n"+
"    -n   The name of the local proxy credential file. If you do NOT\n"+
"         use this option AND also do NOT specify the following two\n"+
"         MyProxy options, the default local proxy file name will be\n"+
"         used, i.e. " + cogprops.getProxyFile() + ".\n"+
"    -u   The username to use when fetching a credential from the MyProxy\n"+
"         server. To get a credential from a MyProxy server, you MUST\n"+
"         MUST specify at least this and the password.\n"+
"    -w   The password for the MyProxy username. To get a credential from a\n"+
"         MyProxy server, you MUST specify at least the username and this.\n"+
"    -h   The host name for the MyProxy server. This MyProxy server is\n"+
"         used for both getting the initial credential (if the username\n"+
"         and password above are specified) and for delegating the\n"+
"         credential to the web service. If not specified, it defaults to\n"+
"         " + MYPROXYHOST + ".\n"+
"    -p   The port number for the MyProxy server. If not specified, it\n"+
"         defaults to " + MYPROXYPORT + ".\n"+
"    -?   Print this help text and exit.");
    }


    /**
     * Read in a credential from a file.  If you do not specify a filename
     * filename (i.e. pass in null), the default Globus filename will be
     * used (e.g. "/tmp/x509up_u_msmith").
     * @param filename The name of the file from which to read the proxy
     *                 credential.  If null, use a default Globus filename.
     * @return A GSS credential read from file if successfully read in, or
     *         null if not.
     */
    public static GSSCredential getCredFromFile(String filename) {

        GSSCredential retcred = null;

        if (filename.length() == 0) {
            filename = cogprops.getProxyFile();
            logger.warn("No proxy file specified. " +
                        "Reading proxy from '" + filename + "'");
        }

        try {
            File inFile = new File(filename);
            byte [] data = new byte[(int)inFile.length()];
            FileInputStream inStream = new FileInputStream(inFile);
            inStream.read(data);
            inStream.close();
            ExtendedGSSManager manager =
                (ExtendedGSSManager)ExtendedGSSManager.getInstance();
            retcred = manager.createCredential(data,
                          ExtendedGSSCredential.IMPEXP_OPAQUE,
                          GSSCredential.DEFAULT_LIFETIME,
                          null, // use default mechanism - GSI
                          GSSCredential.INITIATE_AND_ACCEPT);
        } catch (Exception e) {
            logger.error("Could not read proxy from '" +
                         filename + "' because " + e.getMessage());
        }

        return retcred;
    }


    /**
     * Fetch a credential from a MyProxy server.  This is really just
     * a wrapper for MyProxy's old static 'get' method which is now
     * deprecated. If host or port are not specified (i.e. null or 0), then
     * default values will be used.
     * @param username The username for the credential.
     * @param password The password corresponding to the username.
     * @param host The FQDN of the MyProxy host.
     * @param port The MyProxy port.
     * @return A GSS credential for the proxy credential from the MyProxy
     *         server.
     */
    public static GSSCredential getCredFromMyProxy(
                  String host, int port, String username, String password) {

        GSSCredential retcred = null;

        if (host.length() == 0) {
            host = MYPROXYHOST;
            logger.warn("No MyProxy host specified. Using '" + host + "'");
        }
        if (port == 0) {
            port = MYPROXYPORT;
            logger.warn("No MyProxy port specified. Using '" + port + "'");
        }

        try {
            MyProxy myProxyServer = new MyProxy(host,port);
            retcred = myProxyServer.get(username,password,MYPROXYLONG);
        } catch (Exception e) {
            logger.error("Could not get proxy from '" +
                         host + ":" + port + "' because "+
                         e.getMessage());
        }

        return retcred;
    }


    /**
     * Delegate a GSSCredential to a MyProxy server.  This method puts a
     * GSSCredential to a specified MyProxy server:port using a given
     * username/passphrase with a specified lifetime (in seconds).  If any
     * of the parameters are null/0/empty, then a default value will be used
     * for that parameter.  Since you will probably want to know what
     * values were actually used in this case, a Hashtable is returned with
     * the keys being the names of the parameters ("host", "port",
     * "username", "passphrase", and "lifetime") and the values being the
     * actual strings/ints used.  This is particularly useful when you allow
     * for default username/passphrase since they are random strings which
     * you will need to give to the web service (so it can access the
     * credential).
     * @param host The MyProxy host name string for storing the credential.
     * @param port The MyProxy port number for storing the credential.
     * @param credential A GSSCredential to be stored in a MyProxy server.
     * @param username A username string for storing the credential.  If not
     *                 specified, then a random username is generated using
     *                 the DN of the credential plus some additional random
     *                 characters.
     * @param passphrase A passphrase string for storing the credential.  If
     *                   not specified, then a random passphrase of 16
     *                   characters is generated.
     * @param lifetime A (hopefully) short lifetime (in seconds) for the
     *                 credential.
     * @return A Hashtable containing the actual values used for "server",
     *         "port", "username", "passphrase", and "lifetime".
     */
    public static Hashtable putCredToMyProxy(
                  String host, int port, GSSCredential credential,
                  String username, String passphrase, int lifetime) {

        Hashtable rethash = new Hashtable();

        if (host.length() == 0) {
            host = MYPROXYHOST;
            logger.warn("No MyProxy host specified. Using '" + host + "'");
        }
        rethash.put("host",new String(host));
        if (port == 0) {
            port = MYPROXYPORT;
            logger.warn("No MyProxy port specified. Using '" + port + "'");
        }
        rethash.put("port",new Integer(port));
        if (username.length() == 0) {
            username = randomDNFromCred(credential);
            logger.warn("No MyProxy username specified. Using '"+username+"'");
        }
        rethash.put("username",new String(username));
        if (passphrase.length() == 0) {
            passphrase = randString(16);
            logger.warn("No MyProxy passphrase specified. Using '" +
                        passphrase + "'");
        }
        rethash.put("passphrase",new String(passphrase));
        if (lifetime == 0) {
            lifetime = MYPROXYSHORT;
            logger.warn("No MyProxy lifetime specified. Using '" +
                        lifetime + "seconds.");
        }
        rethash.put("lifetime",new Integer(lifetime));

        try {
            MyProxy myProxyServer = new MyProxy(host,port);
            logger.info("Trying to put credential '" + credential +
                        "' to " + host + ":" + port + " with username = '"+
                        username + "', passphrase = '" +
                        passphrase + "', and lifetime = '" +
                        lifetime + "' ...");
            myProxyServer.put(credential,username,passphrase,lifetime);
            logger.info("Succeeded!");
        } catch (Exception e) {
            logger.error("Failed! Could not put proxy to '" +
                         host + ":" + port + "' because "+
                         e.getMessage());
        }

        return rethash;
    }


    /**
     * Delegate a GSSCredential to a MyProxy server using all default values.
     * This is a shortcut if you want to use the default MyProxy server and
     * port, a random username based on the credential's DN, a random
     * passphrase, and a default (short) lifetime.
     * @param credential A GSSCredential to be put to a MyProxy server.
     * @return A Hashtable containing the actual values used for "server",
     *         "port", "username", "passphrase", and "lifetime".
     */
    public static Hashtable putCredToMyProxy(GSSCredential credential) {
        return putCredToMyProxy(MYPROXYHOST,MYPROXYPORT,credential,
                                randomDNFromCred(credential),
                                randString(16),MYPROXYSHORT);
    }


    /**
     * Given a GSSCredential, return its DN (distinguished name) appended
     * with a hyphen plus a string of 8 random characters.  This is used
     * when generating a 'one time token' for storing a credential in a
     * MyProxy server.  You store the credential with a random username and
     * passphrase (plus a short lifetime).  You can then send the random
     * username and passphrase to another party who would use these values
     * to get that short-lived credential.
     * @param credential A GSSCredential to be stored in a MyProxy server.
     * @return A string consisting of the credential's DN appended with a
     *         hyphen ('-') and a string of 8 random characters.
     */
    public static String randomDNFromCred(GSSCredential credential) {

        String retDN = null;

        try {
            retDN = credential.getName() + "-" + randString(8);
        } catch (Exception e) {
            logger.error("randomDNFromCred: " + e.getMessage());
        }

        return retDN;
    }


    /**
     * Returns a random character in the ASCII char range [lo,hi].  You
     * call this method with a character (e.g. 'A') as parameters since
     * they will be converted to ASCII values automatically.  The value
     * returned is the random ASCII character.
     * @param lo The 'minimum' ASCII value of the random char.
     * @param hi The 'maximum' ASCII value of the random char.
     * @return A random ASCII character in the range [lo,hi].
     */
    public static char randChar(char lo, char hi) {
        if (hi < lo) { // Make sure lo is lower than hi
            char tmp = lo;
            lo = hi;
            hi = tmp;
        }
        int n = (int)hi - (int)lo + 1;
        int i = rn.nextInt() % n;
        if (i < 0)
            i = -i;
        return (char)(lo + i);
    }


    /**
     * Returns a random string containing len characters.  The characters
     * are alphanumeric and some symbols, according to Java's ASCII table,
     * which can be found at http://mindprod.com/jgloss/ascii.html .
     * @param len The length of the new random string.
     * @return A random string of len alphanumeric characters.
     */
    public static String randString(int len) {
        if (len == 0)  // Len = 0 implies empty return string
            return "";
        if (len < 0)   // Make sure len is not negative
            len = -len;
        char c[] = new char[len];
        for (int i = 0; i < len; i++)
            c[i] = randChar('0','z');
        return new String(c);
    }

}

