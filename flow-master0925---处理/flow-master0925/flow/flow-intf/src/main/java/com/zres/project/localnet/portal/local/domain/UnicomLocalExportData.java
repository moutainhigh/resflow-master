package com.zres.project.localnet.portal.local.domain;

import java.io.Serializable;
import java.util.Date;

public class UnicomLocalExportData implements Serializable {


    private static final long serialVersionUID = 2706930253789000305L;

    private String custName;
    private String applyOrdId;
    private String applyOrdName;
    private String dianlNo;
    private String itemTypeName;
    private String prodBustTypeName;
    private String actCodeName;
    private String actTypeState;
    private String dispObjName;
    private Date createDateStr;

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getApplyOrdId() {
        return applyOrdId;
    }

    public void setApplyOrdId(String applyOrdId) {
        this.applyOrdId = applyOrdId;
    }

    public String getApplyOrdName() {
        return applyOrdName;
    }

    public void setApplyOrdName(String applyOrdName) {
        this.applyOrdName = applyOrdName;
    }

    public String getItemTypeName() {
        return itemTypeName;
    }

    public void setItemTypeName(String itemTypeName) {
        this.itemTypeName = itemTypeName;
    }

    public String getProdBustTypeName() {
        return prodBustTypeName;
    }

    public void setProdBustTypeName(String prodBustTypeName) {
        this.prodBustTypeName = prodBustTypeName;
    }

    public String getActCodeName() {
        return actCodeName;
    }

    public void setActCodeName(String actCodeName) {
        this.actCodeName = actCodeName;
    }

    public String getDispObjName() {
        return dispObjName;
    }

    public void setDispObjName(String dispObjName) {
        this.dispObjName = dispObjName;
    }

    public Date getCreateDateStr() {
        return createDateStr;
    }

    public void setCreateDateStr(Date createDateStr) {
        this.createDateStr = createDateStr;
    }

    public String getDianlNo() {
        return dianlNo;
    }

    public void setDianlNo(String dianlNo) {
        this.dianlNo = dianlNo;
    }

    public String getActTypeState() {
        return actTypeState;
    }

    public void setActTypeState(String actTypeState) {
        this.actTypeState = actTypeState;
    }

}
