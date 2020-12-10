package com.zres.project.localnet.portal.report.service;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.local.domain.PageInfo;
import com.zres.project.localnet.portal.report.dao.ReportDao;
import com.zres.project.localnet.portal.report.utils.UserUtil;
import com.ztesoft.res.frame.core.util.MapUtil;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表系统相关接口实现
 *
 * @author PangHao
 * @date 2019/5/10 : 18:54
 */
@Service
public class ReportService implements ReportIntf {

    private Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    ReportDao reportDao;
    @Autowired
    OrderDealDao orderDealDao;

    /**
     * 调单统计报表查询
     *
     * @param map 开始及结束时间
     * @return 查询结果
     * @author PangHao
     * @date 2019/5/10 : 18:56
     */
    @Override
    public Map<String, Object> dispatchOrderStatistics(Map<String, Object> map) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            map.put("userId", UserUtil.getOperatorId());
            List<Map<String, Object>> list = reportDao.dispatchOrderStatistics(map);
            resMap.put("success", true);
            resMap.put("data", list);
            resMap.put("msg", "查询成功！");
        }catch(Exception e){
            resMap.put("success", false);
            resMap.put("msg", e.getMessage());
        }
        return resMap;
    }

    /**
     * 通过类型及描述得到字典编码
     *
     * @param map 类型编码和描述
     * @return 类型编码
     * @author PangHao
     * @date 2019/5/13 : 16:15
     */
    @Override
    public String getDicCodeByContent(Map<String, Object> map) {
        List<String> codeList = reportDao.getDicCodeByContent(map);
        if (null != codeList && !codeList.isEmpty()) {
            return codeList.get(0);
        }
        return null;
    }

    /**
     * 查询调单列表
     *
     * @param param 类型及分页信息
     * @return 调单列表
     * @author PangHao
     * @date 2019/5/13 : 16:42
     */
    @Override
    public Map<String, Object> queryDisOrderList(Map<String, Object> param) {
        Map<String, Object> resMap = new HashMap<>();
        try{
            int pageIndex = MapUtils.getIntValue(param, "pageIndex", 1);
            int pageSize = MapUtils.getIntValue(param, "pageSize", 10);
            int startRow = (pageIndex - 1) * pageSize;
            int endrow = pageIndex * pageSize;
            param.put("startRow", startRow);
            param.put("endRow", endrow);
            param.put("userId", UserUtil.getOperatorId());
            List<Map<String, Object>> circuitList = reportDao.queryDisOrderList(param);
            //封装分页数据
            Map<String, Object> pageMap = new HashMap<>();
            pageMap.put("page", MapUtils.getObject(param,"pageIndex"));
            pageMap.put("rows", circuitList);
            pageMap.put("records", reportDao.countDisOrder(param));
            resMap.put("success", true);
            resMap.put("data", pageMap);
            resMap.put("msg", "查询成功！");
        }catch(Exception e){
            e.printStackTrace();
            resMap.put("success", false);
            resMap.put("msg", e.getMessage());
        }
        return resMap;
    }

    /**
     * 开通及时率统计报表
     *
     * @param map 查询条件
     * @return 报表数据
     * @author PangHao
     * @date 2019/5/17 : 10:15
     */
    @Override
    public List<Map<String, Object>> openTimeRateStatistics(Map<String, Object> map) {
        String operStaffId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        Map<String, Object> staffMap = orderDealDao.getOperStaffInfo(Integer.valueOf(operStaffId));
        Long orgId = MapUtil.getLong(staffMap, "ORG_ID");
        //查询该部门所在省份的org_id
        Map<String, Object> provinceMap = orderDealDao.getProviceOrg(orgId);
        map.put("orgId", MapUtils.getString(provinceMap, "ORG_ID"));
        return reportDao.openTimeRateStatistics(map);
    }

    /**
     * 枚举值字典查询
     *
     * @param map CODE_TYPE编码类型
     * @return 枚举值列表
     * @author PangHao
     * @date 2019/5/20 : 17:11
     */
    @Override
    public List<Map<String, Object>> queryEnum(Map<String, Object> map) {
        return reportDao.queryEnum(map);

    }

    /**
     * 业务网络核查统计列表
     *
     * @param map 查询条件及分页信息
     * @return 分页数据
     * @author PangHao
     * @date 2019/5/20 : 17:11
     */
    @Override
    public Map<String, Object> businessNetworkVerification(Map<String, Object> map) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        PageInfo pageInfo = new PageInfo();
        pageInfo.setIndexSizeData(map.get("pageIndex"), map.get("pageSize"));
        Map<String, Object> params = MapUtils.getMap(map, "parameters");
        // 分页开始行
        params.put("startRow", pageInfo.getRowStart());
        // 分页结束行
        params.put("endRow", pageInfo.getRowEnd());
        params.put("userId", UserUtil.getOperatorId());

        // 查询总数
        Integer srvOrdCount = reportDao.querySrvOrdCount(params);
        if (srvOrdCount > 0) {
            // 通过操作人得到该省份电路单基本信息
            List<Map<String, Object>> srvOrderList = reportDao.querySrvOrdList(params);
            // 循环电路信息
            for (Map<String, Object> srvOrder : srvOrderList) {
                String srvOrdId = MapUtils.getString(srvOrder, "SRV_ORD_ID");
                //  循环拼装电路属性信息
                Map<String, Object> attrInfoMap = reportDao.getAttrInfoBySrvId(srvOrdId);
                if (MapUtils.isNotEmpty(attrInfoMap)){
                    srvOrder.putAll(attrInfoMap);
                }
                /*List<Map<String, Object>> attrList = reportDao.getAttrInfoBySrvId(srvOrdId);
                if (!attrList.isEmpty()) {
                   for (Map<String, Object> attrInfo : attrList) {
                        srvOrder.put(attrInfo.get("ATTR_VALUE_NAME").toString(), attrInfo.get("ATTR_VALUE"));
                    }
                }*/
                //  循环拼装历史路由信息
                List<String> routList = reportDao.getRouteInfoBySrvId(srvOrdId);
                if (!routList.isEmpty() && routList.size()>0) {
                    srvOrder.put("routInfoList", getSplicerString(routList));
                }
                //  循环拼装历史调单编码
                List<String> disOrderList = reportDao.getDisOrderBySrvId(srvOrdId);
                if (!disOrderList.isEmpty()) {
                    srvOrder.put("disOrderNoList", getSplicerString(disOrderList));
                }
            }
            resultMap.put("data", srvOrderList);
        }
        pageInfo.setDataCount(srvOrdCount);
        resultMap.put("dataLength", srvOrdCount);
        resultMap.put("flag", "1");
        resultMap.put("pageIndex", pageInfo.getCurrentPage());
        resultMap.put("rowNum", pageInfo.getPageSize());
        resultMap.put("total", pageInfo.getPageCount());
        return resultMap;
    }

    /**
     * 报竣未起租电路统计
     *
     * @param map 查询条件及分页信息
     * @return 分页数据
     * @author PangHao
     * @date 2019/5/20 : 17:11
     */
    @Override
    public Map<String, Object> queryCompletionNotRented(Map<String, Object> map) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        PageInfo pageInfo = new PageInfo();
        pageInfo.setIndexSizeData(map.get("pageIndex"), map.get("pageSize"));
        Map<String, Object> params = MapUtils.getMap(map, "parameters");
        // 分页开始行
        params.put("startRow", pageInfo.getRowStart());
        // 分页结束行
        params.put("endRow", pageInfo.getRowEnd());
        params.put("userId", UserUtil.getOperatorId());

        // 查询总数
        Integer srvOrdCount = reportDao.queryCompletionNotRentedCount(params);
        if (srvOrdCount > 0) {
            // 通过操作人得到该省份电路单基本信息
            List<Map<String, Object>> srvOrderList = reportDao.queryCompletionNotRentedList(params);
            // 循环电路信息
            resultMap.put("data", srvOrderList);
        }
        pageInfo.setDataCount(srvOrdCount);
        resultMap.put("dataLength", srvOrdCount);
        resultMap.put("flag", "1");
        resultMap.put("pageIndex", pageInfo.getCurrentPage());
        resultMap.put("rowNum", pageInfo.getPageSize());
        resultMap.put("total", pageInfo.getPageCount());
        return resultMap;
    }

    /**
     * 电路汇总查询
     *
     * @param map 查询条件及分页信息
     * @return 分页数据
     * @author PangHao
     * @date 2019/6/26  20:02
     **/
    @Override
    public Map<String, Object> queryCircuitSummaryList(Map<String, Object> map) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        PageInfo pageInfo = new PageInfo();
        pageInfo.setIndexSizeData(map.get("pageIndex"), map.get("pageSize"));
        Map<String, Object> params = MapUtils.getMap(map, "parameters");
        // 分页开始行
        params.put("startRow", pageInfo.getRowStart());
        // 分页结束行
        params.put("endRow", pageInfo.getRowEnd());
        params.put("userId", UserUtil.getOperatorId());
        // 查询总数
        Integer circuitCount = reportDao.queryCircuitSummaryCount(params);
        //TODO 所有数据都遍历太慢 修改sql一次性全查
     if (circuitCount > 0) {
            List<Map<String, Object>> circuitList = reportDao.queryCircuitSummaryList(params);
            // 循环分页数据补充查询数据
           /*    for (Map<String, Object> circuitMap : circuitList) {
                String circuitNo = MapUtils.getString(circuitMap, "CIRCUIT_NO");
                //得到开通信息
                Map<String, Object> dredgeData = reportDao.getDredgeDataByCircuitNo(circuitNo);
                if (null != dredgeData) {
                    circuitMap.put("KT_DIS_NO", MapUtils.getString(dredgeData, "KT_DIS_NO"));
                    circuitMap.put("KT_TIME", MapUtils.getString(dredgeData, "KT_TIME"));
                }
                //得到变更信息
                Map<String, Object> changeData = reportDao.getChangeDataByCircuitNo(circuitNo);
                if (null != changeData) {
                    circuitMap.put("BG_DIS_NO", MapUtils.getString(dredgeData, "KT_DIS_NO"));
                    circuitMap.put("BG_TIME", MapUtils.getString(dredgeData, "KT_TIME"));
                }
            }*/
            resultMap.put("data", circuitList);
        }
        pageInfo.setDataCount(circuitCount);
        resultMap.put("dataLength", circuitCount);
        resultMap.put("flag", "1");
        resultMap.put("pageIndex", pageInfo.getCurrentPage());
        resultMap.put("rowNum", pageInfo.getPageSize());
        resultMap.put("total", pageInfo.getPageCount());
        return resultMap;

    }


    /**
     * 拼接数组字符串
     *
     * @param stringList string数组
     * @return 字符串
     */
    private String getSplicerString(List<String> stringList) {
        StringBuilder splicerString = new StringBuilder();
        for (String str : stringList) {
            if(!StringUtils.isEmpty(str)){
                boolean first = true;
                if (first) {
                    splicerString.append(str);
                    first = false;
                } else {
                    splicerString.append(";").append(str);
                }
            }
        }
        return splicerString.toString();
    }


}
