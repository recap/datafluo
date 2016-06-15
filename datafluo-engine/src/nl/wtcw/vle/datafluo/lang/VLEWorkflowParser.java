/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.lang;

import nl.wtcw.vle.datafluo.core.port.PortConnectionException;
import nl.wtcw.vle.datafluo.core.task.VLETask;
import java.beans.XMLDecoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import nl.wtcw.vle.datafluo.context.Context;

import nl.wtcw.vle.wfd.ConnectionI;
import nl.wtcw.vle.wfd.ModuleI;
import nl.wtcw.vle.wfd.PortDescrI;
import nl.wtcw.vle.wfd.TopologyI;
import nl.wtcw.vle.wfd.impl.Axis.ParameterAxis;
import nl.wtcw.vle.datafluo.messaging.Queue;
import nl.wtcw.vle.datafluo.core.flow.Flow;
import nl.wtcw.vle.datafluo.core.port.Port;
import nl.wtcw.vle.datafluo.core.task.AbstractTask;

import nl.wtcw.vle.datafluo.core.port.VLEPort;
import nl.wtcw.vle.wfd.CategoryI;
import org.apache.log4j.Logger;

/**
 * 
 * @author alogo
 */
public class VLEWorkflowParser implements WorkflowParser<TopologyI> {


	private static Logger logger = Logger.getLogger(VLEWorkflowParser.class);
	private Context context;
	
	private HashMap<String, AbstractTask> taskMap = new HashMap<String, AbstractTask>();
	private HashMap<String, ModuleI> moduleMap = new HashMap<String, ModuleI>();

	public VLEWorkflowParser(Context context){
		logger.debug("VLEWorkflowParser.<init>");
		this.context = context;

	}

	public Flow parse(TopologyI topology) throws ParsingException {
		String id = Integer.toString(topology.hashCode());
		Flow flow = new Flow(id);

		try {

			//topology = parceFromFile(path);
			createTasks(topology, flow);
			createLinks(topology);

			if(logger.isDebugEnabled()){

			logger.debug("<------------Instanceof TopologyI---------------->");
			for (Iterator itr = flow.getTasks().iterator(); itr.hasNext();) {
				VLETask task = (VLETask) itr.next();
				for (Iterator itr2 = task.getOutputPorts().iterator(); itr2.hasNext();) {
					VLEPort port = (VLEPort) itr2.next();
					for (Iterator itr3 = port.getConnections().iterator(); itr3.hasNext();) {
						VLEPort port2 = (VLEPort) itr3.next();
						VLETask task2 = (VLETask) port2.getTask();
						logger.debug(task.getName() + ":" + port.getPortId() + " connected to "
								+ task2.getName() + ":" + port2.getPortId());

					}
				}
			}
			}


		} catch (Exception ex) {
			logger.error(ex);
		}

		return flow;
	}

	@Deprecated
	private TopologyI parceFromFile(String path) throws FileNotFoundException {
		XMLDecoder decoder = new XMLDecoder(new FileInputStream(path));
		Object obj = decoder.readObject();
		logger.debug("Cast from " + obj.getClass().getName());
		return (TopologyI) obj;
	}

	private void createTasks(TopologyI topology, Flow flow) throws URISyntaxException {
		VLETask task;
		List<ModuleI> wfModules = topology.getModules();

		

		for (int i = 0; i < wfModules.size(); i++) {
			ModuleI module = wfModules.get(i);
			String taskId = padId(module.getInstanceID());
			
			task = new VLETask(taskId, module.getName(), flow, context, false);

			for(Iterator itr=module.getCategory().iterator();itr.hasNext();){
				CategoryI category = (CategoryI)itr.next();
				if(category.getCategoryName().equalsIgnoreCase(VLETask.NODESERVICE_CAT)){
					task.setCategory(VLETask.NODESERVICE_CAT);
				}

				if(category.getCategoryName().equalsIgnoreCase(VLETask.VLEHARNESS_CAT)){
					task.setCategory(VLETask.VLEHARNESS_CAT);
				}
			}
					
			if (isParameterExist(module, "_farmtype") == true) {
				task.setFarmType(Integer.parseInt(getParameterValue(module, "_farmtype")));
			}

			if (isParameterExist(module, "_host") == true) {
				task.setHost(getParameterValue(module, "_host"), true);
			}
			
			if (isParameterExist(module, "_budget") == true) {
				task.setBudget(Long.parseLong(getParameterValue(module, "_budget")));
			}

			taskMap.put(task.getTaskId(), task);
			moduleMap.put(module.getInstanceID(), module);
		}
	}

