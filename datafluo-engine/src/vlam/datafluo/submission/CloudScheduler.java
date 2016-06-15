/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.submission;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import vlam.datafluo.utils.GlobalConfiguration;
import vlam.datafluo.wsengine.IStateMonitor;
import vlam.datafluo.wsengine.VlamDatafluoTask;
import uk.ac.soton.itinnovation.freefluo.core.event.FlowStateChangedEvent;
import uk.ac.soton.itinnovation.freefluo.core.event.PortStateChangedEvent;
import uk.ac.soton.itinnovation.freefluo.core.event.TaskStateChangedEvent;

/**
 *
 * @author reggie
 */
public class CloudScheduler extends Scheduler implements IStateMonitor {

	private QueueMonitorServer monitorServer = new QueueMonitorServer();
	private Hashtable costs = new Hashtable();
	private Vector TGrids = new Vector();
	private Hashtable SubmitterToTGrid = new Hashtable();
	//private int uuidCounter = 3400;	
	private int submittersSize = 0;
	private int chunk = 50000000;
	private int defaultCapacity = 5;
	private int overProvision = -2;

	public void flowStateChanged(FlowStateChangedEvent flowStateChangedEvent) {
	//	if(flowStateChangedEvent.getState() == FlowState.COMPLETE_STATE){
	//		for(Iterator itr = this.TGrids.iterator();itr.hasNext();){
	//			TGridHandler tg = (TGridHandler)itr.next();
	//			tg.terminate();
	//		}
	//	}
	}

