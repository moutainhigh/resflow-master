define(['text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/postponementApplyView.html',
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
    var attach = {};
    var circuitGirdInfo;
    var srvOrdIdList=[];
    var upLoadResult = new Array();
    var fileList = null,
        maxFileSize = 50*1024*1024,                //文件大小限制
        maxFileCount = 5 ;

    return fish.View.extend({
        template: fish.compile(operOrderView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
            'click #checkSaveBtn': 'saveApplyInfo',
            'click .js-select-file': 'fileSelect',
            'click #saveBtn': 'saveBtn',
        },

        initialize: function() {
            this.render();
            URL = "";
            FILES = null;
            areaPro = {};
            userData = {'userNodeArrayMake':'','userNodeArrayCon':''}//人员 数据制作

            psIdFlow = {};
            this.isSubmit = false;
            this.delFiles = new Array();

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
            //初始化组件
            this.initModel();

            attach.woId = woId;
            attach.origin = '';
            this.initFileUpdate(attach);


        },
        initModel:function(){
            //初始化时间组件
            $('#postponementDate').datetimepicker({
                orientation: {y: 'bottom'}
            });
            $('#applyType').combobox({
                placeholder: '--请选择延期类型--',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '客户原因', value: '2015111801'},
                    {name: '工程原因', value: '2015111802'},
                    {name: '业务部门原因', value: '2015111803'},
                    {name: '系统原因', value: '2015111804'},
                    {name: '对端原因（含对端运营商）', value: '2015111805'},
                    {name: '海外POP', value: '2015111806'},
                    {name: '运维原因', value: '2015111807'},
                    {name: '其他原因', value: '2015111808'},
                ]
            });
            $('#reason').val('');
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

        },
        submit:function () {
            var me = this;
            var states = true;
            var circuitData = $("#circuitGrid").grid("getCheckRows");
            if (circuitData.length == 0) {
                fish.warn('请至少选择一个电路信息！');
                return;
            };
            for (var i = 0; i < circuitData.length; i++) {
                if (circuitData[i].APPLY_STATE != '290000000') {
                    states = false;
                }
            };
            if (!states) {
                fish.warn('存在待审核延期申请！');
                return;
            };
            var psId = me.options.psId;
            var tacheId = me.options.tacheId;

            var formValue = $('#orderOper-form').form('value');
            var btnFlag = me.options.btnFlag;
            var applyInfo= $("#applyType").combobox('getSelectedItem');
            var applyType = applyInfo.name;
            var applyId = applyInfo.value;
            var params = new Object();

            params.cstOrdId = me.options.cstOrdId;
            params.circuitData=circuitData;
            params.btnFlg='submit';
            params.origin = 'YQ';
            var fileData = $("#fileGrid").grid("getRowData");
            params.fileData = fileData;
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

            orderDetailsAction.postponementApply(params,function (res) {
                if(res.success){
                    fish.toast("success",res.message);
                    me.popup.close();
                }else{
                    fish.toast("error",'提交失败');
                }
            })
        },
        saveBtn:function () {
            var me = this;
            var states = true;
            var circuitData = $("#circuitGrid").grid("getCheckRows");
            for (var i = 0; i < circuitData.length; i++) {
                if (circuitData[i].APPLY_STATE == '290000004') {
                    states = false;
                }
            };
            if (!states) {
                fish.warn('未保存或存在待审核延期申请！');
                return;
            };
            if (circuitData.length == 0) {
                fish.warn('请至少选择一个电路信息！');
                return;
            };

            var fileData = $("#fileGrid").grid("getRowData");
            if (fileData == null || fileData == '') {
                fish.error({title: '提示', message: '请检查附件信息必填'});
                return;
            }
            var checkNull = $("#orderOper-form").isValid();
            if(!checkNull){
                fish.error({title:'提示',message:'请检查必填数据或数据格式'})
                return;
            }
            var psId = me.options.psId;
            var tacheId = me.options.tacheId;
            var buttonState = me.options.buttonState;
            var formValue = $('#orderOper-form').form('value');
            var btnFlag = me.options.btnFlag;
            var applyInfo= $("#applyType").combobox('getSelectedItem');
            var applyId = applyInfo.value;
            var params = new Object();
            params.applyId = applyId;
            params.cstOrdId = me.options.cstOrdId;
            var circuitData = $("#circuitGrid").grid("getCheckRows");
            params.circuitData=circuitData;
            params.reason=formValue.reason;
            params.btnFlg='saveBtn';
            params.postponementDate=formValue.postponementDate;
            params.fileData = fileData;
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
            orderDetailsAction.postponementApply(params,function (res) {
                if(res.success){
                    fish.toast("success",res.message);
                    for (var i = 0; i < circuitData.length; i++) {
                        circuitData[i].APPLY_STATE = '29000000'
                    };
                    $("#circuitGrid").grid("reloadData", circuitData);
                }else{
                    fish.toast("error",res.message);
                }
            })

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
                onSelectRow: function (e, rowId, state, checked) {//选中行事件
                    console.log('grid row selectRow');
                    var row = $("#circuitGrid").grid('getRowData', rowId);
                    var circuitInfo= {};
                    // 点击回显行数据
                    attach.woId = row.WO_ID;
                    attach.srvOrdId = row.SRV_ORD_ID;
                    attach.cstOrdId = me.options.cstOrdId;
                    // attach.applyState = '290000002';
                    // attach.saveState = '0';
                    attach.origin = 'YQ';
                    attach.fileState = '0';
                    orderDetailsAction.queryApplySaveInfo(attach, function(data){
                        if (data != '' && data.fileData != '' && data.fileData != null){
                            $("#postponementDate").datetimepicker('value',  data.newTime);
                            $("#applyType").combobox('value',data.applyType);
                            $("#reason").val(data.reason);
                            $("#fileGrid").grid("reloadData", data.fileData);
                            row.APPLY_STATE=data.applyState;
                            console.log(data.fileInfo);
                        }else{
                            me.initModel()
                            $('#postponementDate').val('');
                            $("#fileGrid").grid("reloadData", [])
                        }
                    });

                },
                onSelectAll: function (e, status){ //全选事件
                    var rowsData = $('#circuitGrid').grid("getCheckRows");
                    for (var i = 0; i < rowsData.length; i++) {
                        attach.woId = rowsData[i].WO_ID;
                        attach.srvOrdId = rowsData[i].SRV_ORD_ID;
                        attach.cstOrdId = me.options.cstOrdId;

                        attach.origin = 'YQ';
                        attach.fileState = '0';
                        orderDetailsAction.queryApplySaveInfo(attach, function(data){
                            if (data != '' ){
                                rowsData[i].APPLY_STATE=data.applyState;
                            }
                        });
                    }
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
                    var data = $("#circuitGrid").grid("getSelection");
                    var param = {};
                    param.attrValue = cellcontext;
                    param.srvOrdId = data.SRV_ORD_ID;
                    operOrderAction.saveCircuitCodeBySrvOrdId(param,function(res){
                        if (res.success){
                            console.log(res.msg);
                        }
                    });
                },
                gridComplete: function () {
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
            param.applyState = '290000000';
            //     var  = me.options.btnFlag;//
            if (psIds.indexOf(psId) != -1){
                param.specialtyCode = specialtyCode;
                param.regionId = regionId;
                param.sysResource=sysResource; //二干：secondary
                //az端要求完成时间，全程要求完成时间
               operOrderAction.qrySrvOrdChildList(param,function (res) {
                    if(res.flag == 1){
                        circuitGirdInfo = res.data;
                        $("#circuitGrid").grid("reloadData", res.data);

                        me.initResourceConstructFn(res.data[0]);
                    }else {
                        fish.toast('error', res.message);
                    }
                });
            }else {
                operOrderAction.qrySrvOrdList(param,function (res) {
                    if(res.flag == 1){
                        $("#circuitGrid").grid("reloadData", res.data);

                        if (res.data[0].TACHE_ID == "500001168"){
                            if ("second-schedule-lt" == sysResource.SYSTEM_RESOURCE
                                &&("secondary" == sysResource.RESOURCES
                                    || "jike" == sysResource.RESOURCES)){
                                me.qrySecondDataMakeLists(res.data[0].SRV_ORD_ID);
                                me.qrySubLocalTestDataInfo(res.data[0].SRV_ORD_ID);
                            }
                        }
                    }else {
                        fish.toast('error', res.message);
                    }
                });
            }
            $(window).trigger("resize");
        },

        initFileUpdate : function(param) {
            var me = this;
            var $fileGrid = $("#fileGrid").grid({
                colModel: [
                    {name: 'fileId', label: '附件ID', hidden: true},
                    {name: 'filePath', label: '附件路径', hidden: true},
                    {name: 'fileName', label: '文件名称', width: 160, sortable: false },
                    {name: 'fileSize', label: '大小', width: 50},
                    {name: 'fileType', label: '类型', width: 40 , sortable: false },
                    {name: 'action', label: '操作', width: 100, formatter: 'actions',
                        //glyphicon glyphicon-remove-circle
                        formatter: function(cellval, opts, rwdat, _act) {
                            return ' <span class="file-remove glyphicon glyphicon-remove-circle"></span>'
                        },
                    }
                ],
                width:516,
                create: function () {//控件初始化完成触发的事件
                    operOrderAction.getAttachInfo(param,function (data) {
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
                                    if (k == "FILE_PATH") {
                                        fileObj.filePath = docList.split(".")[0];
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
                url: URL + '/localScheduleLT/initProdFileUploadController/uploadFiles.spr',
                dataType: 'json',
                autoUpload: true,
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
                     // $("#fileGrid").grid("addRowData", fileObj);
                        data.submit();
                    $('.file-remove').off('click').on('click', function (e) {
                        var name = $(e.currentTarget).prev().children(".fileName").text(), pos, pos2;
                        $.each(data.files, function (index, file) {
                            if (file.name === name) {
                                pos = index;
                                return false;
                            }
                        })
                    })
                },
                always: function (e, data) {
                    me.isSubmit = false;
                    var llll = data.result;
                    fileList=data.result;
                    if (data.result != null) {
                        $.unblockUI();
                      //  fish.toast('success', '上传成功');
                        $("#fileGrid").grid("addRowData", data.result)
                        // me.popup.close();
                    }else {
                        $.unblockUI();
                        fish.toast('error', '失败');
                    }
                },
            });

            me.$("#fileGrid").on('click', '.file-remove', function() {
                fish.warn('you click delete button')
                var fileInfo = {};
                var selrow = $fileGrid.grid("getSelection");
                $fileGrid.grid("delRowData", selrow);//删除记录
                fileInfo.fileId = selrow.fileId;
                fileInfo.filePath = selrow.filePath;
                fileInfo.fileName = selrow.fileName;
                orderDetailsAction.delFileOnFtp(fileInfo)
            })
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
        initGridInfo:function(){
            var tacheId = this.options.tacheId;
            var psId = this.options.psId;
            // 资源配置按钮是否隐藏状态
            var checkSave = $("#configBtnDiv").is(":hidden");
            var selectSpecialFlag = $("input[name='resRadio']:checked").val();
            // debugger
            return [
                {name: 'CIRCUITCODE', label: '电路编号', width: 130, sortable: false,},
                {name: 'RFSDATE', label: '全程要求完成时间', width: 130, sortable: false, },
                {name: 'TRADE_ID', label: '业务订单号', width: 100 },
                {name: 'ORDER_ID', label: '流程订单号', width: 100 , hidden: true },
                {name: 'APPLY_STATE', label: '流程订单号', width: 100 , hidden: true },
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