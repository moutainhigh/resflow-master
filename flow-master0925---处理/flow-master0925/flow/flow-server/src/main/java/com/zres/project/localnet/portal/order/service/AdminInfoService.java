package com.zres.project.localnet.portal.order.service;

import com.zres.project.localnet.portal.local.domain.PageInfo;
import com.zres.project.localnet.portal.order.data.dao.AdminInfoDao;
import com.zres.project.localnet.portal.order.data.dao.GomOrderQueryDao;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminInfoService implements AdminInfoServiceInf{

    @Autowired
    private AdminInfoDao adminInfoDao;

    @Override
    public Map<String, Object> addAdminInfo(Map<String, Object> params) {
        Map<String, Object> returnMap = new HashMap<>();
        params.put("levelId", 2);
        //是否已存在
        Map<String, Object> existAdmin = adminInfoDao.existAdminInfo(params);
        if ( existAdmin != null && existAdmin.size()> 0){
            String userName = MapUtils.getString(existAdmin, "USER_NAME");
            returnMap.put("result",false);
            returnMap.put("message",userName + "：用户已是管理员");
            return returnMap;
        }else{
            //是否存在此用户

                int i = 0;
                try {
                    String userName = MapUtils.getString(params, "userName");
                    //插入并返回用户名
                    i = adminInfoDao.addAdminInfo(params);
                    returnMap.put("result",true);
                    returnMap.put("message",userName + "：添加成功");
                } catch (Exception e) {
                    returnMap.put("result",false);
                    returnMap.put("message", "添加失败 "+e.getMessage());
            }
            return returnMap;
        }
    }

    @Override
    public Map<String, Object> deleteAdminInfo(Map<String, Object> params) {
        String staffId = MapUtils.getString(params, "STAFFID");
        Map<String, Object> returnMap = new HashMap<>();
        int deleteAdminInfo = adminInfoDao.deleteAdminInfo(staffId);
        if (deleteAdminInfo > 0){
            returnMap.put("result", true);
            returnMap.put("message", "删除成功");
        }else{
            returnMap.put("result",false);
            returnMap.put("message", "删除失败");
        }
        return returnMap;
    }

    @Override
    public Map<String, Object> updateAdminInfo(Map<String, Object> params) {
        Map<String, Object> returnMap = new HashMap<>();
        int updateAdminInfo = adminInfoDao.updateAdminInfo(params);
        if (updateAdminInfo > 0){
            returnMap.put("result", true);
            returnMap.put("message", "修改成功");
        }else{
            returnMap.put("result",false);
            returnMap.put("message", "修改失败");
        }
        return returnMap;
    }

    @Override
    public Map<String, Object> queryAdminInfo(Map<String, Object> params) {
        Map<String, Object> returnMap = new HashMap<>();
        List<Map<String, Object>> adminInfo = new ArrayList<>();
//        List<Map<String, Object>> adminInfo = adminInfoDao.queryAdminInfo(params);
        PageInfo pageInfo = new PageInfo(); //分页信息
        pageInfo.setIndexSizeData(params.get("pageIndex"), params.get("pageSize"));
        params.put("endRow", pageInfo.getRowEnd()); //分页结束行
        params.put("startRow", pageInfo.getRowStart()); //分页开始行
        params.put("userId", MapUtils.getString(params, "userId")); //当前登录用户id
        int woCount = adminInfoDao.countInfoList(params);
        if (woCount != 0) {
              adminInfo = adminInfoDao.queryAdminInfoList(params);
        }
        pageInfo.setDataCount(woCount);
        returnMap.put("dataLength", woCount);
        returnMap.put("pageIndex", pageInfo.getCurrentPage());
        returnMap.put("rowNum", pageInfo.getPageSize());
        returnMap.put("total", pageInfo.getPageCount());
        returnMap.put("data", adminInfo);

        return returnMap;
    }

    /**
     * 资源预占自动释放功能 增删改查 方法
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> addDisassemble(Map<String, Object> params) {
        Map<String, Object> returnMap = new HashMap<>();
        String productType = MapUtils.getString(params, "productType");
        //是否已存在
        Map<String, Object> existAdmin = adminInfoDao.existDisassembleInfo(params);
        if ( existAdmin != null && existAdmin.size()> 0){
            returnMap.put("result",false);
            returnMap.put("message",productType + "：该产品已配置，如需修改请选择修改按钮");
            return returnMap;
        }else{
            int i = 0;
            try {
                i = adminInfoDao.addDisassemble(params);
                returnMap.put("result",true);
                returnMap.put("message",productType + "：添加成功");
            } catch (Exception e) {
                returnMap.put("result",false);
                returnMap.put("message", "添加失败 "+e.getMessage());
            }
            return returnMap;
        }
    }

    @Override
    public Map<String, Object> queryDisassemble(Map<String, Object> params) {
        Map<String, Object> returnMap = new HashMap<>();
        PageInfo pageInfo = new PageInfo(); //分页信息
        pageInfo.setIndexSizeData(params.get("pageIndex"), params.get("pageSize"));

        List<Map<String, Object>> adminInfo  = adminInfoDao.queryDisassembleInfo(params);
        returnMap.put("pageIndex", pageInfo.getCurrentPage());
        returnMap.put("rowNum", pageInfo.getPageSize());
        returnMap.put("total", adminInfo.size());
        returnMap.put("data", adminInfo);
        return returnMap;
    }

    @Override
    public Map<String, Object> updateDisassemble(Map<String, Object> params) {
        Map<String, Object> returnMap = new HashMap<>();
        int updateAdminInfo = adminInfoDao.updateDisassemble(params);
        if (updateAdminInfo > 0){
            returnMap.put("result", true);
            returnMap.put("message", "修改成功");
        }else{
            returnMap.put("result",false);
            returnMap.put("message", "修改失败");
        }
        return returnMap;
    }

    @Override
    public Map<String, Object> deleteDisassemble(Map<String, Object> params) {

        Map<String, Object> returnMap = new HashMap<>();
        int deleteAdminInfo = adminInfoDao.deleteDisassemble(params);
        if (deleteAdminInfo > 0){
            returnMap.put("result", true);
            returnMap.put("message", "删除成功");
        }else{
            returnMap.put("result",false);
            returnMap.put("message", "删除失败");
        }
        return returnMap;
    }
}
