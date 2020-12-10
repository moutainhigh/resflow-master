package com.zres.project.localnet.portal.local.dao;

import org.apache.ibatis.annotations.Param;

public interface SrvOrdAttrDao {

    /**
     * 查询电路属性是否存在
     * @param srvOrdId
     * @param attrcode
     * @param sourse
     * @return
     */
    int countSrvOrdAttr(@Param("srvordId") String srvOrdId, @Param("attrcode") String attrcode, @Param("sourse") String sourse);

    /**
     * 新增AZ端是否资源具备的属性
     * @param srvOrdId，accessSources,attrcodeAccress,attrCodeNameAccress,attrCodeValueAccress
     * @author wangxingyu
     * @date 2020/11/1
     */
    void insertSrvOrdAttr(@Param("srvordId") String srvOrdId, @Param("sourse") String sourse, @Param("attrcode") String attrcode, @Param("attrCodeName") String attrCodeName, @Param("attrCodeValue") String attrCodeValue);

    /**
     * 修改
     * @param srvOrdId
     * @param sourse
     * @param attrcode
     * @param attrCodeName
     * @param attrCodeValue
     */
    void updateSrvOrdAttr(@Param("srvordId") String srvOrdId, @Param("sourse") String sourse, @Param("attrcode") String attrcode, @Param("attrCodeName") String attrCodeName, @Param("attrCodeValue") String attrCodeValue);
}
