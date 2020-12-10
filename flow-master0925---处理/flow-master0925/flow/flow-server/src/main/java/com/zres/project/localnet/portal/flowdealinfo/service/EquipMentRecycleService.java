package com.zres.project.localnet.portal.flowdealinfo.service;

import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.EquipMentRecycleDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.service.entry.EquipMentEntry;
import com.zres.project.localnet.portal.local.domain.PageInfo;
import com.zres.project.localnet.portal.until.service.UntilService;
import com.zres.project.localnet.portal.util.HttpClientJson;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;


@Service
public class EquipMentRecycleService implements EquipMentRecycleServiceIntf{

    private static final Logger logger = LoggerFactory.getLogger(EquipMentRecycleService.class);
    @Autowired
    private EquipMentRecycleDao equipMentRecycleDao;
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private UntilService untilService;
    @Autowired
    private OrderDealDao orderDealDao;
    @Override
    public Map<String, Object> addEquip(Map<String, Object> map) {
        EquipMentEntry equipMentEntry = JSON.parseObject(JSON.toJSONString(map),EquipMentEntry.class);
        Map<String, Object> resMap = new HashMap<String, Object>();
        try {
            int i = equipMentRecycleDao.queryEquipCountBySrvOrdIdAndSpecialty(equipMentEntry.getSrvOrdId(), equipMentEntry.getSpecialtyCode());
            if(i == 0)
                equipMentRecycleDao.addEquip(equipMentEntry);
            else
                equipMentRecycleDao.updateEquip(equipMentEntry);
            resMap.put("success",true);
            logger.info("------插入设备回收信息------");
        }
        catch (Exception e) {
            resMap.put("success",false);
            resMap.put("message",e.getMessage());
            e.printStackTrace();
        }
        return resMap;
    }

    @Override
    public List<EquipMentEntry> queryEquipBySrvOrdId(String srvOrdId) {
        return equipMentRecycleDao.queryEquipBySrvOrdId(srvOrdId);
    }

    @Override
    public List<Map> queryResEquip(Map<String, Object> params) {
        List<Map> result = new ArrayList<Map>();
        Map retmap = new HashMap();
        String zyResponse = "";
        String createdate = "";
        String updatedate = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式

        Map<String, Object> userInfoMap = untilService.queryStaffInfo();
        if(MapUtil.getString(params, "region").isEmpty());{
            params.put("region", MapUtil.getString(userInfoMap, "areaId"));
        }
        PageInfo pageInfo= new PageInfo();//分页信息
        pageInfo.setIndexSizeData(params.get("pageIndex"),params.get("pageSize"));
        //查询参数
        String json = null;
        try {
            Map<String, Object> daoMap = new HashMap<String, Object>();
            daoMap.put("START_ROW",pageInfo.getRowStart());//分页开始行
            daoMap.put("END_ROW",pageInfo.getRowEnd());//分页结束行
            daoMap.put("ENGINE_ROOM_NAME",MapUtil.getString(params,"equipName"));
            daoMap.put("ENGINE_ROOM_CODE",MapUtil.getString(params,"equipCode"));
            daoMap.put("STATION",MapUtil.getString(params,"station"));
          //  daoMap.put("EQUIP_NAME",MapUtil.getString(params,"room"));
            daoMap.put("EQUIP_CODE",MapUtil.getString(params,"room"));
            daoMap.put("ALONG_REGION",MapUtil.getString(params,"region"));

            // Map map = new HashMap();
            String userId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            Map<String, Object> staffMap = orderDealDao.getOperStaffInfo(Integer.valueOf(userId));
            String orgId = MapUtil.getString(staffMap, "ORG_ID") ;
            // map.put("HANDLE_DEP_ID",orgId);
            String regionCode = wsd.qryRegionCode(orgId);
            daoMap.put("REGION_ID",regionCode);
            //request.put("requestHeader", HttpClientJson.requestHeader(map, "queryResource"));

            //生成json报文
            JSONObject request = new JSONObject();
            Map<String, Object> equipApiMap = new HashMap<String, Object>();
            equipApiMap.put("EQUIP_API_REQ",daoMap);
            request.put("UNI_BSS_BODY",equipApiMap);

            json = request.toJSONString();
        }
        catch (NumberFormatException e) {
            retmap.put("RESP_CODE", "失败");
            retmap.put("RESP_DESC", "设备查询拼接报文异常！异常信息：" + e.getMessage()+"\n"+json);
            result.add(retmap);
            return result;
        }
        try {
            //调对方接口，发json报文 ，接收返回json报文
            String url = wsd.queryUrl("RES_EQUIPMENT");
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
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("interfname", "资源设备查询接口");
            map.put("url", url);
            map.put("content", json);
            map.put("createdate", updatedate);
            map.put("returncontent", zyResponse);
            //map.put("orderno", circuitCode);
            map.put("remark", "接收资源返回报文");
            map.put("updatedate", updatedate);
            //5.报文入库，数据入库
            wsd.saveJson(map);
        }
        catch (Exception e) {
            logger.error("调用资源设备查询接口异常！异常信息：" + e.getMessage());
        //    e.printStackTrace();
            retmap.put("RESP_CODE", "失败");
            retmap.put("RESP_DESC", "调用资源设备查询接口异常！：" + e.getMessage()+"\n"+json);
            result.add(retmap);
            return result;
        }

        try {
            JSONObject response = JSONObject.parseObject(zyResponse);
            JSONObject body = response.getJSONObject("UNI_BSS_BODY");
            JSONObject equip_api_rsp = body.getJSONObject("EQUIP_API_RSP");
            String resp_code = equip_api_rsp.getString("RESP_CODE");
            String resp_desc = equip_api_rsp.getString("RESP_DESC");
            if("0000".equals(resp_code)){
                JSONArray equip_list = equip_api_rsp.getJSONArray("EQUIP_LIST");
                if(equip_list != null){
                    for (int i = 0;i<equip_list.size();i++) {
                        JSONObject equip_list_Obj = equip_list.getJSONObject(i);
                        String equip_code = equip_list_Obj.getString("EQUIP_CODE");
                        String equip_name = equip_list_Obj.getString("EQUIP_NAME");
                        String equip_type = equip_list_Obj.getString("EQUIP_TYPE");
                        String equip_model = equip_list_Obj.getString("EQUIP_MODEL");
                        retmap.put("equip_code",equip_code);
                        retmap.put("equip_name",equip_name);
                        retmap.put("equip_type",equip_type);
                        retmap.put("equip_model",equip_model);
                        result.add(retmap);
                    }
                }
            }
            else if("8888".equals(resp_code)){
                retmap.put("RESP_CODE", "其他");
                retmap.put("RESP_DESC", resp_desc );
                result.add(retmap);
                return result;
            }
            else{
                retmap.put("RESP_CODE", "失败");
                retmap.put("RESP_DESC", "查询异常-资源系统反馈信息：" + resp_desc +"\n"+json);
                result.add(retmap);
                return result;
            }
        }
        catch (Exception e) {
            logger.error("解析资源设备反馈信息异常！异常信息：" + e.getMessage(),e);
            retmap.put("RESP_CODE", "失败");
            retmap.put("RESP_DESC", "解析资源设备反馈信息异常！：" + e.getMessage()+"\n"+json);
            result.add(retmap);
            return result;
        }
        return result;
    }

    @Override
    public List<Map<String,Object>> queryAlongArea() {
        Map<String, Object> userInfoMap = untilService.queryStaffInfo();
        String areaId = MapUtil.getString(userInfoMap, "areaId");
        return equipMentRecycleDao.queryAreaList(areaId);
    }
}
