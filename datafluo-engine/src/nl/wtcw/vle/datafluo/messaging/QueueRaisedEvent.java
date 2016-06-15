/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.messaging;

/**
 *
 * @author reggie
 */
public class QueueRaisedEvent {

	private Queue queue = null;
	private String message = null;
	private Message messageObject = null;
	private int event;

	public QueueRaisedEvent(Queue queue, Message messageObject, int event) {
		this.queue = queue;
		if (messageObject != null) {
			this.message = messageObject.getMessage();
			this.messageObject = messageObject;
		}
		this.event = event;
	}

	public Queue getQueue() {
		return this.queue;
	}

	public String getMessage() {
		return this.message;
	}

	public Message getMessageObject() {
		return this.messageObject;
	}

	public int getEvent() {
		return this.event;
	}

	@Override
	public String toString() {
		String nl = System.getProperty("line.separator");
		StringBuffer sb = new StringBuffer();
		sb.append("QueueRaisedEvent: " + nl);
		sb.append("\tqueue: " + queue.toString() + nl);
		sb.append("\tmessage: " + message);
		return sb.toString();
	}
}
