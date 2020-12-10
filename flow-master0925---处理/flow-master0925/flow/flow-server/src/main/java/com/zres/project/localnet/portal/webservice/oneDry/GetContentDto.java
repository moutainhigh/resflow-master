package com.zres.project.localnet.portal.webservice.oneDry;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zres.project.localnet.portal.util.AnalysisXML;

/**
 * Created by jiangdebing on 2018/12/20.
 */
public class GetContentDto {
    private Map<String, Object> header = new HashMap<String, Object>();
    private Map<String, Object> body = new HashMap<String, Object>();
    private Map<String, Object> serviceOrderInfo = new HashMap<String, Object>();
    private Map<String, Object> customerInfo = new HashMap<String, Object>();
    private Map<String, Object> dispatchInfo = new HashMap<String, Object>();
    private Map<String, Object> prodsInfo = new HashMap<String, Object>();
    private List<Map<String, Object>> prodInfo = new ArrayList<Map<String, Object>>();

    public GetContentDto(String xml) {
        if (xml != null && !"".equals(xml) && !"null".equals(xml)) {
            Map<String, Object> objxml = AnalysisXML.analysis(xml);
            if (objxml != null && objxml.size() > 0) {
                header = (Map<String, Object>) objxml.get("header");
                body = (Map<String, Object>) objxml.get("body");
                if (body != null && body.size() > 0) {
                    serviceOrderInfo = (Map<String, Object>) body.get("serviceOrderInfo");
                    customerInfo = (Map<String, Object>) body.get("customerInfo");
                    dispatchInfo = (Map<String, Object>) body.get("dispatchInfo");
                    prodsInfo = (Map<String, Object>) body.get("prodsInfo");
                    if (prodsInfo != null && prodsInfo.size() > 0) {
                        try {
                            prodInfo = (List<Map<String, Object>>) prodsInfo.get("prodInfo");
                        }
                        catch (Exception e) {
                            prodInfo.add((Map<String, Object>) prodsInfo.get("prodInfo"));
                        }
                    }
                }
            }
        }
    }

    public List<Map<String, Object>> getProdInfo() {
        return prodInfo;
    }

    public Map<String, Object> getProdsInfo() {
        return prodsInfo;
    }

    public Map<String, Object> getDispatchInfo() {
        return dispatchInfo;
    }

    public Map<String, Object> getCustomerInfo() {
        return customerInfo;
    }

    public Map<String, Object> getServiceOrderInfo() {
        return serviceOrderInfo;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public Map<String, Object> getHeader() {
        return header;
    }

}
