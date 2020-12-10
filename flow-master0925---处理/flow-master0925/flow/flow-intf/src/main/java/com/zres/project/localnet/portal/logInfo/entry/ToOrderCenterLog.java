package com.zres.project.localnet.portal.logInfo.entry;

import java.util.Date;
import java.util.Map;

public class ToOrderCenterLog extends LoggerObj {

    private Map<String, Object> params;

    private Long toOrderCenterLogId; //ID
    private Long circuitId; //电路id  --srvordid
    private int createBy; //创建人id
    private Date createTime; //创建时间
    private String crmProdInstId; //电路明细编号  --业务号码
    private String custOrderNo; //大客户流水号   --申请单编号
    private String dataSources; //数据来源  --TWO_DRY二干   LOCAL_NET本地
    private int isDel; //是否删除（0否1是）
    private int isMerge; //是否合并（0否1是）
    private String operationRemark; //操作备注
    private String operationSegment; //操作环节
    private String operator; //操作人
    private Long sysAreaId; //省份ID
    private int updateBy; //更新人
    private Date updateTime; //更新时间
    private String operate; //操作
    private int timeOut; //是否超时  0:是；1：否

    public ToOrderCenterLog() {}

    public ToOrderCenterLog(Long toOrderCenterLogId, Long circuitId, int createBy, Date createTime, String crmProdInstId, String custOrderNo, String dataSources, int isDel, int isMerge, String operationRemark, String operationSegment, String operator, Long sysAreaId, int updateBy, Date updateTime, String operate, int timeOut) {
        this.toOrderCenterLogId = toOrderCenterLogId;
        this.circuitId = circuitId;
        this.createBy = createBy;
        this.createTime = createTime;
        this.crmProdInstId = crmProdInstId;
        this.custOrderNo = custOrderNo;
        this.dataSources = dataSources;
        this.isDel = isDel;
        this.isMerge = isMerge;
        this.operationRemark = operationRemark;
        this.operationSegment = operationSegment;
        this.operator = operator;
        this.sysAreaId = sysAreaId;
        this.updateBy = updateBy;
        this.updateTime = updateTime;
        this.operate = operate;
        this.timeOut = timeOut;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Long getToOrderCenterLogId() {
        return toOrderCenterLogId;
    }

    public void setToOrderCenterLogId(Long toOrderCenterLogId) {
        this.toOrderCenterLogId = toOrderCenterLogId;
    }

    public Long getCircuitId() {
        return circuitId;
    }

    public void setCircuitId(Long circuitId) {
        this.circuitId = circuitId;
    }

    public int getCreateBy() {
        return createBy;
    }

    public void setCreateBy(int createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCrmProdInstId() {
        return crmProdInstId;
    }

    public void setCrmProdInstId(String crmProdInstId) {
        this.crmProdInstId = crmProdInstId;
    }

    public String getCustOrderNo() {
        return custOrderNo;
    }

    public void setCustOrderNo(String custOrderNo) {
        this.custOrderNo = custOrderNo;
    }

    public String getDataSources() {
        return dataSources;
    }

    public void setDataSources(String dataSources) {
        this.dataSources = dataSources;
    }

    public int getIsDel() {
        return isDel;
    }

    public void setIsDel(int isDel) {
        this.isDel = isDel;
    }

    public int getIsMerge() {
        return isMerge;
    }

    public void setIsMerge(int isMerge) {
        this.isMerge = isMerge;
    }

    public String getOperationRemark() {
        return operationRemark;
    }

    public void setOperationRemark(String operationRemark) {
        this.operationRemark = operationRemark;
    }

    public String getOperationSegment() {
        return operationSegment;
    }

    public void setOperationSegment(String operationSegment) {
        this.operationSegment = operationSegment;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Long getSysAreaId() {
        return sysAreaId;
    }

    public void setSysAreaId(Long sysAreaId) {
        this.sysAreaId = sysAreaId;
    }

    public int getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(int updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }
}
