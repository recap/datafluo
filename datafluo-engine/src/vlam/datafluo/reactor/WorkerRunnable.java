/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vlam.datafluo.reactor;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import vlam.datafluo.utils.GlobalConfiguration;

/**
 *
 * @author reggie
 * based on http://tutorials.jenkov.com/java-multithreaded-servers/multithreaded-server.html
 */
public class WorkerRunnable implements Runnable {

	protected Socket clientSocket = null;
	private CommandHandler cmdHandler = null;

	WorkerRunnable(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {
		try {
			cmdHandler = new CommandHandler(clientSocket);
			byte cmd = ReactorCommands.NO_COMMAND;
			byte[] buf = new byte[1];

			clientSocket.getInputStream().read(buf, 0, 1);
			cmd = buf[0];

			switch (cmd) {
				case ReactorCommands.CHECK_MAIL:
					//GlobalConfiguration.logging.debug("CHECK_MAIL");
					cmdHandler.handleCHECK_MAIL();
					break;
				case ReactorCommands.GET_CONFIG:
					GlobalConfiguration.logging.debug("GET_CONFIG");
					byte[] id = new byte[41];
					clientSocket.getInputStream().read(id, 0, 41);
					String ids = new String(id);
					cmdHandler.handleGET_CONFIG(ids);
					break;
				case ReactorCommands.COMPLETE:
					//GlobalConfiguration.logging.debug("COMPLETE");
					byte[] complete_id = new byte[41];
					clientSocket.getInputStream().read(complete_id, 0, 41);
					String complete_ids = new String(complete_id);
					clientSocket.close();
					cmdHandler.handleCOMPLETE(complete_ids);
					GlobalConfiguration.logging.debug("COMPLETE taskid: " + complete_ids);
					break;
				case ReactorCommands.IM_ALIVE:
					GlobalConfiguration.logging.debug("IM_ALIVE");
					break;
				case ReactorCommands.HEART_BEAT:
					//GlobalConfiguration.logging.debug("HEART_BEAT");
					byte[] task_id = new byte[41];
					clientSocket.getInputStream().read(task_id, 0, 41);
					String task_ids = new String(task_id);

					clientSocket.close();

					cmdHandler.handleHEART_BEAT(task_ids);
					GlobalConfiguration.logging.debug("HEART_BEAT taskid: " + task_ids);
					break;
				case ReactorCommands.POST_EVENT:
					GlobalConfiguration.logging.debug("POST_EVENT");
					break;
				case ReactorCommands.POST_MAIL:
					//GlobalConfiguration.logging.debug("POST_MAIL");
					cmdHandler.handleSEND_POST();
					break;
				default:
					GlobalConfiguration.logging.debug("UNKNOWN_COMMAND");

			}//switch

			
		} catch (IOException ex) {
			ex.printStackTrace();
		}finally{
			if(clientSocket != null)
				try {
				clientSocket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}//run
}//WorkerThread

