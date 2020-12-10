package com.zres.project.localnet.portal.collect.enums;

/**
 * @ClassName ProvinceEnum
 * @Description TODO
 * @Author wang.g2
 * @Date 2020/5/21 11:09
 */
public enum ProvinceEnum {

        HN("海南", "HN","/","SEQ_HN"),
        XJ("新疆", "XJ","/JTOSS-TO-XJ-DATA","SEQ_XJ"),
        CQ("重庆", "CQ","/JTOSS-TO-CQ-DATA","SEQ_CQ"),
        QH("青海", "QH","/JTOSS-TO-QH-DATA","SEQ_QH"),
        NX("宁夏", "NX","/JTOSS-TO-NX-DATA","SEQ_NX"),
        GS("甘肃", "GS","/JTOSS-TO-GS-DATA","SEQ_GS"),
        XZ("西藏", "XZ","/JTOSS-TO-XZ-DATA","SEQ_XZ"),
        HUB("湖北", "HUB","/JTOSS-TO-HUB-DATA","SEQ_HUB"),
        HUN("湖南", "HUN","/JTOSS-TO-HUN-DATA","SEQ_HUN"),
        SX("陕西", "SX","/JTOSS-TO-SX-DATA","SEQ_SX"),
        JS("江苏", "JS","/JTOSS-TO-JS-DATA","SEQ_JS"),
        HLJ("黑龙江", "HLJ","/JTOSS-TO-HLJ-DATA","SEQ_HLJ"),
        AH("安徽", "AH","/JTOSS-TO-AH-DATA","SEQ_AH"),
        JX("江西", "JX","/JTOSS-TO-JX-DATA","SEQ_JX"),
        YN("云南", "YN","/JTOSS-TO-YN-DATA","SEQ_YN"),
        GX("广西", "GX","/JTOSS-TO-GX-DATA","SEQ_GX"),
        ZJ("浙江", "ZJ","/JTOSS-TO-ZJ-DATA","SEQ_ZJ"),
        SXI("山西", "SXI","/JTOSS-TO-SXI-DATA","SEQ_SXI"),
        NM("内蒙", "NM","/JTOSS-TO-NM-DATA","SEQ_NM"),
        JL("吉林", "JL","/JTOSS-TO-JL-DATA","SEQ_JL"),
        SD("山东", "SD","/JTOSS-TO-SD-DATA","SEQ_SD"),
        SH("上海", "SH","/JTOSS-TO-SH-DATA","SEQ_SH"),
        HEN("河南", "HEN","/JTOSS-TO-HEN-DATA","SEQ_HEN"),
        TJ("天津", "TJ","/JTOSS-TO-TJ-DATA","SEQ_TJ"),
        GD("广东", "GD","/JTOSS-TO-GD-DATA","SEQ_GD"),
        GZ("贵州", "GZ","/JTOSS-TO-GZ-DATA","SEQ_GZ"),
        FJ("福建", "FJ","/JTOSS-TO-FJ-DATA","SEQ_FJ"),
        SC("四川", "SC","/JTOSS-TO-SC-DATA","SEQ_SC"),
        HEB("河北", "HEB","/JTOSS-TO-HEB-DATA","SEQ_HEB"),
        LN("辽宁", "LN","/JTOSS-TO-LN-DATA","SEQ_LN"),
        BJ("北京", "BJ","/JTOSS-TO-BJ-DATA","SEQ_BJ");


        private String name ;
        private String code ;
        private String path ;
        private String sqlSign ;

        private ProvinceEnum(String name , String code,String path,String sqlSign){
            this.name = name ;
            this.code = code ;
            this.path = path ;
            this.sqlSign = sqlSign ;
        }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getCode() {
            return code;
        }
        public void setCode(String code) {
            this.code = code;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getSqlSign() {
            return sqlSign;
        }

        public void setSqlSign(String sqlSign) {
            this.sqlSign = sqlSign;
        }
}
