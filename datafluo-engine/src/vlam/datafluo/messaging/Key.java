/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.messaging;

/**
 *
 * @author reggie
 */
public class Key {

	public String messageExchangeId;
	public String taskId;
	public String portId;

	public Key(String messageExchangeId, String taskId, String portId) {
		this.messageExchangeId = messageExchangeId;
		this.taskId = taskId;
		this.portId = portId;
	}

	@Override
	public String toString() {
		String nl = System.getProperty("line.separator");
		StringBuffer sb = new StringBuffer();
		sb.append(messageExchangeId + "." + taskId + "." + portId);

		return sb.toString();
	}
}
