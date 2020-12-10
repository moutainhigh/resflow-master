package com.zres.project.localnet.portal.applpage.service;

import java.util.List;

import com.zres.project.localnet.portal.applpage.domain.PropertyDto;

/**
 * @author :ren.jiahang
 * @date:2019/1/1@time:11:22
 */
public interface PropertyCofgIntf {
    List <PropertyDto>qureyPropConfBySrvId(String srvid );
}
