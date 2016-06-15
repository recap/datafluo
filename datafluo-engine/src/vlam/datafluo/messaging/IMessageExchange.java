/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.messaging;

import vlam.datafluo.wsengine.*;
import java.util.*;

/**
 *
 * @author reggie
 */
public interface IMessageExchange {

	public String getMessageExchangeId();

	public Queue createMessageQueue(VlamDatafluoPort port);

	public void destroyMessageQueue(Key key);

	public void createLink(Queue outQueue, Queue inQueue);

	public void destroyLink(Queue outQueue, Queue inQueue);

	public Collection getLinks(Queue outQueue);
}
