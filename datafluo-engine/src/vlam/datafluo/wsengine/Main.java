/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vlam.datafluo.wsengine;
import java.beans.XMLDecoder;
import java.io.*;
import java.util.Vector;
import nl.wtcw.vle.wfd.TopologyI;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import uk.ac.soton.itinnovation.freefluo.core.event.PortStateChangedEvent;
import uk.ac.soton.itinnovation.freefluo.core.event.TaskStateChangedEvent;


import uk.ac.soton.itinnovation.freefluo.main.*;

import vlam.datafluo.reactor.*;
import vlam.datafluo.submission.BucketScheduler;
import vlam.datafluo.submission.CloudScheduler;
import vlam.datafluo.submission.DummyScheduler;
import vlam.datafluo.submission.HeartBeatMonitor;
import vlam.datafluo.submission.InteractiveScheduler;
import vlam.datafluo.submission.RoundRobinScheduler;
import vlam.datafluo.submission.Scheduler;
import vlam.datafluo.submission.SubmissionScheduler;
import vlam.datafluo.utils.*;
import vlam.datafluo.utils.SimpleLogging;
/**
 * 
 * @author Reggie
 */
//public class Main implements WorkflowStateListener, FlowStateListener
public class Main
{
 
	
    private final Object completionLock = new Object();
	private long startTime = 0;
	private long endTime = 0;

	public void taskStateChanged(TaskStateChangedEvent taskStateChangedEvent) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void portStateChanged(PortStateChangedEvent portStateChangedEvent) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	private enum Schedulers{
		Bucket, RoundRobin, Local, Interactive, Cloud, None;
	}
	private enum Protocol{
		PGsiFtp, GsiFtp, Raw, None;
	}
	
