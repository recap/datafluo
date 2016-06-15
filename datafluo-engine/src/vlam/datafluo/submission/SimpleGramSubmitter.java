/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.submission;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import vlam.datafluo.utils.GlobalConfiguration;
import vlam.datafluo.wsengine.VlamDatafluoTask;

/**
 *
 * @author reggie
 */
public class SimpleGramSubmitter implements ISubmitter, Comparable {

	//private Runtime rt = Runtime.getRuntime();
	private int state = ISubmitter.STATE_NEW;
	private String id = "";
	public String URL = "";
	private int metric = 0;
	private int slots = 0;
	private int freeSlots = 0;
	public String home = GlobalConfiguration.home;
	public String vlport = GlobalConfiguration.vlport;
	private VlamDatafluoTask task = null;
	public boolean firstSubmit = false;
	protected ExecutorService threadPool = null;
	protected int maxConcurrentSubmissions = 100;
	private boolean ready = false;
	private final Object synStats = new Object();
	public final Object syncMe = new Object();

	public SimpleGramSubmitter(String id, String URL, int metric, int slots) {
		this(id, URL, metric, slots, 10);
	}

	public SimpleGramSubmitter(String id, String URL, int slots) {
		this(id, URL, 1, slots, 100);
	}
	
	public SimpleGramSubmitter(String id, String URL, int metric, int slots, int maxConcurrentSubmissions) {
		this.id = id;
		this.URL = URL;
		this.metric = metric;
		this.slots = slots;
		//Optimistic
		this.freeSlots = slots;
		this.maxConcurrentSubmissions = maxConcurrentSubmissions;
		this.threadPool = Executors.newFixedThreadPool(this.maxConcurrentSubmissions);
	}
	
	@Override
	public String getName() {
		return this.id;
	}

	public synchronized void submit(VlamDatafluoTask task) {
		this.threadPool.execute(
				new Thread(new SimpleGramSubmitterThread(task, this)));
	}

	public synchronized void delete(VlamDatafluoTask task) {
		String command = "globusrun-ws -kill -j EPR_" + task.getTaskId() + ".xml";
		LocalSubmitter ls = new LocalSubmitter();
		ls.submit(command);
	}

	public void setMetric(int metric) {
		this.metric = metric;
	}

	public int getMetric() {
		return this.metric;
	}

	public int compareTo(Object o) {
		SimpleGramSubmitter s = (SimpleGramSubmitter) o;
		return (s.metric - this.metric);
	}

	public boolean isFull() {

		if (this.freeSlots < 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void submit(String command) {
		throw new UnsupportedOperationException("Not supported yet.");

	}

	public int getAvailableSlots() {
		return this.freeSlots;
	}

	public void setAvailableSlots(int freeSlots) {
		synchronized (this.synStats) {
			this.freeSlots = freeSlots;
			this.metric = this.metric * freeSlots;
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

	}

	public boolean isReady() {
		return this.ready;
	}

	public long lastSetMetricTime() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	

	public long getConsumedCost() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setConsumedCost(long cost) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public TGridHandler getTGridHandler() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setTGridHandler(TGridHandler tg) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public int getTotalSlots() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isStartingUp() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void incSubmitterCounter() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public int getSubmitterCounter() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setSubmitterCounter(int counter) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
