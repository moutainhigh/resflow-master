package com.zres.project.localnet.portal.webservice.res;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderQrySecondaryDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.logInfo.entry.ResInterfaceLog;
import com.zres.project.localnet.portal.logInfo.service.ResInterfaceLogger;
import com.zres.project.localnet.portal.logInfo.until.LoggerThreadPool;
import com.zres.project.localnet.portal.util.HttpClientJson;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.core.util.MapUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 资源接口-客户端
 * 资源配置页面接口实现
 * Created by Skyla on 2018/12/25.
 */
@Service
public class ResAssignmentService implements ResAssignmentServiceIntf {
    private static Logger logger = LoggerFactory.getLogger(ResAssignmentService.class);

    @Autowired
    private WebServiceDao rsd; //数据库操作-对象

    @Autowired
    private ResInterfaceLogger resInterfaceLogger;

    @Override
    public Map resAssignment(Map<String, Object> params) {
        // 查看需要参数：srvOrdId,userName,isConfig,woId,compIds
        // 子流程配置需要参数：srvOrdId,userName,isConfig,tacheId,orderId:gom_order.order_id
        // 核查预占需要参数:srvOrdId,userName,isConfig,tacheId,woId
        //srvOrdId,userName,isConfig,tacheId,orderId:gom_order.order_id,woId,compIds
        logger.info("资源配置页面接口开始！" + params);
        String json = "";
        String retStr = "";
        String url = rsd.queryUrl("ResAssignment");
        Map retmap = new HashMap();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("interfname", "资源配置页面接口");
        map.put("url", url);
        map.put("createdate", df.format(new Date()));
        map.put("returncontent", "");
        map.put("orderno", MapUtils.getString(params,"srvOrdId")); //业务订单id
        map.put("remark", "");
        map.put("updatedate", "");
        try {
            //生成json报文
            json = createJson(params);
            String retStr1 = json.replace("\\n","");
            String retStr2 = retStr1.replace("\\t","");
            String retStr3 = retStr2.replace("\\r","");
            retStr = retStr3.replace(" ","");
            map.put("content", retStr);
            logger.info("发送报文：" + json);
        }
        catch (Exception e) {
            logger.error("拼接报文异常！异常信息：" + e.getMessage(),e);
            retmap.put("returncode", "失败");
            retmap.put("returndec", "拼接报文异常: " + e.getMessage());
            map.put("content", MapUtils.getString(retmap,"returndec"));
            map.put("remark", "拼接报文异常");
            map.put("updatedate", df.format(new Date()));
            //5.报文入库，数据入库
            this.saveEventJson(map);
//            rsd.saveJson(map);
            return retmap;
        }
            map.put("url", url);
            this.saveEventJson(map);
//            rsd.saveJson(map);
            logger.info("资源接口地址：" + url);
            retmap.put("returncode", "成功");
            retmap.put("url", url);
            retmap.put("json", retStr);
            logger.info("资源配置页面接口结束---" + params);
        return retmap;
    }

