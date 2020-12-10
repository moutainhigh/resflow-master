package com.zres.project.localnet.portal.dict.service;

import com.zres.project.localnet.portal.dict.dao.UnicomSysDictDao;
import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.local.domain.BaseObject;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;
import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;
import com.ztesoft.res.frame.user.inf.UserInfo;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UnicomSysDictService implements UnicomSysDictServiceIntf {

    @Autowired
    public UnicomSysDictDao unicomSysDictDao;

    @Autowired
    private WebServiceDao rsd; //数据库操作-对象

    @Autowired
    private OrderDealDao orderDealDao;

    @Override
    public List<BaseObject> querySysDict(Map<String,Object> mapJson) {
        Object codeType = mapJson.get("codeType");//类型code
        Object codeTypeName = mapJson.get("codeTypeName");//类型名称
        Map<String, Object> params = new HashMap<String, Object>();
        List<BaseObject> dictList = new ArrayList<BaseObject>();
        params.put("codeType",codeType);
        params.put("codeTypeName",codeTypeName);
        List<BaseObject> dataMaps = unicomSysDictDao.querySysDictData(params);
        if(!CollectionUtils.isEmpty(dataMaps)){
            dictList.addAll(dataMaps);
        }
        return dictList;
    }

    @Override
    public List<BaseObject> queryProdTypeLocal(Map<String, Object> mapJson) {
        Object codeType = mapJson.get("codeType");//类型code
        Object codeTypeName = mapJson.get("codeTypeName");//类型名称
        Map<String, Object> params = new HashMap<String, Object>();
        List<BaseObject> dictList = new ArrayList<BaseObject>();
        if(codeType!=null||codeTypeName!=null){
            //查询用户所在省份的区域areaId
            UserInfo user = ThreadLocalInfoHolder.getLoginUser();
            String userId = user.getUserId();
            int title = orderDealDao.queryAdminInfo(userId);
            if (title == 0) {
                params.put("userId", userId);
            }
            Map<String, Object> operStaffInfoMap = orderDealDao.getOperStaffInfo(Integer.valueOf(userId));
            String areaId =  MapUtils.getString(operStaffInfoMap,"AREA_ID");
            params.put("codeType",codeType+"_area");//查询区域产品
            params.put("codeTypeName",codeTypeName);
            params.put("jike","jike");
            params.put("areaId",areaId);
            List<BaseObject> dataMaps = unicomSysDictDao.queryProdTypeLocal(params);
            if(!CollectionUtils.isEmpty(dataMaps)){
                dictList.addAll(dataMaps);
            }else{//为空则查询公共产品
                params.remove("userId");
                params.put("codeType", codeType + "_area");
                dataMaps = unicomSysDictDao.querySysDictData(params);
                dictList.addAll(dataMaps);
            }
        }
        return dictList;
    }

    @Override
    public String qryInterfaceUrl(String interfaceName) {
        return rsd.queryUrl(interfaceName);
    }
}
