package com.zres.project.localnet.portal.flowdealinfo.data.util;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BasicCodeEnum {
    /**
     *  环节id uos_tache.id
     */
    DATA_CHECK("500001146","数据专业核查"),
    OUTSIDElINE_CHECK("500001145","外线专业核查"),
    TRANS_CHECK("500001147","传输专业核查"),
    ACCESS_CHECK("500001148","接入专业核查"),
    OTHER_CHECK("500001149","其他专业核查"),
    CHECK_DISPATCH("500001144","核查调度"),

    /**
     * 岗位id  uos_post.post_id
     */
    OUTSIDElINE_POST_ID("101","外线专业"),
    DATA_POST_ID("102","数据专业"),
    TRANS_POST_ID("103","传输专业"),
    ACCESS_POST_ID("104","接入专业"),
    OTHER_POST_ID("105","其他专业"),

    /**
     * 派发对象类型
     */
    DISP_TYPE_JOB_ORG("260000001","组织"),
    DISP_TYPE_JOB_ROLE("260000002","角色"),
    DISP_TYPE_JOB_PER("260000003","人员"),
    DISP_TYPE_JOB_VP("260000004","虚拟职位"),


    NEW_OPEN("101","新开"),
    UNPACK("102","拆机"),
    ALTERATION("103","变更"),
    CLOSE_DOWN("104","停机"),
    REPLY("105","复机"),
    MOVE_MACHINE("106","移机");

    private String value;
    private String name;

    BasicCodeEnum(String value, String name){
        this.value = value;
        this.name = name;
    }
    /**
     * 通过value获取指定的enum
     * @param value
     * @return
     */
    public static BasicCodeEnum getEnumByCode(String value) throws Exception {
        for(BasicCodeEnum t: BasicCodeEnum.values()){
            if(t.getValue().equals(value)){
                return t;
            }
        }
        return null;
    }
    /**
     * 通过name获取指定的enum
     * @param name
     * @return
     */
    public static BasicCodeEnum getEnumByName(String name) {
        BasicCodeEnum[] values = BasicCodeEnum.values();
        for(BasicCodeEnum value : values){
            if(value.getName().equals(name)){
                return value;
            }
        }
        return null;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "BasicCodeEnum{" +
                "value='" + value + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

}
