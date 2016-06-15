/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.submission;

import gminion_client.gMInION_client;
import vlam.datafluo.wsengine.VlamDatafluoTask;

/**
 * 
 * @author tbalint
 */
public class gMinionSubmitter implements ISubmitter {

    private static final String host = "ve.nikhef.nl";
    private static final int port = 7512;
    private static String user = "jobsP";    // Username & password to get a
    private static String pwd = "jobsP123";    //     cred from MyProxy server
    private gMInION_client engine = null;
    private String id;

    public gMinionSubmitter() {
        engine = gMInION_client.init();
        engine.setUp_gMInION_client_nonSec(host, port, user, pwd);
        //gM.setServiceLocation("https://194.171.96.44:8080/axis/services/gMinionSubmit");
        //gM.setUp_gMInION_client_sec(keyStore, keyStorePass, trustStore, trustStorePass, host, port, user, keyStorePass);
    }

    public void submit(String executable) {
        String sourceDir = "/home/tunde/VLPORT2/";
        String inFile = "test.txt";
        //  String outFile = "test.std";
        //String destDir = "/tmp/";
        //String progFile = "OMIITestApp";
        //String rmc = "fs2.das3.science.uva.nl";

        String stderr = "stderr";
        String stdout = "stdout";
        //working directory on the WN
        String workingD = "/tmp";

        if (engine != null) {

        engine.setCApath("/home/tunde/.globus/certificates");

        String jobId = engine.createJob();
//        gM.sendFile(jobId, "file:///home/tbalint/VLPORT2/test.txt","gsiftp://tbn14.nikhef.nl/tmp/test1.txt");
                //"srm://tbn18.nikhef.nl:8446/dpm/nikhef.nl/home/pvier/tbalint/test2.txt");
        engine.setExecutable(jobId, "/bin/date");
        String[] a = new String[]{"-d", "'UTC 1970-01-01 1240596879 secs'"};
        engine.setArguments(jobId, a);
        String m = engine.getFileToSubmit(jobId);

        //System.out.println(m);

        engine.start_job(jobId);
        //gM.waitForJob(jobId);

        engine.jobStateWatch(jobId);
        }
        else
        {
            System.err.println("Error with WS ..");
        }

    }

    public String getName() {
        return this.id;




    }

    public void setMetric(int metric) {
        throw new UnsupportedOperationException("Not supported yet.");




    }

    public int getMetric() {
        throw new UnsupportedOperationException("Not supported yet.");


    }

	public void submit(VlamDatafluoTask task) {
		throw new UnsupportedOperationException("Not supported yet.");
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

	public void delete(VlamDatafluoTask task) {
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
