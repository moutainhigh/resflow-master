package com.zres.project.localnet.portal.cloudNetWork.service;

import com.zres.project.localnet.portal.cloudNetWork.dao.CloudNetCommonDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 云组网通用处理类
 *
 * @author caomm on 2020/10/12
 */
@Service
public class CloudNetCommonService {
    @Autowired
    private CloudNetCommonDao cloudNetCommonDao;

    /**
     * 枚举转换通用方法
     * @param value
     * @param type
     * @return
     */
    public String enumTrans(String value, String type){
        return cloudNetCommonDao.enumTrans(value, type);
    }

    /**
     * 日志信息入库
     * @param param
     * @return
     */
    public int insertLogInfo(Map<String, Object> param){
        return cloudNetCommonDao.insertLogInfo(param);
    }

    /**
     * 更新返回信息到日志表
     * @param msg
     * @param id
     */
    public void updateLogInfo(String msg, String id){
        cloudNetCommonDao.updateLogInfo(msg, id);
    }

    /**
     * 查询接口地址
     * @param codeType
     * @return
     */
    public String queryUrlInfo(String codeType){
        return cloudNetCommonDao.queryUrlInfo(codeType);
    }
}