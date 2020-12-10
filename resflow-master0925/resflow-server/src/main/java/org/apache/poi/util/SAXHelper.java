package org.apache.poi.util;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;

public final class SAXHelper {
    private static POILogger logger = POILogFactory.getLogger(SAXHelper.class);
    static final EntityResolver IGNORING_ENTITY_RESOLVER = new EntityResolver() {
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return new InputSource(new StringReader(""));
        }
    };
    private static final SAXParserFactory saxFactory = SAXParserFactory.newInstance();

    private SAXHelper() {
    }

    public static synchronized XMLReader newXMLReader() throws SAXException, ParserConfigurationException {
        XMLReader xmlReader = saxFactory.newSAXParser().getXMLReader();
        xmlReader.setEntityResolver(IGNORING_ENTITY_RESOLVER);
        trySetSAXFeature(xmlReader, "http://javax.xml.XMLConstants/feature/secure-processing", true);
        trySetXercesSecurityManager(xmlReader);
        return xmlReader;
    }

    private static void trySetSAXFeature(XMLReader xmlReader, String feature, boolean enabled) {
        try {
            xmlReader.setFeature(feature, enabled);
        } catch (Exception var4) {
            logger.log(5, new Object[]{"SAX Feature unsupported", feature, var4});
        } catch (AbstractMethodError var5) {
            logger.log(5, new Object[]{"Cannot set SAX feature because outdated XML parser in classpath", feature, var5});
        }

    }

    private static void trySetXercesSecurityManager(XMLReader xmlReader) {
        String[] arr$ = new String[]{"com.sun.org.apache.xerces.internal.util.SecurityManager", "org.apache.xerces.util.SecurityManager"};
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String securityManagerClassName = arr$[i$];

            try {
                Object mgr = Class.forName(securityManagerClassName).newInstance();
                Method setLimit = mgr.getClass().getMethod("setEntityExpansionLimit", Integer.TYPE);
                setLimit.invoke(mgr, 4096);
                xmlReader.setProperty("http://apache.org/xml/properties/security-manager", mgr);
                return;
            } catch (Exception var7) {
                logger.log(5, "SAX Security Manager could not be setup", var7);
            } catch (Error var8) {
                logger.log(5, "SAX Security Manager could not be setup", var8);
            }
        }

    }

    static {
        saxFactory.setValidating(false);
        saxFactory.setNamespaceAware(true);
    }
}
