/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vlam.datafluo.wsengine;

import uk.ac.soton.itinnovation.freefluo.core.event.FlowStateListener;
import uk.ac.soton.itinnovation.freefluo.core.event.PortStateListener;
import uk.ac.soton.itinnovation.freefluo.core.event.TaskStateListener;

/**
 *
 * @author reggie
 */
public interface IStateMonitor extends FlowStateListener, TaskStateListener, PortStateListener{

}
