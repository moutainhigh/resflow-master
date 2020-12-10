define(function () {
    return {
        /**
         * 事务单类型
         */
        AFFAIR_ORDER_TYPE:{
            GENERAL: 'GENERAL',              //通用
            EXCHANGE: 'EXCHANGE',            //交换
            DATA: 'DATA',                    //数据
            TRANSMISSION: 'TRANSMISSION',    //传输
            POWER: 'POWER'                   //动力
        },

        /**
         * 事务调单状态
         */
        AFFAIR_ORDER_STATE: {
            CGX: '290000112',   //草稿箱
            FQSW: '290000113',  //发起事务
            SHBH: '290000114',  //事务审核驳回
            SWSH: '290000115',  //事务审核
            SWCLZ: '290000116', //事务处理中
            SWCLBH: '290000117',//事务处理驳回
            SWYCL: '290000118', //事务已处理
            SWQR: '290000119',  //事务确认
            YWC: '290000120',    //已完成
            YGB: '290000121'    //已关闭
        },
        /**审核结果 */
        AFFAIR_CHECK_STATE: {
            /**通过*/
            PASS: '0',
            /**驳回*/
            NO_PASS: '1'
        },

        /**
         * 登陆账号
         * @returns {*}
         */
        queryStaffInfo: function () {
            return ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf", "queryStaffInfo");
        },

        /**
         * 获取调单编号
         * @returns {*}
         */
        getCode: function (param, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.until.service.UntilServiceIntf", "getCode",
                param, success);
        },

        /**
         * 查询事务调单列表
         * @param params
         * @param success
         * @returns {*}
         */
        queryAffairOrderList: function (params, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "queryAffairOrderList",
                params, success);
        },
        /**
         * 保存事务调单
         * @param map
         */
        saveAffairDispatchOrder: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "initAffairDispatchOrder",
                map, success);
        },
        /**
         * 事务调单审核
         * @param map
         */
        affairDispatchOrderCheck: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "affairDispatchOrderCheck",
                map, success);
        },
        /**
         * 事务调单审查
         * @param map
         */
        affairDispatchOrderReview: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "affairDispatchOrderReview",
                map, success);
        },
        /**
         * 事务调单处理
         * @param map
         */
        affairProcessComplateWo: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "affairProcessComplateWo",
                map, success);
        },
        /**
         * 事务调单确认退单
         * @param map
         */
        affairProcessRollBackWo: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "affairProcessRollBackWo",
                map, success);
        },
        /**
         * 事务调单确认回单
         * @param map
         */
        affairAffirmComplateWo: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "affairAffirmComplateWo",
                map, success);
        },
        /**
         * 获取事务调单处理进度列表
         * @param map
         */
        getAffairDisposeList: function (affairId, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "getAffairDisposeList",
                affairId, success);
        },
        /**
         * 获取事务调单子单处理进度
         * @param map
         */
        getChildAffairDisposeList: function (orderId, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "getChildAffairDisposeList",
                orderId, success);
        },
        /**
         * 获取事务调单审核信息
         * @param map
         */
        getAffairCheckInfoArray: function (affairId, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "getAffairCheckInfoArray",
                affairId, success);
        },
        /**
         * 获取调单附件
         */
        getAffairDispatchFile: function (affairId, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "getAffairDispatchFile",
                affairId, success);
        },
        /**
         * 获取工单附件
         */
        getAffairWoAccessoryFile: function (woId, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "getAffairWoAccessoryFile",
                woId, success);
        },

        /**
         * 获取调单抄送人
         */
        getAffairNoticeStaffArray: function (affairId, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "getAffairNoticeStaffArray",
                affairId, success);
        },

        /**
         * 文件下载
         * */
        downLoadAttachMent: function (_url, params) {
            ngc.downLoad(_url, params);
        },

        /**
         * 查询各类事务单数量
         * @param map
         * @param success
         */
        countVariousAffairOrder: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "countVariousAffairOrder",
                map, success);
        },
        /**
         * 查询审核驳回待处理数量
         * @param map
         * @param success
         */
        queryShRejectNum: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "queryShRejectNum",
                map, success);
        },
        /**
         * 查询事务处理被驳回待处理数量
         * @param map
         * @param success
         */
        queryClRejectNum: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "queryClRejectNum",
                map, success);
        },
        /**
         * 导出excel文件
         * @param map
         * @param success
         */
        exportAffairOrderData: function (_url, params) {
            ngc.downLoad(_url, params);
        },

        /**
         * 关闭事务调单
         * @param map
         * @param success
         */
        closeAffair: function (map, success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "closeAffair",
                map, success);
        },
        /**
         * 查询事务单类型
         * @param map
         * @param success
         * @returns {*}
         */
        qryAffairDispatchOrderType: function (success) {
            return ngc.callServerFunction("com.zres.project.localnet.portal.affairdispatch.service.DispatchOrderManageIntf", "qryAffairDispatchOrderType",
                success);
        }
    }
})