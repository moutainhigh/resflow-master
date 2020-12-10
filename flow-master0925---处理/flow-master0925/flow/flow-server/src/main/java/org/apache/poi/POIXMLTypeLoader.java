package org.apache.poi;

import org.apache.poi.util.DocumentHelper;
import org.apache.poi.util.Removal;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLStreamException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class POIXMLTypeLoader {
    private static ThreadLocal<SchemaTypeLoader> typeLoader = new ThreadLocal();
    private static final String MS_OFFICE_URN = "urn:schemas-microsoft-com:office:office";
    private static final String MS_EXCEL_URN = "urn:schemas-microsoft-com:office:excel";
    private static final String MS_WORD_URN = "urn:schemas-microsoft-com:office:word";
    private static final String MS_VML_URN = "urn:schemas-microsoft-com:vml";
    public static final XmlOptions DEFAULT_XML_OPTIONS = new XmlOptions();

    public POIXMLTypeLoader() {
    }

    private static XmlOptions getXmlOptions(XmlOptions options) {
        return options == null ? DEFAULT_XML_OPTIONS : options;
    }

    /** @deprecated */
    @Deprecated
    @Removal(
            version = "4.0"
    )
    public static void setClassLoader(ClassLoader cl) {
    }

    private static SchemaTypeLoader getTypeLoader(SchemaType type) {
        SchemaTypeLoader tl = (SchemaTypeLoader)typeLoader.get();
        if (tl == null) {
            ClassLoader cl = type.getClass().getClassLoader();
            tl = XmlBeans.typeLoaderForClassLoader(cl);
            typeLoader.set(tl);
        }

        return tl;
    }

    public static XmlObject newInstance(SchemaType type, XmlOptions options) {
        return getTypeLoader(type).newInstance(type, getXmlOptions(options));
    }

    public static XmlObject parse(String xmlText, SchemaType type, XmlOptions options) throws XmlException {
        try {
            return parse((Reader)(new StringReader(xmlText)), type, options);
        } catch (IOException var4) {
            throw new XmlException("Unable to parse xml bean", var4);
        }
    }

    public static XmlObject parse(File file, SchemaType type, XmlOptions options) throws XmlException, IOException {
        FileInputStream is = new FileInputStream(file);

        XmlObject var4;
        try {
            var4 = parse((InputStream)is, type, options);
        } finally {
            is.close();
        }

        return var4;
    }

    public static XmlObject parse(URL file, SchemaType type, XmlOptions options) throws XmlException, IOException {
        InputStream is = file.openStream();

        XmlObject var4;
        try {
            var4 = parse(is, type, options);
        } finally {
            is.close();
        }

        return var4;
    }

    public static XmlObject parse(InputStream jiois, SchemaType type, XmlOptions options) throws XmlException, IOException {
        try {
            Document doc = DocumentHelper.readDocument(jiois);
            return getTypeLoader(type).parse(doc.getDocumentElement(), type, getXmlOptions(options));
        } catch (SAXException var4) {
            throw new IOException("Unable to parse xml bean", var4);
        }
    }

    public static XmlObject parse(XMLStreamReader xsr, SchemaType type, XmlOptions options) throws XmlException {
        return getTypeLoader(type).parse(xsr, type, getXmlOptions(options));
    }

    public static XmlObject parse(Reader jior, SchemaType type, XmlOptions options) throws XmlException, IOException {
        try {
            Document doc = DocumentHelper.readDocument(new InputSource(jior));
            return getTypeLoader(type).parse(doc.getDocumentElement(), type, getXmlOptions(options));
        } catch (SAXException var4) {
            throw new XmlException("Unable to parse xml bean", var4);
        }
    }

    public static XmlObject parse(Node node, SchemaType type, XmlOptions options) throws XmlException {
        return getTypeLoader(type).parse(node, type, getXmlOptions(options));
    }

    public static XmlObject parse(XMLInputStream xis, SchemaType type, XmlOptions options) throws XmlException, XMLStreamException {
        return getTypeLoader(type).parse(xis, type, getXmlOptions(options));
    }

    public static XMLInputStream newValidatingXMLInputStream(XMLInputStream xis, SchemaType type, XmlOptions options) throws XmlException, XMLStreamException {
        return getTypeLoader(type).newValidatingXMLInputStream(xis, type, getXmlOptions(options));
    }

    static {
        DEFAULT_XML_OPTIONS.setSaveOuter();
        DEFAULT_XML_OPTIONS.setUseDefaultNamespace();
        DEFAULT_XML_OPTIONS.setSaveAggressiveNamespaces();
        DEFAULT_XML_OPTIONS.setCharacterEncoding("UTF-8");
        Map<String, String> map = new HashMap();
        map.put("http://schemas.openxmlformats.org/drawingml/2006/main", "a");
        map.put("http://schemas.openxmlformats.org/drawingml/2006/chart", "c");
        map.put("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", "wp");
        map.put("http://schemas.openxmlformats.org/markup-compatibility/2006", "ve");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/math", "m");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "r");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "vt");
        map.put("http://schemas.openxmlformats.org/presentationml/2006/main", "p");
        map.put("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w");
        map.put("http://schemas.microsoft.com/office/word/2006/wordml", "wne");
        map.put("urn:schemas-microsoft-com:office:office", "o");
        map.put("urn:schemas-microsoft-com:office:excel", "x");
        map.put("urn:schemas-microsoft-com:office:word", "w10");
        map.put("urn:schemas-microsoft-com:vml", "v");
        DEFAULT_XML_OPTIONS.setSaveSuggestedPrefixes(Collections.unmodifiableMap(map));
    }
}
