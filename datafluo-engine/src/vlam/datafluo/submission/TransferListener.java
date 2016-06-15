/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vlam.datafluo.submission;

import java.io.*;

class TransferListener implements org.globus.io.urlcopy.UrlCopyListener {

    boolean finished = false;
    Exception _exception;

    public synchronized void waitFor() throws InterruptedException {
        while (!finished) {
            wait();
        }
    }

    public void transfer(long current, long total) {
        if (total == -1) {
            if (current == -1) {
                System.out.println("<third party transfer: progress unavailable>");
            } else {
                System.out.println(current);
            }
        } else {
            System.out.println(current + " out of " + total);
        }
    }

    public void transferError(Exception e) {
        _exception = e;
    }

    public void transferCompleted() {
        if (_exception == null) {
            System.out.println("Transfer completed successfully");
        } else {
            System.out.println("Transfer failed: " + _exception.getMessage());
        }
        finished = true;
    }

}