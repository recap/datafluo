/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vlam.datafluo.submission;

import vlam.datafluo.wsengine.VlamDatafluoTask;

/**
 *
 * @author reggie
 */
public interface ISubmitter{

	public static final int STATE_NEW = 0;
	public static final int STATE_RUNNING = 1;
	public static final int STATE_DONE = 2;
	public static final int STATE_FAILED = 3;

	
	public void submit(String Command);
	public void submit(VlamDatafluoTask task);
	public void delete(VlamDatafluoTask task);
	public String getName();
	public void setMetric(int metric);
	public int getMetric();
	public long lastSetMetricTime();
	public int getAvailableSlots();
	public void setAvailableSlots(int slots);
	public int getTotalSlots();
	public void incSlot();
	public void decSlot();
	public void setStats(int slots, int usedSlots, int freeSlots);
	public boolean isReady();
	public long getConsumedCost();
	public void setConsumedCost(long cost);
	public TGridHandler getTGridHandler();
	public void setTGridHandler(TGridHandler tg);
	public boolean isStartingUp();
	public void incSubmitterCounter();
	public int getSubmitterCounter();
	public void setSubmitterCounter(int counter);

}
