package com.zres.project.localnet.portal.local.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface PostponementDao {
    List<Map<String, Object>> queryPostponementInfoBySrvId(@Param("srvOrdId") String srvOrdId);
}
