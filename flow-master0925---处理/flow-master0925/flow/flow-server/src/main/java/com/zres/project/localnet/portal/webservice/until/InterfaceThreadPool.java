package com.zres.project.localnet.portal.webservice.until;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.util.Assert;

import com.zres.project.localnet.portal.logInfo.ILogger;
import com.zres.project.localnet.portal.logInfo.entry.LoggerObj;
import com.zres.project.localnet.portal.webservice.IInterface;

import com.ztesoft.res.frame.web.util.ResMessageSourceUtil;

public class InterfaceThreadPool {

	private static ExecutorService executorService = Executors.newFixedThreadPool(5);

	public static void tuneIntfToExecute(IInterface interfaceIntf, Map<String, Object> intfMap){
		executorService.execute(new InterfaceTuneTask(interfaceIntf, intfMap));
	}

	public static void shutDown(){
		if(!executorService.isShutdown())
			executorService.shutdown();
	}

	static class InterfaceTuneTask implements Runnable {
		private IInterface interfaceIntf;

		private Map<String, Object> intfMap;

		public InterfaceTuneTask(IInterface interfaceIntf, Map<String, Object> intfMap){
			Assert.notNull(interfaceIntf, ResMessageSourceUtil.getMessage("logger.notnull.assert"));
			this.interfaceIntf = interfaceIntf;
			Assert.notNull(intfMap, ResMessageSourceUtil.getMessage("loggerObj.notnull.assert"));
			this.intfMap = intfMap;
		}

		public void run() {
			try {
				interfaceIntf.intfTuneMethod(intfMap);
			}catch (Exception e){
				System.out.println("执行失败:"+e.getMessage());
			}
		}
	}
}
