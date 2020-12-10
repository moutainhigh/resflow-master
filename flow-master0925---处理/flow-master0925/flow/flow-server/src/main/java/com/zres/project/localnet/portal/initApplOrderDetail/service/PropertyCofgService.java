package com.zres.project.localnet.portal.initApplOrderDetail.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.applpage.domain.PropertyDto;
import com.zres.project.localnet.portal.applpage.service.PropertyCofgIntf;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.PropertyCofgDao;

/**
 * @author :ren.jiahang
 * @date:2019/1/1@time:11:43
 */
@Service
public class PropertyCofgService implements PropertyCofgIntf {
    @Autowired
    private PropertyCofgDao propertyCofgDao;

    public List<PropertyDto> qureyPropConfBySrvId(String srvid) {
        return propertyCofgDao.qureyPropConfBySrvId(srvid);
    }
}
