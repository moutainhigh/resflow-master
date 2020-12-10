package com.zres.project.localnet.portal.webservice.disp;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.logInfo.service.TacheDealLogIntf;
import com.zres.project.localnet.portal.util.OrderTrackOperType;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.*;



/**
 * 资源接口-服务端
 * 资源信息返回接口实现
 * Created by csq on 2019/1/2.
 */
@Controller
@RequestMapping("/resReturnServiceIntf")
public class ResReturnService implements ResReturnServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(ResReturnService.class);
    private static final String RESULTS="respCode,respDesc,srvOrderCode,resflowWorkId";
    private static final String CFS="cfsId,cfsVer,prodInstId,prodSpecId,custOrderNo,prodOrderNo,serviceOrderNo,taskNo";
    private static final String MENBERS="resId,resSpecId,resName,aggrResId,aggrResSpec,seq,actionType,resType,rfsMemberResAttr"; //,aggrResName
    private static final String RFS="rfsId,rfsVer,rfsSpecId,rfsSpecVer,rfsAct,systemId,rfsSpecVer";
    private static final String ATTRLIST="attrCode,attrValue,attrName"; //valueDesc
    private static final String RELALIST="rfsId,rfsVer";
    private static final String RFS_MEMBER_RES_ATTR="rfsMemberResAttr";
    private static final String ATTR_VALUE="attrValue";
    private static final String RES_ROUTE="resId,resSpecId,resName,aggrResId,aggrResSpec,seq,actionType,resType";

    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式

    @Autowired
    private WebServiceDao webServiceDao; //数据库操作-对象
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private TacheDealLogIntf tacheDealLogIntf;

    @Override
    @IgnoreSession
    @ResponseBody
    @RequestMapping(value = "/interfaceBDW/resReturnService.spr", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String resReturn(@RequestBody String json) {

        logger.info("调用资源信息返回接口开始!---json：" + json);

        String retJson = "";
        String resflowWorkId = "";
        Boolean resChangeFlag = false;
        Map map = new HashMap();
        //  Map<String, Object> rmap = new HashMap<String, Object>();
        map.put("interfname", "资源信息返回接口");
        map.put("url", "/resReturnServiceIntf/interfaceBDW/resReturnService.spr");
        map.put("content", json);
        map.put("createdate", df.format(new Date()));

        try {
            //1.解析json报文
            JSONObject requests = JSONObject.fromObject(json);
            JSONObject requestJson = requests.getJSONObject("request");
            JSONObject requestBody = requestJson.getJSONObject("requestBody");
            JSONObject result = requestBody.getJSONObject("result");

            if(!"1".equals(result.getString("respCode"))){
                map.put("respCode", "0");
                map.put("respDesc", "返回报文报错：" + json);
                retJson = createRetJson(map);
            }
            String srvOrderId = result.getString("resflowWorkId");
            boolean resSupplement = false;//资源补录标志
            int num = webServiceDao.isResSupplement(srvOrderId);
            if(num > 0){
                resSupplement = true;
            }
            // 子流程工单id
            if(result.keySet().contains("woId")){
                JSONArray orderIdArray = result.getJSONArray("woId");

                if(orderIdArray.size()>0){
                    String orderId =orderIdArray.get(0).toString();
                    if(orderId!=null && !"".equals(orderId)){
                        // select count(*) from where srv_ord_id=#{srvOrdId} and attr_code='orderId' and attr_value=#{orderId}
                       /* String srvOrdIdSupplement = srvOrderId;
                        if(resSupplement){
                            srvOrdIdSupplement = "0";
                        }*/
                        if(webServiceDao.qryNumByOrderId(srvOrderId,orderId)<1){
                            // 入库电路属性表
                            Map<String, Object> rmap = new HashMap<String, Object>();
                            rmap.put("srv_ord_id", srvOrderId);
                            rmap.put("attr_action", "resReturnService");
                            rmap.put("attr_code", "orderId");
                            rmap.put("attr_name", "");
                            rmap.put("attr_value", orderId);
                            rmap.put("attr_value_name", "资源信息返回接口返回的子流程id");
                            rmap.put("create_date", df.format(new Date()));
                            rmap.put("sourse", "res");
                            webServiceDao.saveRetInfo(rmap);
                        }
                    }
                    /*
                     * 判断当前工单状态
                     * 如果为已完成或者已启子流程，则证明为资源修改发起的资源配置。进行发送通知单到当前处理环节
                     */
                    Map<String, Object> woStateMap = orderDealDao.qryWoStateByOrderId(orderId);
                    String woState = MapUtils.getString(woStateMap, "WO_STATE");
                    if(OrderTrackOperType.WO_ORDER_STATE_10.equals(woState) || OrderTrackOperType.WO_ORDER_STATE_4.equals(woState)){
                        resChangeFlag = true;
                    }
                    if (resChangeFlag){
                        //发送通知单到当前订单处理环节
                        /**
                         * 资源修改完提交后将进行主流程及子流程工单消息推送操作 需要工单ID，推送内容
                         * 订单ID， 工单ID，下一环节处理人员、下一环节处理专业，推送内容
                         */
                        Set<String> compIds = getCompIds(requestBody);//获取专业

                        String remark= "资源修改的专业有：";
                        tacheDealLogIntf.writeOrderMessage(orderId, orderId, "资源修改", "dealFlow", remark);
                    }

                }
            }

            logger.info("srvOrderId:"+ srvOrderId);
            // 拼接返回报文
            map.put("respCode", "1");
            map.put("respDesc", "成功");
            map.put("resflowWorkId", resflowWorkId);
            retJson = createRetJson(map);
            map.put("returncontent", retJson);
            map.put("orderno", srvOrderId); //业务订单id
            map.put("remark", "资源信息返回接口");
            map.put("updatedate", df.format(new Date()));
            webServiceDao.saveJson(map);
            Set<String> compIds = getCompIds(requestBody);
            String position =requestBody.getString("position");
            boolean positionFlag = "A".equals(position) || "Z".equals(position);
            for(String compId : compIds){
                if(resSupplement){
                    webServiceDao.deleteResSupple(srvOrderId,compId);
                    webServiceDao.deleteResRouteSupple(srvOrderId,compId);
                }else if(positionFlag){// AZ端核查资源
                    webServiceDao.deleteResRoutePosion(srvOrderId,compId,position);
                }else {
                    webServiceDao.deleteRes(srvOrderId,compId);
                    webServiceDao.deleteResRoute(srvOrderId,compId);
                }
            }
            if(positionFlag){
                getResourceWithPosition(requestBody, "newResource",srvOrderId,position);
            }else{
                getResource(requestBody, "newResource",srvOrderId);
                getResource(requestBody, "oldResource",srvOrderId);
            }
            if(!resSupplement && !positionFlag){
                // 更新电路名称
                updateCircuitCode(requestBody, srvOrderId);
            }

        }
        catch (Exception e) {
            map.put("respCode", "0");
            map.put("respDesc", "报文解析出错！请检查报文格式是否正确:" + json);
            retJson = createRetJson(map);
            map.put("returncontent", retJson);
            map.put("updatedate", df.format(new Date()));
            map.put("orderno", ""); //业务订单id
            map.put("remark", "资源信息返回接口报文格式异常");
            webServiceDao.saveJson(map);
            return retJson;
        }
        logger.info("调用资源信息返回接口结束!---retJson：" + retJson);
        return retJson;
    }

    public String createRetJson(@RequestBody Map map) {
        //2.拼返回报文
        JSONObject json = new JSONObject();
        JSONObject response = new JSONObject();
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("respCode", map.get("respCode"));
            requestBody.put("respDesc", map.get("respDesc"));
        //    requestBody.put("resflowWorkId", map.get("resflowWorkId"));
            response.put("responseBody", requestBody);
            json.put("response", response);
        }
        catch (Exception e) {
        }
        return json.toString();
    }

    public Set<String> getCompIds(JSONObject requestBody){
        Set<String> compIdSet = new HashSet<String>();
        if(requestBody.keySet().contains("newResource")){
            JSONObject newResource = requestBody.getJSONObject("newResource");
            JSONArray rfsList = newResource.getJSONArray("rfsList");
            for (int i = 0; i < rfsList.size(); i++) {
                compIdSet.add(rfsList.getJSONObject(i).getString("compId"));
            }
        }
        if(requestBody.keySet().contains("oldResource")){
            JSONObject oldResource = requestBody.getJSONObject("oldResource");
            JSONArray rfsList2 = oldResource.getJSONArray("rfsList");
            for (int i = 0; i < rfsList2.size(); i++) {
                compIdSet.add(rfsList2.getJSONObject(i).getString("compId"));
            }
        }
        return compIdSet;
    }
    public String getResource(JSONObject requestBody, String resources,String srvOrderId) {
        logger.info("----srvOrderId--"+ srvOrderId);
        String resId = null;
        if(requestBody.keySet().contains(resources)){
            JSONObject newResource = requestBody.getJSONObject(resources);
            JSONArray rfsList = newResource.getJSONArray("rfsList");
            for (int i = 0; i < rfsList.size(); i++) {
                //遍历JSONArray中的每一个对象
                JSONObject  rfs= rfsList.getJSONObject(i);
                String rfsId = rfs.getString("rfsId");
                String compId = rfs.getString("compId");
                //获得每一个对象中的members数组
                JSONArray members = rfs.getJSONArray("members");
                if(members!=null && members.size()>0){
                    for (int j = 0; j < members.size(); j++) {
                        //获取attrListArray数组中每一个对象
                        JSONObject member = members.getJSONObject(j);
                        resId = (String) member.get("resId");
                        if("20000007".equals(compId)){
                            saveRetJsonNew(member,MENBERS,srvOrderId,rfsId,compId,resources,"member",resId);
                        }else {
                            saveRetJson(member,MENBERS,srvOrderId,rfsId,compId,resources,"member",resId);
                        }
                        if(member.keySet().contains("rfsMemberResAttr")){
                            JSONObject rfsMemberResAttr = member.getJSONObject("rfsMemberResAttr");
                            if(rfsMemberResAttr!=null &&rfsMemberResAttr.keySet().contains("attrs")){
                                JSONArray rfsAttrDTOS = rfsMemberResAttr.getJSONArray("attrs");
                                if(rfsAttrDTOS!= null && rfsAttrDTOS.size()>0){
                                    for(Object rfsAttrDTO:rfsAttrDTOS){
                                        saveRetJson((JSONObject)rfsAttrDTO,ATTRLIST,srvOrderId,rfsId,compId,resources,"attr",resId);
                                    }
                                }
                            }
                        }
                        if(member.keySet().contains("resRouteList")){
                            JSONArray resRouteList = member.getJSONArray("resRouteList");
                            if(resRouteList!= null && resRouteList.size()>0){
                                for (Object resRoute :resRouteList ){
                                    JSONObject temp = (JSONObject) resRoute;
                                    Map<String,Object> map = new HashMap<String,Object>();
                                    map.put("srvOrdId",srvOrderId);
                                    map.put("rfsId",rfsId);
                                    map.put("compId",compId);
                                    map.put("resources",resources);
                                    if(resId!=null){
                                        map.put("resId",resId);
                                    }
                                    map.put("attrCode",temp.getString("resTypeId"));
                                    map.put("attrValue",temp.getString("resName"));
                                    map.put("attrName",temp.getString("resType"));
                                    map.put("speciality",temp.getString("speciality"));
                                    map.put("oprState",temp.getString("oprState"));
                                    webServiceDao.saveResRoute(map);
                                    //网元：1053：IP:2101
                                    if(temp.containsKey("ipAddress")&&(!"".equals(MapUtils.getString(temp,"ipAddress","")))){
                                        map.put("attrCode","1053-2101");
                                        map.put("attrValue",temp.getString("ipAddress"));
                                        map.put("attrName","IP地址");
                                        webServiceDao.saveResRoute(map);
                                    }
                                }
                            }
                        }
                        logger.info("----members");
                    }
                }
                if(!"20000007".equals(compId)){
                    // 获得每一个rds对象中的attrs数组
                    JSONArray attrs = rfs.getJSONArray("attrs");
                    if(attrs!=null && attrs.size()>0){
                        for (int j = 0; j < attrs.size(); j++) {
                            //获取attrListArray数组中每一个对象
                            JSONObject attr = attrs.getJSONObject(j);
                            saveRetJson(attr,ATTRLIST,srvOrderId,rfsId,compId,resources,"attr",resId);
                            logger.info("----attrs");
                        }
                    }
                }
            }
        }

        return srvOrderId;
    }
    public void updateCircuitCode(JSONObject requestBody,String srvOrderId){
        Map<String,String> circuitCodeMap = new HashMap<String,String>();
        if(requestBody.keySet().contains("newResource")){
            JSONObject newResource = requestBody.getJSONObject("newResource");
            JSONArray rfsList = newResource.getJSONArray("rfsList");
            for (int i = 0; i < rfsList.size(); i++) {
                String compId = rfsList.getJSONObject(i).getString("compId");
                JSONObject  rfs= rfsList.getJSONObject(i);
                JSONArray members = rfs.getJSONArray("members");
                if(members!=null && members.size()>0){
                    for (int j = 0; j < members.size(); j++) {
                        //获取attrListArray数组中每一个对象
                        JSONObject member = members.getJSONObject(j);
                    //    String circuitCode = member.getString("resName");
                        if("20000007".equals(compId)){// OPTICAL 光路
                            circuitCodeMap.put("optical",member.getString("resName"));
                        } else if("20000001".equals(compId)){ // 传输
                            circuitCodeMap.put("trans",member.getString("resName"));
                        } else if("20000009".equals(compId)){ // 互联网专线
                            circuitCodeMap.put("dia",member.getString("resName"));
                        }
                    }
                }
            }
        }
        String circuitCode ="";
        // 如果配置了传输电路，就是传输电路的电路名称；如果没配置传输只配了光纤，那么就是光纤的光路名称
        if(circuitCodeMap.keySet().contains("trans")){
            circuitCode = MapUtils.getString(circuitCodeMap,"trans");
        } else if(circuitCodeMap.keySet().contains("optical")){
            circuitCode = MapUtils.getString(circuitCodeMap,"optical");
        }
        // 互联网专线比较特殊，不配置传输光纤、汇总后会生成一条互联网专线电路，再次调用查询接口，需要特殊处理
        if("".equals(circuitCode)&&circuitCodeMap.keySet().contains("dia")){
            circuitCode = MapUtils.getString(circuitCodeMap,"dia");
        }
        // 若电路编号为空，则不更新
        if(!StringUtils.isEmpty(circuitCode)){
            // 查询是否存在电路编号记录
            String attrInfoId = webServiceDao.qryResCircuitCode(srvOrderId);
            if (attrInfoId != null && !("null".equals(attrInfoId))){
                webServiceDao.updateResCircuitCode(attrInfoId,circuitCode);
            } else{
                // 新增电路编号属性记录
                Map<String, Object> rmap = new HashMap<String, Object>();
                rmap.put("srv_ord_id", srvOrderId);
                rmap.put("attr_action", "0");
                rmap.put("attr_code", "20000064");
                rmap.put("attr_name", "电路编号");
                rmap.put("attr_value", circuitCode);
                rmap.put("attr_value_name", "circuitCode");
                rmap.put("create_date", df.format(new Date()));
                rmap.put("sourse", "res");
                webServiceDao.saveRetInfo(rmap);
            }
        }
    }

    public void saveRetJson(JSONObject job , String str, String srvOrderId, String rfsId , String compId , String resources,String type,String resId) {
        String[] member = str.split(",");
        Map<String,Object> map = new HashMap<String,Object>();
        for(int i = 0; i < member.length; i++){
            if(job.getString(member[i]) == null || "null".equals(job.getString(member[i])) || "".equals(job.getString(member[i]))){
                map.put(member[i],"");
            } else {
                if (ATTR_VALUE.equals(member[i])){
                    map.put(member[i],job.getString(member[i]).replace("<br>"," "));
                }else {
                    map.put(member[i],job.getString(member[i]));
                }

            }
        }
        map.put("srvOrdId",srvOrderId);
        map.put("rfsId",rfsId);
        map.put("compId",compId);
        map.put("resources",resources);
        if(resId!=null){
            map.put("resId",resId);
        }
        boolean resSupplement = false;//资源补录标志
        int num = webServiceDao.isResSupplement(srvOrderId);
        if(num > 0){
            resSupplement = true;
        }
        if(resSupplement){
            // 如果是资源补录
            if(type.equals("member")){
                webServiceDao.saveResSupple(map);
            } else if(type.equals("attr")) {
                webServiceDao.saveResRouteSupple(map);
            }
        } else{
            if(type.equals("member")){
                webServiceDao.saveRes(map);
            } else if(type.equals("attr")) {
                webServiceDao.saveResRoute(map);
            }
        }
    }

    public void saveRetJsonNew(JSONObject job , String str, String srvOrderId, String rfsId , String compId , String resources,String type,String resId) {
        String[] member = str.split(",");
        Map<String,Object> map = new HashMap<String,Object>();
        for(int i = 0; i < member.length; i++){
            if(job.getString(member[i]) == null || "null".equals(job.getString(member[i])) || "".equals(job.getString(member[i]))){
                map.put(member[i],"");
            } else {
                if(RFS_MEMBER_RES_ATTR.equals(member[i])){
                    // 获得每一个对象中的attrs数组
                    JSONObject rfsMemberResAttr = job.getJSONObject(member[i]);
                    JSONArray attrs = rfsMemberResAttr.getJSONArray("attrs");
                    if(attrs!=null && attrs.size()>0){
                        for (int j = 0; j < attrs.size(); j++) {
                            //获取attrListArray数组中每一个对象
                            JSONObject attr = attrs.getJSONObject(j);
                            saveRetJson(attr,ATTRLIST,srvOrderId,rfsId,compId,resources,"attr",resId);
                            logger.info("----attrs");
                        }
                    }
                }else {
                    map.put(member[i],job.getString(member[i]));
                }
            }
        }
        map.put("srvOrdId",srvOrderId);
        map.put("rfsId",rfsId);
        map.put("compId",compId);
        map.put("resources",resources);
        if(resId!=null){
            map.put("resId",resId);
        }

        boolean resSupplement = false;//资源补录标志
        int num = webServiceDao.isResSupplement(srvOrderId);
        if(num > 0){
            resSupplement = true;
        }
        if(resSupplement){
            // 如果是资源补录
            if(type.equals("member")){
                webServiceDao.saveResSupple(map);
            } else if(type.equals("attr")) {
                webServiceDao.saveResRouteSupple(map);
            }
        } else{
            if(type.equals("member")){
                webServiceDao.saveRes(map);
            } else if(type.equals("attr")) {
                webServiceDao.saveResRoute(map);
            }
        }
    }

    public void saveResAttrs(JSONObject job , String str, String srvOrderId, String rfsId , String compId , String resources) {
        String[] attrs = str.split(",");
        Map<String,Object> map = new HashMap<String,Object>();
        for(int i = 0; i < attrs.length; i++){
            if(job.getString(attrs[i]) == null){
                map.put(attrs[i],"");
            } else {
                map.put(attrs[i],job.getString(attrs[i]));
            }
            //      map.put("createDate",df.format(new Date()));
        }
        map.put("srvOrdId",srvOrderId);
        map.put("rfsId",rfsId);
        map.put("compId",compId);
        map.put("resources",resources);

        webServiceDao.saveRes(map);
    }
    public String getResourceWithPosition(JSONObject requestBody, String resources,String srvOrderId,String position) {
        logger.info("----srvOrderId--"+ srvOrderId);
        String resId = null;
        if(requestBody.keySet().contains(resources)){
            JSONObject newResource = requestBody.getJSONObject(resources);
            JSONArray rfsList = newResource.getJSONArray("rfsList");
            for (int i = 0; i < rfsList.size(); i++) {
                //遍历JSONArray中的每一个对象
                JSONObject  rfs= rfsList.getJSONObject(i);
                String rfsId = rfs.getString("rfsId");
                String compId = rfs.getString("compId");
                //获得每一个对象中的members数组
                JSONArray members = rfs.getJSONArray("members");
                if(members!=null && members.size()>0){
                    for (int j = 0; j < members.size(); j++) {
                        //获取attrListArray数组中每一个对象
                        JSONObject member = members.getJSONObject(j);
                        resId = (String) member.get("resId");
                        if(member.keySet().contains("resRouteList")){
                            JSONArray resRouteList = member.getJSONArray("resRouteList");
                            if(resRouteList!= null && resRouteList.size()>0){
                                for (Object resRoute :resRouteList ){
                                    JSONObject temp = (JSONObject) resRoute;
                                    Map<String,Object> map = new HashMap<String,Object>();
                                    map.put("srvOrdId",srvOrderId);
                                    map.put("rfsId",rfsId);
                                    map.put("compId",compId);
                                    map.put("resources",resources);
                                    if(resId!=null){
                                        map.put("resId",resId);
                                    }
                                    map.put("attrCode",temp.getString("resTypeId"));
                                    map.put("attrValue",temp.getString("resName"));
                                    map.put("attrName",temp.getString("resType"));
                                    map.put("positon",position);
                                    map.put("speciality",temp.getString("speciality"));
                                    map.put("oprState",temp.getString("oprState"));
                                    webServiceDao.saveResRoutePosition(map);
                                    //网元：1053：IP:2101
                                    if(temp.keySet().contains("ipAddress")){
                                        map.put("attrCode","1053-2101");
                                        map.put("attrValue",temp.getString("ipAddress"));
                                        map.put("attrName","IP地址");
                                        webServiceDao.saveResRoutePosition(map);
                                    }
                                }
                            }
                        }
                    }
                   /*     if(member.keySet().contains("rfsMemberResAttr")){
                            JSONObject rfsMemberResAttr = member.getJSONObject("rfsMemberResAttr");
                            if(rfsMemberResAttr!=null &&rfsMemberResAttr.keySet().contains("attrs")){
                                JSONArray rfsAttrDTOS = rfsMemberResAttr.getJSONArray("attrs");
                                if(rfsAttrDTOS!= null && rfsAttrDTOS.size()>0){
                                    for(Object rfsAttrDTO:rfsAttrDTOS){
                                        saveRetJson((JSONObject)rfsAttrDTO,ATTRLIST,srvOrderId,rfsId,compId,resources,"attr",resId);
                                    }
                                }
                            }
                        }*/
                    logger.info("----members");
                }
            }
        }
        return srvOrderId;
    }

}
