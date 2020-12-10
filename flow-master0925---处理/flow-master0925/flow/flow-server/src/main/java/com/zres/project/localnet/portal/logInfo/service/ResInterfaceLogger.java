package com.zres.project.localnet.portal.logInfo.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.logInfo.ILogger;
import com.zres.project.localnet.portal.logInfo.dao.LoggerInfoDao;
import com.zres.project.localnet.portal.logInfo.entry.*;
import com.zres.project.localnet.portal.logInfo.until.LoggerThreadPool;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ztesoft.res.frame.core.util.MapUtil;


/**
 * 业务接口日志记录
 */
@Repository("resInterfaceLogger")
public class ResInterfaceLogger implements ILogger {

	@Autowired
	private LoggerInfoDao loggerInfoDao;
	@Autowired
	private ToOrderCenterIntf toOrderCenterIntf;


	@Override
	public void saveExchangeLog(LoggerObj loggerObj) {
		if(loggerObj instanceof ResInterfaceLog){
			ResInterfaceLog resInterfaceLog = (ResInterfaceLog) loggerObj;
			loggerInfoDao.saveResInterfaceInfo(resInterfaceLog);
		}else if (loggerObj instanceof ResCreateInstanceFlag){
			ResCreateInstanceFlag resCreateInstanceFlag = (ResCreateInstanceFlag) loggerObj;
			loggerInfoDao.insertResCreateInstanceFlag(resCreateInstanceFlag);
		}else if (loggerObj instanceof ResRollbackLog){
			ResRollbackLog resRollbackLog = (ResRollbackLog) loggerObj;
			loggerInfoDao.deleteResAttrCode(resRollbackLog);
		}else if (loggerObj instanceof ToOrderCenterLog){
			ToOrderCenterLog toOrderCenterLog = (ToOrderCenterLog) loggerObj;
            toOrderCenterIntf.pushCenterLog(toOrderCenterLog);
		}/*else if (loggerObj instanceof ToOrderCenterTacheLog){
			ToOrderCenterTacheLog toOrderCenterTacheLog = (ToOrderCenterTacheLog) loggerObj;
			toOrderCenterIntf.toOrderCenterTacheIntf(toOrderCenterTacheLog);
		}*/else if (loggerObj instanceof ToKafkaTacheLog){
			ToKafkaTacheLog toKafkaTacheLog = (ToKafkaTacheLog) loggerObj;
			toOrderCenterIntf.pushKafkaLog(toKafkaTacheLog);
		}

	}
}
