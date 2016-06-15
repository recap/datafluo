/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.datafluo.credential;

import org.ietf.jgss.GSSCredential;

/**
 *
 * @author reggie
 */
public class GlobusCredential implements ICredential<GSSCredential>{

	private String name;
	private GSSCredential credential;

	public GlobusCredential(String name, GSSCredential credential){
		this.name = name;
		this.credential = credential;
	}

	public String getName() {
		return this.name;
	}

	public GSSCredential getCredential() {
		return this.credential;
	}
}
