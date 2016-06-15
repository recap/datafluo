/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vlam.datafluo.reactor;

/**
 *
 * @author reggie
 */
public class ReactorCommands {
	/**
	 * signed byte max 0x7F (+127)
	 */
	public final static byte IM_ALIVE = 0x7F;
	public final static byte CHECK_MAIL = 0x7E;
	public final static byte POST_MAIL = 0x7D;
	public final static byte POST_EVENT = 0x7C;
	public final static byte GET_CONFIG = 0x7B;
	public final static byte HEART_BEAT = 0x7A;
	public final static byte COMPLETE = 0x70;

	public final static byte PORT_DESTROYED = 0x6F;
	public final static byte SENDING_MAIL = 0x6E;
	public final static byte NO_MAIL = 0x6D;

	public final static byte NO_COMMAND = 0x00;
}
