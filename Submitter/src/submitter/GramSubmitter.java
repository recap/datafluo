/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package submitter;

import java.util.concurrent.atomic.AtomicInteger;

import nl.wtcw.vle.wfd.AtomicModuleI;
import org.globus.gram.GramJob;
import org.ietf.jgss.GSSException;
import org.globus.io.gass.server.GassServer;
import org.globus.rsl.NameOpValue;
import org.globus.rsl.RslNode;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;


/**
 *
 * @author reggie
 */
public class GramSubmitter implements ISubmitter, Comparable {

	private String id;
	private String URL;
	public GSSCredential gss_cred;
	private volatile GassServer m_gassServer = null;
	private static final AtomicInteger gassServers = new AtomicInteger();
	private int metric = -1;
	private long metricSetTime = 0;
	private int slots = 0;
	private int freeSlots = 0;
	private final Object synStats = new Object();
	private boolean ready = false;
	private long startTime = 0;
	private long lastTimeStamp = 0;
	private long consumedCost = 0;
	private int submitterCounter = 0;
	

	public GramSubmitter(String id, String URL, int metric, GSSCredential gss_cred) {
		this.id = id;
		this.URL = URL;
		if (gss_cred != null) {
			this.gss_cred = gss_cred;
		} else {
			ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();
			try {
				GSSCredential cred = manager.createCredential(GSSCredential.INITIATE_AND_ACCEPT);
				this.gss_cred = cred;
			} catch (GSSException ex) {
				ex.printStackTrace();
			}
		}
		this.metric = metric;
		this.startTime = System.currentTimeMillis();
		this.lastTimeStamp = this.startTime;
	}

