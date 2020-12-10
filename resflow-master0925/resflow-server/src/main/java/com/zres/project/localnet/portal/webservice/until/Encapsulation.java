package com.zres.project.localnet.portal.webservice.until;

import com.zres.project.localnet.portal.util.HandyTool;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Classname Encapsulation
 * @Description 封装类
 * @Author jiang.debing
 * @Date 2020/11/1610:17
 */
@Component
public class Encapsulation implements ApplicationListener<ContextRefreshedEvent> {
    Logger logger = LoggerFactory.getLogger(Encapsulation.class);
    @Autowired
    private WebServiceDao wsd;
    // 封装GOM_BDW_CODE_INFO_SECOND和GOM_BDW_CODE_INFO（转码表）
    public static Map<String, List<Map<String, Object>>> transcodingMap = new HashMap<String, List<Map<String, Object>>>();
    private static boolean isInit = false;
    public void init() {
        logger.info("=========================开始封装=========================");
        try {
            Map<String,Object> param = new HashMap<String,Object>();
            for(Map<String, Object> transcodingMap : wsd.queryTranscoding()){
                param.put("codeType", MapUtils.getString(transcodingMap,"CODE_TYPE"));
                transcodingMap.put(MapUtils.getString(transcodingMap,"CODE_TYPE"),wsd.queryTranscodingInfo(param));
            }
            logger.info("=========================封装完成=========================");
        } catch (Exception e) {
            logger.error("封装异常"+e);
        }
    }
    /**
     * 获取转码表CODE_CONTENT字段
     * @author jdb
     * @date 2020/11/16 11:51
     * @param codeType
     * @param codeValue
     * @return java.lang.String
     */
    public String getTranscodingContent(String codeType, String codeValue) {
        String content = null;
        List<Map<String, Object>> transcodingList = transcodingMap.get(codeType);
        if (HandyTool.judgeNULL(transcodingList)) {
            for (Map<String, Object> transcoding : transcodingList) {
                if (MapUtils.getString(transcoding, "CODE_VALUE").equals(codeValue)) {
                    return MapUtils.getString(transcoding, "CODE_CONTENT");
                }
            }
        }
        if (content == null) {
            Map<String, Object> codeTypeMap = new HashMap<String, Object>();
            codeTypeMap.put("codeType", codeType);
            List<Map<String, Object>> addTranscodingList = wsd.queryTranscodingInfo(codeTypeMap);
            if (HandyTool.judgeNULL(addTranscodingList)) {
                transcodingMap.put(codeType, addTranscodingList);
                for (Map<String, Object> transcoding : addTranscodingList) {
                    if (MapUtils.getString(transcoding, "CODE_VALUE").equals(codeValue)) {
                        return MapUtils.getString(transcoding, "CODE_CONTENT");
                    }
                }
            }
        }
        return codeValue;
    }
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 需要执行的逻辑代码，当spring容器初始化完成后就会执行该方法。
        if (!isInit) {
            init();
            isInit = true;
        }
    }
}
