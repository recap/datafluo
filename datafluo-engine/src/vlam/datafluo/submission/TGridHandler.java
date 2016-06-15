/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vlam.datafluo.submission;

import java.io.BufferedReader;
import java.io.InputStreamReader;
//import java.math.BigInteger;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import vlam.datafluo.utils.GlobalConfiguration;
//import java.security.SecureRandom;

/**
 *
 * @author reggie
 */
public class TGridHandler implements Runnable{

	private String uid;
	private String name;
	private int capacity;
	private long budget;
	private int state;
	private boolean cameOnline = false;
	private long cameOnlineTime = 0;
	private int pendingGraceTime = 3;
	public String ip;
	private int totalCost = 0;
	public ISubmitter submitter = null;

	public static final int NEW = 0;
	public static final int ACTIVE = 1;
	public static final int IDLE = 2;
	public static final int GRACEDIDLE = 7;
	public static final int CREATING = 3;
	public static final int TERMINATING = 4;
	public static final int TERMINATED = 5;
	public static final int UNKNOWN = 6;

	public TGridHandler(String uid, String name, int capacity, long budget){
		this.uid = uid;
		this.name = name;
		this.capacity = capacity;
		this.budget = budget;
		this.state = TGridHandler.NEW;

	}
	public TGridHandler(int capacity, long budget){
		Random gen = new Random(System.currentTimeMillis());
		int ruid = gen.nextInt(500000);

		//SecureRandom random = new SecureRandom();
		//String suid = new BigInteger(130, random).toString(8);
		this.uid = Integer.toString(ruid);
		this.name = "My_"+this.uid;
		this.capacity = capacity;
		this.budget = budget;
	}

	public void run() {
		URL tgUrl;
		try {
			
			tgUrl = new URL(createURL(this.uid, this.name, this.capacity, this.budget));
			GlobalConfiguration.logging.log("TGRID NEW: " + createURL(this.uid, this.name, this.capacity, this.budget));
			System.err.println("URL: "+ createURL(this.uid, this.name, this.capacity, this.budget));
			URLConnection tgConn = tgUrl.openConnection();
			BufferedReader in = new BufferedReader(
											new InputStreamReader(tgConn.getInputStream()));
			String line;
			while((line = in.readLine()) != null){
				System.err.println(line);
				if(line.contains("<state>")){
					this.state = convertState(stripXML(line));
					if( (this.isAlive() == true) && (this.cameOnline == false) ){
						this.cameOnlineTime = System.currentTimeMillis();
						this.cameOnline = true;
					}
					break;
				}
			}
			in.close();
			//System.exit(0);

			while(blockTillReady() == false){
				Thread.sleep(5000);
			}

			

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	private boolean blockTillReady() throws InterruptedException
	{
		try {
			while ( true ) { //(this.state != TGridHandler.ACTIVE) && (this.state != TGridHandler.IDLE) ){
				URL stgUrl = new URL(statusURL(this.uid));
				URLConnection stgConn = stgUrl.openConnection();
				BufferedReader sin = new BufferedReader(
											new InputStreamReader(stgConn.getInputStream()));
				String sline;
				while((sline = sin.readLine()) != null){
					if(sline.contains("<state>")){
						System.err.println("TGRID: "+this.uid+" STATE: "+stripXML(sline));
						this.state = convertState(stripXML(sline));
						//break;
					}
					if(sline.contains("<ip>")){
						this.ip = stripXML(sline).replace('.', '-');
						System.err.println("IP: "+this.ip);
					}
					if(sline.contains("<totalCost>")){
						this.totalCost = Integer.parseInt(stripXML(sline));
						System.err.println("totalCost: "+Integer.toString(this.totalCost));
						//if(this.submitter != null){
						//	this.submitter.setConsumedCost(this.totalCost);
						//}
					}
				}
				sin.close();

				if( (this.state == TGridHandler.TERMINATED) || (this.state == TGridHandler.UNKNOWN) ){
					URL delUrl = new URL(delURL(this.uid));
					delUrl.openConnection();
					return true;
				}
				Thread.sleep(3000);
			}//while
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
	}

	public void terminate(){
		try {
			URL tUrl = new URL(terminateURL(this.uid));
			tUrl.openConnection();
		} catch (Exception ex) {
			Logger.getLogger(TGridHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public boolean isRunOutOfBudget(){
		if(this.totalCost >= (this.budget - GlobalConfiguration.budgetReserve))
			return true;
		return false;
	}
	public boolean isPending(){
		//return true;
		if(this.state == TGridHandler.CREATING)
			return true;
		if(this.state == TGridHandler.NEW)
			return true;
		if(this.state == TGridHandler.IDLE){
			long timestamp = System.currentTimeMillis();
			long diff = (timestamp - this.cameOnlineTime) /1000;
			if(diff > this.pendingGraceTime){
				this.state = TGridHandler.GRACEDIDLE;
				return true;
			}
		}
		return false;
	}
	public boolean isAlive(){
		if(this.state == TGridHandler.ACTIVE)
			return true;
		if(this.state == TGridHandler.IDLE)
			return true;
		if(this.state == TGridHandler.GRACEDIDLE)
			return true;

		return false;
	}

	public boolean isDead(){
		if(this.state == TGridHandler.TERMINATED)
			return true;
		if(this.state == TGridHandler.UNKNOWN)
			return true;

		return false;
	}

	private int convertState(String state){
		if(state.contentEquals("ACTIVE"))
			return TGridHandler.ACTIVE;
		if(state.contentEquals("IDLE"))
			return TGridHandler.IDLE;
		if(state.contentEquals("CREATING"))
			return TGridHandler.CREATING;
		if(state.contentEquals("TERMINATING"))
			return TGridHandler.TERMINATING;
		if(state.contentEquals("TERMINATED"))
			return TGridHandler.TERMINATED;

		return TGridHandler.UNKNOWN;
	}

	private String stripXML(String s){
		int of1 = s.indexOf(">");
		int of2 = s.lastIndexOf("<");
		if(of1 == of2+1)
			return "";

		return s.substring(of1+1, of2);
	}

	private String createURL(String uuid, String name, int capacity, long budget){
		String baseURL = GlobalConfiguration.transientGridManager;
		//String url = baseURL+"tgrid.create?uuid="+uuid+"&name="+name+"&capacity="+Integer.toString(capacity)+"&budget="+Long.toString(budget)
		//		+"&tgridprovider=OpenNebula@SARA";
		String url = baseURL+"tgrid.create?uuid="+uuid+"&name="+name+"&capacity="+Integer.toString(capacity)+"&budget="+Long.toString(budget);
		return url;
	}
	private String terminateURL(String uuid){
		String baseURL = GlobalConfiguration.transientGridManager;
		String url = baseURL+"tgrid.terminate?uuid="+uuid;
		return url;
	}
	private String delURL(String uuid){
		String baseURL = GlobalConfiguration.transientGridManager;
		String url = baseURL+"tgrid.delete?uuid="+uuid;
		return url;
	}
	private String statusURL(String uuid){
		String baseURL = GlobalConfiguration.transientGridManager;
		String url = baseURL+"tgrid.status?uuid="+uuid;
		return url;
	}

	public int getCapacity(){
		return this.capacity;
	}

}
