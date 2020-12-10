package com.zres.project.localnet.portal.webservice.service;

import java.util.Map;

import com.zres.project.localnet.portal.webservice.res.BusinessRenameService;
import com.zres.project.localnet.portal.webservice.res.BusinessRenameServiceIntf;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.zres.project.localnet.portal.webservice.IInterface;
import com.zres.project.localnet.portal.webservice.interfaceJiKe.FinishOrderServiceIntf;

@Repository("asynchronousInterface")
public class AsynchronousInterface implements IInterface {

    Logger logger = LoggerFactory.getLogger(AsynchronousInterface.class);

    @Autowired
    private FinishOrderServiceIntf finishOrderServiceIntf;
    @Autowired
    private BusinessRenameServiceIntf businessRenameServiceIntf;
    @Override
    public void intfTuneMethod(Map<String, Object> intfMap) {
        String type = MapUtils.getString(intfMap, "type");
        //集客反馈接口
        if ("finishOrder".equals(type)) {
            Map finMap = finishOrderServiceIntf.finishOrder(intfMap);
            if (!"1".equals(MapUtils.getString(finMap, "RESP_CODE"))) {
                logger.info("调用集客反馈接口异常，异常原因：" + MapUtils.getString(finMap, "RESP_DESC"));
            }
        }//过户更名资源同步接口
        else if ("custInfoRename".equals(type)) {
            Map finMap = businessRenameServiceIntf.custInfoRename(intfMap);
            if (!"1".equals(MapUtils.getString(finMap, "RESP_CODE"))) {
                logger.info("调用集客反馈接口异常，异常原因：" + MapUtils.getString(finMap, "RESP_DESC"));
            }
        }

    }
}
