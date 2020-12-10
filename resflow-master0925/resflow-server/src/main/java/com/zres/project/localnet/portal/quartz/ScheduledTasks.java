package com.zres.project.localnet.portal.quartz;

import com.zres.project.localnet.portal.util.DBUtil;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.ResultSet;
import java.util.Timer;

/**
 * Created by jiangdebing on 2020/5/27.
 */
@Component
public class ScheduledTasks implements ServletContextListener {
    private Timer timer = null;
    @Override
    public void contextInitialized(ServletContextEvent event) {
        timer = new Timer(true);
        event.getServletContext().log("定时器已启动");
        try {
            ResultSet stRs = DBUtil.find("SELECT CODE_VALUE,REMARK,CODE_CONTENT FROM GOM_BDW_CODE_INFO_SECOND WHERE CODE_TYPE='ScheduledTasks'");
            while (stRs.next()){
                QuartzManager.deleteJob(stRs.getString("CODE_VALUE"));
                QuartzManager.addJob(stRs.getString("CODE_VALUE"),stRs.getString("REMARK"),stRs.getString("CODE_CONTENT"));
            }
            QuartzManager.startJob();
        }
        catch (Exception e){
            System.out.println("定时器启动异常："+e);
        }finally {
            DBUtil.close();
        }
    }
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (timer != null) {
            timer.cancel();
            event.getServletContext().log("定时器销毁");
        }
    }
}
