/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.submission;

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
import nl.wtcw.vle.datafluo.reactor.DataServerEntry;
import nl.wtcw.vle.datafluo.util.GlobalConfiguration;
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
	private Collection submitters = GlobalConfiguration.getSubmitters();

	public QueueMonitorWorker(Socket clientSocket) {
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

		for (Iterator itr = submitters.iterator(); itr.hasNext();) {
			ISubmitter is = (ISubmitter) itr.next();
			if (is.getName().contains(submitterName) == true) {
				return is;
			}

		}
		return null;
	}

	private synchronized DataServerEntry getServerByName(String serverName){
		for(Iterator itr = GlobalConfiguration.getServers().iterator(); itr.hasNext();){
			DataServerEntry se = (DataServerEntry)itr.next();
			if(se.getServerId().contains(serverName) == true){
				return se;
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
		DataServerEntry se = null;
		//System.err.print("SLOTS:" + spldata[1].toString() + "\n");
		int slots = Integer.parseInt(spldata[1].toString());
		//System.err.print("QD:" + spldata[2].toString() + "\n");
		int qd = Integer.parseInt(spldata[3].toString());
		//System.err.print("FREE:" + spldata[3].toString() + "\n");
		int free = Integer.parseInt(spldata[2].toString());
		int cost = 0;

		if (spldata.length >= 4) {
			cost = Integer.parseInt(spldata[4].toString());
		}
		/*if (spldata.length >= 5) {
			String[] ts = spldata[5].split("\\.");
			int total = 0;
			for(int i =0; i < ts.length; i++){
				total += Integer.parseInt(ts[i]);
			}
			total += 7000;
			hostname = hostname.concat(":"+Integer.toString(total));
		}*/
		//REJECTS
		/*if(hostname.contains("145-100-30-227.cloud.sara.nl"))
			return;s
		if(hostname.contains("145-100-30-222.cloud.sara.nl"))
			return;
		if(hostname.contains("145-100-30-216.cloud.sara.nl"))
			return;
		if(hostname.contains("145-100-30-220.cloud.sara.nl"))
			return;
		if(hostname.contains("145-100-30-226.cloud.sara.nl"))
			return;
		if(hostname.contains("145-100-30-219.cloud.sara.nl"))
			return;
		if(hostname.contains("145-100-30-217.cloud.sara.nl"))
			return;*/
		//if(hostname.contains("145-100-30-216.cloud.sara.nl"))
		//	return;
		//if(hostname.contains("145-100-30-199.cloud.sara.nl"))
		//	return;
		//if(hostname.contains("145-100-30-219.cloud.sara.nl"))
		//	return;
		//if(hostname.contains("145-100-30-220.cloud.sara.nl"))
		//	return;




		is = (ISubmitter) getSubmitterByName(shortHostname);
		if (is == null) {
			synchronized (GlobalConfiguration.submittersSync) {
				is = new GramSubmitter(hostname, hostname, 0, GlobalConfiguration.gss_cred);
				submitters.add(is);
			}
			//GlobalConfiguration.logging.debug(hostname + " is up.");
		}
		synchronized (GlobalConfiguration.submittersSync) {
			is.setStats(slots, qd, free);
			is.setMetric(cost);
		}

		/*se = (DataServerEntry)getServerByName(shortHostname);
		if(se == null){
			synchronized (GlobalConfiguration.serverSync) {
				se = new DataServerEntry(hostname,GlobalConfiguration.protocol,1,hostname+"?"
						+GlobalConfiguration.home
						+GlobalConfiguration.serverRoot+"/");
				GlobalConfiguration.getServers().add(se);
			}
			GlobalConfiguration.logging.debug(hostname + " is up.");
		}*/


		logger.debug("Consumed " + is.getName() + " cost: " + is.getConsumedCost());

	}
}
