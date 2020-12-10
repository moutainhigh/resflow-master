package com.zres.project.localnet.portal.flowdealinfo.service.entry;

import java.util.Map;

public class TacheWoOrder {

    private int tacheId;
    private String tacheCode;
    private String tacheName;
    private Map tacheInfo;

    public TacheWoOrder(int tacheId, String tacheCode, String tacheName, Map tacheInfo) {
        this.tacheId = tacheId;
        this.tacheCode = tacheCode;
        this.tacheName = tacheName;
        this.tacheInfo = tacheInfo;
    }

    public int getTacheId() {
        return tacheId;
    }

    public void setTacheId(int tacheId) {
        this.tacheId = tacheId;
    }

    public String getTacheCode() {
        return tacheCode;
    }

    public void setTacheCode(String tacheCode) {
        this.tacheCode = tacheCode;
    }

    public String getTacheName() {
        return tacheName;
    }

    public void setTacheName(String tacheName) {
        this.tacheName = tacheName;
    }

    public Map getTacheInfo() {
        return tacheInfo;
    }

    public void setTacheInfo(Map tacheInfo) {
        this.tacheInfo = tacheInfo;
    }

    enum TacheCode {

        SPECIALTY_RESOURCE_SUPPLEMENT_LOCAL("specialtyResourceSuppementTache"), //专业资源补录
        ALL_SPECIALTY_RESOURCE_SUPPLEMENT_LOCAL("allSpecialtyResourceSuppementTache");//各专业资源补录

        private final String value;

        TacheCode(String value){
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

    public String getBeanNameByTacheCode(){
        String tacheCode = this.tacheCode;
        for(TacheCode t : TacheCode.values()){
            if(tacheCode!=null && tacheCode.trim().equals(t.name())){
                String str = t.getValue();
                return str;
            }
        }
        return "commonTache";
    }
    public String getBeanNameByTacheCodeWithOperAttr(){
        String tacheCode = this.tacheCode;
        for(TacheCode t : TacheCode.values()){
            if(tacheCode!=null && tacheCode.trim().equals(t.name())){
                String str = t.getValue();
                return str;
            }
        }
        return "commonWithOperAttrTache";
    }

}
