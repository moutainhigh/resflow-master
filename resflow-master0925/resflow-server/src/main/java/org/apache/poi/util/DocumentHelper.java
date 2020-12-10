package org.apache.poi.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.events.Namespace;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

public final class DocumentHelper {
    private static POILogger logger = POILogFactory.getLogger(DocumentHelper.class);
    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private static final DocumentBuilder documentBuilderSingleton;

    private DocumentHelper() {
    }

    public static synchronized DocumentBuilder newDocumentBuilder() {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setEntityResolver(SAXHelper.IGNORING_ENTITY_RESOLVER);
            documentBuilder.setErrorHandler(new DocumentHelper.DocHelperErrorHandler());
            return documentBuilder;
        } catch (ParserConfigurationException var1) {
            throw new IllegalStateException("cannot create a DocumentBuilder", var1);
        }
    }

    private static void trySetSAXFeature(DocumentBuilderFactory dbf, String feature, boolean enabled) {
        try {
            dbf.setFeature(feature, enabled);
        } catch (Exception var4) {
            logger.log(5, new Object[]{"SAX Feature unsupported", feature, var4});
        } catch (AbstractMethodError var5) {
            logger.log(5, new Object[]{"Cannot set SAX feature because outdated XML parser in classpath", feature, var5});
        }

    }

    private static void trySetXercesSecurityManager(DocumentBuilderFactory dbf) {
        String[] arr$ = new String[]{"com.sun.org.apache.xerces.internal.util.SecurityManager", "org.apache.xerces.util.SecurityManager"};
        int len$ = arr$.length;
        int i$ = 0;

        while(i$ < len$) {
            String securityManagerClassName = arr$[i$];

            try {
                Object mgr = Class.forName(securityManagerClassName).newInstance();
                Method setLimit = mgr.getClass().getMethod("setEntityExpansionLimit", Integer.TYPE);
                setLimit.invoke(mgr, 4096);
                dbf.setAttribute("http://apache.org/xml/properties/security-manager", mgr);
                return;
            } catch (Throwable var7) {
                logger.log(5, "SAX Security Manager could not be setup", var7);
                ++i$;
            }
        }

    }

    public static Document readDocument(InputStream inp) throws IOException, SAXException {
        return newDocumentBuilder().parse(inp);
    }

    public static Document readDocument(InputSource inp) throws IOException, SAXException {
        return newDocumentBuilder().parse(inp);
    }

    public static synchronized Document createDocument() {
        return documentBuilderSingleton.newDocument();
    }

    public static void addNamespaceDeclaration(Element element, String namespacePrefix, String namespaceURI) {
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + namespacePrefix, namespaceURI);
    }

    public static void addNamespaceDeclaration(Element element, Namespace namespace) {
        addNamespaceDeclaration(element, namespace.getPrefix(), namespace.getNamespaceURI());
    }

    static {
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setValidating(false);
        trySetSAXFeature(documentBuilderFactory, "http://javax.xml.XMLConstants/feature/secure-processing", true);
        trySetXercesSecurityManager(documentBuilderFactory);
        documentBuilderSingleton = newDocumentBuilder();
    }

    private static class DocHelperErrorHandler implements ErrorHandler {
        private DocHelperErrorHandler() {
        }

        public void warning(SAXParseException exception) throws SAXException {
            this.printError(5, exception);
        }

        public void error(SAXParseException exception) throws SAXException {
            this.printError(7, exception);
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            this.printError(9, exception);
            throw exception;
        }

        private void printError(int type, SAXParseException ex) {
            StringBuilder sb = new StringBuilder();
            String systemId = ex.getSystemId();
            if (systemId != null) {
                int index = systemId.lastIndexOf(47);
                if (index != -1) {
                    systemId = systemId.substring(index + 1);
                }

                sb.append(systemId);
            }

            sb.append(':');
            sb.append(ex.getLineNumber());
            sb.append(':');
            sb.append(ex.getColumnNumber());
            sb.append(": ");
            sb.append(ex.getMessage());
            DocumentHelper.logger.log(type, sb.toString(), ex);
        }
    }
}