    public  String createJson(Map params) {
        //1.查数据库数据  (用来拼报文)
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        String flag = MapUtils.getString(params, "flag");
        Map<String,Object> map = new HashMap<String,Object>();
        if(BasicCode.RES_SUPPLEMENT.equals(flag)){
            map = rsd.queryResInfoBySupplement(srvOrdId);
        } else{
            map = rsd.queryResInfo(MapUtils.getString(params,"srvOrdId"));
        }

        logger.info("报文数据resmap：" + map);

        List<Map<String, Object>> cirlist = null;
        // 资源补录标志
        boolean resSupplement = BasicCode.ACTIVE_TYPE_SUPPLEMENT.equals(MapUtils.getString(map,"ACTIVE_TYPE"));
        // 资源补录不需要查询电路属性表attr_info
        if(resSupplement){
            Set<String> set = new HashSet<>();
            set.add(BasicCode.LOCAL);
            set.add(BasicCode.SECOND);
            if(set.contains(MapUtils.getString(map,"RESOURCES",""))){
                // 如果电路在本系统调度过，根据instance_id查询上次调度的内容
                List<Map<String,Object>> srvList = rsd.qrySrvOrdIdLast(MapUtils.getString(map,"PRODINSTID"));
                if(srvList!=null &&srvList.size()>0){
                    cirlist = rsd.queryCirInfo(MapUtils.getString(srvList.get(0),"SRV_ORD_ID"));
                    map.put("RESOURCES", MapUtils.getString(srvList.get(0), "RESOURCES"));
                }
            }
        } else {
            cirlist = rsd.queryCirInfo(MapUtils.getString(map, "SRV_ORD_ID"));
        }
        //2.拼报文
        JSONObject json = new JSONObject();
        JSONObject request = new JSONObject();
        JSONObject businessInfo = new JSONObject();
        JSONArray rows = new JSONArray();
        JSONObject requestBody = new JSONObject();
        JSONObject resourceAttrs = new JSONObject();

        businessInfo.put("crmOrderCode", MapUtils.getString(map,"CRMORDERCODE"));
        businessInfo.put("prodInstId", MapUtils.getString(map,"PRODINSTID"));
        businessInfo.put("operator", MapUtils.getString(params,"userName")); //　操作人账号
        businessInfo.put("resflowWorkId",MapUtils.getString(map,"SRV_ORD_ID"));
        businessInfo.put("subordinateVersion","2");// 1：本地网;2:二干
        // 特殊：如果是核查单，那么动作类型类型改为核查
        if("102".equals(MapUtils.getString(map,"ORDER_TYPE"))){
            map.put("ACTIVE_TYPE","107");
        }
        businessInfo.put("actionType", MapUtils.getString(map, "ACTIVE_TYPE"));

        // 是否可配置	1:允许配置，0：只查看
        if("0".equals(MapUtils.getString(params,"isConfig"))){
            businessInfo.put("isConfig",MapUtils.getString(params,"isConfig"));
            businessInfo.put("rfsId", params.get("compIds")); //组件ID
            businessInfo.put("woId", MapUtils.getString(params,"woId"));
        } else {
            businessInfo.put("isConfig","1");
            String tacheId = MapUtils.getString(params,"tacheId");
            //qryRegionCode
            // 查询电路受理区域所在省份
            String regionCode = "";
            if(resSupplement){
                regionCode = MapUtils.getString(map, "REGION_ID","");
            } else{
                // 查询电路受理区域所在省份
                String regionCodeName = MapUtils.getString(map,"HANDLE_DEP_ID","");
                regionCode = rsd.qryRegionCode(regionCodeName);
            }

            Set<String> tacheSet = new HashSet<>();
            tacheSet.add(BasicCode.SECOND_CUST_FLOW);
            tacheSet.add(BasicCode.SECOND_CUST_CHILDFLOW);
            tacheSet.add(BasicCode.SECOND_CROSS_FLOW);
            tacheSet.add(BasicCode.SECOND_CROSS_CHILDFLOW);
            if(tacheSet.contains(tacheId) || resSupplement){
                businessInfo.put("rfsId", getRfsIdListByOrderId(tacheId, MapUtils.getString(params,"orderId"),regionCode)); //组件ID
                businessInfo.put("woId", MapUtils.getString(params,"orderId")); // 流程订单id gom_order_order_id
            } else {
                params.put("regionCode",regionCode);
                businessInfo.put("rfsId", getRfsIdListBytaceId(params)); //组件ID
                businessInfo.put("woId", MapUtils.getString(params,"woId"));
            }
        }
        //添加单据来源  modify by wang.g2
        Map<String, Object> codeInfo = new HashMap<>();
        codeInfo.put("codeType", "RESOURCE_FROM");
        codeInfo.put("codeValue", MapUtils.getString(map, "RESOURCES"));
        List<Map<String, Object>> mapList = rsd.queryCodeInfo2(codeInfo);
        JSONObject resource = new JSONObject();
        resource.put("attrAction", 0);
        resource.put("attrCode", "RESOURCE_FROM");
        resource.put("attrDesc", "单据来源");
        if(mapList.size() > 0 ){
            resource.put("attrValue",MapUtils.getString(mapList.get(0), "CODE_CONTENT","20038"));
        }
        if(cirlist!=null && cirlist.size()>0){
            for (Map<String, Object> cir : cirlist) {
                JSONObject row = new JSONObject();
                row.put("attrAction", cir.get("ATTR_ACTION"));
                row.put("attrCode", cir.get("ATTR_CODE"));
                row.put("attrValue", cir.get("ATTR_VALUE"));
                row.put("attrDesc", cir.get("ATTR_NAME"));
                rows.add(row);
            }
            rows.add(resource);
        }

        /**
         * 添加电路编码是否可以编写节点 CIRCUIT_NO_EDIT_FLAG
         * 一干来单 CIRCUIT_NO_EDIT_FLAG = 0  不可编辑
         * 二干自启，集客来单 CIRCUIT_NO_EDIT_FLAG = 1 可以编辑
         */
        /*Map<String, Object> belongSysMap = orderQrySecondaryDao.qrySrvOrderBelongSys(params);
        String RESOURCES = MapUtils.getString(belongSysMap, "RESOURCES");
        String circuitNoEditFlag = "0";
        if (BasicCode.ONEDRY.equals(RESOURCES)){
            circuitNoEditFlag = "0";
        }else if (BasicCode.SECONDARY.equals(RESOURCES) || BasicCode.JIKE.equals(RESOURCES)){
            circuitNoEditFlag = "1";
        }
        JSONObject rowFlag = new JSONObject();
        rowFlag.put("attrAction", "0");
        rowFlag.put("attrCode", "CIRCUIT_NO_EDIT_FLAG");
        rowFlag.put("attrValue", circuitNoEditFlag);
        rowFlag.put("attrDesc", "电路编码是否可以编写");
        rows.add(rowFlag);*/

        resourceAttrs.element("row", rows);
        requestBody.put("businessInfo", businessInfo);
        requestBody.put("resourceAttrs", resourceAttrs);

        request.put("requestHeader", HttpClientJson.requestHeader(map, "ResAssignment"));
        request.put("requestBody", requestBody);
        json.put("request", request);
        //System.out.println(json.toString());
        return json.toString();
    }
    public List<String> getRfsIdListBytaceId(Map params){
        List<String> rfsIdList = new ArrayList<>();
        if (params.keySet().contains("tacheId")){
            String tacheId = MapUtils.getString(params,"tacheId");

            //传输
            if (BasicCode.SECOND_CHECK_TRAN.equals(tacheId)){
                rfsIdList.add("20000001");
            }
            //数据
            if (BasicCode.SECOND_CHECK_DATA.equals(tacheId)){
                rfsIdList.add("20000002");
                rfsIdList.add("20000003");
                rfsIdList.add("20000004");
                rfsIdList.add("20000005");
                rfsIdList.add("20000006");
            }
            //交换
            if (BasicCode.SECOND_CHECK_EXCHANGE.equals(tacheId)){
                rfsIdList.add("20000008");
            }
            //核查调度环节
            if (BasicCode.SECOND_CHECK_FLOW.equals(tacheId)){
                rfsIdList.add("20000001");
                rfsIdList.add("20000002");
                rfsIdList.add("20000003");
                rfsIdList.add("20000004");
                rfsIdList.add("20000005");
                rfsIdList.add("20000006");
                rfsIdList.add("20000008");
                rfsIdList.add("20000013");
            }
            //其他专业
            if (BasicCode.SECOND_CHECK_OTHER.equals(tacheId)){
                rfsIdList.add("20000013");
            }

        }
        return rfsIdList;
    }

