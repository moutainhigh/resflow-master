define(['module/UnicomLocalNet/resmaster/portal/orderTacheDealView/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/templates/orderTransferView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderTacheDealView/styles/operOrderView.css'
], function(operOrderAction,orderTransferView,i18n,css) {

    return fish.View.extend({
        template: fish.compile(orderTransferView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
        },

        initialize: function() {
            this.render();
            URL = "";
            FILES = null; //附件
            userInfo = this.options.userInfo; //用户信息
        },

        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
            this.$el.html(this.template({
                TITLE: this.options.title
            }));
        },

        //初始化fish组件
        afterRender: function() {
            URL=this.getRootPath();
            //初始化电路信息
            this.circuitInfo();
            //初始化附件表格
            this.initFileUpdate();
            //初始化转派人员选择框
            this.initTranStaffPopedit();

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
        //初始化转派人员选择框
        initTranStaffPopedit: function () {
            $("#tranStaffPopedit").popedit({
                initialData: {
                    'name': '请选择转派对象！',
                    'value': ''
                },
                open: function (e) {
                    var _this = $(this);
                    var options = {
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/transferView',
                        height: 460,
                        width: 700,
                        modal: false,
                        draggable: false,
                        autoResizable: true,
                        viewOption: {
                            //flag : "transferStaff",
                            currentAreaId: userInfo.areaId,
                            currentOrgId: userInfo.orgId,
                            currentUserId: userInfo.userId
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                var orgNames = '';
                                var orgIds = '';
                                var objType = '';
                                //    var orgIds = new Array();
                                res.forEach(function (val, i) {
                                    if (i == 0) {
                                        orgNames = val.name;
                                    }
                                    orgIds = val.id;
                                    objType = val.objType;
                                })
                                _this.popedit('setValue', {name: orgNames, value: orgIds, objType: objType});
                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    };
                    var popup = fish.popupView(options);
                }

            });
        },

        submit:function () {
            var me = this;
            var obj = $("#tranStaffPopedit").popedit('getValue');
            var formValue = $('#orderOper-form').form('value');
            var circuitData = $("#circuitGrid").grid("getCheckRows");
            if (circuitData.length == 0) {
                fish.warn('请至少选择一个电路信息！');
                return;
            }
            var circuitData = $("#circuitGrid").grid("getRowData");//订单信息
            var params = new Object();
            params.circuitData = circuitData; //订单信息
            params.remark = formValue.remark;
            //params.staffId = obj.value;
            params.objId = obj.value;
            params.objType = obj.objType;
            params.action = "trans";
            if(params.objId == ""){
                fish.error({title:'提示',message:'转派对象不能为空！请选择。。。'});
                return;
            }
            $("#orderOper-form").blockUI({message: '转派中...'}).data('blockui-content', true);
            if (FILES === null) {
                me.submitOrder(params);
            }else {
                me.fileUpdate(params);
            }

        },
        //提交工单
        submitOrder : function(params){
            var me = this;
            operOrderAction.submitOrder(params,function (res) {
                $("#orderOper-form").unblockUI({message: '转派中...'}).data('blockui-content', true);
                if(res.success){
                    fish.toast('success', res.message);
                    me.popup.close();
                }else {
                    fish.toast('error', res.message);
                }
            });
        },
        circuitInfo : function() {
            var me = this;
            $("#circuitGrid").grid({
                colModel: me.initGridInfo(),
                multiselect: true,
                shrinkToFit: false,
                pageData: me.qrycircuitInfo()
            });
            $("#circuitGrid").grid("setGridHeight", 150);

        },
        initGridInfo:function(){
            return [
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
        qrycircuitInfo : function(){
            var me = this;
            var psIds = '10101384,10101385,10101387,10101427'; //子流程
            var psId = me.options.psId;
            var param = {};
            param.cstOrdId = me.options.cstOrdId + '';
            param.woState = me.options.woState + '';
            param.tacheId = me.options.tacheId + '';
            //param.dealUserId = userInfo.userId + '';
            param.dispObjTyeValue = me.options.dispObjTyeValue + '';
            param.dispObjTye = me.options.dispObjTye + '';
            //param.btnFlag = me.options.btnFlag + '';
            if (psIds.indexOf(psId) != -1) {
                param.specialtyCode = me.options.specialtyCode;
                param.regionId = me.options.regionId;
                operOrderAction.qrySrvOrdChildList(param, function (res) {
                    if (res.flag == 1) {
                        $("#circuitGrid").grid("reloadData", res.data);
                    } else {
                        fish.toast('error', res.message);
                    }
                });
            } else {
                operOrderAction.qrySrvOrdListCheck(param, function (res) {
                    if (res.flag == 1) {
                        $("#circuitGrid").grid("reloadData", res.data);
                    } else {
                        fish.toast('error', res.message);
                    }
                });
            }
        },
        //初始化附件表格
        initFileUpdate : function() {
            $("#fileGrid").grid({
                colModel: [
                    {name: 'fileName', label: '文件名称', width: 160, sortable: false },
                    {name: 'fileSize', label: '大小', width: 40 },
                    {name: 'fileType', label: '类型', width: 40 , sortable: false },
                    {name: 'action', label: '操作', width: 100, formatter: 'actions',
                        formatoptions: {
                            editbutton: false,
                            delbutton: false,
                            inlineButtonAdd: function (rowdata) {
                                return [{ //可以给actions类型添加多个icon图标,事件自行控制
                                    id: "jRemoveButton", //每个图标的id规则为id+"_"+rowid
                                    className: "inline-remove",//每个图标的class
                                    icon: "glyphicon glyphicon-remove-circle",//图标的样子
                                    title: "删除"//鼠标移上去显示的内容
                                }]
                            }
                        }
                    }
                ]
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
            FILES.submit();
        },

    });
});