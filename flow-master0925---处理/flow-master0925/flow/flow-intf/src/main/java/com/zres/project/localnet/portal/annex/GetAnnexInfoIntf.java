package com.zres.project.localnet.portal.annex;

import java.util.List;
import java.util.Map;

/**
 * @Author wangli
 * @Description //TODO
 * @Date 17:18 2019/4/23
 * @Param
 * @return
 **/
public interface GetAnnexInfoIntf {
    /**
     * 查询附件
     * @param params
     * @return
     */
    public List<Map<String, Object>> getAnnexInfo(String params);


}
