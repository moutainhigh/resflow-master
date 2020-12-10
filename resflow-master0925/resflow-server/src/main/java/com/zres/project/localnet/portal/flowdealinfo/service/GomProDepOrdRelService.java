package com.zres.project.localnet.portal.flowdealinfo.service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.GomProDepOrdRelDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class GomProDepOrdRelService implements GomProDepOrdRelServiceIntf{

    @Autowired
    private GomProDepOrdRelDao gomProDepOrdRelDao;

    public void insertGomProDepOrdRel(List<Map<String, Object>> params) {
        List<Map<String, Object>> proDepOrdRelList = new ArrayList<Map<String, Object>>();
        if(!CollectionUtils.isEmpty(params)){
            proDepOrdRelList.addAll(params);
            gomProDepOrdRelDao.insertGomProDepOrdRel(proDepOrdRelList);
        }
    }

    public void updateSupOrderState(Map<String, Object> params) {
        if(!CollectionUtils.isEmpty(params)){
            gomProDepOrdRelDao.updateSupOrderState(params);
        }

    }


}
