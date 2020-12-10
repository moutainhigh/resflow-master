package com.zres.project.localnet.portal.util;

import java.util.List;

/**
 * @author chen.zhangchao
 */
public class PageInfo {
    /**
     * 当前页数
     */
    private int currentPage = 1;
    /**
     * 总页数
     */
    private int pageCount = 0;

    /**
     * 总记录数
     */
    private int rowCount = 0;
    /**
     * 每页的记录数
     */
    private int pageSize = 20;
    /**
     * 记录开始数
     */
    private int rowStart = 0;
    /**
     * 记录结束数
     */
    private int rowEnd = 0;


    private String pagingDisplay = "default";

    public static final String PAGING_DISPLAY_DEFAULT = "default";

    public static final String PAGING_DISPLAY_SIMPLE = "simple";

    private List showList;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * 获取总页数
     *
     * @return
     */
    public int getPageCount() {
        if (this.rowCount % this.pageSize == 0) {
            this.pageCount = this.rowCount / this.pageSize;
        }
        else {
            this.pageCount = this.rowCount / this.pageSize + 1;
        }
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getRowStart() {
        if ((this.currentPage - 1) * this.pageSize + 1 <= this.rowCount) {
            rowStart = (this.currentPage - 1) * this.pageSize;
        }
        else {
            rowStart = this.rowCount;
        }
        return rowStart;
    }

    public void setRowStart(int rowStart) {
        this.rowStart = rowStart;
    }

    public int getRowEnd() {
        if (this.currentPage * this.pageSize <= this.rowCount) {
            rowEnd = this.currentPage * this.pageSize;
        }
        else {
            rowEnd = this.rowCount;
        }
        return rowEnd;
    }

    public void setRowEnd(int rowEnd) {
        this.rowEnd = rowEnd;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public List getShowList() {
        return showList;
    }

    public void setShowList(List showList) {
        this.showList = showList;
    }

    public void setPagingDisplay(String pagingDisplay) {
        this.pagingDisplay = pagingDisplay;
    }

    public String getPagingDisplay() {
        return pagingDisplay;
    }
}