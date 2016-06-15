/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.messaging;

import nl.wtcw.vle.datafluo.core.port.VLEPort;

import java.util.*;
import nl.wtcw.vle.datafluo.context.Context;

/**
 *
 * @author reggie
 */
public class MessageExchange implements IMessageExchange, IQueueEventListener {

	private HashMap exchange = new HashMap();
	private HashMap links = new HashMap();
	private String messageExchangeId;
	private Context context;

	public MessageExchange(Context context, String messageExchangeId) {
		this.messageExchangeId = messageExchangeId;
		this.context = context;		
	}

	public MessageExchange(Context context) {
		this.messageExchangeId = this.hashCode() + "ID";
		this.context = context;		
	}

	public String getMessageExchangeId() {
		return this.messageExchangeId;
	}

	public synchronized Queue createMessageShadowQueue(VLEPort port) {
		Key key = new Key(this.messageExchangeId, port.getTask().getTaskId(), port.getPortId() + "_SHADOW");
		Queue queue = new Queue(this, key);
		queue.setQueueType(Queue.QUEUE_TYPE_SHADOW);
		this.exchange.put(key.toString(), queue);

		port.setShadowQueue(queue);
		return queue;
	}

	public synchronized Queue createMessageQueue(VLEPort port) {
		Key key = new Key(this.messageExchangeId, port.getTask().getTaskId(), port.getPortId());
		Queue queue = new Queue(this, key);
		this.exchange.put(key.toString(), queue);

		port.setQueue(queue);
		return queue;
	}

	public synchronized Queue createMessageQueue(VLEPort port, int queueType) {
		Key key = new Key(this.messageExchangeId, port.getTask().getName(), port.getPortId());
		Queue queue = new Queue(this, key);
		this.exchange.put(key.toString(), queue);
		queue.setQueueType(queueType);

		port.setQueue(queue);
		return queue;
	}

	public synchronized void destroyMessageQueue(Key key) {
		this.exchange.remove(key.toString());
	}

	public synchronized Queue getMessageQueue(Key key) {
		return (Queue) this.exchange.get(key.toString());
	}

	public synchronized Queue getMessageQueue(String key) {
		return (Queue) this.exchange.get(key);
	}

	public synchronized Queue getMessageShadowQueue(String key) {
		return (Queue) this.exchange.get(key + "_SHADOW");
	}

	public synchronized Queue getMessageShadowQueue(Key key) {
		return (Queue) this.exchange.get(key.toString() + "_SHADOW");
	}

	public synchronized void createLink(Queue outQueue, Queue inQueue) {
		if (links.containsKey(outQueue.getKey().toString()) == true) {
			Vector tmpVector = (Vector) links.get(outQueue.getKey().toString());
			tmpVector.add(inQueue);
		} else {
			Vector tmpVector = new Vector();
			tmpVector.add(inQueue);
			this.links.put(outQueue.getKey().toString(), tmpVector);
		}

		for (Iterator itr = outQueue.getList().iterator(); itr.hasNext();) {
			Message message = (Message) itr.next();
			outQueue.queueRaisedEvent(new QueueRaisedEvent(outQueue, message, Queue.MESSAGE_RECEIVED_EVENT));
		}

	}

	public synchronized void createLink(VLEPort outPort, VLEPort inPort) {
		if (links.containsKey(outPort.getQueue().getKey().toString()) == true) {
			Vector tmpVector = (Vector) links.get(outPort.getQueue().getKey().toString());
			tmpVector.add(inPort.getQueue());
		} else {
			Vector tmpVector = new Vector();
			tmpVector.add(inPort.getQueue());
			this.links.put(outPort.getQueue().getKey().toString(), tmpVector);
		}
		for (Iterator itr = outPort.getQueue().getList().iterator(); itr.hasNext();) {
			Message message = (Message) itr.next();
			outPort.getQueue().queueRaisedEvent(new QueueRaisedEvent(outPort.getQueue(), message, Queue.MESSAGE_RECEIVED_EVENT));
		}
	}

	public synchronized void destroyLink(Queue outQueue, Queue inQueue) {
		if (links.containsKey(outQueue.getKey().toString()) == true) {
			Vector tmpVector = (Vector) links.get(outQueue.getKey().toString());
			tmpVector.remove(inQueue);
		}

	}

	public synchronized void destroyLink(VLEPort outPort, VLEPort inPort) {
		if (links.containsKey(outPort.getQueue().getKey().toString()) == true) {
			Vector tmpVector = (Vector) links.get(outPort.getQueue().getKey().toString());
			tmpVector.remove(inPort.getQueue());
		}

	}

	public synchronized Collection getLinks(Queue outQueue) {
		if (links.containsKey(outQueue.getKey().toString()) == true) {
			Vector tmpVector = (Vector) links.get(outQueue.getKey().toString());
			return tmpVector;
		}

		return null;
	}

	/**
	 * Actual message routing
	 * @param queueRasiedEvent
	 */
	public synchronized void queueRaisedEvent(QueueRaisedEvent queueRaisedEvent) {
		if (queueRaisedEvent.getEvent() == Queue.MESSAGE_RECEIVED_EVENT) {
			Queue queue = queueRaisedEvent.getQueue();
			synchronized (queue) {
				Vector tmpVector = (Vector) links.get(queue.getKey().toString());

				if (tmpVector != null) {
					if (queue.getQueueType() == Queue.QUEUE_TYPE_CLEAR) {
						for (Iterator itr = tmpVector.iterator(); itr.hasNext();) {
							Queue tmpQueue = (Queue) itr.next();
							synchronized (tmpQueue.getList()) {
								tmpQueue.putAll(queue.getList());
							}
						}//for
						queue.getList().clear();
					}//if

					if (queue.getQueueType() == Queue.QUEUE_TYPE_SHADOW) {
						for (Iterator itr = tmpVector.iterator(); itr.hasNext();) {
							Queue tmpQueue = (Queue) itr.next();
							synchronized (tmpQueue.getList()) {
								for (Iterator litr = queue.getList().listIterator(tmpQueue.getCursor()); litr.hasNext();) {
									tmpQueue.putFromShadow((Message) litr.next());
								}//for
							}//sync
						}//for
					}

					if (queue.getQueueType() == Queue.QUEUE_TYPE_SHARED) {
						for (Iterator itr = tmpVector.iterator(); itr.hasNext();) {
							Queue tmpQueue = (Queue) itr.next();
							if (queue.getList().size() > 0) {
								synchronized (tmpQueue.getList()) {
									tmpQueue.getList().add(queue.getList().removeLast());
								}
							} else {
								break;
							}
						}//for
					}//if
				}//if
			}//synch(queue)
		}//if
		//	}//synchronized
	}//queueRaisedEvent
}
