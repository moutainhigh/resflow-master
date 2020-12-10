define([
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'text!module/UnicomLocalNet/resmaster/portal/checkFlow/templates/checkTotalViewDouble.html',
    'module/UnicomLocalNet/resmaster/portal/orderTacheDealView/action/operOrderAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'module/UnicomLocalNet/resmaster/portal/checkFlow/views/checkCommon',
    // 'css!module/UnicomLocalNet/resmaster/portal/orderTacheDealView/styles/operOrderView.css',
    'css!module/UnicomLocalNet/resmaster/portal/checkFlow/styles/checkCommon.css'
], function(i18n, operOrderView,operOrderAction,orderStandbyAction,orderDetailsAction,checkCommon) {
    let dispatchNum = '-1';
    let SpecialFlag;
    let srvOrdIds;
    let userInfo;
    let meOpera, URL="", FILES = null;
    let tacheIds = [
        '510101061','510101062','510101063','510101064','1010101082','510101066','',
    ]
    let checkInfo={};
    let collectKey = ["COLLECT_DAY", "COLLECT_MONEY", "COLLECT_DESC"];
    return fish.View.extend({
        template: fish.compile(operOrderView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
            'click #checkSaveBtn': 'saveCheckInfo',
            'click #localCheckBtn': 'openInfoDialog',
        },
        initialize: function() {
            this.render();
            URL = "";
            FILES = null;
            this.isSubmit = false;
            this.delFiles = new Array();
        },

        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },
        afterRender: function() {
            meOpera = this;
            meOpera.$orderOperaFrom = $("#orderOper-form");

            URL=checkCommon.getRootPath();
            userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data;
            meOpera.woId=meOpera.options.woId +'';
            let srvOrdId = meOpera.options.srvOrdId + '';
            let srvBelong = {};

            srvBelong.srvOrdId = srvOrdId;
            sysResource = operOrderAction.qrySrvOrderBelongSys(srvBelong).responseJSON.data;
            console.log(meOpera.options);
            console.log("=============传入页面参数=============");
            srvOrdIds = meOpera.options.srvOrdId;
            checkCommon.initMe(meOpera);
            //初始化电路信息
            this.circuitInfo();
            this.initFish();
            this.initFileCheckTotal(meOpera.woId);

            // 核查专业选择
            checkCommon.initCheck();

            // 初始化选择框
            checkCommon.initCheckComboBox();
            // 初始化校验规则
            checkCommon.initValidate();
            // 初始化显示隐藏
            checkCommon.initShow();

        },
        /**
         * 选择派发本地核查和专业为是,或者 是并行核查流程  则不展示本地录入按钮
         * @param param
         */
        localShow:function(param){
            operOrderAction.queryisSendSpeciallocal(param, function (res) {
                if(res || param.psId=='10101322'){
                    //选择派发本地核查和专业为是   或者 是并行核查流程
                    $('#localCheckBtn').hide();
                }else{
                    $('#localCheckBtn').show();
                }
            });
        },
        // 初始化电路信息表
        circuitInfo : function() {
            let tacheId = meOpera.options.tacheId;
            let btnFlag = meOpera.options.btnFlag;
            let psId = meOpera.options.psId;
            $("#circuitGrid").grid({
                colModel: meOpera.initGridInfo(),
                width: 516,
                multiselect: true,
                shrinkToFit: false,
                pageData: meOpera.qrycircuitInfo(),
                onSelectRow: function (e, rowid, state, checked) {//选中行事件
                    let data = $("#circuitGrid").grid("getSelection");
                    let srvOrdId = data.SRV_ORD_ID + '';
                },
                onSelectAll: function (e, status){ //全选事件
                },
                onCellSelect: function (e, rowid, iCol, cellcontent) { // 选中单元格事件
                    let data = $("#circuitGrid").grid("getRowData",rowid);
                    //当iCol为0时，选中的是复选框而不是行数据，则不触发数据回显事件
                    if(data.CHECK_STATE != '已保存' && tacheId != "510101060" && tacheId != "510101066"){

                    }
                    //回显核查反馈信息
                    meOpera.initCheckInfo(data, 1);
                },
                gridComplete: function () {
                }
            });
            // 设置表格高度
            $("#circuitGrid").grid("setGridHeight", 150);
            // 冻结表格复选框一列
            $("#circuitGrid").grid('setFrozenColumns', 1);

        },
        initGridInfo:function(){
            let tacheId = this.options.tacheId;
            let psId = this.options.psId;
            // 核查调度不选专业、核查流程(非核查调度环节)
            return [
                {name: 'CHECK_STATE', label: '反馈信息', width: 95, sortable: false,
                    formatter: function(cellval, opts, rwdat, _act) {
                        if(cellval == '已保存'){
                            return '<div class="btn-group">' +
                                '<button type="button" class="btn btn-link js-delete" style="color: #6DCC4A">'+cellval+'</button>' +
                                '</div>';
                        }else{
                            return '<div class="btn-group btn btn-link" style="color: #FF5858">'+cellval +
                                '</div>';
                        }
                    }},
                {name: 'CIRCUITCODE', label: '电路编号', width: 100, sortable: false },
                {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                {name: 'SERIAL_NUMBER', label: '业务号码', width: 100 , sortable: false },
                {name: 'AREGIONNAME', label: 'A端所属区域', width: 110, sortable: false},
                {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110, sortable: false},
                {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
            ]
        },

        initFish:function () {

            let tacheId = meOpera.options.tacheId; //"500001144";
            let psId = meOpera.options.psId;
            let woState = meOpera.options.woState;
            let orderId = meOpera.options.orderId;
            let woId = meOpera.options.woId;
            let buttonState = meOpera.options.buttonState;
            let btnFlag = meOpera.options.btnFlag;
            let srvOrdId = meOpera.options.srvOrdId + '';
            let serviceId = meOpera.options.serviceId;
            let resources = meOpera.options.resources;
            let formValue = $('#orderOper-form').form('value');

            if(btnFlag == "submit"){
                //  $("#operOrderViewTitle").text("工单提交");
                if(psId == '1000211' && tacheId != "500001144"){ // 核查流程
                    if(sysResource.SYSTEM_RESOURCE!="second-schedule-lt"){
                        $("#feedBackADiv").show();
                        $("#feedBackZDiv").show();
                    }
                    $("#checkSaveBtnDiv").show();
                }
                if(tacheIds.indexOf(tacheId) !=-1){//专业核查
                    $("#remarkGoRoll").addClass("requireds");
                }
            }
        },
        // 查询是否出现核查反馈页面
        qryCheckFlag:function(){

            let tacheId = meOpera.options.tacheId;
            let psId = meOpera.options.psId;
            let formValue = $('#orderOper-form').form('value');
            let checkFlag = (psId == "1000211" && tacheId != "510101052") || (tacheId == "510101052" && formValue.resRadio == 1);
            //    let checkFlag = true;
            return checkFlag;
        },
        // 初始化核查反馈页面并查询数据
        initCheckInfo : function (param, temp) {
            console.log(temp);
            meOpera.queryCheckInfo(param);
        },
        queryCheckInfo : function (param) {
            let queryParam={};
            queryParam.woId= param.WO_ID;
            queryParam.orderId=param.ORDER_ID;
            queryParam.tacheId= param.TACHE_ID;
            queryParam.srvOrdId = param.SRV_ORD_ID;
            queryParam.psId = meOpera.options.psId;
            meOpera.localShow(queryParam);
            orderDetailsAction.queryCheckInfoBack(queryParam,function (res) {
                checkInfo = res.data;
                // 是否投资估算
                let isInvestment = checkInfo.IS_INVESTMENT;
                let tempColletMoney= checkInfo.COLLECT_MONEY;
                if (isInvestment) {
                    tempColletMoney = checkInfo.COLLECT_MONEY - checkInfo.A_TOTAL_AMOUNT - checkInfo.Z_TOTAL_AMOUNT;
                }
                checkInfo.tempColletMoney = tempColletMoney;
                console.error("已保存的核查");
                console.error(checkInfo);
                // 资源是否具备
                for(let key in checkInfo){
                    let value = checkInfo[key];
                    if (key == "COLLECT_RES") {
                        $("#" + key).combobox('value',value);
                        break;
                    }
                }
                for (let index in collectKey) {
                    let key = collectKey[index];
                    let value = checkInfo[key];
                    $("#" + key).val(value);
                }
            });
        },
        // 控制显示隐藏
        initShow: function (type) {
            if (type == null) {
                meOpera.baseInitShow("A");
                meOpera.baseInitShow("Z");
            } else {
                meOpera.baseInitShow(type);
            }

        },
        // 查询电路信息
        qrycircuitInfo : function(){

            let psIds = '1000248,1000249'; //子流程
            let psId = meOpera.options.psId;
            let specialtyCode = meOpera.options.specialtyCode;
            let regionId = meOpera.options.regionId;
            let dispObjTyeValue = meOpera.options.dispObjTyeValue;
            let dispObjTye = meOpera.options.dispObjTye;
            let param = {};
            param.cstOrdId = meOpera.options.cstOrdId +'';
            param.orderId = meOpera.options.orderId +'';
            param.woIds = meOpera.options.woIds +'';
            param.srvOrdId = meOpera.options.srvOrdId + '';
            param.woState = meOpera.options.woState +'';
            param.tacheId = meOpera.options.tacheId +'';
            param.dealUserId = userInfo.userId +'';
            param.dispObjTyeValue = meOpera.options.dispObjTyeValue +'';
            param.dispObjTye = meOpera.options.dispObjTye +'';
            param.btnFlag = meOpera.options.btnFlag +'';
            param.newCreateResource = "1";// 是否新建资源：否
            param.woIds = meOpera.options.woId;
            param.specialtyCode = specialtyCode;
            param.regionId = regionId;
            operOrderAction.qrySrvOrdListCheck(param,function (res) {
                if(res.flag == 1){
                    $("#circuitGrid").grid("reloadData", res.data);
                    meOpera.initCheckInfo(res.data[0],2);
                }else {
                    fish.toast('error', res.message);
                }
            });
            $(window).trigger("resize");
        },

        // 提交工单
        submit:function () {
            var me = this;
            var psId = me.options.psId;
            var tacheId = me.options.tacheId;
            var serviceId =  me.options.serviceId;
            var buttonState = me.options.buttonState;
            var formValue = $('#orderOper-form').form('value');
            if (formValue.remark == null) {
                fish.toast("warn", "说明不能为空");
                return;
            }
            var btnFlag = me.options.btnFlag;
            var params = new Object();
            var circuitData = $("#circuitGrid").grid("getCheckRows");//订单信息
            if (circuitData.length == 0) {
                fish.warn('请至少选择一个电路信息！');
                return false;
            }
            //判断有没有追单
            var flag = -1;
            var message = '';
            $.each(circuitData,function(i,val){
                var orderId = val.ORDER_ID;
                var cstOrdId = val.CST_ORD_ID;
                var ifFlag = operOrderAction.queryIfTrack(orderId + '', cstOrdId + '').responseJSON.data;
                if (ifFlag){
                    flag = i;
                    message = '存在追单未处理的电路，不能提交，请重新勾选!';
                    return false;
                }
            });
            if (flag > -1) {
                fish.error({title:'提示',message:message});
                return;
            }
            var circuitNum = -1;
            $.each(circuitData,function(i,val){
                if (val.CHECK_STATE != '已保存') {
                    circuitNum = 0;
                }
            });
            if (circuitNum != -1){
                fish.warn('选择了未保存的电路，请选择已保存的电路进行派发！');
                return false;
            };
            var operAttrs = new Object();//线条参数
            var tacheOperInfo = new Object();//环节操作数据信息
            var actionFlag = "complateWo";
            //流程线条参数
            if (tacheId == "510101060"){//核查汇总
                var estimateFlag = $("input[name='estimateRadio']:checked").val();
                if (estimateFlag == '0') {
                    operAttrs.investment_estimation = 0;
                }else if(estimateFlag == '1') {
                    operAttrs.investment_estimation = 1; //是否需要专项投资估算 否：1
                }
            }
            tacheOperInfo.remark = formValue.remark;
            if (formValue.remark == "") {
                fish.warn('说明不能为空！');
                return
            }
            params.circuitData = circuitData;
            params.operAttrsVal = operAttrs;
            params.tacheOperInfo = tacheOperInfo;
            params.actionFlag = actionFlag;
            params.action = btnFlag;
            params.serviceId = serviceId;
            if (FILES === null) {
                me.submitOrder(params);
            }else {
                me.fileUpdate(params); //上传附件并回单
            }
        },
        //提交工单
        submitOrder : function(params){
            var me = this;
            // params.sysComeRes = me.sysResource.RESOURCES; //数据来源
            // if( me.saveCheckInfo()){ //提交前保存反馈信息
            $("#orderOper-form").blockUI({message: '派单中...'}).data('blockui-content', true);
            operOrderAction.submitOrder(params,function (res) {
                if(res.success){
                    $("#orderOper-form").unblockUI().data('blockui-content', false);
                    fish.toast('success', res.message);
                    me.popup.close();
                }else {
                    $("#orderOper-form").unblockUI().data('blockui-content', false);
                    fish.toast('error', res.message);
                }
            });
            //}
        },

        // 保存核查反馈信息
        saveCheckInfo: function(){

            let data = $("#circuitGrid").grid("getCheckRows");
            if (data.length >= 1) {
                debugger;
                let form = meOpera.$orderOperaFrom;
                form.validator("setField", {"remark":""});
                if (!form.isValid()) {
                    // 校验未通过
                    fish.toast('warn', "请完整填写正确信息");
                    return;
                }
                let param = {};
                param.circuitData = data;
                param.formValue = form.form('value');
                for (let key in checkInfo) {
                    if (key.indexOf("COLLECT_") == -1) {
                        param.formValue[key] = checkInfo[key];
                    }
                }
                console.error("提交的信息");
                console.error(param);
                operOrderAction.saveCheckInfo(param,function(res){
                    if (res.success){
                        //核查信息保存完成后，重新刷新电路信息表格数据
                        meOpera.circuitInfo();
                        fish.toast('success', res.message);
                    } else{
                        fish.toast('error', res.message);
                    }
                });
            }else{
                fish.toast('error', "请至少勾选一条电路信息!");
            }
        },
        //初始化附件表格
        initFileCheckTotal : function(param) {

            $("#fileGrid").grid({
                colModel: [
                    {name: 'attachInfoId', label: '附件ID', hidden: true},
                    {name: 'fileName', label: '文件名称', width: 160, sortable: false },
                    {name: 'fileSize', label: '大小', width: 50},
                    {name: 'fileType', label: '类型', width: 40 , sortable: false },
                    {name: 'action', label: '操作', width: 100, formatter: 'actions', formatoptions: {
                            editbutton: false,
                            delbutton: function (rowdata) {
                                if (rowdata.attachInfoId) {
                                    meOpera.delFiles.push(rowdata.attachInfoId);
                                }
                                return true;
                            }
                        }

                    }
                ],
                width:516,
                create: function () {//控件初始化完成触发的事件
                    operOrderAction.getAnnex(param,function (data) {
                        console.log("控件初始化完成触发的事件param");
                        console.log(param);
                        console.log("控件初始化完成触发的事件data");
                        console.log(data);
                        if (data.length != 0) {
                            for (i = 0; i < data.length; i++) {
                                let fileObj = {};
                                let map = data[i];
                                for (let k in map) {  //通过定义一个局部变量k遍历获取到了map中所有的key值
                                    let docList = map[k]; //获取到了key所对应的value的值！
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
            meOpera.$('#selectFiles').fileupload({
                dataType: 'json',
                autoUpload: false,
                add: function(e, data) {
                    console.log("上传文件的element");
                    console.log(e);
                    console.log("上传文件的data");
                    console.log(data);
                    let fileObj = {};
                    let obj = data.files[0];
                    fileObj.attachInfoId = '';
                    let site = obj.name.lastIndexOf(".");
                    fileObj.fileName = obj.name.substring(0, site);
                    fileObj.fileSize = (obj.size / 1024.0).toFixed(2) + "KB";
                    fileObj.fileType = obj.name.substring(site + 1, obj.name.length);
                    if ((obj.size / 1024.0).toFixed(2) >= 20*1024 ){
                        fish.warn('上传文件不能超过20M！');
                        return;
                    }
                    $("#fileGrid").grid("addRowData", fileObj);
                    if (FILES === null) {
                        FILES = data;
                    } else {
                        FILES.files.push(obj);
                    }
                },
                always: function (e, data) {
                    meOpera.isSubmit = false;
                    if (data.result.success) {
                        $.unblockUI();
                        fish.toast('success', data.result.message);
                        meOpera.popup.close();
                    }else {
                        $.unblockUI();
                        fish.toast('error', data.result.message);
                    }
                },
            });
        },
        fileUpdate :function (params){
            FILES.url = URL+"/localScheduleLT/FlieUpdateController/uploadFiles.spr";
            FILES.formData = {
                params : JSON.stringify(params)
            };
            if (!this.isSubmit) {
                this.isSubmit = true;
                FILES.submit();
            } else {
                fish.toast('error', "请勿重复提交！");
            }
        },

        openInfoDialog:function(){
            let data = $("#circuitGrid").grid("getCheckRows");
            if(data.length != 1 ) {
                fish.toast('warn', "请勾选【一条】电路信息!");
                return;
            }
            $.blockUI("页面加载中....")
            let options = {
                url: 'module/UnicomLocalNet/resmaster/portal/checkFlow/views/checkInfoPop.js',
                height: '90%',
                width: '50%',
                modal: true,
                draggable: true,
                resizable:true,
                autoResizable: true,
                viewOption:  checkInfo,
                callback: function (popup, view) {
                    popup.result.then(function (data) {
                        // 数据回填
                        console.log("保存关闭");
                        checkInfo = data;
                        meOpera.collectAllData();
                        console.log(data);
                    }, function (e) {
                        console.log('关闭了', e);
                    });
                }
            };
            let popup = fish.popupView(options);
        },
        collectAllData:function(){
            // 资源具备
            let collectRes =  checkInfo.A_RES_HAVE == 1 && checkInfo.Z_RES_HAVE == 1 && $("#COLLECT_RES").combobox("value")==1?1:0;
            $("#COLLECT_RES").combobox("value", collectRes);

            let dayArray = [$("#COLLECT_DAY").val(), checkInfo.A_LONGEST_PERIOD, checkInfo.Z_LONGEST_PERIOD];
            let collectDay =Math.max.apply(null, dayArray);
            $("#COLLECT_DAY").val(collectDay);

            let amount = [checkInfo.tempColletMoney,checkInfo.A_TOTAL_AMOUNT,checkInfo.Z_TOTAL_AMOUNT];
            let allAmount = eval(amount.join("+"));
            $("#COLLECT_MONEY").val(allAmount);
        }

    });
});