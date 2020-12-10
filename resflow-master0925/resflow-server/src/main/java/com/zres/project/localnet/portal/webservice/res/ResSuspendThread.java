package com.zres.project.localnet.portal.webservice.res;

import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.core.spring.SpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by tang.huili on 2019/8/26.
 */
public class ResSuspendThread extends Thread {
    private static Logger logger = LoggerFactory.getLogger(ResSuspendThread.class);
    private ResSuspendServiceIntf resSuspendServiceIntf;
    private WebServiceDao webServiceDao;

    private Map<String,Object> params = null;

    public ResSuspendThread(Map<String,Object> params){
        this.params = params;
        resSuspendServiceIntf = new ResSuspendService();
        webServiceDao = SpringContext.getBean(WebServiceDao.class);
    }

    public void run() {
        resSuspendServiceIntf.resSuspendSupplement(params);
    }
}
