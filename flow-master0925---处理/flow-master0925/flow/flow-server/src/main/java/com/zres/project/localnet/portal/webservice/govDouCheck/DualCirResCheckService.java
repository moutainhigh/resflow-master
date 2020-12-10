package com.zres.project.localnet.portal.webservice.govDouCheck;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.govDouCheck.DualCirResCheckServiceIntf;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName DualCirResCheckService
 * @Description TODO
 * @Author tang.hl
 * @Date 2020/8/11 17:47
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Service
public class DualCirResCheckService implements DualCirResCheckServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(DualCirResCheckService.class);

    @Autowired
    private XmlUtil xmlUtil;
    @Autowired
    private WebServiceDao webServiceDao;

    /**
     * 双线资源核查接口（数据接口）
     *  调用场景：
     *  1.集客下发互联网专线产品的核查单，收单之后自动调用该接口
     *  2.核查调度环节，互联网专线产品，增加自动化核查按钮，点击按钮调用该接口
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> dualCirResCheck(Map<String, Object> params) {
        Map<String, Object> retMap = sendJson(params);
        return retMap;
    }

    /**
     *
     * @param params
     * @return
     */
    public Map<String, Object> sendJson(Map<String, Object> params) {

        //拼接请求报文
        logger.info("双线资源核查接口开始" + params);
        Map<String, Object> constructResponse = new HashMap<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        Map<String, Object> retMap = new HashMap<>();
        JSONObject requestObj = new JSONObject();
        String srvOrdId = MapUtils.getString(params,"srvOrdId");
        String response = "";
        String url = "";
        List<Map<String, Object>> tacheInfo = new ArrayList<>();
        // 要传过来 srv_ord_id 过来
        try{
            //
            Map<String, Object> autoChkInfo = webServiceDao.queryAutoChkInfo(srvOrdId);
            JSONObject dualCirResCheckReq = new JSONObject();
            // 目前只有互联网产品，
            dualCirResCheckReq.put("CIRCUIT_TYPE", MapUtils.getString(autoChkInfo,"CIRCUIT_TYPE"));//业务类型，包括：互联网专线, 以太网专线
            dualCirResCheckReq.put("PROVINCE_A", MapUtils.getString(autoChkInfo,"PROVINCE_A"));// CON0101
            dualCirResCheckReq.put("STD_ADDRESS_A", MapUtils.getString(autoChkInfo,"STD_ADDRESS_A"));// 200002617	标准地址id
            dualCirResCheckReq.put("PROVINCE_Z", "");// CON0102
            dualCirResCheckReq.put("STD_ADDRESS_Z", "");//200002619	Z端标准地址id
            dualCirResCheckReq.put("CIRCUIT_RATE", transSize(MapUtils.getString(autoChkInfo,"CIRCUIT_RATE")));// 10001110 端口带宽
            dualCirResCheckReq.put("CORE_TYPE", "");// 纤芯类型
            dualCirResCheckReq.put("NEED_RES_LIST", MapUtils.getString(autoChkInfo,"NEED_RES_LIST"));// ORD10171 A端资源是否具备
            requestObj.put("DUAL_CIR_RES_CHECK_REQ", dualCirResCheckReq);
            url = webServiceDao.queryUrl("dualCirResCheck");
            Map<String,Object> respMap = xmlUtil.sendHttpPostOrderCenter(url, requestObj.toString());
            String code = MapUtils.getString(respMap,"code","");
            if("200".equals(code)){
                response = MapUtils.getString(respMap,"msg");
                JSONObject respJson = JSONObject.parseObject(response);
                if("200".equals(respJson.getString("CODE"))){
                    JSONObject dualCirResCheckRsp = JSONObject.parseObject(respJson.getString("DUAL_CIR_RES_CHECK_RSP"));
                    Map<String,Object> cirResCheckMap = new HashMap();
                    String chkCode = dualCirResCheckRsp.getString("CHK_CODE");
                    cirResCheckMap.put("CHK_CODE",chkCode);
                    cirResCheckMap.put("CHK_MESS",dualCirResCheckRsp.getString("CHK_MESS"));
                    cirResCheckMap.put("CHK_LIMIT_TIME",dualCirResCheckRsp.getString("CHK_LIMIT_TIME"));
                //    cirResCheckMap.put("CHK_RES_TXT",dualCirResCheckRsp.getString("CHK_RES_TXT"));
                    cirResCheckMap.put("srvOrdId",srvOrdId);
                    // 解析A端的核查结果
                    Map<String,Object> cirResCheckMapA = setResCheckMap(dualCirResCheckRsp,"");
                    Map<String,Object> cirResCheckMapZ = new HashMap();

                    // 更新入库
                    saveAutoCheckInfo(cirResCheckMap);
                    if("1".equals(chkCode)){
                        cirResCheckMap.put("CHK_CODE","具备资源");
                    } else {
                        cirResCheckMap.put("CHK_CODE","不具备资源");
                    }
                 //   cirResCheckMap.put("chkResList",list);
                    retMap.put("success",true);
                    retMap.put("data",cirResCheckMap);
                } else{
                    retMap.put("success",false);
                    retMap.put("message","接口返回失败，失败原因："+respJson.getString("MESS"));
                    return retMap;
                }
            } else{
                retMap.put("success",false);
                retMap.put("message","接口调用失败");
            }
        } catch (Exception e) {
            retMap.put("success", false);
            retMap.put("message", "接口调用失败");
        } finally {
            insertInterfaceLog(srvOrdId,requestObj.toString(),JSONObject.toJSONString(constructResponse),url);
        }
        return  retMap;

    }

    private Map<String, Object> setResCheckMap(JSONObject dualCirResCheckRsp, String nodeName) {
        Map<String,Object> cirResCheckMap = new HashMap();
        List<Map<String,Object>> list = new ArrayList<>();
        if(dualCirResCheckRsp.keySet().contains(nodeName)){

       /* if(dualCirResCheckRsp.keySet().contains("CHK_RES_LIST")){}
            JSONArray chkResList = JSON.parseArray(dualCirResCheckRsp.getString("CHK_RES_LIST"));
            Iterator it = chkResList.iterator();
            while (it.hasNext()) {
                JSONObject chkResJson = (JSONObject) it.next();
                Map<String, Object> chkResMap = JSONObject.parseObject(String.valueOf(chkResJson),Map.class);
                chkResMap.put("SRV_ORD_ID",srvOrdId);
                chkResMap.put("RESOURCES","autoCheck");
                chkResMap.put("STATE","10A");
                webServiceDao.insertAutoChk(chkResMap);
                list.add(chkResMap);
            }*/
        }
        return cirResCheckMap;

    }

    /**
     * 记录接口日志
     * @param request
     * @param respone
     */
    private void insertInterfaceLog(String tradeId,String request,String respone,String url){
        Map<String,Object> interflog = new HashMap<String, Object>();
        interflog.put("INTERFNAME","双线资源核查接口");
        interflog.put("URL",url);
        interflog.put("CONTENT",request);
        interflog.put("RETURNCONTENT",respone);
        interflog.put("ORDERNO",tradeId);
        interflog.put("REMARK","发送工建系统 json报文");
        webServiceDao.insertInterfLog(interflog);
    }

    /**
     * 带宽速率转换，单位全部转成M
     * @param cirRate
     * @return
     */
    private String transSize(String cirRate){
        String temp = "";
        // 20M/20MBPS
        String[] rateList = cirRate.split("/");
        String rate = rateList[0];
        if(rate.contains("MBPS")){
            temp = rate.replace("MBPS","");// 直接去掉MBPS
        } else if(rate.contains("GBPS")){
            double tmpLong = Double.parseDouble(rate.replace("GBPS",""));
            temp = String.valueOf(tmpLong*1024);
        } else if(rate.contains("M")){
            double tmpLong = Double.parseDouble(rate.replace("M",""));
            temp = String.valueOf(tmpLong);
        } else {
            temp = "0";
        }
        return temp;
    }

    /**
     * 保存自动化核查信息
     * @param autoChkInfo
     */
    private void saveAutoCheckInfo(Map<String,Object> autoChkInfo){
        // 入库自动化核查结果标志
        webServiceDao.updateChkCode(autoChkInfo);
        // 保存大字段文本至路由表
        String chkResult = "CHK_MESS,CHK_LIMIT_TIME,CHK_RES_TXT";
        String[] chkStr = chkResult.split(",");
        for(String key : chkStr){
            if(autoChkInfo.keySet().contains(key)){
                Map<String,Object> tempMap = new HashMap<>();
                tempMap.put("srvOrdId",MapUtils.getString(autoChkInfo,"srvOrdId"));
                tempMap.put("rfsId","");
                tempMap.put("resId","");
                tempMap.put("compId","");
                tempMap.put("attrCode",key);
                tempMap.put("attrValue",MapUtils.getString(autoChkInfo,key));
                tempMap.put("resources","autoCheck");
           //     (RES_ROUTE_ID,SRV_ORD_ID,RFSID,RESID,COMPID,ATTR_CODE,ATTR_VALUE,ATTR_NAME,RESOURCES,CREATE_DATE)
                webServiceDao.saveResRoute(tempMap);
            }
        }
    }
}
