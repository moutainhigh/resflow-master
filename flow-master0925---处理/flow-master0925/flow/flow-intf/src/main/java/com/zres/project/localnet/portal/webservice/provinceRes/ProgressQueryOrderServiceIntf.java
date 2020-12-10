package com.zres.project.localnet.portal.webservice.provinceRes;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ProgressQueryOrderServiceIntf
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/8/11 17:45
 */
public interface ProgressQueryOrderServiceIntf {
    /**
     * dia自动开通 进度查询
     * @return
     */
    public List<Map<String,Object>> progressQueryOrder(Map<String,Object> params);
}
