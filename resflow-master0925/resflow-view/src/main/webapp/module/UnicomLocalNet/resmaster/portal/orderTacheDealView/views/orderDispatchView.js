define(['module/UnicomLocalNet/resmaster/portal/orderTacheDealView/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/orderTacheDealView/templates/orderDispatchView.html',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderTacheDealView/styles/operOrderView.css'
], function(operOrderAction,orderDispatchView,orderStandbyAction,i18n,css) {
    var resultflagA = false;
    var resultflagZ = true;
    var sysResource;

    return fish.View.extend({
        template: fish.compile(orderDispatchView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
        },

        initialize: function() {
            this.render();
            URL = "";
            FILES = null; //附件
            userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data; //用户信息
            this.isSubmit = false;
            this.delFiles = new Array();
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
            var srvOrdId = me.options.srvOrdId + '';
            var srvBelong = new Object();
            srvBelong.srvOrdId = srvOrdId;
            sysResource=operOrderAction.qrySrvOrderBelongSys(srvBelong).responseJSON.data;
            this.initFish();
            //初始化电路信息
            this.circuitInfo();
            //初始化附件表格
            this.initFileUpdate(me.options.woId +'');
            //this.initResources();
        },
        /*initResources:function(){
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
        },*/
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
            //查询数据源信息,如果不是一干调度下发，需要提示电路编号可以手动修改
            var param = {};
            param.srvOrdId = me.options.srvOrdId;
            var res = operOrderAction.querySystemInfo(param).responseJSON.data;
            if (res.success){
                if (res.data.SYSTEM_RESOURCE == 'second-schedule-lt' && res.data.RESOURCES != 'onedry'){
                    $("#modalTitle").text("工单提交(电路编号可手动编辑)");
                }
            };
            var res = operOrderAction.qryCircuitNum(param).responseJSON.data;
            if(res.success){
                console.log("。。。。电路编号为空。。。");
                circuitNum = res.circuitNum;
                ifConfig = res.ifConfig;
                if (circuitNum == "" || circuitNum == null){
                    if (ifConfig == "是") {
                        $("#modalTitle").text("工单提交");
                    }
                }else {
                    $("#modalTitle").text("工单提交");
                }
            }
        },
        submit:function () {
            var me = this;
            var psId = me.options.psId;
            var tacheId = me.options.tacheId;
            var serviceId = me.options.serviceId;
            var buttonState = me.options.buttonState;
            var formValue = $('#orderOper-form').form('value');
            var btnFlag = me.options.btnFlag;
            var params = new Object();

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
            var circuitData = $("#circuitGrid").grid("getCheckRows");//订单信息
            if (circuitData.length == 0) {
                fish.warn('请至少选择一个电路信息！');
                return;
            }
            var flag = -1;
            var message = "";
            $.each(circuitData,function(i,val){
                //先判断有没有追单
                var orderId = val.ORDER_ID;
                var cstOrdId = val.CST_ORD_ID;
                var ifFlag = operOrderAction.queryIfTrack(orderId + '', cstOrdId + '').responseJSON.data;
                if (ifFlag){
                    flag = i;
                    message = '存在追单未处理的电路，不能提交，请重新勾选!';
                    return false;
                }
                //再判断有没有电路编号
                var srvMap = {};
                srvMap.srvOrdId = circuitData[i].SRV_ORD_ID;
                var circuitNum = null;
                var ifConfig = null;
                var res = operOrderAction.qryCircuitNum(srvMap).responseJSON.data;
                if(res.success){
                    console.log("。。。。电路编号为空。。。");
                    circuitNum = res.circuitNum;
                    ifConfig = res.ifConfig;
                }else {
                    console.log("。。。。" + res.message + "。。。");
                    if (res.flag == 0) {
                        message = "勾选电路数据中，存在未起草调单的电路！请先起草调单。。。";
                    }else {
                        message = res.message;
                    }
                    flag = res.flag;
                    return false;
                }
                if (val.CIRCUITCODE == null || val.CIRCUITCODE == ""){
                    if (ifConfig == "否"){
                        flag = i;
                        message = "勾选电路数据中，存在没有电路编号的电路！请添加电路编号或者重新选择。。。";
                        return false;
                    }else if (ifConfig == null || ifConfig == ""){
                        flag = i;
                        message = "勾选电路数据中，存在未起草调单的电路！请添加电路编号或者重新选择。。。";
                        return false;
                    }
                }
            });
            if (flag > -1) {
                fish.warn(message);
               //todo:提示用户输入电路编号
                return;
            }
            var operAttrs = new Object();//线条参数
            var tacheOperInfo = new Object();//环节操作数据信息
            var actionFlag = "complateWo";
            //流程线条参数
            if (tacheId == "510101080"){ //二干调度环节 --[二干电路]客户，局内电路流程
                operAttrs.SCHEDULE_REVIEW = 0;

                /*//AZ端资源是否具备
                if (resultflagA) {
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
            }

            tacheOperInfo.remark = formValue.remark;
            params.circuitData = circuitData;
            params.operAttrsVal = operAttrs;
            params.tacheOperInfo = tacheOperInfo;
            params.actionFlag = actionFlag;
            params.action = btnFlag;
            params.serviceId = serviceId;
            params.region_id = userInfo.areaId;
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
            if (!me.isSubmit) {
                me.isSubmit = true;
                operOrderAction.submitOrder(params, function (res) {
                    me.isSubmit = false;
                    if (res.success) {
                        $("#orderOper-form").unblockUI().data('blockui-content', false);
                        fish.toast('success', res.message);
                        me.popup.close();
                    } else {
                        $("#orderOper-form").unblockUI().data('blockui-content', false);
                        fish.toast('error', res.message);
                    }
                });
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
                cellEdit: true,
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
                beforeEditCell:function (e,rowid,name,value,iRow,iCol){ //修改之前
                    console.log("判断电路编号是否为空。。。");
                    var data = $("#circuitGrid").grid("getSelection");
                    var srvMap = {};
                    srvMap.srvOrdId = data.SRV_ORD_ID;
                    var circuitNum = null;
                    var ifConfig = null;
                    var res = operOrderAction.qryCircuitNum(srvMap).responseJSON.data;
                    if(res.success){
                        console.log("。。。。电路编号为空。。。");
                        circuitNum = res.circuitNum;
                        ifConfig = res.ifConfig;
                        if (circuitNum == "" || circuitNum == null){
                            if (ifConfig == "是") {
                                console.log("。。。。起草调度不需要二干资源分配。。。");
                                return false;
                            }
                        }else {
                            console.log("。。。。电路编号有值。。。");
                            return false;
                        }
                    }else {
                        fish.toast('error', res.message);
                        return false;
                    }
                },
            });
            $("#circuitGrid").grid("setGridHeight", 150);

        },
        initGridInfo:function(){
            return [
                {name: 'CIRCUITCODE', label: '电路编号', width: 95, sortable: false ,editable:true },
                {name: 'A_IF_RES_HAVE', label: 'A端资源是否具备', width: 100, formatter: this.azIfResHaveEnum},
                {name: 'Z_IF_RES_HAVE', label: 'Z端资源是否具备', width: 100, formatter: this.azIfResHaveEnum},
                {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                {name: 'SERIAL_NUMBER', label: '业务号码', width: 100 , sortable: false },
                {name: 'ACITY', label: 'A端城市', width: 100 , sortable: false },
                {name: 'ZCITY', label: 'Z端城市', width: 100 , sortable: false },
                {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
            ]
        },
        azIfResHaveEnum: function (value) {
            var enumTypeMap = {
                '0':'否',
                '1':'是',
                '':''
            };
            return enumTypeMap[value];
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
            operOrderAction.qrySrvOrdList(param,function (res) {
                if(res.flag == 1){
                    $("#circuitGrid").grid("reloadData", res.data);
                }else {
                    fish.toast('error', res.message);
                }
            });
        },
        //初始化附件表格
        initFileUpdate : function(woId) {
            $("#fileGrid").grid({
                colModel: [
                    {name: 'attachInfoId', label: '附件ID', hidden: true},
                    {name: 'fileName', label: '文件名称', width: 160, sortable: false },
                    {name: 'fileSize', label: '大小', width: 40 },
                    {name: 'fileType', label: '类型', width: 40 , sortable: false },
                    {name: 'action', label: '操作', width: 100, formatter: 'actions',
                        formatoptions: {
                            editbutton: false,
                            //delbutton: false,
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
                    operOrderAction.getAnnex(woId,function (data) {
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
                    me.isSubmit = false;
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
            if (!this.isSubmit) {
                this.isSubmit = true;
                FILES.submit();
            } else {
                fish.toast('error', "请勿重复提交！");
            }
        },

    });
});