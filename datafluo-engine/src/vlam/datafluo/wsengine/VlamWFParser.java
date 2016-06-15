/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.wsengine;

import java.beans.XMLDecoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;

import nl.wtcw.vle.wfd.ConnectionI;
import nl.wtcw.vle.wfd.ModuleI;
import nl.wtcw.vle.wfd.PortDescrI;
import nl.wtcw.vle.wfd.TopologyI;
import nl.wtcw.vle.wfd.impl.Axis.ParameterAxis;
import vlam.datafluo.messaging.Queue;
import vlam.datafluo.utils.GlobalConfiguration;
import uk.ac.soton.itinnovation.freefluo.core.flow.Flow;
import uk.ac.soton.itinnovation.freefluo.core.task.AbstractTask;
import uk.ac.soton.itinnovation.freefluo.main.*;
import uk.ac.soton.itinnovation.freefluo.main.WorkflowInstance;

import uk.ac.soton.itinnovation.freefluo.lang.*;
import uk.ac.soton.itinnovation.freefluo.core.port.*;

/**
 * 
 * @author alogo
 */
public class VlamWFParser implements WorkflowParser {

	private XMLDecoder decoder;
	private TopologyI topology;
	private VlamEngine engine;
	private HashMap<String, AbstractTask> taskMap = new HashMap<String, AbstractTask>();
	private HashMap<String, ModuleI> moduleMap = new HashMap<String, ModuleI>();

