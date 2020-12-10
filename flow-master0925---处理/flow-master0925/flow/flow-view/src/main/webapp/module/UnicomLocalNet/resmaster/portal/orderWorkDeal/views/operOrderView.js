define(['text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/operOrderView.html',
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
    var resultflagA=false;
    var resultflagZ=false;
    var resultflagAccess=false;
    var orderPsId = '';
    var standardAddress = false;
    return fish.View.extend({
        template: fish.compile(operOrderView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
            //'click #closeBtn': 'closeBtn',
            'click #configBtn': 'submit',
            'click #saveBtn': 'confirmSavePropertyConfig',
            'click #area_btn_save': 'confirmSavePropertyConfig',
            'click #specialSaveBtn': 'saveSpecialConfig',
            'click #resourceSaveBtn': 'resourceSaveBtnConfig',
            'click #outsideSaveBtn': 'outsideSaveBtnConfig',
            'click #checkSaveBtn': 'saveCheckInfo'
        },

        initialize: function() {
            this.render();
            URL = "";
            FILES = null;
            areaPro = {};
            jobData = {};//岗位
            userData = {'userNodeArrayMake':'','userNodeArrayCon':''}; //人员 数据制作
            //userData = {};//人员 资源施工  外线施工
            psIdFlow = {};
            this.isSubmit = false;
            this.delFiles = new Array();
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
            this.initFish();
            //初始化附件表格
            this.initFileUpdate(woId);
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
                $("#A_RES_PROVIDE,#Z_RES_PROVIDE").combobox({
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
                $("#A_RES_ACCESS,#Z_RES_ACCESS").combobox({
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
                $("#A_EQUIP_READY,#Z_EQUIP_READY").combobox({
                    placeholder: '--请选择备货情况--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: data
                });
            });


            //是否选择专业
            $("input[name = 'resRadio']").bind('click',function(){
                var selectSpecialFlag = $("input[name='resRadio']:checked").val();
                if (selectSpecialFlag == '0') {
                    $("#specialDiv").show();
                    $("#specialSaveBtnDiv").show();
                    $("#feedBackADiv").hide();
                    $("#feedBackZDiv").hide();
                    $("#checkSaveBtnDiv").hide();
                }else if(selectSpecialFlag == '1') {
                    $("#specialDiv").hide();
                    $("#specialSaveBtnDiv").hide();
                    $("#feedBackADiv").show();
                    $("#feedBackZDiv").show();
                    $("#checkSaveBtnDiv").show();
                }
                me.circuitInfo();
                SpecialFlag = selectSpecialFlag;
                //根据条件来判断是否提示电路编号是否可以编辑

            });
            //是否新建资源
            $("input[name = 'newResRadio']").bind('click',function(){
                var newResourceFlag = $("input[name='newResRadio']:checked").val();
                if (newResourceFlag == '0') { //是
                    $("#tacheDealUserDiv").hide();
                    $("#tacheUserDiv").hide();
                    $("#childSendDiv").hide();
                    $("#masterSelectDiv").hide();
                    $('#saveBtnDiv').hide();
                    $('#area_select').show();
                    $('#areaSave').show();
                }else if(newResourceFlag == '1') { //否
                    var tacheDealUserFlag = $("input[name='tacheDealUserRadio']:checked").val();
                    $("#tacheDealUserDiv").show();
                    if (tacheDealUserFlag == '0') {
                        $("#tacheUserDiv").show();
                    }else if(tacheDealUserFlag == '1'){
                        $("#tacheUserDiv").hide();
                    }
                    $("#childSendDiv").show();
                    $("#masterSelectDiv").show();
                    $('#saveBtnDiv').show();
                    $('#area_select').hide();
                    $('#areaSave').hide();
                }
                if(tacheId == "500001150"&&sysResource.SYSTEM_RESOURCE=="second-schedule-lt"){
                    $('#areaSave').hide();
                }
                me.circuitInfo();
            });

            //是否资源施工
            $("input[name = 'makeResource']").bind('click',function(){
                var makeResourceFlag = $("input[name='makeResource']:checked").val();
                if(makeResourceFlag == '0'){//是
                    $("#childFlowresConstructDiv").show();
                }else if(makeResourceFlag = '1'){//否
                    $("#childFlowresConstructDiv").hide();
                }
            });

            //是否外线施工
            $("input[name = 'makeResourceOutside']").bind('click',function(){
                var makeResourceOutsideFlag = $("input[name='makeResourceOutside']:checked").val();
                if (userInfo.ifSelect == '1') {
                    if(makeResourceOutsideFlag == '0'){//是
                        $("#dispObjDiv").show();
                    }else if(makeResourceOutsideFlag = '1'){//否
                        $("#dispObjDiv").hide();
                    }
                }
            });

            //是否选择下游环节处理人
            $("input[name = 'tacheDealUserRadio']").bind('click',function(){
                me.initTacheDealUser(psId, serviceId, orderId, sysResource);
            });

            //本地测试是否不通过
            $('#testResult').bind('click',function(){
                if (tacheId == "500001160" || tacheId == "500001166"){ //本地测试 联调测试
                    var testFlag = $("input[name='testRadio']:checked").val();
                    if (testFlag == '0') {
                        $("#childFlowDiv").hide();
                    }else if(testFlag == '1') {
                        $("#childFlowDiv").show();
                        me.childFlowInfo(); //初始化子流程数据
                    }
                }
                if (me.options.btnFlag == "submit"){
                    var formValue = $('#orderOper-form').form('value');
                    if ((psId == "1000212" && tacheId == "500001161") || (psId == "1000207" && tacheId == "500001166") ) {
                        //本地电路 全程调测  ||  局内电路 联调测试
                        if ($("input[name='testRadio']:checked").val() == '0') { //测试通过
                            $("#attach").addClass("requireds");
                            $("#remarkGoRoll").removeClass("requireds");
                        }
                        else if ($("input[name='testRadio']:checked").val() == '1') {
                            $("#remarkGoRoll").addClass("requireds");
                            $("#attach").removeClass("requireds");
                        }
                    } else if (psId == "1000212" && tacheId == "500001160"){
                        //本地电路 本地测试
                        if ($("input[name='testRadio']:checked").val() == '0') { //测试通过
                            $("#remarkGoRoll").removeClass("requireds");
                            //MV,DIA,语音中继产品只有一段，所以本地测试环节附件必传
                            if (('10000008,10000011,20181211001').indexOf(serviceId) != -1){
                                $("#attach").addClass("requireds");
                            }
                        }
                        else if ($("input[name='testRadio']:checked").val() == '1') {
                            $("#remarkGoRoll").addClass("requireds");
                            if (('10000008,10000011,20181211001').indexOf(serviceId) != -1){
                                $("#attach").removeClass("requireds");
                            }
                        }
                    }else if (psId == "1000209" && tacheId == "500001160"){
                        //跨域电路  本地测试 -----通过附件必填说明不必填；不通过说明必填附件不必填
                        if ($("input[name='testRadio']:checked").val() == '0') { //测试通过
                            $("#attach").addClass("requireds");
                            $("#remarkGoRoll").removeClass("requireds");
                        }
                        else if ($("input[name='testRadio']:checked").val() == '1') {
                            $("#attach").removeClass("requireds");
                            $("#remarkGoRoll").addClass("requireds");
                        }
                    }
                }
            });
            $('#oneDry').bind('click',function(){
                var oneDryValue = $("input[name='oneDry']:checked").val();
                if (tacheId == "500001153"){ //电路调度
                    if (psId == '1000209' || psId == '1000210' || (psId == '1000213' && resources == 'secondary')){
                        //跨域，用来判断是一干来单  二干下发本地的停复机流程
                        if (oneDryValue == '1'){ //否
                            if (dispatchNum == '-1'){
                                me.getSequenceNum();
                            } else{
                                $('#dispatchOrderNum').attr('value',dispatchNum);
                                $('#dispatchOrderNum').attr('disabled',true);
                            }
                        } else if (oneDryValue == '0'){ //是
                            //查询一干调单信息
                            me.queryDispatchInfoByCstOrdId();
                        }
                    }
                }
            });
            $('input[type=radio][name=masterSelectRadio]').bind('click',function(){
                var formValue = $('#orderOper-form').form('value');
                if (tacheId == "500001153" && formValue.masterSelectRadio =="other") { //电路调度 其他
                    $("#otherMaster").removeAttr("disabled");
                }
            });
            //初始化主调局选择框
            $("#otherMaster").popedit({
                initialData: {
                    'name': '请选择区域！',
                    'value': ''
                },
                open:function(e) {
                    var _this = $(this);
                    if(tacheId == '500001153'){ //电路调度
                        var value = $('#otherMaster').popedit('getValue');
                    }
                    var options = {
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/masterTreeView',
                        height: 350,
                        width: 400,
                        modal: false,
                        draggable: false,
                        autoResizable: true,
                        viewOption: {
                            flag : "otherMaster",
                            orderId : me.options.orderId,
                            key : "MASTER",
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                var orgNames = '';
                                var orgIds = '';
                                //
                                var nodeArray = new Array;
                                res.forEach(function(val,i){
                                    //
                                    var nodeSin = new Object();
                                    nodeSin.id = val.id;
                                    nodeArray.push(nodeSin);
                                    if(!val.isParent){
                                        if(orgIds==''){
                                            orgNames = val.name ;
                                            orgIds = val.id;
                                        } else {
                                            orgNames = orgNames + ',' + val.name ;
                                            orgIds = orgIds + ',' + val.id;
                                        }
                                    }
                                })
                                // areaPro[key]=nodeArray;
                                _this.popedit('setValue', {name:orgNames, value:orgIds});
                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    };
                    var popup = fish.popupView(options);
                }
            });
            // var me = this;
            var me = this;
            //初始化专业选择框
            $("input[name$='Popedit']").popedit({
                initialData: {
                    'name': '请选择派发区域！',
                    'value': ''
                },
                open:function(e) {
                    var _this = $(this);
                    var key=_this.get(0).id;
                    var _array = new Array;
                    if(tacheId == '500001153'){ //电路调度
                        if(key == 'areaPopedit'){
                            key = 'AREA_0';
                                var _value = $('#areaPopedit').popedit('getValue');
                                if(_value != null){
                                    _value = _value.value+'';
                                    _array = _value.split(",");
                                }
                            }else if(key=='complexCfgPopedit'){
                                key = 'COMPLEX_1';
                                var _value = $('#complexCfgPopedit').popedit('getValue');
                                if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        } else if(key=='opticalCfgPopedit'){
                            key = 'OPTICAL_2';
                            var _value = $('#opticalCfgPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        } else if(key=='transCfgPopedit'){
                            key = 'TRANS_3';
                            var _value = $('#transCfgPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        }else if(key=='transIPRANCfgPopedit'){
                            key = 'TRANS_IPRAN_13';
                            var _value = $('#transIPRANCfgPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        }else if(key=='transMSAPCfgPopedit'){
                            key = 'TRANS_MSAP_14';
                            var _value = $('#transMSAPCfgPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        } else if(key=='dataCfgPopedit'){
                            key = 'DATA_4';
                            var _value = $('#dataCfgPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        } else if(key=='exchangeCfgPopedit'){
                            key = 'EXCHANGE_5';
                            var _value = $('#exchangeCfgPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        } else if(key=='accessCfgPopedit'){
                            key = 'ACCESS_6';
                            var _value = $('#accessCfgPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        } else if(key=='wirelessCfgPopedit'){
                            key = 'WIRELESS_7';
                            var _value = $('#wirelessCfgPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        } else if(key=='mobileCfgPopedit'){
                            key = 'MOBILE_8';
                            var _value = $('#mobileCfgPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        } else if(key=='synCfgPopedit'){
                            key = 'SYN_9';
                            var _value = $('#synCfgPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        } else if(key=='IMSCfgPopedit'){
                            key = 'IMS_10';
                            var _value = $('#IMSCfgPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        } else if(key=='otherCfgPopedit'){
                            key = 'OTHER_11';
                            var _value = $('#otherCfgPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        } /*else if(key=='p_dataCfgPopedit'){
                            key = 'P_DATA_4';
                            var _value = $('#p_dataCfgPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        } else if(key=='p_exchangeCfgPopedit'){
                            key = 'P_EXCHANGE_5';
                            var _value = $('#p_exchangeCfgPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        }*/
                    }else if(tacheId == '500001144'){ //核查调度
                        if(key=='outsidePopedit'){
                            var _value = $('#outsidePopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        }else if(key=='dataPopedit'){
                            var _value = $('#dataPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        }else if(key=='transPopedit'){
                            var _value = $('#transPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        }else if(key=='changePopedit'){
                            var _value = $('#changePopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        }else if(key=='accessPopedit'){
                            var _value = $('#accessPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        }else if(key=='otherPopedit'){
                            var _value = $('#otherPopedit').popedit('getValue');
                            if(_value != null){
                                _value = _value.value+'';
                                _array = _value.split(",");
                            }
                        }
                    }
                    areaPro[key] = _array;
                    var options = {
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/operTreeOrderView',
                        height: 350,
                        width: 400,
                        modal: false,
                        draggable: false,
                        autoResizable: true,
                        viewOption: {
                            flag : "org",
                            orderId : me.options.orderId,
                            key : key,
                            nodeValues :  areaPro[key]
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                var orgNames = '';
                                var orgIds = '';
                                //
                                var nodeArray = new Array;
                                res.forEach(function(val,i){
                                    //
                                    var nodeSin = new Object();
                                    nodeSin.id = val.id;
                                    nodeArray.push(nodeSin);
                                    if(!val.isParent){
                                        if(orgIds==''){
                                            orgNames = val.name ;
                                            orgIds = val.id;
                                        } else {
                                            orgNames = orgNames + ',' + val.name ;
                                            orgIds = orgIds + ',' + val.id;
                                        }
                                    }
                                })
                                // areaPro[key]=nodeArray;
                                _this.popedit('setValue', {name:orgNames, value:orgIds});
                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    };
                    var popup = fish.popupView(options);
                }

            });
            //初始化转派人员选择框
            $("#tranStaffPopedit").popedit({
                initialData: {
                    'name': '请选择转派对象！',
                    'value': ''
                },
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
            //启子流程选专业
            $("input[name$='Cfg']").bind('click',function(){
                $("input[name$='Cfg']").each(function(){
                    if ($(this).is(":checked") == true && (!$(this).attr("disabled"))) {
                        $(this).parent().parent().next().find("input").popedit('enable');
                    }else{
                        $(this).parent().parent().next().find("input").popedit('disable');
                    }
                })
            });
            //核查选专业
            $("input[name$='Special']").bind('click',function(){
                $("input[name$='Special']").each(function(){
                    if($(this).is(":checked") == true){
                        $(this).parent().parent().next().find("input").attr("disabled",false);
                    }else{
                        $(this).parent().parent().next().find("input").attr("disabled",true);
                    }
                })
            });
            $('#A_RES_SATISFY').on('combobox:change', function(e) {
                var ResSatisfy = $('#A_RES_SATISFY').val();
                // debugger;
                if(ResSatisfy == "1"){
                    //$('#A_INVESTMENT_AMOUNT').attr('required',true);
                    //$('#A_CONSTRUCT_PERIOD').attr('data-rule',required);
                }
            });
            //选择岗位
            $("input[name$='SelectJob']").popedit({
                /*initialData: {
                    'name': '请选择需要派发岗位！',
                    'value': ''
                },*/
                open:function(e) {
                    var _this = $(this);
                    var options = {
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/selectJobView',
                        height: 470,
                        width: 500,
                        modal: false,
                        draggable: false,
                        autoResizable: true,
                        viewOption: {
                            type : _this[0].id,
                            nodeValues :  jobData,
                            selectType : 'job'
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                var jobNames = '';
                                //var jobIds = '';
                                var nodeArray = new Array;
                                var valueArray = new Array;
                                res.forEach(function(val,i){
                                    var nodeSin = new Object();
                                    nodeSin.id = val.id;
                                    nodeSin.pId = '-1';
                                    nodeSin.name = val.name;
                                    nodeArray.push(nodeSin);
                                    if (i == 0) {
                                        jobNames = val.name ;
                                        valueArray.push(nodeSin);
                                        //jobIds = val.id;
                                    }else{
                                        jobNames = jobNames + ',' + val.name ;
                                        valueArray.push(nodeSin);
                                        //jobIds = jobIds + ',' + val.id ;
                                    }
                                })
                                jobData = nodeArray;
                                //_this.popedit('setValue', {name:jobNames, value:jobIds});
                                _this.popedit('setValue', {name:jobNames, value:valueArray});
                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    };
                    var popup = fish.popupView(options);
                }
            });
            //选择人员
            $("input[name$='SelectUser']").popedit({
                open:function(e) {
                    var _this = $(this);
                    var flag = _this[0].id;
                    var options = {
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/selectJobView',
                        height: 470,
                        width: 500,
                        modal: false,
                        draggable: false,
                        autoResizable: true,
                        viewOption: {
                            type : flag,
                            nodeValues :  userData,
                            selectType : 'user'
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                var jobNames = '';
                                //var jobIds = '';
                                var nodeArray = new Array;
                                var valueArray = new Array;
                                if (_this[0].id == 'dataMakeUser') {
                                    res = res.userNodeArrayMake;
                                } else if (_this[0].id == 'resConstructUser') {
                                    res = res.userNodeArrayCon;
                                }
                                res.forEach(function(val,i){
                                    var nodeSin = new Object();
                                    nodeSin.id = val.id;
                                    nodeSin.pId = val.pId;
                                    nodeSin.name = val.name;
                                    nodeArray.push(nodeSin);
                                    if (!val.isParent) {
                                        if (jobNames == '' || jobNames == undefined) {
                                            jobNames = val.name ;
                                            valueArray.push(nodeSin);
                                            //jobIds = val.id;
                                        }else{
                                            jobNames = jobNames + ',' + val.name ;
                                            valueArray.push(nodeSin);
                                            //jobIds = jobIds + ',' + val.id ;
                                        }
                                    }
                                })
                                if(flag == "dataMakeUser"){
                                    userData.userNodeArrayMake = nodeArray;
                                }else if (flag == "resConstructUser") {
                                    userData.userNodeArrayCon = nodeArray;
                                }
                                _this.popedit('setValue', {name:jobNames, value:valueArray});
                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    };
                    var popup = fish.popupView(options);
                }
            });
            //this.initResources();
            this.initAccessType();
        },
        /*initResources:function(){
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

            $('#backReasonType').combobox({
                placeholder: '--请选择退单类型--',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '产品类型错误-整体退单', value: '03'},
                    {name: '带宽错误-整体退单', value: '04'},
                    {name: '其它信息错误-整体退单', value: '05'},
                    {name: '联系人错误-单端退单', value: '06'},
                    {name: '装机地址错误-单端退单', value: '07'},
                    {name: '接口类型错误-单端退单', value: '08'},
                    {name: '带宽错误-单端退单', value: '17'},
                    {name: '其它错误-单端退单', value: '18'},
                ]
            });

        },*/
        initAccessType:function(){
            var psId = this.options.psId;//获取psid;
            var serviceId = this.options.serviceId;
            var resources = sysResource.RESOURCES;
            var newResourceFlag = $("input[name='newResRadio']:checked").val();
            var $AccessType=$('#AccessType').combobox({
                placeholder: '---请选择---',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: 'MSTP', value: '02'},
                    {name: 'SDH', value: '06'},
                    {name: 'OTN', value: '09'},
                    {name: 'MSAP', value: '10'},
                    {name: 'peOTN', value: '11'},
                    {name: '裸纤', value: '12'}
                ],
            });
            if(newResourceFlag=='1'&&serviceId=='10000011'&&resources=='jike'){
                var attrInfo = operOrderAction.queryAttrInfos({attrCode:'10000192',srvOrdId:this.options.srvOrdId}).responseJSON.data;
                if(attrInfo != null && attrInfo != '') {
                    if (attrInfo[0].ATTR_VALUE == '41' || attrInfo[0].ATTR_VALUE == '42') {
                        $('#AccessTypeDiv').show();
                        var AccessType = operOrderAction.queryAttrInfos({attrCode:'ACCESS_CIR_TYPE',srvOrdId:this.options.srvOrdId}).responseJSON.data;
                        if ("" != AccessType && null != AccessType && "" != AccessType[0].ATTR_VALUE && null != AccessType[0].ATTR_VALUE) {
                            $AccessType.combobox('value', AccessType[0].ATTR_VALUE);
                            if(AccessType[0].SOURSE == 'jike'){
                                $AccessType.combobox('disable');
                            }else{
                                resultflagAccess = true;
                            }
                        }else{
                            resultflagAccess = true;
                        }
                    }
                }
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

            if(btnFlag == "submit"){
                //如果是本地调度，且不是一干二干下发的单据,并且不是核查单,需要提示电路编号可以编辑
                if (tacheId == "500001153" && sysResource.SYSTEM_RESOURCE == 'flow-schedule-lt' &&
                    sysResource.RESOURCES != 'onedry' && sysResource.RESOURCES != 'secondary' && sysResource.ORDER_TYPE != '102'){ //电路调度环节
                    $("#operOrderViewTitle").text("工单提交(电路编号可手动编辑)");
                }else{
                    $("#operOrderViewTitle").text("工单提交");
                }
                if(psId == '1000211' && tacheId != "500001144"){ // 核查流程
                    if(sysResource.SYSTEM_RESOURCE!="second-schedule-lt"){
                        $("#feedBackADiv").show();
                        $("#feedBackZDiv").show();
                    }
                    $("#checkSaveBtnDiv").show();
                    if(tacheId!='500001145' && tacheId!='500001146' && tacheId!='500001147'){
                        // $("#accessRoomADiv").hide();
                        // $("#accessRoomZDiv").hide();
                    }
                }


                if(tacheId == "500001162"){
                    $('#finishDate,#endDate').datetimepicker({
                        orientation: {y: 'bottom'}
                    });
                    //客户电路完工确认环节添加报竣时间
                    if(psId=='1000212'){
                        $("#finalReportT").addClass("requireds");
                        $('#finalReportT').show();
                    }

                }
                //父流程环节
                if (tacheId == "500001144"){//核查调度
                    if(buttonState != "dispConfirm"){
                        $("#selectSpecialDiv").show();
                    }
                    $("#specialDiv").show();
                    $('#specialSaveBtnDiv').show();
                    //      $("#accessRoomADiv").hide();
                    //      $("#accessRoomZDiv").hide();
                }
                else if(tacheId == "500001145" || tacheId == "500001146" || tacheId == "500001147" || tacheId == "500001148"
                    || tacheId == "500001149" || tacheId == "510101020"){//专业核查
                    $("#remarkGoRoll").addClass("requireds");
                }
                else if (tacheId == "500001150"){//核查汇总
                    if(sysResource.SYSTEM_RESOURCE=="second-schedule-lt"){
                        $("#checkSaveBtnDiv").hide();
                    }
                    $("#estimateDiv").show();
                }else if (tacheId == "500001145" || tacheId == "500001061"){//传输核查，外线核查
                    //      $("#accessRoomADiv").hide();
                    //      $("#accessRoomZDiv").hide();
                } else if (tacheId == "500001154"){//新建资源录入
                    $("#remarkGoRoll").addClass("requireds");
                 }else if (tacheId == "500001153"){//电路调度
                    if(psId == "1000212" || psId == "1000209"){ //本地，跨域 新开变更
                        if(buttonState != "dispConfirm"){
                            $("#newResourceDiv").show();
                            $("#masterSelectDiv").show();
                        }
                    }else if(psId == "1000207"){ //局内电路
                        if(buttonState != "dispConfirm"){
                            $("#masterSelectDiv").show();
                        }
                    }
                    $("#childSendDiv").show();
                    $("#saveBtnDiv").show();
                    if (psId == '1000209' || psId == '1000210' || (psId == '1000213' && resources == 'secondary')){
                        //跨域，用来判断是一干来单  二干下发本地的停复机流程
                        $("#oneDryDev").show();
                        //加载专业配置信息
                        me.querySpecialtyConfig();
                        //跨域电路打开派单页面时显示调单信息
                        //根据srv_ord_id判断是否二干下发
                        var param = new Object();
                        param.srvOrdId = srvOrdId;
                        operOrderAction.querySrvInfoBySrvOrdId(param,function(res){
                            if(res.success){
                                if (res.result.SYSTEM_RESOURCE == 'second-schedule-lt'){ //二干下发
                                    //查询关联表中的调单ID与电路表中的调单ID是否相同
                                    param.orderId = orderId;
                                    param.cstOrdId = res.result.CST_ORD_ID;
                                    operOrderAction.queryDispatchOrderIdFromRelateTable(param,function(data){
                                        if (data.success){
                                            if (res.result.DISPATCH_ORDER_ID == data.result.DISPATCH_ORDER_ID){
                                                me.queryDispatchOrderInfoByDispatchId(res.result.DISPATCH_ORDER_ID);
                                            }else{
                                                $("#oneDryDev").hide();
                                                me.queryDispatchOrderInfoByDispatchId(data.result.DISPATCH_ORDER_ID);
                                            }

                                        }
                                    });
                                }else{
                                    me.queryDispatchInfoByCstOrdId();
                                }
                            }
                        });
                    }else{
                        if (woState == '290000110') { //补单
                            $("#masterSelectDiv").show();
                            //加载专业信息
                            me.querySpecialtyConfig();
                            me.queryDispatchInfoByCstOrdId();
                        }else{
                            //初始化专业配置
                            me.querySpecialtyConfig();
                        }
                    }
                    /*$("#remarkDiv").hide();
                     $("#fileUpdateDiv").hide();*/
                }else if (tacheId == "500001160"){ //本地测试
                    $("#testDiv").show();
                    var flag = operOrderAction.qryIfPopConfigView(orderId + '').responseJSON.data;
                    if (flag){
                        $("input[name='testRadio']").attr("disabled","disabled");
                    }
                    if ($("input[name='testRadio']:checked").val() == '1') { //不通过 说明必填
                        $("#remarkGoRoll").addClass("requireds");
                    }
                    //跨域电路新开变更移机
                    if (psId == "1000209") {
                        if ($("input[name='testRadio']:checked").val() == '0') { //测试通过 附件必填
                            $("#attach").addClass("requireds");
                        }
                    } else if (psId == "1000212") { //本地电路
                        //MV,DIA,语音中继产品只有一段，所以本地测试环节附件必传
                        if (('10000008,10000011,20181211001').indexOf(serviceId) != -1){
                            $("#attach").addClass("requireds");
                        }
                    }
                }else if (tacheId == "500001166"){//联调测试
                    $("#testDiv").show();
                    if ($("input[name='testRadio']:checked").val() == '0') { //通过 附件必填
                        $("#attach").addClass("requireds");
                    }
                    else if ($("input[name='testRadio']:checked").val() == '1') { //不通过 说明必填
                        $("#remarkGoRoll").addClass("requireds");
                    }
                }else if (tacheId == "500001161"){//全程调测
                    $("#testDiv").show();
                    if (psId == "1000212" && tacheId == "500001161"){ //本地电路 全程调测
                        if ($("input[name='testRadio']:checked").val() == '0') { //测试通过
                            $("#attach").addClass("requireds");
                        }
                        else if ($("input[name='testRadio']:checked").val() == '1') {
                            $("#remarkGoRoll").addClass("requireds");
                        }
                    }
                }else if (tacheId == "500001168"){//跨域全程调测
                    $("#attach").addClass("requireds");
                }else if (tacheId == "500001157"){//资源分配
                    if(psId != "1000213") {
                        $("#makeResourceDiv").show();
                    }
                    //if (userInfo.areaId == '350002000000000042766427'){
                    if (userInfo.ifSelect == '1'){
                        $("#dispObjOtherDiv").show();
                        if(psIdFlow.PSID == "1000213"){ //本地电路的停复机  没有资源施工
                            $("#childFlowresConstructDiv").attr("style","display:none;");
                        }
                        //me.initSelectJob(orderId);
                    }
                }else if (tacheId == "500001155") {//光纤资源分配
                    //if (userInfo.areaId == '350002000000000042766427') {
                    if (userInfo.ifSelect == '1') {
                        $("#dispObjDiv").show();
                        //me.initSelectJob(orderId);
                    }
                    $("#makeResourceDivOutside").show();
                }else {
                    //TODO:其他操作显示模块
                }


                //设备回收情况

                if(tacheId == "500001153" && serviceId != "20181221006" && me.options.activeType == "102"){
                    //非局内客户电路拆机流程
                    orderPsId = operOrderAction.qryParentPsIdBySubOrderId({orderId:orderId}).responseJSON.data.PS_ID;
                    if(orderPsId == "1000214"){
                        $('#equipRecycle').show();
                        $('#IS_RECYCLE_DIV').addClass("requireds");
                        $('#EQUIP_SEQUENCE').clearinput();
                        $('#REMOVE_EQUIP').clearinput();

                        //设备是否回收下拉框初始化
                        orderStandbyAction.queryItemType({codeType:"IS_RECYCLE"}, function (data) {
                            $("#IS_RECYCLE").combobox({
                                placeholder: '--请选择设备回收情况--',
                                dataTextField: 'name',
                                dataValueField: 'value',
                                dataSource: data
                            });
                        });
                        //设备回收数量组件初始化
                        $("#RECYCLE_COUNT").spinner({
                            max: 99,
                            min: 0
                        });
                        //设备是否回收值改变监听
                        $("#IS_RECYCLE").on('combobox:change',function (e) {
                            var isRecycleVal =  $("#IS_RECYCLE").combobox("value");
                            if(isRecycleVal == "0" || isRecycleVal == "1"){
                                $('#RECYCLE_COUNT_DIV').addClass("requireds"); //设备回收数量必填
                                $('#EQUIP_SEQUENCE_DIV').addClass("requireds"); //设备序列号必填
                                //增加校验
                                $("#RECYCLE_COUNT,#EQUIP_SEQUENCE").resetElement();
                                $('#RECYCLE_COUNT,#EQUIP_SEQUENCE').removeAttr('data-rule-ignore');
                            }else{
                                $('#RECYCLE_COUNT_DIV').removeClass("requireds"); //设备回收数量非必填
                                $('#EQUIP_SEQUENCE_DIV').removeClass("requireds"); //设备序列号非必填
                                //取消校验
                                $("#RECYCLE_COUNT,#EQUIP_SEQUENCE").resetElement();
                                $('#RECYCLE_COUNT,#EQUIP_SEQUENCE').attr('data-rule-ignore',true);
                            }
                        });
                        $("#REMOVE_EQUIP").popedit({
                            open:function(e) {
                                var _this = $(this);
                                var options = {
                                    url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/equipQueryView',
                                    height: '99%',
                                    width: '60%',
                                    draggable: true,
                                    resizable:true,
                                    autoResizable: true,
                                    modal: true,
                                    viewOption: {
                                        flag : "org",
                                        srvOrdId:srvOrdId
                                    },
                                    callback: function (popup, view) {
                                        popup.result.then(function (res) {
                                            _this.popedit('setValue',{name:res.equip_name,value:res.equip_code});
                                        }, function (e) {
                                            console.log('关闭了', e);
                                        });
                                    }
                                };
                                var popup = fish.popupView(options);
                            }
                        });
                    }
                }
                if(tacheId == "500001153" && "10000011" == serviceId){
                    //互联网商务专线 初始化标准地址信息
                    operOrderAction.queryIsBussSpecialty({srvOrdId:srvOrdId,orderId:orderId},function (res) {
                        if(res.NEED_SHOW){
                            if(res.BUSSINESS_SPECIALTY){
                                $("#STANDARD_ADDRESS_DIV").show();
                                $('#STANDARD_ADDRESS_DIV').addClass("requireds");
                                if(res.STANDARD_ADDRESS == ""){
                                    //如果是商务专线并且标准地址B侧下发为空
                                    //设为必填、增加校验、初始化选择框
                                    $('#STANDARD_ADDRESS').resetElement();
                                    $('#STANDARD_ADDRESS').removeAttr('data-rule-ignore');
                                    $('#STANDARD_ADDRESS').clearinput();
                                    $('#STANDARD_ADDRESS').popedit({
                                            open:function(e) {
                                                var _this = $(this);
                                                var options = {
                                                    url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/standardAddressView',
                                                    height: '90%',
                                                    width: '60%',
                                                    draggable: true,
                                                    resizable:true,
                                                    autoResizable: true,
                                                    modal: true,
                                                    viewOption: {
                                                        flag : "org",
                                                        srvOrdId:srvOrdId
                                                    },
                                                    callback: function (popup, view) {
                                                        popup.result.then(function (res) {
                                                            _this.popedit('setValue',{name:res.addressName,value:res.addressCode});
                                                        }, function (e) {
                                                            console.log('关闭了', e);
                                                        });
                                                    }
                                                };
                                                var popup = fish.popupView(options);
                                            }
                                        });

                                    standardAddress = true; //标准地址标记为必填
                                }
                                else{
                                    //赋值
                                    $('#STANDARD_ADDRESS').val(res.STANDARD_ADDRESS);
                                }
                            }
                        }else{
                            $("#STANDARD_ADDRESS_DIV").hide();
                        }
                    });
                }

            }else if(btnFlag == "trans"){
                $("#operOrderViewTitle").text("工单转办");
                $("#tranStaffDiv").show();
                $("#tranStaff").addClass("requireds");
            } else if(btnFlag == "resConfig"){
                $("#operOrderViewTitle").html("资源配置");
                $("#fileUpdateDiv").hide();
                $("#remarkDiv").hide();
                $("#subBtnDiv").hide();
                $("#circuitDiv label").hide();
                $("#circuitDiv > div").attr('class', 'col-md-12 col-sm-12');
                $("#configBtnDiv").show();
            } else if(btnFlag == "goBackOrder"){
                $("#operOrderViewTitle").text("工单回退");
                $("#remarkGoRoll").addClass("requireds");

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
            }else if(btnFlag == "rollBackOrder"){
                $("#operOrderViewTitle").text("工单退单");
                $("#remarkGoRoll").addClass("requireds");
                if (tacheId == "500001168") {//跨域全程调测
                    if ("second-schedule-lt" == sysResource.SYSTEM_RESOURCE
                            &&("secondary" == sysResource.RESOURCES
                            || "jike" == sysResource.RESOURCES)){
                        $("#subLocalTestDataDiv").show();
                        $("#subResMakeDiv").show();
                        $("#secondDataMakeDiv").show();
                        // meOper.secondDataMakeInfo(srvOrdId);
                        // meOper.subLocalTestDataInfo(srvOrdId,orderId);
                    } else {
                        $("#crossAZAreaDiv").show();
                        //meOper.getProvince(srvOrdId);
                    }
                }

            }
        },
        submit:function () {
            var me = this;
            var psId = me.options.psId;
            var tacheId = me.options.tacheId;
            /*var orderId = me.options.orderId;
             var woId = me.options.woId;
             var srvOrdId = me.options.srvOrdId;*/
            var buttonState = me.options.buttonState;
            var formValue = $('#orderOper-form').form('value');
            var btnFlag = me.options.btnFlag;
            var params = new Object();
            params.cstOrdId = me.options.cstOrdId;
            params.srvOrdId = me.options.srvOrdId;
            var serviceId = me.options.serviceId;

            var circuitData = $("#circuitGrid").grid("getCheckRows");
            var testFlag = $("input[name='testRadio']:checked").val();
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
            //add by wang.gang2  添加超时原因
            // 超时原因必填
            if(srvOrdIdList.length>0 && formValue.opinion == ''){
                fish.error({title:'提示',message:'超时不能为空'});
                return;
            }else{
                for (var i = 0; i < srvOrdIdList.length; i++) {
                    var info = srvOrdIdList[i];
                    for (var j = 0; j < circuitData.length; j++) {
                        if(circuitData[j].SRV_ORD_ID===info){
                            circuitData[j].opinion = formValue.opinion;
                        }
                    }

                };
            }

            //判断有没有追单
            if('resConfig' != btnFlag){
                var flag = -1;
                var message = '';
                $.each(circuitData,function(i,val){
                    var orderId = val.ORDER_ID;
                    var cstOrdId = val.CST_ORD_ID;
                    var ifFlag = orderDetailsAction.queryIfTrack(orderId + '', cstOrdId + '').responseJSON.data;
                    if (ifFlag){
                        flag = i;
                        message = '存在追单未处理的电路，不能提交，请重新勾选!';
                        return false;
                    }
                });
                if (flag > -1) {
                    // $.unblockUI();
                    fish.error({title:'提示',message:message});
                    return;
                }
                var constructState = true;
                //modify by wang.gang2  配置对接得省份地市 判断是否校验
                var config = new Object();
                config.CODE_TYPE = 'ORDER_SEND';
                config.CODE_VALUE = userInfo.areaId;
                var constructConfig = operOrderAction.queryConstructConf(config).responseJSON.data;
                //二干本地不一样 本地是通过退单按钮  二干是通过同意 不同意回退
                if (constructConfig != '' && constructConfig != null && 'submit' == btnFlag) {

                    $.each(circuitData, function (i, val) {
                        var param = {};
                        param.srvOrdId = val.SRV_ORD_ID;
                        param.cstOrdId = val.CST_ORD_ID;
                        param.tacheId = me.options.tacheId;
                        param.tradeId = circuitData[i].RELATE_INFO_ID;
                        param.checkSrvOrdId = circuitData[i].CHECK_SRV_ORD_ID;
                        param.resources = me.options.resources;
                        param.psId = me.options.psId;
                        var res = orderDetailsAction.summaryBeforeCommit(param).responseJSON.data;

                        if (res.success) {
                            constructState = false;
                        } else {
                            constructState = true;
                            return;
                        }
                    });
                    if (constructState) {
                        // $.unblockUI();
                        fish.error({title: '提示', message: "存在电路未完成工建流程，等待工建系统完工"});
                        return;
                    }
                }
            }

            if (testFlag == '1' && (tacheId == "500001160" || tacheId == "500001166")) { //测试不通过  本地测试 联调测试
                var childFlowData = $("#childFlowGrid").grid("getCheckRows");
                if (childFlowData.length == 0) {
                    fish.warn('请至少选择一个调单信息！');
                    return;
                }
            }
            params.circuitData = circuitData; //订单信息
            //modify by wanggang2  异常单资源配置的时候这来字段空了 报错 不敢直接删掉
            if(sysResource!= null && sysResource!=''){
                params.sysResFullCom = sysResource.SYSTEM_RESOURCE; //系统来源
                params.resFullCom = sysResource.RESOURCES; //数据来源
            }
            if(btnFlag == "submit"){//回单
                var operAttrs = new Object();//线条参数
                //var childFlowSpecialMap = new Object();//子流程专业参数  参数和派发区域
                var tacheOperInfo = new Object();//环节操作数据信息
                var actionFlag = "complateWo";
                // 资源配置按钮是否隐藏状态
                params.opinion = formValue.opinion;
                var checkSaveBtn = $("#configBtnDiv").is(":hidden");
                if (tacheId == "500001153" || (tacheId == "500001144" && checkSaveBtn && formValue.resRadio == 0)){
                    //电路调度  核查调度
                    var circuitNum = -1;
                    $.each(circuitData,function(i,val){
                        if (val.STATE != '已配置') {
                            circuitNum = 0;
                        }
                    });
                    if ($("input[name='newResRadio']:checked").val() == '1'){ //不新建资源
                        if (circuitNum != -1){
                            fish.warn('选择了未配置的电路，请选择已配置的电路进行派发！');
                            return;
                        }
                    }
                    if ($("input[name='newResRadio']:checked").val() == '0'){ //选择新建资源
                        if (circuitNum != -1){
                            fish.warn('选择了未配置的电路，请选择已配置的电路进行派发！');
                            return;
                        }
                    }
                }
                if(me.qryCheckFlag()&& checkSaveBtn){
                    var circuitNum = -1;
                    $.each(circuitData,function(i,val){
                        if (val.CHECK_STATE != '已保存') {
                            circuitNum = 0;
                        }
                    });
                    if (circuitNum != -1 && tacheId != "500001150"){
                        fish.warn('选择了未保存的电路，请选择已保存的电路进行派发！');
                        return;
                    }
                    if (circuitNum != -1 && sysResource.SYSTEM_RESOURCE!="second-schedule-lt"&&tacheId == "500001150"){
                        fish.warn('选择了未保存的电路，请选择已保存的电路进行派发！');
                        return;
                    }
                }

                //资源核查，说明必填 huangxingfei 2019-04-23
                if (tacheId == "500001145" || tacheId == "500001146" || tacheId == "500001147" || tacheId == "500001148"
                    || tacheId == "500001149" || tacheId == "510101020") {
                    if(formValue.remark == null){
                        fish.error({title:'提示',message:'说明不能为空'});
                        return;
                    }
                }

                //流程线条参数
                if (tacheId == "500001144"){//核查调度
                    //    var checkAttrs = me.getAreaSpecialCheck(formValue).checkAreaSpecialArea;
                    if (formValue.resRadio == 1){
                        operAttrs.isOutsideCheck = 1;
                        operAttrs.isDataCheck = 1;
                        operAttrs.isTransCheck = 1;
                        operAttrs.isAccessCheck = 1;
                        operAttrs.isOtherCheck = 1;
                        operAttrs.isChangeCheck = 1;
                        operAttrs.isThoughCheckTatal = 0;
                    }
                }else if (tacheId == "500001150"){//核查汇总
                    var estimateFlag = $("input[name='estimateRadio']:checked").val();
                    if (estimateFlag == '0') {
                        operAttrs.IsNeedInvestment = 0;
                    }else if(estimateFlag == '1') {
                        operAttrs.IsNeedInvestment = 1;
                        operAttrs.ifCollect = 1;
                    }
                }else if (tacheId == "500001151"){//投资估算
                    operAttrs.ifFinish = 1;
                    operAttrs.ifCollect = 0;
                }else if (tacheId == "500001153"){//电路调度
                    //电路调度环节提交时，判断选中电路信息中是否包含多条调单信息
                    if(btnFlag == 'submit'){
                        var rowsData = $('#circuitGrid').grid("getCheckRows");
                        if(rowsData.length > 0) {
                            //判断勾选的电路是否包含不同的调单，如果是则给出提示
                            var dispatchSet = new Set();
                            for (var i = 0; i < rowsData.length; i++) {
                                var dispatchId = rowsData[i].DISPATCH_ORDER_ID;
                                if (dispatchId != '' && dispatchId != undefined) {
                                    dispatchSet.add(dispatchId);
                                }
                            }
                            if (dispatchSet.size > 1) {
                                fish.warn('您所勾选的电路包含多条调单信息，请检查！');
                                return;
                            }
                        }
                        //电路调度环节提交时，将是否复用调单信息传递到后端
                        var oneDryValue = $("input[name='oneDry']:checked").val();
                        params.oneDryValue = oneDryValue;

                        //AZ端资源是否具备
                       /* if (resultflagA) {
                            params.ATTR_CODE_A = 'ORD10171';
                            params.ATTR_CODE_NAME_A = 'A端资源是否具备';
                            params.ATTR_CODE_VALUE_A = $("#AResouresType").val();
                            params.RESULT_FLAG_A = resultflagA;
                        }
                        if (resultflagZ){
                            params.ATTR_CODE_Z = 'ORD10172';
                            params.ATTR_CODE_NAME_Z = 'Z端资源是否具备';
                            params.ATTR_CODE_VALUE_Z = $("#ZResouresType").val();
                            params.RESULT_FLAG_Z = resultflagZ;
                        }*/

                        //接入电路类型
                        if (resultflagAccess){
                            var AccessSrvOrdId = me.options.srvOrdId;
                            var AccessTypeVal = $("#AccessType").val();
                            var resources = sysResource.RESOURCES;
                            params.SRV_ORD_ID_ACCESS = AccessSrvOrdId;
                            params.ACCESS_RESOURCES=resources;
                            params.ATTR_CODE_ACCRESS = 'ACCESS_CIR_TYPE';
                            params.ATTR_CODE_NAME_ACCRESS = '接入电路类型';
                            params.ATTR_CODE_VALUE_ACCRESS = AccessTypeVal;
                            params.RESULT_FLAG_ACCESS = resultflagAccess;
                            if (AccessTypeVal == null || AccessTypeVal == '') {
                                fish.toast('warn','接入电路类型情况不能为空,请选择');
                                return;
                            }
                        }
                    }

                    if(psId == "1000212" || psId == "1000209"){ //本地、跨域的新开变更
                        if (formValue.newResRadio == 0) {
                            operAttrs.isNeedNewRes = 0;
                        }else if (formValue.newResRadio == 1){
                            var checkNull = $("#dispatchOrderName").isValid();
                            if(!checkNull){
                                fish.error({title:'提示',message:'调单标题不能为空!'});
                                return;
                            }
                            operAttrs.isNeedNewRes = 1;
                            actionFlag = "createChildOrder";
                            params.disOrdFlag = true; //调单入库标识
                        }
                    }else {
                        actionFlag = "createChildOrder";
                        params.disOrdFlag = true; //调单入库标识
                    }
                    dispatchNum = '-1'; //提交时将调单号全局变量重置，以便后续操作生成新的调单号

                }else if (tacheId == "500001154"){//新建资源录入
                    operAttrs.isCircuitDispatch = 0;
                    //必须填写说明
                    if(formValue.remark == null){
                        fish.error({title:'提示',message:'说明不能为空'});
                        return;
                    }
                }else if (tacheId == "500001160"){//本地测试
                    var testFlag = $("input[name='testRadio']:checked").val();
                    if (testFlag == '0') {
                        tacheOperInfo.operFlag = "测试通过！";
                        tacheOperInfo.success = true ;
                        operAttrs.isLocaltestRollback = 1;
                        if(psId == "1000209"){
                            operAttrs.ifMainOffice = 0;
                        }
                    }else if(testFlag == '1') {
                        tacheOperInfo.operFlag = "测试不通过！";
                        tacheOperInfo.success = false;
                        operAttrs.isLocaltestRollback = 0;
                        var childFlowGrid = $("#childFlowGrid").grid("getCheckRows");
                        tacheOperInfo.childFlowData = childFlowGrid;
                    }
                    if (psId == "1000212"){//本地电路 本地测试
                        if ($("input[name='testRadio']:checked").val() == '1' && formValue.remark == null){ //测试不通过 说明不能为空
                            fish.error({title:'提示',message:'说明不能为空！'});
                            return;
                        }
                        if ($("input[name='testRadio']:checked").val() == '0' && FILES === null){ //测试通过 附件不能为空
                            var fileFlag = false;
                            $.each(circuitData,function(i,val){
                                //MV,DIA,语音中继产品只有一段，所以本地测试环节附件必传
                                if (('10000008,10000011,20181211001').indexOf(val.SERVICE_ID) != -1){
                                    fileFlag = true;
                                    return false;
                                }
                            });
                            if (fileFlag && !fileData.length > 0){
                                fish.error({title:'提示',message:'存在必须上传附件的电路，请先上传附件！'});
                                return;
                            }
                        }
                    }else if (psId == "1000209"){ // 跨域电路 本地测试
                        if ($("input[name='testRadio']:checked").val() == '0' && FILES === null && !fileData.length > 0){ //测试通过且附件为空
                            fish.error({title:'提示',message:'必须上传附件！'})
                            return;
                        }
                        if ($("input[name='testRadio']:checked").val() == '1' && formValue.remark == null){ //测试不通过 说明不能为空
                            fish.error({title:'提示',message:'说明不能为空！'});
                            return;
                        }
                    }
                }else if (tacheId == "500001166"){//联调测试
                    var testFlag = $("input[name='testRadio']:checked").val();
                    if (testFlag == '0') {
                        tacheOperInfo.operFlag = "测试通过！";
                        tacheOperInfo.success = true ;
                        operAttrs.isTestRollback = 1;
                    }else if(testFlag == '1') {
                        tacheOperInfo.operFlag = "测试不通过！";
                        tacheOperInfo.success = false;
                        operAttrs.isTestRollback = 0;
                        var childFlowGrid = $("#childFlowGrid").grid("getCheckRows");
                        tacheOperInfo.childFlowData = childFlowGrid;
                    }
                    if (psId == "1000207"){//局内电路
                        if ($("input[name='testRadio']:checked").val() == '0' && FILES === null && !fileData.length > 0){ //测试通过且附件为空
                            fish.error({title:'提示',message:'必须上传附件'})
                            return;
                        }
                        if ($("input[name='testRadio']:checked").val() == '1' && formValue.remark == null){ //测试不通过且说明为空
                            fish.error({title:'提示',message:'说明不能为空'});
                            return;
                        }
                    }
                }else if (tacheId == "500001161"){//全程调测
                    var testFlag = $("input[name='testRadio']:checked").val();
                    if (testFlag == '0') {
                        operAttrs.isAllTestRollback = 1;
                    }else if(testFlag == '1') {
                        operAttrs.isAllTestRollback = 0;
                    }
                    if (psId == "1000212"){//本地电路
                        if ($("input[name='testRadio']:checked").val() == '0' && FILES === null && !fileData.length > 0){ //测试通过且附件为空
                            fish.error({title:'提示',message:'必须上传附件'})
                            return;
                        }
                        if ($("input[name='testRadio']:checked").val() == '1' && formValue.remark == null){ //测试不通过且说明为空
                            fish.error({title:'提示',message:'说明不能为空'});
                            return;
                        }
                    }
                }else if (tacheId == "500001168"){//跨域全程调测
                    if (psId == "1000209"){//跨域电路 跨域全程调测
                        if (FILES === null && !fileData.length > 0){
                            //附件不能为空
                            fish.error({title:'提示',message:'必须上传附件'})
                            return;
                        }
                    }
                }else if (tacheId == "500001157"){//资源分配
                    // debugger
                    var tacheObj_01 = {};
                    var tacheObj_02 = {};
                    var dispObjJobs = new Array();
                    var isConf = true;
                    params.isNeedResConstructRes =  $("input[name='makeResource']:checked").val();
                    //if (userInfo.areaId == '350002000000000042766427'){
                    if (userInfo.ifSelect == '1'){
                        // debugger
                        $.each(circuitData,function (v,obj) {
                            if('未保存' == obj.DF_ORDER_CONFIG){
                                isConf = false;
                            }
                        });
                        if(isConf){
                            if(psIdFlow.PSID != "1000213") { //本地电路的停复机  没有资源施工
                                tacheObj_01.tacheId = '500001158';
                                tacheObj_01.jobIds = formValue.dataMakeSelectJob;
                                dispObjJobs.push(tacheObj_01);
                                params.dispObjJobs = dispObjJobs;
                            }else{
                                tacheObj_01.tacheId = '500001158';
                                tacheObj_01.jobIds = formValue.dataMakeSelectJob;
                                dispObjJobs.push(tacheObj_01);
                                tacheObj_02.tacheId = '500001159';
                                tacheObj_02.jobIds = formValue.resSelectJob;
                                dispObjJobs.push(tacheObj_02);
                                params.dispObjJobs = dispObjJobs;
                            }
                        }else{
                            fish.warn('选中的电路中存在未选择需要派发的岗位,请先保存岗位');
                            return;
                        }
                    }
                }else if (tacheId == "500001155"){//光纤资源分配
                    var tacheObjs = {};
                    var dispObjJobs = new Array();
                    var isConf = true;
                    params.isNeedResConstructResOutside =  $("input[name='makeResourceOutside']:checked").val();
                    //if (userInfo.areaId == '350002000000000042766427'){
                    if (userInfo.ifSelect == '1'){
                        debugger
                        var makeResourceOutsideFlag = $("input[name='makeResourceOutside']:checked").val();
                        if(makeResourceOutsideFlag == '0'){
                            $.each(circuitData,function (v,obj) {
                                if('未保存' == obj.DF_ORDER_CONFIG){
                                    isConf = false;
                                }
                            });
                        }

                        if(isConf){
                            tacheObjs.tacheId = '500001156';
                            tacheObjs.jobIds = formValue.outsideSelectJob;
                            dispObjJobs.push(tacheObjs);
                            params.dispObjJobs = dispObjJobs;
                        }else{
                            fish.warn('选中的电路中存在未选择需要派发的岗位,请先保存岗位');
                            return;
                        }

                    }
                }
                else if(tacheId == "500001162"){//完工确认
                    var finishDate =formValue.finishDate;
                    if((finishDate == null || finishDate == "" || finishDate == undefined) && psId =='1000212'){
                        fish.error({title:'提示',message:'报竣时间不能为空'});
                        return;
                    }
                    params.finishDate = finishDate;
                }
                else if(tacheId == "500001159" && serviceId != "20181221006" && me.options.activeType == "102" && orderPsId == "1000214"){
                    //非局内客户电路拆机流程的“资源施工”环节
                    var param = new Object();
                    param.srvOrdId = me.options.srvOrdId;
                    param.specialtyCode = me.options.specialtyCode;
                    param.isRecycle =  $("#IS_RECYCLE").combobox("value");
                    param.recycleCount =  $("#RECYCLE_COUNT").val();
                    param.equipSequence = $("#EQUIP_SEQUENCE").val();
                    param.removeEquip = $("#REMOVE_EQUIP").val();
                    var result = me.equipRecycleSubmit(param);
                    if(result != undefined && !result){
                        return;
                    }
                }
                else {
                    //TODO:其他操作添加参数
                }

                // 电路调度创建调单
                if (tacheId == "500001153"){
                    var dispatchOrderData = {};
                    dispatchOrderData.dispatchOrderNum = formValue.dispatchOrderNum;
                    dispatchOrderData.dispatchOrderName = formValue.dispatchOrderName;
                    dispatchOrderData.dispatchOrderText = formValue.dispatchOrderText;
                    params.dispatchOrderData = dispatchOrderData;
                    params.remark = formValue.remark;
                }
                if(standardAddress && tacheId == "500001153"){
                    if($("#STANDARD_ADDRESS").val() == "" || $("#STANDARD_ADDRESS").val() == null){
                        fish.toast("warn","请补充标准地址信息");
                        return ;
                    }else{
                        params.standardAddress = $('#STANDARD_ADDRESS').popedit('getValue').value;
                    }
                }
                if(buttonState == "dispConfirm" && tacheId == "500001144"){ //核查调度补单
                    params.checkAddOrder = true;
                }else if(buttonState == "dispConfirm" && tacheId == "500001157"){ //资源分配补单
                    params.resAllocatAddOrder = true;
                }
                //电路调度环节，如果为新开单/变更单提交时，需要提示是否需要手工填写电路编号
                if (tacheId == "500001153" && (sysResource.ACTIVE_TYPE == '101' || sysResource.ACTIVE_TYPE == '103')
                    && sysResource.ORDER_TYPE != '102' && sysResource.SYSTEM_RESOURCE == 'flow-schedule-lt'
                    && sysResource.RESOURCES != 'onedry' && sysResource.RESOURCES != 'secondary'){
                    var flag = false;
                    for (var i=0; i<circuitData.length; i++){
                        if(circuitData[i].CIRCUITCODE == null || circuitData[i].CIRCUITCODE == ''){
                            flag = true;
                        }
                    }
                    if(flag){
                        if (provinceConf.REMARK.indexOf(provinceInfo.ATTR_VALUE) > -1){
                            fish.info("勾选记录中,有电路编号为空,请进行手工填写！");
                            return;
                        }else{
                            fish.confirm(
                                {
                                    title: '提示',
                                    message: '勾选记录中,有电路编号为空,是否进行手工填写！',
                                    ok: '是',
                                    cancel: '否'
                                }
                            ).result.then(
                                //确认按钮事件
                                function dismiss() {
                                    return;
                                },
                                //取消按钮事件
                                function close() {
                                    tacheOperInfo.remark = formValue.remark;
                                    params.operAttrsVal = operAttrs;
                                    //params.childFlowSpecialVal = childFlowSpecialMap;
                                    params.tacheOperInfo = tacheOperInfo;
                                    params.actionFlag = actionFlag;
                                    params.action = "submit";
                                    $.blockUI({message: '派单中...'});
                                    if (FILES === null) {
                                        me.submitOrder(params);
                                    } else {
                                        me.fileUpdate(params); //上传附件并回单
                                    }
                                }
                            );
                        }
                    }else{
                        tacheOperInfo.remark = formValue.remark;
                        params.operAttrsVal = operAttrs;
                        //params.childFlowSpecialVal = childFlowSpecialMap;
                        params.tacheOperInfo = tacheOperInfo;
                        params.actionFlag = actionFlag;
                        params.action = "submit";
                        $.blockUI({message: '派单中...'});
                        if (FILES === null) {
                            me.submitOrder(params);
                        }else {
                            me.fileUpdate(params); //上传附件并回单
                        }
                    }
                }else{
                    tacheOperInfo.remark = formValue.remark;
                    params.operAttrsVal = operAttrs;
                    //params.childFlowSpecialVal = childFlowSpecialMap;
                    params.tacheOperInfo = tacheOperInfo;
                    params.actionFlag = actionFlag;
                    params.action = "submit";
                    $.blockUI({message: '派单中...'});
                    if (FILES === null) {
                        me.submitOrder(params);
                    }else {
                        me.fileUpdate(params); //上传附件并回单
                    }
                }


            }else if(btnFlag == "trans"){//转派
                var obj = $("#tranStaffPopedit").popedit('getValue');
                params.remark = formValue.remark;
                //params.staffId = obj.value;
                params.objId = obj.value;
                params.objType = obj.objType;
                params.action = "trans";
                if(params.objId == ""){
                    fish.error({title:'提示',message:'转派对象不能为空！请选择。。。'});
                    return;
                }
                $.blockUI({message: '转派中...'});
                if (FILES === null) {
                    me.submitOrder(params);
                }else {
                    me.fileUpdate(params);
                }
            }else if(btnFlag == "rollBackOrder"){//退单
                if(tacheId == "500001168"){
                    var provinceList = [];
                    if ("second-schedule-lt" == sysResource.SYSTEM_RESOURCE
                        &&("secondary" == sysResource.RESOURCES
                            || "jike" == sysResource.RESOURCES)){
                        /**
                         * 校验选择数据制作的专业和本地主辅调区域的单子
                         */
                        var specialtyFlag = -1;
                        var localDispatchAreaFlag = -1;
                        var specialtyList = [];
                        $('input[name="specialty"]').each(function(){
                            if($(this).is(':checked')==true){
                                specialtyList.push($(this).val());
                            }
                        });
                        if(specialtyList == null || specialtyList == ""){
                            specialtyFlag = 0;
                        }
                        var resMakeSpecialtyList = [];
                        $('input[name="resMakeSpecialty"]').each(function(){
                            if($(this).is(':checked')==true){
                                resMakeSpecialtyList.push($(this).val());
                            }
                        });
                        if(resMakeSpecialtyList == null || resMakeSpecialtyList == ""){
                            resMakeSpecialtyFlag = 0;
                        }
                        var localDispatchAreaList = [];
                        $('input[name="localDispatchArea"]').each(function(){
                            if($(this).is(':checked')==true){
                                localDispatchAreaList.push($(this).val());
                            }
                        });
                        if(localDispatchAreaList == null || localDispatchAreaList ==""){
                            localDispatchAreaFlag = 0;
                        }
                        if(specialtyFlag == 0 && localDispatchAreaFlag == 0 && resMakeSpecialtyFlag == 0){
                            fish.error({title:'提示',message:'退单数据制作专业，资源施工专业和本地主辅调区域至少选择一个。。。'});
                            return;
                        }
                        params.dataMakeData = specialtyList == null ? "" : specialtyList; //数据制作退单专业
                        params.resMakeData = resMakeSpecialtyList == null ? "" : resMakeSpecialtyList; //资源施工退单专业
                        params.subLocalTestData = localDispatchAreaList == null ? "" : localDispatchAreaList ; //本地主辅调退单区域
                    }else{
                        //跨域全程调测环节退单要选择退单省份，至少选择一个

                        $('input[name="province"]').each(function(){
                            if($(this).is(':checked')==true){
                                provinceList.push($(this).val());
                            }
                        });
                        if(provinceList == null || provinceList == ""){
                            fish.error({title:'提示',message:'退单省份至少选择一个。。。'});
                            return;
                        }
                    }
                    params.province = provinceList == null ? "": provinceList ;
                }
                if(formValue.remark == null){
                    fish.error({title:'提示',message:'说明不能为空'});
                    return;
                }


                params.remark = formValue.remark;
                params.opinion = formValue.opinion;
                params.srvOrdIdFullCom = me.options.srvOrdId;
                params.flag = "LOCAL";
                params.action = "rollBackOrder";
                $.blockUI({message: '退单中...'});
                if (FILES === null) {
                    me.submitOrder(params);
                }else {
                    me.fileUpdate(params);
                    //me.fileUpdate(orderId,woId,srvOrdId,action,params);
                }
            }else if(btnFlag == "goBackOrder"){//回退
                    params.remark = formValue.remark;
                    //退B退单类型
                    params.backReasonType = formValue.backReasonType;
             /*   if((psId == '1000207' || psId == '1000207') && tacheId == '500001153'){ //局内电路  电路调度环节
                    params.flag = "LOCAL";
                    params.action = "rollBackOrder";
                }else {*/
                    params.action = "goBackOrder";
            //    }
                if(params.remark == null){
                    fish.error({title:'提示',message:'说明不能为空'});
                    return;
                }
                $.blockUI({message: '处理中...'});
                if (FILES === null) {
                    me.submitOrder(params);
                }else {
                    me.fileUpdate(params);
                }
            } else if(btnFlag == "resConfig"){//资源配置
                if (circuitData.length != 1) {
                    fish.warn('只能选择一个电路信息！');
                    return false;
                }
                params.circuitData = circuitData; //订单信息
                params.action = "resConfig";
                me.submitOrder(params);
            }
        },
        submitOrder : function(params){
            var me = this;
            if (!me.isSubmit) {
                me.isSubmit = true;
                params.newCreateResource = $("input[name='newResRadio']:checked").val();
                operOrderAction.submitOrder(params, function (res) {
                    me.isSubmit = false;
                    if (res.success) {
                        if (params.action == "resConfig") {
                            if ($("#ExportData").size() < 1) {
                                var formHtml = "<form id=\"ExportData\" action=\"http://www.oschina.net\" target=\"_blank\" method=\"post\">"
                                    + "<input type=\"hidden\" id=\"params\" name=\"body\"/>"
                                    + "</form>";
                                $(document.body).append($(formHtml));
                            }
                            var tempForm = document.getElementById("ExportData");
                            tempForm.action = res.url;
                            var paramsInput = document.getElementById("params");
                            paramsInput.value = res.json;
                            tempForm.submit();
                        } else {
                            $.unblockUI();
                            fish.toast('success', res.message);
                            me.popup.close();
                        }
                    } else {
                        if (params.action != "resConfig") {
                            $.unblockUI();
                        }
                        fish.toast('error', res.message);
                    }
                });
            } else {
                fish.toast('error', "请勿重复提交！");
            }
        },
        /*closeBtn:function () {
         console.log('close');
         this.popup.close("closePage");
         },*/
        resConfig : function(){
            /*var params = new Object();
             params.srvOrdId = srvOrdId;
             params.orderId = orderId;
             params.woId = woId;*/
            var params = new Object();
            var circuitData = $("#circuitGrid").grid("getCheckRows");
            if (circuitData.length !=1) {
                fish.warn('只能选择一条电路信息！');
                return false;
            }
            params.circuitData = circuitData; //订单信息
            operOrderAction.resConfig(params,function(res){
                if(res.success){
                    if ($("#ExportData").size() < 1) {
                        var formHtml = "<form id=\"ExportData\" action=\"http://www.oschina.net\" target=\"_blank\" method=\"post\">"
                            + "<input type=\"hidden\" id=\"params\" name=\"body\"/>"
                            + "</form>";
                        $(document.body).append($(formHtml));
                    }
                    var tempForm = document.getElementById("ExportData");
                    tempForm.action = res.url;
                    var paramsInput = document.getElementById("params");
                    paramsInput.value =  res.json;
                    tempForm.submit();
                } else {
                    alert(res.returndec);
                }
            });

        },

        getAreaAfterParams : function(params){
            var childFlowSpecialMap = new Object();
            var childFlowSpecial = new Object();
            if ($('#areaPopedit').popedit('getValue') != null) {
                childFlowSpecial.areaPopedit = params.areaPopedit == "area" ? "区域" : "NULL";
                childFlowSpecialMap.childFlowSpecial = childFlowSpecial;
                var childFlowSpecialArea = new Object();
                var childFlowSpecialAreaName = new Object();
                if (params.areaPopedit != "请选择派发区域！" && params.areaPopedit != undefined) {
                    childFlowSpecialArea.areaPopedit = params.areaPopedit;
                    if ($('#areaPopedit').popedit('getValue') != null) {
                        childFlowSpecialAreaName.areaPopedit = $('#areaPopedit').popedit('getValue').name;
                    }
                } else {
                    fish.warn('请选择需要派发的区域！');
                    return false;
                }
                childFlowSpecialMap.childFlowSpecialArea = childFlowSpecialArea;
                childFlowSpecialMap.childFlowSpecialAreaName = childFlowSpecialAreaName;
                return childFlowSpecialMap;
            }else {
                fish.warn('最少选择一个区域！');
                return false;
            }
        },

        //电路调度环节获取页面区域选择的参数
        getAreaParams : function(params){
            var childFlowSpecialMap = new Object();
            var childFlowSpecial = new Object();
            if ($("input[name$='Cfg']").is(":checked")) {
                childFlowSpecial.COMPLEX_1 = params.complexCfg == "complex" ? "综合设备" : "NULL";
                childFlowSpecial.OPTICAL_2 = params.opticalCfg == "optical" ? "光纤" : "NULL";
                childFlowSpecial.TRANS_3 = params.transCfg == "trans" ? "传输" : "NULL";
                childFlowSpecial.TRANS_IPRAN_13 = params.transIPRANCfg == "transIPRAN" ? "传输-IPRAN" : "NULL";
                childFlowSpecial.TRANS_MSAP_14 = params.transMSAPCfg == "transMSAP" ? "传输-MSAP" : "NULL";
                childFlowSpecial.DATA_4 = params.dataCfg == "data" ? "数据" : "NULL";
                childFlowSpecial.EXCHANGE_5 = params.exchangeCfg == "exchange" ? "交换" : "NULL";
                childFlowSpecial.ACCESS_6 = params.accessCfg == "access" ? "接入" : "NULL";
                childFlowSpecial.WIRELESS_7 = params.wirelessCfg == "wireless" ? "无线" : "NULL";
                childFlowSpecial.MOBILE_8 = params.mobileCfg == "mobile" ? "移动核心" : "NULL";
                childFlowSpecial.SYN_9 = params.synCfg == "syn" ? "同步网" : "NULL";
                childFlowSpecial.IMS_10 = params.IMSCfg == "IMS" ? "IMS" : "NULL";
                childFlowSpecial.OTHER_11 = params.otherCfg == "other" ? "其他" : "NULL";
                childFlowSpecial.P_DATA_4 = params.p_dataCfg == "p_data" ? "省数据" : "NULL";
                childFlowSpecial.P_EXCHANGE_5 = params.p_exchangeCfg == "p_exchange" ? "省核心网" : "NULL";
                childFlowSpecialMap.childFlowSpecial = childFlowSpecial;
                var childFlowSpecialArea = new Object();
                var childFlowSpecialAreaName = new Object();
                if (childFlowSpecial.COMPLEX_1 == "综合设备") {
                    if (params.complexCfgPopedit != "请选择派发区域！" && params.complexCfgPopedit != undefined) {
                        childFlowSpecialArea.COMPLEX_1 = params.complexCfgPopedit;
                        if ($('#complexCfgPopedit').popedit('getValue') != null) {
                            childFlowSpecialAreaName.COMPLEX_1 = $('#complexCfgPopedit').popedit('getValue').name;
                        }
                    } else {
                        fish.warn('请选择综合设备专业，需要派发的区域！');
                        return false;
                    }
                }
                if (childFlowSpecial.OPTICAL_2 == "光纤") {
                    if (params.opticalCfgPopedit != "请选择派发区域！" && params.opticalCfgPopedit != undefined) {
                        childFlowSpecialArea.OPTICAL_2 = params.opticalCfgPopedit;
                        if ($('#opticalCfgPopedit').popedit('getValue') !=null){
                            childFlowSpecialAreaName.OPTICAL_2 = $('#opticalCfgPopedit').popedit('getValue').name;
                        }
                    } else {
                        fish.warn('请选择光纤专业，需要派发的区域！');
                        return false;
                    }
                }
                if (childFlowSpecial.TRANS_3 == "传输") {
                    if (params.transCfgPopedit != "请选择派发区域！" && params.transCfgPopedit != undefined) {
                        childFlowSpecialArea.TRANS_3 = params.transCfgPopedit;
                        if ($('#transCfgPopedit').popedit('getValue') != null){
                            childFlowSpecialAreaName.TRANS_3 = $('#transCfgPopedit').popedit('getValue').name;
                        }
                    } else {
                        fish.warn('请选择传输专业，需要派发的区域！');
                        return false;
                    }
                }
                if (childFlowSpecial.TRANS_IPRAN_13 == "传输-IPRAN") {
                    if (params.transIPRANCfgPopedit != "请选择派发区域！" && params.transIPRANCfgPopedit != undefined) {
                        childFlowSpecialArea.TRANS_IPRAN_13 = params.transIPRANCfgPopedit;
                        if ($('#transIPRANCfgPopedit').popedit('getValue') != null){
                            childFlowSpecialAreaName.TRANS_IPRAN_13 = $('#transIPRANCfgPopedit').popedit('getValue').name;
                        }
                    } else {
                        fish.warn('请选择传输-IPRAN专业，需要派发的区域！');
                        return false;
                    }
                }
                if (childFlowSpecial.TRANS_MSAP_14 == "传输-MSAP") {
                    if (params.transMSAPCfgPopedit != "请选择派发区域！" && params.transMSAPCfgPopedit != undefined) {
                        childFlowSpecialArea.TRANS_MSAP_14 = params.transMSAPCfgPopedit;
                        if ($('#transMSAPCfgPopedit').popedit('getValue') != null){
                            childFlowSpecialAreaName.TRANS_MSAP_14 = $('#transMSAPCfgPopedit').popedit('getValue').name;
                        }
                    } else {
                        fish.warn('请选择传输-MSAP专业，需要派发的区域！');
                        return false;
                    }
                }
                if (childFlowSpecial.DATA_4 == "数据") {
                    if (params.dataCfgPopedit != "请选择派发区域！" && params.dataCfgPopedit != undefined) {
                        childFlowSpecialArea.DATA_4 = params.dataCfgPopedit;
                        if ($('#dataCfgPopedit').popedit('getValue') != null){
                            childFlowSpecialAreaName.DATA_4 = $('#dataCfgPopedit').popedit('getValue').name;
                        }
                    } else {
                        fish.warn('请选择数据专业，需要派发的区域！');
                        return false;
                    }
                }
                if (childFlowSpecial.EXCHANGE_5 == "交换") {
                    if (params.exchangeCfgPopedit != "请选择派发区域！" && params.exchangeCfgPopedit != undefined) {
                        childFlowSpecialArea.EXCHANGE_5 = params.exchangeCfgPopedit;
                        if ($('#exchangeCfgPopedit').popedit('getValue') != null){
                            childFlowSpecialAreaName.EXCHANGE_5 = $('#exchangeCfgPopedit').popedit('getValue').name;
                        }
                    } else {
                        fish.warn('请选择交换专业，需要派发的区域！');
                        return false;
                    }
                }
                if (childFlowSpecial.ACCESS_6 == "接入") {
                    if (params.accessCfgPopedit != "请选择派发区域！" && params.accessCfgPopedit != undefined) {
                        childFlowSpecialArea.ACCESS_6 = params.accessCfgPopedit;
                        if ($('#accessCfgPopedit').popedit('getValue') != null){
                            childFlowSpecialAreaName.ACCESS_6 = $('#accessCfgPopedit').popedit('getValue').name;
                        }
                    } else {
                        fish.warn('请选择接入专业，需要派发的区域！');
                        return false;
                    }
                }
                if (childFlowSpecial.WIRELESS_7 == "无线") {
                    if (params.wirelessCfgPopedit != "请选择派发区域！" && params.wirelessCfgPopedit != undefined) {
                        childFlowSpecialArea.WIRELESS_7 = params.wirelessCfgPopedit;
                        if ($('#wirelessCfgPopedit').popedit('getValue') != null){
                            childFlowSpecialAreaName.WIRELESS_7 = $('#wirelessCfgPopedit').popedit('getValue').name;
                        }
                    } else {
                        fish.warn('请选择无线专业，需要派发的区域！');
                        return false;
                    }
                }
                if (childFlowSpecial.MOBILE_8 == "移动核心") {
                    if (params.mobileCfgPopedit != "请选择派发区域！" && params.mobileCfgPopedit != undefined) {
                        childFlowSpecialArea.MOBILE_8 = params.mobileCfgPopedit;
                        if ($('#mobileCfgPopedit').popedit('getValue') != null){
                            childFlowSpecialAreaName.MOBILE_8 = $('#mobileCfgPopedit').popedit('getValue').name;
                        }
                    } else {
                        fish.warn('请选择移动核心专业，需要派发的区域！');
                        return false;
                    }
                }
                if (childFlowSpecial.SYN_9 == "同步网") {
                    if (params.synCfgPopedit != "请选择派发区域！" && params.synCfgPopedit != undefined) {
                        childFlowSpecialArea.SYN_9 = params.synCfgPopedit;
                        if ($('#synCfgPopedit').popedit('getValue') != null){
                            childFlowSpecialAreaName.SYN_9 = $('#synCfgPopedit').popedit('getValue').name;
                        }
                    } else {
                        fish.warn('请选择同步网专业，需要派发的区域！');
                        return false;
                    }
                }
                if (childFlowSpecial.IMS_10 == "IMS") {
                    if (params.IMSCfgPopedit != "请选择派发区域！" && params.IMSCfgPopedit != undefined) {
                        childFlowSpecialArea.IMS_10 = params.IMSCfgPopedit;
                        if ($('#IMSCfgPopedit').popedit('getValue') != null){
                            childFlowSpecialAreaName.IMS_10 = $('#IMSCfgPopedit').popedit('getValue').name;
                        }
                    } else {
                        fish.warn('请选择IMS专业，需要派发的区域！');
                        return false;
                    }
                }
                if (childFlowSpecial.OTHER_11 == "其他") {
                    if (params.otherCfgPopedit != "请选择派发区域！" && params.otherCfgPopedit != undefined) {
                        childFlowSpecialArea.OTHER_11 = params.otherCfgPopedit;
                        if ($('#otherCfgPopedit').popedit('getValue') != null){
                            childFlowSpecialAreaName.OTHER_11 = $('#otherCfgPopedit').popedit('getValue').name;
                        }
                    } else {
                        fish.warn('请选择其他专业，需要派发的区域！');
                        return false;
                    }
                }
                if (childFlowSpecial.P_DATA_4 == "省数据") {
                    childFlowSpecialArea.P_DATA_4 = '6';
                    childFlowSpecialAreaName.P_DATA_4 = '福建分公司';
                }
                if (childFlowSpecial.P_EXCHANGE_5 == "省核心网") {
                    childFlowSpecialArea.P_EXCHANGE_5 = '6';
                    childFlowSpecialAreaName.P_EXCHANGE_5 = '福建分公司';
                }
                childFlowSpecialMap.childFlowSpecialArea = childFlowSpecialArea;
                childFlowSpecialMap.childFlowSpecialAreaName = childFlowSpecialAreaName;
                return childFlowSpecialMap;
            }else {
                fish.warn('最少选择一个专业！');
                return false;
            }
        },
        //
        getPostResourceData : function(){
            var checkAreaSpecialMap = new Object();

        },
        //核查调度环节选择区域和专业
        getAreaSpecialCheck : function(params){
            var checkAreaSpecialMap = new Object();
            if (params.resRadio == 0) {
                if ($("input[name$='Special']").is(":checked")) {
                    checkAreaSpecialMap.isOutsideCheck = params.outsideSpecial == "outside" ? 0 : 1;
                    checkAreaSpecialMap.isDataCheck = params.dataSpecial == "data" ? 0 : 1;
                    checkAreaSpecialMap.isTransCheck = params.transSpecial == "trans" ? 0 : 1;
                    checkAreaSpecialMap.isAccessCheck = params.accessSpecial == "access" ? 0 : 1;
                    checkAreaSpecialMap.isOtherCheck = params.otherSpecial == "other" ? 0 : 1;
                    checkAreaSpecialMap.isChangeCheck = params.changeSpecial == "change" ? 0 : 1;
                    var checkAreaSpecialArea = new Object();
                    var checkAreaSpecialAreaName = new Object();
                    if(checkAreaSpecialMap.isOutsideCheck == 0){
                        if (params.outsidePopedit != "请选择派发区域！" && params.outsidePopedit != undefined) {
                            checkAreaSpecialArea.outsideOrg = params.outsidePopedit;
                            if ($('#outsidePopedit').popedit('getValue') != null) {
                                checkAreaSpecialAreaName.outsideOrg = $('#outsidePopedit').popedit('getValue').name;
                            }
                        } else {
                            fish.warn('请选择外线专业对应的派发区域！');
                            return false;
                        }
                    }
                    if(checkAreaSpecialMap.isDataCheck == 0){
                        if (params.dataPopedit != "请选择派发区域！" && params.dataPopedit != undefined) {
                            checkAreaSpecialArea.dataOrg = params.dataPopedit;
                            if ($('#dataPopedit').popedit('getValue') != null){
                                checkAreaSpecialAreaName.dataOrg = $('#dataPopedit').popedit('getValue').name;
                            }
                        } else {
                            fish.warn('请选择数据专业对应的派发区域！');
                            return false;
                        }
                    }
                    if(checkAreaSpecialMap.isTransCheck == 0){
                        if (params.transPopedit != "请选择派发区域！" && params.transPopedit != undefined) {
                            checkAreaSpecialArea.transOrg = params.transPopedit;
                            if ($('#transPopedit').popedit('getValue') != null){
                                checkAreaSpecialAreaName.transOrg = $('#transPopedit').popedit('getValue').name;
                            }
                        } else {
                            fish.warn('请选择传输专业对应的派发区域！');
                            return false;
                        }
                    }
                    if(checkAreaSpecialMap.isAccessCheck == 0){
                        if (params.accessPopedit != "请选择派发区域！" && params.accessPopedit != undefined) {
                            checkAreaSpecialArea.accessOrg = params.accessPopedit;
                            if ($('#accessPopedit').popedit('getValue') != null){
                                checkAreaSpecialAreaName.accessOrg = $('#accessPopedit').popedit('getValue').name;
                            }
                        } else {
                            fish.warn('请选择接入专业对应的派发区域！');
                            return false;
                        }
                    }
                    if(checkAreaSpecialMap.isOtherCheck == 0){
                        if (params.otherPopedit != "请选择派发区域！" && params.otherPopedit != undefined) {
                            checkAreaSpecialArea.otherOrg = params.otherPopedit;
                            if ($('#otherPopedit').popedit('getValue') != null){
                                checkAreaSpecialAreaName.otherOrg = $('#otherPopedit').popedit('getValue').name;
                            }
                        } else {
                            fish.warn('请选择其他专业对应的派发区域！');
                            return false;
                        }
                    }
                    if(checkAreaSpecialMap.isChangeCheck == 0){
                        if (params.changePopedit != "请选择派发区域！" && params.changePopedit != undefined) {
                            checkAreaSpecialArea.changeOrg = params.changePopedit;
                            if ($('#changePopedit').popedit('getValue') != null){
                                checkAreaSpecialAreaName.changeOrg = $('#changePopedit').popedit('getValue').name;
                            }
                        } else {
                            fish.warn('请选择交换专业对应的派发区域！');
                            return false;
                        }
                    }
                    checkAreaSpecialMap.isThoughCheckTatal = 1;
                    checkAreaSpecialMap.checkAreaSpecialArea = checkAreaSpecialArea;
                    checkAreaSpecialMap.checkAreaSpecialAreaName = checkAreaSpecialAreaName;
                }else {
                    fish.warn('最少选择一个专业！');
                    return false;
                }
            } else if (params.resRadio == 1){
                checkAreaSpecialMap.isOutsideCheck = 1;
                checkAreaSpecialMap.isDataCheck = 1;
                checkAreaSpecialMap.isTransCheck = 1;
                checkAreaSpecialMap.isAccessCheck = 1;
                checkAreaSpecialMap.isOtherCheck = 1;
                checkAreaSpecialMap.isChangeCheck = 1;
                checkAreaSpecialMap.isThoughCheckTatal = 0;
            }
            return checkAreaSpecialMap;
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
                    me.isOvertime();

                    var srvOrdId = data.SRV_ORD_ID + '';
                    if (tacheId == "500001168"){
                        if ("second-schedule-lt" == sysResource.SYSTEM_RESOURCE
                            &&("secondary" == sysResource.RESOURCES
                                || "jike" == sysResource.RESOURCES)){
                            me.qrySecondDataMakeLists(srvOrdId);
                            me.qrySecondResMakeLists(srvOrdId);
                            me.qrySubLocalTestDataInfo(srvOrdId);
                        } else {
                            me.getProvince(srvOrdId);
                        }
                    }else if (tacheId == "500001153"){ //判断是否是电路调度环节
                        me.qryCircuitAreaInfo(data);//初始化主调局复选按钮
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
                    }
                    if(tacheId != "500001155" && tacheId != "500001157"){
                        if(data.STATE == '已配置' && !(me.qryCheckFlag())){
                            //回显专业配置信息
                            me.qryCircuitAreaInfo(data)
                            // me.queryPropertyConfig(data);
                        }else if (data.STATE == '未配置'){
                            if(tacheId == "500001153"){//电路调度
                                //置灰所有的专业选择
                                // $("input[name=masterSelectRadio][value= "+res.keyNote+"]").attr("checked",true);
                                $("input[name$='Cfg']").each(function(){
                                    $(this).removeAttr("checked");
                                    $(this).parent().parent().next().find("input").attr("disabled",true);
                                    $(this).parent().parent().next().find("input").popedit('setValue',{name:'请选择派发区域！',value:''});
                                });
                            }
                            if (tacheId == "500001144") {//核查调度
                                //置灰所有核查专业
                                $("input[name$='Special']").each(function(){
                                    $(this).removeAttr("checked");
                                    $(this).parent().parent().next().find("input").attr("disabled",true);
                                    $(this).parent().parent().next().find("input").popedit('setValue',{name:'请选择派发区域！',value:''});
                                });
                            }
                        } else if (tacheId == "500001160" || tacheId == "500001166") {
                            me.qrychildFlowInfo(data.ORDER_ID);
                        }
                        if(me.qryCheckFlag()){
                            // 初始化核查反馈的展示情况
                            //       me.initCheckInfo(data);
                        }

                    }

                },
                onSelectAll: function (e, status){ //全选事件
                    var rowsData = $('#circuitGrid').grid("getCheckRows");
                    //add by wang.gang2 勾选数据是否超时需要展示超时原因
                    me.isOvertime();
                    if (tacheId == "500001153"){ //判断是否是电路调度环节
                        if (psId != '1000209' && psId != '1000210'){//判断是否是跨域，不是一干来单可以刷新调单信息
                            // var checkData = $("#circuitGrid").grid("getCheckRows");
                            // if(checkData.length > 0){
                            //     me.queryDispatchInfoByDispatchIds(checkData);
                            // }else {
                            //     $('#dispatchOrderNum').attr('value','');
                            //     $('#dispatchOrderNum').attr('disabled',true);
                            //     $('#dispatchOrderText').val('');
                            //     $('#dispatchOrderName').attr('value','');
                            // }

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
                    // debugger;
                    var data = $("#circuitGrid").grid("getRowData",rowid);
                    //当iCol为0时，选中的是复选框而不是行数据，则不触发数据回显事件
                    if(0 != iCol){
                        if('500001155' == tacheId || '500001157' == tacheId){
                            me.initSelectSave(data.ORDER_ID);
                        }
                    }
                    if(me.qryCheckFlag()){
                        if(data.CHECK_STATE == '已保存' || tacheId == "500001150" || tacheId == "500001151"){
                            //回显核查反馈信息
                            me.initCheckInfo(data)
                        }else {
                            $('#A_CONSTRUCT_SCHEME').val('');
                            // $('#A_RES_SATISFY').val('');
                            $('#A_RES_SATISFY').combobox('clear');
                            $('#A_ACCESS_ROOM').val('');
                            $('#A_INVESTMENT_AMOUNT').val('');
                            $('#A_CONSTRUCT_PERIOD').val('');
                            $('#Z_CONSTRUCT_SCHEME').val('');
                            // $('#Z_RES_SATISFY').val('');
                            $('#Z_ACCESS_ROOM').val('');
                            $('#Z_RES_SATISFY').combobox('clear');
                            $('#Z_INVESTMENT_AMOUNT').val('');
                            $('#Z_CONSTRUCT_PERIOD').val('');
                            $("input[name$='ACCESS_ROOM']").popedit("clear");
                            me.initCheckInfo(data);
                        }
                        if(tacheId == "500001150"&&sysResource.SYSTEM_RESOURCE!="second-schedule-lt"&&data.CHECK_STATE == '未保存'){
                            //本地核查单，资源提供情况，接入情况，备货情况清空
                            $('#Z_RES_PROVIDE').combobox('clear');
                            $('#Z_RES_ACCESS').combobox('clear');
                            $('#Z_EQUIP_READY').combobox('clear');
                            $('#A_RES_PROVIDE').combobox('clear');
                            $('#A_RES_ACCESS').combobox('clear');
                            $('#A_EQUIP_READY').combobox('clear');
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
            // 设置表格高度
            // $("#circuitGrid").grid("setGridHeight", 150);
            // // 冻结表格复选框一列
            // $("#circuitGrid").grid('setFrozenColumns', 1);
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
        qrySecondDataMakeLists : function(srvOrdId){
            var me = this;
            var param = {};
            param.srvOrdId = srvOrdId;
            operOrderAction.qrySecondDataMakeList(param,function (res) {
                var divStr = "";
                if(res.flag){
                    $.each(res.data,function(i,val){
                        divStr = divStr + '<label class="checkbox-inline">'+
                            '<input type="checkbox" name="specialty" id="' + val.SPECIALTY_CODE
                            + '" value= "' + val.SPECIALTY_CODE +'">'
                            + val.PUB_DATE_NAME
                            +'</label>';
                    });
                    me.$("#subLocalTestData").html("");
                    me.$("#subLocalTestData").append(divStr);
                }else {
                    fish.toast('error', '二干数据制作专业查询失败！');
                }
            });

        },
        qrySecondResMakeLists : function(srvOrdId){
            var me = this;
            var param = {};
            param.srvOrdId = srvOrdId;
            operOrderAction.qrySecondResMakeLists(param,function (res) {
                var divStr = "";
                if(res.flag){
                    $.each(res.data,function(i,val){
                        divStr = divStr + '<label class="checkbox-inline">'+
                            '<input type="checkbox" name="resMakeSpecialty" id="' + val.SPECIALTY_CODE
                            + '" value= "' + val.SPECIALTY_CODE +'">'
                            + val.PUB_DATE_NAME
                            +'</label>';
                    });
                    me.$("#subResMake").html("");
                    me.$("#subResMake").append(divStr);
                }else {
                    fish.toast('error', '二干资源施工专业查询失败！');
                }
            });

        },
        qrySubLocalTestDataInfo : function(srvOrdId,orderId){
            var me = this;
            var param = {};
            param.srvOrdId = srvOrdId + "";
            operOrderAction.qrySubLocalTestDataList(param,function (res) {
                var divStr = "";
                if(res.flag){
                    $.each(res.data,function(i,val){
                        divStr = divStr + '<label class="checkbox-inline">'+
                            '<input type="checkbox" name="localDispatchArea" id="' + val.REGION_ID
                            + '" value= "' + val.REGION_ID + '">'
                            + val.DEPT_NAME
                            +'</label>';
                    });
                    me.$("#secondDataMake").html("");
                    me.$("#secondDataMake").append(divStr);
                }else {
                    fish.toast('error', '本地网主辅调区域查询失败！');
                }
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
            }else if (me.qryCheckFlag()){
                param.woIds = me.options.woId;
                param.specialtyCode = specialtyCode;
                param.regionId = regionId;
                operOrderAction.qrySrvOrdListCheck(param,function (res) {
                    if(res.flag == 1){
                        $("#circuitGrid").grid("reloadData", res.data);
                        //      me.qryCircuitAreaInfo(res.data[0]);
                        me.initCheckInfo(res.data[0]);
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
                        if (res.data[0].TACHE_ID == "500001168"){
                            if ("second-schedule-lt" == sysResource.SYSTEM_RESOURCE
                                &&("secondary" == sysResource.RESOURCES
                                    || "jike" == sysResource.RESOURCES)){
                                me.qrySecondDataMakeLists(res.data[0].SRV_ORD_ID);
                                me.qrySecondResMakeLists(res.data[0].SRV_ORD_ID);
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
        // 保存核查反馈信息
        saveCheckInfo: function(){
            var me = this;
            var tacheId = me.options.tacheId;
            var data = $("#circuitGrid").grid("getCheckRows");
            var formValue = $('#orderOper-form').form('value');
            if (data.length >= 1) {
                var param = {};
                param.circuitData = data;
                param.formValue = formValue;
                param.sysResource=sysResource.SYSTEM_RESOURCE;
                // 校验核查数据
                var checkA = $('#A_INVESTMENT_AMOUNT').isValid();
                var checkZ = $('#Z_INVESTMENT_AMOUNT').isValid();
                var tFlag = (tacheId == "500001145"||tacheId == "500001147"||tacheId == "500001146"||tacheId == "500001148"
                    ||tacheId == "500001149"||tacheId == "510101020")&&sysResource.SYSTEM_RESOURCE=="second-schedule-lt";//二干来单专业核查环节
                if(!tFlag){
                    if(!checkA){
                        fish.warn({title:'提示',message:'不能为空或格式不正确！请修改!'})
                        return;
                    }
                }
                if(!checkZ){
                    fish.warn({title:'提示',message:'不能为空或格式不正确！请修改!'})
                    return;
                }
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
                fish.toast('error', "请至少勾选一条电路信息!");
            }

        },
        validCheckInfo : function(param){
            var data = param.circuitData;
            var formValue = param.formValue;
            var me = this;
            var tacheId = me.options.tacheId;
            // 核查汇总500001150、投资估算500001151$
            var isValidSign = !(tacheId == "500001150" || tacheId == "500001151");
            var feedBackSign = data[0].FEEDBACKSIGN;
            if(feedBackSign=="A" || feedBackSign=="ALL"){
                var A_INVESTMENT_AMOUNT = formValue.A_INVESTMENT_AMOUNT;
                var A_CONSTRUCT_PERIOD = formValue.A_CONSTRUCT_PERIOD;
                var A_CONSTRUCT_SCHEME = formValue.A_CONSTRUCT_SCHEME;
                var ResSatisfyA = $('#A_RES_SATISFY').val();//满足-不满足
                if(isValidSign && A_CONSTRUCT_SCHEME==null && ResSatisfyA == 1){
                    fish.warn('A端接入建设方案、资源情况不能为空！');
                    return false;
                }
                if(ResSatisfyA==""){
                    fish.warn('A端资源是否满足不能为空！');
                    return false;
                }
                // if(ResSatisfyA=="1" && (A_INVESTMENT_AMOUNT==null||A_CONSTRUCT_PERIOD==null)){
                //     fish.warn('A端投资金额或A端建设工期不能为空！');
                //     return false;
                // }
                // if(A_INVESTMENT_AMOUNT<0||A_CONSTRUCT_PERIOD<0){
                //     fish.warn('A端投资金额或A端建设工期不能小于0！');
                //     return false;
                // }
            }
            if(feedBackSign=="Z" || feedBackSign=="ALL"){
                var Z_INVESTMENT_AMOUNT = formValue.Z_INVESTMENT_AMOUNT;
                var Z_CONSTRUCT_PERIOD = formValue.Z_CONSTRUCT_PERIOD;
                var Z_CONSTRUCT_SCHEME = formValue.Z_CONSTRUCT_SCHEME;
                var A_RES_PROVIDE = $('#A_RES_PROVIDE').val();
                var Z_RES_PROVIDE = $('#Z_RES_PROVIDE').val();
                var A_RES_ACCESS = $('#A_RES_ACCESS').val();
                var Z_RES_ACCESS = $('#Z_RES_ACCESS').val();
                var A_EQUIP_READY = $('#A_EQUIP_READY').val();
                var Z_EQUIP_READY = $('#Z_EQUIP_READY').val();
                var ResSatisfyZ = $('#Z_RES_SATISFY').val();//满足-不满足
                if(isValidSign && Z_CONSTRUCT_SCHEME==null && ResSatisfyZ == 1){
                    fish.warn('Z端接入建设方案、资源情况不能为空！');
                    return false;
                }
                if(ResSatisfyZ==""){
                    fish.warn('Z端资源是否满足不能为空！');
                    return false;
                }
                if(feedBackSign=="Z" && tacheId == "500001150"){
                    //添加的资源与备货情况校验数据不能为空
                    if(Z_RES_PROVIDE==""){
                        fish.warn('资源提供方式不能为空！');
                        return false;
                    }
                    if(Z_RES_ACCESS==""){
                        fish.warn('资源接入方式不能为空！');
                        return false;
                    }
                    if(Z_EQUIP_READY==""){
                        fish.warn('备货情况不能为空！');
                        return false;
                    }

                }else{
                    //添加的资源与备货情况校验数据不能为空
                    if(tacheId == "500001150"&&(A_RES_PROVIDE==""||Z_RES_PROVIDE=="")){
                        fish.warn('资源提供方式不能为空！');
                        return false;
                    }
                    if(tacheId == "500001150"&&(A_RES_ACCESS==""||Z_RES_ACCESS=="")){
                        fish.warn('资源接入方式不能为空！');
                        return false;
                    }
                    if(tacheId == "500001150"&&(A_EQUIP_READY==""||Z_EQUIP_READY=="")){
                        fish.warn('备货情况不能为空！');
                        return false;
                    }
                }

                // if(ResSatisfyZ=="1" && (Z_INVESTMENT_AMOUNT==null||Z_CONSTRUCT_PERIOD==null)){
                //     fish.warn('Z端投资金额或Z端建设工期不能为空！');
                //     return false;
                // }
                // if(Z_INVESTMENT_AMOUNT<0||Z_CONSTRUCT_PERIOD<0){
                //     fish.warn('Z端投资金额或Z端建设工期不能小于0！');
                //     return false;
                // }

            }
            return true;
        },
        // 查询是否出现核查反馈页面
        qryCheckFlag:function(){
            var me = this;
            var tacheId = me.options.tacheId;
            var psId = me.options.psId;
            var formValue = $('#orderOper-form').form('value');
            var checkFlag = (psId == "1000211" && tacheId != "500001144") || (tacheId == "500001144" && formValue.resRadio == 1);
            return checkFlag;
        },
        //光纤资源分配、资源分配
        initResourceConstructFn : function(param){
            var me = this;
            var tacheId = me.options.tacheId;
            if('500001155' == tacheId || '500001157' == tacheId){
                if('已保存' == param.DF_ORDER_CONFIG){
                    me.initSelectSave(param.ORDER_ID);
                }else{
                    if('500001155' == tacheId ){
                        $("#outside").popedit('setValue', {name:'', value:''});
                        $("#outsideUser").popedit('setValue', {name:'', value:''});
                    }
                    if('500001157' == tacheId){
                        $("#dataMake").popedit('setValue', {name:'', value:''});
                        $("#resConstruct").popedit('setValue', {name:'', value:''});
                        $("#dataMakeUser").popedit('setValue', {name:'', value:''});
                        $("#resConstructUser").popedit('setValue', {name:'', value:''});
                    }
                    jobData = {};
                    userData = {'userNodeArrayMake':'','userNodeArrayCon':''}
                }
            }

        },
        // 初始化核查反馈页面并查询数据
        initCheckInfo : function (param) {
            var me = this;
            var checkFlag = me.qryCheckFlag();
            var tacheId = me.options.tacheId;
            if(checkFlag) {
                var feedBackSign = param.FEEDBACKSIGN;
                // 核查汇总、投资估算、核查调度

                if (tacheId == "500001150" || tacheId == "500001151" || tacheId == "500001144") {
                    feedBackSign = "ALL";
                }
                //如果是二干发往本地的单子是否派发专业选择否只展示填写一端反馈信息
                if(tacheId == "500001144" && sysResource.SYSTEM_RESOURCE=="second-schedule-lt" && $('#orderOper-form').form('value').resRadio == 1){
                    feedBackSign = "Z";
                }
                if(tacheId == "500001150" && sysResource.SYSTEM_RESOURCE=="second-schedule-lt"){
                    feedBackSign = "Z";
                }
                if(tacheId == "500001151" && sysResource.SYSTEM_RESOURCE=="second-schedule-lt"){
                    feedBackSign = "Z";
                }
                if (feedBackSign == "A") {
                    if (me.options.btnFlag != "resConfig" && me.options.btnFlag != "trans") {
                        $("#feedBackADiv").show();
                    }
                    $("#feedBackZDiv").hide();
                } else if (feedBackSign == "Z") {
                    $("#feedBackADiv").hide();
                    if (me.options.btnFlag != "resConfig" && me.options.btnFlag != "trans") {
                        $("#feedBackZDiv").show();
                    }
                } else if (feedBackSign == "ALL") {
                    if (me.options.btnFlag != "resConfig" && me.options.btnFlag != "trans") {
                        $("#feedBackADiv").show();
                        $("#feedBackZDiv").show();
                    }
                }
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
                //           }
                if (feedBackSign == "A" || feedBackSign == "Z") {
                    if (me.options.btnFlag != "resConfig") {
                        $("input[name$='RES_SATISFY']").each(function () {
                            $(this).parent().prev().html("<span style='color: red'>*</span>资源是否满足:");
                        });
                        $("input[name$='CONSTRUCT_SCHEME']").each(function () {
                            $(this).parent().prev().html("<span style='color: red'>*</span>接入建设方案、资源情况:");
                        });
                        $("#A_CONSTRUCT_SCHEME").parent().prev().html("<span style='color: red'>*</span>接入建设方案、资源情况:");
                        $("#Z_CONSTRUCT_SCHEME").parent().prev().html("<span style='color: red'>*</span>接入建设方案、资源情况:");
                        $("input[name$='ACCESS_ROOM']").each(function () {
                            $(this).parent().parent().prev().html("局端接入机房:");
                        });
                        /*       $("input[name$='INVESTMENT_AMOUNT']").each(function () {
                                   $(this).parent().prev().html("投资金额(万):");
                               });
                               $("#A_INVESTMENT_AMOUNT").parent().prev().html("投资金额(万):");
                               $("#Z_INVESTMENT_AMOUNT").parent().prev().html("投资金额(万):");*/
                        $("input[name$='INVESTMENT_AMOUNT']").spinner({
                            max: 100,
                            min: 0,
                        });
                        /*       $("input[name$='CONSTRUCT_PERIOD']").each(function () {
                                   $(this).parent().prev().html("建设工期(工作日):");
                               });
                               $("#A_CONSTRUCT_PERIOD").parent().prev().html("建设工期(工作日):");
                               $("#Z_CONSTRUCT_PERIOD").parent().prev().html("建设工期(工作日):");*/
                        $("input[name$='CONSTRUCT_PERIOD']").spinner({
                            max: 100,
                            min: 0,
                        });
                        $("#amountA").hide();
                        $("#amount").show();
                        $("#amountZ").hide();
                        $("#amountLabel").show();
                        $("#periodA").hide();
                        $("#period").show();
                        $("#periodZ").hide();
                        $("#periodLabel").show();
                        $("#resProvideZ").hide();
                        $("#resProvide").show();
                        $("#resAccessZ").hide();
                        $("#resAccess").show();
                        $("#equipReadyZ").hide();
                        $("#equipReady").show();
                    }
                } else if (feedBackSign == "ALL") {
                    if (me.options.btnFlag != "resConfig") {
                        $("#A_RES_SATISFY").parent().prev().html("<span style='color: red'>*</span>A端资源是否满足:");
                        $("#Z_RES_SATISFY").parent().prev().html("<span style='color: red'>*</span>Z端资源是否满足:");
                        $("#A_CONSTRUCT_SCHEME").parent().prev().html("A端接入建设方案、资源情况:");
                        $("#Z_CONSTRUCT_SCHEME").parent().prev().html("Z端接入建设方案、资源情况:");
                        $("#A_ACCESS_ROOM").parent().parent().prev().html("A端局端接入机房:");
                        $("#Z_ACCESS_ROOM").parent().parent().prev().html("Z端局端接入机房:");
                        //       $("#A_INVESTMENT_AMOUNT").parent().prev().html("A端投资金额(万):");
                        //       $("#Z_INVESTMENT_AMOUNT").parent().prev().html("Z端投资金额(万):");
                        $("#amount").hide();
                        $("#amountA").show();
                        $("#amountLabel").hide();
                        $("#amountZ").show();
                        $("#period").hide();
                        $("#periodA").show();
                        $("#periodLabel").hide();
                        $("#periodZ").show();
                        $("#amountA").show();
                        $("#amountA").show();
                        $("#resProvideZ").show();
                        $("#resProvide").hide();
                        $("#resAccessZ").show();
                        $("#resAccess").hide();
                        $("#equipReadyZ").show();
                        $("#equipReady").hide();

                        $("input[name$='INVESTMENT_AMOUNT']").spinner({
                            max: 100,
                            min: 0,
                        });
                        $("input[name$='CONSTRUCT_PERIOD']").spinner({
                            max: 100,
                            min: 0,
                        });
                    }
                }
                if (tacheId == "500001150" || tacheId == "500001151") {
                    $("input[name$='CONSTRUCT_SCHEME']").each(function () {
                        $(this).attr('readonly', 'readonly');
                    });
                    $("#A_CONSTRUCT_SCHEME").attr('readonly', 'readonly');
                    $("#Z_CONSTRUCT_SCHEME").attr('readonly', 'readonly');
                    $("#A_ACCESS_ROOM").attr('disabled', true);
                    $("#Z_ACCESS_ROOM").attr('disabled', true);


                    $("input[name$='RES_SATISFY']").each(function(){
                        $(this).combobox('disable');
                    });
                }

                if(tacheId == "500001150"){
                    $("#A_INVESTMENT_AMOUNT").spinner('disable');
                    $("#Z_INVESTMENT_AMOUNT").spinner('disable');
                    $("#A_CONSTRUCT_PERIOD").spinner('disable');
                    $("#Z_CONSTRUCT_PERIOD").spinner('disable');
                    $("#A_ACCESS_ROOM").popedit('disable');
                    $("#Z_ACCESS_ROOM").popedit('disable');
                    $("input[name$='INVESTMENT_AMOUNT']").each(function(){
                        $(this).attr("disabled",true);
                    });
                    $("input[name$='CONSTRUCT_PERIOD']").each(function(){
                        $(this).attr("disabled",true);
                    });
                    if(sysResource.SYSTEM_RESOURCE == "second-schedule-lt"){
                        $("#resAp").hide();
                        $("#resA").hide();
                        $("#resZ").hide();
                        $("#resZp").hide();
                    }else{
                        $("#resAp").show();
                        $("#resA").show();
                        $("#resZ").show();
                        $("#resZp").show();
                    }

                }
                me.queryCheckInfo(param);
            }

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
            params.sysResource=sysResource.SYSTEM_RESOURCE;
            params.RELATE_INFO_ID=param.RELATE_INFO_ID;
            operOrderAction.queryCheckInfo(params,function (res) {
                var arrayData = new Map();
                /* if (tacheId=='500001150' || tacheId=='500001151'){
                     $("#A_ACCESS_ROOM").val(res.data.A_ACCESS_ROOM);
                     $("#Z_ACCESS_ROOM").val(res.data.Z_ACCESS_ROOM)
                 }*/
                arrayData = res.data;
                for(var key in arrayData){
                    var value = arrayData[key];
                    if(key=="A_RES_SATISFY" || key=="Z_RES_SATISFY"){
                        $("#"+key).combobox('value',value);
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
                    //将回显方法写入到主调局方法里，是为了避免回显主调局数据后，主调局数据会再次被初始化覆盖
                    if (param.STATE =='已配置' || '102,104,105'.indexOf(param.ACTIVE_TYPE) != -1) {//拆机停机复机都回显上个动作的配置信息
                        me.queryPropertyConfig(param)
                    }else {
                        if("second-schedule-lt" == res.RESOURCES){
                            $("#otherMaster").attr("disabled",false);
                            $("#otherMaster").popedit('setValue',{name:'请选择区域！',value:''});
                        }else{
                            $("#otherMaster").attr("disabled",true);
                            $("#otherMaster").popedit('setValue',{name:'请选择区域！',value:''});
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
                    // if (res.titleMap.productType == "局内中继电路 "){
                    //     $('#dispatchOrderName').attr('value', '关于【'+ res.titleMap.operateType+'】【'+ res.titleMap.num +'条】【' + res.titleMap.productType +'】的通知');
                    // }
                    // else{
                    //     $('#dispatchOrderName').attr('value','关于【'+res.titleMap.custName +'】【'+ res.titleMap.operateType+'】【'+ res.titleMap.num +'条】【' + res.titleMap.productType +'】的通知');
                    // }
                    // var a =  res.titleMap.textInfo;
                    // var orderText ='';
                    // for (i=0; i < a.length; i++) {
                    //     orderText = orderText + (i+1)+'、电路编号：'+ a[i].CIRCUITCODE +'\n业务号码：'+a[i].SERIAL_NUMBER +'\n业务订单号：'+a[i].TRADE_ID +'\nA端所属区域：'+a[i].AREGION+
                    //         '\nZ端所属区域：'+ a[i].ZREGION + '\n';
                    // }
                    // orderText =  orderText+ '由主调局负责对传输电路进行端到端BER测试，相关分公司配合。\n' +
                    //     '传输质量符合要求后，由相关分公司按电路调令要求时限进行反馈，并通知本公司业务受理部门。测试过程中如有问题，请及时反馈网管中心。'
                    // $('#dispatchOrderText').val(orderText);
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
        //根据调单IDs查询调单信息
        queryDispatchInfoByDispatchIds : function(rowsData, isShow){
            var me = this;
            var param = {};
            param.rowsData = rowsData;
            var dispatchOrderId = '';
            operOrderAction.queryDispatchInfoByDispatchIds(param,function(data){
                if (data.success) {
                    if (data.dispatchInfo.length > 0) {
                        //回显调单编号
                        $('#dispatchOrderNum').attr('value',data.dispatchInfo[0].DISPATCH_ORDER_NO);
                        $('#dispatchOrderNum').attr('disabled',true);
                        dispatchOrderId = data.dispatchInfo[0].DISPATCH_ORDER_ID;
                        if(isShow){
                            //勾选电路只包含一条调单，且不包含未处理的电路，回显原有调单信息
                            $('#dispatchOrderName').val(data.dispatchInfo[0].DISPATCH_TITLE);
                            $('#dispatchOrderText').val(data.dispatchInfo[0].DISPATCH_TEXT)
                        }else{
                            //勾选电路只包含一条调单，且包含未处理的电路时，重新生成调单内容
                            //刷新调单标题和调单内容
                            me.refreshDispatchTitle(rowsData,dispatchOrderId);
                        }
                    }else{
                        //如果勾选的电路没有调单信息则需要重新生成调单编号
                        if (dispatchNum == '-1'){
                            me.getSequenceNum();
                            //填充调单标题和调单内容
                            me.refreshDispatchTitle(rowsData,dispatchOrderId);
                        }else{
                            $('#dispatchOrderNum').attr('value',dispatchNum);
                            $('#dispatchOrderNum').attr('disabled',true);
                            //填充调单标题和调单内容
                            me.refreshDispatchTitle(rowsData,dispatchOrderId);
                        }

                    }
                }else {
                    fish.toast('error', data.message);
                }
            });
        },
        //查询专业配置
        querySpecialtyConfig : function(){
            var serviceId = this.options.serviceId;
            var psId = this.options.psId;
            var param = {};
            param.serviceId = serviceId;
            param.psId = psId;
            operOrderAction.querySpecialtyConfig(param,function(data){
                for (var i=0; i<data.length; i++){
                    $("#"+data[i].SPECIALTY + "").attr("style","margin-bottom: 10px;display:show();");
                }

            });
        },

        confirmSavePropertyConfig : function (){
            var me = this;
            var data = $("#circuitGrid").grid("getCheckRows");
            if (data.length > 0){
                var activeType = new Array();
                data.forEach(function(val,i){
                    activeType.push(val.ACTIVE_TYPE);
                });
                if (activeType.indexOf('102') > -1) {
                    fish.confirm('拆机电路，是否确定按照现在选择的专业区域保存？').result.then(function() {
                        me.savePropertyConfig(activeType);
                    });
                }else {
                    me.savePropertyConfig(activeType);
                }
            }else{
                var newResourceFlag = $("input[name='newResRadio']:checked").val();
                if (newResourceFlag == '0') {
                    fish.toast('error', "请至少勾选一条电路信息，并配置区域!");
                }else if(newResourceFlag == '1') {
                    fish.toast('error', "请至少勾选一条电路信息，并配置专业!");
                }
            }
        },
        //电路调度环节保存电路的专业配置信息
        savePropertyConfig : function(activeType){
            var me = this;
            //主调局信息
            var formValue = $('#orderOper-form').form('value');
            var childFlowSpecialMap = null;
            var newResourceFlag = $("input[name='newResRadio']:checked").val();
            var param = {};
            param.dataInfo = $("#circuitGrid").grid("getCheckRows");
            if (newResourceFlag == '0') {
                childFlowSpecialMap = me.getAreaAfterParams(formValue);
            }else if(newResourceFlag == '1') {
                if ('102,104,105'.indexOf(activeType) != -1){
                    param.masterValue = ""; //拆机停复机不用选择主调局
                } else {
                    if (formValue.masterSelectRadio == "other") {
                        if (formValue.otherMaster == "请选择区域！" || formValue.otherMaster == "" || formValue.otherMaster == undefined) {
                            fish.warn('请选择主调局区域！');
                            return false;
                        } else {
                            param.masterValue = formValue.otherMaster;
                            formValue.masterValue = formValue.otherMaster;
                        }
                    } else {
                        param.masterValue = formValue.masterSelectRadio;
                    }
                }
                childFlowSpecialMap = me.getAreaParams(formValue);
            }
            //指定环节处理人
            var tacheDealUserFlag = $("input[name='tacheDealUserRadio']:checked").val();
            if (tacheDealUserFlag == '0') {
                var tacheDealUserData = new Array();
                var num = 0;
                var obj = $("input[name$='DealUserPopedit']");
                for (var i = 0; i < obj.length; i++) {
                    var tacheUserObj = $("#" + obj.get(i).id).popedit('getValue');
                    if (tacheUserObj != null && tacheUserObj.name != '' && tacheUserObj.value != ''){
                    //if (tacheUserObj != null && tacheUserObj.name != '请选择环节派发对象！' && tacheUserObj.value != ''){
                        var tacheObj = obj.get(i).id;
                        if (obj.get(i).id.indexOf("A") != -1 || obj.get(i).id.indexOf("Z") != -1){
                            num = num + 1;
                        }
                        var paramsMap = {};
                        paramsMap.tacheId = obj.get(i).id;
                        paramsMap.tacheUserObj = tacheUserObj;
                        tacheDealUserData.push(paramsMap);
                    }
                }
                if (num == 1 ){
                    fish.warn('A,Z端本地测试环节处理对象必须都选择！');
                    return false;
                }
                param.tacheDealUserData = tacheDealUserData;
            }
            if(childFlowSpecialMap!=false){
                document.getElementById("saveBtn").setAttribute("disabled", true);
                var specialtyConfig = {};
                var specialtyConfigName = {};
                specialtyConfig = childFlowSpecialMap.childFlowSpecialArea;
                specialtyConfigName = childFlowSpecialMap.childFlowSpecialAreaName;
                param.specialtyConfig = specialtyConfig;
                param.specialtyConfigName = specialtyConfigName;
                param.flowSpecialData = childFlowSpecialMap;
                param.newCreateResource = $("input[name='newResRadio']:checked").val();
                operOrderAction.saveSpecialtyConfigInfo(param,function(res){
                    document.getElementById("saveBtn").removeAttribute("disabled");
                    if (res.success){
                        //专业配置保存完成后，重新刷新电路信息表格数据
                        me.circuitInfo();
                        fish.toast('success', res.message);
                    } else{
                        fish.toast('error', res.message);
                    }
                });
            }

        },
        //回显电路对应的专业配置信息
        queryPropertyConfig:function(data){
            var me = this;
            var woState = me.options.woState;
            var param = {};
            param.srvOrdId = data.SRV_ORD_ID;
            param.cstOrdId = data.CST_ORD_ID;
            param.orderId = data.ORDER_ID;
            param.activeType = data.ACTIVE_TYPE;
            var tacheId = this.options.tacheId;
            param.newCreateResource = $("input[name='newResRadio']:checked").val();
            operOrderAction.queryPropertyConfig(param,function(res){
                if (res.success){
                    if (res.message != '102,104,105') {
                        if(tacheId == '500001153'){ //电路调度环节
                            var tacheDealUserFlag = $("input[name='tacheDealUserRadio']:checked").val();
                            if (tacheDealUserFlag == '0') {
                                me.tacheDealUserByOrderId(data.ORDER_ID);
                            }
                            //每次回显前将所有专业配置置灰
                            $("input[name$='Cfg']").each(function(){
                                $(this).removeAttr("checked");
                                $(this).parent().parent().next().find("input").attr("disabled",true);
                                $(this).parent().parent().next().find("input").popedit('setValue',{name:'请选择派发区域！',value:''});
                            });
                            if (res.configInfo != null){
                                //通过each函数取得所有input的值
                                var titles = "";
                                var strs = new Array();
                                $("input[name='masterSelectRadio']").each(function(){
                                    titles += $(this).val()+",";
                                });
                                //定义一数组
                                strs = titles.split(",");
                                var flag = false; //其他
                                for (var i = 0; i < strs.length ;i++){
                                    if (strs[i] == res.keyNote){ //后台返回的主调局选择是AZ端
                                        $("input[name=masterSelectRadio][value= "+res.keyNote+"]").attr("checked",true);
                                        flag = true;
                                    }
                                }
                                if (flag == false){ //选中其他
                                    $("#OTHERNAME").prop("checked",true);
                                    $("#otherMaster").removeAttr("disabled");
                                    $("#otherMaster").popedit('setValue', {name:res.keyName, value:res.keyNote});
                                }else {
                                    $("#otherMaster").prop("disabled",true);
                                    $("#otherMaster").popedit('setValue', {name:'请选择区域！', value:''});
                                }
                                if (res.configInfo.areaPopedit != undefined){
                                    $("#areaPopedit").popedit('setValue', {name:res.configInfoName.areaPopedit, value:res.configInfo.areaPopedit});
                                }
                                if (res.configInfo.COMPLEX_1 != undefined){
                                    $("input[name=complexCfg]").prop("checked",true);
                                    $("input[name=complexCfgPopedit]").removeAttr("disabled");
                                    $("#complexCfgPopedit").popedit('setValue', {name:res.configInfoName.COMPLEX_1, value:res.configInfo.COMPLEX_1});
                                }
                                if (res.configInfo.OPTICAL_2 != undefined){
                                    $("input[name=opticalCfg]").prop("checked",true);
                                    $("input[name=opticalCfgPopedit]").removeAttr("disabled");
                                    $("#opticalCfgPopedit").popedit('setValue', {name:res.configInfoName.OPTICAL_2, value:res.configInfo.OPTICAL_2});
                                }
                                if (res.configInfo.TRANS_3 != undefined){
                                    $("input[name=transCfg]").prop("checked",true);
                                    $("input[name=transCfgPopedit]").removeAttr("disabled");
                                    $("#transCfgPopedit").popedit('setValue', {name:res.configInfoName.TRANS_3, value:res.configInfo.TRANS_3});
                                }
                                if (res.configInfo.TRANS_IPRAN_13 != undefined){
                                    $("input[name=transIPRANCfg]").prop("checked",true);
                                    $("input[name=transIPRANCfgPopedit]").removeAttr("disabled");
                                    $("#transIPRANCfgPopedit").popedit('setValue', {name:res.configInfoName.TRANS_IPRAN_13, value:res.configInfo.TRANS_IPRAN_13});
                                }
                                if (res.configInfo.TRANS_MSAP_14 != undefined){
                                    $("input[name=transMSAPCfg]").prop("checked",true);
                                    $("input[name=transMSAPCfgPopedit]").removeAttr("disabled");
                                    $("#transMSAPCfgPopedit").popedit('setValue', {name:res.configInfoName.TRANS_MSAP_14, value:res.configInfo.TRANS_MSAP_14});
                                }
                                if (res.configInfo.DATA_4 != undefined){
                                    $("input[name=dataCfg]").prop("checked",true);
                                    $("input[name=dataCfgPopedit]").removeAttr("disabled");
                                    $("#dataCfgPopedit").popedit('setValue', {name:res.configInfoName.DATA_4, value:res.configInfo.DATA_4});
                                }
                                if (res.configInfo.EXCHANGE_5 != undefined){
                                    $("input[name=exchangeCfg]").prop("checked",true);
                                    $("input[name=exchangeCfgPopedit]").removeAttr("disabled");
                                    $("#exchangeCfgPopedit").popedit('setValue', {name:res.configInfoName.EXCHANGE_5, value:res.configInfo.EXCHANGE_5});
                                }
                                if (res.configInfo.ACCESS_6 != undefined){
                                    $("input[name=accessCfg]").prop("checked",true);
                                    $("input[name=accessCfgPopedit]").removeAttr("disabled");
                                    $("#accessCfgPopedit").popedit('setValue', {name:res.configInfoName.ACCESS_6, value:res.configInfo.ACCESS_6});
                                }
                                if (res.configInfo.WIRELESS_7 != undefined){
                                    $("input[name=wirelessCfg]").prop("checked",true);
                                    $("input[name=wirelessCfgPopedit]").removeAttr("disabled");
                                    $("#wirelessCfgPopedit").popedit('setValue', {name:res.configInfoName.WIRELESS_7, value:res.configInfo.WIRELESS_7});
                                }
                                if (res.configInfo.MOBILE_8 != undefined){
                                    $("input[name=mobileCfg]").prop("checked",true);
                                    $("input[name=mobileCfgPopedit]").removeAttr("disabled");
                                    $("#mobileCfgPopedit").popedit('setValue', {name:res.configInfoName.MOBILE_8, value:res.configInfo.MOBILE_8});
                                }
                                if (res.configInfo.SYN_9 != undefined){
                                    $("input[name=synCfg]").prop("checked",true);
                                    $("input[name=synCfgPopedit]").removeAttr("disabled");
                                    $("#synCfgPopedit").popedit('setValue', {name:res.configInfoName.SYN_9, value:res.configInfo.SYN_9});
                                }
                                if (res.configInfo.IMS_10 != undefined){
                                    $("input[name=IMSCfg]").prop("checked",true);
                                    $("input[name=IMSCfgPopedit]").removeAttr("disabled");
                                    $("#IMSCfgPopedit").popedit('setValue', {name:res.configInfoName.IMS_10, value:res.configInfo.IMS_10});
                                }
                                if (res.configInfo.OTHER_11 != undefined){
                                    $("input[name=otherCfg]").prop("checked",true);
                                    $("input[name=otherCfgPopedit]").removeAttr("disabled");
                                    $("#otherCfgPopedit").popedit('setValue', {name:res.configInfoName.OTHER_11, value:res.configInfo.OTHER_11});
                                }
                                if (res.configInfo.P_DATA_4 != undefined){
                                    $("input[name=p_dataCfg]").prop("checked",true);
                                    //$("input[name=p_dataCfgPopedit]").removeAttr("disabled");
                                    //$("#p_dataCfgPopedit").popedit('setValue', {name:res.configInfoName.P_DATA_4, value:res.configInfo.P_DATA_4});
                                }
                                if (res.configInfo.P_EXCHANGE_5 != undefined){
                                    $("input[name=p_exchangeCfg]").prop("checked",true);
                                    //$("input[name=p_exchangeCfgPopedit]").removeAttr("disabled");
                                    //$("#p_exchangeCfgPopedit").popedit('setValue', {name:res.configInfoName.P_EXCHANGE_5, value:res.configInfo.P_EXCHANGE_5});
                                }
                            }
                        }else if(tacheId == '500001144'){ //核查调度环
                            //每次回显前置灰所有核查专业
                            $("input[name$='Special']").each(function(){
                                $(this).removeAttr("checked");
                                $(this).parent().parent().next().find("input").attr("disabled",true);
                                $(this).parent().parent().next().find("input").popedit('setValue',{name:'请选择派发区域！',value:''});
                            });
                            if (res.configInfo != null) {
                                // $("input[name=masterSelectRadio][value= "+res.keyNote+"]").attr("checked",true);
                                if (res.configInfo.outsideOrg != undefined) {
                                    $("input[name=outsideSpecial]").prop("checked", true);
                                    $("input[name=outsidePopedit]").removeAttr("disabled");
                                    $("#outsidePopedit").popedit('setValue', {name: res.configInfoName.outsideOrg, value: res.configInfo.outsideOrg});
                                }
                                if (res.configInfo.dataOrg != undefined) {
                                    $("input[name=dataSpecial]").prop("checked", true);
                                    $("input[name=dataPopedit]").removeAttr("disabled");
                                    $("#dataPopedit").popedit('setValue', {name: res.configInfoName.dataOrg, value: res.configInfo.dataOrg});
                                }
                                if (res.configInfo.changeOrg != undefined) {
                                    $("input[name=changeSpecial]").prop("checked", true);
                                    $("input[name=changePopedit]").removeAttr("disabled");
                                    $("#changePopedit").popedit('setValue', {name: res.configInfoName.changeOrg, value: res.configInfo.changeOrg});
                                }
                                if (res.configInfo.transOrg != undefined) {
                                    $("input[name=transSpecial]").prop("checked", true);
                                    $("input[name=transPopedit]").removeAttr("disabled");
                                    $("#transPopedit").popedit('setValue', {name: res.configInfoName.transOrg, value: res.configInfo.transOrg});
                                }
                                if (res.configInfo.accessOrg != undefined) {
                                    $("input[name=accessSpecial]").prop("checked", true);
                                    $("input[name=accessPopedit]").removeAttr("disabled");
                                    $("#accessPopedit").popedit('setValue', {name: res.configInfoName.accessOrg, value: res.configInfo.accessOrg});
                                }
                                if (res.configInfo.otherOrg != undefined) {
                                    $("input[name=otherSpecial]").prop("checked", true);
                                    $("input[name=otherPopedit]").removeAttr("disabled");
                                    $("#otherPopedit").popedit('setValue', {name: res.configInfoName.otherOrg, value: res.configInfo.otherOrg});
                                }
                            }
                        }
                    }
                } else{
                    fish.toast('error', res.message);
                }
            });
        },

        //初始化岗位
        /*initSelectJob : function (orderId) {
            var param = {};
            param.orderId = orderId;
            operOrderAction.qryDispObj(param,function (resData) {
                var initData = {};
                if(resData == null){
                    $("#dataMake").popedit('setValue', {name:'', value:''});
                    $("#resConstruct").popedit('setValue', {name:'', value:''});
                    $("#outside").popedit('setValue', {name:'', value:''});
                }else {
                    var nodeArray = new Array;
                    resData.forEach(function(val,i){
                        var objId = val.DISP_OBJ_ID.split(',');
                        objId.forEach(function (data,j) {
                            var nodeSin = new Object();
                            nodeSin.id = data;
                            nodeArray.push(nodeSin);
                        });
                        if (val.TACHE_ID == '500001158') { //数据制作
                            $("#dataMake").popedit('setValue', {name:val.JOBNAME, value:val.DISP_OBJ_ID});
                        }else if (val.TACHE_ID == '500001159') { //资源施工
                            $("#resConstruct").popedit('setValue', {name:val.JOBNAME, value:val.DISP_OBJ_ID});
                        }else if (val.TACHE_ID == '500001156') { //外线施工
                            $("#outside").popedit('setValue', {name:val.JOBNAME, value:val.DISP_OBJ_ID});
                        }
                    })
                    jobData = nodeArray;
                }

            });

        },*/
        //初始化已保存的岗位
        initSelectSave : function(orderId){
            var param = {};
            param.orderId = orderId;
            operOrderAction.qryDispObjByOrderId(param,function (resData) {
                // debugger
                if(resData != null){
                    var nodeArray = new Array;
                    var userNodeArrayMake = new Array;
                    var userNodeArrayCon = new Array;
                    resData.forEach(function(val,i){
                        var obj = '';
                        var user = '';
                        if (val.JOBOBJ != '' && val.JOBOBJ != undefined) {
                            obj = val.JOBOBJ;
                            obj.forEach(function (data,j) {
                                var nodeSin = new Object();
                                nodeSin = data;
                                nodeArray.push(nodeSin);
                            });
                        }
                        if (val.USEROBJ != '' && val.USEROBJ != undefined) {
                            user = val.USEROBJ;
                        }
                        if (val.TACHE_ID == '500001158') { //数据制作
                            $("#dataMake").popedit('setValue', {name:val.JOBNAME, value:obj});
                            $("#dataMakeUser").popedit('setValue', {name:val.USERNAME, value:user});//选择人员文本框赋值
                            userNodeArrayMake = val.USEROBJ;
                        }else if (val.TACHE_ID == '500001159') { //资源施工
                            $("#resConstruct").popedit('setValue', {name:val.JOBNAME, value:obj});
                            $("#resConstructUser").popedit('setValue', {name:val.USERNAME, value:user});//选择人员文本框赋值
                            userNodeArrayCon = val.USEROBJ;
                        }else if (val.TACHE_ID == '500001156') { //外线施工
                            $("#outside").popedit('setValue', {name:val.JOBNAME, value:obj});
                            $("#outsideUser").popedit('setValue', {name:val.USERNAME, value:user});//选择人员文本框赋值
                            userNodeArrayCon = val.USEROBJ;
                        }
                    })
                    jobData = nodeArray;
                    userData.userNodeArrayMake = userNodeArrayMake;
                    userData.userNodeArrayCon = userNodeArrayCon;
                }
            });

        },
        //电路专业信息配置完成后刷新调单内容
        refreshDispatchInfo : function(data){
            var me = this;
            var cstOrdId = me.options.cstOrdId + '';
            var param = {};
            param.cstOrdId = cstOrdId;
            // param.orderIds = orderIds;
            operOrderAction.getDispatchInfo(param,function(res){
                if (res.success){
                    // $('#dispatchOrderName').attr('value','关于【'+res.custName +'】【'+ res.operateType+'】【'+ data.length +'条】【' + res.productType +'】的通知');
                    var a =  res.textInfo;
                    var orderText ='';
                    for (i=0; i < a.length; i++) {
                        orderText = orderText + (i + 1) + '、';
                        if (a[i].CIRCUITCODE) {
                            orderText += '电路编号：' + a[i].CIRCUITCODE + ';\n';
                        }
                        if (a[i].SERIAL_NUMBER) {
                            orderText += '业务号码：' + a[i].SERIAL_NUMBER + ';\n';
                        }
                        if (a[i].TRADE_ID) {
                            orderText += '业务订单号：' + a[i].TRADE_ID + ';\n';
                        }
                        if (a[i].AREGION) {
                            orderText += 'A端所属区域：' + a[i].AREGION + ';\n';
                        }
                        if (a[i].ZREGION) {
                            orderText += 'Z端所属区域：' + a[i].ZREGION + ';\n';
                        }

                    }
                    orderText =  orderText+ '由主调局负责对传输电路进行端到端BER测试，相关分公司配合。\n' +
                        '传输质量符合要求后，由相关分公司按电路调令要求时限进行反馈，并通知本公司业务受理部门。测试过程中如有问题，请及时反馈网管中心。'
                    $('#dispatchOrderText').val(orderText);
                } else{
                    fish.toast('error', res.message);
                }
            });
        },
        //勾选数据时刷新调单内容
        refreshDispatchTitle: function(rowsData,dispatchOrderId){
            var me = this;
            var cstOrdId = me.options.cstOrdId + '';
            var param = {};
            param.cstOrdId = cstOrdId;
            param.rowsData = rowsData;
            param.dispatchOrderId = dispatchOrderId;
            //查询勾选电路的相关信息拼接调单内容

            // var orderText ='';
            // for (i=0; i < data.length; i++) {
            //     orderText = orderText + (i+1)+'、电路编号：'+ data[i].CIRCUITCODE +'\n业务号码：'+data[i].SERIAL_NUMBER +'\n业务订单号：'+data[i].TRADE_ID +'\nA端所属区域：'+data[i].AREGIONNAME+
            //         '\nZ端所属区域：'+ data[i].ZREGIONNAME + '\n';
            // }
            // orderText =  orderText+ '由主调局负责对传输电路进行端到端BER测试，相关分公司配合。\n' +
            //     '传输质量符合要求后，由相关分公司按电路调令要求时限进行反馈，并通知本公司业务受理部门。测试过程中如有问题，请及时反馈网管中心。'
            // $('#dispatchOrderText').val(orderText);
            operOrderAction.getDispatchInfo(param,function(res){
                if (res.success) {
                    //填调单标题
                    if (res.productType == "局内中继电路 "){
                        // $('#dispatchOrderName').attr('value', '关于【' + res.operateType + '】【' + data.length + '条】【' + res.productType + '】的通知');
                        $('#dispatchOrderName').val('关于【' + res.operateType + '】【' + res.countNum + '条】【' + res.productType + '】的通知');
                    }
                    else{
                        // $('#dispatchOrderName').attr('value', '关于【' + res.custName + '】【' + res.operateType + '】【' + data.length + '条】【' + res.productType + '】的通知');
                        $('#dispatchOrderName').val('关于【' + res.custName + '】【' + res.operateType + '】【' + res.countNum + '条】【' + res.productType + '】的通知');
                    }
                    //填调单内容
                    var orderText ='';
                    for (i=0; i < res.dispatchTextList.length; i++) {
                        orderText = orderText + (i + 1) + '、';
                        if (res.dispatchTextList[i].CIRCUITCODE) {
                            orderText += '电路编号：' + res.dispatchTextList[i].CIRCUITCODE + ';\n';
                        }
                        if (res.dispatchTextList[i].SERIAL_NUMBER) {
                            orderText += '业务号码：' + res.dispatchTextList[i].SERIAL_NUMBER + ';\n';
                        }
                        if (res.dispatchTextList[i].TRADE_ID) {
                            orderText += '业务订单号：' + res.dispatchTextList[i].TRADE_ID + ';\n';
                        }
                        if (res.dispatchTextList[i].AREGION) {
                            orderText += 'A端所属区域：' + res.dispatchTextList[i].AREGION + ';\n';
                        }
                        if (res.dispatchTextList[i].ZREGION) {
                            orderText += 'Z端所属区域：' + res.dispatchTextList[i].ZREGION + ';\n';
                        }

                    }
                    orderText =  orderText+ '由主调局负责对传输电路进行端到端BER测试，相关分公司配合。\n' +
                        '传输质量符合要求后，由相关分公司按电路调令要求时限进行反馈，并通知本公司业务受理部门。测试过程中如有问题，请及时反馈网管中心。'
                    $('#dispatchOrderText').val(orderText);
                }else{
                    fish.toast('error', res.message);
                }
            });
        },
        getProvince : function (srvOrdId) {
            var me = this;
            var params = {};
            params.srvOrdId = srvOrdId;
            operOrderAction.getProvinceName(params,function (res) {
                var divStr = "";
                $.each(res,function(i,val){
                    divStr = divStr + '<label class="checkbox-inline">'+
                        '<input type="checkbox" name="province" id="' + i + '" value= "' + val.province + '" checked="true" >'
                        + val.province
                        +'</label>';
                });
                //$("#provinceDiv").replaceWith(divStr);
                me.$("#backAZProvinceDiv").html("");
                me.$("#backAZProvinceDiv").append(divStr);

            })
        },
        //核查调度环节保存核查专业
        saveSpecialConfig:function(){
            var me = this;
            var data = $("#circuitGrid").grid("getCheckRows");
            var formValue = $('#orderOper-form').form('value');
            if(data.length>0){
                var param = {};
                param.dataInfo = data;
                param.masterValue = '';
                var childFlowSpecialMap = me.getAreaSpecialCheck(formValue);
                if(childFlowSpecialMap != false){
                    var specialtyConfig = {};
                    var specialtyConfigName = {};
                    specialtyConfig = childFlowSpecialMap.checkAreaSpecialArea;
                    specialtyConfigName = childFlowSpecialMap.checkAreaSpecialAreaName;
                    param.specialtyConfig = specialtyConfig;
                    param.specialtyConfigName = specialtyConfigName;
                    param.flowSpecialData = childFlowSpecialMap;
                    param.newCreateResource = $("input[name='newResRadio']:checked").val();
                    operOrderAction.saveSpecialtyConfigInfo(param,function(res){
                        if (res.success){
                            //专业配置保存完成后，重新刷新电路信息表格数据
                            me.circuitInfo();
                            fish.toast('success', res.message);
                        } else{
                            fish.toast('error', res.message);
                        }
                    });
                }
            }else{
                fish.toast('error', "请至少勾选一条电路信息，并配置专业!");
            }
        },
        //资源分配环节保存岗位
        resourceSaveBtnConfig:function(){
            var me = this;
            var data = $("#circuitGrid").grid("getCheckRows");
            var formValue = $('#orderOper-form').form('value');
            // debugger
            if(data.length>0){
                var dataMake = formValue.dataMakeSelectJob;
                var resConstruct = formValue.resSelectJob;
                var dataMakeUser = formValue.dataMakeSelectUser;
                var resConstructUser = formValue.resConstructSelectUser;
                var makeResourceFlag = $("input[name='makeResource']:checked").val();
                var param = {};
                if((dataMake == undefined || dataMake == '') && (dataMakeUser == undefined || dataMakeUser == '')){
                    fish.warn('至少选择一项数据制作派发的岗位和人员！');
                    return;
                }
                if("1000213" != psIdFlow.PSID ) { //本地电路的停复机  没有资源施工
                    if(makeResourceFlag == '0'){
                        if((resConstruct == undefined || resConstruct == '') && (resConstructUser == undefined || resConstructUser == '')){
                            fish.warn('至少选择一项资源施工派发的岗位和人员！');
                            return;
                        }
                        param.resConstruct = resConstruct;
                    }
                }
                param.dataInfo = data;
                param.dataMake = dataMake;
                param.dataMakeUser = dataMakeUser;
                param.resConstructUser = resConstructUser;
                param.tacheId = this.options.tacheId;
                param.flag = 'other';
                operOrderAction.saveResConstructConfigInfo(param,function(res){
                    if (res.success){
                        //数据制作、资源施工对应的岗位
                        me.circuitInfo();
                        fish.toast('warn', res.message);
                    } else{
                        fish.toast('warn', res.message);
                    }
                });
            }else{
                fish.toast('warn', "请至少勾选一条电路信息，并配置岗位!");
            }

        },
        //光纤资源分配保存岗位
        outsideSaveBtnConfig :function(){
            var me = this;
            var data = $("#circuitGrid").grid("getCheckRows");
            var formValue = $('#orderOper-form').form('value');
            // debugger
            if(data.length>0){
                var makeResourceOutsideFlag = $("input[name='makeResourceOutside']:checked").val();
                if(makeResourceOutsideFlag == '0'){//是
                    var outside = formValue.outsideSelectJob; //外线施工的岗位
                    var outsideUser = formValue.outsideSelectUser; //外线施工的人员
                    if((outside == undefined || outside == '') && (outsideUser == undefined || outsideUser == '')){
                        fish.warn('至少选择一项外线施工派发的岗位和人员！');
                        return;
                    }
                    var param = {};
                    param.dataInfo = data;
                    param.outside = outside;
                    param.outsideUser = outsideUser;
                    param.tacheId = this.options.tacheId;
                    param.flag = 'outside';
                    operOrderAction.saveResConstructConfigInfo(param,function(res){
                        if (res.success){
                            //外线施工对应的岗位
                            me.circuitInfo();
                            fish.toast('warn', res.message);
                        } else{
                            fish.toast('warn', res.message);
                        }
                    });
                }

            }else{
                fish.toast('warn', "请至少勾选一条电路信息，并配置岗位!");
            }

        },
        initGridInfo:function(){
            var tacheId = this.options.tacheId;
            var psId = this.options.psId;
            // 资源配置按钮是否隐藏状态
            var checkSave = $("#configBtnDiv").is(":hidden");
            var selectSpecialFlag = $("input[name='resRadio']:checked").val();
            // debugger
            if(tacheId == '500001153') { //电路调度
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
                    {name: 'A_IF_RES_HAVE', label: 'A端资源是否具备', width: 100, formatter: this.azIfResHaveEnum},
                    {name: 'Z_IF_RES_HAVE', label: 'Z端资源是否具备', width: 100, formatter: this.azIfResHaveEnum},
                    {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                    {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                    {name: 'SERIAL_NUMBER', label: '业务号码', width: 100 , sortable: false },
                    {name: 'AREGIONNAME', label: 'A端所属区域', width: 110, sortable: false},
                    {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110, sortable: false},
                    {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                    {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
                ]
            }else if( tacheId == '500001144'&& checkSave && selectSpecialFlag=='0') { // 核查调度(选专业)
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
            }else if((tacheId == '500001144'&& checkSave&& selectSpecialFlag=='1') || (psId == '1000211' && tacheId != "500001144") ){

                // 核查调度不选专业、核查流程(非核查调度环节)
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
                    {name: 'CIRCUITCODE', label: '电路编号', width: 100, sortable: false },
                    {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                    {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                    {name: 'SERIAL_NUMBER', label: '业务号码', width: 100 , sortable: false },
                    {name: 'AREGIONNAME', label: 'A端所属区域', width: 110, sortable: false},
                    {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110, sortable: false},
                    {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                    {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
                ]
            }else if(tacheId == '500001155' || tacheId == '500001157') {
                return [
                    {name: 'DF_ORDER_CONFIG', label: '岗位配置', width: 95, sortable: false, hidden: true,
                        formatter: function(cellval, opts, rwdat, _act) {
                            // debugger
                            if(cellval == '已保存'){
                                return '<div class="btn-group">' +
                                    '<button type="button" class="btn btn-link js-delete" style="color: #6DCC4A">'+cellval+'</button>' +
                                    '</div>';
                            }else{
                                return '<div class="btn-group btn btn-link" style="color: #FF5858">'+cellval +
                                    '</div>';
                            }
                        }},
                    {name: 'CIRCUITCODE', label: '电路编号', width: 100, sortable: false },
                    {name: 'A_IF_RES_HAVE', label: 'A端资源是否具备', width: 100, formatter: this.azIfResHaveEnum},
                    {name: 'Z_IF_RES_HAVE', label: 'Z端资源是否具备', width: 100, formatter: this.azIfResHaveEnum},
                    {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                    {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                    {name: 'AREGIONNAME', label: 'A端所属区域', width: 110, sortable: false},
                    {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110, sortable: false},
                    {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                    {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
                ]

            }else {
                return [
                    // {name: 'STATE', label: '专业配置', width: 135, sortable: false,
                    //     formatter: function(cellval, opts, rwdat, _act) {
                    //         if(cellval == '已配置'){
                    //             return '<div class="btn-group">' +
                    //                 '<button type="button" class="btn btn-link js-delete">'+cellval+'</button>' +
                    //                 '</div>';
                    //         }else{
                    //             return '<div class="btn-group btn" class="btn-link">'+cellval +
                    //                 '</div>';
                    //         }
                    //     }},
                    {name: 'CIRCUITCODE', label: '电路编号', width: 95, sortable: false },
                    {name: 'A_IF_RES_HAVE', label: 'A端资源是否具备', width: 100, formatter: this.azIfResHaveEnum},
                    {name: 'Z_IF_RES_HAVE', label: 'Z端资源是否具备', width: 100, formatter: this.azIfResHaveEnum},
                    {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                    {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                    {name: 'SERIAL_NUMBER', label: '业务号码', width: 100 , sortable: false },
                    {name: 'AREGIONNAME', label: 'A端所属区域', width: 110, sortable: false},
                    {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110, sortable: false},
                    {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                    {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
                ]
            }
        },
        azIfResHaveEnum: function (value) {
            var enumTypeMap = {
                '0':'否',
                '1':'是',
                '':''
            };
            return enumTypeMap[value];
        },
        childFlowInfo : function () {
            var me = this;
            var data = $("#circuitGrid").grid("getRowData")[0];
            $("#childFlowGrid").grid({
                colModel: [
                    {name: 'SPECIALTYNAME', label: '专业', width: 200, sortable: false },
                    {name: 'AREANAME', label: '区域', width: 290 }
                ],
                width: 516,
                multiselect: true,
                shrinkToFit: false,
                pageData: me.qrychildFlowInfo(data.ORDER_ID),
            });
            $("#childFlowGrid").grid("setGridHeight", 150);
        },
        qrychildFlowInfo : function (orderId) {
            var me = this;
            var param = {};
            param.orderId = orderId + '';
            operOrderAction.qrychildFlowInfo(param,function (res) {
                if(res.flag == 1){
                    $("#childFlowGrid").grid("reloadData", res.data);
                }else {
                    fish.toast('error', res.message);
                }
            });
        },
        queryDispatchOrderInfoByDispatchId : function (dispatchOrderId) {
            var param = {};
            param.dispatchId = dispatchOrderId;
            orderDetailsAction.queryDispatchOrderInfoByDispatchId(param,function (res) {
                if(res != null){
                    $('#dispatchOrderNum').attr('value',res[0].DISPATCH_ORDER_NO);
                    $('#dispatchOrderNum').attr('disabled',true);
                    $('#dispatchOrderName').attr('value',res[0].DISPATCH_TITLE);
                    $('#dispatchOrderText').val(res[0].DISPATCH_TEXT);
                }
            });
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
        isOvertime: function(){
            //add by wang.gang2 勾选数据是否超时需要展示超时原因
            var flag = false;
            var params = new Object();
            var checkRows = $("#circuitGrid").grid("getCheckRows");
            for (var i = 0; i < checkRows.length; i++) {
                var srvOrdId = checkRows[i].SRV_ORD_ID;
                var orderId = checkRows[i].ORDER_ID;
                var tacheId = checkRows[i].TACHE_ID;
                var overTime=checkRows[i].OVERTIME
                //1.查询是否主辅调
                params.srvOrdId=srvOrdId;
                params.orderId=orderId;
                var resData = operOrderAction.queryIfMainOrg(params).responseJSON.data;
                if(resData != null){
                    // 2.本地测试环节 辅调局展示resData.
                    if(0===overTime && tacheId =="500001160" && resData.IFMAINORG==1){
                        flag = true;
                    }
                    //3.跨域全程调测 主调展示
                    if(0===overTime && (tacheId =="500001168" || tacheId == '500001161') && resData.IFMAINORG==0){//500001161
                        flag = true;
                    }
                }
                if(flag){
                    if ($.inArray(srvOrdId, srvOrdIdList) == -1) {
                        srvOrdIdList.push(srvOrdId);
                    }
                }
            }
            if(flag){
                $("#overTimeRemarkDiv").show();
                $("#opinionGoRoll").addClass("requireds");
            }else {
                $("#overTimeRemarkDiv").hide();
                $("#opinionGoRoll").removeClass("requireds");

            }

        },
        equipRecycleSubmit:function (param) {
            /**
             * 必填校验
             * @type {jQuery}
             */
            if(param.isRecycle == "" || param.isRecycle == null){
                fish.toast("warn","设备是否回收不允许为空");
                return false;
            }
            else if(param.isRecycle == "0" || param.isRecycle == "1"){
                if( param.recycleCount == "" || param.recycleCount == null){
                    fish.toast("warn","设备回收数量不允许为空");
                    return false;
                }
                else if (param.equipSequence == "" || param.equipSequence == null){
                    fish.toast("warn","设备序列号不允许为空");
                    return false;
                }
            }

            /**
             * 设备回收信息提交保存
             */
            operOrderAction.equipSubmit(param,function (data) {
                if(!data.success){
                    fish.error({title:'错误',message:'设备回收情况保存失败！'+data.message});
                }
            });

        }

    });
});