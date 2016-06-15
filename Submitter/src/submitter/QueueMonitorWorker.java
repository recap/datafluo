/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package submitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
 *
 * @author reggie
 */
class QueueMonitorWorker implements Runnable {

	private static Logger logger = Logger.getLogger(QueueMonitorWorker.class);
	protected Socket clientSocket = null;
	private boolean rrdlog = true;
	private String rrd_prefix = "public_html/";
	private ResourceManager resourceManager = null;

	public QueueMonitorWorker(Socket clientSocket) {
		resourceManager = ResourceManager.getClassInstance();
		this.clientSocket = clientSocket;
	}

	public synchronized void run() {
		try {

			//BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			Writer w = new StringWriter();
			char[] buffer = new char[1024];
			InputStream in = clientSocket.getInputStream();
			try{
				Reader reader = new BufferedReader(	new InputStreamReader(in));
				int n;
				while ((n = reader.read(buffer)) != -1){
					w.write(buffer, 0, n);
				}
			} finally{
				in.close();
				if(clientSocket != null)
					clientSocket.close();
			}
			String data = w.toString();
			data = data.trim();

			
			logger.debug("QUEUELOAD: " + data);
			String dat_file = new String();
			updateSubmitterStats(data);
			if (rrdlog == true) {
				try {
					String[] spl_data = data.split(" ");
					int per = 0;
					if (Integer.parseInt(spl_data[1]) > 0)
						per = (Integer.parseInt(spl_data[3]) * 100) / Integer.parseInt(spl_data[1]);
					else
						per = 0;

					System.err.println(spl_data[0] + ": " + per + "%");
					String rrd_file = this.rrd_prefix+spl_data[0] + ".rrd";
					dat_file=this.rrd_prefix+spl_data[0] + ".dat";
					File f = new File(rrd_file);
					if (f.exists() == true) {
						Runtime rt = Runtime.getRuntime();
						String cmd = "rrdtool update " + rrd_file + " N:" + Integer.toString(per);
						Process pr = rt.exec(cmd);
						pr.getErrorStream().close();
						pr.getInputStream().close();
						pr.getOutputStream().close();
						pr.waitFor();
					} else {
						Runtime rt = Runtime.getRuntime();
						String cmd = "rrdtool create " + rrd_file + " --start N --step 20 DS:probe1:GAUGE:40:0:1000 RRA:MAX:0.5:1:100";
						Process pr = rt.exec(cmd);
						pr.getErrorStream().close();
						pr.getInputStream().close();
						pr.getOutputStream().close();
						pr.waitFor();
					}
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				finally{
					if(this.clientSocket != null)
						this.clientSocket.close();
				}
			}

			FileWriter fstream = new FileWriter(dat_file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(data+"\n");
			out.close();
			fstream.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}//run

	private synchronized ISubmitter getSubmitterByName(String submitterName) {
		Collection submitters = this.resourceManager.getSubmitters();
		for (Iterator itr = submitters.iterator(); itr.hasNext();) {
			ISubmitter is = (ISubmitter) itr.next();
			if (is.getName().contains(submitterName) == true) {
				return is;
			}

		}
		return null;
	}

	private synchronized void updateSubmitterStats(String data) {

		String[] spldata = data.split(" ");
		String hostname = spldata[0];		
		String[] hostnameParts = hostname.split("\\.");
		String shortHostname = hostnameParts[0];

		ISubmitter is = null;
		
		int slots = Integer.parseInt(spldata[1].toString());		
		int qd = Integer.parseInt(spldata[3].toString());		
		int free = Integer.parseInt(spldata[2].toString());
		int cost = 0;

		if (spldata.length >= 4) {
			cost = Integer.parseInt(spldata[4].toString());
		}
		
		is = (ISubmitter) getSubmitterByName(shortHostname);
		if (is == null) {			
				is = new GramSubmitter(hostname, hostname, 0, null);
				resourceManager.addSubmitter(is);
		}
		
		is.setStats(slots, qd, free);
		is.setMetric(cost);		

	}
}
