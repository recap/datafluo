/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.illusion;

import java.util.ArrayList;

/**
 *
 * @author reggie
 */
class AbstractService {
	  private String name;
	  private ArrayList<String> operations = new ArrayList();

	  public AbstractService(String name){
		  this.name = name;
	  }

	  public String getName(){
		  return this.name;
	  }

	  public void addOperation(String operation){
		  operations.add(operation);
	  }

	  public boolean hasOperation(String operation){
		  if(operations.contains(operation) == true)
			  return true;
		  else
			  return false;
	  }
}
