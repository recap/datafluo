/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.wsengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Vector;
import vlam.datafluo.submission.ISubmitter;
import vlam.datafluo.utils.GlobalConfiguration;

/**
 *
 * @author reggie
 */
public class UserInterface implements Runnable {
	
	public void run() {
		while (true) {
			System.out.println("Global cost set to: " + GlobalConfiguration.globalCost);
			System.out.print("Enter new global cost : ");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				long i = Long.parseLong(br.readLine());
				if(i > 0)
					GlobalConfiguration.globalCost = i;
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
