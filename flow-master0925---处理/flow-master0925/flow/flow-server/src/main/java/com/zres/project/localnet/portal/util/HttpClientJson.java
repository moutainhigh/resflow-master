package com.zres.project.localnet.portal.util;

import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import net.sf.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * client of httpServer
 *
 * @author kml
 */
public class HttpClientJson {

    private static WebServiceDao webServiceDao = SpringContextHolderUtil.getBean("webServiceDao"); //数据库操作-对象

    /**
     * 客户端调用数据，返回数据
     */
    public static Map sendHttpPost(String url, String body) {
        String responseContent = null;
      //  CloseableHttpClient httpClient = null;
        HttpClient httpClient = null;
        Map<String, Object> map = new HashMap<String, Object>();
        String retStr1 = body.replace("\\n","");
        String retStr2 = retStr1.replace("\\t","");
        String retStr4 = retStr2.replace("\\r","");
        try {
            httpClient = new HttpClient();
            PostMethod method = new PostMethod(url);
            method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"utf-8");
       //     method.setParameter("body",body);
            method.setParameter("body",retStr4);
        /*    HttpMethodParams param = new HttpMethodParams();//method.getParams();
            param.setContentCharset("UTF-8");
            method.setParams(param);*/

            int code = httpClient.executeMethod(method);
            String str=method.getResponseBodyAsString();

            map.put("msg", str);
            map.put("code", String.valueOf(code));
        }
        catch (Exception e) {
            //e.printStackTrace();
            map.put("msg", "接口调用失败，请联系管理员！" + e);
            map.put("code", "接口异常");
        }
        finally {
          //  httpClient.getConnectionManager().shutdown();
            //postMethod.releaseConnection();
        }
        return map;
    }

    public static JSONObject requestHeader(Map map, String serviceCode) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss"); //设置日期格式
        Random rand = new Random();
        String  exchangeid = "120301" + df.format(new Date()) + (rand.nextInt(9999) + 10);
        if (exchangeid.length() < 24) {
            exchangeid = exchangeid + "6";
        }
        JSONObject requestHeader = new JSONObject();
        requestHeader.put("exchangeId", exchangeid);
        requestHeader.put("clientId", "120301");
        requestHeader.put("password", "");
        requestHeader.put("serviceCode", serviceCode);
        requestHeader.put("serviceId", "");
        requestHeader.put("requestTime", df.format(new Date()));
        // 资源补录标志
        boolean resSupplement = BasicCode.ACTIVE_TYPE_SUPPLEMENT.equals(MapUtils.getString(map,"ACTIVE_TYPE"));
        if(resSupplement){
            requestHeader.put("partitionId",MapUtils.getString(map, "REGION_ID","") );
        } else{
            String regionCodeName = map.get("HANDLE_DEP_ID").toString();
            String regionCode = webServiceDao.qryRegionCode(regionCodeName);
            requestHeader.put("partitionId", regionCode==null?"":regionCode);
        }

        String operator = "";
        if (ThreadLocalInfoHolder.getLoginUser()!=null){
             operator = ThreadLocalInfoHolder.getLoginUser().getLoginName();
        }
        requestHeader.put("operator", operator==null?"":operator);

        return requestHeader;
    }
    public static Object checkNull(Map map, String key) {
        if (null == map.get(key)) {
            return "";
        }
        return map.get(key);
    }
    public static Object checkNull2(Map map, String key) {
        if (null == map.get(key) || "".equals(map.get(key))) {
            return 0;
        }
        return map.get(key);
    }

    public static Map<String, String> httpPostRequest(String url, String body) {
        String responseContent = null;
        org.apache.http.client.HttpClient httpClient = null;
        Map<String, String> map = new HashMap<String, String>();
        try {
            httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            //httpPost.addHeader("Content-Type", "text/plain;charset=utf-8");
            httpPost.setEntity(new StringEntity(body, "utf-8"));

            HttpResponse response = httpClient.execute(httpPost);
            System.out.println(response.getStatusLine().getStatusCode() + "\n");
            HttpEntity entity = response.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
            System.out.println(responseContent);
            map.put("msg", responseContent);
            map.put("code", "200");
        } catch (Exception e) {
            e.printStackTrace();
            map.put("msg", "接口调用失败，请联系管理员！" + e);
            map.put("code", "300");
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return map;
    }

}

