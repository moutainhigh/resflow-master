package com.zres.project.localnet.portal.webservice.govDouCheck;

import java.util.List;
import java.util.Map;

/**
 * @ClassName DualCirResCheckServiceIntf
 * @Description TODO
 * @Author tang.hl
 * @Date 2020/8/11 17:45
 */
public interface DualCirResCheckServiceIntf {
    /**
     * 4.2.	双线资源核查接口（数据接口）
     * 此接口实现了根据指定业务类型、速率、标准地址等入参信息，返回是否具备相关资源，以开通相应业务电路
     * DUAL_CIR_RES_CHECK
     * @return
     */
    public Map<String,Object> dualCirResCheck(Map<String, Object> params);
}
