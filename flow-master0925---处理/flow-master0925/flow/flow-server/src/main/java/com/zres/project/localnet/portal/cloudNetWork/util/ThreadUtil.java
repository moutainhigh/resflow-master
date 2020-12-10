package com.zres.project.localnet.portal.cloudNetWork.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtil {
    private static ExecutorService executors;
    //创建线程池
    public static ExecutorService getsThreadInstance(){
        if (executors == null){
            executors = Executors.newCachedThreadPool();
        }
        return executors;
    }
}
