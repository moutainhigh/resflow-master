define(['text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/provinceAutoOperView.html',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/operOrderView.css'
], function(operOrderView,operOrderAction,orderStandbyAction,orderDetailsAction,i18n,css) {
    var dispatchNum = '-1';
    var SpecialFlag ;
    var srvOrdIds;
    var userInfo;
    var sysResource;
    var meOper;
    var provinceInfo;
    var provinceConf;
    var circuitGirdInfo;
    var provinceAutoConf;

    return fish.View.extend({
        template: fish.compile(operOrderView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #configBtn': 'submit'
        },
        initialize: function() {
            this.render();
        },
        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },
        //初始化fish组件
        afterRender: function() {
            meOper = this;
            SpecialFlag='-1';
            // dispatchNum = '-1';
            URL=this.getRootPath();
            var me = this;
            userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data;
            var tacheId = me.options.tacheId;
            var orderId = me.options.orderId + '';
            var woId=me.options.woId +'';
            var psId = me.options.psId;
            var srvOrdId = me.options.srvOrdId + '';
            var serviceId = me.options.serviceId;
            var resources = me.options.resources;
            var srvBelong = new Object();
            srvBelong.srvOrdId = srvOrdId;
            sysResource=operOrderAction.qrySrvOrderBelongSys(srvBelong).responseJSON.data;
            var psIds = '1000248,1000249';
            if (psIds.indexOf(psId) != -1){
                psIdFlow = operOrderAction.getMainFlowPsId(orderId).responseJSON.data;
            }
            var param = {};
            param.srvOrdId = me.options.srvOrdId;
            provinceInfo = operOrderAction.queryProvinceName(param).responseJSON.data;
            //500001168
            srvOrdIds = me.options.srvOrdId;
            //初始化电路信息
            this.circuitInfo();
            this.initFish();

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
            var serviceId = me.options.serviceId;
            var resources = me.options.resources;
            var formValue = $('#orderOper-form').form('value');
            //var userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data;
            if(btnFlag == "resConfig"){
                $("#operOrderViewTitle").html("资源配置");
                $("#fileUpdateDiv").hide();
                $("#remarkDiv").hide();
                $("#subBtnDiv").hide();
                $("#circuitDiv label").hide();
                $("#circuitDiv > div").attr('class', 'col-md-12 col-sm-12');
                $("#configBtnDiv").show();
            }
        },
        submit:function () {
            var me = this;
            var psId = me.options.psId;
            var tacheId = me.options.tacheId;
            var buttonState = me.options.buttonState;
            var formValue = $('#orderOper-form').form('value');
            var btnFlag = me.options.btnFlag;
            var circuitDatas = $("#circuitGrid").grid("getCheckRows");

            $.each(circuitDatas, function(i, circuitData){
                var params = new Object();
                if(sysResource!= null && sysResource!=''){
                    params.sysResFullCom = sysResource.SYSTEM_RESOURCE; //系统来源
                    params.resFullCom = sysResource.RESOURCES; //数据来源
                }
                params.circuitData = circuitData; //订单信息
                params.SRV_ORD_ID=circuitData.SRV_ORD_ID;
                params.CST_ORD_ID=circuitData.CST_ORD_ID;
                params.TRADE_TYPE_CODE=circuitData.TRADE_TYPE_CODE;
                params.WO_ID=circuitData.WO_ID;
                params.action = "resConfig";
                me.submitOrder(params);
            });
        },
        submitOrder : function(params){
            //省份自动开通
            var me = this;
            params.areaId=userInfo.areaId;
            //查询直接通过接口调用省分DIA自动开通的省份配置
            provinceAutoConf = operOrderAction.queryProvinceAutoConf(params).responseJSON.data;
                //判断该省份是否是直接调用省分DIA接口
            if(provinceAutoConf=='undefined'||provinceAutoConf==null){
                fish.toast('error', "请检查省分自建系统的配置！");
                return;
            }else{
                if ((provinceInfo.ATTR_VALUE).indexOf(provinceAutoConf.REMARK) > -1){
                    me.isSubmit = true;
                    params.flag = 'resConfig';
                    orderDetailsAction.provinceResOrder(params, function (res) {
                        if (res.success) {
                                $.unblockUI();
                                fish.toast('success', res.message);
                                me.popup.close();
                        } else {
                            fish.toast('error', res.message);
                        }
                    });
                }else{
                    fish.toast('error', "请检查省分自建系统的配置！");
                    return;
                }
            }
        },
        qrycircuitInfo : function(){
            var me = this;
            var psIds = '1000248,1000249'; //子流程
            var psId = me.options.psId;
            var specialtyCode = me.options.specialtyCode;
            var regionId = me.options.regionId;
            var dispObjTyeValue = me.options.dispObjTyeValue;
            var dispObjTye = me.options.dispObjTye;
            var param = {};
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
            param.newCreateResource = $("input[name='newResRadio']:checked").val();
            //     var  = me.options.btnFlag;//
            if (psIds.indexOf(psId) != -1){
                param.specialtyCode = specialtyCode;
                param.regionId = regionId;
                param.sysResource=sysResource; //二干：secondary
                operOrderAction.qrySrvOrdChildList(param,function (res) {
                    if(res.flag == 1){
                        circuitGirdInfo = res.data;
                        $("#circuitGrid").grid("reloadData", res.data);
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
                cellEdit: true,
                pageData: me.qrycircuitInfo(),
                onSelectRow: function (e, rowid, state, checked) {//选中行事件
                },
                onSelectAll: function (e, status){ //全选事件
                    if (tacheId == "500001153"){ //判断是否是电路调度环节
                        if (psId != '1000209' && psId != '1000210'){//判断是否是跨域，不是一干来单可以刷新调单信息
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
                        }
                    }
                },
                //选中单元格
                onCellSelect: function (e, rowid, iCol, cellcontent) {
                    var data = $("#circuitGrid").grid("getRowData",rowid);
                },
                beforeEditCell:function (e, rowid, colName, cellcontext, iRow, iCol){
                    if (sysResource.RESOURCES == 'onedry' || sysResource.RESOURCES == 'secondary' || sysResource.ORDER_TYPE == '102'){
                        return false;
                    }
                },
                afterSaveCell:function( e, rowid, colName, cellcontext, iRow, iCol){
                    //编辑单元格后对数据进行保存
                    // if (colName == 'CIRCUITCODE' && cellcontext != '' && cellcontext!=undefined){
                    var data = $("#circuitGrid").grid("getSelection");
                    var param = {};
                    param.attrValue = cellcontext;
                    param.srvOrdId = data.SRV_ORD_ID;
                    operOrderAction.saveCircuitCodeBySrvOrdId(param,function(res){
                        if (res.success){
                            console.log(res.msg);
                        }
                    });
                    // }
                },
                gridComplete: function () {
                    //if (userInfo.areaId == '350002000000000042766427') { //如果是海南用户
                    if (userInfo.ifSelect == '1') {
                        if (tacheId == '500001155' || tacheId == '500001157') {
                            /*if (btnFlag == 'trans' || btnFlag == 'rollBackOrder' || btnFlag == 'resConfig') {
                                $("#circuitGrid").grid("hideCol", 'DF_ORDER_CONFIG');
                            }*/
                            if (btnFlag == 'submit') {
                                $("#circuitGrid").grid("showCol", 'DF_ORDER_CONFIG');
                            }
                        }
                    }
                }
                // }
            });
        },
        initGridInfo:function(){
            var tacheId = this.options.tacheId;
            var psId = this.options.psId;
            // 资源配置按钮是否隐藏状态
            var checkSave = $("#configBtnDiv").is(":hidden");
            var selectSpecialFlag = $("input[name='resRadio']:checked").val();
            // debugger
            if(tacheId == '500001153' || (tacheId == '500001144'&& checkSave && selectSpecialFlag=='0')) { //电路调度、核查调度(选专业)
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
            }else{
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
            }
        }

    });
});