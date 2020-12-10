package com.zres.project.localnet.portal.order.data.dao;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AdminInfoDao {

    List<Map<String, Object>> queryAdminInfoList(Map<String, Object> params);

    int countInfoList(Map<String, Object> params);

    int addAdminInfo(Map<String, Object> params);

    int deleteAdminInfo(String taffId);

    int updateAdminInfo(Map<String, Object> params);

    Map<String, Object> existAdminInfo(Map<String, Object> params);

    Map<String, Object>  existStaff(Map<String, Object> params);

    /**
     * 资源释放相关
     * @param params
     * @return
     */
    int addDisassemble(Map<String, Object> params);

    int deleteDisassemble(Map<String, Object> params);

    int updateDisassemble(Map<String, Object> params);

    List<Map<String, Object>> queryDisassembleInfo(Map<String, Object> params);

    Map<String, Object>  existDisassembleInfo(Map<String, Object> params);

}
