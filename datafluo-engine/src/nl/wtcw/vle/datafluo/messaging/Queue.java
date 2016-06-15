/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.messaging;

import java.util.*;

/**
 *
 * @author reggie
 */
public class Queue {

	public final static int MESSAGE_RECEIVED_EVENT = 1;
	public final static int MESSAGE_CONSUMED_EVENT = 2;
	public final static int MESSAGE_READ_EVENT = 3;
	public final static int QUEUE_TYPE_CLEAR = 0;
	public final static int QUEUE_TYPE_SHARED = 1;
	public final static int QUEUE_TYPE_SHADOW = 2;
	private MessageExchange messageExchange;
	private final LinkedList eventListeners = new LinkedList();
	private LinkedList list = new LinkedList();
	private Key key;
	private long lastTimeCheck = 0;
	private long timeQuantum = 0;
	private int cursor = 0;
	private int maxCounter = 0;
	private int attachedConsumers = 1;
	private int type = Queue.QUEUE_TYPE_CLEAR;
	private boolean debugOn = true;
	public final Object syncEvents = new Object();

	public Queue(String queueId, MessageExchange messageExchange, Key key) {
		this.messageExchange = messageExchange;
		this.key = key;
	}

	public Queue(MessageExchange messageExchange, Key key) {
		this.messageExchange = messageExchange;		
		this.eventListeners.add(messageExchange);
		
		
		this.key = key;
	}

	public synchronized void incAttachedConsumers() {
		this.attachedConsumers++;
	}

	public int getAttachedConsumers() {
		return this.attachedConsumers;
	}

	public synchronized void addEventListener(IQueueEventListener listener) {
		synchronized(eventListeners){
			this.eventListeners.add(listener);
		}
	}

	public void setQueueType(int type) {
		this.type = type;
	}

	public int getQueueType() {
		return this.type;
	}

	public LinkedList getList() {
		return this.list;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Key getKey() {
		return this.key;
	}

	public synchronized void putFromShadow(Message message) {
		if (this.list.contains(message) == false) {
			this.list.add(message);
			this.cursor++;
		}
	}

	public synchronized int getCursor() {
		return this.cursor;
	}

	public synchronized int getMaxCounter() {
		return this.maxCounter;
	}

	public synchronized void putAll(Collection col) {
		
		this.list.addAll(col);
		this.maxCounter += col.size();
		for (Iterator itr = col.iterator(); itr.hasNext();) {
			Message msg = (Message) itr.next();
			queueRaisedEvent(new QueueRaisedEvent(this, msg, Queue.MESSAGE_RECEIVED_EVENT));
		}
		

	}

	public synchronized void put(Message message) {
		this.list.add(message);
		this.maxCounter++;
		queueRaisedEvent(new QueueRaisedEvent(this, message, Queue.MESSAGE_RECEIVED_EVENT));

	}

	public synchronized void rePut(Message message) {
		this.list.addFirst(message);
	}

	public synchronized Message get() {
		long time = System.currentTimeMillis();
		if (this.lastTimeCheck == 0) {
			this.lastTimeCheck = time;
		}

		if (this.timeQuantum != 0) {
			this.timeQuantum = (this.timeQuantum + (time - this.lastTimeCheck)) / 2;
		} else {
			this.timeQuantum = time - this.lastTimeCheck;
		}

		this.lastTimeCheck = time;
		Message message = (Message) this.list.removeFirst();
		queueRaisedEvent(new QueueRaisedEvent(this, message, Queue.MESSAGE_CONSUMED_EVENT));

		return message;
	}

	public synchronized Message get(int cloneNumber) {
		long time = System.currentTimeMillis();
		if (this.lastTimeCheck == 0) {
			this.lastTimeCheck = time;
		}

		if (this.timeQuantum != 0) {
			this.timeQuantum = (this.timeQuantum + (time - this.lastTimeCheck)) / 2;
		} else {
			this.timeQuantum = time - this.lastTimeCheck;
		}

		this.lastTimeCheck = time;
		Message message = (Message) this.list.removeFirst();
		message.setAux(cloneNumber);
		queueRaisedEvent(new QueueRaisedEvent(this, message, Queue.MESSAGE_CONSUMED_EVENT));

		return message;
	}

	@Deprecated
	public synchronized Message getNoRemove() {
		long time = System.currentTimeMillis();
		this.timeQuantum = (this.timeQuantum + (time - this.lastTimeCheck)) / 2;
		this.lastTimeCheck = time;
		Message m = (Message) this.list.removeFirst();
		this.list.add(m);
		return m;
	}

	public synchronized double calcLoad() {
		double load = Math.ceil((timeQuantum / 1000) * this.getSize());
		return load;
	}

	public int getSize() {
		return this.list.size();
	}

	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	public synchronized void queueRaisedEvent(QueueRaisedEvent queueRasiedEvent) {
		synchronized(eventListeners){
			LinkedList tmp = new LinkedList();
			tmp.addAll(this.eventListeners);
			for (Iterator itr = tmp.iterator(); itr.hasNext();) {
				IQueueEventListener l = (IQueueEventListener) itr.next();
				l.queueRaisedEvent(queueRasiedEvent);
			}
			
		}
		
	}
}
