/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.wsengine;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JTextArea;

import uk.ac.soton.itinnovation.freefluo.core.flow.Flow;
import uk.ac.soton.itinnovation.freefluo.event.WorkflowStateChangedEvent;
import uk.ac.soton.itinnovation.freefluo.event.WorkflowStateListener;
import uk.ac.soton.itinnovation.freefluo.main.FlowContext;
import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;
import uk.ac.soton.itinnovation.freefluo.main.UnknownProcessorException;
import uk.ac.soton.itinnovation.freefluo.main.WorkflowInstance;
import uk.ac.soton.itinnovation.freefluo.main.WorkflowState;

/**
 * 
 * @author alogo
 */
class VlamWorkflowInstance implements WorkflowInstance {

	private Flow flow;
	private HashSet<WorkflowStateListener> workflowStateListeners = new HashSet<WorkflowStateListener>();
	private static boolean debugOn = false;

	public VlamWorkflowInstance(Flow flow) {
		this.flow = flow;
	}

	public Flow getFlow() {
		return this.flow;
	}

	public FlowContext getFlowContext() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setFlowContext(FlowContext fc) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setInput(Map map) throws InvalidInputException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void addWorkflowStateListener(WorkflowStateListener wl) {
		workflowStateListeners.add(wl);
	}

	public void removeWorkflowStateListener(WorkflowStateListener wl) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void run() {
		debug("Start running: " + this.flow.getFlowId());

		Iterator<WorkflowStateListener> iter = workflowStateListeners.iterator();

		while (iter.hasNext()) {
			WorkflowStateListener listener = iter.next();
			listener.workflowStateChanged(new WorkflowStateChangedEvent(flow.getFlowId(), WorkflowState.RUNNING));
		}
		//Start the flow. What this does, it to get all tasks in this flow,
		//create a thread for them and start running them. So all the functionality
		//is at the VlamProcessorTask.handleRun
		this.flow.run();
	}

	private void debug(String msg) {
		if (debugOn) {
			System.err.println(this.getClass().getName() + ": " + msg);
		}
	}

	public String getStatus() {
		return flow.getStateString();
	}

	public Map[] getIntermediateResultsForProcessor(String string) throws UnknownProcessorException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Map getOutput() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String getProvenanceXML() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String getProgressReportXML() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String getErrorMessage() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void cancel() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean pause() {
		Iterator<WorkflowStateListener> iter = workflowStateListeners.iterator();

		while (iter.hasNext()) {
			WorkflowStateListener listener = iter.next();
			listener.workflowStateChanged(new WorkflowStateChangedEvent(flow.getFlowId(), WorkflowState.PAUSED));
		}
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean resume() {
		Iterator<WorkflowStateListener> iter = workflowStateListeners.iterator();

		while (iter.hasNext()) {
			WorkflowStateListener listener = iter.next();
			listener.workflowStateChanged(new WorkflowStateChangedEvent(flow.getFlowId(), WorkflowState.RUNNING));
		}
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void destroy() {
		Iterator<WorkflowStateListener> iter = workflowStateListeners.iterator();

		while (iter.hasNext()) {
			WorkflowStateListener listener = iter.next();
			listener.workflowStateChanged(new WorkflowStateChangedEvent(flow.getFlowId(), WorkflowState.DESTROYED));
		}
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isDataNonVolatile(String string) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean changeOutputPortTaskData(String string, String string1, Object o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	void addStdOutput(JTextArea consoleTextArea) {
		/*Iterator iter = flow.getTasks().iterator();
		while (iter.hasNext()) {
		VlamProcessorTask task = (VlamProcessorTask) iter.next();
		task.addStdOutput(consoleTextArea);
		}*/
	}
}
