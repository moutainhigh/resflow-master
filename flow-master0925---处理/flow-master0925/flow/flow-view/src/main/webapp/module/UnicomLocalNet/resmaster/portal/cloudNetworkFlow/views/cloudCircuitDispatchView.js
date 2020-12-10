define([
    'text!module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/templates/cloudCircuitDispatchView.html',
    'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/action/woOrderCloudAction',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/operOrderView.css'
], function (woOrderSubmitCloudView, woOrderCloudAction, operOrderAction, css) {
    return fish.View.extend({
        template: fish.compile(woOrderSubmitCloudView),
        //i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submitDetail',
            'click #saveBtn': 'saveConfig'
        },

        initialize: function () {
            this.render();
            dispatchNum = '-1';
            FILES = null; //附件
            //areaPro = {};
            userInfo = this.options.userInfo; //用户信息
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
            var me = this;
            me.initFinsh();
            //初始化电路信息
            me.circuitInfo();
            //初始化附件表格
            me.initFileUpdate(me.options.woId + '');
        },

        initFinsh : function(){
            var me = this;
            //是否新建资源
            $("input[name = 'newResRadio']").bind('click',function(){
                var newResourceFlag = $("input[name='newResRadio']:checked").val();
                if (newResourceFlag == '0') { //是
                    $('#newResourceAreaDiv').show();
                    $('#masterSelectDiv').hide();
                    $('#constructDiv').hide();
                    $('#childSendDiv').hide();
                    $('#dispatchOrderDiv').hide();
                }else if(newResourceFlag == '1') { //否
                    $('#newResourceAreaDiv').hide();
                    $('#masterSelectDiv').show();
                    $('#constructDiv').show();
                    $('#childSendDiv').show();
                    $('#dispatchOrderDiv').show();
                }
                me.circuitInfo();
            });
            //是否需要网络施工
            $("input[name = 'constructRadio']").bind('click',function(){
                var constructFlag = $("input[name='constructRadio']:checked").val();
                if (constructFlag == '0') { //是
                    //$('#ipranDiv').show();
                    $('#opticalDiv').show();
                }else if(constructFlag == '1') { //否
                    //$('#ipranDiv').hide();
                    $('#opticalDiv').hide();
                    $("#YZW_MCPE_Checkbox").prop("checked", true);
                }
            });
            //区域选择框
            $("input[name$='Popedit']").popedit({
                initialData: {
                    'name': '请选择派发区域！',
                    'value': ''
                },
                open:function(e) {
                    var _this = $(this);
                    // var key=_this.get(0).id;
                    // var _array = new Array;
                    // if(key == 'ipran_aCfgPopedit'){
                    //     key = 'AREA_0';
                    //     var _value = $('#ipran_aCfgPopedit').popedit('getValue');
                    //     if(_value != null){
                    //         _value = _value.value+'';
                    //         _array = _value.split(",");
                    //     }
                    // }
                    // areaPro[key] = _array;
                    var options = {
                        //url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/operTreeOrderView',
                        //url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/masterTreeView',
                        url: 'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/views/cloudAreaTreeView',
                        height: 350,
                        width: 400,
                        modal: false,
                        draggable: false,
                        autoResizable: true,
                        viewOption: {
                            deptId : userInfo.orgId,
                            /*flag : "org",
                            orderId : me.options.orderId,
                            key : key,*/
                            //nodeValues :  areaPro[key]
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                var orgNames = '';
                                var orgIds = '';
                                res.forEach(function(val,i){
                                    if(orgIds==''){
                                        orgNames = val.name ;
                                        orgIds = val.id;
                                    } else {
                                        orgNames = orgNames + ',' + val.name ;
                                        orgIds = orgIds + ',' + val.id;
                                    }
                                });
                                _this.popedit('setValue', {name:orgNames, value:orgIds});
                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    };
                    var popup = fish.popupView(options);
                }
            });
        },

        circuitInfo: function () {
            var me = this;
            var tacheId = me.options.tacheId;
            $("#circuitGrid").grid({
                colModel: me.initGridInfo(),
                //width: 516,
                multiselect: true,
                shrinkToFit: false,
                pageData: me.qrycircuitInfo(),
                onSelectRow: function (e, rowid, state, checked) {//选中行事件
                    var rowsData = $('#circuitGrid').grid("getCheckRows");
                    if(rowsData.length > 0) {
                        //判断勾选的电路是否包含不同的调单，如果是则给出提示
                        var dispatchSet = new Set();
                        var isShow = true; //是否回填原调单内容true:回填，false:不回填
                        for (var i = 0; i < rowsData.length; i++) {
                            var dispatchId = rowsData[i].DISPATCH_ORDER_ID;
                            if (dispatchId != '' && dispatchId != undefined) {
                                dispatchSet.add(dispatchId);
                            }else if(dispatchId == ''){
                                isShow = false;
                            }
                        }
                        //如果勾选的电路只包含一条调单信息，且没有未处理的电路，则回填原调单内容
                        //如果勾选的点路只包含一条调单信息，且包含未处理的电路，则重新生成调单信息
                        if (dispatchSet.size <= 1) {
                            me.queryDispatchInfoByDispatchIds(rowsData, isShow);
                        } else { //如果勾选的电路包含多条调单信息时，给出提示
                            fish.warn('您所勾选的电路包含多条调单信息，请检查！');
                        }
                    }
                },
                onSelectAll: function (e, status) { //全选事件
                    var rowsData = $('#circuitGrid').grid("getCheckRows");
                    if(rowsData.length > 0) {
                        //判断勾选的电路是否包含不同的调单，如果是则给出提示
                        var dispatchSet = new Set();
                        var isShow = true; //是否回填原调单内容true:回填，false:不回填
                        for (var i = 0; i < rowsData.length; i++) {
                            var dispatchId = rowsData[i].DISPATCH_ORDER_ID;
                            if (dispatchId != '' && dispatchId != undefined) {
                                dispatchSet.add(dispatchId);
                            }else if(dispatchId == ''){
                                isShow = false;
                            }
                        }
                        //如果勾选的电路只包含一条调单信息，且没有未处理的电路，则回填原调单内容
                        //如果勾选的点路只包含一条调单信息，且包含未处理的电路，则重新生成调单信息
                        if (dispatchSet.size <= 1) {
                            me.queryDispatchInfoByDispatchIds(rowsData, isShow);
                        } else { //如果勾选的电路包含多条调单信息时，给出提示
                            fish.warn('您所勾选的电路包含多条调单信息，请检查！');
                        }
                    }
                },
            });
            $("#circuitGrid").grid("setGridHeight", 150);

        },
        initGridInfo: function () {
            return [
                {name: 'STATE', label: $("input[name='newResRadio']:checked").val()  == 0 ? '区域配置' : '专业配置', width: 95, sortable: false,
                    formatter: function(cellval, opts, rwdat, _act) {
                        if(cellval === '已配置'){
                            return '<div class="btn-group">' +
                                '<button type="button" class="btn btn-link js-delete" style="color: #6DCC4A">'+cellval+'</button>' +
                                '</div>';
                        }
                        return '<div class="btn-group btn btn-link" style="color: #FF5858">'+cellval +
                            '</div>';
                    }},
                {name: 'CIRCUITCODE', label: '电路编号', width: 100, sortable: false, editable:true },
                {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                {name: 'SERIAL_NUMBER', label: '业务号码', width: 100 , sortable: false },
                {name: 'AREGIONNAME', label: 'A端所属区域', width: 110, sortable: false},
                {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110, sortable: false},
                {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
            ]
        },
        qrycircuitInfo: function () {
            var me = this;
            // var psIds = '1000248,1000249'; //子流程
            // var psId = me.options.psId;
            var param = {};
            param.cstOrdId = me.options.cstOrdId + '';
            param.woState = me.options.woState + '';
            param.tacheId = me.options.tacheId + '';
            //param.dealUserId = userInfo.userId + '';
            param.dispObjTyeValue = me.options.dispObjTyeValue + '';
            param.dispObjTye = me.options.dispObjTye + '';
            param.newCreateResource = $("input[name='newResRadio']:checked").val();
            //param.btnFlag = me.options.btnFlag + '';
            operOrderAction.qrySrvOrdList(param, function (res) {
                if (res.flag == 1) {
                    $("#circuitGrid").grid("reloadData", res.data);
                    me.qryCircuitAreaInfo(res.data[0]);
                } else {
                    fish.toast('error', res.message);
                }
            });
        },

        //初始化附件表格
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

        submitDetail: function () {
            var me = this;
            var params = new Object();
            var circuitData = $("#circuitGrid").grid("getCheckRows");//订单信息
            if (circuitData.length <= 0){
                fish.toast('warn', "请至少勾选一条电路处理！");
                return false;
            }
            var flag = false;
            $.each(circuitData,function(i,val){
                if (val.STATE == '未配置') {
                    flag = true;
                }
            });
            if (flag){
                fish.toast('warn', "勾选电路存在未配置数据，请重新勾选或者先配置！");
                return false;
            }
            var formValue = $('#cloudSubmit-form').form('value');
            var operAttrs = new Object();
            operAttrs.remark = formValue.remark;
            operAttrs.newResRadio = formValue.newResRadio;
            //调单信息
            var dispatchOrderData = {};
            dispatchOrderData.dispatchOrderNum = formValue.dispatchOrderNum;
            dispatchOrderData.dispatchOrderName = formValue.dispatchOrderName;
            dispatchOrderData.dispatchOrderText = formValue.dispatchOrderText;
            params.dispatchOrderData = dispatchOrderData;

            params.operAttrs = operAttrs;
            params.circuitData = circuitData;
            params.delFiles = me.dealFileData();
            params.buttonAction = "submit";
            $.blockUI({message: '派单中...'});
            if (FILES != null) { //如果附件非空先先上传附件
                me.fileUpdate(params);
            } else {
                me.submitOrder(params); //提交工单
            }
        },

        //提交工单
        submitOrder: function (params) {
            var me = this;
            if (!isSubmit) {
                isSubmit = true;
                woOrderCloudAction.submitWoOrder(params, function (res) {
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

        getSequenceNum :function() {
            //获取电路信息表中的第一条数据的SRV_ORD_ID
            var data = $("#circuitGrid").grid("getRowData")[0];
            var me = this;
            // var serviceId = me.options.serviceId + '';
            var cstOrdId = me.options.cstOrdId + '';
            var param = {};
            param.cstOrdId = cstOrdId;
            param.srvOrdId = data.SRV_ORD_ID;
            param.orderId = data.ORDER_ID;
            // param.orderIds = orderIds;
            operOrderAction.getsequenceNum(param,function (res) {
                if(res.success){
                    var date = new Date();
                    $('#dispatchOrderNum').attr('value',res.sign + '[' + date.getFullYear() + ']'+ res.message +'号');
                    $('#dispatchOrderNum').attr('disabled',true);
                    dispatchNum = $('#dispatchOrderNum').val();
                }else {
                    fish.toast('error', res.message);
                }
            });
        },

        //根据调单IDs查询调单信息
        queryDispatchInfoByDispatchIds : function(rowsData, isShow){
            var me = this;
            var param = {};
            param.rowsData = rowsData;
            var dispatchOrderId = '';
            operOrderAction.queryDispatchInfoByDispatchIds(param,function(data){
                if (data.success) {
                    if (data.dispatchInfo.length > 0) {
                        //回显调单编号
                        $('#dispatchOrderNum').attr('value',data.dispatchInfo[0].DISPATCH_ORDER_NO);
                        $('#dispatchOrderNum').attr('disabled',true);
                        dispatchOrderId = data.dispatchInfo[0].DISPATCH_ORDER_ID;
                        if(isShow){
                            //勾选电路只包含一条调单，且不包含未处理的电路，回显原有调单信息
                            $('#dispatchOrderName').val(data.dispatchInfo[0].DISPATCH_TITLE);
                            $('#dispatchOrderText').val(data.dispatchInfo[0].DISPATCH_TEXT)
                        }else{
                            //勾选电路只包含一条调单，且包含未处理的电路时，重新生成调单内容
                            //刷新调单标题和调单内容
                            me.refreshDispatchTitle(rowsData,dispatchOrderId);
                        }
                    }else{
                        //如果勾选的电路没有调单信息则需要重新生成调单编号
                        if (dispatchNum == '-1'){
                            me.getSequenceNum();
                            //填充调单标题和调单内容
                            me.refreshDispatchTitle(rowsData,dispatchOrderId);
                        }else{
                            $('#dispatchOrderNum').attr('value',dispatchNum);
                            $('#dispatchOrderNum').attr('disabled',true);
                            //填充调单标题和调单内容
                            me.refreshDispatchTitle(rowsData,dispatchOrderId);
                        }

                    }
                }else {
                    fish.toast('error', data.message);
                }
            });
        },

        queryDispatchOrderInfoByDispatchId : function (dispatchOrderId) {
            var param = {};
            param.dispatchId = dispatchOrderId;
            orderDetailsAction.queryDispatchOrderInfoByDispatchId(param,function (res) {
                if(res != null){
                    $('#dispatchOrderNum').attr('value',res[0].DISPATCH_ORDER_NO);
                    $('#dispatchOrderNum').attr('disabled',true);
                    $('#dispatchOrderName').attr('value',res[0].DISPATCH_TITLE);
                    $('#dispatchOrderText').val(res[0].DISPATCH_TEXT);
                }
            });
        },

        //勾选数据时刷新调单内容
        refreshDispatchTitle: function(rowsData,dispatchOrderId){
            var me = this;
            var cstOrdId = me.options.cstOrdId + '';
            var param = {};
            param.cstOrdId = cstOrdId;
            param.rowsData = rowsData;
            param.dispatchOrderId = dispatchOrderId;
            //查询勾选电路的相关信息拼接调单内容

            // var orderText ='';
            // for (i=0; i < data.length; i++) {
            //     orderText = orderText + (i+1)+'、电路编号：'+ data[i].CIRCUITCODE +'\n业务号码：'+data[i].SERIAL_NUMBER +'\n业务订单号：'+data[i].TRADE_ID +'\nA端所属区域：'+data[i].AREGIONNAME+
            //         '\nZ端所属区域：'+ data[i].ZREGIONNAME + '\n';
            // }
            // orderText =  orderText+ '由主调局负责对传输电路进行端到端BER测试，相关分公司配合。\n' +
            //     '传输质量符合要求后，由相关分公司按电路调令要求时限进行反馈，并通知本公司业务受理部门。测试过程中如有问题，请及时反馈网管中心。'
            // $('#dispatchOrderText').val(orderText);
            operOrderAction.getDispatchInfo(param,function(res){
                if (res.success) {
                    //填调单标题
                    if (res.productType == "局内中继电路 "){
                        // $('#dispatchOrderName').attr('value', '关于【' + res.operateType + '】【' + data.length + '条】【' + res.productType + '】的通知');
                        $('#dispatchOrderName').val('关于【' + res.operateType + '】【' + res.countNum + '条】【' + res.productType + '】的通知');
                    }
                    else{
                        // $('#dispatchOrderName').attr('value', '关于【' + res.custName + '】【' + res.operateType + '】【' + data.length + '条】【' + res.productType + '】的通知');
                        $('#dispatchOrderName').val('关于【' + res.custName + '】【' + res.operateType + '】【' + res.countNum + '条】【' + res.productType + '】的通知');
                    }
                    //填调单内容
                    var orderText ='';
                    for (i=0; i < res.dispatchTextList.length; i++) {
                        orderText = orderText + (i + 1) + '、';
                        if (res.dispatchTextList[i].CIRCUITCODE) {
                            orderText += '电路编号：' + res.dispatchTextList[i].CIRCUITCODE + ';\n';
                        }
                        if (res.dispatchTextList[i].SERIAL_NUMBER) {
                            orderText += '业务号码：' + res.dispatchTextList[i].SERIAL_NUMBER + ';\n';
                        }
                        if (res.dispatchTextList[i].TRADE_ID) {
                            orderText += '业务订单号：' + res.dispatchTextList[i].TRADE_ID + ';\n';
                        }
                        if (res.dispatchTextList[i].AREGION) {
                            orderText += 'A端所属区域：' + res.dispatchTextList[i].AREGION + ';\n';
                        }
                        if (res.dispatchTextList[i].ZREGION) {
                            orderText += 'Z端所属区域：' + res.dispatchTextList[i].ZREGION + ';\n';
                        }

                    }
                    orderText =  orderText+ '由主调局负责对传输电路进行端到端BER测试，相关分公司配合。\n' +
                        '传输质量符合要求后，由相关分公司按电路调令要求时限进行反馈，并通知本公司业务受理部门。测试过程中如有问题，请及时反馈网管中心。'
                    $('#dispatchOrderText').val(orderText);
                }else{
                    fish.toast('error', res.message);
                }
            });
        },

        saveConfig : function (){
            var me = this;
            var data = $("#circuitGrid").grid("getCheckRows");
            if (data.length > 0){
                me.savePropertyConfig();
            }else{
                var newResourceFlag = $("input[name='newResRadio']:checked").val();
                if (newResourceFlag == '0') {
                    fish.toast('warn', "请至少勾选一条电路信息，并配置区域!");
                }else if(newResourceFlag == '1') {
                    fish.toast('warn', "请至少勾选一条电路信息，并配置专业!");
                }
            }
        },

        //电路调度环节保存电路的专业配置信息
        savePropertyConfig : function(){
            var me = this;
            var param = {};
            //主调局信息
            var formValue = $('#cloudSubmit-form').form('value');
            var childFlowSpecialMap = null;
            var newResourceFlag = $("input[name='newResRadio']:checked").val();
            var specialtyConfig = {};
            var specialtyConfigName = {};
            if (newResourceFlag == '0') {
                var newResourceCfg = $('#newResource_CfgPopedit').popedit('getValue');
                if(newResourceCfg.value != "" && newResourceCfg != undefined){
                    specialtyConfig.YZW_NEWRES = newResourceCfg.value;
                    specialtyConfigName.YZW_NEWRES = newResourceCfg.name;
                }else {
                    fish.toast('warn', "新建资源录入至少选择一个派发区域！");
                    return false;
                }
            }else if(newResourceFlag == '1') {
                var specialtyConfig_A = {};
                var specialtyConfigName_A = {};
                var specialtyConfig_Z = {};
                var specialtyConfigName_Z = {};
                param.masterValue = formValue.masterSelectRadio;
                var specialtyArr = formValue.specialtyCheckbox;
                var constructFlag = $("input[name='constructRadio']:checked").val();
                if (constructFlag == '1') {
                    //如果不需要网络施工，这里只需要选择终端盒的派发区域
                    specialtyArr[0] = true;
                    specialtyArr[1] = true;
                    specialtyArr[2] = 'YZW_MCPE';
                }
                if (!specialtyArr[0] && !specialtyArr[1] && !specialtyArr[2]) {
                    fish.toast('warn', "派发专业至少选择一个！");
                    return false;
                }else {
                    var flag = false;
                    $.each(specialtyArr,function(i,val){
                        if (val){
                            switch(val) {
                                case 'YZW_OPTICAL':
                                    var opticalCfgA = $('#optical_aCfgPopedit').popedit('getValue');
                                    if(opticalCfgA.value != "" && opticalCfgA != undefined){
                                        specialtyConfig_A.YZW_OPTICAL = opticalCfgA.value;
                                        specialtyConfigName_A.YZW_OPTICAL = opticalCfgA.name;
                                    }else {
                                        flag = true;
                                        fish.toast('warn', "光纤专业A端至少选择一个派发区域！");
                                        return false;
                                    }
                                    var opticalCfgZ = $('#optical_zCfgPopedit').popedit('getValue');
                                    if(opticalCfgZ.value != "" && opticalCfgZ != undefined){
                                        specialtyConfig_Z.YZW_OPTICAL = opticalCfgZ.value;
                                        specialtyConfigName_Z.YZW_OPTICAL = opticalCfgZ.name;
                                    }else {
                                        flag = true;
                                        fish.toast('warn', "光纤专业Z端至少选择一个派发区域！");
                                        return false;
                                    }
                                    break;
                                case 'YZW_IPRAN':
                                    var ipranCfgA = $('#ipran_aCfgPopedit').popedit('getValue');
                                    if(ipranCfgA.value != "" && ipranCfgA != undefined){
                                        specialtyConfig_A.YZW_IPRAN = ipranCfgA.value;
                                        specialtyConfigName_A.YZW_IPRAN = ipranCfgA.name;
                                    }else {
                                        flag = true;
                                        fish.toast('warn', "传输ipran专业A端至少选择一个派发区域！");
                                        return false;
                                    }
                                    var ipranCfgZ = $('#ipran_zCfgPopedit').popedit('getValue');
                                    if(ipranCfgZ.value != "" && ipranCfgZ != undefined){
                                        specialtyConfig_Z.YZW_IPRAN = ipranCfgZ.value;
                                        specialtyConfigName_Z.YZW_IPRAN = ipranCfgZ.name;
                                    }else {
                                        flag = true;
                                        fish.toast('warn', "传输ipran专业Z端至少选择一个派发区域！");
                                        return false;
                                    }
                                    break;
                                case 'YZW_MCPE':
                                    var mcpeCfgA = $('#mcpe_aCfgPopedit').popedit('getValue');
                                    if(mcpeCfgA.value != "" && mcpeCfgA != undefined){
                                        specialtyConfig_A.YZW_MCPE = mcpeCfgA.value;
                                        specialtyConfigName_A.YZW_MCPE = mcpeCfgA.name;
                                    }else {
                                        flag = true;
                                        fish.toast('warn', "终端盒A端至少选择一个派发区域！");
                                        return false;
                                    }
                                    var mcpeCfgZ = $('#mcpe_zCfgPopedit').popedit('getValue');
                                    if(mcpeCfgZ.value != "" && mcpeCfgZ != undefined){
                                        specialtyConfig_Z.YZW_MCPE = mcpeCfgZ.value;
                                        specialtyConfigName_Z.YZW_MCPE = mcpeCfgZ.name;
                                    }else {
                                        flag = true;
                                        fish.toast('warn', "终端盒Z端至少选择一个派发区域！");
                                        return false;
                                    }
                                    break;
                            }
                        }
                    });
                }
                if (flag){
                    return false;
                }
                specialtyConfig.specialtyConfig_A = specialtyConfig_A;
                specialtyConfig.specialtyConfig_Z = specialtyConfig_Z;
                specialtyConfigName.specialtyConfigName_A = specialtyConfigName_A;
                specialtyConfigName.specialtyConfigName_Z = specialtyConfigName_Z;
                param.constructFlag = constructFlag;
            }
            document.getElementById("saveBtn").setAttribute("disabled", true);
            param.specialtyConfig = specialtyConfig;
            param.specialtyConfigName = specialtyConfigName;
            var childFlowSpecialMap = {};
            childFlowSpecialMap.specialtyConfig = specialtyConfig;
            childFlowSpecialMap.specialtyConfigName = specialtyConfigName;
            param.flowSpecialData = childFlowSpecialMap;
            param.dataInfo = $("#circuitGrid").grid("getCheckRows");
            param.newCreateResource = newResourceFlag;
            operOrderAction.saveSpecialtyConfigInfo(param,function(res){
                document.getElementById("saveBtn").removeAttribute("disabled");
                if (res.success){
                    //专业配置保存完成后，重新刷新电路信息表格数据
                    me.circuitInfo();
                    fish.toast('success', res.message);
                } else{
                    fish.toast('error', res.message);
                }
            });
        },

        qryCircuitAreaInfo : function(param) {
            var srvOrdId = param.SRV_ORD_ID + '';
            var me = this;
            operOrderAction.qryCircuitAreaInfo(srvOrdId,function (res) {
                var divStr = "";
                if (res.PORT == 'A') {
                    if (res.AREAID != res.AREGIONID){
                        divStr = '<label class="radio-inline" id="AREGIONNAME">'
                            + '<input type="radio" name="masterSelectRadio" id="AREGIONNAME" value="'+ res.AREGIONID +'">'
                            + res.AREGIONNAME + '</label>';
                    }
                    divStr += '<label class="radio-inline" id="AREANAME">'
                        + '<input type="radio" name="masterSelectRadio" id="AREANAME" checked="true" value="'+ res.AREAID +'">'
                        +  res.AREANAME + '</label>';
                } else if (res.PORT == 'Z') {
                    if (res.AREAID != res.ZREGIONID){
                        divStr = '<label class="radio-inline" id="ZREGIONNAME">'
                            + '<input type="radio" name="masterSelectRadio" id="ZREGIONNAME" value="'+ res.ZREGIONID +'">'
                            + res.ZREGIONNAME + '</label>';
                    }
                    divStr += '<label class="radio-inline" id="AREANAME">'
                        + '<input type="radio" name="masterSelectRadio" id="AREANAME" checked="true" value="'+ res.AREAID +'">'
                        +  res.AREANAME + '</label>';
                } else if (res.PORT == 'ALL') {
                    if (res.AREAID != res.AREGIONID){
                        divStr = '<label class="radio-inline" id="AREGIONNAME">'
                            + '<input type="radio" name="masterSelectRadio" id="AREGIONNAME" value="'+ res.AREGIONID +'">'
                            + res.AREGIONNAME + '</label>';
                    }
                    if(res.AREAID != res.ZREGIONID){
                        divStr += '<label class="radio-inline" id="ZREGIONNAME">'
                            + '<input type="radio" name="masterSelectRadio" id="ZREGIONNAME" value="'+ res.ZREGIONID +'">'
                            + res.ZREGIONNAME + '</label>';
                    }
                    divStr += '<label class="radio-inline" id="AREANAME">'
                        + '<input type="radio" name="masterSelectRadio" id="AREANAME" checked="true" value="'+ res.AREAID +'">'
                        +  res.AREANAME + '</label>';
                } else {
                    divStr = '<label class="radio-inline" id="AREANAME">'
                        + '<input type="radio" name="masterSelectRadio" id="AREANAME" checked="true" value="'+ res.AREAID +'">'
                        +  res.AREANAME + '</label>';
                }
                $("#masterSelectAZProtDiv").html("");
                $("#masterSelectAZProtDiv").append(divStr);
                //将回显方法写入到主调局方法里，是为了避免回显主调局数据后，主调局数据会再次被初始化覆盖
                if (param.STATE =='已配置') {
                    $("input[name=constructRadio]").prop("checked",false);
                    $("input[name=masterSelectRadio]").prop("checked",false);
                    me.queryPropertyConfig(param)
                }else {
                    $("#YZW_OPTICAL_Checkbox").prop("checked", true);
                    $("#YZW_IPRAN_Checkbox").prop("checked", true);
                    $("#YZW_MCPE_Checkbox").prop("checked", true);
                }
            });
        },

        //回显电路对应的专业配置信息
        queryPropertyConfig:function(data){
            var me = this;
            var param = {};
            param.srvOrdId = data.SRV_ORD_ID;
            param.cstOrdId = data.CST_ORD_ID;
            param.orderId = data.ORDER_ID;
            param.activeType = data.ACTIVE_TYPE;
            param.newCreateResource = $("input[name='newResRadio']:checked").val();
            operOrderAction.queryPropertyConfig(param,function(res){
                if (res.success){
                    if (res.configInfo != null){
                        //回显主辅调
                        var keyNote = res.keyNote;
                        $("input[name='masterSelectRadio']").each(function(){
                            if($(this).val() == keyNote){
                                $("input[name=masterSelectRadio][value= "+keyNote+"]").prop("checked",true);
                            }
                        });
                        //回显是否施工
                        var constructFlag = res.constructFlag;
                        $("input[name='constructRadio']").each(function(){
                            if($(this).val() == constructFlag){
                                $("input[name=constructRadio][value= "+constructFlag+"]").prop("checked",true);
                            }
                        }); if (constructFlag == '0') { //是
                            //$('#ipranDiv').show();
                            $('#opticalDiv').show();
                        }else if(constructFlag == '1') { //否
                            //$('#ipranDiv').hide();
                            $('#opticalDiv').hide();
                        }
                        //回显派发区域
                        if (res.configInfo.specialtyConfig_A != null){
                            //回显光纤,传输ipran,终端盒的派发区域
                            $.each(res.configInfo.specialtyConfig_A,function(i,val){
                                switch(i) {
                                    case 'YZW_IPRAN':
                                        $("#YZW_IPRAN_Checkbox").prop("checked", true);
                                        $("#ipran_aCfgPopedit").popedit('setValue', {name:res.configInfoName.specialtyConfigName_A.YZW_IPRAN, value:val});
                                        break;
                                    case 'YZW_OPTICAL':
                                        $("#YZW_OPTICAL_Checkbox").prop("checked", true);
                                        $("#optical_aCfgPopedit").popedit('setValue', {name:res.configInfoName.specialtyConfigName_A.YZW_OPTICAL, value:val});
                                        break;
                                    case 'YZW_MCPE':
                                        $("#YZW_MCPE_Checkbox").prop("checked", true);
                                        $("#mcpe_aCfgPopedit").popedit('setValue', {name:res.configInfoName.specialtyConfigName_A.YZW_MCPE, value:val});
                                        break;
                                }
                            });
                            $.each(res.configInfo.specialtyConfig_Z,function(i,val){
                                switch(i) {
                                    case 'YZW_IPRAN':
                                        //$("#YZW_IPRAN_Checkbox").prop("checked", true);
                                        $("#ipran_zCfgPopedit").popedit('setValue', {name:res.configInfoName.specialtyConfigName_Z.YZW_IPRAN, value:val});
                                        break;
                                    case 'YZW_OPTICAL':
                                        //$("#YZW_OPTICAL_Checkbox").prop("checked", true);
                                        $("#optical_zCfgPopedit").popedit('setValue', {name:res.configInfoName.specialtyConfigName_Z.YZW_OPTICAL, value:val});
                                        break;
                                    case 'YZW_MCPE':
                                        //$("#YZW_IPRAN_Checkbox").prop("checked", true);
                                        $("#mcpe_zCfgPopedit").popedit('setValue', {name:res.configInfoName.specialtyConfigName_Z.YZW_MCPE, value:val});
                                        break;
                                }
                            });
                        }else {
                            //回显新建资源录入的区域
                            $.each(res.configInfo,function(i,val){
                                switch(i) {
                                    case 'YZW_NEWRES':
                                        $("#newResource_CfgPopedit").popedit('setValue', {name:res.configInfoName.YZW_NEWRES, value:val});
                                        break;
                                }
                            });
                        }
                    }
                } else{
                    fish.toast('error', res.message);
                }
            });
        }
    });
});