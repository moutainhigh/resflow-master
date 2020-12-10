package com.zres.project.localnet.portal.quartz;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.CronScheduleBuilder.*;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzManager {
    private static SchedulerFactory gSchedulerFactory = new StdSchedulerFactory();
    private static String JOB_GROUP_NAME = "CPDS_JOBGROUP";
    private static String TRIGGER_GROUP_NAME = "CPDS_TRIGGERGROUP";
    public static String JOB_NAME_PRE = "JOB";

    /**
      * 添加job
      * @param jobName
      * @param className
      * @param timeExp
      * @throws ClassNotFoundException 
      * @throws SchedulerException 
      */
    public static void addJob(String jobName,String className,String timeExp) throws ClassNotFoundException, SchedulerException{
        addJob(jobName,JOB_GROUP_NAME,className,timeExp,jobName,TRIGGER_GROUP_NAME);
    }
    /**
      * 添加job
      * @param jobName：job名称
      * @param jobGroup：job群组
      * @param className：job扩展执行类
      * @param timeExp：运行时间表达式
      * @param triggerName：触发器名称
      * @param triggerGroup：触发器群组
      */
    @SuppressWarnings("unchecked")
    public static void addJob(String jobName,String jobGroup,String className,String timeExp,String triggerName,String triggerGroup) throws ClassNotFoundException, SchedulerException{
        Scheduler scheduler = gSchedulerFactory.getScheduler();
        Class<? extends Job> cls = (Class<? extends Job>)Class.forName(className);
        JobDetail job = newJob(cls).withIdentity(jobName, jobGroup).build();
        job.getJobDataMap().put("id", jobName);

        CronTrigger trigger = newTrigger()
                .withIdentity(triggerName, triggerGroup)
                .startNow()
                .withSchedule(cronSchedule(timeExp))
                .build();

        scheduler.scheduleJob(job, trigger);

    }
    /**
      * 运行job
      * @throws SchedulerException 
      * 
      */
    public static void startJob() throws SchedulerException{
        System.out.println("=============== job计划任务调度器启动 =================");
        Scheduler scheduler = gSchedulerFactory.getScheduler();
        scheduler.start();
    }
    /**
      * 修改job运行时间参数
      * @param triggerName:触发器名称
      * @param timeExp：时间表达式
      */
    public static void updateJobTime(String triggerName,String timeExp) throws SchedulerException{
        updateJobTime(triggerName,TRIGGER_GROUP_NAME,timeExp);
    }
    /**
      * 修改job运行时间参数
      * @param triggerName:触发器名称
      * @param trigerGroup：触发器群组
      * @param timeExp：时间表达式
      * @throws SchedulerException 
      */
    public static void updateJobTime(String triggerName,String trigerGroup,String timeExp) throws SchedulerException{
        Scheduler scheduler = gSchedulerFactory.getScheduler();
        CronTrigger oldTrigger = (CronTrigger) scheduler.getTrigger(new TriggerKey(triggerName,trigerGroup));
        if(oldTrigger.getCronExpression().equals(timeExp))
            return;
        CronTrigger newTrigger = newTrigger()
                .withIdentity(triggerName, trigerGroup)
                .startNow()
                .withSchedule(cronSchedule(timeExp))
                .build();
        scheduler.rescheduleJob(oldTrigger.getKey(), newTrigger);
    }
    /**
      * 删除job
      * @param jobName：job名称
      * @throws SchedulerException 
      */
    public static void deleteJob(String jobName) throws SchedulerException{
        deleteJob(jobName,JOB_GROUP_NAME);
    }
    /**
      * 删除job
      * @param jobName：job名称
      * @param jobGroup：job群组
      * @throws SchedulerException 
      */
    public static void deleteJob(String jobName,String jobGroup) throws SchedulerException{
        Scheduler scheduler = gSchedulerFactory.getScheduler();
        scheduler.deleteJob(new JobKey(jobName,jobGroup));
    }
    /**
      * 停止job
      * @throws SchedulerException
      */
    public static void shutDown() throws SchedulerException{
        System.out.println("=============== job计划任务调度器关闭 =================");
        Scheduler scheduler = gSchedulerFactory.getScheduler();
        scheduler.shutdown(true);
    }
}  