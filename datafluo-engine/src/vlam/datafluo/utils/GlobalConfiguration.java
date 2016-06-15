/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.utils;

import java.io.File;
import java.util.Collection;
import java.util.Vector;
import org.ietf.jgss.GSSCredential;
import uk.ac.soton.itinnovation.freefluo.core.flow.*;
import vlam.datafluo.messaging.*;
import vlam.datafluo.submission.*;
import vlam.datafluo.reactor.*;
import vlam.datafluo.wsengine.IStateMonitor;
import vlam.datafluo.wsengine.StateMonitor;
import vlam.datafluo.wsengine.UserInterface;
import vlam.datafluo.wsengine.VlamEngine;

/**
 *
 * @author reggie
 */
public class GlobalConfiguration {

	public static String logFile = "Datafluo.log";
	public static SimpleLogging logging = null;
	public static boolean debugOn = true;
	public static int debugLevel = 10;
	private static Flow flow = null;
	private static Vector servers = null;
	private static Vector submitters = null;
	private static Vector stateMonitors = new Vector();
	private static VlamEngine engine = null;
	private static MessageExchange exchange = null;
	public static Scheduler submissionScheduler = null;
	public static HeartBeatMonitor heartBeatMonitor = null;
	public static IStateMonitor stateMonitor = null;
	public static UserInterface userInterface = null;
	public static long globalCost = 1000000000;
	public static long budgetReserve = 10000;
	public static long startTime = 0;
	public static long stopTime = 0;
	public static String centralReactor = "146.50.12.20";//fs2
	public static int centralReactorPort = 5555;
	public static int queueMonitorPort = 5556;
	public static String scheduler = "Bucket";
	public static String transientGridManager = "http://edge.ict.tno.nl:8001/";
	public static String protocol = "PGsiFtp";
	public static String vlamXMLfile;
	public final static String home = "/home/rcushing/";
	public final static String serverRoot = "mdata";
	public final static String vlport = "vlport2d/src/";
	public static int loadThreshold = 60; //seconds
	public final static int balanceFrequency = 10; //seconds
	public static int maxCloneBurst = 10;
	public final static int farming = 1; //0-disables, 1-enabled
	public static GSSCredential gss_cred = null;
	private final static Object globalThreadSync = new Object();
	public final static Object submittersSync = new Object();
	public final static Object serverSync = new Object();

	public static void addStateMonitor(IStateMonitor monitor) {
		GlobalConfiguration.stateMonitors.add(monitor);
	}

	public static Collection getStateMonitors() {
		return (Collection) GlobalConfiguration.stateMonitors;
	}

	public static int setFlow(Flow flow) {
		synchronized (globalThreadSync) {
			if (GlobalConfiguration.flow == null) {
				GlobalConfiguration.flow = flow;
				return 0;
			} else {
				return -1;
			}
		}

	}

	public static void setHeartBeatMonitor(HeartBeatMonitor hbm) {
		GlobalConfiguration.heartBeatMonitor = hbm;
	}

	public static HeartBeatMonitor getHeartBeatMonitor() {
		return GlobalConfiguration.heartBeatMonitor;
	}

	public static void setMessageExchange(MessageExchange messageExchange) {
		GlobalConfiguration.exchange = messageExchange;
	}

	public static MessageExchange getMessageExchange() {
		return GlobalConfiguration.exchange;
	}

	public static void setSubmitters(Vector submitters) {
		GlobalConfiguration.submitters = submitters;
	}

	public static Vector getSubmitters() {
		return GlobalConfiguration.submitters;
	}

	public static Flow getFlow() {
		synchronized (globalThreadSync) {
			return GlobalConfiguration.flow;
		}
	}

	public static int setEngine(VlamEngine engine) {
		synchronized (globalThreadSync) {
			if (GlobalConfiguration.engine == null) {
				GlobalConfiguration.engine = engine;
				return 0;
			} else {
				return -1;
			}
		}

	}

	public static VlamEngine getEngine() {
		synchronized (globalThreadSync) {
			return GlobalConfiguration.engine;
		}
	}

	public static Collection getServers() {
		synchronized (globalThreadSync) {
			return GlobalConfiguration.servers;
		}
	}

	public static int setServers(Vector servers) {
		synchronized (globalThreadSync) {
			if (GlobalConfiguration.servers == null) {
				GlobalConfiguration.servers = servers;
				return 0;
			} else {
				return -1;
			}
		}

	}
}
