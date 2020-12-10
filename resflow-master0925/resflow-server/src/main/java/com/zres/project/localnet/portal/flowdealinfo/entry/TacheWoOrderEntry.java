package com.zres.project.localnet.portal.flowdealinfo.entry;

public class TacheWoOrderEntry {

    private int tacheId;
    private String tacheCode;
    private String tacheName;
    //private String tacheDispObj;


    public TacheWoOrderEntry() {
    }

    public TacheWoOrderEntry(int tacheId, String tacheCode, String tacheName) {
        this.tacheId = tacheId;
        this.tacheCode = tacheCode;
        this.tacheName = tacheName;
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

    enum Tache {

        //定义枚举类
        LOCAL_TEST("LOCAL_TEST") {
            @Override
            public String getMethodName(String tacheCode) {
                return "mainDispService";
            }
        },
        WHOLE_COURSE_TEST("WHOLE_COURSE_TEST") {
            @Override
            public String getMethodName(String tacheCode) {
                return "mainDispService";
            }
        },
        DATA_MAKE("DATA_MAKE") {
            @Override
            public String getMethodName(String tacheCode) {
                return "dataMakeDispService";
            }
        },
        OUTSIDE_CONSTRUCT("OUTSIDE_CONSTRUCT") {
            @Override
            public String getMethodName(String tacheCode) {
                return "resourceConstructionDispService";
            }
        },
        RES_CONSTRUCT("RES_CONSTRUCT") {
            @Override
            public String getMethodName(String tacheCode) {
                return "resourceConstructionDispService";
            }
        },
        UNION_DEBUG_TEST("UNION_DEBUG_TEST") {
            @Override
            public String getMethodName(String tacheCode) {
                return "adjustTestDispService";
            }
        },
        INTER_PROVINCIAL_COMMISSIONING("INTER_PROVINCIAL_COMMISSIONING") {
            @Override
            public String getMethodName(String tacheCode) {
                return "fullCommissioningDispService";
            }
        },
        FULL_COMMISSIONING("FULL_COMMISSIONING") {
            @Override
            public String getMethodName(String tacheCode) {
                return "provincialCommissioningDispService";
            }
        };

        //枚举属性
        private final String tacheCode;

        Tache(String tacheCode) {
            this.tacheCode = tacheCode;
        }

        public String getTacheCode() {
            return tacheCode;
        }

        public abstract String getMethodName(String tacheCode);
    }

    public String getMethodNameByTacheCode(){
        String tacheCode = this.tacheCode;
        //循环枚举类，其实是循环定义在枚举类中的枚举
        for(Tache t : Tache.values()){
            if(tacheCode!=null && tacheCode.trim().equals(t.getTacheCode())){
                String str = t.getMethodName(tacheCode);
                return str;
            }
        }
        return "没有配置该枚举值。。";

    }

}