	public void taskStateChanged(TaskStateChangedEvent taskStateChangedEvent) {
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	public void portStateChanged(PortStateChangedEvent portStateChangedEvent) {
		//throw new UnsupportedOperationException("Not supported yet.");
	}
	
	private class TransientEntry{
		public String uuid = new String();
		public String name = new String();
		public int capacity = 5;
		public String status = new String();
	}


	public CloudScheduler() {
		super();
		Thread t = new Thread(monitorServer);
		t.start();
		this.submitters = (Vector) GlobalConfiguration.getSubmitters();		
	}
	
	/*public void run(){
		callWebService();
	}*/
	@Override
	public void run() {
		try {
			while (submitters.size() == 0) {
				Thread.sleep(2000);
			}
			//Grace Period
			Thread.sleep(5000);
			this.submittersSize = submitters.size();

			while (true) {
				//Main loop that gets the gets a task from the queue and matches it to a resource
				sem.acquire();
				System.err.println("AQUIRE");
				//Linking a TGrid handler to a submitter
				for(Iterator itr = this.TGrids.iterator();itr.hasNext();){
						TGridHandler tg = (TGridHandler)itr.next();
						if((tg.submitter == null) && (tg.isAlive() ==true)){
							ISubmitter is = this.getSubmitterByName(tg.ip);
							if(is != null){
							   is.setTGridHandler(tg);
							}
						}
				}
				VlamDatafluoTask task = null;

				//Getting a task from the runqueue
				synchronized (this.runnableTasks) {
					task = (VlamDatafluoTask) this.runnableTasks.firstElement();
					this.runnableTasks.remove(task);
					//System.err.println("TASK HOST: "+task.getHost());
				}
				ISubmitter is = null;

				//Check if task can run on any resource else binf the task to the specified
				//resource
				if ( (task.getHost().contains("any") == true ) || (task.getHost().trim().isEmpty() == true) ) {
					//Where a resource is found for the task
					is = (ISubmitter) matchMake(task);					
				} else {
					String[] spl_temp = task.getHost().toString().split("\\.");					
					is = (ISubmitter) getSubmitterByName(spl_temp[0].toString());					
				}

				if (is == null){
					System.err.println("REQUEUE1: "+task.getName());
					this.requeueRunnableTask(task);
					System.err.println("REQUEUE2: "+task.getName());
					Thread.sleep(2000);
					continue;
				}
				System.err.println("submitting task: " + task.getName() + " to " + is.getName());
				GlobalConfiguration.logging.log(task.getName() + " " + task.getTaskId() + " START_TIME: " + task.getStartTime().getShortString() + " " + task.getHost());
				is.submit(task);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	/*private ISubmitter matchMake(VlamDatafluoTask task) {
		ISubmitter is = null;
		synchronized (GlobalConfiguration.submittersSync) {
				for (Iterator itr = submitters.iterator(); itr.hasNext();) {
					is = (ISubmitter) itr.next();
					if(is.getMetric() == 81)
						return is;
				}
		}
		return null;
	}*/
	private ISubmitter matchMake(VlamDatafluoTask task) {
		ISubmitter is = null;
		boolean matched = false;
		System.err.println("IN MATCH MAKER");
		
			synchronized (GlobalConfiguration.submittersSync) {
				for (Iterator itr = submitters.iterator(); itr.hasNext();) {
					is = (ISubmitter) itr.next();
					if(is.getTGridHandler() != null){
						TGridHandler tg = is.getTGridHandler();
						if(tg.isDead() == true){
							//this.submitters.remove(is);
							continue;
						}
						if((tg.isAlive() == true) && (tg.isRunOutOfBudget() == false)){

							if(is.isStartingUp() == true){
								if(is.getSubmitterCounter() < tg.getCapacity())
									return is;
							}else{
								if(is.getAvailableSlots() > 0)
									return is;
							}
						}//if
					}//if
				}//for
				//Fall back on Das3
				for (Iterator itr = submitters.iterator(); itr.hasNext();) {
					is = (ISubmitter) itr.next();
					if( (is.getMetric() == 10) && (is.getAvailableSlots() > 0) )
						return is;
				}
				
			}//sync
			//put task back on queue
			
			boolean pending = false;
			Vector tgToDel = new Vector();
			for(Iterator itr = this.TGrids.iterator();itr.hasNext();){
				
				TGridHandler tg = (TGridHandler)itr.next();
				if(tg.isDead() == true){
					ISubmitter tgis = this.getSubmitterByName(tg.ip);
					if(tgis != null){
						synchronized (GlobalConfiguration.submittersSync) {
							this.submitters.remove(tgis);
						}
					}
					tgToDel.add(tg);
					//this.TGrids.remove(tg);
				}
				if(tg.isPending() == true)
					pending = true;					
			}

			for(Iterator itr = tgToDel.iterator();itr.hasNext();){
				TGridHandler tg = (TGridHandler)itr.next();
				this.TGrids.remove(tg);
			}

			tgToDel.clear();
			
			
			//GlobalConfiguration.logging.log("PENDING "+pending);
			if(pending == false){
				if (GlobalConfiguration.globalCost >= this.chunk) {
					GlobalConfiguration.globalCost -= this.chunk;					
					TGridHandler tg = new TGridHandler(defaultCapacity,this.chunk);
					this.TGrids.add(tg);
					Thread t = new Thread(tg);
					t.start();
				}else{
					GlobalConfiguration.logging.log("RUN OUT OF BUDGET");
				}
			}


		return null;
	}
	@Override
	public void submitterAdded(ISubmitter submitter){

	}

	private ISubmitter getSubmitterByName(String submitterName) {
		if(submitterName.isEmpty() == true)
			return null;
		for (Iterator itr = submitters.iterator(); itr.hasNext();) {
			ISubmitter is = (ISubmitter) itr.next();
			GlobalConfiguration.logging.log("TRYING TO MATCH "+is.getName()+" "+submitterName);
			//if ((is.getName().contains(submitterName)) && (is.getMetric() >=0)) {
			if (is.getName().contains(submitterName)) {
				GlobalConfiguration.logging.log("MATCHED "+is.getName()+" "+submitterName);
				return is;
			}
		}
		return null;
	}
}
