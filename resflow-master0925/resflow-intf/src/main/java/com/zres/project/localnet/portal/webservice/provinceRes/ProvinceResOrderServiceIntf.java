package com.zres.project.localnet.portal.webservice.provinceRes;

import java.util.Map;

/**
 * @ClassName ProvinceResOrderServiceIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/8/11 16:40
 */
public interface ProvinceResOrderServiceIntf {
    /**
     * 省内dia自动开通资源配置接口
     * @param map
     * @return
     */
    public Map<String,Object> provinceResOrderService( Map<String,Object> map);

}
