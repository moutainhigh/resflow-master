package com.zres.project.localnet.portal.cloudNetWork.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.cloudNetWork.PortInfoSyncServiceIntf;
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
 * 端口信息同步service
 *
 * @author caomm on 2020/10/16
 */
@Service
public class PortInfoSyncService implements PortInfoSyncServiceIntf {
    Logger logger = LoggerFactory.getLogger(PortInfoSyncService.class);
    @Autowired
    private CloudNetCommonService cloudNetCommonService;
    @Autowired
    private ReceiveOrderDao receiveOrderDao;
    @Override
    public String protInfoSync(String request) {
        String ret = "";
        Map<String, Object> logMap = new HashMap<>();
        try{
            logMap.put("INTERFNAME", "端口信息同步接口");
            logMap.put("URL", "/portInfo/interfaceBDW/portInfoSync.spr");
            logMap.put("CONTENT", request);
            logMap.put("REMARK", "终端信息同步接口");
            cloudNetCommonService.insertLogInfo(logMap);
            JSONObject jsonObject = JSON.parseObject(request);
            JSONObject PORT_LIST = jsonObject.getJSONObject("PORT_LIST");
            JSONArray PORT = PORT_LIST.getJSONArray("PORT");
            if (PORT != null && PORT.size() > 0){
                List<Map<String, Object>> portList = new ArrayList<>();
                for (int i = 0; i < PORT.size(); i++) {
                    JSONObject json = PORT.getJSONObject(i);
                    Map<String, Object> portMap = new HashMap<>();
                    portMap.put("deviceName", json.getString("DEVICE_NAME"));
                    portMap.put("deviceIp", json.getString("DEVICE_IP"));
                    portMap.put("name", json.getString("NAME"));
                    portMap.put("rate", json.getString("RATE"));
                    portMap.put("manageStatus", json.getString("MANAGE_STATUS"));
                    portMap.put("status", json.getString("STATUS"));
                    portMap.put("type", json.getString("TYPE"));
                    portMap.put("mode", json.getString("MODE"));
                    portMap.put("isSub", json.getString("IS_SUB"));
                    portMap.put("ipAddress", json.getString("IP_ADDRESS"));
                    portMap.put("operation", json.getString("OPERATION"));
                    portList.add(portMap);
                }
                //批量入库端口同步信息
                receiveOrderDao.batchInsertPortInfo(portList);
                //调用资源接口将端口信息同步过去
                //TODO
                String url = cloudNetCommonService.queryUrlInfo("PORT_INFO_SYNCC_URL");
                if (!StringUtils.isEmpty(url)){

                }
            }
        }catch(Exception e){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>端口信息同步发生异常:{}", e.getMessage());
        }
        return null;
    }
}