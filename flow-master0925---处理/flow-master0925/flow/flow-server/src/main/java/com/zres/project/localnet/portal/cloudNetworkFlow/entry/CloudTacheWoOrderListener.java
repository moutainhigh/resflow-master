package com.zres.project.localnet.portal.cloudNetworkFlow.entry;

import com.ztesoft.res.frame.flow.xpdl.model.TacheDto;

import java.util.Map;

/**
 * 监听到单环节相关处理
 */
public class CloudTacheWoOrderListener {

    private int tacheId;
    private String tacheCode;
    private String tacheName;
    private Map tacheInfo;

    /*public CloudTacheWoOrder(int tacheId, String tacheCode, String tacheName, Map tacheInfo) {
        this.tacheId = tacheId;
        this.tacheCode = tacheCode;
        this.tacheName = tacheName;
        this.tacheInfo = tacheInfo;
    }*/
    public CloudTacheWoOrderListener(TacheDto tacheDto, Map tacheInfo) {
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
        YZW_A_CONSTRUCT_WAIT("constructWaitTache"), //A端网络施工等待
        YZW_Z_CONSTRUCT_WAIT("constructWaitTache"), //Z端网络施工等待
        YZW_CHILDFLOWWAIT("childFlowWaitTache"), //子流程等待
        YZW_A_MCPE_INSTALL("mcpeInstallTestTache"), //A端MCPE安装测试
        YZW_Z_MCPE_INSTALL("mcpeInstallTestTache"), //Z端MCPE安装测试
        YZW_A_PORT_CONFIG("portConfigTache"), //A端终端下联端口配置
        YZW_Z_PORT_CONFIG("portConfigTache"), //Z端终端下联端口配置
        YZW_A_LOCAL_TEST("localTestTache"), //A端本地测试报竣
        YZW_Z_LOCAL_TEST("localTestTache"), //Z端本地测试报竣

        YZW_L_UPLINK_CONFIG_WAIT("uplinkConfigWaitTache"), //上联设备业务配置
        YZW_UPEQUIP_BUSICONFIG("upequipBusiConfigTache"), //上联设备业务配置处理--子流程

        YZW_L_MCPE_CONFIG("mcpeConfigTache"), //MCPE业务配置
        YZW_L_WHOLE_TEST("wholeTestTache"), //全程测试报竣

        //移机
        YZW_L_CONSTRUCT_WAIT("constructWaitTache"), //网络施工等待
        YZW_L_MCPE_INSTALL("mcpeInstallTestTache"), //MCPE安装测试
        YZW_L_PORT_CONFIG("portConfigTache"), //终端下联端口配置
        YZW_L_LOCAL_TEST("localTestTache"), //本地测试报竣
        YZW_LY_UPLINK_CONFIG_WAIT("uplinkConfigWaitYTache"), //上联设备业务配置

        YZW_START_MCPE_CONFIG("startMcpeConfigTache"), //启动MCPE业务配置处理
        YZW_END_MCPE_CONFIG("endMcpeConfigTache"), //完成MCPE业务配置处理

        // 本地跨域业务变更-升降速
        YZW_RATE_UPLINK_CONFIG_WAIT("uplinkConfigWaitRateTache"), //上联设备业务配置 -变更升降速
        // 本地跨域业务变更-下联端口
        YZW_CHG_NEW_APPLICATION("newApplicationChangeTache"), //新建申请单 -变更下联端口
        YZW_LC_PORT_CONFIG("newApplicationChangeTache"), //终端下联端口配置

        //跨域业务
        YZW_C_CONSTRUCT_WAIT("constructWaitTache"), //网络施工等待
        YZW_C_MCPE_INSTALL("mcpeInstallTestTache"), //MCPE安装测试
        YZW_C_PORT_CONFIG("portConfigTache"), //终端下联端口配置
        YZW_C_LOCAL_TEST("localTestCrossTache"), //本地测试报竣
        YZW_C_UPLINK_CONFIG("upequipBusiConfigTache"), //上联设备业务配置
        YZW_C_WHOLE_TEST("wholeTestCrossTache"), //全程测试报竣

        YZW_C_MCPE_CONFIG("mcpeConfigTache"), //MCPE业务配置

        //移机业务MCPE业务配置处理子流程
        YZW_SINGLE_MCPE_CONFIG("singleMcpeConfigTache"), //单端MCPE业务配置
        YZW_SINGLE_MCPE_OFFLINE("singleMcpeOfflineTache"), //单端MCPE终端盒下线
        YZW_UPLINK_DATA_DETELE("uplinkDataDeteleTache"), //上联设备数据删除
        YZW_UPLINK_CONFIG("upequipBusiConfigTache"), //上联设备业务配置
        YZW_MCPE_CONFIG_FINISH("mcpeConfigFinishTache"), //完成MCPE业务配置处理

        //跨域移机配合端
        YZW_C_UPLINK_CONFIG_COOPERATE("upequipBusiConfigTache"), //上联设备业务配置
        YZW_C_PORT_CONFIG_COOPERATE("portConfigTache"), //终端下联端口配置
        YZW_C_MANUAL_CONFIG_COOPERATE("mcpeConfigTache"), //人工业务配置

        //跨域移机IPRAN释放
        YZW_C_MANUAL_CONFIG_IPRAN("ipranManualConfigTache"), //人工业务配置

        tache_code("beanTache"),
        YZW_CHECK_WAITING("checkChildFlowWaitTache"); //核查子流程等待环节

        private final String value;

        TacheCode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public String getBeanNameByTacheCode() {
        String tacheCode = this.tacheCode;
        for (TacheCode t : TacheCode.values()) {
            if (tacheCode != null && tacheCode.trim().equals(t.name())) {
                String str = t.getValue();
                return str;
            }
        }
        return "commonCloudServiceTache";
    }

}
