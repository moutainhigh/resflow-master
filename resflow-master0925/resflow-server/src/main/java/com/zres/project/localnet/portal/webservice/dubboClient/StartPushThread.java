package com.zres.project.localnet.portal.webservice.dubboClient;

import com.zres.project.localnet.portal.collect.util.TxtExport;
import com.zres.project.localnet.portal.util.ApplicationContextProvider;
import com.zres.project.localnet.portal.util.SpringContextHolderUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

public class StartPushThread extends Thread{
    private static final Logger logger = Logger.getLogger(StartPushThread.class);
    Map<String,Object> ObjMap = null;
    private JdbcTemplate springJdbcTemplate = SpringContextHolderUtil.getBean("springJdbcTemplate");
    public StartPushThread(Map<String,Object> rcMap){
        ObjMap = rcMap;
    }
    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            String queryJSON = "{\"same_day\":\"YES\",\"provinceId\":\"" + MapUtils.getObject(ObjMap,"provinceId")+ "\"}";
            String resListStr = ((ExternalDataService) ApplicationContextProvider.getBean("externalDataService")).externalData(queryJSON);
            TxtExport.creatTxtFile(MapUtils.getString(ObjMap,"fileName"));
            TxtExport.writeTxtFile(resListStr);
            // 数据库配置gom_sys_param_s表
            //String ftpBasePath = SysParams.getIns().find("FTP_SERVER_FILE_PATH");
            Map<String, Object> ftpServerMap = new HashMap<>();
            ftpServerMap.put("ftpBasePath", MapUtils.getObject(ObjMap,"ftpBasePath")); //文件目录
            ftpServerMap.put("ftpServerInfo", "FTP_SERVER_IP_PORT_INFO"); //ip 端口
            TxtExport.updateTxtFile(ftpServerMap);
            long end = System.currentTimeMillis();
            springJdbcTemplate.update("UPDATE GOM_BDW_CODE_INFO SET JIKE_ACT_TYPE='历时:"+(end - start)+"' WHERE CODE_TYPE = 'REGION_RELETED' AND RELATED_SIGN='"+MapUtils.getObject(ObjMap,"provinceId")+"'");
        }
        catch (Exception e) {
            logger.error("txt文件创建失败！", e);
        }
    }
}
