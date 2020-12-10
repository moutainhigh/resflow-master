package com.zres.project.localnet.portal.webservice.res;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    /*@Autowired
    private OrderQrySecondaryDao orderQrySecondaryDao;*/
    @Autowired
    private ResInterfaceLogger resInterfaceLogger;
    @Autowired
    private OrderDealDao orderDealDao;

    public Map resAssignment(Map<String, Object> params) {
        // 查看需要参数：srvOrdId,userName,isConfig,woId,compIds
        // 子流程配置需要参数：srvOrdId,userName,isConfig,tacheId,orderId:gom_order.order_id
        // 核查预占需要参数:srvOrdId,userName,isConfig,tacheId,woId
        //srvOrdId,userName,isConfig,tacheId,orderId:gom_order.order_id,woId,compIds
        logger.info("资源配置页面接口开始！" + params);
        String json = "";
        String retStr = "";
        String url =  rsd.queryUrl("ResAssignment");
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
        JSONObject resFullCom = new JSONObject();
        //1.查数据库数据  (用来拼报文)
        String srvOrdId = MapUtils.getString(params, "srvOrdId");
        String flag = MapUtils.getString(params, "flag");
        Map<String, Object> codeInfo = new HashMap<>();
        codeInfo.put("codeType", "RESOURCE_FROM");
        codeInfo.put("codeValue", MapUtils.getString(params,"resFullCom"));
        List<Map<String, Object>> codeInfoList = orderDealDao.queryRouteInfoUrl(codeInfo);
        //每条电路新增单据来源字段
        resFullCom.put("attrAction", 0);
        resFullCom.put("attrCode", "RESOURCE_FROM");
        resFullCom.put("attrDesc", "单据来源");
        if(codeInfoList.size() > 0){
            //每条电路新增单据来源字段
            resFullCom.put("attrValue", MapUtils.getString(codeInfoList.get(0), "CODE_CONTENT","20039"));
        }else{
            resFullCom.put("attrValue", "");
        }
        Map<String,Object> map = new HashMap<String,Object>();
        /**
         * 外层业务根据所属系统分为本地和二干
         * 这里本地要更细的区分；本地包含一干来单，集客来单，本地自启；
         * 1，如果是本地的单子查询单子来源    ----目前先不用了
         */
        //String RESOURCES = BasicCode.LOCALBUILD;
        if (BasicCode.LOCALBUILD.equals(flag)){
            map = rsd.queryResInfo(srvOrdId);
        }else if (BasicCode.SECONDARY.equals(flag)) {
            map = rsd.queryResInfoFromSec(srvOrdId);
        } else if(BasicCode.RES_SUPPLEMENT.equals(flag)){
            map = rsd.queryResInfoBySupplement(srvOrdId);
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
            List<Map<String,Object>> srvList = new ArrayList<>();
            String resource = MapUtils.getString(map,"RESOURCES","");
            if(set.contains(resource)){
                // 如果电路在本系统调度过，根据instance_id查询上次调度的内容
                srvList = rsd.qrySrvOrdIdLast(MapUtils.getString(map,"PRODINSTID"));
            } else if(BasicCode.SEC_LOCAL.equals(resource)){
                srvList = rsd.qrySrvOrdIdSecLast(MapUtils.getString(map,"PRODINSTID"));
            }
            if(srvList!=null &&srvList.size()>0){
                cirlist = rsd.queryCirInfo(MapUtils.getString(srvList.get(0),"SRV_ORD_ID"));
            }
        } else {
            cirlist = rsd.queryCirInfo(MapUtils.getString(params, "srvOrdIdOld"));
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
        businessInfo.put("subordinateVersion","1");// 1：本地网;2:二干
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
            String regionCode = "";
            if(resSupplement){
                regionCode = MapUtils.getString(map, "REGION_ID","");
            } else{
                // 查询电路受理区域所在省份
                String regionCodeName = MapUtils.getString(map,"HANDLE_DEP_ID","");
                regionCode = rsd.qryRegionCode(regionCodeName);
            }

            Set<String> tacheSet = new HashSet<>();
            tacheSet.add(BasicCode.FIBER_RES_ALLOCATE);
            tacheSet.add(BasicCode.RES_ALLOCATE);
            tacheSet.add(BasicCode.SPECIALTY_RESOURCE_SUPPLEMENT_LOCAL);
            if(tacheSet.contains(tacheId)){
                businessInfo.put("rfsId", getRfsIdListByOrderId(MapUtils.getString(params,"orderId"),regionCode)); //组件ID
                businessInfo.put("woId", MapUtils.getString(params,"orderId")); // 流程订单id gom_order_order_id
            } else {
                params.put("regionCode",regionCode);
                businessInfo.put("rfsId", getRfsIdListBytaceId(params)); //组件ID
                businessInfo.put("woId", MapUtils.getString(params,"woId"));
            }
        }
        // 业务所属端 A或Z (目前只有核查流程下发工建页面查询传输及光路时使用)
        businessInfo.put("position", MapUtils.getString(params,"position",""));
        if(cirlist != null && cirlist.size() > 0 ){
            for (Map<String, Object> cir : cirlist) {
                JSONObject row = new JSONObject();
                if (cir.get("ATTR_CODE").equals("Z_ADDRESS") ||
                        cir.get("ATTR_CODE").equals("ADDRESS") ||
                        cir.get("ATTR_CODE").equals("A_ADDRESS") ||
                        cir.get("ATTR_CODE").equals("Z_OLD_ADDRESS") ||
                        cir.get("ATTR_CODE").equals("A_OLD_ADDRESS")
                ) {
                    row.put("attrAction", cir.get("ATTR_ACTION"));
                    row.put("attrCode", cir.get("ATTR_CODE"));
                    row.put("attrValue", removeStr(cir.get("ATTR_VALUE").toString()));
                    row.put("attrDesc", cir.get("ATTR_NAME"));
                }
                else {
                    row.put("attrAction", cir.get("ATTR_ACTION"));
                    row.put("attrCode", cir.get("ATTR_CODE"));
                    row.put("attrValue", cir.get("ATTR_VALUE"));
                    row.put("attrDesc", cir.get("ATTR_NAME"));
                }
                rows.add(row);
            }
            rows.add(resFullCom);
        }
        /**
         * 添加电路编码是否可以编写节点 CIRCUIT_NO_EDIT_FLAG
         * 一干，二干来单 CIRCUIT_NO_EDIT_FLAG = 0  不可编辑
         * 本地自启，集客来单 CIRCUIT_NO_EDIT_FLAG = 1 可以编辑
         */
        /*String circuitNoEditFlag = "0";
        if (BasicCode.ONEDRY.equals(RESOURCES) || BasicCode.SECONDARY.equals(RESOURCES)){
            circuitNoEditFlag = "0";
        }else if (BasicCode.LOCALBUILD.equals(RESOURCES) || BasicCode.JIKE.equals(RESOURCES)){
            circuitNoEditFlag = "1";
        }*/
        JSONObject rowFlag = new JSONObject();
        rowFlag.put("attrAction", "0");
        rowFlag.put("attrCode", "CIRCUIT_NO_EDIT_FLAG");
        rowFlag.put("attrValue", "1");
        rowFlag.put("attrDesc", "电路编码是否可以编写");
        rows.add(rowFlag);
        if(params.containsKey("hasDevice") ){
            // 打开设备配置页面
            JSONObject openFlag = new JSONObject();
            openFlag.put("attrAction", "0");
            openFlag.put("attrCode", "DEVICEISORNO");
            openFlag.put("attrValue", MapUtils.getString(params,"hasDevice","1"));
            openFlag.put("attrDesc", "打开设备配置页面");
            rows.add(openFlag);
        } else if(params.containsKey("hasOptical") ){
            // 打开光路配置页面
            JSONObject openFlag = new JSONObject();
            openFlag.put("attrAction", "0");
            openFlag.put("attrCode", "OPTISORNO");
            openFlag.put("attrValue", MapUtils.getString(params,"hasOptical","1"));
            openFlag.put("attrDesc", "打开光路配置页面");
            rows.add(openFlag);
        }

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
            if(BasicCode.OUTSIDElINE_CHECK.equals(tacheId) || BasicCode.CHECK_DISPATCH.equals(tacheId)||tacheId.equals(BasicCode.CIRCUIT_DISPATCH)){
                rfsIdList.add("20000007");
            }
            if (BasicCode.TRANS_CHECK.equals(tacheId) || BasicCode.CHECK_DISPATCH.equals(tacheId)||tacheId.equals(BasicCode.CIRCUIT_DISPATCH)){
                rfsIdList.add("20000001");
               /* if("83".equals(MapUtils.getString(params,"regionCode"))){// 83代表重庆
                    rfsIdList.add("20000010");
                }*/
            }
            if (BasicCode.DATA_CHECK.equals(tacheId) || BasicCode.CHECK_DISPATCH.equals(tacheId)||tacheId.equals(BasicCode.CIRCUIT_DISPATCH)){
                rfsIdList.add("20000002");
                rfsIdList.add("20000003");
                rfsIdList.add("20000004");
                rfsIdList.add("20000005");
                rfsIdList.add("20000006");
            }
            if (BasicCode.CHANGE_CHECK.equals(tacheId) || BasicCode.CHECK_DISPATCH.equals(tacheId)||tacheId.equals(BasicCode.CIRCUIT_DISPATCH)){
                rfsIdList.add("20000008");
            }
            // 传输-ipran
            if(tacheId.equals(BasicCode.CIRCUIT_DISPATCH)){
                rfsIdList.add("20000010");
                rfsIdList.add("20000011");
            }
            // 二干代码   接入专业选择   20000012 二干-OBD端口分配
            if(tacheId.equals(BasicCode.CIRCUIT_DISPATCH)|| BasicCode.CHECK_DISPATCH.equals(tacheId)|| BasicCode.ACCESS_CHECK.equals(tacheId)){
                rfsIdList.add("20000012");
            }
            // 二干代码   其他专业，20000013 二干-全专业端口分配
            if(tacheId.equals(BasicCode.CIRCUIT_DISPATCH)|| BasicCode.CHECK_DISPATCH.equals(tacheId)|| BasicCode.OTHER_CHECK.equals(tacheId)){
                rfsIdList.add("20000013");
            }
        }
        if(params.containsKey("hasDevice")){
            rfsIdList.add("20000001");
        }
        if(params.containsKey("hasOptical")){
            rfsIdList.add("20000007");
        }
        return rfsIdList;
    }

    /**
     * 20000001	传输资源配置
     20000007	光路配置
     20000002	数据资源配置、 20000003 数据-端口配置、 20000004	数据-IP配置、   20000005	数据-VLAN配置 、20000006	数据-VRF配置
     20000008	交换-端口配置
     设备综合专业[包含除光纤专业外所有设备专业]: 除了20000007
     传输专业：20000001，【如果区域是重庆，那么需要增加伪线配置20000010】
     光纤/外线：20000007； 交换：20000008
     数据：20000002，20000003，20000004，20000005，20000006
     接入专业:  20000012( 二干-OBD端口分配}
     其他专业:20000013 (二干-全专业端口分配)
     * @param orderId
     * @return
     */
    public List<String> getRfsIdListByOrderId(String orderId,String regionCode){
        List<String> rfsIdList = new ArrayList<>();
        Map<String,Object> map = rsd.qryGomOrderAttr(orderId,BasicCode.SPECIALTY_CODE);
        // 工单的专业
        String attrVal = MapUtils.getString(map,"ATTR_VAL");
        if(attrVal.equals(BasicCode.OPTICAL_2)){ // 外线/光纤专业
            rfsIdList.add("20000007");
        }
        // BasicCode.TRANS_3：传输 ；BasicCode.COMPLEX_1：设备综合
        if(attrVal.equals(BasicCode.TRANS_3) || attrVal.equals(BasicCode.COMPLEX_1) || attrVal.equals(BasicCode.TRANS_MSAP_14)){
           /* if("83".equals(regionCode)){// 83代表重庆
                rfsIdList.add("20000010");
            }*/
            rfsIdList.add("20000001");
        }
        // BasicCode.DATA_4：数据
        if(attrVal.equals(BasicCode.DATA_4) || attrVal.equals(BasicCode.COMPLEX_1) || attrVal.equals(BasicCode.P_DATA_4)){
            rfsIdList.add("20000002");
            rfsIdList.add("20000003");
            rfsIdList.add("20000004");
            rfsIdList.add("20000005");
            rfsIdList.add("20000006");
        }
        // BasicCode.EXCHANGE_5：交换
        if(attrVal.equals(BasicCode.EXCHANGE_5) || attrVal.equals(BasicCode.COMPLEX_1) ||  attrVal.equals(BasicCode.P_EXCHANGE_5)){
            rfsIdList.add("20000008");
        }
        // 传输-ipran
        if(attrVal.equals(BasicCode.TRANS_IPRAN_13)){
            rfsIdList.add("20000010");
            rfsIdList.add("20000011");
        }
        // 二干代码   接入专业选择   20000012 二干-OBD端口分配
        if(BasicCode.ACCESS_6.equals(attrVal)){
            rfsIdList.add("20000012");
        }
        // 二干代码   其他专业，20000013 二干-全专业端口分配  'WIRELESS_7' 无线网,'MOBILE_8' 移动核心,'SYN_9' 同步网,'IMS_10' IMS网
        if(BasicCode.OTHER_11.equals(attrVal)||BasicCode.WIRELESS_7.equals(attrVal)
                ||BasicCode.SYN_9.equals(attrVal)||BasicCode.MOBILE_8.equals(attrVal)||BasicCode.IMS_10.equals(attrVal)){
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

    public  String removeStr(String a){
        String regEx="[\"'\n]";
        String aa="";//这里是将特殊字符换为aa字符串,""代表直接去掉
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(a);//这里把想要替换的字符串传进来
        String newString = m.replaceAll(aa).trim();
        return newString;
    }

}
