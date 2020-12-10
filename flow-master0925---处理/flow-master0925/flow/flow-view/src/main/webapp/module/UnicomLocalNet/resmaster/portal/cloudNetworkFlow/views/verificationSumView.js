define([
    'text!module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/templates/verificationSumView.html',
    'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/action/cloudNetWorkResCheckFlowAction',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/operOrderView.css'
], function (verificationSumView, cloudNetWorkResCheckFlowAction, operOrderAction, css) {
    return fish.View.extend({
        template: fish.compile(verificationSumView),
        events: {
            'click #submitBtn': 'submit',
            'click #saveBtn': 'saveCheckInfo'
        },

        initialize: function () {
            this.render();
            FILES = null; //附件
            userInfo = cloudNetWorkResCheckFlowAction.queryStaffInfo().responseJSON.data; //用户信息
            isSubmit = false;
            this.delFiles = new Array();
        },

        //渲染页面
        render: function () {
            this.$el.html(this.template({
                TITLE: this.options.title
            }));
        },

        //初始化fish组件
        afterRender: function () {
            //初始化电路信息
            this.initCircuitInfo();
            //初始化页面
            this.initComBoBox();
            //初始化附件表格
            this.initFileUpdate(this.options.woId + '');
        },
        initComBoBox: function(){
            $('#isResSatisfy').combobox({
                placeholder: '--请选择资源是否满足--',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '满足', value: '1'},
                    {name: '不满足', value: '0'}
                ]
            });
            $(".deviceType").combobox({
                placeholder: '--请选择设备类型--',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: 'IPRAN', value: 'IPRAN'},
                    {name: 'M3', value: 'M3'}
                ]
            });
        },
        initCircuitInfo: function () {
            var me = this;
            $("#cloudNetWorkResCheckGrid").grid({
                colModel: me.initColModel(),
                width: 516,
                shrinkToFit: false,
                multiselect: true,
                pageData: me.qrycircuitInfo(),
                onSelectRow: function (e, rowid, state, checked) {//选中行事件
                    me.initCheckInfoValue();
                }
            });
            $("#cloudNetWorkResCheckGrid").grid("setGridHeight", 150);

        },
        initCheckInfoValue: function(){
            var data = $("#cloudNetWorkResCheckGrid").grid("getSelection");
            if (data != null || data != undefined || data != {}){
                var param = {};
                param.srvOrdId = data.SRV_ORD_ID;
                param.woId = data.WO_ID;
                param.tacheId = data.TACHE_ID;
                cloudNetWorkResCheckFlowAction.queryCheckInfo(param, function(res){
                    if (res.success){
                        // $("#A_RES_SATISFY").combobox('value', res.data.A_RES_SATISFY)
                        // $("#Z_RES_SATISFY").combobox('value', res.data.Z_RES_SATISFY)
                        // $("#resDesc").val(res.data.A_OPTICALCABLE_TEXT);
                    }
                });
            }
        },
        initColModel: function(){
            return [
                {name: 'CHECK_STATE', label: '核查信息', width: 95, sortable: false,
                    formatter: function(cellval, opts, rwdat, _act) {
                        if(cellval == '已保存'){
                            return '<div class="btn-group">' +
                                '<button type="button" class="btn btn-link js-delete" style="color: #6DCC4A">'+ cellval +'</button>' +
                                '</div>';
                        }else{
                            return '<div class="btn-group btn btn-link" style="color: #FF5858">'+ cellval +
                                '</div>';
                        }
                    }
                },
                {name: 'CIRCUITCODE', label: '电路编号', width: 100, sortable: false },
                {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                {name: 'SERIAL_NUMBER', label: '业务号码', width: 100 , sortable: false },
                {name: 'AREGIONNAME', label: 'A端所属区域', width: 110, sortable: false},
                {name: 'AREGIONID', label: 'A端所属区域ID', width: 110, sortable: false, hidden: true },
                {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110, sortable: false},
                {name: 'ZREGIONID', label: 'Z端所属区域ID', width: 110, sortable: false, hidden: true },
                {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
            ]
        },
        /**
         * 查询电路信息
         */
        qrycircuitInfo: function () {
            var me = this;
            var param = {};
            param.cstOrdId = me.options.cstOrdId;
            param.tacheId = me.options.tacheId;
            param.userId = userInfo.userId;
            param.woState = me.options.woState;
            cloudNetWorkResCheckFlowAction.qrycircuitInfo(param, function(res){
                if (res.success){
                    $("#cloudNetWorkResCheckGrid").grid("reloadData", res.data);
                }else {
                    fish.toast('error', res.message);
                }
            })
            $(window).trigger("resize");
        },

        initFileUpdate: function (woId) {
            $("#fileGrid").grid({
                colModel: [
                    {name: 'attachInfoId', label: '附件ID', hidden: true},
                    {name: 'fileName', label: '文件名称', width: 160, sortable: false},
                    {name: 'fileSize', label: '大小', width: 40},
                    {name: 'fileType', label: '类型', width: 40, sortable: false},
                    {
                        name: 'action', label: '操作', width: 100, formatter: 'actions',
                        formatoptions: {
                            editbutton: false,
                            delbutton: function (rowdata) {
                                if (rowdata.attachInfoId) {
                                    me.delFiles.push(rowdata.attachInfoId);
                                }
                                return true;
                            }
                        }
                    }
                ],
                width: 516,
                create: function () {//控件初始化完成触发的事件
                    operOrderAction.getAnnex(woId, function (data) {
                        if (data.length != 0) {
                            for (i = 0; i < data.length; i++) {
                                var fileObj = {};
                                var map = data[i];
                                for (var k in map) {  //通过定义一个局部变量k遍历获取到了map中所有的key值
                                    var docList = map[k]; //获取到了key所对应的value的值！
                                    if (k == "ATTACH_INFO_ID") {
                                        fileObj.attachInfoId = docList;
                                    }
                                    if (k == "FILE_SIZE") {
                                        fileObj.fileSize = docList + "KB";
                                    }
                                    if (k == "FILE_TYPE") {
                                        fileObj.fileType = docList;
                                    }
                                    if (k == "FILE_NAME") {
                                        fileObj.fileName = docList.split(".")[0];
                                    }
                                }
                                $("#fileGrid").grid("addRowData", fileObj);
                            }
                        }
                    });
                }
            });
            var me = this;
            me.$('#selectFiles').fileupload({
                dataType: 'json',
                autoUpload: false,
                add: function (e, data) {
                    var fileObj = {};
                    var obj = data.files[0];
                    fileObj.fileName = obj.name.split(".")[0];
                    fileObj.fileSize = (obj.size / 1024.0).toFixed(2) + "KB";
                    fileObj.fileType = obj.name.split(".")[1];
                    if ((obj.size / 1024.0).toFixed(2) >= 20 * 1024) {
                        fish.warn('上传文件不能超过20M！');
                        return;
                    }
                    $("#fileGrid").grid("addRowData", fileObj);
                    if (FILES === null) {
                        FILES = data;
                    } else {
                        FILES.files.push(obj);
                    }
                    $('.inline-remove').off('click').on('click', function (e) {
                        var rowid = $(this).closest("tr.jqgrow").attr("id");
                        var data = $("#fileGrid").grid("getRowData", rowid)
                        $("#fileGrid").grid("delRowData", data);
                        var i;
                        $.each(FILES.files, function (index, file) {
                            if (file.name === data.fileName) {
                                i = index;
                            }
                        });
                        FILES.files.splice(i, 1);
                    });
                },
                always: function (e, data) {
                    isSubmit = false;
                    if (data.result.success) {
                        me.submitOrder(data.submitInfo); //提交工单
                        fish.toast('success', data.result.message);
                        me.popup.close();
                    } else {
                        $.unblockUI();
                        fish.toast('error', data.result.message);
                    }
                },
            });
        },
        /**
         * 提交
         */
        submit: function () {
            var me = this;
            var circuitData = $("#cloudNetWorkResCheckGrid").grid("getCheckRows");//订单信息
            if (circuitData != null && circuitData.length < 1){
                fish.info("请先选择要提交的电路！");
                return;
            }
            var params = {};
            params.circuitData = circuitData
            params.remark = $('#remark').val();
            params.delFiles = me.dealFileData();
            params.circuitData = circuitData;
            params.tacheId = me.options.tacheId;
            params.isInvestEst = $("input[name='investEst']:checked").val();

            $.blockUI({message: '派单中...'});
            if (FILES != null) { //如果附件非空先先上传附件
                me.fileUpdate(params);
            } else {
                me.submitOrder(params);
            }
        },

        //处理附件
        dealFileData: function () {
            var me = this;
            var delFiles = '';
            //处理附件
            var fileData = $("#fileGrid").grid("getRowData");
            var delFilesStr = "";
            for (var j = 0; j < me.delFiles.length; j++) {
                var temp = false;
                for (var i = 0; i < fileData.length; i++) {
                    if (me.delFiles[j] == fileData[i].attachInfoId) {
                        temp = true;
                        break;
                    }
                }
                if (!temp) {
                    delFilesStr += me.delFiles[j] + ",";
                }
            }
            if (delFilesStr) {
                delFiles = delFilesStr.substring(0, delFilesStr.length - 1);
            }
            if (FILES) {
                var vernier = 0;
                var fileLength = FILES.files.length;
                for (var index = 0; index < fileLength; index++) {
                    var file = FILES.files[index - vernier];
                    var isRemove = true;
                    for (var i = 0; i < fileData.length; i++) {
                        var data = fileData[i];
                        if (file.name == data.fileName + "." + data.fileType) {
                            isRemove = false;
                        }
                    }
                    if (isRemove) {
                        FILES.files.splice(index - vernier, 1);
                        vernier++;
                    }
                }
            }
            return delFiles;
        },

        //附件上传
        fileUpdate: function (params) {
            FILES.url = this.getRootPath() + "/localScheduleLT/FlieWoOrderDealController/uploadFiles.spr";
            FILES.formData = {
                params: JSON.stringify(params)
            }
            FILES.submitInfo = params;
            if (!isSubmit) {
                isSubmit = true;
                FILES.submit();
            } else {
                fish.toast('error', "请勿重复提交！");
            }
        },
        getRootPath: function () {
            //获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
            var curWwwPath = window.document.location.href;
            //获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
            var pathName = window.document.location.pathname;
            var pos = curWwwPath.indexOf(pathName);
            //获取主机地址，如： http://localhost:8083
            var localhostPaht = curWwwPath.substring(0, pos);
            //获取带"/"的项目名，如：/uimcardprj
            var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
            return (localhostPaht + projectName);
        },

        //提交工单
        submitOrder: function (params) {
            var me = this;
            if (!isSubmit) {
                isSubmit = true;
                cloudNetWorkResCheckFlowAction.submitWoOrder(params, function(res){
                    isSubmit = false;
                    $.unblockUI();
                    if (res.success) {
                        fish.toast('success', res.message);
                        me.popup.close();
                    } else {
                        fish.toast('error', res.message);
                    }
                });
            }
        },
        //保存核查信息
        saveCheckInfo:function(){
            var me = this;
            var circuitData = $("#cloudNetWorkResCheckGrid").grid("getCheckRows");//订单信息
            if (circuitData != null && circuitData.length < 1){
                fish.info("请先勾选要保存核查信息的电路！");
                return;
            }
            //校验资源是否满足
            var isResSatisfy = $('#isResSatisfy').val();
            if (isResSatisfy == null || isResSatisfy == ''){
                fish.info("资源是否满足不能为空！");
                return;
            }
            //校验A端上联设备类型
            var upDeviceTypeA = $('#upDeviceTypeA').val();
            if (upDeviceTypeA == null || upDeviceTypeA == ''){
                fish.info("A端上联设备类型不能为空！");
                return;
            }
            //校验Z端上联设备类型
            var upDeviceTypeZ = $('#upDeviceTypeZ').val();
            if (upDeviceTypeZ == null || upDeviceTypeZ == ''){
                fish.info("Z端上联设备类型不能为空！");
                return;
            }

            //校验资源描述
            if (isResSatisfy == '0'){
                var resDesc = $("#resDesc").val();
                if (resDesc == null || resDesc == ''){
                    fish.info("资源描述不能为空！");
                    return;
                }
            }

            // var params = {};
            // params.circuitData = circuitData;
            // var checkInfo = {};
            // checkInfo.mcpeTerminal = $("#mcpeTerminal").val();
            // checkInfo.terminalSituation = $("#terminalSituation").val();
            // checkInfo.distance = $("#distance").val();
            // checkInfo.terminalSeqNum = $("#terminalSeqNum").val();
            // checkInfo.resDesc = $("#resDesc").val();
            // params.checkInfo = checkInfo;
            // $.blockUI({message: '保存中...'});
            // cloudNetWorkResCheckFlowAction.saveCheckInfo(params, function(res){
            //     $.unblockUI();
            //     if (res.success){
            //         fish.info("核查信息保存成功！");
            //         me.qrycircuitInfo();
            //     }else{
            //         fish.info(res.msg);
            //     }
            // });
        }


    });
});