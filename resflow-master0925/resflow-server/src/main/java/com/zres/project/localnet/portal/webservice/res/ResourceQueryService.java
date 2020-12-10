package com.zres.project.localnet.portal.webservice.res;

import com.sun.mail.iap.ByteArray;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.util.HttpClientJson;
import com.zres.project.localnet.portal.util.SpringContextHolderUtil;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import org.apache.commons.collections4.map.MultiValueMap;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 资源接口-客户端
 * 资源查询接口实现
 * Created by csq on 2019/3/5.
 */
@Service
public class ResourceQueryService implements ResourceQueryServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(ResourceQueryService.class);
    @Autowired
    private WebServiceDao rsd; //数据库操作-对象

    public List<Map<String,Object>> resourceQuery(Map intMap) { //crm产品实例标识
        OrderDealDao orderDealDao = SpringContextHolderUtil.getBean("orderDealDao");
        WebServiceDao rsd = SpringContextHolderUtil.getBean("webServiceDao"); //数据库操作-对象

        logger.info("机房查询接口开始！" + intMap);
        String province = intMap.get("province").toString();
        String city = intMap.get("city").toString();
        String startRow = intMap.get("beginNum").toString();
        int  begin = Integer.parseInt(intMap.get("beginNum").toString());
        int  end = Integer.parseInt(intMap.get("endNum").toString());
        int endRow = (begin-1)*10+end;
        String resourceName = intMap.get("resourceName").toString();
        /*
        请求类型：机房(MACHROOM) 、放置点（POINT）
         */
        String searchType = MapUtil.getString(intMap,"searchType","MACHROOM");
        String isExist ;
        String json = "";
        String url = rsd.queryUrl("queryResource");
        String zyResponse = "";
        List<Map<String,Object>> allMap = new ArrayList<Map<String,Object>>();
        Map retmap = new HashMap();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        String createdate = "";
        String updatedate = "";
        JSONObject request = new JSONObject();

        try {
            //生成json报文
            JSONObject jsonObj = new JSONObject();
            JSONObject requestBody = new JSONObject();
            JSONObject requireData = new JSONObject();
            //productType= "20181211001";
            requireData.put("SEARCH_TYPE", searchType);
            requireData.put("PROVINCE_CODE", rsd.qryCrmRegion(province));
            requireData.put("EPARCHY_CODE", rsd.qryCrmRegion(city));
            requireData.put("MACROOM_NAME", resourceName);
            requireData.put("START_ROW", startRow);
            requireData.put("END_ROW", endRow);
            /*requireData.put("circuitCode", "20181300000005");
            requireData.put("productId", "20181211001");*/
            requestBody.put("MACHROOM_API_REQ", requireData);
            request.put("UNI_BSS_BODY", requestBody);
            Map map = new HashMap();

            String userId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            Map<String, Object> staffMap = orderDealDao.getOperStaffInfo(Integer.valueOf(userId));
            String orgId = MapUtil.getString(staffMap, "ORG_ID") ;
            map.put("HANDLE_DEP_ID",orgId);
            request.put("requestHeader", HttpClientJson.requestHeader(map, "queryResource"));
            //String jsonStr = JSONObject.toJSONString(request);
            //jsonObj.put("request", request);
            json = request.toString();
            //json = new String(json.toString().getBytes("UTF-8"));
            //createdate = df.format(new Date());
            logger.info("发送报文：" + json);
        }
        catch (Exception e) {
            logger.error("拼接报文异常！异常信息：" + e.getMessage(),e);
            retmap.put("RESP_CODE", "失败");
            retmap.put("RESP_DESC", "拼接报文异常！异常信息：" + e.getMessage()+"/n"+json);
            allMap.add(retmap);
            return allMap;
        }

            logger.info("资源接口地址：" + url);
        try {
            //3.调对方接口，发json报文 ，接收返回json报文
            HttpHeaders requestHeaders = new HttpHeaders();


            requestHeaders.add("Accept","application/json");
            requestHeaders.add("Context-Type","application/json; charset=utf-8");

            HttpEntity<String> requestEntity = new HttpEntity<String>(json, requestHeaders);
            RestTemplate restTemplate = new RestTemplate();
            //ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            ResponseEntity<String> response = restTemplate.postForEntity(url, json.getBytes("UTF-8"), String.class);
            zyResponse = response.getBody();
            //Map respons = HttpClientJson.sendHttpPost(url, json);
            //zyResponse = respons.get("msg").toString();
            updatedate = df.format(new Date());
            logger.info("资源返回报文：" + zyResponse);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("interfname", "机房查询接口");
            map.put("url", url);
            map.put("content", json);
            map.put("createdate", updatedate);
            map.put("returncontent", zyResponse);
            //map.put("orderno", circuitCode);
            map.put("remark", "接收资源返回报文");
            map.put("updatedate", updatedate);
            //5.报文入库，数据入库
            rsd.saveJson(map);
        }
        catch (Exception e) {
            logger.error("调资源接口异常！异常信息：" + e.getMessage(),e);
            retmap.put("RESP_CODE", "失败");
            retmap.put("RESP_DESC", "调资源接口异常！：" + e.getMessage()+"/n"+json);
            allMap.add(retmap);
            return allMap;
        }
        try {
            //4.解析json报文
           // String ss = "{\"UNI_BSS_BODY\": {\"MACHROOM_API_RSP\": {\"MACHROOM_LIST\": [{\"PROVINCE_CODE\": \"北京\",\"EPARCHY_CODE\": \"北京\",\"MACROOM_NUM\": \"1\",\"MACROOM_NAME\": \"beijing\",\"MACROOM_ADD\": \"123\",\"MACROOM_LEVEL\": \"2\"}],\"RESP_CODE\": \"0000\",\"RESP_DESC\": \"\",\"MACROOM_COUNT\": \"1\"}}}";
            //JSON respJson = new XMLSerializer().read(zyResponse);
            //String jsonString = getBodyString(request.getReader());
            Document doc = null;
                // 下面的是通过解析xml字符串的
            doc = DocumentHelper.parseText(zyResponse); // 将字符串转为XML
            Element rootElt = doc.getRootElement(); // 获取根节点
            Element UNI_BSS_BODY = rootElt.element("UNI_BSS_BODY");

            Element MACHROOM_API_RSP = UNI_BSS_BODY.element("MACHROOM_API_RSP");
            //Element MACHROOM_API_RSP = MACHROOM_API_RSP.element("MACHROOM_API_RSP");
            //JSONObject retJson = JSONObject.fromObject(zyResponse);
            //JSONObject responseData = retJson.getJSONObject("UNI_BSS_BODY");
            //JSONObject response = responseData.getJSONObject("MACHROOM_API_RSP");
            isExist = MACHROOM_API_RSP.elementTextTrim("RESP_CODE");
            String errMsg = MACHROOM_API_RSP.elementTextTrim("RESP_DESC");

            if(isExist == "0001" || "0001".equals(isExist)){
                retmap.put("RESP_CODE", "0001");
                retmap.put("RESP_DESC","请检查输入的内容是否正确!---返回报文："+zyResponse);
                allMap.add(retmap);
                return allMap;
            }else if(isExist == "0000" || "0000".equals(isExist)) {
                String macroomCount = MACHROOM_API_RSP.elementTextTrim("MACROOM_COUNT");//机房数量
                Iterator MACROOM_LIST = MACHROOM_API_RSP.elementIterator("MACROOM_LIST");
                if(MACROOM_LIST == null || macroomCount=="0"){
                    retmap.put("RESP_CODE", "8888");
                    retmap.put("RESP_DESC", "未查到该机房信息"+"---返回报文："+zyResponse);
                    allMap.add(retmap);
                    return allMap;
                }else{
                    int j =0;
                    while (MACROOM_LIST.hasNext()) {//遍历保存查询到的记录
                        Map<String, Object> map = new HashMap<String, Object>();
                        if(j==0) {
                            map.put("RESP_CODE", "0000");
                            map.put("macroomCount", macroomCount);
                        }
                        Element itemEle = (Element) MACROOM_LIST.next();
                        map.put("PROVINCE_CODE", itemEle.elementTextTrim("PROVINCE_CODE"));
                        map.put("EPARCHY_CODE", itemEle.elementTextTrim("EPARCHY_CODE"));
                        map.put("MACROOM_NUM", itemEle.elementTextTrim("MACROOM_NUM"));
                        map.put("MACROOM_NAME", itemEle.elementTextTrim("MACROOM_NAME"));
                        map.put("MACROOM_ADD", itemEle.elementTextTrim("MACROOM_ADD"));
                        map.put("MACROOM_LEVEL", itemEle.elementTextTrim("MACROOM_LEVEL"));
                        map.put("ROWNO", itemEle.elementTextTrim("ROWNO"));
                        allMap.add(map);
                        j++;
                    }
                    for (int i = j; i < Integer.parseInt(macroomCount); i++) {//补充保存待查询机房
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("PROVINCE_CODE", "");
                        map.put("EPARCHY_CODE", "");
                        map.put("MACROOM_NUM", "");
                        map.put("MACROOM_NAME", "");
                        map.put("MACROOM_ADD", "");
                        map.put("MACROOM_LEVEL", "");
                        map.put("ROWNO", "");
                        allMap.add(map);
                    }
                }
            }else if(isExist == "8888" || "8888".equals(isExist)) {
                retmap.put("RESP_CODE", "8888");
                retmap.put("RESP_DESC", "其它错误"+"---返回报文："+zyResponse);
                allMap.add(retmap);
                return allMap;
            }
            else if(isExist == "0002" || "0002".equals(isExist)) {
                retmap.put("RESP_CODE", "0002");
                retmap.put("RESP_DESC", "请检查输入的起始行结束行是否正确！"+"---返回报文："+zyResponse);
                allMap.add(retmap);
                return allMap;
            }
            else if(isExist == "0003" || "0003".equals(isExist)) {
                retmap.put("RESP_CODE", "0003");
                retmap.put("RESP_DESC", "请检查输入的省份编码是否正确!"+"---返回报文："+zyResponse);
                allMap.add(retmap);
                return allMap;
            }
        }
        catch (Exception e) {
            logger.error("接口返回报文格式异常！异常信息：" + e.getMessage(),e);
            retmap.put("RESP_CODE", "失败");
            retmap.put("RESP_DESC", "接口返回报文格式异常！返回报文：：" + zyResponse);
            allMap.add(retmap);
            return allMap;
        }
        return allMap;
    }
}
