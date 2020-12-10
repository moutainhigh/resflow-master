package com.zres.project.localnet.portal.webservice.res;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tang.huili on 2020/2/19.
 */
public interface ResCfsAttrUpdateServiceIntf {
    /**
     * 更新资源配置cfs扩展属性，并更新电路属性
     * 更新资源配置cfs扩展属性，并更新电路属性
     * @param param
     * @return Map
     */
    public Map resCfsAttrUpdate(Map<String, Object> param);

    /**
     * 是否调用资源拓展接口更新全程报竣时间、起止租时间
     */
    public void resAttrUpdate(Map<String, Object> resParams,List<HashMap<String, String>> operAttrsList);

    /**
     * 一干起租通知、集客起租通知 传入起租时间
     * @param srvOrdId
     * @param rentTime
     */
    public void resRentTimeUpdate(String srvOrdId,String rentTime);
    /**
     * 更新全程报竣时间、起租时间
     * @param srvOrdId
     * @param rentTime
     * @param finshishTime
     */
    public void resTimeUpdate(String srvOrdId,String rentTime,String finshishTime);
}
