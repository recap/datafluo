/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.datafluo.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import nl.wtcw.vle.datafluo.core.engine.Engine;
import nl.wtcw.vle.datafluo.core.flow.Flow;
import nl.wtcw.vle.datafluo.core.port.Port;
import nl.wtcw.vle.datafluo.core.port.VLEPort;
import nl.wtcw.vle.datafluo.core.task.AbstractTask;
import nl.wtcw.vle.datafluo.core.task.Task;
import nl.wtcw.vle.datafluo.credential.ICredential;
import nl.wtcw.vle.datafluo.event.IStateMonitor;
import nl.wtcw.vle.datafluo.messaging.IMessageExchange;
import nl.wtcw.vle.datafluo.messaging.MessageExchange;
import nl.wtcw.vle.datafluo.reactor.DataServerEntry;
import nl.wtcw.vle.datafluo.submission.BucketScheduler;
import nl.wtcw.vle.datafluo.submission.CloudScheduler;
import nl.wtcw.vle.datafluo.submission.DummyScheduler;
import nl.wtcw.vle.datafluo.submission.FifoLocalScheduler;
import nl.wtcw.vle.datafluo.submission.HeartBeatMonitor;
import nl.wtcw.vle.datafluo.submission.RoundRobinScheduler;
import nl.wtcw.vle.datafluo.submission.Scheduler;
import org.apache.log4j.Logger;

/**
 *
 * @author reggie
 */
public class Context {
	private static Logger logger = Logger.getLogger(Context.class);
	private Engine engine = null;
	private IMessageExchange messageExchange = null;
	private Scheduler scheduler;	
	private ArrayList stateMonitors = new ArrayList();
	private String contextId = null;
	private ArrayList dataServers = new ArrayList();
	private ArrayList resourceSubmitters = new ArrayList();
	private HashMap<String, Object> credentials = new HashMap();

	private static enum parameters{
		scheduler,dataserver,submitter
	}

	private static enum schedulers{
		bucket,roundrobin,cloud,dummy,fifolocal
	}

	public Context(String id){
		logger.debug("Context.<init>");
		this.contextId = id;
	}

	public void addStateMonitor(IStateMonitor stateMonitor){
		this.stateMonitors.add(stateMonitor);

		if(this.engine != null){
			Flow flow = engine.getFlow();
			flow.addFlowStateListener(stateMonitor);

			for(Iterator itr = flow.getTasks().iterator(); itr.hasNext();){
				AbstractTask task = (AbstractTask)itr.next();
				task.addTaskStateListener(stateMonitor);

				Collection c1 = task.getInputPorts();
				Collection c2 = task.getOutputPorts();

				for(Iterator pitr = task.getInputPorts().iterator(); pitr.hasNext();){
					Port port = (Port)pitr.next();
					port.addPortStateListener(stateMonitor);
				}
				for(Iterator pitr = task.getOutputPorts().iterator(); pitr.hasNext();){
					Port port = (Port)pitr.next();
					port.addPortStateListener(stateMonitor);
				}
			}
		}
	}

	public Collection getStateMonitors(){
		return this.stateMonitors;
	}

	public void addCredential(ICredential credential){
		this.credentials.put(credential.getName(), credential);
	}
	
	private void setScheduler(String scheduler){
		switch(Context.schedulers.valueOf(scheduler)){
			case bucket:{
				this.scheduler = new BucketScheduler(this);
			}
			break;
			case roundrobin:{
				this.scheduler = new RoundRobinScheduler(this);
			}
			break;
			case cloud:{
				this.scheduler = new CloudScheduler(this);
			}
			break;
			case dummy:{
				this.scheduler = new DummyScheduler(this);
			}
			break;
			case fifolocal:{
				this.scheduler = new FifoLocalScheduler(this);
			}
			break;
			default:{}
		}
	}

	public void setConfigParameter(String config){
		String parameter[] = config.split("=");
		switch(Context.parameters.valueOf(parameter[0])){
			case scheduler:{
				setScheduler(parameter[1]);
			}
			break;
			case dataserver:{

			}
			break;
			case submitter:{

			}
			break;
			default:{}
		}
	}

	public Scheduler getScheduler(){
		return this.scheduler;
	}
	
	public void setEngine(Engine engine){
		this.engine = engine;
	}

	public void setMessageExchange(IMessageExchange exchange){
		this.messageExchange = exchange;
	}
	
	public void setContextId(String id){
		this.contextId = id;
	}

	public String getContextId(){
		return this.contextId;
	}

	public Engine getEngine(){
		return this.engine;
	}

	public IMessageExchange getMessageExchange(){
		return this.messageExchange;
	}

	public void addDataServer(DataServerEntry dataServer){
		this.dataServers.add(dataServer);
	}

	public ArrayList getDataServers(){
		return this.dataServers;
	}
}
