/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.reactor;

/**
 *
 * @author reggie
 */
public class ServerEntry {

	private String serverId;
	private String serverType;
	private int metric;
	private String parameters;

	public ServerEntry(String serverId, String serverType, int metric, String parameters) {
		this.serverId = serverId;
		this.serverType = serverType;
		this.metric = metric;
		this.parameters = parameters;

	}

	public ServerEntry(String serverId, String serverType, String parameters) {
		this(serverId, serverType, 0, parameters);
	}

	public ServerEntry(String serverId, String serverType) {
		this(serverId, serverType, 0, "");
	}

	public String getServerId() {
		return this.serverId;
	}

	public String getServerType() {
		return this.serverType;
	}

	public String getParameters() {
		return this.parameters;
	}

	public int getMetric() {
		return this.metric;
	}
}
