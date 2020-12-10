package com.zres.project.localnet.portal.cloudNetworkFlow.entry;

import java.util.Map;

import org.springframework.util.StringUtils;

import com.ztesoft.res.frame.flow.xpdl.model.TacheDto;

/**
 * 云网平台反馈接口调用配置
 */
public class CloudFeedbackIntfWoOrder {

    private int tacheId;
    private String tacheCode;
    private String tacheName;
    private Map tacheInfo;

    public CloudFeedbackIntfWoOrder(TacheDto tacheDto, Map tacheInfo) {
        this.tacheId = Integer.parseInt(tacheDto.getTacheId());
        this.tacheCode = tacheDto.getTacheCode();
        this.tacheName = tacheDto.getTacheName();
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

        //本地业务
        YZW_A_MCPE_INSTALL("mcpeInstallTestTache"), //A端MCPE安装测试
        YZW_Z_MCPE_INSTALL("mcpeInstallTestTache"), //Z端MCPE安装测试
        YZW_L_MCPE_CONFIG("mcpeConfigTache"), //MCPE业务配置
        YZW_UPEQUIP_BUSICONFIG("upequipBusiConfigTache"), //上联设备业务配置处理--子流程

        //本地移机
        YZW_L_MCPE_INSTALL("mcpeInstallTestTache"), //MCPE安装测试

        //跨域新开
        YZW_C_MCPE_INSTALL("mcpeInstallTestTache"), //MCPE安装测试
        YZW_C_MCPE_CONFIG("mcpeConfigTache"), //MCPE业务配置
        YZW_C_UPLINK_CONFIG("upequipBusiConfigTache"), //上联设备业务配置
        YZW_C_WHOLE_TEST("wholeTestCrossTache"), //全程测试报竣

        //移机业务MCPE业务配置处理子流程
        YZW_SINGLE_MCPE_CONFIG("singleMcpeConfigTache"), //单端MCPE业务配置
        YZW_SINGLE_MCPE_OFFLINE("singleMcpeOfflineTache"), //单端MCPE终端盒下线
        YZW_UPLINK_DATA_DETELE("uplinkDataDeteleTache"), //上联设备数据删除
        //YZW_UPLINK_CONFIG("upequipBusiConfigTache"), //上联设备业务配置
        tache_code("beanTache");


        private final String value;

        TacheCode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public String getBeanNameByTacheCode() throws Exception {
        String tacheCode = this.tacheCode;
        //otn环节处理的通用bean为commonCloudServiceTache
        String str = "";
        for (TacheCode t : TacheCode.values()) {
            if (tacheCode != null && tacheCode.trim().equals(t.name())) {
                if (!StringUtils.isEmpty(t.getValue())) {
                    str = t.getValue();
                }
            }
        }
        if (StringUtils.isEmpty(str)){
            throw new Exception("没有找到配置的对应环节接口的bean...");
        }
        return str;
    }

}
