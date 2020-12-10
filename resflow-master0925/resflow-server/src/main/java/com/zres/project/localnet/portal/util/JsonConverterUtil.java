package com.zres.project.localnet.portal.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.webservice.dto.JiKeCustomInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Description:转换器：key都为大写且带下划线的map和javabean的互相转换
 * @Author:zhang.kaigang
 * @Date:2019/6/20
 * @Version:1.0
 */
public class JsonConverterUtil {

    private static Logger logger = LoggerFactory.getLogger(JsonConverterUtil.class);

    /**
     * 将key为大写且带下划线的map转成javabean
     * @param upperKeyMap 大写且带下划线的map
     * @param instance
     * @param <T>
     * @return
     */
    public static <T> T convertUpperKeyMap2Bean(Map upperKeyMap, Class<T> instance){
        try {
            // 将key全部变成小写
            Map lowerKeyMap = JsonConverterUtil.transformUpperCase(upperKeyMap);
            String mapStr = JSON.toJSONString(lowerKeyMap);
            // 下划线转成驼峰
            Map resultMap = (Map)JsonConverterUtil.convertJsonStr(mapStr);
            // MapConverterUtil.convertMap2Bean需要用这个方法，因为这个方法里有处理枚举值
            T object = (T)MapConverterUtil.convertMap2Bean(resultMap, instance);
            return object;
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 将map中的key全部转换成小写
     * @param orgMap
     * @return
     */
    public static Map<String, Object> transformUpperCase(Map<String, Object> orgMap) {
        Map<String, Object> resultMap = new HashMap<>();
        if (orgMap == null || orgMap.isEmpty()) {
            return resultMap;
        }
        Set<String> keySet = orgMap.keySet();
        for (String key : keySet) {
            String newKey = key.toLowerCase();
            resultMap.put(newKey, orgMap.get(key));
        }
        return resultMap;
    }

    /**
     * 将json带下划线字符串转成驼峰
     * @param json
     * @return
     */
    public static Object convertJsonStr(String json) {
        Object obj = JSON.parse(json);
        convert(obj);
        return obj;
    }

    public static void convert(Object json) {
        if (json instanceof JSONArray) {
            JSONArray arr = (JSONArray) json;
            for (Object obj : arr) {
                convert(obj);
            }
        }
        else if (json instanceof JSONObject) {
            JSONObject jo = (JSONObject) json;
            Set<String> keys = jo.keySet();
            String[] array = keys.toArray(new String[keys.size()]);
            for (String key : array) {
                Object value = jo.get(key);
                String[] key_strs = key.split("_");
                if (key_strs.length > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < key_strs.length; i++) {
                        String ks = key_strs[i];
                        if (!"".equals(ks)) {
                            if (i == 0) {
                                sb.append(ks);
                            }
                            else {
                                int c = ks.charAt(0);
                                if (c >= 97 && c <= 122) {
                                    int v = c - 32;
                                    sb.append((char) v);
                                    if (ks.length() > 1) {
                                        sb.append(ks.substring(1));
                                    }
                                }
                                else {
                                    sb.append(ks);
                                }
                            }
                        }
                    }
                    jo.remove(key);
                    jo.put(sb.toString(), value);
                }
                convert(value);
            }
        }
    }

    /**
     * 测试方法，后续删除
     * @param args
     */
    public static void main(String[] args) {
        String jsonStr = "{\"CUST_ID\":\"5050150252170002809\",\"CUST_TEL\":\"\",\"CUST_CONTACT_MAN_NAME\":\"赵迪\",\"DEAL_AREA_CODE\":\"501\",\"TRADE_STAFF_PHONE\":\"18601221333\",\"CONTRACT_ID\":\"CU15-5001-2013-000003\",\"SRV_ORD_LIST\":{\"SRV_ORD\":[{\"SERVICE_OFFER_ID\":\"100000177\",\"SERIAL_NUMBER\":\"501HLW012730\",\"SERVICE_ID\":\"80000017\",\"USER_ID\":\"1392000003453074\",\"SRV_ORD_INFO\":{\"SRV_ATTR_INFO\":[{\"ATTR_VALUE\":\"1\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"10000193\"},{\"ATTR_VALUE\":\"2\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"10000199\"},{\"ATTR_VALUE\":\"11\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"10000192\"},{\"ATTR_VALUE\":\"1\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"10000194\"},{\"ATTR_VALUE\":\"1\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"10000198\"},{\"ATTR_VALUE\":\"2\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"10001104\"},{\"ATTR_VALUE\":\"29\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"10001110\"},{\"ATTR_VALUE\":\"1\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"10001113\"},{\"ATTR_VALUE\":\"2\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"10001117\"},{\"ATTR_VALUE\":\"0\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"10001121\"},{\"ATTR_VALUE\":\"0\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"10001102\"},{\"ATTR_VALUE\":\"1\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"10001111\"},{\"ATTR_VALUE\":\"1\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"10000100\"},{\"ATTR_VALUE\":\"0\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"10000930\"},{\"ATTR_VALUE\":\"0\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"CON0001\"},{\"ATTR_VALUE\":\"0\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"CON0002\"},{\"ATTR_VALUE\":\"501\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"CON0005\"},{\"ATTR_VALUE\":\"海南市经济技术开发区\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"CON0007\"},{\"ATTR_VALUE\":\"50\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"CON0101\"},{\"ATTR_VALUE\":\"501\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"CON0103\"},{\"ATTR_VALUE\":\"20190510000000\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"CON0014\"},{\"ATTR_VALUE\":\"1\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"CON0095\"},{\"ATTR_VALUE\":\"0\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"CON0093\"},{\"ATTR_VALUE\":\"1\",\"ATTR_ACTION\":\"0\",\"ATTR_CODE\":\"CON0094\"}]},\"TRADE_ID_RELA\":\"16000000424161\",\"FLOW_ID\":\"440207212\",\"TRADE_TYPE_CODE\":\"2001\",\"TRADE_ID\":\"16000000424284\",\"ACTIVE_TYPE\":\"4A\"}]},\"CONTRACT_NAME\":\"张慧42合同\",\"CUST_ADDRESS\":\"辽宁省大连出口加工区A区气体工业园F1-1栋厂房\",\"ACCEPT_DATE\":\"20190509114415\",\"CUST_CITY\":\"501\",\"CUST_CONTACT_MAN_TEL\":\"13411111111\",\"NETWORK_LEVEL\":\"\",\"DEPART_NAME\":\"海南省分公司海口市分公司\",\"CUST_NAME_CHINESE\":\"阿波罗(大连)照明制品有限公司\",\"CUST_EMAIL\":\"\",\"TRADE_EPARCHY_CODE\":\"501\",\"TRADE_STAFF_NAME\":\"海南计费\",\"REMARK\":\"工号：hnbill对客户阿波罗(大连)照明制品有限公司进行产品订购操作！\",\"CUST_FAX\":\"\"}\n";
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Map targetMap = jsonObject;
        // 移除无用元素
        targetMap.remove("SRV_ORD_LIST");
        JiKeCustomInfoDTO jiKeCustomInfoDTO = convertUpperKeyMap2Bean(jsonObject, JiKeCustomInfoDTO.class);
        System.out.println(jiKeCustomInfoDTO.getCustId());
    }
}
