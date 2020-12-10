package com.zres.project.localnet.portal.orderAbnormal.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.ztesoft.res.frame.flow.common.util.DAOUtils;

@Repository
public class ExceptionOrderDispRulesDAO {

    /**
     * 获取异常单环节派发对象候选人
     *
     * @param woPsId  工单定单规格
     * @param orderId 原单定单id
     * @return
     */
    public List<Map<String, String>> getDispCanditates(String woPsId, String[] tacheCodes, String orderId) {
        StringBuffer sb = new StringBuffer();
        List<String> sbParm = new ArrayList<>();
        sb.append("SELECT DEAL_USER_ID DISP_ID, WO_ID, WO_STATE, DISP_OBJ_ID \n")
                .append("FROM GOM_WO \n")
                .append("WHERE DEAL_USER_ID IS NOT NULL \n")
                .append("  and WIID IN (select max(a.WIID) \n")
                .append("               from GOM_WO a \n")
                .append("                      join GOM_PS_2_WO_S b on b.id = a.PS_ID \n")
                .append("               where 1 = 1 \n");
        if (StringUtils.isNotEmpty(woPsId)) {
            sb.append("                 and b.TACHE_ID in (select t.tache_id from GOM_PS_2_WO_S t where t.id = ?) \n");
            sbParm.add(woPsId);
        }
        else {
            sb.append("                 and b.TACHE_ID in ( \n");
            sb.append(" select t.id from uos_tache t where t.tache_code in ( \n");
            for (String tacheCode : tacheCodes) {
                sb.append("?,");
                sbParm.add(tacheCode);
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(")) \n");
        }
        sb.append("                 and a.ORDER_ID = ? \n")
                .append("                 and a.WO_TYPE = '620000001') \n");
        sbParm.add(orderId);
        return DAOUtils.executeQueryToListUpperCase(sb.toString(), sbParm.toArray());
    }

    /**
     * 获取异常单环节派发对象候选人
     * 根据原单子流程环节
     *
     * @param tacheCode
     * @param orderId
     * @return
     */
    public List<Map<String, String>> getCldDispCanditates(String tacheCode, String orderId) {
        StringBuffer sb = new StringBuffer();
        sb.append("select cw.DEAL_USER_ID disp_id, cw.WO_ID, cw.WO_STATE, cw.DISP_OBJ_ID, co.ORDER_ID \n")
                .append("from gom_wo cw \n")
                .append("       join GOM_ORDER co on co.ORDER_ID = cw.ORDER_ID \n")
                .append("       join GOM_PS_2_WO_S cpw on cpw.ID = cw.PS_ID \n")
                .append("       join UOS_TACHE ut on ut.ID = cpw.TACHE_ID \n")
                .append("where 1 = 1 \n")
                .append("  and co.PARENT_ORDER_ID = ? \n")
                .append("  and cw.DEAL_USER_ID is not null \n")
                .append("  and ut.TACHE_CODE in (select TACHE_PARAM2 FROM UOS_TACHE WHERE ut.TACHE_CODE = ?) \n");
        return DAOUtils.executeQueryToListUpperCase(sb.toString(), new String[]{orderId, tacheCode});
    }

    /**
     * 获取原单定单id
     *
     * @param chgOrderId 异常单定单id
     * @return
     */
    public String getSrcOrderId(String chgOrderId) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT SRC_ORDER_ID \n")
                .append("        FROM GOM_CHANGE_ORDER_LOG_S \n")
                .append("        WHERE CHG_ORDER_ID = ? \n")
                .append("          AND SRC_ORDER_ID IS NOT NULL");
        return DAOUtils.executeQueryToString(sb.toString(), new String[]{chgOrderId});
    }

    /**
     * 获取追单状态
     *
     * @param chgOrderId
     * @return
     */
    public String getAppendOrderState(String chgOrderId) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT STATE \n")
                .append("        FROM GOM_CHANGE_ORDER_LOG_S \n")
                .append("        WHERE CHG_ORDER_ID = ? \n")
                .append("           AND CHG_TYPE = '104' \n");
        return DAOUtils.executeQueryToString(sb.toString(), new String[]{chgOrderId});
    }

    /**
     * 获取异常单对应的原单子流程定单id
     *
     * @param chgOrderId 异常单子流程定单id
     * @return
     */
    public String getSrcOrderIdFromOrdKeyInfo(String chgOrderId) {
        StringBuffer sb = new StringBuffer();
        sb.append("select b.SRC_ORDER_ID \n")
                .append("        from gom_order a \n")
                .append("               join GOM_ORD_KEY_INFO b on b.ORDER_ID = a.ORDER_ID \n")
                .append("        where a.ORDER_STATE = '200000002' \n")
                .append("          and a.ORDER_ID = ? ");
        return DAOUtils.executeQueryToString(sb.toString(), new String[]{chgOrderId});
    }

    /**
     * 根据工单规格id获取环节tachecode
     *
     * @param woPsId 工单规格id
     * @return
     */
    public String getDispTacheCode(String woPsId) {
        String sql = "SELECT B.TACHE_CODE FROM GOM_PS_2_WO_S A JOIN UOS_TACHE B ON A.TACHE_ID = B.ID WHERE A.ID = ?";
        return DAOUtils.executeQueryToString(sql, new String[]{woPsId});
    }

    /**
     * 列举出定单下所有签收人
     *
     * @return
     */
    public String[] lstOrderDealUsers(String orderId) {

        StringBuffer sb = new StringBuffer();
        sb.append("WITH X AS \n")
                .append(" (SELECT ORDER_ID \n")
                .append("    FROM GOM_ORDER \n")
                .append("   WHERE ORDER_ID = ? \n")
                .append("  UNION ALL \n")
                .append("  SELECT ORDER_ID \n")
                .append("    FROM GOM_ORDER \n")
                .append("   WHERE PARENT_ORDER_ID = ? \n")
                .append("  UNION ALL \n")
                .append("  SELECT ORDER_ID\n")
                .append("    FROM GOM_BDW_SEC_LOCAL_RELATE_INFO \n")
                .append("   WHERE PARENT_ORDER_ID = ? \n")
                .append("  UNION ALL \n")
                .append("  SELECT ORDER_ID \n")
                .append("    FROM GOM_ORDER \n")
                .append("   WHERE PARENT_ORDER_ID IN \n")
                .append("         (SELECT ORDER_ID \n")
                .append("            FROM GOM_BDW_SEC_LOCAL_RELATE_INFO \n")
                .append("           WHERE PARENT_ORDER_ID = ?)) \n")
                .append(" \n")
                .append("SELECT DISTINCT DISP_ID \n")
                .append("  FROM (SELECT DEAL_USER_ID DISP_ID \n")
                .append("          FROM GOM_WO \n")
                .append("         WHERE ORDER_ID IN (SELECT ORDER_ID FROM X) \n")
                .append("           AND DEAL_USER_ID IS NOT NULL \n")
                .append("           AND WO_TYPE = '620000001')");

        return DAOUtils.executeQueryToStringArray(sb.toString(), new String[]{orderId, orderId, orderId, orderId});
    }


}
