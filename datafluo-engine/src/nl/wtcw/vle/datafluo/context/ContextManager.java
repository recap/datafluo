/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.datafluo.context;

import java.util.HashMap;
import java.util.UUID;
import org.apache.log4j.Logger;

/**
 *
 * @author reggie
 */
public class ContextManager {
	private static ContextManager instance = null;
	private HashMap<String, Context> contextMap = new HashMap();
	private static Logger logger = Logger.getLogger(ContextManager.class);

	public ContextManager(){
		logger.debug("ContextManager initialized");
	}
	public static ContextManager getInstance(){
		if(instance == null){
			instance = new ContextManager();
		}
		return instance;
	}

	public String newContext(){
		String id = UUID.randomUUID().toString();
		contextMap.put(id, new Context(id));
		logger.debug("Added new context id: " + id);
		return id;
	}
	
	public Context getContext(String contextId){
		Context context = contextMap.get(contextId);
		//TODO throw exception if null
		return context;
	}

}
