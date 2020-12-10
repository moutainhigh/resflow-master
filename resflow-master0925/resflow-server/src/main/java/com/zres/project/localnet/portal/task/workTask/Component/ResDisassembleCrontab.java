package com.zres.project.localnet.portal.task.workTask.Component;

import com.zres.project.localnet.portal.collect.data.MonitorCollectDao;
import com.zres.project.localnet.portal.collect.enums.ProvinceEnum;
import com.zres.project.localnet.portal.flowdealinfo.data.util.BasicCode;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.zres.project.localnet.portal.webservice.res.ResDisassembleServiceIntf;
import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @ClassName ResDisassembleCrontab
 * @Description ：精准串行核查流程，增加资源预占释放功能，在后台管理下增加，核查资源预占时间设置
 * @Author wang.g2
 * @Date 2020/9/2 14:38
 * I am not responsible for this code,
 * They asked me to write, not voluntarily
 */
@Component
public class ResDisassembleCrontab {

    Logger logger = LoggerFactory.getLogger(ResDisassembleCrontab.class);
    @Autowired
    private MonitorCollectDao monitorCollectDao;
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private ResDisassembleServiceIntf resDisassembleServiceIntf;

    public void autoResDisassemble() {
        try {
            logger.info("--------------  精准串行核查流程，资源预占释放功能开始 -------------------");
            //1.查询所有的（所有的数据会越来越多，做成可配置sql 可以配置成最近几个月的）已完成的核查单数据
            for (ProvinceEnum provinceEnum : ProvinceEnum.values()) {
                String sql = monitorCollectDao.qryResDisassemble(provinceEnum.getCode());
                if(!StringUtils.isEmpty(sql)){

                    //2. 根据已完成的核查单的省份 产品的 查询用户配置的预占时长/天
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<Map<String, Object>> finshCheckList = monitorCollectDao.qryData(sql);
                            int normal = 0;
                            for (Map<String, Object> checkInfo : finshCheckList) {
                                String regionId = MapUtils.getString(checkInfo, "area_id");//省份
                                String serviceId = MapUtils.getString(checkInfo, "service_id");//产品
                                String srvOrdId = MapUtils.getString(checkInfo, "srv_ord_id");//产品
                                String cstOrdId = MapUtils.getString(checkInfo, "cst_ord_id");//产品
                                String resouse = MapUtils.getString(checkInfo, "resources");//系统来源
                                String systemResource = MapUtils.getString(checkInfo, "system_resource");//系统
                                String state = MapUtils.getString(checkInfo, "state");//是否已释放
                                String createDate = MapUtils.getString(checkInfo, "create_date");//资源预占时间  order_id = 10214260
                                //关联开通单参数
                                String serialNumber = MapUtils.getString(checkInfo, "serial_number");//关联开通单
                                String tradeId = MapUtils.getString(checkInfo, "trade_id");//关联开通单
                                String instanceId = MapUtils.getString(checkInfo, "instance_id");//关联开通单
                                Map<String, Object> preemption = wsd.queryDisassembleInfo(serviceId, regionId);//配置预占时长
                                //TODO （开通单作废再开通，开通单作废 要做判断处理）  集客 本地 关联原单的方法不一样  一干暂时没有核查单
                                //2.1 关联开通单 集客以及本地发起 （一干没有核查单）
                                if("jike".equals(resouse)){
                                    //集客来单
                                    normal = wsd.existOpenOrder(serialNumber, tradeId);
                                }else{
                                    //本地发起
                                    normal = wsd.existOpenOrderLocal(instanceId);
                                }
                                //2.2  需要特殊处理下 所属系统 （二干，本地，以及二干下发本地 其中二干下发本地的在自动拆机接口特殊处理）

                                if (BasicCode.SECOND.equals(systemResource)){
                                    List<Map<String, Object>> system = wsd.queryBelongSystem(srvOrdId);
                                    //2.1.1  先把二干拆掉  如果有下发本地再遍历拆掉本地
                                    for (Map<String,Object> srvOrdIds:system ) {
                                        systemResource = "second-local-flow";
                                        //3.根据对比核查单完成时间与配置时长 看下有无开通单  最新的 状态是
                                        disassembleRes(preemption,normal,state,createDate,MapUtils.getString(srvOrdIds,"RELATE_INFO_ID"),cstOrdId,systemResource);
                                    }
                                    systemResource = "second-schedule-lt";
                                    disassembleRes(preemption,normal,state,createDate, srvOrdId,cstOrdId,systemResource);

                                }else if (BasicCode.LOCAL.equals(systemResource)) {
                                    //2.1.2 拆掉本地
                                    systemResource = "local-flow-schedule";
                                    disassembleRes(preemption,normal,state,createDate, srvOrdId,cstOrdId,systemResource);
                                }
                            }
                        }
                    }).start();
                    logger.info("--------------  精准串行核查流程，资源预占释放功能结束， -------------------");
                }
            }
        }
        catch (Exception e) {
            logger.error("精准串行核查流程，资源预占释放功能失败！", e);
        }
    }


    private  boolean getDatePoor(String fishDate,Long preemptionDate) {
        boolean flag = false;
        try {
            Date nowDate = new Date();
            SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
            Date endDate = sdf.parse( fishDate );
            long nd = 1000 * 24 * 60 *60;

            // 获得两个时间的毫秒时间差异
            long diff =  nowDate.getTime()-endDate.getTime() ;
            // 计算差多少天
            long day = diff / nd;
            // 日期字符串
            if(day > preemptionDate){
                flag = true;
            }else{
                flag = false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return flag;
    }


    private void disassembleRes(Map<String,Object> preemption,int normal,String state,String createDate, String srvOrdId,String cstOrdId,String systemResource){

        if (preemption != null && preemption.size() > 0 && normal < 1 && !"1".equals(state) ) {
            boolean flag = getDatePoor(createDate, MapUtils.getLong(preemption, "REMARK"));
            if (flag) {
                Map<String, Object> resDisassemble = new HashMap<>();
                resDisassemble.put("srvOrdId", srvOrdId);
                resDisassemble.put("cstOrdId", cstOrdId);
                resDisassemble.put("remark", "精准串行核查流程，资源预占释放");
                resDisassemble.put("flag", systemResource);//所属系统
                //4.没有开通单的需要释放掉资源
                Map<String,Object> disassemble = resDisassembleServiceIntf.resDisassemble(resDisassemble);
                if(MapUtils.getBoolean(disassemble,"returncode")){
                    resDisassemble.put("state", "1");// 1成功 0 失败
                }else{
                    resDisassemble.put("state", "0");
                }
                //5.新建表记录回滚  回滚过就不需要回滚了
                wsd.insertDisassembleInfo(resDisassemble);
            }
        }
    }
}
