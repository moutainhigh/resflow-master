package com.zres.project.localnet.portal.util;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by tang.huili on 2019/4/29.
 */
@Service
public class XmlUtil {

    private static final Logger logger = LoggerFactory.getLogger(XmlUtil.class);

    @Autowired
    private WebServiceDao webServiceDao;

    /**
     * 客户端调用数据，返回数据
     */
    public Map<String, Object> sendHttpPostOrderCenter(String url, String body)  {
        String responseContent = null;
        HttpClient httpClient=null;
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            httpPost.setEntity(new StringEntity(body,"utf-8"));

            HttpResponse response = httpClient.execute(httpPost);
            System.out.println(response.getStatusLine().getStatusCode() + "\n");
            HttpEntity entity = response.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
            System.out.println(responseContent);
            map.put("msg",responseContent);
            map.put("code","200");
        }catch(Exception e) {
            logger.error("接口调用失败，请联系管理员" + e.getMessage(), e);
            map.put("msg","接口调用失败，请联系管理员！");
            map.put("code","300");
        }finally {
            httpClient.getConnectionManager().shutdown();
        }
        return map;
    }

    public Map requestHeader(){
        Random rand = new Random();
        SimpleDateFormat time_formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        SimpleDateFormat time_formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        String responseTime = time_formatter.format(Calendar.getInstance().getTime());
        String transId=responseTime+(rand.nextInt(999999) + 10);
        if(transId.length()<22){
            transId=transId+'6';
        }
        Map<String, String> appIdMap = webServiceDao.queryCodeInfo("param_value","app_id");
        Map<String, String> tokenMap = webServiceDao.queryCodeInfo("param_value","AppSecret");

        String  s=time_formatter2.format(Calendar.getInstance().getTime());
        String TOKEN=gettoToken("APP_ID"+appIdMap.get("CODE_CONTENT")+"TIMESTAMP"+s
                +"TRANS_ID"+transId+tokenMap.get("CODE_CONTENT"));
        System.out.println("token="+TOKEN+"-----------");
        Map  headerMap=new HashMap();
        headerMap.put("APP_ID",appIdMap.get("CODE_CONTENT"));
        headerMap.put("TRANS_ID",transId);
        headerMap.put("TIMESTAMP",s);
        headerMap.put("TOKEN",TOKEN);
        return  headerMap;
    }
    public String  gettoToken(String value){
        MD5  m=new  MD5();
        System.out.println(value+"----------");
        return (m.getMD5ofStr(value)).toLowerCase();

    }
    /*
     * 校验报文的必填项
     * @Param:
     * @Return:
     */
    public Map<String,Object> checkJson(String reqField, JSONObject jsonObject){
        Map<String,Object> checkResultMap = new HashMap<>();
        // begin 取值 CST_ORD_INFO 下的字段
        String[] jcoi = reqField.split(",");
        StringBuffer lackFiled = new StringBuffer();
        Boolean checkFlag = true;
        for (int i = 0; i < jcoi.length; i++) {
            if (!jsonObject.containsKey(jcoi[i])){
                //不包含的字符串传进返回结果中用于提示
                lackFiled.append(jcoi[i]+",");
                checkFlag = false;
                checkResultMap.put("lackFiled",lackFiled);
            }
        }
        checkResultMap.put("checkFlag",checkFlag);
        return checkResultMap;
    }

    /**
     * 调用能力平台  标准地址
     */
    public Map<String, Object> sendDataSharingPlatform(String url, String body,String authentication)  {
        String responseContent = null;
        HttpClient httpClient=null;
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            httpPost.addHeader("Authentication", authentication);
            httpPost.setEntity(new StringEntity(body,"utf-8"));
            HttpResponse response = httpClient.execute(httpPost);
            System.out.println(response.getStatusLine().getStatusCode() + "\n");
            HttpEntity entity = response.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
            System.out.println(responseContent);
            map.put("msg",responseContent);
            map.put("code","200");
        }catch(Exception e) {
            logger.error("接口调用失败，请联系管理员" + e.getMessage(), e);
            map.put("msg","接口调用失败，请联系管理员！");
            map.put("code","300");
        }finally {
            httpClient.getConnectionManager().shutdown();
        }
        return map;
    }

    /*
     * 标准地址 请求报文头
     * @Param:
     * @Return:
     */
    public String generateHead(String body){
        JSONObject json=new JSONObject();
        try {
            Map<String, String> appIdMap = webServiceDao.queryCodeInfo("param_value","APP_ID_DATAPLAT");
            Map<String, String> appSecretMap = webServiceDao.queryCodeInfo("param_value","APPSECRET_DATAPLAT");
            SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-mm-dd HH:MM:ss SSS");
            String TRANS_ID = String.valueOf(System.currentTimeMillis()/1000)+String.valueOf((int)((Math.random()*9+1)*100000));
            String timeStamp = dateFormat.format(Calendar.getInstance().getTime());
            String appId = appIdMap.get("CODE_CONTENT");
            //获取Token APP_IDAPP3948371TIMESTAMP2019-06-22 15:06:06 100TRANS_ID1571742971864431
            String token = gettoToken("APP_ID"+appId+"TIMESTAMP"+timeStamp
                    +"TRANS_ID"+TRANS_ID+body+appSecretMap.get("CODE_CONTENT"));
            //生成请求头
            json.put("APP_ID", appId);
            json.put("TIMESTAMP", timeStamp);
            json.put("TRANS_ID", TRANS_ID);
            json.put("TOKEN", token);
        } catch (Exception e) {
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>拼接请求头失败:{}", e.getMessage());
        }
        return json.toJSONString();
    }


}
