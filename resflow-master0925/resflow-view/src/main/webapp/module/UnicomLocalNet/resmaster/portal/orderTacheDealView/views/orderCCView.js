define(['module/UnicomLocalNet/resmaster/portal/orderTacheDealView/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/orderTacheDealView/templates/orderCCView.html',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderTacheDealView/styles/operOrderView.css'
], function(operOrderAction,orderTransferView,orderStandbyAction,i18n,css) {

    var meTran,userInfo;
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
            userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data; //用户信息
        },

        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },

        //初始化fish组件
        afterRender: function() {
            URL=this.getRootPath();
            var me = this;
            meTran = this;
            var orderId = me.options.orderId + '';
            this.initFish();
            //初始化电路信息
            this.circuitInfo();
            //初始化附件表格
            this.initFileUpdate();

            //初始化转派人员选择框
            $("#tranStaffPopedit").popedit({
                open:function(e) {
                    var _this = $(this);
                    var options = {
                        url: 'module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/views/userView',
                        height: 490,
                        width: 700,
                        title:'选择抄送人员',
                        modal: false,
                        draggable: false,
                        autoResizable: true,
                        viewOption: {
                            flag: "transferStaff",
                            title: '选择抄送人员',
                            currentAreaId: userInfo.areaId,
                            currentOrgId: userInfo.orgId,
                            currentUserId: userInfo.userId,
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                var orgNames = '';
                                var orgIds = '';
                                var objType = '';
                                var ids = new Array();
                                var names = new Array();
                                res.forEach(function (val, i) {
                                    if (i == 0) {
                                        orgNames = val.name;
                                    }
                                    orgIds = val.id;
                                    objType = val.objType;
                                    ids.push(val.id);
                                    names.push(val.name);
                                });
                                // _this.popedit('setValue', {name: orgNames, value: orgIds, objType: objType});
                                $("#dispatchId").val(ids.join(","));
                                $("#dispatchType").val(objType);
                                $("#tranStaffPopedit").val(names.join(","));
                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    };
                    var popup = fish.popupView(options);
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
        },
        submit:function () {
            var formValue = $('#orderOper-form').form('value');
            var circuitData = $("#circuitGrid").grid("getCheckRows");

            if (circuitData.length == 0) {
                fish.warn('请至少选择一个电路信息！');
                return;
            }
            var params = new Object();
            debugger
            params.circuitData = circuitData; //订单信息
            params.remark = formValue.remark;
            params.opinion = formValue.opinion; // 抄送意见
            params.objId = $("#dispatchId").val();
           // params.objType = $("#dispatchType").val();
            params.objType = '260000003';
            params.action = "wocc";
            if(params.objId == ""){
                fish.error({title:'提示',message:'转派对象不能为空！请选择。。。'});
                return;
            }
            $("#orderOper-form").blockUI({message: '抄送中...'}).data('blockui-content', true);
            if (FILES === null) {
                meTran.submitOrder(params);
            }else {
                meTran.fileUpdate(params);
            }

        },
        //提交工单
        submitOrder : function(params){
            var me = this;
            operOrderAction.submitOrder(params,function (res) {
                debugger
                if(res.success){
                    fish.toast('success', res.message);
                    $("#orderOper-form").unblockUI({message: '抄送中...'}).data('blockui-content', true);
                    me.popup.close();
                }else {
                    fish.toast('error', res.message);
                    $("#orderOper-form").unblockUI({message: '抄送中...'}).data('blockui-content', true);
                }
            });
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
            //$("#circuitGrid").grid("setAllCheckRows",true); //默认选中
            //$("#circuitGrid").grid("setAllCheckDisabled",true); //禁用全部复选框
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
                ],
                width:516
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