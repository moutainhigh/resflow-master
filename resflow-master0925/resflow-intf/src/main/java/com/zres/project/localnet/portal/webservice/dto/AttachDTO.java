package com.zres.project.localnet.portal.webservice.dto;

/**
 * @Description:附件信息DTO
 * @Author:zhang.kaigang
 * @Date:2019/5/15 18:04
 * @Version:1.0
 */
public class AttachDTO {

    @FieldMeta(column = "FILE_PATH", name = "附件路径")
    private String path;

    @FieldMeta(column = "FILE_NAME", name = "附件文件名称")
    private String name;

    // TODO 需要处理枚举值
    @FieldMeta(column = "FILE_TYPE", name = "附件文件类型")
    private String type;

    // TODO 需要确认原名是哪个字段
    @FieldMeta(column = "", name = "附件文件原名")
    private String value;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