	public static void main(String[] args){
		try{

			if(args.length == 8){
				GlobalConfiguration.vlamXMLfile = args[0].toString();
				GlobalConfiguration.scheduler = args[1].toString();
				GlobalConfiguration.protocol = args[2].toString();
				GlobalConfiguration.centralReactor = args[3].toString();			
				GlobalConfiguration.centralReactorPort = Integer.parseInt(args[4].toString());			
				GlobalConfiguration.queueMonitorPort = Integer.parseInt(args[5].toString());
				GlobalConfiguration.loadThreshold = Integer.parseInt(args[6].toString());
				GlobalConfiguration.maxCloneBurst = Integer.parseInt(args[7].toString());
			}
			else
				throw new Exception("too few args did u forget the last two params?");

		//Get default cred from grid-proxy-init
		ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();
        GSSCredential gss_cred = manager.createCredential(GSSCredential.INITIATE_AND_ACCEPT);
		//Init stuff
        Main.inits(GlobalConfiguration.scheduler, GlobalConfiguration.protocol, gss_cred, 
				args[3].toString(), Integer.parseInt(args[4].toString()),Integer.parseInt(args[5].toString()) );
		//assume TopologyI is engough and gss credential is avail
		InputStream is_topology = new FileInputStream(GlobalConfiguration.vlamXMLfile);
        XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(is_topology));
        TopologyI topology = (TopologyI) decoder.readObject();

		//Implements event listeners
		DefaultEventListener eventListener = new DefaultEventListener();
		Main.registerEventListener(eventListener);

		//ok
		SomeClient client = new SomeClient();
        client.engine = Main.create(topology,  (IStateMonitor)eventListener);
         //Main.registerCallback(client.engine, (IStateMonitor)client);
         Main.executeWorkflow(client.engine);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
   

	public static void registerEventListener(IStateMonitor stateMonitor){
		GlobalConfiguration.addStateMonitor(stateMonitor);
	}
	public static VlamEngine create(TopologyI topology, IStateMonitor stateMonitor){
		try{

		
		VlamEngine engine = new VlamEngine();
		GlobalConfiguration.stateMonitor = stateMonitor;
		String flowId = engine.compile(topology);		
		//engine.addWorkflowStateListener(flowId, GlobalConfiguration.stateMonitor);
		GlobalConfiguration.setEngine(engine);
		WorkflowInstance flowInstance = engine.getWorkflowInstance(flowId);
		//Chgange
		GlobalConfiguration.setFlow(flowInstance.getFlow());
		GlobalConfiguration.getFlow().addFlowStateListener(GlobalConfiguration.stateMonitor);

		return engine;

		}catch(Exception ex)
		{ex.printStackTrace();}


		return null;

	}

	public static void registerCallback(VlamEngine engine, IStateMonitor stateMonitor){
		try{
			String flowId = engine.getFlowID();
			WorkflowInstance flowInstance = engine.getWorkflowInstance(flowId);
			flowInstance.getFlow().addFlowStateListener(stateMonitor);
			GlobalConfiguration.stateMonitor = stateMonitor;
		}catch(Exception ex)
		{ex.printStackTrace();}


	}

	public static void executeWorkflow(VlamEngine engine){
		try{
			ReactorServer reactorServer = new ReactorServer(GlobalConfiguration.centralReactorPort, 300);
			new Thread(reactorServer).start();

			engine.run(engine.getFlowID());

		}catch(Exception ex)
		{ex.printStackTrace();}
	}

	public static void setGSSCredential(GSSCredential gss_cred){
		GlobalConfiguration.gss_cred = gss_cred;
	}

	public static void inits(String scheduler, String protocol, GSSCredential gss_cred,
		String reactorIP, int reactorPort, int queueMonitorPort){

		GlobalConfiguration.logging = new SimpleLogging(GlobalConfiguration.logFile);
		GlobalConfiguration.gss_cred = gss_cred;
		GlobalConfiguration.centralReactor = reactorIP;
		GlobalConfiguration.centralReactorPort = reactorPort;
		GlobalConfiguration.queueMonitorPort = queueMonitorPort;
		
		//Vector servers = new Vector();
		//servers.add(new ServerEntry("VU-fs0","PGsiFtp",1,"fs0.das3.cs.vu.nl?"+GlobalConfiguration.home+"mdata/"));
		//servers.add(new ServerEntry("Leiden-fs1","PGsiFtp",1,"fs1.das3.liacs.nl?"+GlobalConfiguration.home+"mdata/"));
		//servers.add(new ServerEntry("UvA-fs2","PGsiFtp",1,"fs2.das3.science.uva.nl?"+GlobalConfiguration.home+"mdata/"));
		//servers.add(new ServerEntry("Delft-fs3","PGsiFtp",1,"fs3.das3.tudelft.nl?"+GlobalConfiguration.home+"mdata/"));
		//servers.add(new ServerEntry("Multimedia-fs4","PGsiFtp",1,"fs4.das3.science.uva.nl?"+GlobalConfiguration.home+"mdata/"));
		//GlobalConfiguration.setServers(servers);
		

		//Vector submitters = new Vector();
		//submitters.add(new GramSubmitter("fs0.das3.cs.vu.nl","fs0.das3.cs.vu.nl",0,gss_cred));
		//submitters.add(new GramSubmitter("fs1.das3.liacs.nl","fs1.das3.liacs.nl",0,gss_cred));
		//submitters.add(new GramSubmitter("fs2.das3.science.uva.nl","fs2.das3.science.uva.nl",0,gss_cred));
		//submitters.add(new GramSubmitter("fs3.das3.tudelft.nl","fs3.das3.tudelft.nl",0,gss_cred));
		//submitters.add(new GramSubmitter("fs4.das3.science.uva.nl","fs4.das3.science.uva.nl",0,gss_cred));

		//for(Iterator itr = submitters.iterator();itr.hasNext();){
		//	ISubmitter is = (ISubmitter)itr.next();
		//	is.setMetric(0);
		//}

		//GlobalConfiguration.setSubmitters(submitters);

		GlobalConfiguration.setSubmitters(new Vector());
		GlobalConfiguration.setServers(new Vector());
		
		synchronized (GlobalConfiguration.serverSync) {
				ServerEntry se = new ServerEntry("elab.science.uva.nl",GlobalConfiguration.protocol,1,"elab.science.uva.nl?"
						+GlobalConfiguration.home
						+GlobalConfiguration.serverRoot+"/");
				GlobalConfiguration.getServers().add(se);
		}

		Scheduler submissionScheduler = null;
		switch(Schedulers.valueOf(GlobalConfiguration.scheduler)){
			case Bucket:{
				submissionScheduler = new BucketScheduler();
				GlobalConfiguration.submissionScheduler = submissionScheduler;
				Thread t = new Thread(GlobalConfiguration.submissionScheduler);
				t.start();
				HeartBeatMonitor heartBeatMonitor = new HeartBeatMonitor();
				GlobalConfiguration.setHeartBeatMonitor(heartBeatMonitor);
				Thread hbm_t = new Thread(GlobalConfiguration.getHeartBeatMonitor());
				hbm_t.start();}
				break;
			case RoundRobin:{
				submissionScheduler = new RoundRobinScheduler();
				GlobalConfiguration.submissionScheduler = submissionScheduler;
				Thread t = new Thread(GlobalConfiguration.submissionScheduler);
				t.start();
				HeartBeatMonitor heartBeatMonitor = new HeartBeatMonitor();
				GlobalConfiguration.setHeartBeatMonitor(heartBeatMonitor);
				Thread hbm_t = new Thread(GlobalConfiguration.getHeartBeatMonitor());
				hbm_t.start();}
				break;
			case Interactive:{
				submissionScheduler = new InteractiveScheduler();
				GlobalConfiguration.submissionScheduler = submissionScheduler;
				Thread t = new Thread(GlobalConfiguration.submissionScheduler);
				t.start();
				HeartBeatMonitor heartBeatMonitor = new HeartBeatMonitor();
				GlobalConfiguration.setHeartBeatMonitor(heartBeatMonitor);
				Thread hbm_t = new Thread(GlobalConfiguration.getHeartBeatMonitor());
				hbm_t.start();
				UserInterface UI = new UserInterface();
				GlobalConfiguration.userInterface = UI;
				Thread uit = new Thread(UI);
				uit.start();}
				break;
			case Cloud:{
				submissionScheduler = new CloudScheduler();
				//Main.registerEventListener((CloudScheduler)submissionScheduler);
				GlobalConfiguration.submissionScheduler = submissionScheduler;
				Thread t = new Thread(GlobalConfiguration.submissionScheduler);
				t.start();
				HeartBeatMonitor heartBeatMonitor = new HeartBeatMonitor();
				GlobalConfiguration.setHeartBeatMonitor(heartBeatMonitor);
				Thread hbm_t = new Thread(GlobalConfiguration.getHeartBeatMonitor());
				hbm_t.start();
				UserInterface UI = new UserInterface();
				GlobalConfiguration.userInterface = UI;
				Thread uit = new Thread(UI);
				uit.start();}
				break;
			case Local:{
				submissionScheduler = new SubmissionScheduler();
				GlobalConfiguration.submissionScheduler = submissionScheduler;
				Thread t = new Thread(GlobalConfiguration.submissionScheduler);
				t.start();
				HeartBeatMonitor heartBeatMonitor = new HeartBeatMonitor();
				GlobalConfiguration.setHeartBeatMonitor(heartBeatMonitor);
				Thread hbm_t = new Thread(GlobalConfiguration.getHeartBeatMonitor());
				hbm_t.start();}
				break;
			case None:{
				submissionScheduler = new DummyScheduler();
				GlobalConfiguration.submissionScheduler = submissionScheduler;
				}
				break;
			default:
				//System.err.println("Unknown Scheduler!");
		}
		
	}   
	
	
}
