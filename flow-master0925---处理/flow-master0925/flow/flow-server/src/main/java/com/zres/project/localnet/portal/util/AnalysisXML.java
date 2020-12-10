package com.zres.project.localnet.portal.util;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisXML {

    private static Logger logger = LoggerFactory.getLogger(AnalysisXML.class);

    /**
     * XML转Map
     */
    public static Map<String, Object> analysis(String xml) {
        Map<String, Object> objxml = null;
        Document document = null;
        try {
            document = DocumentHelper.parseText(xml);
        }
        catch (DocumentException e1) {
            logger.debug("XML转Map" + e1);
        }
        objxml = dom2Map(document);
        return objxml;
    }

    /**
     * Document转Map
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> dom2Map(Document doc) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (doc == null) {
            return map;
        }
        Element root = doc.getRootElement();
        for (Iterator<Object> iterator = root.elementIterator(); iterator.hasNext();) {
            Element e = (Element) iterator.next();
            List<Object> list = e.elements();
            if (list.size() > 0) {
                map.put(e.getName(), dom2Map(e));
            }
            else {
                map.put(e.getName(), e.getText());
            }
        }
        return map;
    }

    /**
     * Document转Map
     */
    @SuppressWarnings("unchecked")
    private static Map<Object, Object> dom2Map(Element e) {
        Map<Object, Object> map = new HashMap<Object, Object>();
        List<Object> list = e.elements();
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                Element iter = (Element) list.get(i);
                List<Object> mapList = new ArrayList<Object>();

                if (iter.elements().size() > 0) {
                    Map<Object, Object> m = dom2Map(iter);
                    if (map.get(iter.getName()) != null) {
                        Object obj = map.get(iter.getName());
                        if (!(obj instanceof ArrayList)) {
                            mapList = new ArrayList<Object>();
                            mapList.add(obj);
                            mapList.add(m);
                        }
                        if (obj instanceof ArrayList) {
                            mapList = (List<Object>) obj;
                            mapList.add(m);
                        }
                        map.put(iter.getName(), mapList);
                    }
                    else {
                        map.put(iter.getName(), m);
                    }
                }
                else {
                    if (map.get(iter.getName()) != null) {
                        Object obj = map.get(iter.getName());
                        if (!(obj instanceof ArrayList)) {
                            mapList = new ArrayList<Object>();
                            mapList.add(obj);
                            Map<Object, Object> attrMap = new HashMap<Object, Object>();
                            for (int j = 0; j < iter.attributes().size(); j++) {
                                Attribute attribute = (Attribute) iter.attributes().get(j);
                                attrMap.put(attribute.getName(), attribute.getText());
                            }
                            if (attrMap.size() > 0) {
                                mapList.add(attrMap);
                            }
                            else {
                                mapList.add(iter.getText().trim());
                            }
                        }
                        if (obj instanceof ArrayList) {
                            mapList = (List<Object>) obj;
                            Map<Object, Object> attrMap = new HashMap<Object, Object>();
                            for (int j = 0; j < iter.attributes().size(); j++) {
                                Attribute attribute = (Attribute) iter.attributes().get(j);
                                attrMap.put(attribute.getName(), attribute.getText());
                            }
                            if (attrMap.size() > 0) {
                                mapList.add(attrMap);
                            }
                            else {
                                mapList.add(iter.getText().trim());
                            }
                        }
                        map.put(iter.getName(), mapList);
                    }
                    else {
                        map.put(iter.getName(), iter.getText().trim());
                        Map<Object, Object> attrMap = new HashMap<Object, Object>();
                        for (int j = 0; j < iter.attributes().size(); j++) {
                            Attribute attribute = (Attribute) iter.attributes().get(j);
                            attrMap.put(attribute.getName(), attribute.getText());
                        }
                        if (attrMap.size() > 0) {
                            map.put(iter.getName(), attrMap);
                        }
                    }
                }
            }
        }
        else {
            map.put(e.getName(), e.getText());
        }
        return map;
    }
}
