package com.zres.project.localnet.portal.local.domain;


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
    private int pageSize = 10;
    /**
     * 记录开始数
     */
    private int rowStart = 0;
    /**
     * 记录结束数
     */
    private int rowEnd = 0;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getRowStart() {
        return rowStart;
    }

    public void setRowStart(int rowStart) {
        this.rowStart = rowStart;
    }

    public int getRowEnd() {
        return rowEnd;
    }

    public void setRowEnd(int rowEnd) {
        this.rowEnd = rowEnd;
    }

    /**
     * @param pageIndexObj 页码
     * @param pageSizeObj 每页显示数量
     */
    public void setIndexSizeData(Object pageIndexObj, Object pageSizeObj){
        int pageIndex = 1;
        if(pageIndexObj!=null){
            if(pageIndexObj instanceof String){
                String pageIndexS = (String)pageIndexObj;
                pageIndex = Integer.parseInt(pageIndexS);
            }else{
                pageIndex = ((Integer) pageIndexObj).intValue();
            }
        }
        int pageSize = 10;
        if(pageSizeObj!=null){
            if(pageSizeObj instanceof String){
                String pageSizeObjS = (String)pageSizeObj;
                pageSize = Integer.parseInt(pageSizeObjS);
            }else{
                pageSize = ((Integer) pageSizeObj).intValue();
            }
        }
        int startRow = (pageIndex - 1) * pageSize;
        int endRow = pageIndex * pageSize;
        this.setCurrentPage(pageIndex);
        this.setPageSize(pageSize);
        this.setRowStart(startRow);
        this.setRowEnd(endRow);

    }

    /**
     * @param pageIndex 页码
     * @param pageSize 每页显示数量
     */
    public void setIndexSizeData(int pageIndex, int pageSize){
        int startRow = (pageIndex - 1) * pageSize;
        int endRow = pageIndex * pageSize;
        this.setCurrentPage(pageIndex);
        this.setPageSize(pageSize);
        this.setRowStart(startRow);
        this.setRowEnd(endRow);

    }

    public void setDataCount(int rowCount){
        this.setRowCount(rowCount);
        this.setPageCount(rowCount==0?0:(rowCount%this.getPageSize()==0?rowCount/this.getPageSize():(rowCount/this.getPageSize()+1)));
    }

}
