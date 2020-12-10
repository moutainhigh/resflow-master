define(['text!module/UnicomLocalNet/resmaster/portal/checkFlow/templates/investEstimationViewDouble.html',
    'module/UnicomLocalNet/resmaster/portal/orderTacheDealView/action/operOrderAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'module/UnicomLocalNet/resmaster/portal/checkFlow/views/checkCommon',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderTacheDealView/styles/operOrderView.css'
], function(operOrderView,operOrderAction,orderStandbyAction,orderDetailsAction,common ,i18n,css) {
    let SpecialFlag ;
    let srvOrdIds;
    let userInfo;
    let sysResource;
    let meOpera;
    return fish.View.extend({
        template: fish.compile(operOrderView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
            'click #checkSaveBtn': 'saveCheckInfo',
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
            SpecialFlag='-1';
            URL=this.getRootPath();
            let me = this;
            userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data;
            let woId=me.options.woId +'';
            let srvOrdId = me.options.srvOrdId + '';
            let srvBelong = new Object();
            srvBelong.srvOrdId = srvOrdId;
            sysResource=operOrderAction.qrySrvOrderBelongSys(srvBelong).responseJSON.data;
            common.initMe(meOpera);
            //初始化电路信息
            this.circuitInfo();
            this.initFish();
            this.initFileCheckTotal(meOpera.woId);

            // 核查专业选择
            common.initCheck();

            // 初始化选择框
            common.initCheckComboBox();
            // 初始化校验规则
            common.initValidate();
            // 初始化显示隐藏
            common.initShow();

            meOpera.$orderOperaFrom = this.$("#orderOper-form");
        },
        getRootPath:function (){
            //获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
            let curWwwPath=window.document.location.href;
            //获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
            let pathName=window.document.location.pathname;
            let pos=curWwwPath.indexOf(pathName);
            //获取主机地址，如： http://localhost:8083
            let localhostPaht=curWwwPath.substring(0,pos);
            //获取带"/"的项目名，如：/uimcardprj
            let projectName=pathName.substring(0,pathName.substr(1).indexOf('/')+1);
            return (localhostPaht+projectName);
        },
        // 初始化电路信息表
        circuitInfo : function() {
            let me = this;
            let tacheId = me.options.tacheId;
            let btnFlag = me.options.btnFlag;
            let psId = me.options.psId;
            $("#circuitGrid").grid({
                colModel: me.initGridInfo(),
                width: 516,
                multiselect: true,
                shrinkToFit: false,
                pageData: me.qrycircuitInfo(),
                onSelectRow: function (e, rowid, state, checked) {//选中行事件
                    let data = $("#circuitGrid").grid("getSelection");
                    let srvOrdId = data.SRV_ORD_ID + '';
                },
                onSelectAll: function (e, status){ //全选事件
                },
                onCellSelect: function (e, rowid, iCol, cellcontent) { // 选中单元格事件
                    let data = $("#circuitGrid").grid("getRowData",rowid);
                    //当iCol为0时，选中的是复选框而不是行数据，则不触发数据回显事件

                    if(me.qryCheckFlag()){
                        if(data.CHECK_STATE == '已保存' || tacheId == "500001150" || tacheId == "500001151"){
                            //回显核查反馈信息
                            me.initCheckInfo(data)
                        }else {
                            $('#A_CONSTRUCT_SCHEME').val('');
                            $('#A_RES_SATISFY').combobox('clear');
                            $('#A_ACCESS_ROOM').val('');
                            $('#A_INVESTMENT_AMOUNT').val('');
                            $('#A_CONSTRUCT_PERIOD').val('');
                            $('#Z_CONSTRUCT_SCHEME').val('');
                            $('#Z_ACCESS_ROOM').val('');
                            $('#Z_RES_SATISFY').combobox('clear');
                            $('#Z_INVESTMENT_AMOUNT').val('');
                            $('#Z_CONSTRUCT_PERIOD').val('');
                            $("input[name$='ACCESS_ROOM']").popedit("clear");
                            me.initCheckInfo(data);
                        }
                    }

                },
                gridComplete: function () {
                }
            });
            // 设置表格高度
            $("#circuitGrid").grid("setGridHeight", 150);
            // 冻结表格复选框一列
            $("#circuitGrid").grid('setFrozenColumns', 1);
            $("input[name$='A_ACCESS_ROOM']").popedit({
                open:function(e) {
                    let _this = $(this);
                    let param = new Object();
                    let province ;
                    let value ;
                    param.srvOrdId = srvOrdIds;
                    param.type = 'A';
                    let param2 =  new Object();
                    operOrderAction.qryProvinceName(param,function (date1) {
                        province = date1.ATTR_VALUE;
                        param2.province = province;
                        if(province != null ){
                            operOrderAction.qryProvinceValue(param2,function (date2) {
                                value = date2.ID;
                                let options = {
                                    url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/resourceSelectHcView',
                                    height: '80%',
                                    width: '80%',
                                    modal: false,
                                    draggable: true,
                                    resizable:true,
                                    autoResizable: true,
                                    viewOption: {
                                        flag : "org",
                                        province : province,
                                        value : value
                                    },
                                    callback: function (popup, view) {
                                        popup.result.then(function (data) {
                                            _this.popedit('setValue', {name:data.MACROOM_NAME, value:data.MACROOM_NAME});
                                        }, function (e) {
                                            console.log('关闭了', e);
                                        });
                                    }
                                };
                                let popup = fish.popupView(options);
                            });
                        }
                    });
                }
            });
            $("input[name$='Z_ACCESS_ROOM']").popedit({
                open:function(e) {
                    let _this = $(this);
                    let param = new Object();
                    let province ;
                    let value ;
                    param.srvOrdId = srvOrdIds;
                    param.type = 'Z';
                    let param2 =  new Object();
                    operOrderAction.qryProvinceName(param,function (date1) {
                        province = date1.ATTR_VALUE;
                        param2.province = province;
                        if(province != null ){
                            operOrderAction.qryProvinceValue(param2,function (date2) {
                                value = date2.ID;
                                //let city = $("#A_belong_city").popedit("getValue");
                                let options = {
                                    url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/resourceSelectHcView',
                                    height: '80%',
                                    width: '80%',
                                    modal: false,
                                    draggable: true,
                                    resizable:true,
                                    autoResizable: true,
                                    viewOption: {
                                        flag : "org",
                                        province : province,
                                        value : value
                                    },
                                    callback: function (popup, view) {
                                        popup.result.then(function (data) {
                                            _this.popedit('setValue', {name:data.MACROOM_NAME, value:data.MACROOM_NAME});
                                            //$('input[name="serialNumber"]').val(res.prodInstId);
                                        }, function (e) {
                                            console.log('关闭了', e);
                                        });
                                    }
                                };
                                let popup = fish.popupView(options);
                            });
                        }

                    });

                }
            });
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
            let me = this;
            let tacheId = me.options.tacheId; //"500001144";
            let psId = me.options.psId;
            let woState = me.options.woState;
            let orderId = me.options.orderId;
            let woId = me.options.woId;
            let buttonState = me.options.buttonState;
            let btnFlag = me.options.btnFlag;
            let srvOrdId = me.options.srvOrdId + '';
            let serviceId = me.options.serviceId;
            let resources = me.options.resources;
            let formValue = $('#orderOper-form').form('value');

            if(btnFlag == "submit"){
                $("#operOrderViewTitle").text("工单提交");
                if(psId == '1000211' && tacheId != "500001144"){ // 核查流程
                    if(sysResource.SYSTEM_RESOURCE!="second-schedule-lt"){
                        $("#feedBackADiv").show();
                        $("#feedBackZDiv").show();
                    }
                    $("#checkSaveBtnDiv").show();
                }
                if(tacheId == "500001145" || tacheId == "500001146" || tacheId == "500001147" || tacheId == "500001148"
                    || tacheId == "500001149" || tacheId == "510101020"){//专业核查
                    $("#remarkGoRoll").addClass("requireds");
                }
            }
        },

        //初始化附件表格
        initFileUpdate : function(param) {
            let me = this;
            $("#fileGrid").grid({
                colModel: [
                    {name: 'attachInfoId', label: '附件ID', hidden: true},
                    {name: 'fileName', label: '文件名称', width: 160, sortable: false },
                    {name: 'fileSize', label: '大小', width: 50},
                    {name: 'fileType', label: '类型', width: 40 , sortable: false },
                    {name: 'action', label: '操作', width: 100, formatter: 'actions',
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
                width:516,
                create: function () {//控件初始化完成触发的事件
                    operOrderAction.getAnnex(param,function (data) {
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
            me.$('#selectFiles').fileupload({
                dataType: 'json',
                autoUpload: false,
                add: function(e, data) {
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
                    me.isSubmit = false;
                    if (data.result.success) {
                        $.unblockUI();
                        fish.toast('success', data.result.message);
                        me.popup.close();
                    }else {
                        $.unblockUI();
                        fish.toast('error', data.result.message);
                    }
                },
            });
        },
        // 查询是否出现核查反馈页面
        qryCheckFlag:function(){
            let me = this;
            let tacheId = me.options.tacheId;
            let psId = me.options.psId;
            let formValue = $('#orderOper-form').form('value');
        //    let checkFlag = (psId == "1000211" && tacheId != "500001144") || (tacheId == "500001144" && formValue.resRadio == 1);
            let checkFlag = true;
            return checkFlag;
        },
        // 初始化核查反馈页面并查询数据
        initCheckInfo : function (param) {
            meOpera.queryCheckInfo(param);
        },
        // 查询核查反馈信息 参数：WO_ID,SRV_ORD_ID
        queryCheckInfo : function (param) {
            let me = this;
            let queryParam={};
            queryParam.woId=param.WO_ID;
            queryParam.orderId=param.ORDER_ID;
            queryParam.tacheId=param.TACHE_ID;
            queryParam.srvOrdId = param.SRV_ORD_ID;
            orderDetailsAction.queryCheckInfoBack(queryParam,function (res) {
                let dataArray = res.data;
                console.error("已保存的核查");
                console.error(dataArray);
                $("#COLLECT_RES").combobox('value',dataArray["COLLECT_RES"]);
                $("#COLLECT_MONEY").val(dataArray["COLLECT_MONEY"]);
                $("#COLLECT_DAY").val(dataArray["COLLECT_DAY"]);
                $("#COLLECT_DESC").val(dataArray["COLLECT_DESC"]);
            });
        },
        // 查询电路信息
        qrycircuitInfo : function(){
            let me = this;
            let psIds = '1000248,1000249'; //子流程
            let psId = me.options.psId;
            let specialtyCode = me.options.specialtyCode;
            let regionId = me.options.regionId;
            let dispObjTyeValue = me.options.dispObjTyeValue;
            let dispObjTye = me.options.dispObjTye;
            let param = {};
            param.cstOrdId = me.options.cstOrdId +'';
            param.orderId = me.options.orderId +'';
            param.woIds = me.options.woIds +'';
            param.srvOrdId = me.options.srvOrdId + '';
            param.woState = me.options.woState +'';
            param.tacheId = me.options.tacheId +'';
            param.dealUserId = userInfo.userId +'';
            param.dispObjTyeValue = me.options.dispObjTyeValue +'';
            param.dispObjTye = me.options.dispObjTye +'';
            param.btnFlag = me.options.btnFlag +'';
            param.newCreateResource = "1";// 是否新建资源：否
            if (me.qryCheckFlag()){
                param.woIds = me.options.woId;
                param.specialtyCode = specialtyCode;
                param.regionId = regionId;
                operOrderAction.qrySrvOrdListCheck(param,function (res) {
                    if(res.flag == 1){
                        $("#circuitGrid").grid("reloadData", res.data);
                        //      me.qryCircuitAreaInfo(res.data[0]);
                        me.initCheckInfo(res.data[0]);
                    }else {
                        fish.toast('error', res.message);
                    }
                });
            }else {
                operOrderAction.qrySrvOrdList(param,function (res) {
                    if(res.flag == 1){
                        $("#circuitGrid").grid("reloadData", res.data);
                        me.qryCircuitAreaInfo(res.data[0]);
                        if(me.qryCheckFlag()){
                            me.initCheckInfo(res.data[0]);
                        }
                    }else {
                        fish.toast('error', res.message);
                    }
                });
            }
            $(window).trigger("resize");
            //(specialtyCode != null && specialtyCode != null)
        },

        // 提交工单
        submit:function () {
            let formValue = $('#orderOper-form').form('value');
            if (formValue.remark == "") {
                fish.error("说明不能为空");
                return ;
            }
            let me = this;
            let psId = me.options.psId;
            let tacheId = me.options.tacheId;
            let buttonState = me.options.buttonState;
            let btnFlag = me.options.btnFlag;
            let params = new Object();
            let circuitData = $("#circuitGrid").grid("getCheckRows");
            let fileData = $("#fileGrid").grid("getRowData");
            let delFilesStr = "";
            for (let j = 0; j < this.delFiles.length; j++) {
                let temp = false;
                for (let i = 0; i < fileData.length; i++) {
                    if (this.delFiles[j] == fileData[i].attachInfoId) {
                        temp = true;
                        break;
                    }
                }
                if (!temp) {
                    delFilesStr += this.delFiles[j] + ",";
                }
            }
            if (delFilesStr) {
                params.delFiles = delFilesStr.substring(0, delFilesStr.length - 1);
            }
            if (FILES) {
                let vernier = 0;
                let fileLength = FILES.files.length;
                for (let index = 0; index < fileLength; index++) {
                    let file = FILES.files[index - vernier];
                    let isRemove = true;
                    for (let i = 0; i < fileData.length; i++) {
                        let data = fileData[i];
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
            if (circuitData.length == 0) {
                fish.warn('请至少选择一个电路信息！');
                return;
            }
            //判断有没有追单
            let flag = -1;
            let message = '';
            $.each(circuitData,function(i,val){
                let orderId = val.ORDER_ID;
                let cstOrdId = val.CST_ORD_ID;
                let ifFlag = orderDetailsAction.queryIfTrack(orderId + '', cstOrdId + '').responseJSON.data;
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
            params.circuitData = circuitData; //订单信息
            //modefy by wanggang2  异常单资源配置的时候这来字段空了 报错 不敢直接删掉
            if(sysResource!= null && sysResource!=''){
                params.sysResFullCom = sysResource.SYSTEM_RESOURCE; //系统来源
                params.resFullCom = sysResource.RESOURCES; //数据来源
            }
            if(btnFlag == "submit"){//回单
                let operAttrs = new Object();//线条参数
                let tacheOperInfo = new Object();//环节操作数据信息
                let actionFlag = "complateWo";
                if(me.qryCheckFlag()){
                    let circuitNum = -1;
                    $.each(circuitData,function(i,val){
                        if (val.CHECK_STATE != '已保存') {
                            circuitNum = 0;
                        }
                    });
                    if (circuitNum != -1 && tacheId != "500001150"){
                        fish.warn('选择了未保存的电路，请选择已保存的电路进行派发！');
                        return;
                    }
                }

                //资源核查，说明必填 huangxingfei 2019-04-23
                if (tacheId == "500001145" || tacheId == "500001146" || tacheId == "500001147" || tacheId == "500001148"
                    || tacheId == "500001149" || tacheId == "510101020") {
                    if(formValue.remark == null){
                        fish.error({title:'提示',message:'说明不能为空'});
                        return;
                    }
                }
                //流程线条参数
                if (tacheId == "500001151"){//投资估算
                    operAttrs.ifFinish = 1;
                    operAttrs.ifCollect = 0;
                }
                tacheOperInfo.remark = formValue.remark;
                params.operAttrsVal = operAttrs;
                params.tacheOperInfo = tacheOperInfo;
                params.actionFlag = actionFlag;
                params.action = "submit";
                $.blockUI({message: '派单中...'});
                if (FILES === null) {
                    me.submitOrder(params);
                }else {
                    me.fileUpdate(params); //上传附件并回单
                }
            }
        },
        submitOrder : function(params){
            let me = this;
            if (!me.isSubmit) {
                me.isSubmit = true;
                params.newCreateResource = "1";
                operOrderAction.submitOrder(params, function (res) {
                    me.isSubmit = false;
                    if (res.success) {
                        $.unblockUI();
                        fish.toast('success', res.message);
                        me.popup.close();
                    } else {
                        $.unblockUI();
                        fish.toast('error', res.message);
                    }
                });
            } else {
                fish.toast('error', "请勿重复提交！");
            }
        },

        // 保存核查反馈信息
        saveCheckInfo: function(){
            let data = $("#circuitGrid").grid("getCheckRows");
            if (data.length >= 1) {
                let param = {};
                param.circuitData = data;
                param.formValue = meOpera.$orderOperaFrom.form('value');
                // 校验核查数据
                let form = meOpera.$orderOperaFrom;
                meOpera.$orderOperaFrom.validator("setField", {"remark":""});
                if (!form.isValid()) {
                    // 校验未通过
                    fish.toast('warn', "请完整填写正确信息");
                    return;
                }
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
                fish.toast('warn', "请至少勾选一条电路信息!");
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
                    // operOrderAction.getAnnex(param,function (data) {
                    //     console.log("控件初始化完成触发的事件param");
                    //     console.log(param);
                    //     console.log("控件初始化完成触发的事件data");
                    //     console.log(data);
                    //     if (data.length != 0) {
                    //         for (i = 0; i < data.length; i++) {
                    //             let fileObj = {};
                    //             let map = data[i];
                    //             for (let k in map) {  //通过定义一个局部变量k遍历获取到了map中所有的key值
                    //                 let docList = map[k]; //获取到了key所对应的value的值！
                    //                 if (k == "ATTACH_INFO_ID") {
                    //                     fileObj.attachInfoId = docList;
                    //                 }
                    //                 if (k == "FILE_SIZE") {
                    //                     fileObj.fileSize = docList + "KB";
                    //                 }
                    //                 if (k == "FILE_TYPE") {
                    //                     fileObj.fileType = docList;
                    //                 }
                    //                 if (k == "FILE_NAME") {
                    //                     fileObj.fileName = docList.split(".")[0];
                    //                 }
                    //             }
                    //             $("#fileGrid").grid("addRowData", fileObj);
                    //         }
                    //     }
                    // });
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
                }
            });
        }
    });
});