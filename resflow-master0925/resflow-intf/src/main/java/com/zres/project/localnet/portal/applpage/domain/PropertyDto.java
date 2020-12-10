package com.zres.project.localnet.portal.applpage.domain;

/**
 * @author :ren.jiahang
 * @date:2019/1/1@time:11:11
 */
public class PropertyDto {
    private String SRV_ID;
    private String SRV_NAME;
    private String PROPERTY_ID;
    private String PROPERTY_NAME;
    private String LOCAL_CODE;

    public String getSRV_ID() {
        return SRV_ID;
    }

    public void setSRV_ID(String SRV_ID) {
        this.SRV_ID = SRV_ID;
    }

    public String getSRV_NAME() {
        return SRV_NAME;
    }

    public void setSRV_NAME(String SRV_NAME) {
        this.SRV_NAME = SRV_NAME;
    }

    public String getPROPERTY_ID() {
        return PROPERTY_ID;
    }

    public void setPROPERTY_ID(String PROPERTY_ID) {
        this.PROPERTY_ID = PROPERTY_ID;
    }

    public String getPROPERTY_NAME() {
        return PROPERTY_NAME;
    }

    public void setPROPERTY_NAME(String PROPERTY_NAME) {
        this.PROPERTY_NAME = PROPERTY_NAME;
    }

    public String getLOCAL_CODE() {
        return LOCAL_CODE;
    }

    public void setLOCAL_CODE(String LOCAL_CODE) {
        this.LOCAL_CODE = LOCAL_CODE;
    }
}
