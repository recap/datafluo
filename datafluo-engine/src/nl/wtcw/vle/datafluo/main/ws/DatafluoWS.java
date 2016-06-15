/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.datafluo.main.ws;

import nl.wtcw.vle.datafluo.context.Context;
import nl.wtcw.vle.datafluo.context.ContextManager;
import nl.wtcw.vle.datafluo.core.engine.VLEEngine;
import nl.wtcw.vle.datafluo.credential.GlobusCredential;
import nl.wtcw.vle.datafluo.credential.ICredential;
import nl.wtcw.vle.datafluo.event.IStateMonitor;
import nl.wtcw.vle.datafluo.messaging.MessageExchange;
import nl.wtcw.vle.datafluo.reactor.ReactorServer;
import nl.wtcw.vle.datafluo.submission.HeartBeatMonitor;
import nl.wtcw.vle.datafluo.submission.Scheduler;
import nl.wtcw.vle.wfd.TopologyI;
import org.ietf.jgss.GSSCredential;



/**
 *
 * @author reggie
 */
public class DatafluoWS {

	/*STEP 1*/
	public String createContext(TopologyI topology){
		ContextManager cm = ContextManager.getInstance();
		String contextId = cm.newContext();
		Context context = cm.getContext(contextId);			

		VLEEngine engine = new VLEEngine(context);
		context.setEngine(engine);

		MessageExchange exchange = new MessageExchange(context);
		context.setMessageExchange(exchange);

		engine.compile(topology);		

		return contextId;

	}
	/*STEP 2*/
	public void setConfigParamters(String contextId, String parameter /*param=value*/){
		ContextManager cm = ContextManager.getInstance();
		Context context = cm.getContext(contextId);

		context.setConfigParameter(parameter);
	}

	public void addStateMonitor(String contextId, IStateMonitor stateMonitor){
		ContextManager cm = ContextManager.getInstance();
		Context context = cm.getContext(contextId);
		context.addStateMonitor(stateMonitor);
	}

	public void addGSSCredential(String contextId, String name, GSSCredential credential){
		ContextManager cm = ContextManager.getInstance();
		Context context = cm.getContext(contextId);

		GlobusCredential globusCredential = new GlobusCredential(name,credential);
		context.addCredential(globusCredential);
	}

	public void addCredential(String contextId, ICredential credential){
		ContextManager cm = ContextManager.getInstance();
		Context context = cm.getContext(contextId);

		context.addCredential(credential);
	}

	public void execute(String contextId){
		ContextManager cm = ContextManager.getInstance();
		Context context = cm.getContext(contextId);

		ReactorServer rs = ReactorServer.getInstance();
		if(rs.isRunning() == false){
			Thread tr = new Thread(rs);
			tr.start();
		}

		HeartBeatMonitor hb = HeartBeatMonitor.getInstance();
		if(hb.isRunning() == false){
			Thread th = new Thread(hb);
			th.start();
		}

		Scheduler scheduler = context.getScheduler();
		Thread st = new Thread(scheduler);
		st.start();

		context.getEngine().run();

	}

	public void kill(String contextId){
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void pause(String contextId){
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void resume(String contextId){
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void getStatus(String contextId){
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
