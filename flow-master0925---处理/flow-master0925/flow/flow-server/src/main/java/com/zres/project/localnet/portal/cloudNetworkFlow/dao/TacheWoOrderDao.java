package com.zres.project.localnet.portal.cloudNetworkFlow.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

/*
 * 云组网业务--环节处理dao
 * @author guanzhao
 * @date 2020/11/3
 *
 */
public interface TacheWoOrderDao {

    /*
     * 更新电路调度环节配置的专业数据 gom_bdw_ord_specialty表字段delete_state改为2
     * 新建资源还是派发专业
     * @author guanzhao
     * @date 2020/11/3
     *
     */
    int updOrderConfig(Map<String, String> updMap);

    /*
     * 查询子流程到达最后一个环节的数量
     * @author guanzhao
     * @date 2020/11/5
     *
     */
    int qryChildFlowNumAtLastNew(Map<String, String> qryMap);

    /*
     * 查询子流程的数量
     * @author guanzhao
     * @date 2020/11/5
     *
     */
    int qryChildFlowNumNew(Map<String, String> qryMap);

    /*
     * 查询退单区域和专业的工单id
     * @author guanzhao
     * @date 2020/11/16
     *
     */
    Map<String, String> qryBackOrderWoId(Map<String, String> qryMap);

    /**
     *
     * @author 查询AZ城市
     * @param qryMap
     * @return java.lang.String
     */

    String qryCity(Map<String, Object> qryMap);

    /**
     * 查询电路下发单端还是双端
     * @author thl
     * @param qryMap
     * @return java.util.Map<java.lang.String, java.lang.Object>
     */

    Map<String, Object> qryAZFlag(Map<String, Object> qryMap);

    /**
     * 查询已完成新开的流程订单orderId
     * @author thl
     * @date 2020/11/18 11:53
     * @param qryMap
     * @return java.util.Map<java.lang.String, java.lang.Object>
     */

    Map<String, Object> qryNewOpenOrderInfo(Map<String, Object> qryMap);

    /**
     * 查询B侧工单下发的端口号和VLAN号
     * @author thl
     * @date 2020/11/25 21:03
     * @param param
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */

    List<Map<String, Object>> qryAttrInfoByOrderId(Map<String, Object> param);
}
