define(['module/UnicomLocalNet/resmaster/portal/orderTacheDealView/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/orderTacheDealView/templates/orderChargeBackView.html',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderTacheDealView/styles/operOrderView.css'
], function(operOrderAction,orderChangeBackView,orderStandbyAction,i18n,css) {

    var meBack;
    return fish.View.extend({
        template: fish.compile(orderChangeBackView),
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
            meBack = this;
            var orderId = me.options.orderId + '';
            this.initFish();
            //初始化电路信息
            this.circuitInfo();
            //初始化附件表格
            this.initFileUpdate(me.options.woId +'');

            $('#backReasonType').combobox({
                placeholder: '--请选择退单类型--',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    /*
                         {name: '产品类型错误-整体退单', value: '03'},
                         {name: '带宽错误-整体退单', value: '04'},
                         {name: '其它信息错误-整体退单', value: '05'},
                         {name: '联系人错误-单端退单', value: '06'},
                         {name: '装机地址错误-单端退单', value: '07'},
                         {name: '接口类型错误-单端退单', value: '08'},
                         {name: '带宽错误-单端退单', value: '17'},
                         {name: '其它错误-单端退单', value: '18'},   */
                    {name: '一干退单-整体退单', value: '01'},
                    {name: 'IP地址错误-一干退单', value: '09'},
                    {name: '掩码错误-一干退单', value: '10'},
                    {name: '广播范围错误-一干退单', value: '11'},
                    {name: '其他错误-一干退单', value: '12'},
                    {name: '二干退单-整体退单', value: '02'},
                    {name: 'IP地址错误-二干退单', value: '13'},
                    {name: '掩码错误-二干退单', value: '14'},
                    {name: '广播范围错误-二干退单', value: '15'},
                    {name: '其他错误-二干退单', value: '16'},
                ]
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

            //modify by wang.gang2   并行核查在核查调度环节回退政企中台 需要增加退单类型、退单范围
            var queryInfo = {};
            queryInfo.srvOrdId = srvOrdId;
            queryInfo.attrCode = 'ORD10222'; //判断并行流程标识
            var attrInfo = operOrderAction.queryAttrInfos(queryInfo).responseJSON.data;
            if ('' != attrInfo && null != attrInfo
                &&  '2' == attrInfo[0].ATTR_VALUE) {
                $("#backType").show();
                $("#backType").addClass("requireds");
            }

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
            if (tacheId == '510101080'){ //二干客户局内电路--二干调度环节
                var flag = false;
                circuitData.forEach(function(val,i){
                    var num = operOrderAction.qryChildOrderDealing(val.ORDER_ID + '').responseJSON.data;
                    if (num > 0){
                        flag = true;
                        return false;
                    }
                });
                if (flag) {
                    fish.toast('warn', '所选电路有正在执行的子流程不能退单！');
                    return;
                }
            }
            var operAttrs = new Object();//线条参数
            var tacheOperInfo = new Object();//环节操作数据信息
            var actionFlag = "complateWo";
            params.flag = "LOCAL";
            if(formValue.remark == null){
                fish.error({title:'提示',message:'说明不能为空'});
                return;
            }
            //流程线条参数
            if (tacheId == "510101080"){ //二干调度环节 --[二干电路]客户，局内电路流程
                operAttrs.SCHEDULE_REVIEW = 1;
                params.action = "submit"; //回绕线退单
            }else if (tacheId == "510101052"){ //核查调度
                operAttrs.Is_Return = 1;
                params.action = "submit"; //回绕线退单
            }else {
                params.action = btnFlag; //异常节点退单
                params.remark = formValue.remark;
            }
            tacheOperInfo.remark = formValue.remark;
            params.backReasonType = formValue.backReasonType;
            params.circuitData = circuitData;
            params.operAttrsVal = operAttrs;
            params.tacheOperInfo = tacheOperInfo;
            params.actionFlag = actionFlag;
            params.serviceId = me.options.serviceId;
            //params.action = btnFlag;
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
                    var num = operOrderAction.qryChildOrderDealing(data.ORDER_ID + '').responseJSON.data;
                    if (num > 0){
                        fish.toast('warn', '改电路有正在执行的子流程不能退单！');
                    }
                },
                onSelectAll: function (e, status){ //全选事件

                },
                onCellSelect:function(e, rowid, iCol, cellcontent){
                    // debugger;
                    var data = $("#circuitGrid").grid("getRowData",rowid);
                    //当iCol为0时，选中的是复选框而不是行数据，则不触发数据回显事件
                    if(0 != iCol){
                        if('500001155' == tacheId || '500001157' == tacheId){
                            me.initSelectSave(data.ORDER_ID);
                        }
                    }

                },
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
            var specialtyCode = me.options.specialtyCode;
            var param = {};
            param.cstOrdId = me.options.cstOrdId +'';
            param.woState = me.options.woState +'';
            param.tacheId = me.options.tacheId +'';
            param.dealUserId = userInfo.userId +'';
            param.dispObjTyeValue = me.options.dispObjTyeValue +'';
            param.dispObjTye = me.options.dispObjTye +'';
            param.btnFlag = me.options.btnFlag +'';
            if (specialtyCode != '' && specialtyCode != null) {
                param.specialtyCode = specialtyCode ? specialtyCode : '';
            }
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