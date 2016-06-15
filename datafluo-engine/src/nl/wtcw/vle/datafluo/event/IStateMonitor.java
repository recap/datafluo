/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.datafluo.event;

import nl.wtcw.vle.datafluo.core.event.FlowStateListener;
import nl.wtcw.vle.datafluo.core.event.PortStateListener;
import nl.wtcw.vle.datafluo.core.event.TaskStateListener;

/**
 *
 * @author reggie
 */
public interface IStateMonitor extends FlowStateListener, TaskStateListener, PortStateListener{

}
