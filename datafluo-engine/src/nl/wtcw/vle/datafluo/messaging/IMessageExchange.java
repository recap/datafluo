/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.messaging;

import nl.wtcw.vle.datafluo.core.port.VLEPort;
import java.util.*;

/**
 *
 * @author reggie
 */
public interface IMessageExchange<P> {

	public String getMessageExchangeId();

	public Queue createMessageQueue(VLEPort port);

	public Queue createMessageShadowQueue(VLEPort port);

	public void destroyMessageQueue(Key key);

	public void createLink(Queue outQueue, Queue inQueue);

	public void createLink(VLEPort inPort, VLEPort outPort);

	public void destroyLink(Queue outQueue, Queue inQueue);

	public Collection getLinks(Queue outQueue);
}
