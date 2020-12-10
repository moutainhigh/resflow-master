package com.zres.project.localnet.portal.cloudNetWork.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.cloudNetWork.TerminalInfoSyncServiceIntf;
import com.zres.project.localnet.portal.cloudNetWork.dao.ReceiveOrderDao;
import org.apache.axis.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 终端信息同步service
 *
 * @author caomm on 2020/10/16
 */
@Service
public class TerminalInfoSyncService implements TerminalInfoSyncServiceIntf {
    Logger logger = LoggerFactory.getLogger(TerminalInfoSyncService.class);
    @Autowired
    private CloudNetCommonService cloudNetCommonService;
    @Autowired
    private ReceiveOrderDao receiveOrderDao;
    @Override
    public String terminalInfoSync(String request) {
        String ret = "";
        Map<String, Object> logMap = new HashMap<>();
        try{
            logMap.put("INTERFNAME", "终端信息同步接口");
            logMap.put("URL", "/terminalInfo/interfaceBDW/terninalInfoSync.spr");
            logMap.put("CONTENT", request);
            logMap.put("REMARK", "终端信息同步接口");
            cloudNetCommonService.insertLogInfo(logMap);
            //解析报文
            JSONObject jsonObject = JSON.parseObject(request);
            JSONObject DEVICE_LIST = jsonObject.getJSONObject("DEVICE_LIST");
            JSONArray DEVICE = DEVICE_LIST.getJSONArray("DEVICE");
            List<Map<String, Object>> deviceList = new ArrayList<>();
            if (DEVICE != null && DEVICE.size() > 0){
                for (int i = 0; i < DEVICE.size(); i++){
                    Map<String, Object> deviceMap = new HashMap<>();
                    JSONObject json = DEVICE.getJSONObject(i);
                    deviceMap.put("vendor", json.getString("VENDOR"));
                    deviceMap.put("model", json.getString("MODEL"));
                    deviceMap.put("name", json.getString("NAME"));
                    deviceMap.put("ip", json.getString("IP"));
                    deviceMap.put("mac", json.getString("MAC"));
                    deviceMap.put("province", json.getString("PROVINCE"));
                    deviceMap.put("city", json.getString("CITY"));
                    deviceMap.put("onlineTime", json.getString("ONLINE_TIME"));
                    deviceMap.put("custName", json.getString("CUST_NAME"));
                    deviceMap.put("address", json.getString("ADDRESS"));
                    deviceMap.put("deviceLevel", json.getString("DEVICE_LEVEL"));
                    deviceMap.put("operation", json.getString("OPERATION"));
                    deviceList.add(deviceMap);
                }
                //批量入库终端同步信息
                receiveOrderDao.batchInsertDeviceInfo(deviceList);
                //调用资源接口将终端信息转派给资源侧
                //TODO
                String url = cloudNetCommonService.queryUrlInfo("TERMINAL_INFO_SYNCC_URL");
                if (!StringUtils.isEmpty(url)){

                }
            }
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>终端信息同步发生异常:{}", e.getMessage());
        }
        return ret;
    }
}