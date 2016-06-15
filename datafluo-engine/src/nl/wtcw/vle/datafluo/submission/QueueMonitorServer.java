/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.datafluo.submission;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nl.wtcw.vle.datafluo.util.GlobalConfiguration;
import org.apache.log4j.Logger;

/**
 *
 * @author reggie
 */
public class QueueMonitorServer implements Runnable{

	private static Logger logger				= Logger.getLogger(QueueMonitorServer.class);
	protected int				port			= GlobalConfiguration.queueMonitorPort;
	protected int				maxConnections	= 100;
	protected ServerSocket		serverSocket	= null;
	protected boolean			isStopped		= false;
	protected Thread			runningThread	= null;
	protected ExecutorService	threadPool		= null;
	
	public QueueMonitorServer(){
		this.threadPool = Executors.newFixedThreadPool(this.maxConnections);
	}


	public void run() {
		synchronized(this){
			this.runningThread = Thread.currentThread();
		}
		openServerSocket();
		while(!isStopped()){
			Socket clientSocket = null;
			try{
				clientSocket = this.serverSocket.accept();				
			}catch(IOException ex){
				if(isStopped()){
					logger.debug("Queue Monitor Server stopped");
					return;
				}//if

				ex.printStackTrace();
				throw new RuntimeException("Server error");

			}//catch
			this.threadPool.execute(
					new Thread(new QueueMonitorWorker(clientSocket)) );
		}//while
		this.threadPool.shutdown();
		logger.debug("Queue Monitor Server stopped");
	}//run

	private synchronized boolean isStopped(){
		return this.isStopped;
	}//isStopped

	public synchronized void stop(){
		this.isStopped = true;
		try{
			this.serverSocket.close();
		}catch(IOException ex){
			throw new RuntimeException("Error closing server.");
		}//catch
	}//stop

	private void openServerSocket(){
		try{
			this.serverSocket = new ServerSocket(this.port);
		}catch(IOException ex){
			throw new RuntimeException("Cannot open server");
		}
	}
}