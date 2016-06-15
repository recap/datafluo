/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.submission;

import java.util.*;
import java.io.*;
import nl.wtcw.vle.datafluo.util.GlobalConfiguration;
import nl.wtcw.vle.datafluo.core.task.VLETask;

/**
 *
 * @author reggie
 */
class Printer implements Runnable {

	private final BufferedReader buf;

	Printer(BufferedReader buf) {
		this.buf = buf;
	}

	public void run() {
		String line = null;

		try {
			while ((line = this.buf.readLine()) != null) {
				System.err.println(line);
			}
		} catch (Exception ex) {
			System.err.println(ex.toString());
			ex.printStackTrace();
		}
	}
}

public class LocalSubmitter implements ISubmitter, Comparable {

	private Runtime rt = Runtime.getRuntime();
	private int state = ISubmitter.STATE_NEW;
	private BufferedReader input;
	private BufferedReader error;
	private String command;
	private String id;
	private String directory;
	private int metric;

	public LocalSubmitter(String id, String directory, int metric) {
		this.id = id;
		this.directory = directory;
		this.metric = metric;
	}

	public LocalSubmitter() {
		this("Local_Default", "./", 0);
	}

	@Override
	public String getName() {
		return this.id;
	}

	public int compareTo(Object o) {
		LocalSubmitter s = (LocalSubmitter) o;
		return (s.metric - this.metric);
	}

	@Override
	public void submit(String command) {
		try {
			this.command = command;
			this.state = ISubmitter.STATE_RUNNING;
			System.err.print("COMMAND: " + this.command + "\n");

			Process pr = rt.exec(this.command);
			pr.getErrorStream().close();
			pr.getInputStream().close();
			pr.getOutputStream().close();



			int exitVal = pr.waitFor();
			if (exitVal == 0) {
				this.state = ISubmitter.STATE_DONE;
			} else {
				this.state = ISubmitter.STATE_FAILED;
			}

			pr.getErrorStream().close();
			pr.getInputStream().close();
			pr.getOutputStream().close();
			pr.destroy();

		} catch (Exception e) {
			System.err.println(e.toString());
			e.printStackTrace();
		}
	}

	public BufferedReader getOutput() {
		return this.input;
	}

	public void printOutput() {
		String line = null;

		try {
			while ((line = this.input.readLine()) != null) {
				System.err.println(line);
			}
		} catch (Exception ex) {
			System.err.println(ex.toString());
			ex.printStackTrace();
		}
	}

	public void printErrorOutput() {
		String line = null;

		try {
			while ((line = this.error.readLine()) != null) {
				System.err.println(line);
			}
		} catch (Exception ex) {
			System.err.println(ex.toString());
			ex.printStackTrace();
		}
	}

	public void setMetric(int metric) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public int getMetric() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void submit(VLETask task) {
		String lcommand = "/home/reggie/workspace/hg/datafluo/trunk/vlport-df/vlport2 -s 127.0.0.1 -p 5555 -i "+task.getTaskId()+" -h 127.0.0.1";
		this.submit(lcommand);
	}

	public int getAvailableSlots() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setAvailableSlots(int slots) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void incSlot() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void decSlot() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setStats(int slots, int usedSlots, int freeSlots) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isReady() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void delete(VLETask task) {
		throw new UnsupportedOperationException("Not supported yet.");
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
