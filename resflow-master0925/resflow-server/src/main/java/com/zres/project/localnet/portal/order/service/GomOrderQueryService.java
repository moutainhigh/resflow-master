package com.zres.project.localnet.portal.order.service;

import com.zres.project.localnet.portal.local.domain.PageInfo;
import com.zres.project.localnet.portal.order.data.dao.GomOrderQueryDao;
import com.zres.project.localnet.portal.order.domain.GomDispatcherOrderPo;
import com.zres.project.localnet.portal.order.domain.GomDispatcherOrderVo;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GomOrderQueryService implements GomOrderQueryServiceIntf {
    Logger logger = LoggerFactory.getLogger(GomOrderQueryService.class);

    @Autowired
    private GomOrderQueryDao gomOrderQueryDao;

//    @Value("${localSchedule.upload.path}")
//    private String localPath;

    public GomDispatcherOrderVo queryWo(Map<String,Object> params) {
        GomDispatcherOrderVo gomOrderVo = new GomDispatcherOrderVo();
        List<GomDispatcherOrderPo> mapArrayList = new ArrayList<GomDispatcherOrderPo>();//返回的订单数据
        PageInfo pageInfo= new PageInfo();//分页信息
        pageInfo.setIndexSizeData(params.get("pageIndex"),params.get("pageSize"));
        int localCount = 0;
        try{
            SimpleDateFormat simpleFormate =  new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String finishDateStart = (String)params.get("finishDateStart");
            String finishDateEnd = (String)params.get("finishDateEnd");
            java.sql.Date finishDateStartsql=StringUtils.hasText(finishDateStart)?new java.sql.Date(simpleFormate.parse(finishDateStart).getTime()):null;
            java.sql.Date finishDateEndsql=StringUtils.hasText(finishDateEnd)?new java.sql.Date(simpleFormate.parse(finishDateEnd).getTime()):null;

            //查询参数
            HashMap<String, Object> daoMap = new HashMap<String, Object>();
            daoMap.put("sortCol",params.get("sortCol"));//排序列
            daoMap.put("sortOrder",params.get("sortOrder")); //排序列顺序
            daoMap.put("startRow",pageInfo.getRowStart());//分页开始行
            daoMap.put("endRow",pageInfo.getRowEnd());//分页结束行
            daoMap.put("databaseType","oracle");//数据库类型
            daoMap.put("orderTitle",params.get("orderTitle")); //调单标题
            daoMap.put("custName",params.get("custName")); //客户名称
            daoMap.put("cirNum",params.get("cirNum")); //调单编号
            daoMap.put("serialNumber",params.get("serialNumber"));//业务号码
            daoMap.put("tradeId",params.get("tradeId"));//业务订单号
            daoMap.put("finishDateStart",finishDateStartsql);//完成开始时间
            daoMap.put("finishDateEnd",finishDateEndsql);//完成结束时间
            daoMap.put("subscribeId",params.get("subscribeId"));//客户流水号
            daoMap.put("actType",params.get("actType"));//动作类型
            daoMap.put("productType",params.get("productType"));//产品类型
            String userId = "";
            if (ThreadLocalInfoHolder.getLoginUser()!=null){
                userId = ThreadLocalInfoHolder.getLoginUser().getUserId();
            }
            daoMap.put("userId",userId);//当前登录用户id

            localCount = gomOrderQueryDao.queryGomDispatcherOrderCount(daoMap);
            if(localCount!=0){
                List<GomDispatcherOrderPo> gomDispatcherOrderPosList = gomOrderQueryDao.queryGomDispatcherOrderPage(daoMap);
                if(!CollectionUtils.isEmpty(gomDispatcherOrderPosList)){
                    mapArrayList.addAll(gomDispatcherOrderPosList);
                }
            }
            gomOrderVo.setGomDispatcherOrderPoList(mapArrayList);
            gomOrderVo.setMessage("success");

        }catch(Exception e){
            logger.error(e.getMessage());
            gomOrderVo.setMessage("fail");
            gomOrderVo.setGomDispatcherOrderPoList(mapArrayList);
        }
        pageInfo.setDataCount(localCount);
        gomOrderVo.setPageInfo(pageInfo);
        return gomOrderVo;
    }
    public int countWo(Map<String,Object> params) {
        return gomOrderQueryDao.queryGomDispatcherOrderCount(params);
    }

    public List<GomDispatcherOrderPo> getGomOrderExportData(Map<String,Object> params){
        List<GomDispatcherOrderPo> gomDispatcherOrderPosList = gomOrderQueryDao.queryGomDispatcherExportOrderData(params);
        return gomDispatcherOrderPosList;
    }


}
