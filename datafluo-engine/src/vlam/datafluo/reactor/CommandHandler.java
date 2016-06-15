/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vlam.datafluo.reactor;
import java.io.*;
import java.net.*;
import java.util.*;
import uk.ac.soton.itinnovation.freefluo.core.flow.*;
import vlam.datafluo.messaging.*;
import vlam.datafluo.messaging.Queue;
import vlam.datafluo.wsengine.*;
import vlam.datafluo.utils.*;
import nl.wtcw.vle.wfd.ModuleI;
import nl.wtcw.vle.wfd.ParameterI;
import uk.ac.soton.itinnovation.freefluo.core.port.PortState;

/**
 *
 * @author reggie
 */
public class CommandHandler {
	private Flow flow;
	private Socket connection;
	private static int postCounter = 0;
	private final static Object syncMe = new Object();

	public CommandHandler(Socket connection){
		this.flow = GlobalConfiguration.getFlow();
		this.connection = connection;

	}

	private void sendInt(int i) throws IOException{
		byte[] b = UtilMethods.intToByteArray(i);
		connection.getOutputStream().write(b);
	}

	public void handleHEART_BEAT(String id) throws IOException{
		VlamDatafluoTask task = (VlamDatafluoTask)flow.getTask(getTaskID(id));
		task.heartBeat();

	}
	public void handleCOMPLETE(String id) throws IOException{
		VlamDatafluoTask task = (VlamDatafluoTask)flow.getTask(getTaskID(id));
		try {
					Thread.sleep(5000);
		} catch (InterruptedException ex) {ex.printStackTrace();}

		task.tryComplete();
	}