	@Override
	public WorkflowInstance parse(Engine engine, String path) throws ParsingException {

		String flowId = this.hashCode() + "ID";
		Flow flow = new Flow(flowId, engine);

		this.engine = (VlamEngine) engine;

		try {

			topology = parceFromFile(path);

			createTasks(topology, flow);
			createLinks(topology);

			for (Iterator itr = flow.getTasks().iterator(); itr.hasNext();) {
				VlamDatafluoTask task = (VlamDatafluoTask) itr.next();
				for (Iterator itr2 = task.getOutputPorts().iterator(); itr2.hasNext();) {
					VlamDatafluoPort port = (VlamDatafluoPort) itr2.next();
					for (Iterator itr3 = port.getConnections().iterator(); itr3.hasNext();) {
						VlamDatafluoPort port2 = (VlamDatafluoPort) itr3.next();
						VlamDatafluoTask task2 = (VlamDatafluoTask) port2.getTask();
						GlobalConfiguration.logging.debug(task.getModule().getName() + ":" + port.getPortId() + " connected to "
								+ task2.getModule().getName() + ":" + port2.getPortId());
					}
				}
			}


		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return new VlamWorkflowInstance(flow);
	}

	public WorkflowInstance parse(Engine engine, TopologyI topology) throws ParsingException {

		String flowId = this.hashCode() + "ID";
		Flow flow = new Flow(flowId, engine);

		this.engine = (VlamEngine) engine;

		try {

			//topology = parceFromFile(path);
			createTasks(topology, flow);
			createLinks(topology);

			for (Iterator itr = flow.getTasks().iterator(); itr.hasNext();) {
				VlamDatafluoTask task = (VlamDatafluoTask) itr.next();
				for (Iterator itr2 = task.getOutputPorts().iterator(); itr2.hasNext();) {
					VlamDatafluoPort port = (VlamDatafluoPort) itr2.next();
					for (Iterator itr3 = port.getConnections().iterator(); itr3.hasNext();) {
						VlamDatafluoPort port2 = (VlamDatafluoPort) itr3.next();
						VlamDatafluoTask task2 = (VlamDatafluoTask) port2.getTask();
						GlobalConfiguration.logging.debug(task.getModule().getName() + ":" + port.getPortId() + " connected to "
								+ task2.getModule().getName() + ":" + port2.getPortId());
					}
				}
			}


		} catch (Exception e) {
			e.printStackTrace();
		}

		return new VlamWorkflowInstance(flow);
	}

	private TopologyI parceFromFile(String path) throws FileNotFoundException {
		decoder = new XMLDecoder(new FileInputStream(path));
		Object obj = decoder.readObject();
		GlobalConfiguration.logging.debug("Cast from " + obj.getClass().getName());
		return (TopologyI) obj;
	}

	private void createTasks(TopologyI topology, Flow flow) throws URISyntaxException {
		VlamDatafluoTask task;
		List<ModuleI> wfModules = topology.getModules();

		GlobalConfiguration.logging.debug("------------Instanceof TopologyI----------------");

		for (int i = 0; i < wfModules.size(); i++) {


			ModuleI module = wfModules.get(i);


			task = new VlamDatafluoTask(UUID.randomUUID().toString(), flow, module);
			task.setInstanceID(module.getInstanceID());
			task.setName(module.getName());
			if (isParameterExist(module, "_farm") == true) {
				task.setFarmCount(0);
			}
			if (isParameterExist(module, "_farmtype") == true) {
				task.setFarmType(Integer.parseInt(getParameterValue(module, "_farmtype")));
			}

			if (isParameterExist(module, "_host") == true) {
				task.setHost(getParameterValue(module, "_host"), true);
			}

			taskMap.put(module.getInstanceID(), task);
			moduleMap.put(module.getInstanceID(), module);
		}
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

	private void createLinks(TopologyI topology) throws URISyntaxException {
		List<ConnectionI> connections = topology.getConnections();


		String from;
		String to;
		VlamDatafluoTask fromTask;
		VlamDatafluoTask toTask;
		ModuleI fromModule;
		ModuleI toModule;
		PortDescrI fromConnection;
		PortDescrI toConnection;


		for (int i = 0; i < connections.size(); i++) {
			// GlobalConfiguration.logging.debug("Connection["+i+"]");
			fromConnection = connections.get(i).getFrom();
			toConnection = connections.get(i).getTo();
			from = fromConnection.getInstanceID();
			to = toConnection.getInstanceID();

			fromTask = (VlamDatafluoTask) taskMap.get(from);
			toTask = (VlamDatafluoTask) taskMap.get(to);

			fromModule = (ModuleI) moduleMap.get(from);
			toModule = (ModuleI) moduleMap.get(to);

			if (fromTask.containsOutputPort(fromConnection.getName()) == false) {
				VlamDatafluoPort port = new VlamDatafluoPort(fromConnection.getName(), Port.DIRECTION_OUT, fromTask);
				port.setInstanceID(from);
				this.engine.messageExchange.createMessageQueue(port);
			}
			if (toTask.containsInputPort(toConnection.getName()) == false) {
				VlamDatafluoPort port = new VlamDatafluoPort(toConnection.getName(), Port.DIRECTION_IN, toTask);
				port.setInstanceID(to);
				if (isParameterExist(toModule, "_farm_" + port.getName())) {
					toTask.setFarmCount(Integer.parseInt(getParameterValue(toModule, "_farm_" + port.getName())));
					toTask.setFarmedPort(port);
				}
				this.engine.messageExchange.createMessageQueue(port);
			}

			try {
				fromTask.linkTo(fromTask.getOutputPort(fromConnection.getName()),
						toTask.getInputPort(toConnection.getName()));
				this.engine.messageExchange.createLink(fromTask.getOutputPort(fromConnection.getName()),
						toTask.getInputPort(toConnection.getName()));
				//Create shadow queues for farmed tasks
				if (toTask.isFarmed()) {
					VlamDatafluoPort port = toTask.getInputPort(toConnection.getName());
					if (port.equals(toTask.getFarmedPort()) == false) {
						Queue queue = this.engine.messageExchange.createMessageShadowQueue(port);
						this.engine.messageExchange.createLink(fromTask.getOutputPort(fromConnection.getName()).getQueue(), queue);
					}
				}

			} catch (PortConnectionException ex) {
				Logger.getLogger(VlamWFParser.class.getName()).log(Level.SEVERE, null, ex);
			}

		}
	}

	public WorkflowDescription describe(String workflowDefinition) throws ParsingException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
