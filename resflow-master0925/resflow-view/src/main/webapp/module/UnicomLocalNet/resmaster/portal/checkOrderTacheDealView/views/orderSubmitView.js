define(['module/UnicomLocalNet/resmaster/portal/orderTacheDealView/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/checkOrderTacheDealView/templates/orderSubmitView.html',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderTacheDealView/styles/operOrderView.css'
], function(operOrderAction,operOrderView,orderStandbyAction,i18n,css) {
    var srvOrdIds;

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
            var orderId = me.options.orderId + '';
            this.initFish();
            //初始化电路信息
            this.circuitInfo();
            //初始化附件表格
            this.initFileUpdate();
            //初始化下拉框
            this.initCombobox();
            //初始化数字框
            this.initSpinner();
            //初始化弹出窗口
            this.initPopedit();
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
            srvOrdIds = me.options.srvOrdId;
            var formValue = $('#orderOper-form').form('value');
        },
        submit:function () {
            var me = this;
            var psId = me.options.psId;
            var tacheId = me.options.tacheId;
            var buttonState = me.options.buttonState;
            var formValue = $('#orderOper-form').form('value');
            var btnFlag = me.options.btnFlag;
            var params = new Object();
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
                //判断选的电路有没有保存派发专业信息
                if (val.CHECK_STATE != '已保存') {
                    flag = i;
                    message = '选择了未保存的电路，请选择已保存的电路进行派发！';
                    return false;
                }
            });
            if (flag > -1) {
                fish.error({title:'提示',message:message});
                return;
            }
            var operAttrs = new Object();//线条参数
            var tacheOperInfo = new Object();//环节操作数据信息
            var actionFlag = "complateWo";
            //流程线条参数
            if (tacheId == "510101066"){//投资估算
                operAttrs.investment_estimation = 0; //是否需要专项投资估算 是：0
            }
            tacheOperInfo.remark = formValue.remark;
            params.circuitData = circuitData;
            params.operAttrsVal = operAttrs;
            params.tacheOperInfo = tacheOperInfo;
            params.actionFlag = actionFlag;
            params.action = btnFlag;
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
            //提交前保存反馈信息
           // me.saveCheckInfo();
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
                    debugger;
                    var data = $("#circuitGrid").grid("getRowData",rowid);
                    //当iCol为0时，选中的是复选框而不是行数据，则不触发数据回显事件
                    if(me.qryCheckFlag()){
                        if(data.CHECK_STATE == '已保存' || tacheId == "510101060" || tacheId == "510101066"){
                            //回显核查反馈信息
                            me.initCheckInfo(data)
                        }else {
                            $('#Z_CONSTRUCT_SCHEME').val('');
                            // $('#Z_RES_SATISFY').val('');
                            $('#Z_ACCESS_ROOM').val('');
                            $('#Z_RES_SATISFY').combobox('clear');
                            $('#Z_INVESTMENT_AMOUNT').val('');
                            $('#Z_CONSTRUCT_PERIOD').val('');
                            me.initCheckInfo(data);
                        }
                    }
                },
            });
            $("#circuitGrid").grid("setGridHeight", 150);

        },
        initGridInfo:function(){
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
                {name: 'CIRCUITCODE', label: '电路编号', width: 95, sortable: false },
                {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                {name: 'SERIAL_NUMBER', label: '业务号码', width: 100 , sortable: false },
                {name: 'AREGIONNAME', label: 'A端所属区域', width: 100 , sortable: false },
                {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 100 , sortable: false },
                {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
            ]
        },
        saveCheckInfo: function(){
            var me = this;
            var data = $("#circuitGrid").grid("getCheckRows");
            var formValue = $('#orderOper-form').form('value');
            if (data.length >= 1) {
                var param = {};
                param.circuitData = data;
                param.formValue = formValue;
                var checkA = $('#L_SPECIAL_INVESTMENT_AMOUNT').isValid();
                if(!checkA){
                    fish.warn({title:'提示',message:'不能为空或格式不正确！请修改!'})
                    return;
                }
                // 校验核查数据
                if(me.validCheckInfo(param)){
                    operOrderAction.saveCheckInfo(param,function(res){
                        if (res.success){
                            //核查信息保存完成后，重新刷新电路信息表格数据
                            me.circuitInfo();
                            fish.toast('success', res.message);

                        } else{
                            fish.toast('error', res.message);
                        }
                    });
                }
            }else{
                fish.toast('warn', "请至少勾选一条电路信息!");
            }

        },
        validCheckInfo : function(param){
            var data = param.circuitData;
            var formValue = param.formValue;
            var me = this;
            var tacheId = me.options.tacheId;
            // 投资估算510101066
            var isValidSign = !(tacheId == "510101066");
            var feedBackSign = data[0].FEEDBACKSIGN;
            if(isValidSign){
                $('#orderOper-form').validator('setIgnoreField','L_CONSTRUCT_SCHEME,L_RES_SATISFY',true);//取消字段校验
            }
            $('#orderOper-form').validator('setIgnoreField','remark',true);//取消字段校验
            var checkNull = $("#orderOper-form").isValid();
            $('#orderOper-form').validator('setIgnoreField','remark',false);//恢复字段校验
            if(isValidSign){
                $('#orderOper-form').validator('setIgnoreField','L_CONSTRUCT_SCHEME,L_RES_SATISFY',false);//取消字段校验
            }
            if(!checkNull){
                fish.warn({title:'提示',message:'数据格式不正确，请按提示修改为正确格式！'})
                return false;
            }
            return true;
        },
        qrycircuitInfo : function(){
            var me = this;
            var psId = me.options.psId;
            var specialtyCode = me.options.specialtyCode;
            var regionId = me.options.regionId;
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
            param.specialtyCode = specialtyCode;
            param.regionId = regionId;
            operOrderAction.qrySrvOrdListCheck(param,function (res) {
                if(res.flag == 1){
                    $("#circuitGrid").grid("reloadData", res.data);
                    me.initCheckInfo(res.data[0]);
                }else {
                    fish.toast('warn', res.message);
                }
            });
        },

        // 初始化核查反馈页面并查询数据
        initCheckInfo : function (param) {
            var me = this;
            var checkFlag = true;
            var tacheId = me.options.tacheId;
            if(checkFlag) {
                me.queryCheckInfo(param);
            }
        },
        // 查询是否出现核查反馈页面
        qryCheckFlag:function(){
            var me = this;
            var tacheId = me.options.tacheId;
            var psId = me.options.psId;
            var formValue = $('#orderOper-form').form('value');
            var checkFlag = (psId == "10101042" && tacheId != "510101052") || (tacheId == "510101052" && formValue.resRadio == 1);
            return checkFlag;
        },
        // 查询核查反馈信息 参数：WO_ID,SRV_ORD_ID
        queryCheckInfo : function (param) {
            var me = this;
            var tacheId = me.options.tacheId;
            var params = {};

            //       if(tacheId=='500001150' || tacheId=='500001151'){

            if(tacheId=='510101066'){
                $('#feedBackLDiv').show();
                //投资预算设置只读
                $("input[name$='Z_RES_SATISFY']").combobox('disable');
                $("#Z_CONSTRUCT_SCHEME").attr("readonly","readonly");
                $("input[name$='Z_INVESTMENT_AMOUNT']").spinner('disable');
                $("input[name$='Z_CONSTRUCT_PERIOD']").spinner('disable');
            }
            params.tacheId = tacheId;
            params.woId =param.WO_ID;
            params.srvOrdId = param.SRV_ORD_ID;
            params.orderId = param.ORDER_ID;
            operOrderAction.queryCheckInfo(params,function (res) {
                var arrayData = new Map();
                arrayData = res.data;
                for(var key in arrayData){
                    var value = arrayData[key];
                    if(key=="A_RES_SATISFY" || key=="Z_RES_SATISFY"|| key=="L_RES_SATISFY"){
                        $("#"+key).combobox('value',value);
                    }else{
                        $("#"+key).val(value);
                    }
                }
            });
            //      }
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
                        fish.toast('warn', data.result.message);
                    }
                },
            });
        },

        initCombobox:function () {
            $("input[name$='RES_SATISFY']").combobox({
                placeholder: '--请选择资源是否满足--',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '满足', value: '0'},
                    {name: '不满足', value: '1'}
                ]
            });
        },
        initSpinner:function () {
            $("#A_INVESTMENT_AMOUNT").spinner({
                max: 100,
                min: 0,
            });
            $("#A_CONSTRUCT_PERIOD").spinner({
                max: 100,
                min: 0,
            });
            $("#Z_INVESTMENT_AMOUNT").spinner({
                max: 100,
                min: 0,
            });
            $("#Z_CONSTRUCT_PERIOD").spinner({
                max: 100,
                min: 0,
            });
            $("#L_INVESTMENT_AMOUNT").spinner({
                max: 100,
                min: 0,
            });
            $("#L_CONSTRUCT_PERIOD").spinner({
                max: 100,
                min: 0,
            });
            $("#L_SPECIAL_INVESTMENT_AMOUNT").spinner({
                max: 100,
                min: 0,
            });
        },
        initPopedit:function () {
            $("input[name$='A_ACCESS_ROOM']").popedit({
                open:function(e) {
                    var _this = $(this);
                    var param = new Object();
                    var province ;
                    var value ;
                    param.srvOrdId = srvOrdIds;
                    param.type = 'A';
                    var param2 =  new Object();
                    operOrderAction.qryProvinceName(param,function (date1) {
                        province = date1.ATTR_VALUE;
                        param2.province = province;
                        if(province != null ){
                            operOrderAction.qryProvinceValue(param2,function (date2) {
                                value = date2.ID;
                                //var city = $("#A_belong_city").popedit("getValue");
                                var options = {
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
                                var popup = fish.popupView(options);
                            });
                        }

                    });

                }
            });
            $("input[name$='Z_ACCESS_ROOM']").popedit({
                open:function(e) {
                    var _this = $(this);
                    var param = new Object();
                    var province ;
                    var value ;
                    param.srvOrdId = srvOrdIds;
                    param.type = 'Z';
                    var param2 =  new Object();
                    operOrderAction.qryProvinceName(param,function (date1) {
                        province = date1.ATTR_VALUE;
                        param2.province = province;
                        if(province != null ){
                            operOrderAction.qryProvinceValue(param2,function (date2) {
                                value = date2.ID;
                                //var city = $("#A_belong_city").popedit("getValue");
                                var options = {
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
                                var popup = fish.popupView(options);
                            });
                        }

                    });

                }
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