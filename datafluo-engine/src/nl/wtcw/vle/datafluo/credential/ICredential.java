/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.datafluo.credential;

/**
 *
 * @author reggie
 */
public interface  ICredential<C> {
	public String getName();
	public C getCredential();	
}
