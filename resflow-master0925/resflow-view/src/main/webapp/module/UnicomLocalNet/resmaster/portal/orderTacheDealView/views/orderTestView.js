define(['module/UnicomLocalNet/resmaster/portal/orderTacheDealView/action/operOrderAction',
    'module/UnicomLocalNet/resmaster/portal/orderTacheDealView/action/orderRollBackAction',
    'text!module/UnicomLocalNet/resmaster/portal/orderTacheDealView/templates/orderTestView.html',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderTacheDealView/styles/operOrderView.css'
], function(operOrderAction,orderRollBackAction,orderTestView,orderStandbyAction,i18n,css) {

    var meTest;
    return fish.View.extend({
        template: fish.compile(orderTestView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
        },

        initialize: function() {
            this.render();
            URL = "";
            FILES = null; //附件
            userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data; //用户信息
            this.isSubmit = false;
            this.delFiles = new Array();
        },

        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },

        //初始化fish组件
        afterRender: function() {
            URL=this.getRootPath();
            var me = this;
            meTest = this;
            var orderId = me.options.orderId + '';
            var tacheId = me.options.tacheId; //"500001144";
            this.initFish();
            //初始化电路信息
            this.circuitInfo();
            //初始化附件表格
            this.initFileUpdate(me.options.woId +'');
            //测试结果不通过说明必填
            $("input[name = 'testRadio']").bind('click',function(){
                debugger
                var testRadioFlag = $("input[name='testRadio']:checked").val();
                if (testRadioFlag == '0') {
                    $("#remarkGoRoll").removeClass("requireds");
                    if("510101051" == tacheId){
                        $("#secondDataMake").hide();
                        $("#secondResMake").hide();
                    }else if("510101045" == tacheId){
                        $("#crossAZAreaDiv").hide();
                    }
                }else if(testRadioFlag == '1') {
                    $("#remarkGoRoll").addClass("requireds");
                    var data = $("#circuitGrid").grid("getSelection");
                    if (JSON.stringify(data) == "{}"){
                        data = $("#circuitGrid").grid("getRowData")[0];
                    }
                    if("510101051" == tacheId){
                        $("#secondDataMake").show();
                        $("#secondResMake").show();
                        me.qryspecialtyData(data.ORDER_ID);
                        me.qryspecialtyRes(data.ORDER_ID);
                    }else if("510101045" == tacheId){
                        $("#crossAZAreaDiv").show();
                        me.getProvince(data.SRV_ORD_ID);
                    }
                }
            });
        },
        getRootPath:function (){
            //获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
            var curWwwPath=window.document.location.href;
            //获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
            var pathName=window.document.location.pathname;
            var pos=curWwwPath.indexOf(pathName);
            //获取主机地址，如： http://localhost:8083
            var localhostPaht=curWwwPath.substring(0,pos);
            //获取带"/"的项目名，如：/uimcardprj
            var projectName=pathName.substring(0,pathName.substr(1).indexOf('/')+1);
            return (localhostPaht+projectName);
        },
        initFish:function () {
            var me = this;
            var tacheId = me.options.tacheId; //"500001144";
            var psIds = '1000248,1000249';
            var psId = me.options.psId;
            var woState = me.options.woState;
            var orderId = me.options.orderId;
            var woId = me.options.woId;
            var buttonState = me.options.buttonState;
            var btnFlag = me.options.btnFlag;
            var srvOrdId = me.options.srvOrdId + '';
            var formValue = $('#orderOper-form').form('value');
            /*if("510101051" == tacheId){
                meTest.secondDataMakeInfo(srvOrdId);
            }*/
            $('#testDiv').bind('click',function(){
                if ($("input[name='testRadio']:checked").val() == '0') { //通过 附件必填
                    $("#attach").addClass("requireds");
                } else if ($("input[name='testRadio']:checked").val() == '1') { //不通过 附件不必填
                    $("#attach").removeClass("requireds");
                }
            });

        },
        /*secondDataMakeInfo: function(srvOrdId){
            $("#secondDataMakeGrid").grid({
                colModel: [
                    {name: 'WO_ID', label: '二干专业制作工单Id', width: 100 , hidden: true },
                    {name: 'ORDER_ID', label: '二干专业制作子流程Id', width: 100 , hidden: true },
                    {name: 'SRV_ORD_ID', label: '业务订单Id', width: 100 , hidden: true },
                    {name: 'SPECIALTY_CODE', label: '专业Code', width: 100 , hidden: true },
                    {name: 'DEAL_USER_ID', label: '工单处理人Id', width: 100 , hidden: true },
                    {name: 'COMP_USER_ID', label: '工单完成人Id', width: 100 , hidden: true },
                    {name: 'PUB_DATE_NAME', label: '专业', width: 510 , sortable: false },
                ],
                width: 510,
                multiselect: true,
                shrinkToFit: false,
                pageData: meTest.qrySecondDataMakeLists(srvOrdId),
                onSelectRow: function (e, rowid, state, checked) {//选中行事件
                },
                onSelectAll: function (e, status) {
                },

            });
            // 设置表格高度
            $("#secondDataMakeGrid").grid("setGridHeight", 50);
            // 冻结表格复选框一列
            $("#secondDataMakeGrid").grid('setFrozenColumns', 1);

        },*/
        //查询退单省份
        getProvince : function (srvOrdId) {
            var me = this;
            var params = {};
            params.srvOrdId = srvOrdId;
            operOrderAction.getProvinceName(params,function (res) {
                var divStr = "";
                $.each(res,function(i,val){
                    divStr = divStr + '<label class="checkbox-inline">'+
                        '<input type="checkbox" name="province" id="' + i + '" value= "' + val.province + '" checked="true" >'
                        + val.province
                        +'</label>';
                });
                //$("#provinceDiv").replaceWith(divStr);
                me.$("#backAZProvinceDiv").html("");
                me.$("#backAZProvinceDiv").append(divStr);

            })
        },

        /*qrySecondDataMakeLists: function(srvOrdId){
            var me = this;
            var param = {};
            param.tacheId = '1551002630';
            param.woState = '290000002';
            param.srvOrdId = srvOrdId; //(主调局)订单Id
            param.cstOrdId = me.options.cstOrdId +'';
            operOrderAction.qrySecondDataMakeList(param,function (res) {
                if('1' == res.flag){
                    $("#secondDataMakeGrid").grid("reloadData", res.data);
                }else {
                    fish.toast('error', res.message);
                }
            });

        },*/

        qryspecialtyData : function(orderId){
            var me = this;
            var params = {};
            params.orderId = orderId;
            orderRollBackAction.qrySecondDataMake(params,function (res) {
                var divStr = "";
                if(res.flag){
                    $.each(res.data,function(i,val){
                        divStr = divStr + '<label class="checkbox-inline">'+
                            '<input type="checkbox" name="specialty" id="' + val.SPECIALTY_CODE
                            + '" value= "' + val.SPECIALTY_CODE +'">'
                            + val.PUB_DATE_NAME
                            +'</label>';
                    });
                    me.$("#secondDataMakeDiv").html("");
                    me.$("#secondDataMakeDiv").append(divStr);
                }else {
                    fish.toast('error', '二干数据制作专业查询失败！');
                }
            })
        },

        qryspecialtyRes : function(orderId){
            var me = this;
            var params = {};
            params.orderId = orderId;
            orderRollBackAction.qrySecondResMake(params,function (res) {
                var divStr = "";
                if(res.flag){
                    $.each(res.data,function(i,val){
                        divStr = divStr + '<label class="checkbox-inline">'+
                            '<input type="checkbox" name="specialtyRes" id="' + val.SPECIALTY_CODE_RES
                            + '" value= "' + val.SPECIALTY_CODE +'">'
                            + val.PUB_DATE_NAME
                            +'</label>';
                    });
                    me.$("#secondResMakeDiv").html("");
                    me.$("#secondResMakeDiv").append(divStr);
                }else {
                    fish.toast('error', '二干资源施工专业查询失败！');
                }
            })
        },

        submit:function () {
            var me = this;
            var psId = me.options.psId;
            var tacheId = me.options.tacheId;
            var buttonState = me.options.buttonState;
            var formValue = $('#orderOper-form').form('value');
            var btnFlag = me.options.btnFlag;
            var params = new Object();

            var fileData = $("#fileGrid").grid("getRowData");
            var delFilesStr = "";
            for (var j = 0; j < this.delFiles.length; j++) {
                var temp = false;
                for (var i = 0; i < fileData.length; i++) {
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

            var circuitData = $("#circuitGrid").grid("getCheckRows");//订单信息
            if (circuitData.length == 0) {
                fish.warn('请至少选择一个电路信息！');
                return;
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
            if ($("input[name='testRadio']:checked").val() == '0' && FILES === null && !fileData.length > 0){ //测试通过且附件为空
                fish.error({title:'提示',message:'必须上传附件'})
                return;
            }
            var operAttrs = new Object();//线条参数
            var tacheOperInfo = new Object();//环节操作数据信息
            var actionFlag = "complateWo";
            //流程线条参数
            if (tacheId == "510101045"){ //省级全程调测 --[一干电路]客户,局内电路流程
                var testFlag = $("input[name='testRadio']:checked").val();
                if (testFlag == '0') {
                    operAttrs.COMMISSIONING_RESULT = 0;
                }else if(testFlag == '1') {
                    operAttrs.COMMISSIONING_RESULT = 1;
                    var provinceList = [];
                    $('input[name="province"]').each(function(){
                        if($(this).is(':checked')==true){
                            provinceList.push($(this).val());
                        }
                    });
                    if(provinceList == null || provinceList == ""){
                        fish.error({title:'提示',message:'退单省份至少选择一个。。。'});
                        return;
                    }
                    if(formValue.remark == null){
                        fish.error({title:'提示',message:'说明不能为空！'});
                        return;
                    }
                }
            }
            params.action = btnFlag;
            if (tacheId == "510101051"){ //全程调测 --[二干电路]客户,局内电路流程
                debugger
                var testFlag = $("input[name='testRadio']:checked").val();
                if (testFlag == '0') {
                    operAttrs.COMMISSIONING_REVIEW = 0;
                }else if(testFlag == '1') {
                    params.action = "rollBackOrder";
                    params.flag = "LOCAL";
                    var specialtyList = [];
                    $('input[name="specialty"]').each(function(){
                        if($(this).is(':checked')==true){
                            specialtyList.push($(this).val());
                        }
                    });

                    var specialtyResList = [];
                    $('input[name="specialtyRes"]').each(function(){
                        if($(this).is(':checked')==true){
                            specialtyResList.push($(this).val());
                        }
                    });
                    if((specialtyList == null || specialtyList == "") && (specialtyResList == null || specialtyResList == "")){
                        fish.error({title:'提示',message:'请选择数据制作或者资源施工专业'});
                        return;
                    }
                    /*var dataMakeLen = $("#secondDataMakeGrid").grid("getCheckRows");
                    if(dataMakeLen.length == 0){
                        fish.error({title:'提示',message:'请选择数据制作专业'});
                        return;
                    }else{
                        params.dataMakeData = dataMakeLen; //数据制作信息
                    }*/
                    if(formValue.remark == null){
                        fish.error({title:'提示',message:'说明不能为空'});
                        return;
                    }
                    params.specialty = specialtyList == null ? "" : specialtyList ; //需要退单的专业
                    params.specialtyRes = specialtyResList == null ? "" : specialtyResList ; //需要退单的专业

                }
            }
            tacheOperInfo.remark = formValue.remark;
            params.circuitData = circuitData;
            params.operAttrsVal = operAttrs;
            params.tacheOperInfo = tacheOperInfo;
            params.actionFlag = actionFlag;
            params.province = provinceList == null ? "": provinceList;
            $("#orderOper-form").blockUI({message: '派单中...'}).data('blockui-content', true);
            if (FILES === null) {
                me.submitOrder(params);
            }else {
                me.fileUpdate(params); //上传附件并回单
            }

        },
        //提交工单
        submitOrder : function(params){
            var me = this;
            if (!me.isSubmit) {
                me.isSubmit = true;
                operOrderAction.submitOrder(params,function (res) {
                    me.isSubmit = false;
                    if(res.success){
                        $("#orderOper-form").unblockUI().data('blockui-content', false);
                        fish.toast('success', res.message);
                        me.popup.close();
                    }else {
                        $("#orderOper-form").unblockUI().data('blockui-content', false);
                        fish.toast('error', res.message);
                    }
                });
            }
        },
        circuitInfo : function() {
            var me = this;
            var tacheId = me.options.tacheId;
            var btnFlag = me.options.btnFlag;
            var psId = me.options.psId;
            $("#circuitGrid").grid({
                colModel: me.initGridInfo(),
                width: 516,
                multiselect: true,
                shrinkToFit: false,
                pageData: me.qrycircuitInfo(),
                onSelectRow: function (e, rowid, state, checked) {//选中行事件
                    var data = $("#circuitGrid").grid("getSelection");
                    var testRadioFlag = $("input[name='testRadio']:checked").val();
                    if(testRadioFlag == '1') {
                        if("510101051" == tacheId){
                            me.qryspecialtyData(data.ORDER_ID);
                            me.qryspecialtyRes(data.ORDER_ID);
                        }else if("510101045" == tacheId){
                            me.getProvince(data.SRV_ORD_ID);
                        }
                    }
                },
                onSelectAll: function (e, status){ //全选事件
                },
                /*onCellSelect:function(e, rowid, iCol, cellcontent){
                    // debugger;
                    var data = $("#circuitGrid").grid("getRowData",rowid);
                    //当iCol为0时，选中的是复选框而不是行数据，则不触发数据回显事件
                    if(0 != iCol){
                        if('500001155' == tacheId || '500001157' == tacheId){
                            me.initSelectSave(data.ORDER_ID);
                        }
                    }

                },*/
            });
            $("#circuitGrid").grid("setGridHeight", 150);

        },
        initGridInfo:function(){
            return [
                {name: 'CIRCUITCODE', label: '电路编号', width: 95, sortable: false },
                {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                {name: 'SERIAL_NUMBER', label: '业务号码', width: 100 , sortable: false },
                {name: 'ACITY', label: 'A端城市', width: 100 , sortable: false },
                {name: 'ZCITY', label: 'Z端城市', width: 100 , sortable: false },
                {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
            ]
        },
        qrycircuitInfo : function(){
            var me = this;
            var psId = me.options.psId;
            var dispObjTyeValue = me.options.dispObjTyeValue;
            var dispObjTye = me.options.dispObjTye;
            var param = {};
            param.cstOrdId = me.options.cstOrdId +'';
            param.woState = me.options.woState +'';
            param.tacheId = me.options.tacheId +'';
            param.dealUserId = userInfo.userId +'';
            param.dispObjTyeValue = me.options.dispObjTyeValue +'';
            param.dispObjTye = me.options.dispObjTye +'';
            param.btnFlag = me.options.btnFlag +'';
            operOrderAction.qrySrvOrdList(param,function (res) {
                if(res.flag == 1){
                    $("#circuitGrid").grid("reloadData", res.data);
                }else {
                    fish.toast('error', res.message);
                }
            });
        },
        //初始化附件表格
        initFileUpdate : function(woId) {
            $("#fileGrid").grid({
                colModel: [
                    {name: 'attachInfoId', label: '附件ID', hidden: true},
                    {name: 'fileName', label: '文件名称', width: 160, sortable: false },
                    {name: 'fileSize', label: '大小', width: 40 },
                    {name: 'fileType', label: '类型', width: 40 , sortable: false },
                    {name: 'action', label: '操作', width: 100, formatter: 'actions',
                        formatoptions: {
                            editbutton: false,
                            //delbutton: false,
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
                    operOrderAction.getAnnex(woId,function (data) {
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
                add: function(e, data) {
                    var fileObj = {};
                    var obj = data.files[0];
                    fileObj.fileName = obj.name.split(".")[0];
                    fileObj.fileSize = (obj.size / 1024.0).toFixed(2) + "KB";
                    fileObj.fileType = obj.name.split(".")[1];
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
                    $('.inline-remove').off('click').on('click', function (e) {
                        var rowid = $(this).closest("tr.jqgrow").attr("id");
                        var data = $("#fileGrid").grid("getRowData",rowid)
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
                    me.isSubmit = false;
                    if (data.result.success) {
                        $("#orderOper-form").unblockUI().data('blockui-content', false);
                        fish.toast('success', data.result.message);
                        me.popup.close();
                    }else {
                        $("#orderOper-form").unblockUI().data('blockui-content', false);
                        fish.toast('error', data.result.message);
                    }
                },
            });
        },
        //附件上传
        fileUpdate :function (params){
            FILES.url = URL+"/localScheduleLT/FlieUpdateController/uploadFiles.spr";
            FILES.formData = {
                params : JSON.stringify(params)
            }
            if (!this.isSubmit) {
                this.isSubmit = true;
                FILES.submit();
            } else {
                fish.toast('error', "请勿重复提交！");
            }
        },

    });
});