/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.datafluo.submission;

import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;

abstract class JobListener implements GramJobListener {

    protected int status = 0;
    protected int error = 0;

    public abstract void waitFor()
        throws InterruptedException;

    public int getError() {
        return error;
    }

    public int getStatus() {
        return status;
    }

    public boolean isFinished() {
        return (status == GramJob.STATUS_DONE ||
                status == GramJob.STATUS_FAILED);
    }
}