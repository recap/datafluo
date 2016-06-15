/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.core.engine;

import nl.wtcw.vle.datafluo.lang.VLEWorkflowParser;
import nl.wtcw.vle.datafluo.context.Context;
import nl.wtcw.vle.datafluo.core.flow.Flow;
import nl.wtcw.vle.wfd.TopologyI;
import org.apache.log4j.Logger;

/**
 * 
 * @author S. Koulouzis
 */
public class VLEEngine implements Engine<TopologyI>
{
	private static Logger logger = Logger.getLogger(VLEEngine.class);
	private Flow flow = null;
	private Context context = null;

	public VLEEngine(Context context){
		logger.debug("VLEEngine.<init>");
		this.context = context;		
	}	
	
    public String compile(TopologyI topology) {
		try {
			VLEWorkflowParser parser = new VLEWorkflowParser(context);
			flow = parser.parse(topology);
			return flow.getFlowId();
		} catch (Exception ex) {
			logger.error(ex);
		}
		return null;
    }

	public void run() {
		/*try{
			
			final Flow finalFlow = this.flow;
			Thread thread = new Thread() {
				@Override
				public void run() {
					finalFlow.run();
				}
			};
			thread.start();
		
		}catch(Exception ex){
			logger.error("Unknown General Exception while running workflow: "+flow.getFlowId(), ex);
		}*/
		this.flow.run();
		
	}

	public void stop() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void pause() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void resume() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String getContextId() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String getStatus() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isRunning() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isStopped() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isPaused() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	//public void addWorkflowStateListener(WorkflowStateListener workflowStateListener) {
	//	throw new UnsupportedOperationException("Not supported yet.");
	//}

	//public void removeWorkflowStateListener(WorkflowStateListener workflowStateListener) {
	//	throw new UnsupportedOperationException("Not supported yet.");
	//}

	public Flow getFlow() {
		return this.flow;
	}
	
}

