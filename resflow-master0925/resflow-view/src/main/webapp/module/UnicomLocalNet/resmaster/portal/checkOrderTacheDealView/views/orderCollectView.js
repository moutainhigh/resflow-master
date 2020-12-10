define(['module/UnicomLocalNet/resmaster/portal/orderTacheDealView/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/checkOrderTacheDealView/templates/orderCollectView.html',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderTacheDealView/styles/operOrderView.css'
], function(operOrderAction,operOrderView,orderStandbyAction,i18n,css) {
    var srvOrdIds,tacheId;
    return fish.View.extend({
        sysResource:'',
        isSendSpeciallocal:true,
        localCount:0,zConstructSch:'',
        template: fish.compile(operOrderView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
            'click #checkSaveBtn': 'saveCheckInfo',
            'click #localCheckBtn':'localCheckBtn'
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
            this.sysResource=operOrderAction.qrySrvOrderBelongSys({srvOrdId:me.options.srvOrdId}).responseJSON.data;
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
            debugger;
            var me = this;
            tacheId = me.options.tacheId; //"500001144";
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
            var serviceId =  me.options.serviceId;
            var buttonState = me.options.buttonState;
            var formValue = $('#orderOper-form').form('value');
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
            params.sysComeRes = me.sysResource.RESOURCES; //数据来源
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
        saveCheckInfo: function(){
            var me = this;
            var data = $("#circuitGrid").grid("getCheckRows");
            var formValue = $('#orderOper-form').form('value');

            if (data.length >= 1) {
                var param = {};
                param.circuitData = data;
                param.formValue = formValue;
                // 校验核查数据
                if(me.validCheckInfo(param)){
                    var res = operOrderAction.saveCheckInfo2(param).responseJSON.data;
                        if (res.success){
                            //核查信息保存完成后，重新刷新电路信息表格数据
                            me.circuitInfo();
                            fish.toast('success', res.message);
                            return true;

                        } else{
                            fish.toast('error', res.message);
                            return false;
                        }
                }
            }else{
                fish.toast('warn', "请至少勾选一条电路信息!");
                return false;
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
                    me.queryCheckInfo(data);
                },
                onSelectAll: function (e, status){ //全选事件

                },
                onCellSelect:function(e, rowid, iCol, cellcontent){
                    // debugger;
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
            params.tacheId = tacheId;
            params.woId =param.WO_ID;
            params.srvOrdId = param.SRV_ORD_ID;
            params.orderId = param.ORDER_ID;
            me.initPageInfo(params); //判断核查调度选择是否派发专业或本地
            operOrderAction.queryCheckInfo(params,function (res) {
                var arrayData = new Map();
                arrayData = res.data;
                for(var key in arrayData){
                    var value = arrayData[key];
                    if(key=="Z_RES_SATISFY"){
                        $("#"+key).combobox('value',value);
                    }else if (key=="Z_CONSTRUCT_SCHEME"){
                        $('#Z_CONSTRUCT_SCHEME').val(value+arrayData['L_CONSTRUCT_SCHEME']);
                    }else{
                        $("#"+key).val(value);
                    }

                    //初始化资源提供方式
                    if(key=="A_RES_PROVIDE" || key=="Z_RES_PROVIDE"){
                        $("#"+key).combobox('value',value);
                    }else{
                        $("#"+key).val(value);
                    }
                    //初始化资源接入方式
                    if(key=="A_RES_ACCESS" || key=="Z_RES_ACCESS"){
                        $("#"+key).combobox('value',value);
                    }else{
                        $("#"+key).val(value);
                    }
                    //初始化备货情况
                    if(key=="A_EQUIP_READY" || key=="Z_EQUIP_READY"){
                        $("#"+key).combobox('value',value);
                    }else{
                        $("#"+key).val(value);
                    }

                }
            });
            //      }
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
            operOrderAction.qrySrvOrdListCheck(param,function (res) {
                if(res.flag == 1){
                    $("#circuitGrid").grid("reloadData", res.data);
                    //查询核查反馈信息并展示
                    me.queryCheckInfo(res.data[0]);
                }else {
                    fish.toast('warn', res.message);
                }
            });
        },
        validCheckInfo : function(param){
            var data = param.circuitData;
            var formValue = param.formValue;
            var me = this;
            var tacheId = me.options.tacheId;
            // 核查汇总510101060、投资估算510101066
            var isValidSign = !(tacheId == "510101060" || tacheId == "510101066");
            var feedBackSign = data[0].FEEDBACKSIGN;
            $('#orderOper-form').validator('setIgnoreField', 'remark', true);
            var checkNull = $("#orderOper-form").isValid();
            $('#orderOper-form').validator('setIgnoreField', 'remark', false);
            if(!checkNull){
                fish.warn({title:'提示',message:'请按提示修改数据！'})
                return false;
            }
            //核查汇总环节判断如果选择直接派发核查汇总环节是否填写过本地录入信息
            else if  (!isSendSpeciallocal&&($('#L_RES_SATISFY').val()==null||$('#L_RES_SATISFY').val()=='')){
                if($('#Z_INVESTMENT_AMOUNT').val()!=''){
                    return true;
                }
                fish.warn('请录入本地核查信息！');
                return false;
            }
            else{
                return true;
            }
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
            //资源提供方式
            var obj = new Object();
            obj.codeType = "REC_10022";
            orderStandbyAction.queryItemType(obj, function (data) {
                $("#Z_RES_PROVIDE").combobox({
                    placeholder: '--请选择资源提供方式--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: data
                });
            });
            //资源接入方式
            var obj = new Object();
            obj.codeType = "REC_10024";
            orderStandbyAction.queryItemType(obj, function (data) {
                $("#Z_RES_ACCESS").combobox({
                    placeholder: '--请选择资源接入方式--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: data
                });
            });
            //备货情况
            var obj = new Object();
            obj.codeType = "REC_10028";
            orderStandbyAction.queryItemType(obj, function (data) {
                $("#Z_EQUIP_READY").combobox({
                    placeholder: '--请选择备货情况--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: data
                });
            });

            //核查汇总资源满足情况只读
            if(tacheId=='510101060') {
                $("input[name$='RES_SATISFY']").combobox('disable')
            }
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
            //核查汇总投资金额、建设工期只读
            if(tacheId=='510101060'){
                $("input[name$='INVESTMENT_AMOUNT']").spinner('disable');
                $("input[name$='CONSTRUCT_PERIOD']").spinner('disable');
            };
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
            //核查汇总机房只读
            if(tacheId=='510101060'){
                $("input[name$='ACCESS_ROOM']").popedit('disable');
            };
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
        //附件上传
        fileUpdate :function (params){
            FILES.url = URL+"/localScheduleLT/FlieUpdateController/uploadFiles.spr";
            FILES.formData = {
                params : JSON.stringify(params)
            }
            FILES.submit();
        },
        localCheckBtn:function () {
            var me=this;
            var data = $("#circuitGrid").grid("getCheckRows");
            if(data.length<1) {
                fish.toast('warn', "请至少勾选一条电路信息!");
                return;
            }
            var options = {
                url: 'module/UnicomLocalNet/resmaster/portal/checkOrderTacheDealView/views/orderCollectLocalView.js',
                height: '90%',
                width: '100%',
                modal: true,
                draggable: true,
                resizable:true,
                autoResizable: true,
                viewOption: {
                    CONSTRUCT_SCHEME:$('#Z_CONSTRUCT_SCHEME').val(),
                    A_RES_SATISFY:$('#AL_RES_SATISFY').val(),
                    A_CONSTRUCT_SCHEME:$('#AL_CONSTRUCT_SCHEME').val(),
                    A_INVESTMENT_AMOUNT:$('#AL_INVESTMENT_AMOUNT').val(),
                    A_CONSTRUCT_PERIOD:$('#AL_CONSTRUCT_PERIOD').val(),
                    Z_RES_SATISFY:$('#ZL_RES_SATISFY').val(),
                    Z_CONSTRUCT_SCHEME:$('#ZL_CONSTRUCT_SCHEME').val(),
                    Z_INVESTMENT_AMOUNT:$('#ZL_INVESTMENT_AMOUNT').val(),
                    Z_CONSTRUCT_PERIOD:$('#ZL_CONSTRUCT_PERIOD').val(),
                },
                callback: function (popup, view) {
                    popup.result.then(function (data) {
                        var zResSat = $('#Z_RES_SATISFY').val();
                        if(me.localCount==0){
                            me.zConstructSch=$('#Z_CONSTRUCT_SCHEME').val();
                            me.localCount++;
                        }
                        var aresSatisfy=data['A_RES_SATISFY'];
                        var zresSatisfy=data['Z_RES_SATISFY'];
                        zResSat=aresSatisfy+zresSatisfy>0 ? 1:0;
                        $("#Z_RES_SATISFY").combobox('value',zResSat);
                        var aScheme = data['A_CONSTRUCT_SCHEME'];
                        var zScheme = data['Z_CONSTRUCT_SCHEME'];
                        if(typeof(aScheme)=='undefined'){
                            aScheme=""
                        }
                        if(typeof(zScheme)=='undefined'){
                            zScheme=""
                        }
                        $("#L_RES_SATISFY").val(zResSat);
                        var arr = me.zConstructSch.split("本地A端:");
                        var constructSch = arr[0];
                        document.getElementById("Z_CONSTRUCT_SCHEME").value='二干资源：'+constructSch +
                            '\n' +'本地A端:'+aScheme+':'+'\n' +
                            '本地Z端:'+zScheme+':';
                        var aAmount=isNaN(parseInt(data['A_INVESTMENT_AMOUNT']))?0:parseFloat(data['A_INVESTMENT_AMOUNT']);
                        var zAmount=isNaN(parseInt(data['Z_INVESTMENT_AMOUNT']))?0:parseFloat(data['Z_INVESTMENT_AMOUNT']);
                        var amount = aAmount + zAmount;
                        $('#Z_INVESTMENT_AMOUNT').val(amount.toFixed(2));
                        var aPeriod=isNaN(parseInt(data['A_CONSTRUCT_PERIOD']))?0:parseInt(data['A_CONSTRUCT_PERIOD']);
                        var zPeriod=isNaN(parseInt(data['Z_CONSTRUCT_PERIOD']))?0:parseInt(data['Z_CONSTRUCT_PERIOD']);
                        $('#Z_CONSTRUCT_PERIOD').val(aPeriod>zPeriod?aPeriod:zPeriod);

                        $("#AL_RES_SATISFY").combobox('value',data['A_RES_SATISFY']);
                        $('#AL_CONSTRUCT_SCHEME').val(data['A_CONSTRUCT_SCHEME']);
                        $('#AL_INVESTMENT_AMOUNT').val(data['A_INVESTMENT_AMOUNT']);
                        $('#AL_CONSTRUCT_PERIOD').val(data['A_CONSTRUCT_PERIOD']);
                        $("#ZL_RES_SATISFY").combobox('value',data['Z_RES_SATISFY']);
                        $('#ZL_CONSTRUCT_SCHEME').val(data['Z_CONSTRUCT_SCHEME']);
                        $('#ZL_INVESTMENT_AMOUNT').val(data['Z_INVESTMENT_AMOUNT']);
                        $('#ZL_CONSTRUCT_PERIOD').val(data['Z_CONSTRUCT_PERIOD']);
                        if(typeof(aresSatisfy)!='undefined'&&typeof(zresSatisfy)!='undefined'){
                            isSendSpeciallocal=true;
                        }
                    }, function (e) {
                        console.log('关闭了', e);
                    });
                }
            };
            var popup = fish.popupView(options);
        },
        initPageInfo:function (param) {
            operOrderAction.queryisSendSpeciallocal(param, function (res) {
                isSendSpeciallocal=res;
                if(res){ //选择派发本地核查和专业为是
                    $('#localCheckBtn').hide();
                }else{
                    $('#localCheckBtn').show();
                }
            });
        }
    });
});