package com.zres.project.localnet.portal.webservice.dto;

/**
 * @Description:异常单枚举类
 * @Author:zhang.kaigang
 * @Date:2019/5/15 18:04
 * @Version:1.0
 */
public enum ExceptionFlowEnum {
    /**------------------------------一干相关枚举值映射start-----------------------------*/

    // 1.产品信息相关枚举值
    // 1.1动作类型
    ACTIVE_TYPE_101("新装", "101"),
    ACTIVE_TYPE_102("关闭", "102"),
    ACTIVE_TYPE_103("变更", "103"),

    // 2.调单信息相关枚举值
    // 2.1调单类型
    DISPATCH_TYPE_10("调单", "10"),
    DISPATCH_TYPE_11("变更单", "11"),
    /**------------------------------一干相关枚举值映射start-----------------------------*/


    /**------------------------------集客相关枚举值映射start-----------------------------*/
    // 1.产品信息相关枚举值
    // 1.1 业务类型
    SERVICE_ID_80000039("IDC", "80000039"),
    SERVICE_ID_80000465("UTN智能专线", "80000465"),
    SERVICE_ID_80000014("SDH", "80000014"),
    SERVICE_ID_80000015("以太网专线（MSTP）", "80000015"),
    SERVICE_ID_80000017("互联网专线", "80000017"),

    // 1.2 订单业务受理编码
    TRADE_TYPE_CODE_2001("产品订购", "2001"),
    TRADE_TYPE_CODE_2007("集团用户信息变更", "2007"),
    TRADE_TYPE_CODE_2010("集团产品变更", "2010"),
    TRADE_TYPE_CODE_2011("移机", "2011"),
    TRADE_TYPE_CODE_2013("停机", "2013"),
    TRADE_TYPE_CODE_2014("复机", "2014"),
    TRADE_TYPE_CODE_2019("拆机", "2019"),
    TRADE_TYPE_CODE_2024("信控停机", "2024"),
    TRADE_TYPE_CODE_2025("强制停机", "2025"),
    TRADE_TYPE_CODE_2026("信控复机", "2026"),
    TRADE_TYPE_CODE_2027("强制复机", "2027"),
    TRADE_TYPE_CODE_2028("资费变更", "2028"),

    // 1.3 服务提供标识
    SERVICE_OFFER_ID_100000159("产品订购", "100000159"),
    SERVICE_OFFER_ID_100000219("产品订购资源核查", "100000219"),
    SERVICE_OFFER_ID_100000158("产品变更", "100000158"),
    SERVICE_OFFER_ID_100000220("产品变更资源核查", "100000220"),
    SERVICE_OFFER_ID_100000160("产品停机", "100000160"),
    SERVICE_OFFER_ID_100000161("产品开机", "100000161"),
    SERVICE_OFFER_ID_100000162("产品移机", "100000162"),
    SERVICE_OFFER_ID_100000221("产品移机资源核查", "100000221"),
    SERVICE_OFFER_ID_100000355("产品拆机", "100000355"),
    SERVICE_OFFER_ID_100000165("产品订购", "100000165"),
    SERVICE_OFFER_ID_100000222("产品订购资源核查", "100000222"),
    SERVICE_OFFER_ID_100000164("产品变更", "100000164"),
    SERVICE_OFFER_ID_100000223("产品变更资源核查", "100000223"),
    SERVICE_OFFER_ID_100000166("产品停机", "100000166"),
    SERVICE_OFFER_ID_100000167("产品开机", "100000167"),
    SERVICE_OFFER_ID_100000168("产品移机", "100000168"),
    SERVICE_OFFER_ID_100000224("产品移机资源核查", "100000224"),
    SERVICE_OFFER_ID_100000356("产品拆机", "100000356"),
    SERVICE_OFFER_ID_100000177("产品订购", "100000177"),
    SERVICE_OFFER_ID_100000228("产品订购资源核查", "100000228"),
    SERVICE_OFFER_ID_100000176("产品变更", "100000176"),
    SERVICE_OFFER_ID_100000229("产品变更资源核查", "100000229"),
    SERVICE_OFFER_ID_100000178("产品停机", "100000178"),
    SERVICE_OFFER_ID_100000179("产品开机", "100000179"),
    SERVICE_OFFER_ID_100000180("产品移机", "100000180"),
    SERVICE_OFFER_ID_100000230("产品移机资源核查", "100000230"),
    SERVICE_OFFER_ID_100000358("产品拆机", "100000358");

    /**------------------------------集客相关枚举值映射end-----------------------------*/


    private String name;
    private String index;

    /**
     * 构造方法
     * @param name
     * @param index
     */
    ExceptionFlowEnum(String name, String index) {
        this.name = name;
        this.index = index;
    }

    /**
     * 普通方法
     * @param index
     * @return
     */
    public static String getName(String index) {
        for (ExceptionFlowEnum exceptionFlowEnum : ExceptionFlowEnum.values()) {
            if (index.equals(exceptionFlowEnum.getIndex())) {
                return exceptionFlowEnum.getName();
            }
        }
        return "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
