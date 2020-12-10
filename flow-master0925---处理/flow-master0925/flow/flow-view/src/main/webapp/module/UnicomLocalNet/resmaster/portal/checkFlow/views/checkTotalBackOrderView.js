define(['module/UnicomLocalNet/resmaster/portal/orderTacheDealView/action/operOrderAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'text!module/UnicomLocalNet/resmaster/portal/checkFlow/templates/checkTotalBackOrderView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderTacheDealView/styles/operOrderView.css'
], function(operOrderAction,orderStandbyAction,checkBackOrderView,i18n,css) {

    return fish.View.extend({
        template: fish.compile(checkBackOrderView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit'
        },

        initialize: function() {
            this.render();
            FILES = null; //附件
            userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data; //用户信息
            this.isSubmit = false;
            rollBackFlag = 'dispatchCheck';
            this.delFiles = new Array();
        },

        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },

        //初始化fish组件
        afterRender: function() {
            var me = this;
            this.initFish();
            //初始化电路信息
            this.circuitInfo();
            //初始化附件表格
            this.initFileUpdate(me.options.woId +'');
        },
        initFish:function () {
            var me = this;
            $('#rollBackResult').bind('click',function(){
                rollBackFlag = $("input[name='rollBackRadio']:checked").val();
                if (rollBackFlag == 'dispatchCheck') {
                    $("#backSpecialDiv").hide();
                }else if(rollBackFlag == 'specialCheck') {
                    $("#backSpecialDiv").show();
                    /*var data = $("#circuitGrid").grid("getSelection");
                    if(JSON.stringify(data) == "{}"){
                        data = $("#circuitGrid").grid("getRowData")[0];
                    }
                    me.qrySpecialData(data.ORDER_ID);*/
                }
            });
        },

        qrySpecialData : function(orderId){
            var me = this;
            operOrderAction.qrySpecialData(orderId + '',function (res) {
                if(res.success){
                    if (res.specialData.length == 0){
                        $('#inteFinish').attr("disabled",true);
                    } else {
                        $('#inteFinish').attr("disabled",false);
                        var divStr = '';
                        var idVal = '';
                        $.each(res.specialData,function(i,val){
                            divStr = divStr + '<label class="radio-inline">'+
                                '<input type="radio" name="special" id="' + val.ID
                                + '" value= "' + val.ID +'">'
                                + val.TACHE_NAME
                                +'</label>';
                            if (i == 0 ){
                                idVal = val.ID;
                            }
                        });
                        me.$("#backSpecialDataDiv").html("");
                        me.$("#backSpecialDataDiv").append(divStr);
                        $('#'+idVal).attr("checked","checked");
                    }
                }else {
                    fish.toast('error', res.message);
                }
            })
        },

        circuitInfo : function() {
            var me = this;
            $("#circuitGrid").grid({
                colModel: me.initGridInfo(),
                width: 516,
                multiselect: true,
                shrinkToFit: false,
                pageData: me.qrycircuitInfo(),
                onSelectRow: function (e, rowid, state, checked) {//选中行事件
                    var data = $("#circuitGrid").grid("getSelection");
                    me.qrySpecialData(data.ORDER_ID);
                },
                onSelectAll: function (e, status){ //全选事件

                },
                onCellSelect:function(e, rowid, iCol, cellcontent){

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
                {name: 'AREGIONNAME', label: 'A端所属区域', width: 110, sortable: false},
                {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110, sortable: false},
                {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
            ]
        },
        qrycircuitInfo : function(){
            var me = this;
            var param = {};
            param.cstOrdId = me.options.cstOrdId +'';
            param.woState = me.options.woState +'';
            param.tacheId = me.options.tacheId +'';
            param.dealUserId = userInfo.userId +'';
            param.dispObjTyeValue = me.options.dispObjTyeValue +'';
            param.dispObjTye = me.options.dispObjTye +'';
            operOrderAction.qrySrvOrdList(param, function (res) {
                if (res.flag = '1') {
                    $("#circuitGrid").grid("reloadData", res.data);
                    me.qrySpecialData(res.data[0].ORDER_ID);
                } else {
                    fish.toast('error', res.message);
                }
            });
        },

        submit:function () {
            var me = this;
            var formValue = $('#orderOper-form').form('value');
            var params = new Object();
            //var operAttrs = new Object();
            var circuitData = $("#circuitGrid").grid("getCheckRows");//订单信息
            if (circuitData.length == 0) {
                fish.warn('请至少选择一个电路信息！');
                return;
            }
            params.delFiles = me.dealFileData();
            if(formValue.remark == null){
                fish.error({title:'提示',message:'说明不能为空'});
                return;
            }
            //params.operAttrsVal = operAttrs;
            params.toTacheFlag = rollBackFlag;  //退单到哪个环节
            params.toSpecialTache = $("input[name='special']:checked").val(); //退单专业

            params.remark = formValue.remark;
            params.circuitData = circuitData;
            params.action = "checkTotalBackOrder";
            $("#orderOper-form").blockUI({message: '退单中...'}).data('blockui-content', true);
            if (FILES != null) { //如果附件非空先先上传附件
                me.fileUpdate(params);
            }else {
                me.submitOrder(params); //提交工单
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

        dealFileData : function(){
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
                    isSubmit = false;
                    if (data.result.success) {
                        me.submitOrder(data.submitInfo); //提交工单
                        fish.toast('success', data.result.message);
                        me.popup.close();
                    }else {
                        $("#orderOper-form").unblockUI().data('blockui-content', false);
                        fish.toast('error', data.result.message);
                    }
                }
            });
        },
        //附件上传
        fileUpdate :function (params){
            FILES.url = this.getRootPath() + "/localScheduleLT/FlieUpdateController/uploadFiles.spr";
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
        }

    });
});