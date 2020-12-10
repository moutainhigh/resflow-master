package com.zres.project.localnet.portal.applpage.service;

import java.util.Map;

/**
 * 编辑草稿
 * Created by lu.haoyang on 2019/1/4 0004.
 */
public interface EditDraftIntf {

    /**
     * 查询被选中的草稿单数据
     * @param param
     * @return
     */
    Map<String, Object> querySelectedInfo(Map<String, Object> param);

    /**
     * 查询核查单电路数据
     * @param SubscribeId
     * @return
     */
    Map<String, Object> querySelectInfo(String SubscribeId);
    Map<String, Object> queryCustInfoByAppId(String appId);


}
