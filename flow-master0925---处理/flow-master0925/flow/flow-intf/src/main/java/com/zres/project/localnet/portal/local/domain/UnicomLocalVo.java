package com.zres.project.localnet.portal.local.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 业务申请单VO
 */
public class UnicomLocalVo implements Serializable {

    private static final long serialVersionUID = 8107027583452709043L;

    private List<UnicomLocalOrderPo> unicomVoList;

    private String message;

    private PageInfo pageInfo;

    public List<UnicomLocalOrderPo> getUnicomVoList() {
        return unicomVoList;
    }

    public void setUnicomVoList(List<UnicomLocalOrderPo> unicomVoList) {
        this.unicomVoList = unicomVoList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
}
