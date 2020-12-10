package com.zres.project.localnet.portal.webservice.res;

import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.core.spring.SpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by tang.huili on 2019/8/26.
 */
public class ResUnsuspendThread extends Thread {
    private static Logger logger = LoggerFactory.getLogger(ResUnsuspendThread.class);
    private ResUnsuspendServiceIntf resUnsuspendServiceIntf;
    private WebServiceDao webServiceDao;

    private Map<String,Object> params = null;

    public ResUnsuspendThread(Map<String,Object> params){
        this.params = params;
        resUnsuspendServiceIntf = new ResUnsuspendService();
        webServiceDao = SpringContext.getBean(WebServiceDao.class);
    }

    public void run() {
        resUnsuspendServiceIntf.resUnsuspendSupplement(params);
    }
}
