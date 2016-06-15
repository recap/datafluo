/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.wtcw.vle.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author reggie
 */
public class Utils {

	public String sayHello(){
		return (String)"Hello";
	}

	public String echo(String say){
		return (String)"echoed: "+say;
	}
	
	public String[] getSequenceForId(String[] ids) throws Exception{
		List<String> lst = new ArrayList<String>();
		for(int i = 0; i < ids.length; i++){
			//URL uniprotFasta = new URL(String.format("http://www.uniprot.org/uniprot/%s.fasta", ids[i]));
			URL uniprotFasta = new URL(String.format("http://elab.science.uva.nl:8080/~reggie/dbfiles2/%s.fasta", ids[i]));
			//System.out.println("URL: "+uniprotFasta.toString());
			BufferedReader in = new BufferedReader(	new InputStreamReader(uniprotFasta.openStream()));
			String contents = new String();
			String line;
			while ((line = in.readLine()) != null)
					contents = contents.concat(line+"\n");
			in.close();
			lst.add(contents);
			//Thread.sleep(100);
		}

		String[] result = (String[])lst.toArray(new String[lst.size()]);
		return result;
	}

}
