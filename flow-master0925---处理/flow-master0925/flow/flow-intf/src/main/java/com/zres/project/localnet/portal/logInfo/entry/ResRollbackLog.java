package com.zres.project.localnet.portal.logInfo.entry;

public class ResRollbackLog extends LoggerObj {

    private String srvOrdId;
    private String orderId;
    private String interfaceName;
    private String flag;

    public ResRollbackLog() {
    }

    public ResRollbackLog(int id,String srvOrdId,String flag,String orderId,String interfaceName) {
        super(id);
        this.srvOrdId = srvOrdId;
        this.flag = flag;
        this.orderId = orderId;
        this.interfaceName = interfaceName;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getSrvOrdId() {
        return srvOrdId;
    }

    public void setSrvOrdId(String srvOrdId) {
        this.srvOrdId = srvOrdId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }


}
