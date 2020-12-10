define([
    'text!module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/templates/cloudNetWorkResCheckFlowView.html',
    'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/action/cloudNetWorkResCheckFlowAction',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/operOrderView.css'
], function (cloudNetWorkResCheckFlowView, cloudNetWorkResCheckFlowAction, operOrderAction, css) {
    return fish.View.extend({
        template: fish.compile(cloudNetWorkResCheckFlowView),
        events: {
            'click #submitBtn': 'submit',
            'click #saveBtn' : 'save'
        },

        initialize: function () {
            this.render();
            FILES = null; //附件
            userInfo = cloudNetWorkResCheckFlowAction.queryStaffInfo().responseJSON.data; //用户信息
            isSubmit = false;
            this.delFiles = new Array();
        },

        //渲染页面
        render: function () {
            this.$el.html(this.template({
                TITLE: this.options.title
            }));
        },

        //初始化fish组件
        afterRender: function () {
            //初始化电路信息
            this.initCircuitInfo();
            //初始化专业区域信息
            this.initSpecialArea();
            //IPRAN专业选中事件
            $("input[id='ipRan']").bind('click',function(){
                if($(this).is(":checked") == true){
                    $("input[id='ipRanPopedit']").attr("disabled",false);
                }else{
                    $("input[id='ipRanPopedit']").attr("disabled",true);
                }
            });
            //初始化附件表格
            var woId = this.options.woId;
            this.initFileUpdate(woId);
        },

        initCircuitInfo: function () {
            var me = this;
            $("#cloudNetWorkResCheckGrid").grid({
                colModel: me.initColModel(),
                width: 516,
                shrinkToFit: false,
                multiselect: true,
                pageData: me.qrycircuitInfo(),
                onSelectRow: function (e, rowid, state, checked) {//选中行事件
                    me.initSpecialAreaValue();
                }
            });
            $("#cloudNetWorkResCheckGrid").grid("setGridHeight", 150);
        },
        initColModel: function(){
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
                    }
                },
                {name: 'CIRCUITCODE', label: '电路编号', width: 100, sortable: false },
                {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                {name: 'SERIAL_NUMBER', label: '业务号码', width: 100 , sortable: false },
                {name: 'AREGIONNAME', label: 'A端所属区域', width: 110, sortable: false},
                {name: 'AREGIONID', label: 'A端所属区域ID', width: 110, sortable: false, hidden: true },
                {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110, sortable: false},
                {name: 'ZREGIONID', label: 'Z端所属区域ID', width: 110, sortable: false, hidden: true },
                {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
            ]
        },
        /**
         * 查询电路信息
         */
        qrycircuitInfo: function () {
            var me = this;
            var param = {};
            param.cstOrdId = me.options.cstOrdId;
            param.tacheId = me.options.tacheId;
            param.userId = userInfo.userId;
            param.woState = me.options.woState;
            cloudNetWorkResCheckFlowAction.qrycircuitInfo(param, function(res){
                if (res.success){
                    $("#cloudNetWorkResCheckGrid").grid("reloadData", res.data);
                }else {
                    fish.toast('error', res.message);
                }
            });
            $(window).trigger("resize");
        },
        /**
         * 给专业区域赋值
         */
        initSpecialAreaValue: function(){
            var data = $("#cloudNetWorkResCheckGrid").grid("getSelection");
            if (data != null || data != undefined || data != {}){
                var param = {};
                param.cstOrdId = data.CST_ORD_ID;
                param.srvOrdId = data.SRV_ORD_ID;
                //先查询该条电路是否已经配置过专业区域信息，如果配置过就回显，没有配置过就进行初始化
                cloudNetWorkResCheckFlowAction.querySpecialAreaInfo(param, function(res){
                    if (res.success){
                        if (res.data != null && res.data != ''){
                            //是否直接到核查汇总回显
                            var radioValue = res.data.NEW_CREATE_RESOURCE;
                            $("input[name=resRadio]").each(function(i,obj){
                                if (obj.value = radioValue){
                                    $("input[name=resRadio][value= "+radioValue+"]").attr("checked",true);
                                }
                            });
                            //IPRAN专业区域信息回显
                            if (res.data.SPECIALTY_INFO != null && res.data.SPECIALTY_INFO != ''){
                                $("input[name=ipRan]").attr("checked", true);
                                var ipRanInfo = eval("("+res.data.SPECIALTY_INFO+")");
                                $("#ipRanPopedit").popedit('setValue', {name: ipRanInfo.areaName, value: ipRanInfo.areaId});
                            }
                            //光纤专业区域信息回显
                            if (res.data.SPECIALTY_INFO_NAME != null && res.data.SPECIALTY_INFO_NAME != ''){
                                $("input[name=fiber]").attr("checked", true);
                                var fiberInfo = eval("("+res.data.SPECIALTY_INFO_NAME+")");
                                $("#fiberPopeditA").popedit('setValue', {name: fiberInfo.areaNameA, value: fiberInfo.areaIdA});
                                $("#fiberPopeditZ").popedit('setValue', {name: fiberInfo.areaNameZ, value: fiberInfo.areaIdZ});
                            }
                            //终端盒区域信息回显
                            if (res.data.FLOW_SPECIALTY_DATA && res.data.FLOW_SPECIALTY_DATA != ''){
                                $("input[name=terminalBox]").attr("checked", true);
                                var terminalInfo = eval("("+res.data.FLOW_SPECIALTY_DATA+")");
                                $("#terminalBoxPopeditA").popedit('setValue', {name: terminalInfo.areaNameA, value: terminalInfo.areaIdA});
                                $("#terminalBoxPopeditZ").popedit('setValue', {name: terminalInfo.areaNameZ, value: terminalInfo.areaIdZ});
                            }
                        }else{
                            //是否直接到核查汇总设置为否
                            $("input[name=resRadio]").each(function(i,obj){
                                if (obj.value = "1"){
                                    $("input[name=resRadio][value= '1']").prop("checked",true);
                                }
                            });
                            //专业设置都不勾选
                            $("input[name=ipRan]").prop("checked", false);
                            $("input[name=fiber]").prop("checked", false);
                            $("input[name=terminalBox]").prop("checked", false);
                            //给光纤、终端盒专业区域赋值
                            $("#fiberPopeditA, #terminalBoxPopeditA").popedit('setValue', {name:data.AREGIONNAME, value:data.AREGIONID});
                            $("#fiberPopeditZ, #terminalBoxPopeditZ").popedit('setValue', {name:data.ZREGIONNAME, value:data.ZREGIONID});
                        }
                    }else{
                        console.log(res.msg);
                    }
                });
            }
        },
        /**
         * 初始化专业区域信息
         */
        initSpecialArea: function(){
            var me = this;
            $("input[name$='Popedit']").popedit({
                initialData: {
                    'name': '--请选择专业区域--',
                    'value': ''
                },
                open:function(e) {
                    var _this = $(this);
                    var _this = $(this);
                    var key=_this.get(0).id;
                    if (key != 'ipRanPopedit'){
                        return;
                    }
                    var options = {
                        url: 'module/UnicomLocalNet/resmaster/portal/cloudNetworkFlow/views/cloudAreaTreeView',
                        height: 350,
                        width: 400,
                        modal: false,
                        draggable: false,
                        autoResizable: true,
                        viewOption: {
                            deptId : userInfo.orgId,
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                var orgNames = '';
                                var orgIds = '';
                                res.forEach(function(val,i){
                                    if(orgIds==''){
                                        orgNames = val.name ;
                                        orgIds = val.id;
                                    } else {
                                        orgNames = orgNames + ',' + val.name ;
                                        orgIds = orgIds + ',' + val.id;
                                    }
                                });
                                _this.popedit('setValue', {name:orgNames, value:orgIds});
                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    };
                    var popup = fish.popupView(options);
                }
            });
        },
        initFileUpdate: function (woId) {
            $("#fileGrid").grid({
                colModel: [
                    {name: 'attachInfoId', label: '附件ID', hidden: true},
                    {name: 'fileName', label: '文件名称', width: 160, sortable: false},
                    {name: 'fileSize', label: '大小', width: 40},
                    {name: 'fileType', label: '类型', width: 40, sortable: false},
                    {
                        name: 'action', label: '操作', width: 100, formatter: 'actions',
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
                width: 516,
                create: function () {//控件初始化完成触发的事件
                    operOrderAction.getAnnex(woId, function (data) {
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
                add: function (e, data) {
                    var fileObj = {};
                    var obj = data.files[0];
                    fileObj.fileName = obj.name.split(".")[0];
                    fileObj.fileSize = (obj.size / 1024.0).toFixed(2) + "KB";
                    fileObj.fileType = obj.name.split(".")[1];
                    if ((obj.size / 1024.0).toFixed(2) >= 20 * 1024) {
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
                        var data = $("#fileGrid").grid("getRowData", rowid)
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
                    debugger;
                    isSubmit = false;
                    if (data.result.success) {
                        me.submitOrder(data.submitInfo); //提交工单
                        fish.toast('success', data.result.message);
                        me.popup.close();
                    } else {
                        $.unblockUI();
                        fish.toast('error', data.result.message);
                    }
                },
            });
        },
        /**
         * 提交
         */
        submit: function () {
            var me = this;
            var circuitData = $("#cloudNetWorkResCheckGrid").grid("getCheckRows");//订单信息
            if (circuitData != null && circuitData.length < 1){
                fish.info("请先选择要提交的电路！");
                return;
            }
            //校验勾选电路中是否有未配置的电路
            if (circuitData != null && circuitData.length > 0){
                for (var i = 0; i < circuitData.length; i++){
                    if (circuitData[i].STATE == '未配置'){
                        fish.info("选择电路中，有未进行专业区域配置的，请重新选择！");
                        return;
                    }
                }
            }
            var params = {};
            params.circuitData = circuitData
            params.remark = $('#remark').val();
            params.delFiles = me.dealFileData();
            params.circuitData = circuitData;
            params.tacheId = me.options.tacheId;
            params.cstOrdId = me.options.cstOrdId;
            params.delFiles = me.dealFileData();

            $.blockUI({message: '派单中...'});
            if (FILES != null) { //如果附件非空先先上传附件
                me.fileUpdate(params);
            } else {
                me.submitOrder(params);
            }
        },

        //处理附件
        dealFileData: function () {
            var me = this;
            var delFiles = '';
            //处理附件
            var fileData = $("#fileGrid").grid("getRowData");
            var delFilesStr = "";
            for (var j = 0; j < me.delFiles.length; j++) {
                var temp = false;
                for (var i = 0; i < fileData.length; i++) {
                    if (me.delFiles[j] == fileData[i].attachInfoId) {
                        temp = true;
                        break;
                    }
                }
                if (!temp) {
                    delFilesStr += me.delFiles[j] + ",";
                }
            }
            if (delFilesStr) {
                delFiles = delFilesStr.substring(0, delFilesStr.length - 1);
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
            return delFiles;
        },

        //附件上传
        fileUpdate: function (params) {
            FILES.url = this.getRootPath() + "/localScheduleLT/FlieWoOrderDealController/uploadFiles.spr";
            FILES.formData = {
                params: JSON.stringify(params)
            }
            FILES.submitInfo = params;
            if (!isSubmit) {
                isSubmit = true;
                FILES.submit();
            } else {
                fish.toast('error', "请勿重复提交！");
            }
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

        //提交工单
        submitOrder: function (params) {
            var me = this;
            if (!isSubmit) {
                isSubmit = true;
                cloudNetWorkResCheckFlowAction.submitWoOrder(params, function(res){
                    $.unblockUI();
                    if (res.success) {
                        fish.toast('success', res.message);
                        me.popup.close();
                    } else {
                        fish.toast('error', res.message);
                    }
                });
            }
        },
        /**
         * 专业区域信息保存
         */
        save: function(){
            var me = this;
            var data = $("#cloudNetWorkResCheckGrid").grid("getSelection");
            if (data == null || data == undefined || data == {}){
                fish.info("请先选中一条数据进行专业区域信息配置！");
                return;
            }else{
                var param = {};
                param.cstOrdId = data.CST_ORD_ID;
                param.srvOrdId = data.SRV_ORD_ID;
                param.isDirctCheckSum = $("input[name='resRadio']:checked").val();
                var terminalBox = $("input[name='terminalBox']:checked").val();
                if (terminalBox != null && terminalBox != undefined){
                  var boxParam = {};
                    boxParam.areaNameA = $('#terminalBoxPopeditA').val();
                    boxParam.areaIdA = $('#terminalBoxPopeditA').popedit('getValue').value;
                    boxParam.areaNameZ = $('#terminalBoxPopeditZ').val();
                    boxParam.areaIdZ = $('#terminalBoxPopeditZ').popedit('getValue').value;
                    param.terminalBox = JSON.stringify(boxParam);
                }
                var fiber = $("input[name='fiber']:checked").val();
                if (fiber != null && fiber != undefined){
                    var fiberParam = {};
                    fiberParam.areaNameA = $('#fiberPopeditA').val();
                    fiberParam.areaIdA = $('#fiberPopeditA').popedit('getValue').value;
                    fiberParam.areaNameZ = $('#fiberPopeditZ').val();
                    fiberParam.areaIdZ = $('#fiberPopeditZ').popedit('getValue').value;
                    param.fiber = JSON.stringify(fiberParam);
                }
                var ipRan = $("input[name='ipRan']:checked").val();
                if (ipRan != null && ipRan != undefined){
                    var ipRanParam = {};
                    ipRanParam.areaName = $('#ipRanPopedit').val();
                    ipRanParam.areaId = $('#ipRanPopedit').popedit('getValue').value;
                    param.ipRan = JSON.stringify(ipRanParam);
                }
                $.blockUI({message: '保存中...'});
                cloudNetWorkResCheckFlowAction.saveSpecialArea(param, function(res){
                    $.unblockUI();
                    if (res.success){
                        fish.info("专业区域信息保存成功！");
                        //刷新grid数据
                        me.qrycircuitInfo();
                    }else{
                        fish.info(res.msg);
                    }
                })
            }
        }

    });
});