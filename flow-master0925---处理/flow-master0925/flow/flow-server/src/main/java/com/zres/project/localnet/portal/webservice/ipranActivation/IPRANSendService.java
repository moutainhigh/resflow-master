package com.zres.project.localnet.portal.webservice.ipranActivation;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.spi.http.HttpHandler;

import org.python.antlr.ast.Str;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.client.RestTemplate;

@Service
public class IPRANSendService implements IPRANSendIntf {
    @Override
    public Map<String, Object> send(Map<String, Object> map) {
        Map<String, Object> resultMap = new HashMap<>();
        //1、判断动作类型，拼接报文
        String jsonStr = "";
        //2、拼接报文头，查询请求地址
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("contentType","application/json;charset=utf-8");
        httpHeaders.add("xAuthToken",""); //已认证的预置用户token
        httpHeaders.add("clientId",""); //预置账号用户名
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonStr, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> resopnse = restTemplate.postForEntity("", httpEntity, String.class);

        //3、请求
        //4、解析响应报文
        return null;
    }

    /**
     * IPRAN网络本地点到点VPWS业务创建
     * @param param
     * @return
     */
    public String create(Map<String, Object> param){
        String jsonStr = null;
        return "";
    }

    /**
     * IPRAN网络本地点到点VPWS业务删除
     * @param param
     * @return
     */
    public String remove(Map<String, Object> param){
        String jsonStr = null;
        return "";
    }

    /**
     * VPWS业务停/复机。
     * @param param
     * @return
     */
    public String modify(Map<String, Object> param){
        String jsonStr = null;
        return "";
    }

    /**
     * IPRAN网络本地点到点VPWS业务变更。（暂时只做调速）
     * @param param
     * @return
     */
    public String statusChange(Map<String, Object> param){
        String jsonStr = null;
        return "";
    }
}
