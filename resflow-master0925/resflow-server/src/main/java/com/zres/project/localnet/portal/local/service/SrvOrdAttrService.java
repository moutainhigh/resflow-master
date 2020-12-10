package com.zres.project.localnet.portal.local.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.local.SrvOrdAttrServiceIntf;
import com.zres.project.localnet.portal.local.dao.SrvOrdAttrDao;

@Service
public class SrvOrdAttrService implements SrvOrdAttrServiceIntf {
    @Autowired
    private SrvOrdAttrDao srvOrdAttrDao;
    @Override
    public void insertSrvOrdAttr(String srvOrdId, String attrcode, String attrCodeName, String attrCodeValue, String sources) {
        int i = srvOrdAttrDao.countSrvOrdAttr(srvOrdId, attrcode, null);
        if(i<1){
            srvOrdAttrDao.insertSrvOrdAttr(srvOrdId,sources,attrcode,attrCodeName,attrCodeValue);
        }else {
            srvOrdAttrDao.updateSrvOrdAttr(srvOrdId,sources,attrcode,attrCodeName,attrCodeValue);
        }
    }
}