	public GassServer getGassServer() {
		if (m_gassServer == null) {
			try {
				m_gassServer = new GassServer(gss_cred, 0);
				//m_gassServer.registerDefaultDeactivator();
				m_gassServer.setOptions(m_gassServer.getOptions() | GassServer.CLIENT_SHUTDOWN_ENABLE);
				//m_gassServer.setTimeout(GlobalConstant.EXP_TIMEOUT);
				m_gassServer.setTimeout(30000);
				System.err.println("GASS SERVERS: " + gassServers.incrementAndGet());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return m_gassServer;
	}

	@Override
	public String getName() {
		return this.id;
	}

	/*public synchronized void submit(VLETask task) {

		this.decSlot();
		this.incSubmitterCounter();
		String RSL = getRSL(task).toRSL(true);
		System.err.println(RSL);
		GramJob m_job = new GramJob(RSL);
		m_job.setCredentials(this.gss_cred);		

		try {
			
			m_job.request(this.URL, false, false);
			
			//m_job.request("145-100-30-203.cloud.sara.nl:5119",false, false);

		} catch (Exception ex) {
			Scheduler sched = GlobalConfiguration.submissionScheduler;
			sched.requeueRunnableTask(task);
			this.incSlot();
			this.submitterCounter--;
			ex.printStackTrace();
		}
	}*/

/*	public RslNode getRSL(VLETask task) {
		AtomicModuleI module = (AtomicModuleI) task.getModule();
		RslNode node = new RslNode();
		String hostName = module.getHost(); // rc="fs2.das3.science.uva.nl -q debug";

		//node.add(new NameOpValue("executable", NameOpValue.EQ, "${GLOBUS_USER_HOME}/local/vlport2/vlport2"));
		node.add(new NameOpValue("executable", NameOpValue.EQ, "/home/rcushing/local/vlport2/vlport2"));
		node.add(new NameOpValue("directory", NameOpValue.EQ, ".vlport2"));
		node.add(new NameOpValue("arguments", NameOpValue.EQ, new String[]{
					"-s", GlobalConfiguration.centralReactor, "-p", Integer.toString(GlobalConfiguration.centralReactorPort), 
					"-i", task.getTaskId()+"@"+String.format("%04d",task.getCloneNumber()), "-h", this.URL
				}));
		//node.add(new NameOpValue("environment", NameOpValue.EQ, new String[]{
		//			"FLIPPY", "dippy/flippy"
		//}));
		node.add(new NameOpValue("maxTime", NameOpValue.EQ, "60"));


		return node;
	}*/
	public RslNode getRSL(String serviceName){
		
		RslNode node = new RslNode();
		//String hostName = module.getHost(); // rc="fs2.das3.science.uva.nl -q debug";

		//node.add(new NameOpValue("executable", NameOpValue.EQ, "${GLOBUS_USER_HOME}/local/vlport2/vlport2"));
		node.add(new NameOpValue("executable", NameOpValue.EQ, "/home/rcushing/LocalApps/axis2-1.5.4/bin/axis2server.sh"));
		//node.add(new NameOpValue("directory", NameOpValue.EQ, ".axis2"));
		/*node.add(new NameOpValue("arguments", NameOpValue.EQ, new String[]{
		"-s", GlobalConfiguration.centralReactor, "-p", Integer.toString(GlobalConfiguration.centralReactorPort),
		"-i", task.getTaskId()+"@"+String.format("%04d",task.getCloneNumber()), "-h", this.URL
		}));*/ //node.add(new NameOpValue("environment", NameOpValue.EQ, new String[]{
		//			"JAVA_HOME", "/usr/local/package/jdk1.6.0_17-linux-i586"
		//}));
		node.add(new NameOpValue("maxTime", NameOpValue.EQ, "60"));


		return node;
	}



	public boolean isFull() {

		if (this.freeSlots < 0) {
			return true;
		} else {
			return false;
		}
	}

	public void setMetric(int metric) {
		this.metric = metric;
		this.metricSetTime = System.currentTimeMillis();
	}

	public int getMetric() {
		return this.metric;
	}

	public int getAvailableSlots() {
		return this.freeSlots;
	}
	public int getTotalSlots(){
		return this.slots;
	}

	public void setAvailableSlots(int freeSlots) {
		synchronized (this.synStats) {
			this.freeSlots = freeSlots;
			//this.metric = this.metric * freeSlots;
		}
	}

	public void incSlot() {
		synchronized (this.synStats) {
			this.freeSlots++;
		}
	}

	public void decSlot() {
		synchronized (this.synStats) {
			this.freeSlots--;
		}
	}

	public void setStats(int slots, int usedSlots, int freeSlots) {

		synchronized (this.synStats) {			
			this.slots = slots;
			this.freeSlots = freeSlots;
			this.ready = true;
		}
		System.out.println("stats: "+this.getName()+" slots: "+Integer.toString(this.slots)+" used: "+Integer.toString(this.slots - this.freeSlots));

	}

	public long getConsumedCost() {
		return this.consumedCost;
	}
	public void setConsumedCost(long cost){
		this.consumedCost = cost;
	}

	public boolean isReady() {
		return this.ready;
	}

	/*public void delete(VLETask task) {
		throw new UnsupportedOperationException("Not supported yet.");
	}*/

	public synchronized void submit(String serviceName) {

		this.decSlot();
		this.incSubmitterCounter();
		String RSL = getRSL("").toRSL(true);
		System.err.println("Submitting to "+ this.URL);
		System.err.println(RSL);
		GramJob m_job = new GramJob(RSL);
		m_job.setCredentials(this.gss_cred);

		try {

			m_job.request(this.URL, false, false);
			//m_job.request("145-100-30-203.cloud.sara.nl:5119",false, false);

		} catch (Exception ex) {
			this.incSlot();
			this.submitterCounter--;
			ex.printStackTrace();
		}
	}


	public int compareTo(Object o) {
		ISubmitter s = (ISubmitter) o;
		//return( (s.metric*s.freeSlots)  - (this.metric*this.freeSlots) );
		return (s.getMetric() - this.metric);
		//return (s.getConsumedCost() - this.getConsumedCost());
	}

	public long lastSetMetricTime() {
		return this.metricSetTime;
	}

	

	public void incSubmitterCounter() {
		this.submitterCounter++;
	}

	public int getSubmitterCounter() {
		return this.submitterCounter;
	}

	public void setSubmitterCounter(int counter) {
		this.submitterCounter = counter;
	}

	public boolean isStartingUp() {
		throw new UnsupportedOperationException("Not supported yet.");
	}




}
