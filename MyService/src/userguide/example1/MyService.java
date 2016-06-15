/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userguide.example1;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;

import javax.xml.stream.XMLStreamException;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Jun 2, 2005
 * Time: 2:17:58 PM
 */
public class MyService {
    public OMElement echo(OMElement element) throws XMLStreamException {
        //Praparing the OMElement so that it can be attached to another OM Tree.
        //First the OMElement should be completely build in case it is not fully built and still
        //some of the xml is in the stream.
        element.build();
        //Secondly the OMElement should be detached from the current OMTree so that it can be attached
        //some other OM Tree. Once detached the OmTree will remove its connections to this OMElement.
        element.detach();
        return element;
    }

    public void ping(OMElement element) throws XMLStreamException {
        //Do some processing
    }
    public void pingF(OMElement element) throws AxisFault{
        throw new AxisFault("Fault being thrown");
    }
}

