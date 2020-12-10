package com.zres.project.localnet.portal.webservice.res;

import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.core.spring.SpringContext;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by tang.huili on 2019/8/26.
 */
public class BusinessQueryThread extends Thread {
    private static Logger logger = LoggerFactory.getLogger(BusinessQueryThread.class);
    private BusinessQueryServiceIntf businessQueryServiceIntf;
    private WebServiceDao webServiceDao;
    private ResSuspendThread resSuspendThread;

    private Map<String,Object> params = null;

    public BusinessQueryThread(Map<String,Object> params){
        this.params = params;
        businessQueryServiceIntf = new BusinessQueryService();
        webServiceDao = SpringContext.getBean(WebServiceDao.class);


    }

    public void run() {
//        List<Map<String,Object>> listMap = new ArrayList();
        Map<String, Object> map = businessQueryServiceIntf.businessQuery(params);
        Map<String, Object> suspendParam = new HashMap<>();
        suspendParam.put("serialNumber",MapUtils.getString(params,"accNbr",""));
        if(MapUtils.getBoolean(map, "isExist")){
            List<Map<String, Object>> listMap = (List<Map<String, Object>>) map.get("data");
            // 获取查到的第一条电路
            Map<String,Object> circuitMap = listMap.get(0);
            if(circuitMap.keySet().contains("prodInstId")){
                // 如果有电路信息
                String srvOrdId = MapUtils.getString(params,"SRV_ORD_ID","");
                // 更新产品实例id
                String instanceId = MapUtils.getString(circuitMap,"prodInstId","");
                webServiceDao.updateInstanceId(srvOrdId,instanceId);
                suspendParam.put("instanceId",instanceId);
                // 插入电路编号
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置日期格式
                Map<String, Object> rmap = new HashMap<String, Object>();
                rmap.put("srv_ord_id", srvOrdId);
                rmap.put("attr_action", "0");
                rmap.put("attr_code", "20000064");
                rmap.put("attr_name", "电路编号");
                rmap.put("attr_value", MapUtils.getString(circuitMap,"circuitCode",""));
                rmap.put("attr_value_name", "");
                rmap.put("create_date", df.format(new Date()));
                rmap.put("sourse", "res");
                webServiceDao.saveRetInfo(rmap);
            }

        }
       /* suspendParam.put("activeType",MapUtils.getString(params,"ACTIVE_TYPE"));
        resSuspendThread = new ResSuspendThread(suspendParam);
        resSuspendThread.start();*/
    }
}
