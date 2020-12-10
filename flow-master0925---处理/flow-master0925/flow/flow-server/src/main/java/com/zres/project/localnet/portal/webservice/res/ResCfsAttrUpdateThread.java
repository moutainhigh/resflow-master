package com.zres.project.localnet.portal.webservice.res;

import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.zsmart.core.spring.SpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by tang.huili on 2019/8/26.
 */
public class ResCfsAttrUpdateThread extends Thread {
    private static Logger logger = LoggerFactory.getLogger(ResCfsAttrUpdateThread.class);
    private ResCfsAttrUpdateServiceIntf resCfsAttrUpdateServiceIntf;
    private WebServiceDao webServiceDao;

    private Map<String,Object> params = null;

    public ResCfsAttrUpdateThread(Map<String,Object> params){
        this.params = params;
        resCfsAttrUpdateServiceIntf = new ResCfsAttrUpdateService();
        webServiceDao = SpringContext.getBean(WebServiceDao.class);
    }

    @Override
    public void run() {
        Map<String, Object> map = resCfsAttrUpdateServiceIntf.resCfsAttrUpdate(params);
    }
}
