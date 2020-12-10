package com.zres.project.localnet.portal.cloudNetWork.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;

import com.alibaba.fastjson.JSONObject;

/**
 * @Classname CircuitActionNoticeService
 * @Description 停机复机拆机接口
 * @Author guanzhao
 * @Date 2020/11/20 10:34
 */
public class CircuitActionNoticeService implements CircuitActionNoticeIntf {

    private static Logger logger = LoggerFactory.getLogger(CircuitActionNoticeService.class);

    @Autowired
    private WebServiceDao webServiceDao;
    @Autowired
    private XmlUtil xmlUtil;

    @Override
    public Map<String, Object> circuitActionNotice(Map<String, Object> intfMap) {
        Map<String, Object> retMap = new HashMap<>();
        Map<String,Object> interflog = new HashMap<>();

        try{
            Map<String, Object> requestMap = new HashMap<>();
            Map<String, Object> orderInfo = new HashMap<>();
            Map<String, Object> operator = new HashMap<>();
            Map<String, Object> param = new HashMap<>();

            Map<String, Object> dataOrder = new HashMap<>(); //todo:查询数据
            String srvOrdId = MapUtils.getString(dataOrder, "srvOrdId");
            orderInfo.put("FLOW_ID", MapUtils.getString(dataOrder, "FLOW_ID"));
            orderInfo.put("SERIAL_NUMBER", MapUtils.getString(dataOrder, "SERIAL_NUMBER"));
            orderInfo.put("SVR_CODE", MapUtils.getString(dataOrder, "SVR_CODE"));
            orderInfo.put("TRADE_ID", MapUtils.getString(dataOrder, "TRADE_ID"));
            orderInfo.put("AZ", MapUtils.getString(dataOrder, "AZ"));

            operator.put("USER_NAME", MapUtils.getString(dataOrder, "USER_NAME"));
            operator.put("PHONE", MapUtils.getString(dataOrder, "PHONE"));
            operator.put("PROVINCE", MapUtils.getString(dataOrder, "PROVINCE"));
            operator.put("CITY", MapUtils.getString(dataOrder, "CITY"));
            operator.put("COMPANY", MapUtils.getString(dataOrder, "COMPANY"));

            requestMap.put("ORDER_INFO", orderInfo);
            requestMap.put("OPERATOR", operator);
            requestMap.put("PARAM", param);
            requestMap.put("WORK_SHEET_NUMBER", "");

            String requestJson = JSONObject.toJSONString(requestMap);
            logger.info("云组网停机复机拆机接口请求报文 ：" + requestJson);
            String url = webServiceDao.queryUrl(""); //todo:查询接口
            //set接口需要入库的数据
            interflog.put("INTERFNAME","云组网停机复机拆机接口");
            interflog.put("URL",url);
            interflog.put("CONTENT", requestJson);
            interflog.put("ORDERNO",srvOrdId);

            Map response = xmlUtil.sendHttpPostOrderCenter(url, requestJson);
            interflog.put("RETURNCONTENT", JSONObject.toJSONString(response)); //set接口需要入库的数据
            logger.info("云组网停机复机拆机接口返回报文 ：" + JSONObject.toJSONString(response));
            String code = MapUtils.getString(response,"code","");
            if("200".equals(code)){
                String responses = MapUtils.getString(response,"msg");
                Map respJson = JSONObject.parseObject(responses,Map.class);
                //0：成功；-1：失败；
                String respDesc = MapUtils.getString(respJson,"MESSAGE","云组网停机复机拆机接口交互异常");//失败原因 -1时必填
                if("0".equals(MapUtils.getString(respJson,"CODE"))){
                    Map<String,Object> data = MapUtils.getMap(respJson,"DATA");
                    String status = MapUtils.getString(data,"status");
                    String reason = MapUtils.getString(respJson,"reason","云组网停机复机拆机接口成功");//失败原因 -1时必

                    //TODO：做一些操作
                    retMap.put("success",true);
                    retMap.put("message",data);
                } else{
                    retMap.put("success",false);
                    retMap.put("message",respDesc);
                }
            } else{
                retMap.put("success",false);
                retMap.put("message","接口交互失败："+code);
            }
        }catch (Exception e){
            retMap.put("success",false);
            retMap.put("message","接口交互失败：" + e.getMessage());
        }finally {
            //插入接口记录
            if (MapUtils.getBoolean(retMap, "success")){
                interflog.put("REMARK","云组网停机复机拆机接口");
            }else {
                interflog.put("REMARK", MapUtils.getString(retMap, "message"));
            }
            webServiceDao.insertInterfLog(interflog);
        }

        return retMap;
    }
}