    /**
     * 20000001	传输资源配置
     20000007	光路配置
     20000002	数据资源配置、 20000003 数据-端口配置、 20000004	数据-IP配置、   20000005	数据-VLAN配置 、20000006	数据-VRF配置
     20000008	交换-端口配置
     20000010	伪线
     20000011	L3VPN
     20000012	二干-OBD端口分配
     20000013	二干-全专业端口分配
     设备综合专业[包含除光纤专业外所有设备专业]: 除了20000007
     传输专业：20000001，20000010 ，20000011【二干没有传输-ipran,把这个合并到传输专业一起配置】
     传输-ipran 20000010 20000011
     光纤/外线：20000007； 交换：20000008
     数据：20000002，20000003，20000004，20000005，20000006
     接入专业:  20000012( 二干-OBD端口分配}
     其他专业:20000013 (二干-全专业端口分配)

     * @param orderId
     * @return
     */
    public List<String> getRfsIdListByOrderId(String tacheId, String orderId,String regionCode){
        List<String> rfsIdList = new ArrayList<>();
//        Map<String,Object> map = rsd.qryGomOrderAttr(orderId,BasicCode.SPECIALTY_CODE);
        Map<String,Object> map = rsd.qrypSecialtyCode(orderId);

        // 工单的专业
        String attrVal = MapUtils.getString(map,"SPECIALTY_CODE");
        //二干调度环节、其他专业    全专业展示
        if(tacheId.equals(BasicCode.SECOND_CUST_FLOW) || tacheId.equals(BasicCode.SECOND_CROSS_FLOW) ){
            rfsIdList.add("20000001");
            rfsIdList.add("20000002");
            rfsIdList.add("20000003");
            rfsIdList.add("20000004");
            rfsIdList.add("20000005");
            rfsIdList.add("20000006");
            rfsIdList.add("20000007");
            rfsIdList.add("20000008");
            rfsIdList.add("20000010");
            rfsIdList.add("20000011");
        //    rfsIdList.add("20000012");
            rfsIdList.add("20000013");
        }

        if(BasicCode.OPTICAL_2.equals(attrVal)){ // 外线/光纤专业
            rfsIdList.add("20000007");
        }
        // BasicCode.TRANS_3：传输 ；BasicCode.COMPLEX_1：设备综合
        if(BasicCode.TRANS_3.equals(attrVal) || BasicCode.TRANS_MSAP_14.equals(attrVal)){
            rfsIdList.add("20000001");
            // 二干没有传输-ipran,把这个合并到传输专业一起配置
            rfsIdList.add("20000010");
            rfsIdList.add("20000011");
        }
        // BasicCode.DATA_4：数据
        if( BasicCode.DATA_4.equals(attrVal)){
            rfsIdList.add("20000002");
            rfsIdList.add("20000003");
            rfsIdList.add("20000004");
            rfsIdList.add("20000005");
            rfsIdList.add("20000006");
        }
        // BasicCode.EXCHANGE_5：交换
        if( BasicCode.EXCHANGE_5.equals(attrVal) ){
            rfsIdList.add("20000008");
        }
        //ip地址  跟数据制作暂时一样 后期确定
        if( BasicCode.IP_15.equals(attrVal)){
            rfsIdList.add("20000002");
            rfsIdList.add("20000003");
            rfsIdList.add("20000004");
            rfsIdList.add("20000005");
            rfsIdList.add("20000006");
        }
        // 传输-ipran
        if(BasicCode.TRANS_IPRAN_13.equals(attrVal)){
            rfsIdList.add("20000010");
            rfsIdList.add("20000011");
        }
        // 二干代码   接入专业选择   20000012 二干-OBD端口分配
        // 二干没有接入专业
       /* if(BasicCode.ACCESS_6.equals(attrVal)){
            rfsIdList.add("20000012");
        }*/
        // 二干代码   其他专业，20000013 二干-全专业端口分配
        if(BasicCode.OTHER_11.equals(attrVal)){
            rfsIdList.add("20000013");
        }

        return rfsIdList;
    }

    public void saveEventJson(Map<String, Object> map){
        ResInterfaceLog resInterfaceLog = new ResInterfaceLog();
        resInterfaceLog.setInterfName(MapUtil.getString(map ,"interfname"));
        //resInterfaceLog.setCreateDate(new Date());
        resInterfaceLog.setOrderNo(MapUtil.getString(map ,"orderno"));
        resInterfaceLog.setRemark(MapUtil.getString(map ,"remark"));
        resInterfaceLog.setReturnContent(MapUtil.getString(map ,"returncontent"));
        //resInterfaceLog.setUpdateDate(new Date());
        resInterfaceLog.setUrl(MapUtil.getString(map ,"url"));
        resInterfaceLog.setContent(MapUtil.getString(map ,"content"));
        LoggerThreadPool.addLoggerToExecute(resInterfaceLogger,resInterfaceLog);
        //rsd.saveJson(map);
    }


}
