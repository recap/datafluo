/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.submission;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import vlam.datafluo.utils.GlobalConfiguration;
import vlam.datafluo.wsengine.VlamDatafluoTask;

/**
 *
 * @author reggie
 */
public class SimpleGramSubmitterThread implements Runnable {

	protected VlamDatafluoTask task = null;
	protected SimpleGramSubmitter sgs = null;

	public SimpleGramSubmitterThread(VlamDatafluoTask task, SimpleGramSubmitter sgs) {
		this.task = task;
		this.sgs = sgs;
	}

	public void run() {
		
		String rslFile = "/tmp/VL_" + UUID.randomUUID().toString() + "_"
				+ sgs.getName() + ".rsl";
		String rslCommand = "globusrun-ws -submit -F " + sgs.URL + " -Ft SGE -S -f " + rslFile + " -o EPR_" + task.getTaskId() + ".xml";

		String RSL = new String();



		RSL = "<job>\n";
		RSL = RSL + "\t<executable>${GLOBUS_USER_HOME}/local/vlport2/vlport2</executable>\n";		
		RSL = RSL + "\t<directory>.vlport2</directory>\n";
		RSL = RSL + "\t<argument>-s</argument>\n";
		RSL = RSL + "\t<argument>" + GlobalConfiguration.centralReactor + "</argument>\n";
		RSL = RSL + "\t<argument>-p</argument>\n";
		RSL = RSL + "\t<argument>" + Integer.toString(GlobalConfiguration.centralReactorPort) + "</argument>\n";
		RSL = RSL + "\t<argument>-i</argument>\n";
		RSL = RSL + "\t<argument>" + task.getTaskId() + "</argument>\n";
		RSL = RSL + "\t<argument>-h</argument>\n";
		RSL = RSL + "\t<argument>" + task.getHost() + "</argument>\n";
		RSL = RSL + "\t<stdout>${GLOBUS_USER_HOME}/stdout_" + task.getTaskId() + "</stdout>\n";
		RSL = RSL + "\t<stderr>${GLOBUS_USER_HOME}/stderr_" + task.getTaskId() + "</stderr>\n";
		RSL = RSL + "\t<maxTime>720</maxTime>\n";
		synchronized (sgs.syncMe) {
			if (sgs.firstSubmit == false) {
				//only stage int proxy certificate the first time around
				sgs.firstSubmit = true;
				RSL = RSL + "\t<fileStageIn>\n";
				RSL = RSL + "\t\t<transfer>\n";
				RSL = RSL + "\t\t\t<sourceUrl>gsiftp://fs2.das3.science.uva.nl:2811/tmp/x509up_u1621</sourceUrl>\n";
				RSL = RSL + "\t\t\t<destinationUrl>file://" + sgs.home + "/.globus/x509up_u1621</destinationUrl>\n";
				RSL = RSL + "\t\t</transfer>\n";
				RSL = RSL + "\t</fileStageIn>\n";
			}
		}

		RSL = RSL + "\t<fileStageOut>\n";

		RSL = RSL + "\t\t<transfer>\n";
		RSL = RSL + "\t\t\t<sourceUrl>file://" + sgs.home + "/stdout_" + task.getTaskId() + "</sourceUrl>";
		RSL = RSL + "\t\t\t<destinationUrl>gsiftp://fs2.das3.science.uva.nl:2811" + sgs.home + "/stdout/stdout_" + task.getTaskId() + "</destinationUrl>";
		RSL = RSL + "\t\t</transfer>\n";

		RSL = RSL + "\t\t<transfer>\n";
		RSL = RSL + "\t\t\t<sourceUrl>file://" + sgs.home + "/stderr_" + task.getTaskId() + "</sourceUrl>";
		RSL = RSL + "\t\t\t<destinationUrl>gsiftp://fs2.das3.science.uva.nl:2811" + sgs.home + "/stderr/stderr_" + task.getTaskId() + "</destinationUrl>";
		RSL = RSL + "\t\t</transfer>\n";

		RSL = RSL + "\t</fileStageOut>\n";
		RSL = RSL + "</job>\n";

		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter(rslFile));
			fout.write(RSL);
			fout.close();


		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.err.print("Submitting command: " + rslCommand + "\n");
		LocalSubmitter ls = new LocalSubmitter();
		ls.submit(rslCommand);
	
	}
}
