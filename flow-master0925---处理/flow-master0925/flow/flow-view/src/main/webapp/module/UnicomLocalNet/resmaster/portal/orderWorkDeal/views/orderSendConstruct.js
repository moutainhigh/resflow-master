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
            // fish.store.set('COMPLEX_1','');
            // fish.store.set('OPTICAL_2','');
            // fish.store.set('TRANS_3','');
            // fish.store.set('DATA_4','');
            // fish.store.set('EXCHANGE_5','');
            // fish.store.set('ACCESS_6','');
            // fish.store.set('WIRELESS_7','');
            // fish.store.set('MOBILE_8','');
            // fish.store.set('SYN_9','');
            // fish.store.set('IMS_10','');
            // fish.store.set('OTHER_11','');
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
            if (tacheId == '500001153'){
                //电路调度环节查询单据所在的省分
                var param = {};
                param.srvOrdId = me.options.srvOrdId;
                param.serviceId = me.options.serviceId;
                provinceInfo = operOrderAction.queryProvinceName(param).responseJSON.data;
                //查询电路调度环节，电路编号必填的省份配置
                provinceConf = operOrderAction.queryProvinceConf(param).responseJSON.data;
            }
            //500001168
            srvOrdIds = me.options.srvOrdId;
            //初始化电路信息
            this.circuitInfo();
            this.initResources();
            //this.initFish();
            //初始化附件表格
            this.initFileUpdate(woId);

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
            var activeType = sysResource.ACTIVE_TYPE;
            var $AResouresType=$('#AResouresType').combobox({
                placeholder: '---请选择---',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '否', value: '0'},
                    {name: '是', value: '1'}
                ],
            });
            var $ZResouresType=$('#ZResouresType').combobox({
                placeholder: '---请选择---',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '否', value: '0'},
                    {name: '是', value: '1'}
                ],
            });
            //本地客户电路-新开、变更、移机流程、跨域电路-新开、变更 非中继电路产品
            if((psId == "1000212" || psId == "1000209") && serviceId != '20181221006' && (activeType == '101'|| activeType == '103'|| activeType == '106')){
                // $('#ASourceDiv').show();
                // $('#ZSourceDiv').show();
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
            operOrderAction.orderSendConstruct(params, function (res) {
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
                    //add by wang.gang2 勾选数据是否超时需要展示超时原因
                    //me.isOvertime();

                    var srvOrdId = data.SRV_ORD_ID + '';
                    if (tacheId == "500001168"){
                        if ("second-schedule-lt" == sysResource.SYSTEM_RESOURCE
                            &&("secondary" == sysResource.RESOURCES
                                || "jike" == sysResource.RESOURCES)){
                            me.qrySecondDataMakeLists(srvOrdId);
                            me.qrySubLocalTestDataInfo(srvOrdId);
                        } else {
                            me.getProvince(srvOrdId);
                        }
                    }else if (tacheId == "500001153"){ //判断是否是电路调度环节

                        me.initTacheDealUser(data.PS_ID + '', data.SERVICE_ID + '', data.ORDER_ID + '', sysResource);
                        if (psId != '1000209' && psId != '1000210'){//判断是否是跨域，不是一干来单可以刷新调单信息
                            //20190509选中行事件时，先对选中的行进行判断是否有相应的调单
                            if (btnFlag == 'submit'){//提交的时候才去做这些判断
                                var rowsData = $('#circuitGrid').grid("getCheckRows");
                                if(rowsData.length > 0) {
                                    //判断勾选的电路是否包含不同的调单，如果是则给出提示
                                    var dispatchSet = new Set();
                                    var isShow = true; //是否回填原调单内容true:回填，false:不回填
                                    for (var i = 0; i < rowsData.length; i++) {
                                        var dispatchId = rowsData[i].DISPATCH_ORDER_ID;
                                        if(dispatchId == ''){
                                            isShow = false;
                                        }
                                    }

                                }
                            }
                        }
                    }


                },
                onSelectAll: function (e, status){ //全选事件
                    var rowsData = $('#circuitGrid').grid("getCheckRows");

                    if (tacheId == "500001153"){ //判断是否是电路调度环节
                        if (psId != '1000209' && psId != '1000210'){//判断是否是跨域，不是一干来单可以刷新调单信息

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

                            }
                        }
                    }
                },
                //选中单元格
                onCellSelect: function (e, rowid, iCol, cellcontent) {
                    // debugger;
                    var data = $("#circuitGrid").grid("getRowData",rowid);
                    //当iCol为0时，选中的是复选框而不是行数据，则不触发数据回显事件
                    if(0 != iCol){
                        if('500001155' == tacheId || '500001157' == tacheId){
                            me.initSelectSave(data.ORDER_ID);
                        }
                    }


                },
                beforeEditCell:function (e, rowid, colName, cellcontext, iRow, iCol){
                    // //获取选中的行数据
                    // var data = $("#circuitGrid").grid("getSelection");
                    // //判断是否为一干调度、二干调度下发的单据，如果是，电路编号则不允许修改
                    // var param = {};
                    // param.srvOrdId = data.SRV_ORD_ID;
                    // //查询单据来源
                    // var res = operOrderAction.querySystemResource(param).responseJSON.data;
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
        initFileUpdate : function(param) {
            var me = this;
            $("#fileGrid").grid({
                colModel: [
                    {name: 'attachInfoId', label: '附件ID', hidden: true},
                    {name: 'fileName', label: '文件名称', width: 160, sortable: false },
                    {name: 'fileSize', label: '大小', width: 50},
                    {name: 'fileType', label: '类型', width: 40 , sortable: false },
                    {name: 'action', label: '操作', width: 100, formatter: 'actions',
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
                width:516,
                create: function () {//控件初始化完成触发的事件
                    operOrderAction.getAnnex(param,function (data) {
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
                    fileObj.attachInfoId = '';
                    var site = obj.name.lastIndexOf(".");
                    fileObj.fileName = obj.name.substring(0, site);
                    fileObj.fileSize = (obj.size / 1024.0).toFixed(2) + "KB";
                    fileObj.fileType = obj.name.substring(site + 1, obj.name.length);
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
                },
                always: function (e, data) {
                    me.isSubmit = false;
                    if (data.result.success) {
                        $.unblockUI();
                        fish.toast('success', data.result.message);
                        me.popup.close();
                    }else {
                        $.unblockUI();
                        fish.toast('error', data.result.message);
                    }
                },
            });
        },
        fileUpdate :function (params){
            FILES.url = URL+"/localScheduleLT/FlieUpdateController/uploadFiles.spr";
            FILES.formData = {
                params : JSON.stringify(params)
            };
            if (!this.isSubmit) {
                this.isSubmit = true;
                FILES.submit();
            } else {
                fish.toast('error', "请勿重复提交！");
            }
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
        //补单查询原调单信息
        queryDispatchInfoByCstOrdId : function(){
            var me = this;
            var param = {};
            param.cstOrdId = me.options.cstOrdId;
            operOrderAction.queryDispatchInfoByCstOrdId(param,function(data){
                if (data.success) {
                    if (data.dispatch.length > 0){
                        $('#dispatchOrderNum').attr('value',data.dispatch[0].DISPATCH_ORDER_NO);
                        $('#dispatchOrderNum').attr('disabled',true);
                        $('#dispatchOrderName').attr('value',data.dispatch[0].DISPATCH_TITLE);
                        $('#dispatchOrderText').val(data.dispatch[0].DISPATCH_TEXT);
                    }else{
                        //打开派单页面如果第一条电路，没有调单信息，则生成新的调单信息
                        me.getSequenceNum();
                    }
                }else {
                    fish.toast('error', data.message);
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

        //初始化电路调度环节需要指定处理对象的环节
        initTacheDealUser : function(psId, serviceId, orderId, sysResource){
            var me = this;
            var tacheDealUserFlag = $("input[name='tacheDealUserRadio']:checked").val();
            if (tacheDealUserFlag == '0') { //是
                $('#tacheUserDiv').show();
                var data = $("#circuitGrid").grid("getSelection");
                if (!JSON.stringify(data) == "{}"){
                    orderId = data.ORDER_ID;
                }
                var param = new Object();
                param.psId = psId;
                param.serviceId = serviceId;
                param.orderId = orderId;
                param.sysResource = sysResource.SYSTEM_RESOURCE;
                operOrderAction.qryDealUserTacheConfig(param,function(res){
                    var divHtml = '';
                    var styleStr = '';
                    if (res.success){
                        res.data.forEach(function(val,i){
                            if (i == 2){
                                styleStr = 'style="padding-top: 35px;"';
                            }else {
                                styleStr = '';
                            }
                            divHtml = divHtml + '<div '+ styleStr +'>\n' +
                                '<label class="col-md-2 col-sm-2 control-label" style="padding-right: 0;">'+ val.TACHE_NAME +'：</label>\n' +
                                '    <div class="col-md-4 col-sm-4">\n' +
                                '        <div class="input-group">\n' +
                                '            <input id="'+ val.TACHE_ID +'_'+ val.TACHE_NAME + '"'+
                                '                   name="'+ val.TACHE_ID +'DealUserPopedit" ' +
                                '                   class="form-control">\n' +
                                '        </div>\n' +
                                '    </div>\n' +
                                '</div>';
                            me.$("#tacheUserDiv").html("");
                            me.$("#tacheUserDiv").append(divHtml);
                        });
                        $("input[name$='DealUserPopedit']").popedit({
                            /*initialData: {
                                'name': '请选择环节派发对象！',
                                'value': ''
                            },*/
                            open:function(e) {
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
                                        currentAreaId : userInfo.areaId,
                                        currentOrgId : userInfo.orgId,
                                        currentUserId : userInfo.userId
                                    },
                                    callback: function (popup, view) {
                                        popup.result.then(function (res) {
                                            var orgNames = '';
                                            var orgIds = '';
                                            var objType = '';
                                            //    var orgIds = new Array();
                                            res.forEach(function(val,i){
                                                if (i == 0) {
                                                    orgNames = val.name ;
                                                }
                                                orgIds = val.id;
                                                objType = val.objType;
                                            })
                                            _this.popedit('setValue', {name:orgNames, value:orgIds, objType:objType});
                                        }, function (e) {
                                            console.log('关闭了', e);
                                        });
                                    }
                                };
                                var popup = fish.popupView(options);
                            }
                        });

                        me.tacheDealUserByOrderId(orderId);
                    } else {
                        fish.error({title:'提示',message:'查询需要指定处理人的环节失败！'});
                    }
                });
            }else if(tacheDealUserFlag == '1') { //否
                $('#tacheUserDiv').hide();
            }
        },
        //查询电路调度环节指定的下游环节处理对象
        tacheDealUserByOrderId : function(orderId){
            var param = {};
            param.orderId = orderId;
            operOrderAction.qryTacheDealUserByOrderId(param,function (resData) {
                if(resData != null){
                    resData.forEach(function(val,i){
                        var dispObj = JSON.parse(val.DISP_OBJ_LIST);
                        $("#"+val.TACHE_ID+"_"+val.TACHE_NAME).popedit('setValue', {name:dispObj.name, value:dispObj.value, objType:dispObj.objType});
                    });
                }
            });

        },


    });
});