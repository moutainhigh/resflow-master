package com.zres.project.localnet.portal.webservice.res;

import java.util.Map;

/**
 * 资源接口-客户端
 * 业务实例创建接口
 * Created by csq on 2018/12/25.
 */
public interface BusinessArchiveServiceIntf {
    public Map businessArchive(Map<String,Object> params);

    public Map businessArchiveLocalSche(Map<String,Object> params);
}
