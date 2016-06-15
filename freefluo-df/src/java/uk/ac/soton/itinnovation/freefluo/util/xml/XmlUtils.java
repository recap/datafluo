////////////////////////////////////////////////////////////////////////////////
//
// University of Southampton IT Innovation Centre, 2002
//
// Copyright in this library belongs to the IT Innovation Centre of
// 2 Venture Road, Chilworth Science Park, Southampton SO16 7NP, UK.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1
// of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation Inc, 59 Temple Place, Suite 330, Boston MA 02111-1307 USA.
//
//      Created By          :   Justin Ferris
//      Created Date        :   2003/11/03
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: ferris $
//                              $Date: 2005/08/23 06:24:48 $
//                              $Revision: 1.1 $
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.freefluo.util.xml;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.xml.sax.*;

import org.w3c.dom.*;

import org.apache.log4j.*;

public class XmlUtils {
  private static Logger logger = Logger.getLogger(XmlUtils.class);
	
  public static org.jdom.Document jdomDocumentFromFile(File file) throws IOException, org.jdom.JDOMException {
    FileInputStream fis = new FileInputStream(file);
    org.jdom.Document document = jdomDocumentFromStream(fis);

    fis.close();
    return document;
  }

  public static org.jdom.Document jdomDocumentFromString(String strDocument) throws IOException, org.jdom.JDOMException {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(strDocument.getBytes());

    return jdomDocumentFromStream(inputStream);
  }

  public static org.jdom.Document jdomDocumentFromStream(InputStream inputStream) throws IOException, org.jdom.JDOMException {
    org.jdom.input.SAXBuilder sb = new org.jdom.input.SAXBuilder();
    org.jdom.Document document = sb.build(inputStream);

    return document;
  }

  public static Document documentFromString(String strDocument) throws FactoryConfigurationError, ParserConfigurationException, IOException, SAXException {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(strDocument.getBytes());

    return documentFromStream(inputStream);
  }

  public static Document documentFromStream(InputStream inputStream) throws FactoryConfigurationError, ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory documentBuilderFactory = null;
    DocumentBuilder documentBuilder = null;
    Document document = null; 

    try {
      documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(true);
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }
    catch(FactoryConfigurationError e) {
      String msg = "Error in configuration of DOM factory.";

      logger.error(msg, e);
      throw e;
    }
    catch(ParserConfigurationException e) {
      String msg = "Error in creating DOM parser.  No suitable implementation for the configuration.";

      logger.error(msg, e);
      throw e;
    }

    try {
      document = documentBuilder.parse(inputStream);
    }
    catch(IOException e) {
      String msg = "IO error reading from input stream";

      logger.error(msg, e);
      throw e;
    }
    catch(SAXException e) {
      String msg = "Error parsing XML stream.  Invalid XML.";

      logger.error(msg, e);
      throw e;
    }

    return document;
  }

  public static Element elementFromJdom(org.jdom.Element jdomElement) throws FactoryConfigurationError, ParserConfigurationException, IOException, SAXException {
    org.jdom.Document jdomDocument = new org.jdom.Document();

    jdomDocument.setRootElement(jdomElement);
    String strDoc = jdomDocumentToString(jdomDocument);
    Document doc = documentFromString(strDoc);

    return doc.getDocumentElement();
  }

  public static String jdomDocumentToString(org.jdom.Document document) {
    org.jdom.output.XMLOutputter out = new org.jdom.output.XMLOutputter(org.jdom.output.Format.getPrettyFormat());

    return out.outputString(document);
  }

  public static Element getFirstChildElement(Node node) {
    Element element = null;
    NodeList childs = node.getChildNodes();

    for(int i = 0; i < childs.getLength(); i++) {
      if(childs.item(i).getNodeType() == Node.ELEMENT_NODE) {
        element = (Element) childs.item(i);
        break;
      }
    }

    return element;
  }

  public static Text getFirstChildText(Element element) {
    Text text = null;
    NodeList childs = element.getChildNodes();

    for(int i = 0; i < childs.getLength(); i++) {
      if(childs.item(i).getNodeType() == Node.TEXT_NODE) {
        text = (Text) childs.item(i);
        break;
      }
    }

    return text;
  }

  public static String getFirstChildTextTrim(Element element) {
    Text text = getFirstChildText(element);
    String str = null;

    if(text != null) {
      str = text.getData().trim();
    }
    return str;
  }

  public static String getLocalName(Element element) {
    String nsPrfix = null;
    String tagName = element.getTagName();
    String[] tokens = tagName.split(":"); 

    return tokens.length == 2 ? tokens[1] : tokens[0];
  }
}
