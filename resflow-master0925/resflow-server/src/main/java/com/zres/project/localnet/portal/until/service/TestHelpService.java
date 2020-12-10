package com.zres.project.localnet.portal.until.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.DelOrderInfoDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.EditDraftDao;
import com.zres.project.localnet.portal.initApplOrderDetail.service.DelOrderInfoService;
import com.zres.project.localnet.portal.until.data.dao.TestHelpDao;
import com.zres.project.localnet.portal.webservice.res.BusinessRollbackServiceIntf;

import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.core.util.StringUtils;

/**
 * @author :ren.jiahang
 * @date:2019/8/8@time:14:51
 */
@Service
public class TestHelpService implements TestHelpServiceIntf {
    Logger logger = LoggerFactory.getLogger(TestHelpServiceIntf.class);

    @Autowired
    private BusinessRollbackServiceIntf businessRollbackServiceIntf;
    @Autowired
    private EditDraftDao editDraftDao;
    @Autowired
    private OrderDealDao orderDealDao;
    @Autowired
    private DelOrderInfoDao delOrderInfoDao;
    @Autowired
    private DelOrderInfoService delOrderInfoService;
    @Autowired
    private TestHelpDao testHelpDao;
    @Override
    public String businessRollback(Map<String, Object> map) {
        Map returnJson = businessRollbackServiceIntf.businessRollback(map);
        return returnJson.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String delApplicationByApplCode(String appId) {
       /* HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder .getRequestAttributes()).getRequest();
        String requestUrl = request.getRequestURL().toString();//得到请求的URL地址
        String requestUri = request.getRequestURI();//得到请求的资源
        String queryString = request.getQueryString();//得到请求的URL地址中附带的参数
        String remoteAddr = request.getRemoteAddr();//得到来访者的IP地址
        String remoteHost = request.getRemoteHost();
        String remoteUser = request.getRemoteUser();
        String method = request.getMethod();//得到请求URL地址时使用的方法
        String pathInfo = request.getPathInfo();
        String localAddr = request.getLocalAddr();//获取WEB服务器的IP地址
        String localName = request.getLocalName();//获取WEB服务器的主机名*/
        appId.replaceAll("\\s*", ""); //去除空格和空白
        StringBuilder result=new StringBuilder();
        String[] applicationCodes = appId.split(",");
        for(String applicationCode:applicationCodes){

            List<String> CustOrdIdList = editDraftDao.queryCustOrdIdByApplicationCode(applicationCode);
            if (CustOrdIdList.isEmpty()){
                result.append(applicationCode+"-:信息不存在或已经删除！");
            }else{
                for (String custOrdId:CustOrdIdList) {
                    List<String> srvOrdIdList = editDraftDao.querySrvOrdIdByCustId(custOrdId,"");
                    if (!srvOrdIdList.isEmpty()) {
                        try {
                            /**
                             * 如果是退单过来的单子，草稿箱删除要回滚实例
                             * 1,先查询是否调用过创建实例接口
                             * 2，再查询是否成功调用多回滚接口
                             */
                            for (int i = 0; i < srvOrdIdList.size(); i++) {
                                int num = orderDealDao.qryInterResult(srvOrdIdList.get(i), "ResBusinessCreate");
                                if (num > 0) {
                                    Map<String, Object> param = new HashMap<String, Object>();
                                    // 调用资源回滚接口
                                    param.put("srvOrdId", srvOrdIdList.get(i));// gom_bdw_srv_ord_info.srv_ord_id
                                    param.put("rollbackDesc", "草稿箱删除。。。"); // 回滚原因
                                    /*Map retmap = businessRollbackServiceIntf.businessRollback(param);
                                    if (!"成功".equals(MapUtils.getString(retmap, "returncode"))) {*/
                                    Map retmap =  businessRollbackServiceIntf.resRollBack(param);
                                    if(!MapUtils.getBoolean(retmap,"success")){
                                        result.append(applicationCode+"-->")
                                                .append(MapUtil.getString(retmap,"message"))
                                                .append("--调用资源回滚接口失败,可能没有创建资源，但是申请单信息已删除\n");

                                        logger.error("调用资源回滚接口失败！！！" + MapUtils.getString(retmap, "message"));
                                    }
                                }
                            }
                            //删除业务订单对应的电路信息
                            // this.delOrdAttrInfoBySrvId(srvOrdIdList);
                            delOrderInfoDao.delOrdAttrInfoAll(srvOrdIdList);
                            //删除业务订单信息
                            delOrderInfoService.delOrderInfoBySrvId(srvOrdIdList);
                            //删除客户信息
                            delOrderInfoService.delCustomerInfo(custOrdId);
                        }
                        catch (Exception e) {
                            result.append(e.getMessage()) ;
                            logger.info(e.getMessage());
                            // e.printStackTrace();
                        }
                    }
                }
                result.append(applicationCode).append("删除成功\r\r");
            }
        }
        try {
            if(StringUtils.hasText(appId)){
                Map<String, Object> applicationLogMap = new HashMap<>();
                String custName = InetAddress.getLocalHost().getHostName().toString();
                String  ip = InetAddress.getLocalHost().getHostAddress().toString();
                applicationLogMap.put("custName",custName);
                applicationLogMap.put("ip",ip);
                applicationLogMap.put("applicationCode",appId);
                applicationLogMap.put("result",result.toString());
                applicationLogMap.put("type","删除申请单");
                testHelpDao.insertDelApplicationLog(applicationLogMap);
            }
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    @Override
    public List<Map<String, Object>> getApplicationLog() {
        return testHelpDao.getApplicationLog();
    }
}
