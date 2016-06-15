/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.axis.AxisFault;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.Constants;
import org.apache.axis2.client.ServiceClient;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;



public class RESTClient {

    private static String toEpr = "http://localhost:8080/axis2/services/MyService";

   public static void main(String[] args) throws Exception {
    	String request = "http://localhost:8080/axis2/services/Utils/echo";

        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(request);

        method.addParameter("say","YahooDemo");
        //method.addParameter("query","umbrella");
        //method.addParameter("results","10");

        // Send POST request
        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
        	System.err.println("Method failed: " + method.getStatusLine());
        }
        InputStream rstream = null;

        // Get the response body
        rstream = method.getResponseBodyAsStream();

        // Process the response from Yahoo! Web Services
        BufferedReader br = new BufferedReader(new InputStreamReader(rstream));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();
	}

}
