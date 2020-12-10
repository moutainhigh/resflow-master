define([
    'text!module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/templates/mcpeInstallBackOrderView.html',
    'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/action/woOrderCloudAction',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/operOrderView.css'
], function (woOrderSubmitCloudView, woOrderCloudAction, operOrderAction, css) {
    return fish.View.extend({
        template: fish.compile(woOrderSubmitCloudView),
        //i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submitDetail'
        },

        initialize: function () {
            this.render();
            FILES = null; //附件
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
            //初始化电路信息
            me.circuitInfo();
            //初始化附件表格
            me.initFileUpdate(me.options.woId + '');
        },

        circuitInfo: function () {
            var me = this;
            $("#circuitGrid").grid({
                colModel: me.initGridInfo(),
                //width: 516,
                multiselect: true,
                shrinkToFit: false,
                pageData: me.qrycircuitInfo(),
                onSelectRow: function (e, rowid, state, checked) {//选中行事件
                    var data = $("#circuitGrid").grid("getSelection");
                },
                onSelectAll: function (e, status) { //全选事件

                },
            });
            $("#circuitGrid").grid("setGridHeight", 150);

        },
        initGridInfo: function () {
            return [
                {name: 'CIRCUITCODE', label: '电路编号', width: 100, sortable: false, editable:true },
                {name: 'BACK_SPECIALTY', label: '需要退单的专业区域', width: 180, sortable: false,
                    formatter : this.specialtyFormatter, unformat:this.specialtyUnFormat},
                {name: 'BACK_REMACK', label: '退单原因', width: 180, sortable: false,
                    editable:true,
                    //edittype:"textarea"
                    formatter : function() {
                        return '<textarea id="backRemark" name="backRemark" class="form-control" rows="3"></textarea>'
                    },
                    unformat:this.remarkUnFormat},
                {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                {name: 'SERIAL_NUMBER', label: '业务号码', width: 100 , sortable: false },
                {name: 'AREGIONNAME', label: 'A端所属区域', width: 110, sortable: false},
                {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110, sortable: false},
                {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
            ]
        },
        specialtyFormatter : function (cellvalue, options, rowObject){
            var me = this;
            var qryParams = new Object();
            qryParams.orderId = rowObject.ORDER_ID;
            qryParams.woId = rowObject.WO_ID;
            var res = woOrderCloudAction.qryMcpeInstallBackOrderData(qryParams).responseJSON.data;
            if(res.success){
                var divStr = '';
                $.each(res.dataList,function(i,val){
                    divStr = divStr + '<label class="checkbox-inline">'+
                        '<input type="checkbox" name="specialty" id="' + val.IDVALUE
                        + '" value= "' + val.IDVALUE +'">'
                        + val.NAMEVALUE
                        +'</label></br>';
                });
                return "<div>" + divStr + "</div>";
            }else {
                fish.toast('error', res.message);
            }

        },
        specialtyUnFormat: function(cellvalue, options, cell){
            //用于取formatter的值
            var specialtyList = [];
            $('input[name="specialty"]').each(function(){
                if($(this).is(':checked')==true){
                    specialtyList.push($(this).val());
                }
            });
            return specialtyList;
        },
        remarkUnFormat: function(cellvalue, options, cell){
            //用于取formatter的值
            return $('#backRemark').val();
        },

        qrycircuitInfo: function () {
            var me = this;
            var param = {};
            param.cstOrdId = me.options.cstOrdId + '';
            param.woState = me.options.woState + '';
            param.tacheId = me.options.tacheId + '';
            //param.dealUserId = userInfo.userId + '';
            param.dispObjTyeValue = me.options.dispObjTyeValue + '';
            param.dispObjTye = me.options.dispObjTye + '';
            //param.btnFlag = me.options.btnFlag + '';
            operOrderAction.qrySrvOrdList(param, function (res) {
                if (res.flag == 1) {
                    $("#circuitGrid").grid("reloadData", res.data);
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
                            delbutton: function (rowdata) {
                                if (rowdata.attachInfoId) {
                                    me.delFiles.push(rowdata.attachInfoId);
                                }
                                return true;
                            }
                        }
                    }
                ],
                //width: 516,
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
                var rowId = $("#circuitGrid").grid("getRowid", val);
                var specialtyList = $("#circuitGrid").grid("getCell", rowId, 'BACK_SPECIALTY');
                if(specialtyList == null || specialtyList == ""){
                    fish.error({title:'提示',message:'电路编号为：' + val.CIRCUITCODE + '的电路，需要退单的专业区域至少选择一个。。。'});
                    flag = true;
                    return;
                }
                var remark = $("#circuitGrid").grid("getCell", rowId, 'BACK_REMACK');
                if(remark == null || remark == ""){
                    fish.error({title:'提示',message:'电路编号为：' + val.CIRCUITCODE + '的电路，退单原因,请填写！！！'});
                    flag = true;
                    return;
                }
                val.specialty = specialtyList;
                val.remark = remark;
            });
            if (flag){
                return;
            }
            params.delFiles = me.dealFileData();
            params.circuitData = circuitData;
            params.buttonAction = "rollback";
            $.blockUI({message: '退单中...'});
            if (FILES != null) { //如果附件非空先先上传附件
                me.fileUpdate(params);
            } else {
                me.submitOrder(params); //提交工单
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
        }

    });
});