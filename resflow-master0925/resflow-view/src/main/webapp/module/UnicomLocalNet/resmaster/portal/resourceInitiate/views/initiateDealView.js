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
            'click #submit': 'submit',

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
            //初始化专业下拉框
            this.initSpecialtyPopedit();
            //初始化城市多选树
            this.initCityPopedit();

            //是否派发本地网选择事件
            $('#isSendLocalNet').bind('click',function(){
                var sendFlag = $("input[name='isSendLocalNet']:checked").val();
                if (sendFlag == 1){ //不派发本地网
                    $('#cityDev').hide();
                } else if (sendFlag == 0){ //下发本地网
                    $('#cityDev').show();
                }
            });


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
                gridData.systemResource = 'second-schedule-lt';
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
            $('#cityPopedit').popedit({
                initialData: {
                    'name': '请选择城市...',
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
                            cityTreeData:$('#cityId').val()
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
        initSpecialtyPopedit:function(){
            var param = {};
            param.type = 'SPECIALTY_TYPE';
            param.proIdRef = '23';
            resourceInitiateAction.initSpecialtyInfo(param, function(res){
                if (res.success){
                    $('#resourceSpecialty').multiselect('option',{
                        dataTextField:'NAME',
                        dataValueField:'ID',
                        dataSource:res.data
                    });
                } else {
                    fish.info(res.msg);
                }
            });
        },
        //提交
        submit:function () {
            var me = this;
            var sendFlag = $("input[name='isSendLocalNet']:checked").val();
            if (sendFlag == 0){ //派发本地网
                var cityValue = $('#cityId').val();
                if (cityValue == '' || cityValue == undefined || cityValue == null ){
                    fish.info('城市不能为空，请选择！');
                    return;
                }
                var specialtyValue = $('#resourceSpecialty').val();
                if (specialtyValue == '' || specialtyValue == undefined || specialtyValue == null) {
                    fish.info('专业不能为空，请选择！');
                    return;
                }
            } else if (sendFlag == 1 ) { //不派发本地网
                var specialtyValue = $('#resourceSpecialty').val();
                if (specialtyValue == '' || specialtyValue == undefined || specialtyValue == null) {
                    fish.info('专业不能为空，请选择！');
                    return;
                }
            }
            //获取表格数据
            var gridData = $('#initiateResourceGrid').grid("getRowData")[0];
            if (this.options.flag == 'routingFlag') {
                gridData.isoldres = 1; //存量数据
            }else if (this.options.flag == 'flowFlag'){
                gridData.isoldres = 0; //不是存量数据
            }
            var formData = {};
            formData.isSendLocalNet = sendFlag;
            formData.cityId = $('#cityId').val();
            formData.specialtyInfo = $('#resourceSpecialty').val();
            formData.remark = $('#remark').val();
            formData.regionId =this.options.checkData.regionId;
            if (this.options.flag == 'routingFlag'){
                formData.custId = this.options.gridData.propertyInfo.CUST_ID;
                formData.linkMan = this.options.gridData.propertyInfo.LINK_MAN;
                formData.address = this.options.gridData.propertyInfo.ADDRESS;
            }else if (this.options.flag == 'flowFlag'){
                formData.custId = this.options.custInfo.CUST_ID;
                formData.linkMan = this.options.custInfo.LINK_MAN;
                formData.address = this.options.custInfo.CUST_ADDRESS;
            }

            var param = {};
            param.gridData = gridData;
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