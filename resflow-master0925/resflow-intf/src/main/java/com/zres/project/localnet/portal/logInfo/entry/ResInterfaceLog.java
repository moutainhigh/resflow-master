package com.zres.project.localnet.portal.logInfo.entry;

import java.util.Date;

public class ResInterfaceLog extends LoggerObj{

    private String interfName;
    private String url;
    private String content;
    private Date createDate;
    private String returnContent;
    private String orderNo;
    private String remark;
    private Date updateDate;

    public ResInterfaceLog(){}

    public ResInterfaceLog(int id, String interfName, String url, String content, Date createDate, String returnContent, String orderNo, String remark, Date updateDate) {
        super(id);
        this.interfName = interfName;
        this.url = url;
        this.content = content;
        this.createDate = createDate;
        this.returnContent = returnContent;
        this.orderNo = orderNo;
        this.remark = remark;
        this.updateDate = updateDate;
    }

    public String getInterfName() {
        return interfName;
    }

    public void setInterfName(String interfName) {
        this.interfName = interfName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getReturnContent() {
        return returnContent;
    }

    public void setReturnContent(String returnContent) {
        this.returnContent = returnContent;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
