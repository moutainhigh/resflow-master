package com.zres.project.localnet.portal.webservice.dubboClient;

import com.zres.project.localnet.portal.collect.enums.ProvinceEnum;
import com.zres.project.localnet.portal.collect.util.TxtExport;
import com.zres.project.localnet.portal.util.ApplicationContextProvider;
import com.zres.project.localnet.portal.util.HandyTool;
import com.zres.project.localnet.portal.util.SpringContextHolderUtil;
import com.ztesoft.res.frame.flow.task.service.SysParams;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jiangdebing on 2020/5/27.
 */
public class PushExternalDataService implements Job {
    private static final Logger logger = Logger.getLogger(PushExternalDataService.class);
    private JdbcTemplate springJdbcTemplate = SpringContextHolderUtil.getBean("springJdbcTemplate");
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            List<Map<String,Object>> pedList = springJdbcTemplate.queryForList("SELECT CODE_VALUE FROM GOM_BDW_CODE_INFO_SECOND WHERE CODE_TYPE='pushExternalData'");
            if(HandyTool.judgeNULL(pedList)&&HandyTool.judgeNULL(pedList.get(0))&&HandyTool.GetServiceName().equals(MapUtils.getString(pedList.get(0),"CODE_VALUE"))) {
                logger.info("--------------  对外接口上传数据开始 -------------------");
                long start = System.currentTimeMillis();
                for (ProvinceEnum provinceEnum : ProvinceEnum.values()) {
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmm");
                    StringBuffer fileName = new StringBuffer("DISPATCH_ORDER_");
                    fileName.append(provinceEnum.getCode()).append("_").append(df.format(new Date()));
                    String provinceId = null;
                    try {
                        Map provinceMap = springJdbcTemplate.queryForMap("SELECT CODE_VALUE FROM GOM_BDW_CODE_INFO WHERE CODE_TYPE = 'REGION_RELETED' AND RELATED_SIGN='"+provinceEnum+"'");
                        provinceId = HandyTool.judgeNULL(provinceMap)?provinceMap.get("CODE_VALUE").toString():null;
                    }
                    catch (Exception e) {
                        continue;
                    }
                    if(HandyTool.judgeNULL(provinceId)) {
                        Map<String,Object> rcMap = new HashMap<String,Object>();
                        rcMap.put("provinceId",provinceId);
                        rcMap.put("fileName",fileName.toString());
                        rcMap.put("ftpBasePath",provinceEnum.getPath());
                        new StartPushThread(rcMap).start();
                    }
                }
                long end = System.currentTimeMillis();
                logger.info("--------------   对外接口上传数据开始结束，历时-------------------" + (end - start));
                springJdbcTemplate.update("UPDATE GOM_BDW_CODE_INFO_SECOND SET CODE_CONTENT='历时:"+(end - start)+"' WHERE CODE_TYPE='pushExternalData'");
            }
        }
        catch (Exception e) {
            logger.error("txt文件创建失败！", e);
        }
    }
}
