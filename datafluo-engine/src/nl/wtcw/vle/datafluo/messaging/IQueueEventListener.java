/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.datafluo.messaging;

/**
 *
 * @author reggie
 */
public interface IQueueEventListener {
	public void queueRaisedEvent(QueueRaisedEvent queueRaisedEvent);
}
