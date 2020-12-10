package com.zres.project.localnet.portal.local.domain;

import java.io.Serializable;

/**
 * 业务查询每个tab数量
 */
public class UnicomLocalCount implements Serializable {

    private static final long serialVersionUID = 5163298586122147298L;

    private int draftOrderCount;
    private int allOrderCount;
    private int completeOrderCount;
    private int submitedOrderCount;
    private String message;

    public int getDraftOrderCount() {
        return draftOrderCount;
    }

    public void setDraftOrderCount(int draftOrderCount) {
        this.draftOrderCount = draftOrderCount;
    }

    public int getAllOrderCount() {
        return allOrderCount;
    }

    public void setAllOrderCount(int allOrderCount) {
        this.allOrderCount = allOrderCount;
    }

    public int getCompleteOrderCount() {
        return completeOrderCount;
    }

    public void setCompleteOrderCount(int completeOrderCount) {
        this.completeOrderCount = completeOrderCount;
    }

    public int getSubmitedOrderCount() {
        return submitedOrderCount;
    }

    public void setSubmitedOrderCount(int submitedOrderCount) {
        this.submitedOrderCount = submitedOrderCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

}
