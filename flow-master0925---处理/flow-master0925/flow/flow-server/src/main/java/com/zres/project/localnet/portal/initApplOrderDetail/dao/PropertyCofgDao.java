package com.zres.project.localnet.portal.initApplOrderDetail.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.zres.project.localnet.portal.applpage.domain.PropertyDto;

/**
 * @author :ren.jiahang
 * @date:2019/1/1@time:11:28
 */
@Repository
public interface PropertyCofgDao {
    List<PropertyDto> qureyPropConfBySrvId(String srvid);
}
