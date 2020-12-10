define(['text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/checkApplyView.html',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/operOrderView.css'
], function(checkApplyView,operOrderAction,orderStandbyAction,orderDetailsAction,i18n,css) {
    var dispatchNum = '-1';
    var SpecialFlag ;
    var srvOrdIds;
    var userInfo;
    var sysResource;
    var meOper;
    var URL = '';
    var provinceConf;
    var attach = {};
    var circuitGirdInfo;
    var upLoadResult = new Array();
    var fileList = null,
        maxFileSize = 50*1024*1024,                //文件大小限制
        maxFileCount = 5 ;

    return fish.View.extend({
        template: fish.compile(checkApplyView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
            'click .js-select-file': 'fileSelect'

        },

        initialize: function() {
            this.render();
            URL = this.getRootPath();
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

            //500001168
            srvOrdIds = me.options.srvOrdId;
            //初始化电路信息
            this.circuitInfo();

            this.initFish();
            //初始化附件表格
            var attach = {};
            attach.woId = woId;
            attach.origin = 'SH';
            this.initFileUpdate(attach);


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
            var circuitData = $("#circuitGrid").grid("getCheckRows");
            if (circuitData.length == 0) {
                fish.warn('请至少选择一个电路信息！');
                return;
            };
            var checkNull = $("#orderOper-form").isValid();

            var psId = me.options.psId;
            var tacheId = me.options.tacheId;

            var formValue = $('#orderOper-form').form('value');
            var btnFlag = me.options.btnFlag;
            var params = {};
            params.cstOrdId = me.options.cstOrdId;
            params.srvOrdId = me.options.srvOrdId;
            params.circuitData=circuitData;
            params.remark=formValue.remark;
            params.btnFlg='submit';
            params.postponementDate=formValue.postponementDate;
            params.agreeOrNot = $("input[name='agreeOrNot']:checked").val();
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

            me.submitOrder(params);

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
                    var row = $("#circuitGrid").grid('getRowData', rowId);
                    var circuitInfo= {};
                    // 点击回显行数据
                    attach.woId = me.options.woId;
                    attach.srvOrdId = row.SRV_ORD_ID;
                    attach.cstOrdId = me.options.cstOrdId;
                    attach.applyState = '290000002';
                    attach.saveState = '1';
                    attach.fileState = '1';
                    orderDetailsAction.queryApplySaveInfo(attach, function(data){
                        $("#applyTypeDiv").show();
                        $("#postponementDateDiv").show();
                        $("#applyRemarkDiv").show();
                        if (data != ''){
                            $("#postponementDate").datetimepicker('value',  data.newTime );
                            $("#postponementDate").attr('disabled',true);
                            $("#applyType").combobox('value',data.applyType );
                            $("#applyType").combobox('disable');
                            $("#applyRemark").val(data.reason);
                            $("#applyRemark").attr('disabled',true);
                            console.log(data.fileInfo);
                        }
                    });
                },
                onSelectAll: function (e, status){ //全选事件
                    $("#applyTypeDiv").hide();
                    $("#postponementDateDiv").hide();
                    $("#applyRemarkDiv").hide();
                },
                //选中单元格
                onCellSelect: function (e, rowid, iCol, cellcontent) {
                    // debugger;
                    var data = $("#circuitGrid").grid("getRowData",rowid);


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
            param.applyState = '290000002';

            operOrderAction.qrySrvOrdList(param,function (res) {
                    if(res.flag == 1){
                        $("#circuitGrid").grid("reloadData", res.data);
                    }else {
                        fish.toast('error', res.message);
                    }
                })
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
                {name: 'SERIAL_NUMBER', label: '业务号码', width: 100 , sortable: false },
                {name: 'AREGIONNAME', label: 'A端所属区域', width: 110, sortable: false},
                {name: 'ZREGIONNAME', label: 'Z端所属区域', width: 110, sortable: false},
                {name: 'A_INSTALLED_ADD', label: 'A端装机地址', width: 100 , sortable: false },
                {name: 'Z_INSTALLED_ADD', label: 'Z端装机地址', width: 100 , sortable: false }
            ]
        },

        submitOrder : function(params){
            var me = this;
            params.origin = 'SH';
            params.action = 'postponementApply';
            params.woId = me.options.woId;
            params.cstOrdId = me.options.cstOrdId;

            operOrderAction.feedBackToOneDry(params, function (res) {
                $.unblockUI();
                if (res.success) {
                    fish.toast('success', res.message);
                    me.popup.close();
                }else{
                    fish.toast('error', res.message);
                }
            });
        },
        fileUpdate :function (params){
            params.origin = 'SH';
            params.action = 'postponementApply';
            params.woId = this.options.woId;
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
        }
    });
});