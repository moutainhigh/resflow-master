define(['text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/orderSendConstruct.html',
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
    var srvOrdIdList=[];

    return fish.View.extend({
        template: fish.compile(operOrderView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
            //'click #closeBtn': 'closeBtn',
            'click #saveBtn': 'confirmSavePropertyConfig',
            'click #area_btn_save': 'confirmSavePropertyConfig',
            'click #specialSaveBtn': 'saveSpecialConfig',
            'click #resourceSaveBtn': 'resourceSaveBtnConfig',
            'click #outsideSaveBtn': 'outsideSaveBtnConfig',
            'click #checkSaveBtn': 'saveCheckInfo',
        },

        initialize: function() {
            this.render();
            URL = "";
            FILES = null;
            areaPro = {};
            jobData = {};//岗位
            userData = {'userNodeArrayMake':'','userNodeArrayCon':''}//人员 数据制作
            //userData = {};//人员 资源施工  外线施工
            psIdFlow = {};
            this.isSubmit = false;
            this.delFiles = new Array();
            resultflagA=false;
            resultflagZ=false;
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

            //500001168
            srvOrdIds = me.options.srvOrdId;
            //初始化电路信息
            this.circuitInfo();
            this.initResources();
            //this.initFish();

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

        },
        initResources:function(){
            var param = new Object();
            var srvOrdId = this.options.srvOrdId;//获取SRV_ORD_ID
            param.srvOrdId = srvOrdId;
            var psId = this.options.psId;//获取psid;
            var serviceId = this.options.serviceId;
            var resources = sysResource.RESOURCES;
            var activetype = sysResource.ACTIVE_TYPE;
            $AResouresType = $('#AResouresType').combobox({
                placeholder: '---请选择---',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '否', value: '0'},
                    {name: '是', value: '1'}
                ],
            });
            $ZResouresType =$('#ZResouresType').combobox({
                placeholder: '---请选择---',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '否', value: '0'},
                    {name: '是', value: '1'}
                ],
            });
            //[一干电路]客户、局内电路流程、[二干电路]客户电路流程。 非局内中继电路新开、变更、移机
            if((psId == "10101020" || psId == "10101060")&& serviceId!='20181221006'&&(activetype=='101'||activetype=='103'||activetype=='106')&&resources=='jike'){
                $('#ASourceDiv').show();
                $('#ZSourceDiv').show();
                var attrInfoa = operOrderAction.queryAttrInfos({srvOrdId:srvOrdId,attrCode:'ORD10171'}).responseJSON.data;
                if ("" != attrInfoa && null != attrInfoa && "" != attrInfoa[0].ATTR_VALUE && null != attrInfoa[0].ATTR_VALUE) {
                    $AResouresType.combobox('value', attrInfoa[0].ATTR_VALUE);
                    if(attrInfoa[0].SOURSE == 'jike'){
                        $AResouresType.combobox('disable');
                    }else{
                        resultflagA = true;
                    }
                }else{
                    resultflagA = true; //需要页面补充填入
                }
                var attrInfoz = operOrderAction.queryAttrInfos({srvOrdId:srvOrdId,attrCode:'ORD10172'}).responseJSON.data
                if ("" != attrInfoz && null != attrInfoz && "" != attrInfoz[0].ATTR_VALUE && null != attrInfoz[0].ATTR_VALUE) {
                    $ZResouresType.combobox('value', attrInfoz[0].ATTR_VALUE);
                    if(attrInfoz[0].SOURSE == 'jike'){
                        $ZResouresType.combobox('disable');
                    }else{
                        resultflagZ = true;
                    }
                }else {
                    resultflagZ = true; //需要页面补充填入
                }
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
            params.cstOrdId = me.options.cstOrdId;
            var circuitData = $("#circuitGrid").grid("getCheckRows");
            params.circuitData=circuitData;
            params.TRADE_TYPE_CODE=circuitData.TRADE_TYPE_CODE;

            if (circuitData.length == 0) {
                fish.warn('请至少选择一个电路信息！');
                return;
            }
            var aResourceValue = $("#AResouresType").val();
            var zResourceValue = $("#ZResouresType").val();
            //AZ端资源是否具备
            if (resultflagA) {
                if (aResourceValue == '' ) {
                    fish.warn('A端资源具备情况必填！');
                    return false;
                } else {
                    params.ATTR_CODE_A = 'ORD10171';
                    params.ATTR_CODE_NAME_A = 'A端资源是否具备';
                    params.ATTR_CODE_VALUE_A = aResourceValue;
                    params.RESULT_FLAG_A = resultflagA;
                }
            }
            if (resultflagZ){
                if (zResourceValue == '') {
                    fish.warn('Z端资源具备情况必填！');
                    return false;
                } else {
                    params.ATTR_CODE_Z = 'ORD10172';
                    params.ATTR_CODE_NAME_Z = 'Z端资源是否具备';
                    params.ATTR_CODE_VALUE_Z = zResourceValue;
                    params.RESULT_FLAG_Z = resultflagZ;
                }
            }
            if (aResourceValue == '1' && zResourceValue == '1') {
                fish.confirm('az端资源都已具备,是否下发工建系统?').result.then(
                    function() {
                        me.sendConstruct(params);
                    },
                    function() {
                        $.unblockUI();
                        me.popup.close();
                    }
                );
            }else {
                me.sendConstruct(params);
            }
        },
        sendConstruct : function(params) {
            var me = this;
            $.blockUI({message: '下发中...'});
            orderDetailsAction.orderSendConstruct(params, function (res) {
                me.isSubmit = false;
                $.unblockUI();
                if (res.success) {
                    me.popup.close();
                }else{
                    fish.toast('error', res.message);
                }
            });
        },

        // 初始化电路信息表
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
                    var data = $("#circuitGrid").grid("getSelection");

                },
                onSelectAll: function (e, status){ //全选事件
                    var rowsData = $('#circuitGrid').grid("getCheckRows");
                },
                //选中单元格
                onCellSelect: function (e, rowid, iCol, cellcontent) {
                    // debugger;
                    var data = $("#circuitGrid").grid("getRowData",rowid);
                    //当iCol为0时，选中的是复选框而不是行数据，则不触发数据回显事件
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

                },
                gridComplete: function () {
                    //if (userInfo.areaId == '350002000000000042766427') { //如果是海南用户
                    if (userInfo.ifSelect == '1') {
                        if (tacheId == '500001155' || tacheId == '500001157') {
                            if (btnFlag == 'submit') {
                                $("#circuitGrid").grid("showCol", 'DF_ORDER_CONFIG');
                            }
                        }
                    }
                }
                // }
            });
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

            if (psIds.indexOf(psId) != -1){
                param.specialtyCode = specialtyCode;
                param.regionId = regionId;
                param.sysResource=sysResource; //二干：secondary
                operOrderAction.qrySrvOrdChildList(param,function (res) {

                    if(res.flag == 1){
                        circuitGirdInfo = res.data;
                        $("#circuitGrid").grid("reloadData", res.data);
                        me.qryCircuitAreaInfo(res.data[0]);
                        me.initResourceConstructFn(res.data[0]);
                    }else {
                        fish.toast('error', res.message);
                    }
                });
            }else {
                operOrderAction.qrySrvOrdList(param,function (res) {
                    if(res.flag == 1){
                        $("#circuitGrid").grid("reloadData", res.data);
                        me.qryCircuitAreaInfo(res.data[0]);

                        if (res.data[0].TACHE_ID == "500001168"){
                            if ("second-schedule-lt" == sysResource.SYSTEM_RESOURCE
                                &&("secondary" == sysResource.RESOURCES
                                    || "jike" == sysResource.RESOURCES)){
                                me.qrySecondDataMakeLists(res.data[0].SRV_ORD_ID);
                                me.qrySubLocalTestDataInfo(res.data[0].SRV_ORD_ID);
                            } else {
                                me.getProvince(res.data[0].SRV_ORD_ID);
                            }
                        }
                    }else {
                        fish.toast('error', res.message);
                    }
                });
            }
            $(window).trigger("resize");
            //(specialtyCode != null && specialtyCode != null)
        },
        qryCircuitAreaInfo : function(param) {
            var srvOrdId = param.SRV_ORD_ID + '';
            console.log("www"+srvOrdId)
            var me = this;
            var formValue = $('#orderOper-form').form('value');
            operOrderAction.qryCircuitAreaInfo(srvOrdId,function (res) {
                var divStr = "";
                var otherStr = "";
                if ("102,104,105".indexOf(param.ACTIVE_TYPE) == -1) { //拆机，停机，复机，不用查询主辅调
                    if("second-schedule-lt" == res.RESOURCES){
                        $("#OTHERNAME").prop("checked",true);
                        $("#otherMaster").removeAttr("disabled");
                        $("#secondaryOther").hide();
                    }else{
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
                        //$("#masterDiv").replaceWith(divStr);
                        $("#masterSelectAZProtDiv").html("");
                        $("#masterSelectAZProtDiv").append(divStr);
                    }
                }

            });
        },

        initGridInfo:function(){
            var tacheId = this.options.tacheId;
            var psId = this.options.psId;
            // 资源配置按钮是否隐藏状态
            var checkSave = $("#configBtnDiv").is(":hidden");
            var selectSpecialFlag = $("input[name='resRadio']:checked").val();
            // debugger
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
    });
});