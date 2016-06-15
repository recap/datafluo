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
package uk.ac.soton.itinnovation.freefluo.util.event;

import java.io.*;
import java.util.*;

/**
 * A queue that can have multiple consumers.
 */
public class Topic {
  private List consumerQueues = new ArrayList();

  public Topic() {
  }

  public void addConsumer(Consumer consumer) {
    synchronized(consumerQueues) {
      consumerQueues.add(new Queue(consumer));
    }
  }

  public void start() {
    synchronized(consumerQueues) {
      for(Iterator i = consumerQueues.iterator(); i.hasNext();) {
        Queue queue = (Queue) i.next();
        queue.start();
      }
    }
  }
  
  public void put(Object event) {
    synchronized(consumerQueues) {
      for(Iterator i = consumerQueues.iterator(); i.hasNext();) {
        Queue queue = (Queue) i.next();
        queue.put(event);
      }
    }
  }

  public void stop() {
    synchronized(consumerQueues) {
      for(Iterator i = consumerQueues.iterator(); i.hasNext();) {
        Queue queue = (Queue) i.next();
        queue.stop();
      }
    }
  }
}