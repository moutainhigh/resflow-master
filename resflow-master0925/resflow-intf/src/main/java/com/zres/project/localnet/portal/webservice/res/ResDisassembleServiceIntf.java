package com.zres.project.localnet.portal.webservice.res;

import java.util.Map;

/**
 * Created by tang.huili on 2020/2/19.
 */
public interface ResDisassembleServiceIntf {
    /**
     * 资源自动拆机接口:自动预释放资源
     *
     * @param param
     * @return Map
     */
    public Map resDisassemble(Map<String,Object> param);
}
