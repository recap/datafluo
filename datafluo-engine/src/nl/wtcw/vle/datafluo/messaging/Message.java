/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.messaging;

/**
 *
 * @author reggie
 */
public class Message {

	private String contextId;
	private String source;
	private String destination;
	private long dataLength;
	private int sequenceNumber;
	private String message;
	private int aux = -1;

	public Message(String source, String destination, long dataLength, int sequenceNumber, String message) {
		this.source = source;
		this.destination = destination;
		this.dataLength = dataLength;
		this.sequenceNumber = sequenceNumber;
		this.message = message;
	}

	public void setAux(int aux){
		this.aux = aux;
	}

	public int getAux(){
		return this.aux;
	}

	public Message() {
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return this.source;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getDestination() {
		return this.destination;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	public long getDataLength() {
		return this.dataLength;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public int getSequenceNumber() {
		return this.sequenceNumber;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

	@Override
	public String toString() {
		String nl = System.getProperty("line.separator");
		StringBuffer sb = new StringBuffer();
		sb.append("Message: " + nl);
		sb.append("\tsource: " + source + nl);
		sb.append("\tdestination: " + destination + nl);
		sb.append("\tlength: " + dataLength + nl);
		sb.append("\tseq: " + sequenceNumber + nl);
		sb.append("\tmessage: " + message + nl);
		return sb.toString();
	}
}
