package com.zres.project.localnet.portal.logInfo.until;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.util.Assert;

import com.zres.project.localnet.portal.logInfo.ILogger;
import com.zres.project.localnet.portal.logInfo.entry.LoggerObj;

import com.ztesoft.res.frame.web.util.ResMessageSourceUtil;


/**
 *     异步日志记录器线程池。
 *     主要用于记录存量接口日志、业务割接与外系统中间表交互日志、Job日志等。
 */
public class LoggerThreadPool {

	private static ExecutorService executorService = Executors.newFixedThreadPool(50);

	public static void addLoggerToExecute(ILogger logger, LoggerObj loggerObj){
		executorService.execute(new AsynLoggerTask(logger, loggerObj));
	}

	public static void shutDown(){
		if(!executorService.isShutdown())
			executorService.shutdown();
	}

	static class AsynLoggerTask implements Runnable {
		private ILogger logger;

		private LoggerObj loggerObj;

		public AsynLoggerTask(ILogger logger, LoggerObj loggerObj){
			Assert.notNull(logger, ResMessageSourceUtil.getMessage("logger.notnull.assert"));
			this.logger = logger;
			Assert.notNull(loggerObj, ResMessageSourceUtil.getMessage("loggerObj.notnull.assert"));
			this.loggerObj = loggerObj;
		}

		public void run() {
			try {
				logger.saveExchangeLog(loggerObj);
			}catch (Exception e){
				System.out.println("执行失败:"+e.getMessage());
			}
		}
	}

}
