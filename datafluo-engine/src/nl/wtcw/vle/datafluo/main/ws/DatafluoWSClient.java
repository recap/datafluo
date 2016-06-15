/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.datafluo.main.ws;


import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.jms.Message;
import javax.jms.MessageListener;
import nl.wtcw.vle.datafluo.util.GlobalConfiguration;
import nl.wtcw.vle.wfd.TopologyI;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import nl.wtcw.vle.datafluo.event.DefaultStateMonitor;


/**
 *
 * @author reggie
 */
public class DatafluoWSClient {	
	private static DatafluoWS service;

    public static void main(String[] args) throws FileNotFoundException, GSSException, InterruptedException {
		PropertyConfigurator.configure("log4j.properties");
		
		service = new DatafluoWS();
		InputStream is_topology = new FileInputStream("octave_template3.xml");
		XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(is_topology));
		TopologyI topology = (TopologyI)decoder.readObject();

		//TODO temp
		DefaultStateMonitor stateMonitor = new DefaultStateMonitor();
		//GlobalConfiguration.addStateMonitor(eventListener);

		String contextId = service.createContext(topology);

		service.addStateMonitor(contextId, stateMonitor);
		

		/*ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();
        GSSCredential gss_cred = manager.createCredential(GSSCredential.INITIATE_AND_ACCEPT);

		service.addGSSCredential(contextId, "DefaultGlobus", gss_cred);*/

		service.setConfigParamters(contextId, "scheduler=fifolocal");
		//service.setConfigParamters(contextId, "scheduler=dummy");

		service.execute(contextId);

		Thread.sleep(10000);


		System.out.println("EXIT");
		

	}

}
