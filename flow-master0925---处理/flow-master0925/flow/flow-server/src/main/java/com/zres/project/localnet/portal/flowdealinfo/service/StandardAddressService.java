package com.zres.project.localnet.portal.flowdealinfo.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.InsertOrderInfoDao;
import com.zres.project.localnet.portal.until.service.UntilService;
import com.zres.project.localnet.portal.util.XmlUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.core.util.ListUtil;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class StandardAddressService implements StandardAddressIntf {
    private static final Logger logger = LoggerFactory.getLogger(StandardAddressService.class);

    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private InsertOrderInfoDao insertOrderInfoDao;
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private UntilService untilService;
    @Autowired
    private XmlUtil xmlUtil;

    @Override
    public Map<String, Object> queryIsBussSpecialty(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("NEED_SHOW", false); //查询配置信息查询该省份是否需要录入标准地址
        resultMap.put("BUSSINESS_SPECIALTY", false); //商务专线
        resultMap.put("STANDARD_ADDRESS", ""); //标准地址
        try {
            String srvOrdId = MapUtil.getString(paramMap, "srvOrdId");
            String orderId = MapUtil.getString(paramMap, "orderId");
            int ifNum = orderDealDao.qryAccessAutoConfig(orderId);
            if (ifNum > 0) {
                resultMap.put("NEED_SHOW", true);
                List<Map<String, Object>> standardAddressId = insertOrderInfoDao.queryAttrInfos("200002617", srvOrdId); //标准地址id
                List<Map<String, Object>> address = insertOrderInfoDao.queryAttrInfos("CON0007", srvOrdId);//装机地址
                String subType = orderDealDao.qrySubType(srvOrdId);
                if ("5".equals(subType)) {
                    resultMap.put("BUSSINESS_SPECIALTY", true);
                }
                if (!ListUtil.isEmpty(standardAddressId) && !ListUtil.isEmpty(address)) {
                    resultMap.put("STANDARD_ADDRESS", MapUtil.getString(address.get(0), "ATTR_VALUE")); //标准地址
                }
            }
        } catch (Exception e) {
            logger.error("商务专线初始化电路调度提交页面查询失败\n{}", e.getMessage());
        }
        return resultMap;
    }

    @Override
    public List<Map> queryStandardAddressInfo(Map<String, Object> params) {
        Base64.Encoder encoder = Base64.getEncoder();
        List<Map> result = new ArrayList<Map>();
        Map retmap = new HashMap();
        Map<String, Object> aiResponse = new HashMap<>();
        String updatedate = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式

        //查询参数
        String json = null;
        String headStr = "";
        try {
            //生成json报文
            JSONObject request = new JSONObject();
            String userId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            Map<String, Object> staffMap = orderDealDao.getOperStaffInfo(Integer.valueOf(userId));
            //地市ID和省份ID转化
            String cityId = orderDealDao.querySwitchCode(MapUtil.getString(params, "regionCity"),"CITY_CODE");
            String provinceId = orderDealDao.querySwitchCode( MapUtil.getString(staffMap, "AREA_ID"),"PROVINCE_CODE");
            request.put("pageNum",String.valueOf(Integer.parseInt(MapUtil.getString(params, "pageIndex"))-1));//分页开始行
            request.put("pageSize", MapUtil.getString(params, "pageSize"));//分页结束行
            request.put("addressName", MapUtil.getString(params, "addressName")); //地址名
            request.put("cityId", cityId); //地市ID，对应统一资源库行政区域表区域等级对应省（2000022）类型的行政区域ID，长度为24位
            request.put("provinceId", provinceId); //省份ID，对应统一资源库行政区域表区域等级对应省（2000021）类型的行政区域ID，长度为24位
            json = request.toJSONString();

            String bodyJson = request.toJSONString();
            headStr = xmlUtil.generateHead(bodyJson);
            json = request.toJSONString();
        } catch (NumberFormatException e) {
            retmap.put("RESP_CODE", "失败");
            retmap.put("RESP_DESC", "标准地址查询拼接报文异常！异常信息：" + e.getMessage() + "\n" + json);
            result.add(retmap);
            return result;
        }
        try {
            //查询url
            String url = wsd.queryUrl("STANDARD_ADDRESS");
            //获取authentication
            byte[] headStrByte = headStr.getBytes("UTF-8");
            String authentication = encoder.encodeToString(headStrByte);
            aiResponse = xmlUtil.sendDataSharingPlatform(url, json, authentication);
            logger.info("------返回报文---response: " + aiResponse);
            if ("200".equals(aiResponse.get("code"))) {
                JSONObject response = JSONObject.parseObject(aiResponse.get("msg").toString());
                JSONObject responseHead = JSONObject.parseObject(response.get("UNI_NET_HEAD").toString());
                JSONObject responseBody = JSONObject.parseObject(response.get("UNI_NET_BODY").toString());
                String resp_code = responseHead.getString("RESP_CODE");
                String message = responseHead.getString("RESP_DESC");
                if ("0000".equals(resp_code)) {
                    JSONArray dataList = responseBody.getJSONArray("data");
                    if (dataList != null & dataList.size() > 0) {
                        for (int i = 0; i < dataList.size(); i++) {
                            JSONObject dataObj = dataList.getJSONObject(i);
                            Map<String, Object> addMap = new HashMap<>();
                            addMap.put("addressCode", dataObj.getString("addressFrom"));
                            addMap.put("addressName", dataObj.getString("addressName"));
                            result.add(addMap);
                        }
                    } else {
                        retmap.put("RESP_CODE", "失败");
                        retmap.put("RESP_DESC", "查询结果为空");
                        result.add(retmap);
                        return result;
                    }
                } else {
                    retmap.put("RESP_CODE", "失败");
                    retmap.put("RESP_DESC", "查询异常-反馈信息：" + message);
                    result.add(retmap);
                    return result;
                }

            } else {
                logger.error("调用标准地址查询接口失败");
                retmap.put("RESP_CODE", "失败");
                retmap.put("RESP_DESC", "调用标准地址查询接口失败");
                result.add(retmap);
                return result;
            }
            updatedate = df.format(new Date());
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("interfname", "标准地址查询接口");
            map.put("url", url);
            map.put("content", json);
            map.put("createdate", updatedate);
            map.put("returncontent", aiResponse.toString());
            map.put("remark", "接收上海AI中心返回报文");
            map.put("updatedate", updatedate);
            //5.报文入库，数据入库
            wsd.saveJson(map);
        } catch (Exception e) {
            logger.error("调用标准地址查询接口异常！异常信息：" + e.getMessage());
            //    e.printStackTrace();
            retmap.put("RESP_CODE", "失败");
            retmap.put("RESP_DESC", "调用标准地址查询接口异常！：" + e.getMessage() + "\n" + json);
            result.add(retmap);
            return result;
        }
        return result;
    }


}
