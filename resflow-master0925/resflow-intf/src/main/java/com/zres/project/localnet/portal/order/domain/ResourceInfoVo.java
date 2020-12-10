package com.zres.project.localnet.portal.order.domain;

import com.zres.project.localnet.portal.local.domain.PageInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author sunlb
 * @since 2018/12/24
 * 工单查询Vo
 */
public class ResourceInfoVo implements Serializable {

    private static final long serialVersionUID = 8690850105275164743L;

    List<Map<String, Object>> resorceInfoList;

    private String message;

    private PageInfo pageInfo;

    public List<Map<String, Object>> getResorceInfoList() {
        return resorceInfoList;
    }

    public void setResorceInfoList(List<Map<String, Object>> resorceInfoList) {
        this.resorceInfoList = resorceInfoList;
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
