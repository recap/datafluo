/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.reactor;

import java.util.List;

/**
 *
 * @author reggie
 */
public class DataServerEntry {

	private String serverId;
	private List protocols;
	private int metric;
	private String parameters;

	public DataServerEntry(String serverId, String protocol, int metric, String parameters) {
		this.serverId = serverId;
		this.protocols.add(protocol);
		this.metric = metric;
		this.parameters = parameters;

	}

	public DataServerEntry(String serverId, String protocol, String parameters) {
		this(serverId, protocol, 0, parameters);
	}

	public DataServerEntry(String serverId, String protocol) {
		this(serverId, protocol, 0, "");
	}

	public String getServerId() {
		return this.serverId;
	}

	public List getProtocols() {
		return this.protocols;
	}

	public String getDefaultProtocol(){
		String protocol = (String)this.protocols.get(0);
		return protocol;
	}

	public String getParameters() {
		return this.parameters;
	}

	public int getMetric() {
		return this.metric;
	}
}
