/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package submitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author reggie
 */
public class ResourceManager {
	private static ResourceManager instance = null;
	private ArrayList<ISubmitter> resourceList = new ArrayList();
	private static ResourceManager classInstance = null;
	private int index = 0;
	private final Object sync = new Object();


	public static ResourceManager getClassInstance(){
		if(ResourceManager.classInstance == null){
			ResourceManager.classInstance = new ResourceManager();
		}
		return ResourceManager.classInstance;
	}

	public Collection getSubmitters(){
		return this.resourceList;
	}

	public void addSubmitter(ISubmitter submitter){
		synchronized(sync){
			resourceList.add(submitter);
			System.out.println("added submitter: "+submitter.getName());
		}
	}

	public ISubmitter getASubmitter(){
		Iterator itr = resourceList.iterator();
		while(itr.hasNext()){
			ISubmitter is = (ISubmitter)itr.next();
			if(is.getAvailableSlots() > 0){
				is.decSlot();
				return is;
			}
		}
		return null;
		/*ISubmitter ret = resourceList.get(index);
		if(index >= resourceList.size() -1)
			index = 0;
		else
			index++;
		return ret;*/
	}
}
