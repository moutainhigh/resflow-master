package com.zres.project.localnet.portal.annex;

import com.zres.project.localnet.portal.listener.util.EnmuValueUtil;
import com.zres.project.localnet.portal.order.data.dao.OrderQueryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.ztesoft.res.frame.core.util.ListUtil;
import com.ztesoft.res.frame.core.util.MapUtil;

/**
 * @author wangli
 * @title: GetAnnexInfoImpl
 * @projectName flow
 * @description: TODO
 * @date 2019/4/2317:24
 */
@Service
public class GetAnnexInfoIntfService implements GetAnnexInfoIntf {
    @Autowired
    private OrderQueryDao orderQueryDao;



    @Override
    public  List<Map<String, Object>> getAnnexInfo(String params) {
        /**
         * 页面传过来的是工单id
         *
         * 之前是用工单id去查询附件，但是如果流程退单会产生新的工单id，这样退单后的工单查询附件回显会有问题；
         * 本次优化：
         * 1，用工单id查询到环节，再从附件表中查询对应环节的所有附件回显；
         * 2，子流程环节要添加专业限制；
         */
        Map<String, Object> woInfo = orderQueryDao.qryWoInfo(params);
        List<Long> woIds = orderQueryDao.qryWoIdSameTache(MapUtil.getString(woInfo, "ORDER_ID"),
                MapUtil.getString(woInfo, "TACHE_CODE"));
        //如果是完工汇总环节需要查询本地测试环节的附件，这里查询本地测试环节工单
        //update ren.jiahang at 20200921 for 福建需求：完工确认环节提交页面自动加载出来 a/z本地测试的附件+全程调测的附件
        if (EnmuValueUtil.SUMMARY_OF_COMPLETION.equals(MapUtil.getString(woInfo, "TACHE_CODE"))
                || EnmuValueUtil.SUMMARY_OF_COMPLETION_2.equals(MapUtil.getString(woInfo, "TACHE_CODE"))){
            List<Long> localTestWoIds = orderQueryDao.qryToLocalTestWoId(MapUtil.getString(woInfo, "ORDER_ID"),"LOCAL_TEST,CROSS_WHOLE_COURDER_TEST");
            if (!ListUtil.isEmpty(localTestWoIds)){
                woIds.addAll(localTestWoIds);
            }
        }
        List<Map<String, Object>> annexInfo = orderQueryDao.getAnnexInfo(woIds);
        return annexInfo;
    }
}
