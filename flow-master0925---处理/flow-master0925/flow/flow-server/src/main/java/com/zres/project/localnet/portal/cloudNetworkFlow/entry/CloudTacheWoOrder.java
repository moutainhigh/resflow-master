package com.zres.project.localnet.portal.cloudNetworkFlow.entry;

import java.util.Map;

import org.springframework.util.StringUtils;

import com.ztesoft.res.frame.flow.xpdl.model.TacheDto;

/**
 * 页面提交回单环节相关处理
 */
public class CloudTacheWoOrder {

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
    public CloudTacheWoOrder(TacheDto tacheDto, Map tacheInfo) {
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
        YZW_L_CIRCUIT_DISPATCH("circuitDispatchTache"), //电路调度
        YZW_L_NEW_RES_ENTRY("newResourceTache"), //新建资源录入
        YZW_A_MCPE_INSTALL("mcpeInstallTestTache"), //A端MCPE安装测试
        YZW_Z_MCPE_INSTALL("mcpeInstallTestTache"), //Z端MCPE安装测试
        YZW_A_LOCAL_TEST("localTestTache"), //A端本地测试报竣
        YZW_Z_LOCAL_TEST("localTestTache"), //Z端本地测试报竣

        YZW_L_MCPE_INSTALL("mcpeInstallTestTache"), //MCPE安装测试
        YZW_L_WHOLE_TEST("wholeTestTache"), //全程测试报竣

        //跨域业务
        YZW_C_CIRCUIT_DISPATCH("circuitDispatchTache"), //电路调度
        YZW_C_MCPE_INSTALL("mcpeInstallTestTache"), //MCPE安装测试
        YZW_C_LOCAL_TEST("localTestCrossTache"), //本地测试报竣
        YZW_C_WHOLE_TEST("wholeTestCrossTache"), //全程测试报竣

        //移机业务MCPE业务配置处理子流程
        YZW_SINGLE_MCPE_CONFIG("singleMcpeConfigTache"), //单端MCPE业务配置

        YZW_OUTSIDE_CONSTRUCT("constructTache"), //外线施工
        YZW_RES_CONSTRUCT("constructTache"), //资源施工


        YZW_CHECK_DISPATCH("checkDispatchTache"),//核查调度
        YZW_RES_CHECK_IPRAN("cloudNetWorkChildFlowCheckTache"),//IPRAN核查环节
        YZW_FIBER_CHECK("cloudNetWorkChildFlowCheckTache"),//光纤资源核查环节
        YZW_MCPE_CHECK("cloudNetWorkChildFlowCheckTache"), //MCPE终端盒核查环节
        YZW_CHECK_TOTAL("verificationSumTache"),//核查汇总环节
        YZW_INVESTMENT_ESTIMATION("investmentEstimationTache"),// 投资估算
        tache_code("beanTache");


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
        //otn环节处理的通用bean为commonCloudServiceTache
        String str = "commonCloudServiceTache";
        for (TacheCode t : TacheCode.values()) {
            if (tacheCode != null && tacheCode.trim().equals(t.name())) {
                if (!StringUtils.isEmpty(t.getValue())) {
                    str = t.getValue();
                }
            }
        }
        return str;
    }

}
