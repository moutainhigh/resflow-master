package com.zres.project.localnet.portal.webservice.provinceRes;

import java.util.Map;

/**
 * @ClassName ProvinceFinishOrderServiceIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/8/11 17:51
 */
public interface ProvinceFinishOrderServiceIntf {
    /**
     * 省内dia自动开通反馈接口（激活）
     * @param  request
     * @return
     */
    public Map<String,Object> provinceFinishOrder(String request);
}
