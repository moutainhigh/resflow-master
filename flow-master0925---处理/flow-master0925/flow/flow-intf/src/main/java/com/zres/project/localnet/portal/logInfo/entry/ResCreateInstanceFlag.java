package com.zres.project.localnet.portal.logInfo.entry;

import java.util.Date;

public class ResCreateInstanceFlag extends LoggerObj{

    private String srvOrdId;
    private String attrAction;
    private String attrCode;
    private String attrName;
    private String attrValue;
    private String attrValueName;
    private Date createDate;
    private String sourse;

    public ResCreateInstanceFlag(){}

    public ResCreateInstanceFlag(int id, String srvOrdId, String attrAction, String attrCode, String attrName, String attrValue, String attrValueName, Date createDate, String sourse) {
        super(id);
        this.srvOrdId = srvOrdId;
        this.attrAction = attrAction;
        this.attrCode = attrCode;
        this.attrName = attrName;
        this.attrValue = attrValue;
        this.attrValueName = attrValueName;
        this.createDate = createDate;
        this.sourse = sourse;
    }

    public String getSrvOrdId() {
        return srvOrdId;
    }

    public void setSrvOrdId(String srvOrdId) {
        this.srvOrdId = srvOrdId;
    }

    public String getAttrAction() {
        return attrAction;
    }

    public void setAttrAction(String attrAction) {
        this.attrAction = attrAction;
    }

    public String getAttrCode() {
        return attrCode;
    }

    public void setAttrCode(String attrCode) {
        this.attrCode = attrCode;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getAttrValue() {
        return attrValue;
    }

    public void setAttrValue(String attrValue) {
        this.attrValue = attrValue;
    }

    public String getAttrValueName() {
        return attrValueName;
    }

    public void setAttrValueName(String attrValueName) {
        this.attrValueName = attrValueName;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getSourse() {
        return sourse;
    }

    public void setSourse(String sourse) {
        this.sourse = sourse;
    }
}
