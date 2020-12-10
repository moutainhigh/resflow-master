package com.zres.project.localnet.portal.initApplOrderDetail.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.applpage.service.QueryAttachmentIntf;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.EditDraftDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.QueryAttachmentDao;
import com.zres.project.localnet.portal.localStanbdyInfo.data.dao.OrderStandbyDao;
import com.zres.project.localnet.portal.localStanbdyInfo.service.OrderStandbyService;


@Service
public class QueryAttachmentService implements QueryAttachmentIntf {
    @Autowired
    EditDraftDao editDraftDao;
    @Autowired
    QueryAttachmentDao queryAttachmentDao;
    @Autowired
    private OrderStandbyDao orderStandbyDao;
    @Autowired
    private OrderStandbyService orderStandbyService;

    org.slf4j.Logger logger = LoggerFactory.getLogger(OrderStandbyService.class);

    @Override
    public List<Map<String, Object>> queryAttachment(String CustId) {
        //根据客户ID 查询业务订单ID
        List<String> srvOrdIdList = editDraftDao.querySrvOrdIdByCustId(CustId, null);
        //根据业务订单id查询附件
        Map<String, String> param = new HashMap<>();
        param.put("srv_ord_id", srvOrdIdList.get(0));
        param.put("origin", "FQ"); // 发起时附件标识
        List<Map<String, Object>> fileList = queryAttachmentDao.queryAttachmentId(param);
        return fileList;
    }

    @Override
    public List<Map<String, Object>> queryDDKAttachment(String srvId, String origin) {
        Map<String, String> param = new HashMap<>();
        param.put("srv_ord_id", srvId);
        param.put("origin", origin); // 发起时附件标识
        List<Map<String, Object>> fileList = queryAttachmentDao.queryAttachmentId(param);
        return fileList;
    }

    @Override
    public boolean delFileOnFtpAndDao(Map<String, Object> FileInfo) {
        try {
            orderStandbyDao.delAttachBuSrvId(FileInfo); //如果存在附件删除
            orderStandbyService.delFileToFtp(FileInfo); //删除ftp附件
            return true;
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }

    }

    @Override
    public List<Map<String, Object>> queryAttachmentOnCircuit(String SrvOrdId) {
        //根据业务订单id查询附件
        Map<String, String> param = new HashMap<>();
        param.put("srv_ord_id", SrvOrdId);
        param.put("origin", "DKDSH"); // 大宽带审核附件标识
        List<Map<String, Object>> fileList = queryAttachmentDao.queryAttachmentId(param);
        return fileList;
    }
}
