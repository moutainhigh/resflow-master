package com.zres.project.localnet.portal.order.domain;

import com.zres.project.localnet.portal.local.domain.PageInfo;

import java.io.Serializable;
import java.util.List;

/**
 * @author sunlb
 * @since 2018/12/24
 * 工单查询Vo
 */
public class GomDispatcherOrderVo implements Serializable {

    private static final long serialVersionUID = 8690850105275164753L;

    List<GomDispatcherOrderPo> gomDispatcherOrderPoList;

    private String message;

    private PageInfo pageInfo;

    public List<GomDispatcherOrderPo> getGomDispatcherOrderPoList() {
        return gomDispatcherOrderPoList;
    }

    public void setGomDispatcherOrderPoList(List<GomDispatcherOrderPo> gomDispatcherOrderPoList) {
        this.gomDispatcherOrderPoList = gomDispatcherOrderPoList;
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
