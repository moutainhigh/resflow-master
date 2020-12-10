define(['text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/operOrderView.html',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/operOrderView.css'
], function(operOrderView,operOrderAction,orderStandbyAction,i18n,css) {
    var dispatchNum = '-1';
    var SpecialFlag ;
    var srvOrdIds;
    var userInfo;
    return fish.View.extend({
        template: fish.compile(operOrderView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
            //'click #closeBtn': 'closeBtn',
            'click #configBtn': 'submit',
            'click #saveBtn': 'confirmSavePropertyConfig',
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
            userData = {};//人员
            psIdFlow = {};
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
            SpecialFlag='-1';
            URL=this.getRootPath();
            var me = this;
            userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data;
            var tacheId = me.options.tacheId;
            var orderId = me.options.orderId + '';
            var woId=me.options.woId +'';
            var psId = me.options.psId;
            var srvOrdId = me.options.srvOrdId + '';
            var srvBelong = new Object();
            srvBelong.srvOrdId = srvOrdId;
            sysResource=operOrderAction.qrySrvOrderBelongSys(srvBelong).responseJSON.data;
            var psIds = '1000248,1000249';
            if (psIds.indexOf(psId) != -1){
                psIdFlow = operOrderAction.getMainFlowPsId(orderId).responseJSON.data;
            }
            srvOrdIds = me.options.srvOrdId;
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
            //初始化电路信息
            this.circuitInfo();

            //是否选择专业
            $('#selectSpecial').bind('click',function(){
                var selectSpecialFlag = $("input[name='resRadio']:checked").val();
                if (selectSpecialFlag == '0') {
                    $("#specialDiv").show();
                    $("#specialSaveBtnDiv").show();
                    $("#feedBackADiv").hide();
                    $("#feedBackZDiv").hide();
                    $("#checkSaveBtnDiv").hide();
                    me.circuitInfo();
                }else if(selectSpecialFlag == '1') {
                    $("#specialDiv").hide();
                    $("#specialSaveBtnDiv").hide();
                    $("#feedBackADiv").show();
                    $("#feedBackZDiv").show();
                    $("#checkSaveBtnDiv").show();
                    me.circuitInfo();
                }
                SpecialFlag = selectSpecialFlag;
            });
            //是否新建资源
            $('#newResource').bind('click',function(){
                var newResourceFlag = $("input[name='newResRadio']:checked").val();
                if (newResourceFlag == '0') {
                    $("#childSendDiv").hide();
                    $("#masterSelectDiv").hide();
                    $('#saveBtnDiv').hide();
                }else if(newResourceFlag == '1') {
                    $("#childSendDiv").show();
                    $("#masterSelectDiv").show();
                    $('#saveBtnDiv').show();
                }
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
                    } else if ((psId == "1000212" && tacheId == "500001160") || (psId == "1000209" && tacheId == "500001160")){
                        //本地电路 本地测试 || 跨域电路  本地测试
                        if ($("input[name='testRadio']:checked").val() == '0') { //测试通过
                            $("#remarkGoRoll").removeClass("requireds");
                        }
                        else if ($("input[name='testRadio']:checked").val() == '1') {
                            $("#remarkGoRoll").addClass("requireds");
                        }
                    }
                }
            });
            $('#oneDry').bind('click',function(){
                var oneDryValue = $("input[name='oneDry']:checked").val();
                if (tacheId == "500001153"){ //电路调度
                    if (psId == '1000209' || psId == '1000210'){ //跨域，用来判断是一干来单
                        if (oneDryValue == '1'){ //否
                            if (dispatchNum == '-1'){
                                me.getSequenceNum();
                            } else{
                                $('#dispatchOrderNum').attr('value',dispatchNum);
                                $('#dispatchOrderNum').attr('disabled',true);
                            }
                        } else if (oneDryValue == '0'){ //是
                            me.queryDispatchInfoBySrvOrdId(srvOrdId);
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
                        if(key=='complexCfgPopedit'){
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
                        }
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
                            flag : "transferStaff",
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
                    if($(this).is(":checked") == true){
                        $(this).parent().parent().next().find("input").attr("disabled",false);
                    }else{
                        $(this).parent().parent().next().find("input").attr("disabled",true);
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
                        height: 350,
                        width: 400,
                        modal: false,
                        draggable: false,
                        autoResizable: true,
                        viewOption: {
                            type : _this[0].id,
                            nodeValues :  jobData
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
                    var options = {
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/selectJobView',
                        height: 350,
                        width: 400,
                        modal: false,
                        draggable: false,
                        autoResizable: true,
                        viewOption: {
                            type : _this[0].id,
                            nodeValues :  userData
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
                                userData = nodeArray;
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
            //var userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data;

            if(btnFlag == "submit"){
                $("#operOrderViewTitle").text("工单提交");
                if(psId == '1000211' && tacheId != "500001144"){ // 核查流程
                    $("#feedBackADiv").show();
                    $("#feedBackZDiv").show();
                    $("#checkSaveBtnDiv").show();
                    if(tacheId!='500001145' && tacheId!='500001146' && tacheId!='500001147'){
                        // $("#accessRoomADiv").hide();
                        // $("#accessRoomZDiv").hide();
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
                }else if (tacheId == "500001150"){//核查汇总
                    $("#checkSaveBtnDiv").hide();
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
                    if (psId == '1000209' || psId == '1000210'){ //跨域，用来判断是一干来单
                        $("#oneDryDev").show();
                        me.queryDispatchInfoBySrvOrdId(srvOrdId);
                    }else{
                        if (woState == '290000110') { //补单
                            $("#masterSelectDiv").show();
                            me.queryDispatchInfoBySrvOrdId(srvOrdId);
                        }else{
                            me.getSequenceNum();
                            // var orderText = '由主调局负责对传输电路进行端到端BER测试，相关分公司配合。\n' +
                            //     '传输质量符合要求后，由相关分公司按电路调令要求时限进行反馈，并通知本公司业务受理部门。测试过程中如有问题，请及时反馈网管中心。'
                            // $('#dispatchOrderText').val(orderText);
                        }
                    }
                    /*$("#remarkDiv").hide();
                     $("#fileUpdateDiv").hide();*/
                }else if (tacheId == "500001160"){//本地测试
                    $("#testDiv").show();
                    if ($("input[name='testRadio']:checked").val() == '1') { //不通过 说明必填
                        $("#remarkGoRoll").addClass("requireds");
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
                    if (userInfo.areaId == '350002000000000042766427'){
                        $("#dispObjOtherDiv").show();
                        if(psIdFlow.PSID == "1000213"){ //本地电路的停复机  没有资源施工
                            $("#childFlowresConstructDiv").attr("style","display:none;");
                        }
                        //me.initSelectJob(orderId);
                    }
                }else if (tacheId == "500001155") {//光纤资源分配
                    if (userInfo.areaId == '350002000000000042766427') {
                        $("#dispObjDiv").show();
                        //me.initSelectJob(orderId);
                    }
                }else {
                    //TODO:其他操作显示模块
                }
            }else if(btnFlag == "trans"){
                $("#operOrderViewTitle").text("工单转派");
                $("#tranStaffDiv").show();
                $("#tranStaff").addClass("requireds");
            } else if(btnFlag == "resConfig"){
                $("#operOrderViewTitle").text("资源配置");
                $("#fileUpdateDiv").hide();
                $("#remarkDiv").hide();
                $("#subBtnDiv").hide();
                $("#configBtnDiv").show();
            } else if(btnFlag == "goBackOrder"){
                $("#operOrderViewTitle").text("工单回退");
                $("#remarkGoRoll").addClass("requireds");
            }else if(btnFlag == "rollBackOrder"){
                $("#operOrderViewTitle").text("工单退单");
                $("#remarkGoRoll").addClass("requireds");
                if (tacheId == "500001168"){//跨域全程调测
                    $("#crossAZAreaDiv").show();
                    me.getProvince(srvOrdId);
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
            var circuitData = $("#circuitGrid").grid("getCheckRows");

            if (circuitData.length == 0) {
                fish.warn('请至少选择一个电路信息！');
                return;
            }

            var testFlag = $("input[name='testRadio']:checked").val();
            if (testFlag == '1' && (tacheId == "500001160" || tacheId == "500001166")) { //测试不通过  本地测试 联调测试
                var childFlowData = $("#childFlowGrid").grid("getCheckRows");
                if (childFlowData.length == 0) {
                    fish.warn('请至少选择一个调单信息！');
                    return;
                }
            }
            params.circuitData = circuitData; //订单信息
            if(btnFlag == "submit"){//回单
                var operAttrs = new Object();//线条参数
                //var childFlowSpecialMap = new Object();//子流程专业参数  参数和派发区域
                var tacheOperInfo = new Object();//环节操作数据信息
                var actionFlag = "complateWo";
                // 资源配置按钮是否隐藏状态
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
                            console.log("za")

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
                    /*if(childFlowSpecialMap==false){
                     return;
                     }*/
                    /*if(psId == "1000212" || psId == "1000207" || psId == "1000209"){ //本地、局内、跨域
                     if (formValue.masterSelectRadio == 0) {
                     params.master = 'AREGION';
                     }else if (formValue.masterSelectRadio == 1){
                     params.master = 'ZREGION';
                     }else if (formValue.masterSelectRadio == 2){
                     params.master = 'AREAID';
                     }
                     }*/
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
                    if (psId == "1000212" || psId == "1000209"){//本地电路 || 跨域电路 本地测试
                        /*if ($("input[name='testRadio']:checked").val() == '0' && FILES === null){ //测试通过且附件为空
                            fish.error({title:'提示',message:'必须上传附件'})
                            return;
                        }*/
                        if ($("input[name='testRadio']:checked").val() == '1' && formValue.remark == null){ //测试不通过 说明不能为空
                            fish.error({title:'提示',message:'说明不能为空'});
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
                        if ($("input[name='testRadio']:checked").val() == '0' && FILES === null){ //测试通过且附件为空
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
                        if ($("input[name='testRadio']:checked").val() == '0' && FILES === null){ //测试通过且附件为空
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
                        if (FILES === null){
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
                    if (userInfo.areaId == '350002000000000042766427'){
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
                    debugger
                    var tacheObjs = {};
                    var dispObjJobs = new Array();
                    var isConf = true;
                    if (userInfo.areaId == '350002000000000042766427'){
                        debugger
                        $.each(circuitData,function (v,obj) {
                            if('未保存' == obj.DF_ORDER_CONFIG){
                                isConf = false;
                            }
                        });
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
                }else {
                    //TODO:其他操作添加参数
                }
                // 电路调度创建调单
                if (tacheId == "500001153"){
                   // debugger;
                    var dispatchOrderData = {};
                    dispatchOrderData.dispatchOrderNum = formValue.dispatchOrderNum;
                    dispatchOrderData.dispatchOrderName = formValue.dispatchOrderName;
                    dispatchOrderData.dispatchOrderText = formValue.dispatchOrderText;
                    params.dispatchOrderData = dispatchOrderData;
                    params.remark = formValue.remark;
                }
                if(buttonState == "dispConfirm" && tacheId == "500001144"){ //核查调度补单
                    params.checkAddOrder = true;
                }else if(buttonState == "dispConfirm" && tacheId == "500001157"){ //资源分配补单
                    params.resAllocatAddOrder = true;
                }
                tacheOperInfo.remark = formValue.remark;
                params.operAttrsVal = operAttrs;
                //params.childFlowSpecialVal = childFlowSpecialMap;
                params.tacheOperInfo = tacheOperInfo;
                params.actionFlag = actionFlag;
                params.action = "submit";
                $("#orderOper-form").blockUI({message: '派单中...'}).data('blockui-content', true);
                if (FILES === null) {
                    me.submitOrder(params);
                }else {
                    me.fileUpdate(params); //上传附件并回单
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
                $("#orderOper-form").blockUI({message: '转派中...'}).data('blockui-content', true);
                if (FILES === null) {
                    me.submitOrder(params);
                }else {
                    me.fileUpdate(params);
                }
            }else if(btnFlag == "rollBackOrder"){//退单
                params.remark = formValue.remark;
                params.provincea = formValue.PROVINCEA == null ? "": formValue.PROVINCEA ;
                params.provincez = formValue.PROVINCEZ == null ? "" : formValue.PROVINCEZ ;
                params.flag = "LOCAL";
                params.action = "rollBackOrder";
                if(params.remark == null){
                    fish.error({title:'提示',message:'说明不能为空'});
                    return;
                }
                $("#orderOper-form").blockUI({message: '退单中...'}).data('blockui-content', true);
                if (FILES === null) {
                    me.submitOrder(params);
                }else {
                    me.fileUpdate(params);
                    //me.fileUpdate(orderId,woId,srvOrdId,action,params);
                }
            }else if(btnFlag == "goBackOrder"){//回退
                params.remark = formValue.remark;
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
                $("#orderOper-form").blockUI({message: '回退中...'}).data('blockui-content', true);
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
            operOrderAction.submitOrder(params,function (res) {
                if(res.success){
                    if(params.action == "resConfig"){
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
                        $("#orderOper-form").unblockUI().data('blockui-content', false);
                        fish.toast('success', res.message);
                        me.popup.close();
                    }
                }else {
                    if(params.action != "resConfig"){
                        $("#orderOper-form").unblockUI().data('blockui-content', false);
                    }
                    fish.toast('error', res.message);
                }
            });
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
                    if (tacheId == "500001168"){//跨域全程调测
                        var srvOrdId = data.SRV_ORD_ID;
                        me.getProvince(srvOrdId);
                    }else if (tacheId == "500001153"){ //判断是否是电路调度环节
                        me.qryCircuitAreaInfo(data);//初始化主调局复选按钮
                        if (psId != '1000209' && psId != '1000210'){//判断是否是跨域，不是一干来单可以刷新调单信息
                            var checkData = $("#circuitGrid").grid("getCheckRows");
                            //刷新调单数据
                            me.refreshDispatchTitle(checkData);
                        }
                    }else if(tacheId != "500001155" && tacheId != "500001157"){
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
                    if (tacheId == "500001153"){ //判断是否是电路调度环节
                        if (psId != '1000209' && psId != '1000210'){//判断是否是跨域，不是一干来单可以刷新调单信息
                            var checkData = $("#circuitGrid").grid("getCheckRows");
                            //刷新调单标题条数数据
                            me.refreshDispatchTitle(checkData);
                        }
                    }
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
                    }

                },
                gridComplete: function () {
                    if (userInfo.areaId == '350002000000000042766427') { //如果是海南用户
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
            $("#circuitGrid").grid("setGridHeight", 150);
            // 冻结表格复选框一列
            $("#circuitGrid").grid('setFrozenColumns', 1);
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
            param.woState = me.options.woState +'';
            param.tacheId = me.options.tacheId +'';
            param.dealUserId = userInfo.userId +'';
            param.dispObjTyeValue = me.options.dispObjTyeValue +'';
            param.dispObjTye = me.options.dispObjTye +'';
            param.btnFlag = me.options.btnFlag +'';
            //     var  = me.options.btnFlag;//
            if (psIds.indexOf(psId) != -1){
                param.specialtyCode = specialtyCode;
                param.regionId = regionId;
                operOrderAction.qrySrvOrdChildList(param,function (res) {
                    if(res.flag == 1){
                        $("#circuitGrid").grid("reloadData", res.data);
                        me.qryCircuitAreaInfo(res.data[0]);
                        me.initResourceConstructFn(res.data[0]);
                    }else {
                        fish.toast('error', res.message);
                    }
                });
            }else if (me.qryCheckFlag()){
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
                        // var orderId = me.options.orderId;
                        // if (res.data[0].STATE == '已配置'){
                        //     me.getSequenceNum();
                        // }
                        // else if (res.data[0].STATE == '未配置'){
                        //     me.getSequenceNum(); //只初始化标题
                        //     var orderText = '由主调局负责对传输电路进行端到端BER测试，相关分公司配合。\n' +
                        //         '传输质量符合要求后，由相关分公司按电路调令要求时限进行反馈，并通知本公司业务受理部门。测试过程中如有问题，请及时反馈网管中心。'
                        //     $('#dispatchOrderText').val(orderText);
                        // }

                    }else {
                        fish.toast('error', res.message);
                    }
                });
            }
            //(specialtyCode != null && specialtyCode != null)
        },
        // 保存核查反馈信息
        saveCheckInfo: function(){
            var me = this;
            var data = $("#circuitGrid").grid("getCheckRows");
            var formValue = $('#orderOper-form').form('value');
            // debugger;

            if(data.length == 1){
                var param = {};
                param.circuitData = data;
                param.formValue = formValue;
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
                fish.toast('error', "请勾选一条电路信息!");
            }

        },
        validCheckInfo : function(param){
            var data = param.circuitData;
            var formValue = param.formValue;
            var me = this;
            var tacheId = me.options.tacheId;
            // 核查汇总500001150、投资估算500001151
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
                var ResSatisfyZ = $('#Z_RES_SATISFY').val();//满足-不满足
                if(isValidSign && Z_CONSTRUCT_SCHEME==null && ResSatisfyZ == 1){
                    fish.warn('Z端接入建设方案、资源情况不能为空！');
                    return false;
                }
                if(ResSatisfyZ==""){
                    fish.warn('Z端资源是否满足不能为空！');
                    return false;
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
                    }
                    if('500001157' == tacheId){
                        $("#dataMake").popedit('setValue', {name:'', value:''});
                        $("#resConstruct").popedit('setValue', {name:'', value:''});
                        $("#dataMakeUser").popedit('setValue', {name:'', value:''});
                    }
                    jobData = {};
                    userData = {};
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

                if (feedBackSign == "A") {
                    if (me.options.btnFlag != "resConfig") {
                        $("#feedBackADiv").show();
                    }
                    $("#feedBackZDiv").hide();
                } else if (feedBackSign == "Z") {
                    $("#feedBackADiv").hide();
                    if (me.options.btnFlag != "resConfig") {
                        $("#feedBackZDiv").show();
                    }
                } else if (feedBackSign == "ALL") {
                    if (me.options.btnFlag != "resConfig") {
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
                    $("input[name$='INVESTMENT_AMOUNT']").each(function(){
                        $(this).attr("disabled",true);
                    });
                    $("input[name$='CONSTRUCT_PERIOD']").each(function(){
                        $(this).attr("disabled",true);
                    });

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
                // //将回显方法写入到主调局方法里，是为了避免回显主调局数据后，主调局数据会再次被初始化覆盖
                if (param.STATE =='已配置' || param.ACTIVE_TYPE == '102') {
                    me.queryPropertyConfig(param)
                }else {
                    $("#otherMaster").attr("disabled",true);
                    $("#otherMaster").popedit('setValue',{name:'请选择区域！',value:''});
                }
            });
        },
        initFileUpdate : function(param) {
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
                width:516,
                create: function () {//控件初始化完成触发的事件
                    operOrderAction.getAnnex(param,function (data) {
                        console.log(param)
                        if(data.length!=0){
                        for (i=0; i < data.length; i++) {
                            var fileObj = {};
                            var map=data[i];
                            for(var k in map) {  //通过定义一个局部变量k遍历获取到了map中所有的key值
                                var docList = map[k]; //获取到了key所对应的value的值！
                                if(k =="FILE_SIZE"){
                                    fileObj.fileSize =docList + "KB";
                                }
                                if(k =="FILE_TYPE"){
                                    fileObj.fileType = docList;
                                }
                                if(k =="FILE_NAME"){
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
                /*maxFileSize: 20000000,*/
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
        fileUpdate :function (params){
            FILES.url = URL+"/localScheduleLT/FlieUpdateController/uploadFiles.spr";
            FILES.formData = {
                params : JSON.stringify(params)
            }
            FILES.submit();
        },
        getSequenceNum :function() {
            var me = this;
            var serviceId = me.options.serviceId + '';
            var cstOrdId = me.options.cstOrdId + '';
            var param = {};
            param.cstOrdId = cstOrdId;
            // param.orderIds = orderIds;
            operOrderAction.getsequenceNum(param,function (res) {
                if(res.success){
                    me.querySpecialtyConfig(res.areaId, serviceId);
                    var date = new Date();
                    $('#dispatchOrderNum').attr('value',res.sign + '[' + date.getFullYear() + ']'+ res.message +'号');
                    $('#dispatchOrderNum').attr('disabled',true);
                    dispatchNum = $('#dispatchOrderNum').val();
                    if (res.titleMap.productType == "局内中继电路 "){
                        $('#dispatchOrderName').attr('value', '关于【'+ res.titleMap.operateType+'】【'+ res.titleMap.num +'条】【' + res.titleMap.productType +'】的通知');
                    }
                    else{
                        $('#dispatchOrderName').attr('value','关于【'+res.titleMap.custName +'】【'+ res.titleMap.operateType+'】【'+ res.titleMap.num +'条】【' + res.titleMap.productType +'】的通知');
                    }
                    var a =  res.titleMap.textInfo;
                    var orderText ='';
                    for (i=0; i < a.length; i++) {
                        orderText = orderText + (i+1)+'、电路编号：'+ a[i].CIRCUITCODE +'\n业务号码：'+a[i].SERIAL_NUMBER +'\n业务订单号：'+a[i].TRADE_ID +'\nA端所属区域：'+a[i].AREGION+
                            '\nZ端所属区域：'+ a[i].ZREGION + '\n';
                    }
                    orderText =  orderText+ '由主调局负责对传输电路进行端到端BER测试，相关分公司配合。\n' +
                        '传输质量符合要求后，由相关分公司按电路调令要求时限进行反馈，并通知本公司业务受理部门。测试过程中如有问题，请及时反馈网管中心。'
                    $('#dispatchOrderText').val(orderText);
                }else {
                    fish.toast('error', res.message);
                }
            });
        },
        //补单查询原调单信息
        queryDispatchInfoBySrvOrdId : function(srvOrdId){
            var me = this;
            var serviceId = me.options.serviceId + '';
            operOrderAction.queryDispatchInfoBySrvOrdId(srvOrdId,function(data){
                if (data.success) {
                    me.querySpecialtyConfig(data.areaId,serviceId);
                    if (data.dispatch.length > 0){
                        $('#dispatchOrderNum').attr('value',data.dispatch[0].DISPATCH_ORDER_NO);
                        $('#dispatchOrderNum').attr('disabled',true);
                        $('#dispatchOrderName').attr('value',data.dispatch[0].DISPATCH_TITLE);
                        $('#dispatchOrderText').val(data.dispatch[0].DISPATCH_TEXT);
                    }
                }else {
                    fish.toast('error', data.message);
                }
            });
        },
        //查询专业配置
        querySpecialtyConfig : function(areaId,serviceId){
            operOrderAction.querySpecialtyConfig(areaId,serviceId,function(data){
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
                        me.savePropertyConfig();
                    });
                }else {
                    me.savePropertyConfig();
                }
            }else{
                fish.toast('error', "请至少勾选一条电路信息，并配置专业!");
            }

        },
        //电路调度环节保存电路的专业配置信息
        savePropertyConfig : function(){
            var me = this;
            var data = $("#circuitGrid").grid("getCheckRows");
            //var selectData = $("#circuitGrid").grid("getSelection");
            // var tacheId = me.options.tacheId;
            var psId = me.options.psId;
            //主调局信息
            var formValue = $('#orderOper-form').form('value');
            if (data.length > 0){
                var param = {};
                param.dataInfo = data;
                if (formValue.masterSelectRadio == "other"){
                    if (formValue.otherMaster == "请选择区域！" || formValue.otherMaster == "" || formValue.otherMaster == undefined){
                        fish.warn('请选择主调局区域！');
                        return false;
                    }else {
                        param.masterValue = formValue.otherMaster;
                        formValue.masterValue = formValue.otherMaster;
                    }
                }else {
                    param.masterValue = formValue.masterSelectRadio;
                }
                var childFlowSpecialMap = me.getAreaParams(formValue);
                if(childFlowSpecialMap!=false){
                    var specialtyConfig = {};
                    var specialtyConfigName = {};
                    specialtyConfig = childFlowSpecialMap.childFlowSpecialArea;
                    specialtyConfigName = childFlowSpecialMap.childFlowSpecialAreaName;
                    param.specialtyConfig = specialtyConfig;
                    param.specialtyConfigName = specialtyConfigName;
                    param.flowSpecialData = childFlowSpecialMap;
                    operOrderAction.saveSpecialtyConfigInfo(param,function(res){
                        if (res.success){
                            //专业配置保存完成后，重新刷新电路信息表格数据
                            me.circuitInfo();
                            if (psId != '1000209' && psId != '1000210'){//判断是否是跨域，不是一干来单可以刷新调单信息
                                // 刷新调单数据
                                // var orderId = selectData.ORDER_ID + "";
                                me.refreshDispatchInfo();
                            }
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
        //回显电路对应的专业配置信息
        queryPropertyConfig:function(data){
            var param = {};
            param.srvOrdId = data.SRV_ORD_ID;
            param.cstOrdId = data.CST_ORD_ID;
            param.activeType = data.ACTIVE_TYPE;
            var tacheId = this.options.tacheId;
            operOrderAction.queryPropertyConfig(param,function(res){
                if (res.success){
                    if (res.message != '102') {
                        if(tacheId == '500001153'){ //电路调度环节
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
                                    $("input[name=synCfgCfgPopedit]").removeAttr("disabled");
                                    $("#synCfgCfgPopedit").popedit('setValue', {name:res.configInfoName.SYN_9, value:res.configInfo.SYN_9});
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
                    var userNodeArray = new Array;
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
                            userNodeArray = val.USEROBJ;
                        }else if (val.TACHE_ID == '500001159') { //资源施工
                            $("#resConstruct").popedit('setValue', {name:val.JOBNAME, value:obj});
                        }else if (val.TACHE_ID == '500001156') { //外线施工
                            $("#outside").popedit('setValue', {name:val.JOBNAME, value:obj});
                        }
                    })
                    jobData = nodeArray;
                    userData = userNodeArray;
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
                        orderText = orderText + (i+1)+'、电路编号：'+ a[i].CIRCUITCODE +'\n业务号码：'+a[i].SERIAL_NUMBER +'\n业务订单号：'+a[i].TRADE_ID +'\nA端所属区域：'+a[i].AREGION+
                            '\nZ端所属区域：'+ a[i].ZREGION + '\n';
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
        refreshDispatchTitle: function(data){
            var me = this;
            var cstOrdId = me.options.cstOrdId + '';
            var param = {};
            param.cstOrdId = cstOrdId;
            var orderText ='';
            for (i=0; i < data.length; i++) {
                orderText = orderText + (i+1)+'、电路编号：'+ data[i].CIRCUITCODE +'\n业务号码：'+data[i].SERIAL_NUMBER +'\n业务订单号：'+data[i].TRADE_ID +'\nA端所属区域：'+data[i].AREGIONNAME+
                    '\nZ端所属区域：'+ data[i].ZREGIONNAME + '\n';
            }
            orderText =  orderText+ '由主调局负责对传输电路进行端到端BER测试，相关分公司配合。\n' +
                '传输质量符合要求后，由相关分公司按电路调令要求时限进行反馈，并通知本公司业务受理部门。测试过程中如有问题，请及时反馈网管中心。'
            $('#dispatchOrderText').val(orderText);
            operOrderAction.getDispatchInfo(param,function(res){
                if (res.success) {
                    if (res.productType == "局内中继电路 "){
                        $('#dispatchOrderName').attr('value', '关于【' + res.operateType + '】【' + data.length + '条】【' + res.productType + '】的通知');
                    }
                    else{
                        $('#dispatchOrderName').attr('value', '关于【' + res.custName + '】【' + res.operateType + '】【' + data.length + '条】【' + res.productType + '】的通知');
                    }
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
                        '<input type="checkbox" name="' + i + '" id="' + i + '" value= "' + val + '" checked="true" >'
                        + val
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
        //光纤资源分配、资源分配环节保存岗位
        resourceSaveBtnConfig:function(){
            var me = this;
            var data = $("#circuitGrid").grid("getCheckRows");
            var formValue = $('#orderOper-form').form('value');
            // debugger
            if(data.length>0){
                var dataMake = formValue.dataMakeSelectJob;
                var resConstruct = formValue.resSelectJob;
                var dataMakeUser = formValue.dataMakeSelectUser;
                var param = {};
                if((dataMake == undefined || dataMake == '') && (dataMakeUser == undefined || dataMakeUser == '')){
                    fish.warn('至少选择一项数据制作派发的岗位和人员！');
                    return;
                }
                if("1000213" != psIdFlow.PSID ) { //本地电路的停复机  没有资源施工
                    if(resConstruct == undefined || resConstruct == ''){
                        fish.warn('请选择资源施工派发的岗位！');
                        return;
                    }
                    param.resConstruct = resConstruct;
                }
                param.dataInfo = data;
                param.dataMake = dataMake;
                param.dataMakeUser = dataMakeUser;
                param.tacheId = this.options.tacheId;
                // debugger
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
        //外线施工保存岗位
        outsideSaveBtnConfig :function(){
            var me = this;
            var data = $("#circuitGrid").grid("getCheckRows");
            var formValue = $('#orderOper-form').form('value');
            // debugger
            if(data.length>0){
                var outside = formValue.outsideSelectJob;
                if(outside == undefined || outside == ''){
                    fish.warn('请选择外线施工派发的岗位！');
                    return;
                }
                var param = {};
                param.dataInfo = data;
                param.outside = outside;
                param.tacheId = this.options.tacheId;
                // debugger
                operOrderAction.saveResConstructConfigInfo(param,function(res){
                    if (res.success){
                        //外线施工对应的岗位
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
        initGridInfo:function(){
            var tacheId = this.options.tacheId;
            var psId = this.options.psId;
            // 资源配置按钮是否隐藏状态
            var checkSave = $("#configBtnDiv").is(":hidden");
            var selectSpecialFlag = $("input[name='resRadio']:checked").val();
            // debugger
            if(tacheId == '500001153' || (tacheId == '500001144'&& checkSave && selectSpecialFlag=='0')) { //电路调度、核查调度(选专业)
                return [
                    {name: 'STATE', label: '专业配置', width: 95, sortable: false,
                        formatter: function(cellval, opts, rwdat, _act) {
                            if(cellval == '已配置'){
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
                    {name: 'AREGIONNAME', label: 'A端所属区域', width: 100 , sortable: false },
                    {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 100 , sortable: false },
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
                    {name: 'AREGIONNAME', label: 'A端所属区域', width: 100 , sortable: false },
                    {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 100 , sortable: false },
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
                    {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                    {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                    {name: 'AREGIONNAME', label: 'A端所属区域', width: 100 , sortable: false },
                    {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 100 , sortable: false },
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
                    {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                    {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                    {name: 'SERIAL_NUMBER', label: '业务号码', width: 100 , sortable: false },
                    {name: 'AREGIONNAME', label: 'A端所属区域', width: 100 , sortable: false },
                    {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 100 , sortable: false },
                    {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                    {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
                ]
            }


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

    });
});