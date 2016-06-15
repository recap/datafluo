/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.datafluo.reactor;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nl.wtcw.vle.datafluo.util.GlobalConfiguration;


/**
 *
 * @author reggie
 * based on http://tutorials.jenkov.com/java-multithreaded-servers/multithreaded-server.html
 */
public class ReactorServer implements Runnable{
	private	  static ReactorServer instance		= null;
	private   boolean			running			= false;
	protected int				port			= GlobalConfiguration.centralReactorPort;
	protected int				maxConnections	= 400;
	protected ServerSocket		serverSocket	= null;
	protected boolean			isStopped		= false;
	protected Thread			runningThread	= null;
	protected ExecutorService	threadPool		= null;
	
	public ReactorServer(int port, int maxConnections){
		this.port = port;
		this.maxConnections = maxConnections;
		this.threadPool = Executors.newFixedThreadPool(this.maxConnections);
	}
	
	public ReactorServer(){		
		this.threadPool = Executors.newFixedThreadPool(this.maxConnections);
	}

	public static ReactorServer getInstance(){
		if(instance == null){
			instance = new ReactorServer();
		}
		return instance;
	}

	public boolean isRunning(){
		return this.running;
	}
	
	public void run(){
		running = true;
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
					System.err.println("Server stopped");
					return;
				}//if
				ex.printStackTrace();
				throw new RuntimeException("Server error");
			}//finally{ this.stop(); }
			this.threadPool.execute(
					new Thread(new WorkerRunnable(clientSocket)) );
		}//while
		this.threadPool.shutdown();
		System.err.println("Server stopped");
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

	
}//ReactorServer


	