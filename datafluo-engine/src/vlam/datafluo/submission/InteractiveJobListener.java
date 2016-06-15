/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vlam.datafluo.submission;

import java.io.*;

import org.globus.gram.Gram;
import org.globus.gram.GramJob;
import org.globus.gram.GramException;
import org.globus.gram.WaitingForCommitException;
import org.globus.gram.GramJobListener;

class InteractiveJobListener extends JobListener {

    private boolean quiet;
    private boolean finished = false;

    public InteractiveJobListener(boolean quiet) {
        this.quiet = quiet;
    }

    public boolean stillActive() {
        return !this.finished;
    }

    // waits for DONE or FAILED status
    public synchronized void waitFor()
        throws InterruptedException {
        while (!finished) {
            wait();
        }
    }

    public synchronized void statusChanged(GramJob job) {
        if (!quiet) {
            System.out.println("Job: "+ job.getStatusAsString());
        }
        status = job.getStatus();
        if (status == GramJob.STATUS_DONE) {
            finished = true;
            error = 0;
            notify();
        } else if (job.getStatus() == GramJob.STATUS_FAILED) {
            finished = true;
            error = job.getError();
            notify();
        }
    }
}