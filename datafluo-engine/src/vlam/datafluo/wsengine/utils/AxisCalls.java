/*
Copyright 2009 S. Koulouzis

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.  
 */
package vlam.datafluo.wsengine.utils;

import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.client.async.AsyncCall;
import org.apache.axis.client.async.IAsyncResult;

/**
 *
 * @author S. Koulouzis
 */
public class AxisCalls
{
    
    public Object asyncCall(Object[] args, String method, URL endpoint)
    {
        Service service = null;
        Call call = null;
        AsyncCall aCall = null;

        IAsyncResult asyncResult = null;
        try
        {
            service = new Service();
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(endpoint);
            call.setOperationName(new QName(method));

            aCall = new AsyncCall(call);

            asyncResult = aCall.invoke(args);
        }
        catch (ServiceException ex)
        {
            ex.printStackTrace();
        }
        return asyncResult.getResponse();
    }

    // public static Object asncCallBack(Object[] args, String method, URL
    // endpoint) {
    // MyCallBack callBack = null;
    // try {
    // Service aService = new Service();
    // final Call call = (Call) aService.createCall();
    // call.setTargetEndpointAddress(endpoint);
    // call.setOperationName(new QName(method));
    //
    // callBack = new MyCallBack(call);
    // AsyncCall aCall = new AsyncCall(call, callBack);
    // IAsyncResult result = aCall.invoke(args);
    //
    // synchronized (call) {
    // call.wait(0);
    // }
    //
    // } catch (javax.xml.rpc.ServiceException ex) {
    // ex.printStackTrace();
    // } catch (InterruptedException ex) {
    // ex.printStackTrace();
    // }
    // return callBack.getResponce();
    // }

    public static Object call(Object[] args, String method, URL endpoint, int timeout)
    {
        Service service = null;
        Call call = null;
        Object result = null;
        try
        {
            service = new Service();
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(endpoint);
            call.setOperationName(new QName(method));
            call.setTimeout(timeout);
            result = call.invoke(args);
        }
        catch (ServiceException ex)
        {
            ex.printStackTrace();
        }
        catch (RemoteException ex)
        {
            ex.printStackTrace();
        }
        return result;
    }

    public Object call2(Object[] args, String method, URL endpoint, int timeout) throws RemoteException
    {

        // OperationDesc oper = new org.apache.axis.description.OperationDesc();
        // oper.setName("manyArgsRetunsDirLoc");
        // ParameterDesc param = new
        // org.apache.axis.description.ParameterDesc(new
        // javax.xml.namespace.QName("", "arg1"),
        // org.apache.axis.description.ParameterDesc.IN, new
        // javax.xml.namespace.QName(
        // "http://www.w3.org/2001/XMLSchema", "string"),
        // java.lang.String.class, false, false);
        // oper.addParameter(param);
        // param = new org.apache.axis.description.ParameterDesc(new
        // javax.xml.namespace.QName("", "arg2"),
        // org.apache.axis.description.ParameterDesc.IN, new
        // javax.xml.namespace.QName(
        // "http://www.w3.org/2001/XMLSchema", "string"),
        // java.lang.String.class, false, false);
        // oper.addParameter(param);
        // param = new org.apache.axis.description.ParameterDesc(new
        // javax.xml.namespace.QName("", "dirPath"),
        // org.apache.axis.description.ParameterDesc.IN, new
        // javax.xml.namespace.QName(
        // "http://www.w3.org/2001/XMLSchema", "string"),
        // java.lang.String.class, false, false);
        // oper.addParameter(param);
        //        
        // oper.setReturnType(new
        // javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
        // "string"));
        // oper.setReturnClass(java.lang.String.class);
        // oper.setReturnQName(new javax.xml.namespace.QName("",
        // "manyArgsRetunsDirLocReturn"));
        // oper.setStyle(org.apache.axis.constants.Style.RPC);
        // oper.setUse(org.apache.axis.constants.Use.ENCODED);

        org.apache.axis.client.Call _call = new org.apache.axis.client.Call(endpoint);
        _call.setOption(method, args);
        
        // _call.setOperation(oper);
        _call.setOperationName(new QName(method));
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://examples.proxyWS", method));

        Object result = _call.invoke(args);

        return result;
    }
}
