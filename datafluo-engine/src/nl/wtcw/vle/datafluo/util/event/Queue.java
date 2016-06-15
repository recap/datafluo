/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2005
//
// Copyright in this software belongs to the IT Innovation Centre of
// 2 Venture Road, Chilworth Science Park, Southampton SO16 7NP, UK.
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//	Created By :          Justin Ferris
//	Created Date :        2005/08/08
//	Created for Project : Simdat
//
/////////////////////////////////////////////////////////////////////////
//
//	Dependencies : none
//
/////////////////////////////////////////////////////////////////////////
//
//	Last commit info:	$Author: ferris $
//                    $Date: 2005/08/23 06:24:48 $
//                    $Revision: 1.1 $
//
/////////////////////////////////////////////////////////////////////////
package nl.wtcw.vle.datafluo.util.event;

import java.io.*;
import java.util.*;

import org.apache.log4j.*;

public class Queue {
  private static Logger logger = Logger.getLogger(Queue.class);
  private Consumer consumer;
  private LinkedList list = new LinkedList();
  private boolean isRunning = true;

  public Queue(Consumer consumer) {
    this.consumer = consumer;
  }

  public void start() {
    (new ConsumerThread()).start();
  }
  
  public void put(Object event) {
    if(!isRunning) {
      throw new RuntimeException("put called on Queue that isn't running");
    }
    
    synchronized(list) {
      list.addFirst(event);
      list.notify();
    }
  }

  public void stop() {
    synchronized(list) {
      isRunning = false;
      list.notify();
    }
  }

  private class ConsumerThread extends Thread {
    public void run() {
      try {
        while(isRunning) {
          sendEvents();

          synchronized(list) {
            if(isRunning && list.size() == 0) {
              list.wait();
            }
          }
        }

        // the queue has been stopped, but still need
        // to empty and sent any events that remain
        // in the queue.
        sendEvents();
      }
      catch(Exception e) {
        logger.error("Error notifying consumer", e);
      }
    }

    private void sendEvents() {
      List copiedList = new ArrayList();
      synchronized(list) {
        copiedList.addAll(list);
        list.clear();
      }

      for(Iterator i = copiedList.iterator(); i.hasNext();) {
        consumer.newEvent(i.next());
      }
    }
  }
}