	private String padId(String id){
		String lid = id;
		if(lid.length() < 10){
			int diff = 10 - lid.length();
			for(int j=0;j<diff;j++){
				lid = lid+"0";
			}
		}
		if(lid.length() > 10){
			lid = lid.substring(0, 10);
		}
		return lid;
	}
	private boolean isParameterExist(ModuleI module, String parameter) {
		for (Iterator itr = module.getParameters().iterator(); itr.hasNext();) {
			ParameterAxis pa = (ParameterAxis) itr.next();
			if (pa.getName().contains(parameter)) {
				return true;
			}
		}
		return false;
	}

	private String getParameterValue(ModuleI module, String parameter) {
		for (Iterator itr = module.getParameters().iterator(); itr.hasNext();) {
			ParameterAxis pa = (ParameterAxis) itr.next();			
			if (pa.getName().contains(parameter)) {
				return pa.getValue();
			}
		}
		return null;
	}

	private void createLinks(TopologyI topology) throws URISyntaxException, PortConnectionException {
		List<ConnectionI> connections = topology.getConnections();


		String from;
		String to;
		VLETask fromTask;
		VLETask toTask;		
		ModuleI toModule;
		PortDescrI fromConnection;
		PortDescrI toConnection;


		for (int i = 0; i < connections.size(); i++) {			
			fromConnection = connections.get(i).getFrom();
			toConnection = connections.get(i).getTo();
			from = fromConnection.getInstanceID();
			to = toConnection.getInstanceID();

			fromTask = (VLETask) taskMap.get(padId(from));
			toTask = (VLETask) taskMap.get(padId(to));
			
			toModule = (ModuleI) moduleMap.get(to);

			if (fromTask.containsOutputPort(fromConnection.getName()) == false) {
				VLEPort port = new VLEPort(padId(from),fromConnection.getName(), Port.DIRECTION_OUT, fromTask);
				context.getMessageExchange().createMessageQueue(port);
			}
			if (toTask.containsInputPort(toConnection.getName()) == false) {
				VLEPort port = new VLEPort(padId(to),toConnection.getName(), Port.DIRECTION_IN, toTask);
				if (isParameterExist(toModule, "_farm_" + port.getName())) {
					toTask.setFarmCount(Integer.parseInt(getParameterValue(toModule, "_farm_" + port.getName())));
					toTask.setDesignatedDataPartitionPort(port);
				}
				context.getMessageExchange().createMessageQueue(port);
			}

			try {
				fromTask.linkTo(fromTask.getOutputPort(padId(from)),
						toTask.getInputPort(padId(to)));
				context.getMessageExchange().createLink(fromTask.getOutputPort(padId(from)),
						toTask.getInputPort(padId(to)));
				//Create shadow queues for farmed tasks
				if (toTask.isFarmed()) {
					VLEPort port = toTask.getInputPort(padId(to));
					if (port.equals(toTask.getDesignatedDataPartitionPort()) == false) {
						Queue queue = context.getMessageExchange().createMessageShadowQueue(port);
						context.getMessageExchange().createLink(fromTask.getOutputPort(padId(from)).getQueue(), queue);
					}
				}

			} catch (PortConnectionException ex) {
				logger.error(ex);
			}

		}
	}	

}
