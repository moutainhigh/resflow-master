package com.zres.project.localnet.portal.dict.service;

import com.zres.project.localnet.portal.local.domain.BaseObject;

import java.util.List;
import java.util.Map;

public interface UnicomSysDictServiceIntf {

    public List<BaseObject> querySysDict(Map<String,Object> mapJson);

    public List<BaseObject> queryProdTypeLocal(Map<String,Object> mapJson);

    public String qryInterfaceUrl(String interfaceName);

}
