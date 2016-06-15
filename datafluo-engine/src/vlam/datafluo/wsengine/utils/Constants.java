/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vlam.datafluo.wsengine.utils;

import java.net.URI;
import java.util.Date;
import javax.xml.namespace.QName;

/**
 *
 * @author skoulouz
 */
public class Constants {

    //A simple map

    public static final String[] xsdTypes={"anyType",
                                            "string",
                                            "boolean",
                                            "int",
                                            "long",
                                            "short",
                                            "float",
                                            "double",
                                            "dateTime",
                                            "Qname",
                                            "Java.xml.namespace.QName",
                                            "anyURI",
                                            "ArrayOf_xsd_anyType",
                                            "ArrayOf_xsd_string",
                                            "ArrayOf_xsd_boolean",
                                            "ArrayOf_xsd_int",
                                            "ArrayOf_xsd_short",
                                            "ArrayOf_xsd_float",
                                            "ArrayOf_xsd_double"};


    public static final Class[] javaTypes={Object.class,
                                            String.class,
                                            boolean.class,
                                            int.class,
                                            long.class,
                                            short.class,
                                            float.class,
                                            double.class,
                                            Date.class,
                                            QName.class,
                                            URI.class,
                                            java.lang.reflect.Array.class,
                                            java.lang.reflect.Array.class,
                                            java.lang.reflect.Array.class,
                                            java.lang.reflect.Array.class,
                                            java.lang.reflect.Array.class,
                                            java.lang.reflect.Array.class,
                                            java.lang.reflect.Array.class};

}
