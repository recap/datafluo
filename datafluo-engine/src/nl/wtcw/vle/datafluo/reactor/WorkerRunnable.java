/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.wtcw.vle.datafluo.reactor;

import java.io.IOException;
import java.net.Socket;

import nl.wtcw.vle.datafluo.util.GlobalConfiguration;
import org.apache.log4j.Logger;

/**
 *
 * @author reggie
 * based on http://tutorials.jenkov.com/java-multithreaded-servers/multithreaded-server.html
 */
public class WorkerRunnable implements Runnable {

	private static Logger logger = Logger.getLogger(WorkerRunnable.class);

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
					//logger.debug("CHECK_MAIL");
					cmdHandler.handleCHECK_MAIL();
					break;
				case ReactorCommands.GET_CONFIG:
					logger.debug("GET_CONFIG");
					//byte[] id = new byte[41];
					byte[] id = new byte[10];
					//clientSocket.getInputStream().read(id, 0, 41);
					clientSocket.getInputStream().read(id, 0, 10);
					String ids = new String(id);
					cmdHandler.handleGET_CONFIG(ids);
					break;
				case ReactorCommands.COMPLETE:
					//logger.debug("COMPLETE");
					byte[] complete_id = new byte[41];
					clientSocket.getInputStream().read(complete_id, 0, 41);
					String complete_ids = new String(complete_id);
					clientSocket.close();
					cmdHandler.handleCOMPLETE(complete_ids);
					logger.debug("COMPLETE taskid: " + complete_ids);
					break;
				case ReactorCommands.IM_ALIVE:
					logger.debug("IM_ALIVE");
					break;
				case ReactorCommands.HEART_BEAT:
					//logger.debug("HEART_BEAT");
					byte[] task_id = new byte[41];
					clientSocket.getInputStream().read(task_id, 0, 41);
					String task_ids = new String(task_id);

					clientSocket.close();

					cmdHandler.handleHEART_BEAT(task_ids);
					logger.debug("HEART_BEAT taskid: " + task_ids);
					break;
				case ReactorCommands.POST_EVENT:
					logger.debug("POST_EVENT");
					break;
				case ReactorCommands.POST_MAIL:
					//logger.debug("POST_MAIL");
					cmdHandler.handleSEND_POST();
					break;
				default:
					logger.debug("UNKNOWN_COMMAND");

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

