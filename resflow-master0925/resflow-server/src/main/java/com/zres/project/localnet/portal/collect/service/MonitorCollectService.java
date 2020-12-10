package com.zres.project.localnet.portal.collect.service;

import java.text.SimpleDateFormat;
import java.util.*;

import com.zres.project.localnet.portal.collect.enums.ProvinceEnum;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.mongodb.util.JSON.serialize;

import com.zres.project.localnet.portal.collect.data.MonitorCollectDao;
import com.zres.project.localnet.portal.collect.util.TxtExport;

import com.ztesoft.res.frame.flow.task.service.SysParams;
import org.springframework.util.StringUtils;

@Service
public class MonitorCollectService {

    private static final Logger logger = Logger.getLogger(MonitorCollectService.class);

    @Autowired
    private MonitorCollectDao monitorCollectDao;

    public void fileDataUpload() {
        try {
            logger.info("--------------  端对端采集开始 -------------------");
            long t1 = System.currentTimeMillis();
            for (ProvinceEnum provinceEnum : ProvinceEnum.values()) {
                String sql = monitorCollectDao.qryCollectSql(provinceEnum.getSqlSign());
                if(!StringUtils.isEmpty(sql)) {
                    List<Map<String, Object>> resDataList = monitorCollectDao.qryData(sql);
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmm");
                    StringBuffer fileName = new StringBuffer("DISPATCH_TACHE_");
                    fileName.append(provinceEnum.getCode()).append("_").append(df.format(new Date()));
                    //将list格式的数据转换成json
                    String resListStr = serialize(resDataList);
                    TxtExport.creatTxtFile(fileName.toString());
                    TxtExport.writeTxtFile(resListStr);
                    // 数据库配置gom_sys_param_s表
                    //String ftpBasePath = SysParams.getIns().find("FTP_SERVER_FILE_PATH");
                    Map<String, Object> ftpServerMap = new HashMap<>();
                    ftpServerMap.put("ftpBasePath", provinceEnum.getPath()); //文件目录
                    ftpServerMap.put("ftpServerInfo", "FTP_SERVER_IP_PORT_INFO"); //ip 端口
                    TxtExport.updateTxtFile(ftpServerMap);
                    long t2 = System.currentTimeMillis();
                    logger.info("--------------  端对端采集结束，历时-------------------"+ (t1 - t2));
                }
            }
        }
        catch (Exception e) {
            logger.error("txt文件创建失败！", e);
        }

    }

}
