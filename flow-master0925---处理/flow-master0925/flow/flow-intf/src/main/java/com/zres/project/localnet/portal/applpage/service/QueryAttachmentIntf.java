package com.zres.project.localnet.portal.applpage.service;

import java.util.List;
import java.util.Map;

/**
 * 查询或删除附件
 */
public interface QueryAttachmentIntf {

    /**
     *
     * @param CustId
     * @return
     */
    List<Map<String, Object>> queryAttachment(String CustId);

    List<Map<String, Object>> queryDDKAttachment(String srvId,String origin);


    /**
     *
     * @param FileInfo
     * @return
     */
    boolean delFileOnFtpAndDao(Map<String, Object> FileInfo);

    /**
     *
     * @param SrvOrdId
     * @return
     */
    List<Map<String, Object>> queryAttachmentOnCircuit(String SrvOrdId);
}
