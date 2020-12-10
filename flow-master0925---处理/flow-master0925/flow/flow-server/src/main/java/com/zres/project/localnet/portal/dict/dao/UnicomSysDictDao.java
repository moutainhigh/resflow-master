package com.zres.project.localnet.portal.dict.dao;

import com.zres.project.localnet.portal.local.domain.BaseObject;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface UnicomSysDictDao {

    public List<BaseObject> querySysDictData(Map params);

    public List<BaseObject> querySysDictDataByName(Map params);

    public List<String> querySysDictDataName(Map params);

    public List<Map<String,Object>> queryAreaIdByName(@Param("areaName") String areaName);

    public List<Map<String,Object>> queryDeptIdByName(@Param("areaName") String areaName);
    List<BaseObject> queryProdTypeLocal(Map<String, Object> params);
}