	public void handleCHECK_MAIL() throws IOException{
		//read queue uid length
		byte[] buf_len = new byte[4];
		connection.getInputStream().read(buf_len, 0, 4);
		int len = UtilMethods.byteArrayToInt(buf_len, 0);
		//GlobalConfiguration.logging.debug("RECEIVED: " + len + "\n");
		//read queue_uid
		byte[] queue_uid_buf = new byte[len];
		connection.getInputStream().read(queue_uid_buf,0,len);
		String queue_uid = new String(queue_uid_buf);
		//GlobalConfiguration.logging.debug("MAIL QUEUE: " + queue_uid + "\n");
		String[] queue_uid_tokens = null;
		queue_uid_tokens = queue_uid.split("\\.");
		VlamDatafluoTask task = (VlamDatafluoTask)flow.getTask(queue_uid_tokens[1]);		
		VlamDatafluoPort port = (VlamDatafluoPort)task.getInputPort(queue_uid_tokens[2]);		
		int cloneNumber = Integer.parseInt(queue_uid_tokens[3]);
		queue_uid = queue_uid_tokens[0]+"."+queue_uid_tokens[1]+"."+queue_uid_tokens[2];
		//check mailbox
		VlamEngine engine = (VlamEngine)flow.getEngine();
		MessageExchange messageX = engine.messageExchange;
		Queue queue = messageX.getMessageQueue(queue_uid);

		if( (port.getState().getState() == PortState.DESTROYED_STATE) && (queue.isEmpty() == true) ){
		//	GlobalConfiguration.logging.debug("PORT DESTROYED.\n");
			byte[] cmd = new byte[1];
			cmd[0] = ReactorCommands.PORT_DESTROYED;
			connection.getOutputStream().write(cmd,0,1);

		}
		else{
			if(queue.isEmpty() != true){
				byte[] cmd = new byte[1];
				cmd[0] = ReactorCommands.SENDING_MAIL;
				connection.getOutputStream().write(cmd,0,1);
				Message message = (Message)queue.get(cloneNumber);
				
				//Send message length
				len = message.getMessage().toString().length();
				sendInt(len);
				connection.getOutputStream().write(message.getMessage().getBytes(), 0, len);
				GlobalConfiguration.logging.debug("MESSAGE TO: " + task.getName()+":"+port.getName()+" MSG: "+ message.toString() + "\n");
			}
			else{
				if(GlobalConfiguration.getFlow().getStartTask(task.getTaskId()) != null)
				{
					//GlobalConfiguration.logging.debug("END START TASK PORT\n");
					byte[] cmd = new byte[1];
					cmd[0] = ReactorCommands.PORT_DESTROYED;
					connection.getOutputStream().write(cmd,0,1);
				}
				//GlobalConfiguration.logging.debug("NO_MAIL\n");
				byte[] cmd = new byte[1];
				cmd[0] = ReactorCommands.NO_MAIL;
				//connection.setSoTimeout(30000);
				connection.getOutputStream().write(cmd,0,1);				
			}//esle
		}//else

	}
	public synchronized void handleSEND_POST() throws IOException
	{
		//GlobalConfiguration.logging.debug("In Post\n");
		byte[] buf_len = new byte[4];
		connection.getInputStream().read(buf_len, 0, 4);
		int len = UtilMethods.byteArrayToInt(buf_len, 0);

		byte[] queue_uid_buf = new byte[len];
		connection.getInputStream().read(queue_uid_buf, 0, len);
		String queue_uid = new String(queue_uid_buf);
		//GlobalConfiguration.logging.debug("QUEUE_UID: " + queue_uid + "\n");

		connection.getInputStream().read(buf_len, 0, 4);
		len = UtilMethods.byteArrayToInt(buf_len, 0);
		//GlobalConfiguration.logging.debug("LEN: " + len + "\n");
		byte[] message_buf = new byte[len];
		connection.getInputStream().read(message_buf, 0, len);
		String smessage = new String(message_buf);

		connection.close();

		String[] queue_uid_tokens = null;
		queue_uid_tokens = queue_uid.split("\\.");
		
		VlamDatafluoTask task = (VlamDatafluoTask)flow.getTask(queue_uid_tokens[1]);
		VlamDatafluoPort port = (VlamDatafluoPort)task.getOutputPort(queue_uid_tokens[2]);
		int cloneNumber = Integer.parseInt(queue_uid_tokens[3]);
		queue_uid = queue_uid_tokens[0]+"."+queue_uid_tokens[1]+"."+queue_uid_tokens[2];

		GlobalConfiguration.logging.debug("MESSAGE FORM: " + task.getName()+":"+port.getName()+ " MSG: " + smessage + "\n");

		Message message = new Message();
		message.setMessage(smessage);
		port.getQueue().put(message);
		synchronized(syncMe){
			CommandHandler.postCounter++;		
		}
	}
	public void handleGET_CONFIG(String taskId){

		VlamDatafluoTask ptask = (VlamDatafluoTask)flow.getTask(getTaskID(taskId));
		VlamDatafluoTask task = ptask.getClone(getCloneID(taskId));
		ModuleI module = task.getModule();
		String configStr = new String();
		String params = new String();
		params = "";

		for(Iterator itr = module.getParameters().iterator(); itr.hasNext();){
			ParameterI p = (ParameterI)itr.next();
			params = params + p.getValue() + "?";			
		}

		configStr = "Module:" + module.getName() + ":" + task.getTaskVersion() + ":"
					+ task.getSoLibURL() + ":" + "param?" + params + "\n";

		int i = 0;
		for(Iterator itr = task.getInputPorts().iterator(); itr.hasNext();){
			i++;
			VlamDatafluoPort port = (VlamDatafluoPort)itr.next();			
			
			configStr = configStr +
					"Queue:" + port.getPortId() + ":" + port.getQueue().getKey().toString()+"."+getCloneID(taskId) + ":in:" + "1" + "\n";
		}//for
		i = 0;
		for(Iterator itr = task.getOutputPorts().iterator(); itr.hasNext();){
			i++;
			VlamDatafluoPort port = (VlamDatafluoPort)itr.next();		

			configStr = configStr +
					"Queue:" + port.getPortId() + ":" + port.getQueue().getKey().toString() +"."+getCloneID(taskId) + ":out:" + "1" + "\n";
		}//for

		for(Iterator itr = GlobalConfiguration.getServers().iterator(); itr.hasNext();){
			ServerEntry server = (ServerEntry)itr.next();
			configStr = configStr +
					"Server:" + server.getServerId() + ":" + server.getServerType() +
					":" + server.getMetric() + ":params?" + server.getParameters() + "\n";
		}

		try{
		connection.getOutputStream().write(UtilMethods.intToByteArray(configStr.getBytes().length),0,4);
		connection.getOutputStream().write(configStr.getBytes(), 0, configStr.getBytes().length);
		connection.getOutputStream().flush();
		}catch(IOException ex){
			ex.printStackTrace();
		}


	}
	private String getTaskID(String taskId){
		GlobalConfiguration.logging.debug("TASKID: "+taskId);
		String[] tokens = taskId.split("\\@");
		return tokens[0].toString();
	}
	private int getCloneID(String taskId){
		GlobalConfiguration.logging.debug("TASKID: "+taskId);
		String[] tokens = taskId.split("\\@");
		return Integer.parseInt(tokens[1].toString());
	}

}
