package com.zres.project.localnet.portal.initApplOrderDetail.service;

import com.zres.project.localnet.portal.applpage.service.GomPropertiesConfIntf;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.GomPropertiesConfDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GomPropertiesConfService implements GomPropertiesConfIntf {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GomPropertiesConfDao gomPropertiesConfDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertGomPropertiesConfView(String sourceDataArray) throws Exception {
        String[] oneDryLocal = sourceDataArray.split(",");//一干、本地、集客标识
//        String[] oneDryLocal = {"onedry","localBuild"};
        try {
            //产品类型分组
            List<Map<String, Object>> mapsList = gomPropertiesConfDao.srvIdGroup();
            for (Map<String, Object> mapSrv : mapsList) {
                BigDecimal srv_id = (BigDecimal) mapSrv.get("SRV_ID");
                for (int y = 0; y < oneDryLocal.length; y++) {
                    List<Map<String, Object>> propertiesView = new ArrayList<Map<String, Object>>();
                    //本地、一干、集客配置
                    Map<String, Object> pceazConfMap = new HashMap<String, Object>();
                    pceazConfMap.put("sourceData", oneDryLocal[y]);
                    pceazConfMap.put("srvId", srv_id);
                    //A端
                    pceazConfMap.put("peCeAz", "A");
                    List<Map<String, Object>> mapsItemA = gomPropertiesConfDao.selectProperConfItem(pceazConfMap);
                    int aMax = gomPropertiesConfDao.selectPCAZColumnSortMax(pceazConfMap);
                    int h = 1;
                    for(int i=0;i<mapsItemA.size();i++){
                        Map<String, Object> stringMap = mapsItemA.get(i);
                        stringMap.put("columSort",i+h+aMax);
                        h++;
                        propertiesView.add(stringMap);
                    }
                    //Z端
                    pceazConfMap.put("peCeAz", "Z");
                    List<Map<String, Object>> mapsItemZ = gomPropertiesConfDao.selectProperConfItem(pceazConfMap);
                    int zMax = gomPropertiesConfDao.selectPCAZColumnSortMax(pceazConfMap);
                    int k = 2;
                    for(int i=0;i<mapsItemZ.size();i++){
                        Map<String, Object> stringMap = mapsItemZ.get(i);
                        stringMap.put("columSort",i+k+zMax);
                        k++;
                        propertiesView.add(stringMap);
                    }
                    //PE端
                    pceazConfMap.put("peCeAz", "PE");
                    List<Map<String, Object>> mapsItemPE = gomPropertiesConfDao.selectProperConfItem(pceazConfMap);
                    int peMax = gomPropertiesConfDao.selectPCAZColumnSortMax(pceazConfMap);
                    h = 1;
                    for(int i=0;i<mapsItemPE.size();i++){
                        Map<String, Object> stringMap = mapsItemPE.get(i);
                        stringMap.put("columSort",i+h+peMax);
                        h++;
                        propertiesView.add(stringMap);
                    }
                    //CE端
                    pceazConfMap.put("peCeAz", "CE");
                    List<Map<String, Object>> mapsItemCE = gomPropertiesConfDao.selectProperConfItem(pceazConfMap);
                    int ceMax = gomPropertiesConfDao.selectPCAZColumnSortMax(pceazConfMap);
                    k = 2;
                    for(int i=0;i<mapsItemCE.size();i++){
                        Map<String, Object> stringMap = mapsItemCE.get(i);
                        stringMap.put("columSort",i+k+ceMax);
                        k++;
                        propertiesView.add(stringMap);
                    }
                    //公共信息
                    pceazConfMap.put("peCeAz", "PU");
                    List<Map<String, Object>> mapsItemPu = gomPropertiesConfDao.selectProperConfPuItem(pceazConfMap);
                    int puMax = gomPropertiesConfDao.selectProperConfPuItemMax(pceazConfMap);
                    for (int i = 0; i < mapsItemPu.size(); i++) {
                        Map<String, Object> stringMap = mapsItemPu.get(i);
                        stringMap.put("columSort", i + 1 + puMax);
                        propertiesView.add(stringMap);
                    }
                    if (!CollectionUtils.isEmpty(propertiesView)) {
                        gomPropertiesConfDao.insertGomPropertiesConfView(propertiesView);
                    }
                }

            }

        }
        catch (Exception e) {
            throw new Exception();
        }


    }

}
