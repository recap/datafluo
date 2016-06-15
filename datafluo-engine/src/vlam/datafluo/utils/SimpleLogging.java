/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vlam.datafluo.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author reggie
 */
public class SimpleLogging {
	private String logFile;
	private BufferedWriter fout;

	public SimpleLogging(String logFile){
		this.logFile = logFile;
		try {
			fout = new BufferedWriter(new FileWriter(logFile));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public synchronized void log(String message){
		try {
			fout.write(message + "\n");
			fout.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
			//Logger.getLogger(SimpleLogging.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	public synchronized String getTime(){
		Date timePoint = new Date();
		SimpleDateFormat simpleDate = new SimpleDateFormat("HH:mm:ss");
		return simpleDate.format(timePoint);
	}

	public void close(){
		try {
			fout.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			//Logger.getLogger(SimpleLogging.class.getName()).log(Level.SEVERE, null, ex);
		}

	}
	public void debug(String msg) {
        if (GlobalConfiguration.debugOn == true) {
            System.err.println(this.getClass().getName() +  ": " + msg);
        }
    }
	public void debug(String msg, int level) {
        if( (GlobalConfiguration.debugOn == true) && (GlobalConfiguration.debugLevel > level) ) {
            System.err.println(this.getClass().getName() +  ": " + msg);
        }
    }


}
