define(["text!module/UnicomLocalNet/resmaster/portal/networkAudit/templates/networkAuditView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/networkAudit/i18n/networkAuditView.i18n',
        'module/UnicomLocalNet/resmaster/portal/networkAudit/action/networkAuditAction.js',
        "css!module/UnicomLocalNet/resmaster/portal/networkAudit/styles/networkAudit.css"],
    function(template,portalViewi18n,networkAuditActionAs,css) {
        var userId;
        var meNet;
        var localNetAudiNum = 10;
        var localNetAudiPage = 1;
        var URl;
        return ngc.View.extend({
                template : ngc.compile(template),
                i18nData: ngc.extend({}, portalViewi18n),
                events: {
                    "click #queryNetWorkAudit" : 'queryNetWorkAuditDatasFun',
                    "click #resetLocalApply" : 'resetNetWorkAuditFun',
                    "click #exportNetWorkAudit" : 'exportNetWorkAudit',

                },
            initialize: function() {
                this.render();
            },
            //渲染页面
            render: function() {
                this.$el.html(this.template(this.i18nData));
                return this;
            },
            afterRender: function() {
                meNet = this;
                meNet.userInfo = networkAuditActionAs.queryStaffInfo().responseJSON.data;
                userId=this.userInfo.userId;
                // debugger
                $('#serialNumber').clearinput();
                $('#circuitCode').clearinput();
                $('#custChineseName').clearinput();
                $('#dispatchOrderNo').clearinput();
                URl=this.getRootPath();
                //初始化时间控件
                $('#sumCompletionStartDate,#sumCompletionEndDate,#rentConfirmationStartDate,#rentConfirmationEndDate').datetimepicker({
                    orientation:{y:'bottom'}
                });
                this.initNetWorkAuditGrid();
                // this.queryNetWorkAuditDatasFun();
                this.initAuditCombobox();

                $('.rowtext .ui-pagination').change(function () {
                        var p1=$(this).children('option:selected').val();//这就是selected的值
                        localNetAudiNum = p1;
                        localNetAudiPage = 1;
                        this.queryNetWorkAuditDatasFun();
                    }
                );
            },
            initAuditCombobox:function(){
                this.initProductTypeData();
                this.initOperationData();
            },
            //产品类型
            initProductTypeData: function(){
                var productTypeObj = new Object();
                productTypeObj.codeType = 'product_code';
                //产品类型
                networkAuditActionAs.queryProdTypeData(productTypeObj,function(data) {
                    $('#serviceCode').combobox({
                        placeholder: '--请选择产品类型--',
                        dataTextField: 'name',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });
            },
            initOperationData: function(){
                var operationTypeObj = new Object();
                operationTypeObj.codeType = 'operate_type';
                //动作类型1
                networkAuditActionAs.queryProdTypeData(operationTypeObj,function(data){
                    $('#activeCode').combobox({
                        dataSource: data,
                        placeholder: '--请选择动作类型--',
                        dataTextField: 'name',
                        dataValueField: 'value'
                    });
                });
            },

            closeFile: function() {
                meNet.trigger('close');
                meNet.popup.close();
            },
            //初始化业务稽核信息
            initNetWorkAuditGrid:function(){
                var queryNetWorkAuditDataList = $.proxy(this.queryNetWorkAuditDatas,this); //函数作用域改变
                $("#netWorkAuditGrid").grid({
                    colModel: [
                        //默认展示字段
                        {name:'cstOrdId',label:'客户ID',width:200,hidden: true },
                        {name:'srvOrdId',label:'业务定单ID',width:200,hidden: true },
                        {name: 'custNameChinese',label:'客户名称',width:200, align: 'left' },
                        {name:'subScribeId',label:'客户订单号',width:200,align: 'left' },
                        {name:'tradeId',label:'业务订单号',width:200,align: 'left' },
                        {name:'serialNumber',label:'业务号码',width:200,align: 'left' },
                        {name: 'circuitCode',label:'电路编号',width:150, align: 'left' },
                        {name: 'serviceName',label:'产品类型',width:150, align: 'left' },
                        {name: 'activeTypeName',label:'动作类型',width:150, align: 'left' },
                        {name: 'speed',label:'带宽速率',width:150, align: 'left',},
                        {name: 'sumCompletionDate',label:'报竣时间',width:150, align: 'left',},
                        {name: 'rentConfirmationDate',label:'起租时间/止租时间',width:150, align: 'left',},
                        {name: 'routeInfo',label:'详细路由',width:400, align: 'left',},
                        {name: 'dispatchOrderNo',label:'电路历史调单编号',width:400, align: 'left'},
                        {name: 'custManager',label:'客户经理',width:150, align: 'left'},
                        {name: 'custManaPhone',label:'客户经理联系电话',width:150, align: 'left'},
                        {name: 'abelongprovince',label:'A端归属省分',width:150,align: 'left' },
                        {name: 'abelongcity',label:'A端归属地市',width:150,align: 'left' },
                        {name: 'abelongregion',label:'A端归属分公司',width:150,align: 'left' },
                        {name: 'ainstalledadd',label:'A端装机地址',width:150,align: 'left' },
                        {name: 'acontactman',label:'A端联系人',width:150,align: 'left' },
                        {name: 'acontacttel',label:'A端联系人电话',width:150,align: 'left' },
                        {name: 'zbelongprovince',label:'Z端归属省分',width:150,align: 'left' },
                        {name: 'zbelongcity',label:'Z端归属地市',width:150,align: 'left' },
                        {name: 'zbelongregion',label:'Z端归属分公司',width:150,align: 'left' },
                        {name: 'zinstalledadd',label:'Z端装机地址',width:150,align: 'left' },
                        {name: 'zcontactman',label:'Z端联系人',width:150,align: 'left' },
                        {name: 'zcontacttel',label:'Z端联系人电话',width:150,align: 'left' },
                        {name: 'cirremark',label:'电路备注',width:400,align: 'left' }

                    ],
                    datatype: "json",
                    autowidth: true,
                    curPageSort: true,
                    recordtext:"{0}-{1} 共{2}条",
                    pgtext: " 第{0}页/共{1}页",
                    rowtext: "每页{0}条",
                    rowNum: 10,
                    rowList: [10,20,50,100,200,500],
                    pager: true,
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    multiselect: false,
                    shrinkToFit: false,
                    autoResizable: true,
                    showColumnsFeature: false, //允许用户自定义列展示设置
                    cached: true, //把用户自定义的列展示设置缓存在本地
                    pageData: queryNetWorkAuditDataList,
                    gridComplete: function () {
                        $('.gotext').html("<span>跳转至<input class=\"ui-pagination-input\"></span>");
                    }

                });

            },
            queryNetWorkAuditDatasFun : function(){
                    this.queryNetWorkAuditDatas();
            },
            //查询填写阶段性意见子信息
            queryNetWorkAuditDatas: function(page, rowNum, sortname, sortorder){
                var serialNumber = $("#serialNumber").val();//业务号码
                var circuitCode = $("#circuitCode").val();//电路编号
                var custChineseName = $("#custChineseName").val();//客户名称
                var serviceCode = $("#serviceCode").val();//产品类型
                var activeCode = $("#activeCode").val();//动作类型
                var dispatchOrderNo = $("#dispatchOrderNo").val();//调单号
                var sumCompletionStartDate = $("#sumCompletionStartDate").val();//报竣开始时间
                var sumCompletionEndDate = $("#sumCompletionEndDate").val();//报竣结束时间
                var rentConfirmationStartDate = $("#rentConfirmationStartDate").val();//起租开始时间
                var rentConfirmationEndDate = $("#rentConfirmationEndDate").val();//起租结束时间

                if(sumCompletionStartDate==undefined
                    ||sumCompletionStartDate==""
                    ||sumCompletionEndDate==undefined
                    ||sumCompletionEndDate==""){
                    sumCompletionStartDate = "";
                    sumCompletionEndDate = "";
                }
                if(rentConfirmationStartDate==undefined
                    ||rentConfirmationStartDate==""
                    ||rentConfirmationEndDate==undefined
                    ||rentConfirmationEndDate==""){
                    rentConfirmationStartDate = "";
                    rentConfirmationEndDate = "";
                }
                debugger;
                rowNum = (rowNum!=''&&rowNum!=undefined)?rowNum:localNetAudiNum;
                page = (page!=''&&page!=undefined)?page:localNetAudiPage;

                var collQueryObj = new Object();
                collQueryObj.userId = userId;
                collQueryObj.SERIAL_NUMBER = serialNumber;
                collQueryObj.circuitCode = circuitCode;
                collQueryObj.CUST_NAME_CHINESE = custChineseName;
                collQueryObj.serviceCode = serviceCode;
                collQueryObj.activeCode = activeCode;
                collQueryObj.DISPATCH_ORDER_NO = dispatchOrderNo;
                collQueryObj.SUM_COMPLETION_START_DATE = sumCompletionStartDate;
                collQueryObj.SUM_COMPLETION_END_DATE = sumCompletionEndDate;
                collQueryObj.RENT_CONFIRMATION_START_DATE = rentConfirmationStartDate;
                collQueryObj.RENT_CONFIRMATION_END_DATE = rentConfirmationEndDate;
                collQueryObj.pageIndex = page+'';
                collQueryObj.pageSize = rowNum+'';
                $("#netWorkAuditGrid").blockUI({message: '加载中'}).data('blockui-content', true);
                // debugger;
                networkAuditActionAs.queryNetworkAuditData(collQueryObj,function (data) {
                    debugger;
                    if (data.flag == "success") {
                        var gridData = {
                            "rows": data.data,
                            "page": data.pageIndex,
                            "records": data.dataCount,
                            "rowNum": data.rowNum,
                            "total": data.total
                        };
                        $("#netWorkAuditGrid").grid("reloadData", gridData);
                        $("#netWorkAuditGrid").unblockUI({message: '加载中'}).data('blockui-content', false);
                    }else{
                        $("#netWorkAuditGrid").unblockUI({message: '加载中'}).data('blockui-content', false);
                        fish.toast("error", "获取数据失败"+data.message);
                    }
                    meNet.resize();
                })
            },
            cleanup: function() {
                appendLog('PopView cleanup...');
            },
            resize: function () {
                // $("#orderDeal-grid").grid("resize",true);
                $("#netWorkAuditGrid").grid("setGridHeight", 327);
            },

            //重置
            resetNetWorkAuditFun:function(){
                $("#serialNumber").val("");//业务号码
                $("#circuitCode").val("");//电路编号
                $("#custChineseName").val("");//客户名称
                $("#dispatchOrderNo").val("");//调单号
                $("#serviceCode").combobox('value','');//产品类型
                $("#activeCode").combobox('value','');//动作类型
                $("#sumCompletionStartDate").val("");//报竣开始时间
                $("#sumCompletionEndDate").val("");//报竣结束时间
                $("#rentConfirmationStartDate").val("");//起租开始时间
                $("#rentConfirmationEndDate").val("");//起租结束时间

            },
            exportNetWorkAudit:function(){
                var me = this;
                me.exportNetWorkAuditFun();
            },

            //网络稽核导出
            exportNetWorkAuditFun:function(){
                var serialNumber = $("#serialNumber").val();//业务号码
                var circuitCode = $("#circuitCode").val();//电路编号
                var custChineseName = $("#custChineseName").val();//客户名称
                var serviceCode = $("#serviceCode").val();//产品类型
                var activeCode = $("#activeCode").val();//动作类型
                var dispatchOrderNo = $("#dispatchOrderNo").val();//调单号
                var sumCompletionStartDate = $("#sumCompletionStartDate").val();//报竣开始时间
                var sumCompletionEndDate = $("#sumCompletionEndDate").val();//报竣结束时间
                var rentConfirmationStartDate = $("#rentConfirmationStartDate").val();//起租开始时间
                var rentConfirmationEndDate = $("#rentConfirmationEndDate").val();//起租结束时间

                var collQueryObj = new Object();
                collQueryObj.userId = userId;
                collQueryObj.SERIAL_NUMBER = serialNumber;
                collQueryObj.circuitCode = circuitCode;
                collQueryObj.CUST_NAME_CHINESE = custChineseName;
                collQueryObj.serviceCode = serviceCode;
                collQueryObj.activeCode = activeCode;
                collQueryObj.DISPATCH_ORDER_NO = dispatchOrderNo;
                collQueryObj.SUM_COMPLETION_START_DATE = sumCompletionStartDate;
                collQueryObj.SUM_COMPLETION_END_DATE = sumCompletionEndDate;
                collQueryObj.RENT_CONFIRMATION_START_DATE = rentConfirmationStartDate;
                collQueryObj.RENT_CONFIRMATION_END_DATE = rentConfirmationEndDate;

                networkAuditActionAs.exportNetWorkAuditData(URl+'/localScheduleLT/networkAuditController/exportData.spr',collQueryObj);

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


        }); //fish.View.extend END
    }); //ALL END