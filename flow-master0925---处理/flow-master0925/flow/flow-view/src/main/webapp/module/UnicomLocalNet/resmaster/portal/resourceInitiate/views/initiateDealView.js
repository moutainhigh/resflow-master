/**
 * 路由信息
 */
define([
    'module/UnicomLocalNet/resmaster/portal/resourceInitiate/action/resourceInitiateAction',
    'text!module/UnicomLocalNet/resmaster/portal/resourceInitiate/templates/initiateDealView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/orderDetailsView.css'
], function(resourceInitiateAction,routingInfoView,i18n,css) {

    return fish.View.extend({
        resNetworkUrl: '',
        crmRegion: '',
        template: fish.compile(routingInfoView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #close': 'close',
            'click #submit': 'submit'

        },
        initialize: function() {
            this.render();
        },
        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },
        afterRender: function() {
            //初始化grid
            this.initGrid();
            //初始化grid信息
            this.initCircuitInfo();
            //初始化专业区域信息
            this.initSpecialtyAreaInfo();
            // //初始化专业下拉框
            // this.initSpecialtyPopedit();
            // //初始化城市多选树
            // this.initCityPopedit();


        },
        //初始化grid
        initGrid: function(){
            var me = this;
            $("#initiateResourceGrid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'custName',label:'客户名称',width:170 },
                    {name: 'accNbr',label:'业务号码',width:170, align: 'left' },
                    {name: 'circuitCode',label:'电路编码',width:170, align: 'left'},
                    {name: 'aInstalledAdd',label:'A端装机地址',width:170, align: 'left'},
                    {name: 'zInstalledAdd',label:'Z端装机地址',width:170, align: 'left'},
                    {name: 'prodInstId',label:'实例ID',width:170, align: 'left', hidden:true},
                    {name: 'systemResource',label:'系统来源',width:170, align: 'left', hidden:true},
                    {name: 'serviceId',label:'产品类型',width:170, align: 'left', hidden:true},
                ],
                autowidth: true,
                multiselect: false,
                shrinkToFit: false, //表格列宽是否按比例缩放，默认true
                pageData: me.initCircuitInfo(),
            });
        },
        //查询电路信息
        initCircuitInfo:function(){
            if (this.options.flag == 'routingFlag') { //存量数据
                //封装grid所需数据
                var gridData = {};
                //存量资源返回数据
                gridData.custName = this.options.gridData.propertyInfo.custName;
                gridData.aInstalledAdd = this.options.gridData.propertyInfo.A_ADDRESS;
                gridData.zInstalledAdd = this.options.gridData.propertyInfo.Z_ADDRESS;
                //资源侧返回数据
                gridData.prodInstId = this.options.checkData.prodInstId;
                gridData.accNbr = this.options.checkData.accNbr;
                gridData.circuitCode = this.options.checkData.circuitCode;
                gridData.serviceId = this.options.checkData.serviceId;
                gridData.systemResource = 'flow-schedule-lt';
                var gridArr = [];
                gridArr.push(gridData);
                $("#initiateResourceGrid").grid("reloadData", gridArr);
            }else{ //在调度走过流程
                var param = {};
                param.srvOrdId = this.options.srvOrdId;
                param.serviceId = this.options.serviceId;
                $("#initiateResourceGrid").blockUI({
                    message: '加载中'
                }).data('blockui-content', true);
                resourceInitiateAction.initCircuitInfo(param, function(res){
                    if (res.success){
                        $("#initiateResourceGrid").grid("reloadData", res.data);
                    }else {
                        fish.info(res.msg);
                    }
                    $("#initiateResourceGrid").unblockUI().data('blockui-content', false);
                });
            }
        },
        initCityPopedit: function(){
            $('input[name$="Popedit"]').popedit({
                initialData: {
                    'name': '请选择区域...',
                    'value': ''
                },
                open: function (e) {
                    var _this = $(this);
                    var options = {
                        url: 'module/UnicomLocalNet/resmaster/portal/resourceInitiate/views/cityTreeView',
                        height: 350,
                        width: 400,
                        modal: false,
                        draggable: false,
                        autoResizable: true,
                        viewOption: {
                            cityTreeData:_this.popedit('getValue').value,
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                var orgNames = '';
                                var orgIds = '';
                                var nodeArray = new Array;
                                res.forEach(function (val, i) {
                                    var nodeSin = new Object();
                                    nodeSin.id = val.id;
                                    nodeArray.push(nodeSin);
                                    if (!val.isParent) {
                                        if (orgIds == '') {
                                            orgNames = val.name;
                                            orgIds = val.id;
                                        } else {
                                            orgNames = orgNames + ',' + val.name;
                                            orgIds = orgIds + ',' + val.id;
                                        }
                                    }
                                });
                                _this.popedit('setValue', {name: orgNames, value: orgIds});
                                $('#cityId').val(orgIds);
                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    };
                    var popup = fish.popupView(options);
                }
            });
        },
        //初始化专业区域信息
        initSpecialtyAreaInfo:function(){
            var me = this;
            var param = {};
            if(me.options.flag == 'flowFlag'){
                param.serviceId = this.options.serviceId;
            }else{
                param.serviceId = this.options.checkData.serviceId;
            }
            $("#resourceInitiateForm").blockUI({
                message: '加载中'
            }).data('blockui-content', true);
            resourceInitiateAction.initSpecialtyInfo(param, function(res){
                if (res.success){
                    var specialtyHtml = "";
                    for(var i = 0; i < res.data.length; i++){
                        specialtyHtml = specialtyHtml + '<div class="col-md-6" style="margin-bottom: 10px;">' +
                            '                    <div class="col-md-3">' +
                            '                        <label class="checkbox-inline">' +
                            '                            <input type="checkbox" name="resourceSpecialty"' +
                            '                                    value='+ res.data[i].VALUE +
                            '                                   >'+res.data[i].NAME +'</label>' +
                            '                    </div>' +
                            '                    <div class="col-md-9">' +
                            '                        <div class="input-group">' +
                            '                            <input name='+ res.data[i].VALUE + "Popedit" +
                            '                                   class="form-control" disabled>' +
                            '                        </div>' +
                            '                    </div>' +
                            '                </div>';
                    }
                    //添加其它专业
                    specialtyHtml = specialtyHtml +'<div class="col-md-6" style="margin-bottom: 10px;">' +
                        '                    <div class="col-md-3">' +
                        '                        <label class="checkbox-inline">' +
                        '                            <input type="checkbox" name="resourceSpecialty"' +
                        '                                    value="OTHER_11"' +
                        '                                   >其它</label>' +
                        '                    </div>' +
                        '                    <div class="col-md-9">' +
                        '                        <div class="input-group">' +
                        '                            <input name="OTHER_11Popedit" '+
                        '                                   class="form-control" disabled>' +
                        '                        </div>' +
                        '                    </div>' +
                        '                </div>';
                    $('#childSendDiv').append(specialtyHtml);
                    //初始化区域信息
                    me.initCityPopedit();
                    //专业初始化完成后，初始化复选框勾选事件
                    me.initCheckBoxClickEvent();
                }
                $("#resourceInitiateForm").unblockUI().data('blockui-content', false);
            });
        },
        initCheckBoxClickEvent:function(){
            //选中对应专业后，区域选择框要置为可编辑
            $("input[name='resourceSpecialty']").bind('click',function(){
                $("input[name='resourceSpecialty']").each(function(){
                    if ($(this).is(":checked") == true && (!$(this).attr("disabled"))) {
                        $(this).parent().parent().next().find("input").popedit('enable');
                    }else{
                        $(this).parent().parent().next().find("input").popedit('disable');
                    }
                })
            });
        },
        //提交
        submit:function () {
            var me = this;
            var specialParam = {};
            var specialtyInfo = '';
            var _name = '';
            var eachFlag = false;
            var specialtyArr = $('input[name="resourceSpecialty"]:checked');
            if (specialtyArr.length < 1){
                fish.info("请选择专业和区域信息！");
                return;
            }else{
                $('input[name="resourceSpecialty"]:checked').each(function(){ //jquery中each循环中返回的结果不能返回到each函数外
                    eachFlag = false;
                    if ($(this).is(":checked") == true) {
                        //先校验派发区域不能为空
                        _name = $(this).val();
                        if($('input[name='+ $(this).val() + "Popedit" + ']').popedit('getValue').value == null || $('input[name='+ $(this).val() + "Popedit" + ']').popedit('getValue').value == ''){
                            fish.info($(this).parent()[0].innerText + "专业对应的区域不能为空！");
                            eachFlag = true;
                            return false; //跳出循环
                        }
                        //处理专业派发区域
                        specialParam[_name] = $('input[name='+ $(this).val() + "Popedit" + ']').popedit('getValue').value;
                        //拼接专业
                        if(specialtyInfo == ''){
                            specialtyInfo = $(this).val();
                        }else{
                            specialtyInfo = specialtyInfo + ',' + $(this).val();
                        }
                    }
                });
            }
            if (eachFlag){
                return;
            }

            var param = {};
            //获取表格数据
            var gridData = $('#initiateResourceGrid').grid("getRowData")[0];
            if (this.options.flag == 'routingFlag') {
                gridData.isoldres = 1; //存量数据
            }else if (this.options.flag == 'flowFlag'){
                gridData.isoldres = 0; //不是存量数据
                gridData.srvordId = this.options.srvOrdId;
                param.srvordId=this.options.srvOrdId;
                param.isoldres = 0; //不是存量数据
            }
            var formData = {};
            formData.cityId = JSON.stringify(specialParam); //专业区域信息
            formData.specialtyInfo = specialtyInfo; //专业信息
            formData.remark = $('#remark').val();
            param.startOrSupp = me.options.startOrSupp;
            if("start" == me.options.startOrSupp) {
                param.woId = me.options.woId;
                param.orderId = me.options.orderId;
                param.tacheId = me.options.tacheId;
            }else if ("supp" == me.options.startOrSupp) {
                formData.regionId = this.options.checkData.regionId;
                if (this.options.flag == 'routingFlag'){
                    formData.custId = this.options.gridData.propertyInfo.CUST_ID;
                    formData.linkMan = this.options.gridData.propertyInfo.LINK_MAN;
                    formData.address = this.options.gridData.propertyInfo.ADDRESS;
                }else if (this.options.flag == 'flowFlag'){
                    formData.custId = this.options.custInfo.CUST_ID;
                    formData.linkMan = this.options.custInfo.LINK_MAN;
                    formData.address = this.options.custInfo.CUST_ADDRESS;
                }
                param.gridData = gridData;
            }
            param.formData = formData;
            $("#resourceInitiateForm").blockUI({
                message: '提交中...'
            }).data('blockui-content', true);
            resourceInitiateAction.startResourceInitiateFlow(param, function(res){
                if (res.success){
                    $("#resourceInitiateForm").unblockUI().data('blockui-content', false);
                    me.popup.close();
                    fish.info('资源补录成功');
                } else{
                    fish.info(res.msg);
                    $("#resourceInitiateForm").unblockUI().data('blockui-content', false);
                }
            });


        }
    });
});