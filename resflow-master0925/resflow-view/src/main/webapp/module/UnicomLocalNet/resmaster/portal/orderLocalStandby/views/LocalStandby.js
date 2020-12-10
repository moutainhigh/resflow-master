define(["text!module/UnicomLocalNet/resmaster/portal/orderLocalStandby/templates/LocalStandby.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
        "css!module/UnicomLocalNet/resmaster/portal/orderLocalStandby/styles/localStandby.css"],
    function (template, portalViewi18n, orderStandbyAction, css) {

        return ngc.View.extend({
            staffId: "",
            queryObj: new Object(),
            rUrl:"",
            URl:"",
            deptStandny:false,
            jobStandby:false,
            staffStandby:false,
            dealOrder:false,
            dispConfirm:false,
            abnormalOrder:false,
            ccOrder:false,
            localStandyNum: 10,
            localStandyPage: 1,
            showActionButtons: "",
            userInfo: new Object(),
            resNetworkUrl: '',
            crmRegion: '',
            initTab:this.dialogArguments._src ? this.dialogArguments._src : "",
            standbyType:'',
            template: ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #queryOrder': 'initQueryBut',
                'click #tabs-a-a-link': 'aaClick',
                'click #tabs-a-b-link': 'abClick',
                'click #tabs-a-c-link': 'acClick',
                'click #tabs-a-d-link': 'adClick',
                'click #tabs-a-e-link': 'aeClick',
                'click #tabs-a-f-link': 'afClick',
                'click #tabs-a-g-link': 'agClick',
                'click #tabs-a-h-link': 'ahClick',   //延期申请单
                'click #touchGet': 'touchGet',
                'click #touchConfirm': 'cc_confirm',
                'click #releaseTouchGet': 'releaseTouchGet',
                'click #collapsible': 'collapsible',
                'click #exportExcel': 'exportExcel',
                'click #resetSelect': 'resetSelect',
                'click #showMore': 'showMore',
                'click #retract': 'retract',
                'click #applyCheck': 'applyCheck',
                // 'click #fileupload': 'attachFileupload',
            },
            initialize: function () {
                this.render();
                // this.workOrderState = "";

            },
            render: function () {           //渲染页面
                this.$el.html(this.template(this.i18nData));
            },

            /**
             *  消息列表跳转页面URL参数获取
             *  @author wangsen 2020年10月16日 10:25:00
             */
            getUrlParam:function(name) {
                var query = window.location.search.substring(1); // 取得url中?后面的字符
                var param_arr = query.split("&"); // 把参数按&拆分成数组
                for (var i = 0; i < param_arr.length; i++) {
                    var pair = param_arr[i].split("=");
                    if (pair[0] == name) {
                        return pair[1];
                    }
                }
                return (false);
            },

            //初始化fish组件
            afterRender: function () {
                debugger
                this.showActionButtons = "";
                this.userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data;
                crmRegionMap = orderStandbyAction.qryMsmSwitchByArea(this.userInfo.areaId).responseJSON.data;
                if(crmRegionMap !=''
                    && crmRegionMap != undefined){
                    this.crmRegion = crmRegionMap.CRM_REGION;
                }
                this.resNetworkUrl = orderStandbyAction.qryInterfaceUrl('ResourceNetWork').responseJSON.data;
                this.staffId = this.userInfo.userId;
                $('#srvOrdId').clearinput();
                $('#orderTitle').clearinput();
                $('#custName').clearinput();
                $('#dispatchOrderId').clearinput();
                $('#serialNumber').clearinput();
                $('#subscribeId').clearinput();
                $('#teacheName').clearinput();
                this.rUrl = this.getRootPath();
                this.URl = this.getRootPath();
                //初始化tab
                $("#orderDeal-tab").tabs();
                $("#tabs").tabs();
                //初始化表格
                this.initorderDealGridGroup();
                //固定tab页
                this.initClick();
                //初始化clearinput
                $('#srvOrdId，#orderTitle').clearinput();
                // $('#orderDeal-workOrderCode').clearinput();
                //隐藏高级查询条件
                /*$(".orderDeal-advSearchFields-row").hide();
                $("#orderDeal-panel-body").attr("style","padding-bottom: 0px;");*/
                //初始化时间控件
                $('#finishDate,#endDate,#localFinishDate,#localEndDate,#qcFinishDate,#qcEndDate').datetimepicker({
                    orientation: {y: 'bottom'}
                });
                this.initComboxData();
                //初始化定时查询
                //this.initAutoQry();
                // this.initTree();

                $('.rowtext .ui-pagination').change(function () {
                        var p1 = $(this).children('option:selected').val();//这就是selected的值
                        this.localStandyNum = p1;
                        this.localStandyPage = 1;
                        this.queryWorkOrdersGroup();
                    }
                );

                /**
                 *  消息列表跳转页面参数设置以及初始化
                 *  @author wangsen 2020年10月16日 10:25:00
                 */
                var srvOrdId = this.getUrlParam('srvOrdId');
                if (srvOrdId && srvOrdId != "") {
                    $('#tabs-a-d-link').click();
                    $("#srvOrdId").val(srvOrdId);
                    $("#touchGet").hide();
                    $("#touchConfirm").hide();
                    $("#collapsible").show();
                    $("#resetSelect").show();
                    $("#releaseTouchGet").show();
                    /**
                     * 处理中
                     */
                    this.showActionButtons = 'dealOrder';
                    this.queryObj.queryTypeLocal = 'dealOrder';
                    this.queryObj.srvOrdId = srvOrdId;
                    this.queryObj.compUserId = '';
                    this.queryObj.dealUserId = this.staffId;
                    this.queryObj.staffId = '';
                    this.queryObj.dispType = '260000003';
                    this.queryObj.woState = '290000002'; //处理中
                    this.queryObj.standbyType = standbyType;
                    this.initorderDealGridGroup();
                    this.queryWorkOrdersGroup();
                }
            },
            initTree : function(){
                $("input[name$='_DEP']").popedit({

                    open:function(e) {
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/operTreeOrderView',
                            height: 500,
                            width: 400,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag : "org"
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    _this.popedit('setValue', {name:res.name, value:res.id});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }

                });
            },
            initClick : function () {
                if (this.initTab != "" && this.initTab != null) {
                    standbyType = [
                        'deptStandny',
                        'jobStandby',
                        'staffStandby',
                        'dealOrder',
                        'dispConfirm',
                        //'exceptionOrder',
                        'abnormalOrder',
                        'ccOrder'];
                    $('#' + this.initTab).click();
                }else {
                    standbyType = [];
                    //默认选中待接单区
                    $('#tabs-a-b-link').click();
                }
            },
            //延期申请批量审核
            applyCheck: function(){
                var selarrrow = $("#orderDeal-grid").grid("getCheckRows");
                var fileId = new Date().getTime();
                var updateParams = new Object();
                updateParams.selarrrow = selarrrow;
                var meTempCo = this;
                if (selarrrow.length < 1) {
                    fish.warn("请选择一条数据");
                    return;
                }
                var popFile = fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/applyCheckView',
                    width: "65%",
                    height: "90%",
                    title: "审核意见",
                    viewOption: {
                        selArrrow: selarrrow,
                        URl: this.URl,
                        staffIdColl: this.userInfo.userId
                    },
                    callback: function (popup, view) {
                        // debugger
                        popup.result.then(function (e) {
                            meTempCo.queryWorkOrdersGroup();
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                });

            },
            initComboxData: function () {
                $('#resourceType').combobox({
                    placeholder: '--请选择单据来源--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '政企中台', value: 'jike'},
                        {name: '一干调度', value: 'onedry'},
                        {name: '二干调度', value: 'secondary'},
                        {name: '本地调度', value: 'localBuild'},
                        {name: '云网协同', value: 'cloudNetwork'},
                    ]
                });
                //是否退单
                $('#ifWoOrderBack').combobox({
                    placeholder: '--请选择是否退单--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '是', value: '1'},
                        {name: '否', value: '0'}
                    ]
                });
                //单据类型下拉框
                var obj = {};
                obj.codeType = "ITEM_TYPE";
                orderStandbyAction.queryItemType(obj, function (data) {
                    $('#itemType').combobox({
                        placeholder: '--请选择单据类型--',
                        dataTextField: 'name',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });
                // 环节名称 teacheName
                obj.codeType = "TEACH_NAME";
                orderStandbyAction.queryItemType(obj, function (data) {
                    $('#teacheName').multiselect({
                        containSearch:true,
                        placeholder: '--请选择环节名称--',
                        dataTextField: 'name',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });
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
            initorderDealGridGroup: function () {
                this.deptStandny = true;
                this.jobStandby = true;
                this.staffStandby = true;
                this.dealOrder = true;
                this.dispConfirm = true;
                this.abnormalOrder = true;
                this.ccOrder = true;
                this.queryObj.standbyType = [
                    'deptStandny',
                    'jobStandby',
                    'staffStandby',
                    'dealOrder',
                    'dispConfirm',
                    //'exceptionOrder',
                    'abnormalOrder',
                    'ccOrder'
                ];
                var me = this;
                if (this.queryObj.queryTypeLocal == 'abnormalOrder') { //异常单
                    this.girdMdel = [
                        //默认展示字段
                        {name: 'WO_COMPLETE_STATE', label: '单据状态', width: 80, hidden: true},
                        {name: 'WOORDERBACKFLAGS', label: '是否退单', width: 80 , formatter : me.ifBackOrder },
                        {name: 'CST_ORD_ID', label: '客户Id', width: 80, sortable: false, hidden: true},
                        {name: 'WO_IDS', label: '工单号集', width: 80, sortable: false, hidden: true},
                        {name: 'SRV_ORD_IDS', label: '业务定单信息Id', width: 80, sortable: false, hidden: true},
                        {name: 'ORDER_CODE', label: '流程定单编码', width: 80, sortable: false, hidden: true},
                        {name: 'COMP_USER_ID', label: '处理人', width: 80, sortable: false, hidden: true},
                        {name: 'SRV_ORD_ID', label: '业务定单信息ID', width: 240, sortable: false, hidden: true},
                        {name: 'PS_ID', label: 'PS_ID', width: 240, sortable: false, hidden: true},
                        {name: 'ORDER_ID', label: '定单ID', width: 240, sortable: false, hidden: true},
                        {name: 'WO_STATE', label: '工单状态', width: 240, sortable: false, hidden: true},
                        {name: 'WO_ID', label: '工单ID', width: 240, sortable: false, hidden: true, align: 'center'},
                        {name: 'TACHE_ID', label: '环节ID', width: 240, sortable: false, hidden: true},
                        {name: 'REGION_ID', label: '区域ID', width: 240, sortable: false, hidden: true},
                        {name: 'SPECIALTY_CODE', label: '专业Code', width: 240, sortable: false, hidden: true},
                        {name: 'DISPOBJTYEVALUE', label: '岗位/部门值', width: 240, sortable: false, hidden: true},
                        {name: 'DISPOBJTYE', label: '派发类型', width: 240, sortable: false, hidden: true},
                        {name: 'CUST_NAME_CHINESE', label: '客户名称', width: 160},
                        {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 120, align: 'center'},
                        {name: 'APPLY_ORD_ID', label: '申请单编码', width: 120,},
                        {name: 'APPLY_ORD_NAME', label: '申请单标题', width: 200},
                        {name: 'SPC_TAC_NAME', label: '环节名称', width: 120, align: 'left'},
                        {name: 'COUNTS', label: '电路数量', width: 80, align: 'center'},
                        {name: 'SERVICE_ID', label: '产品类型Id', width: 80, sortable: false, hidden: true},
                        {name: 'SERVICETYPE', label: '产品类型', width: 80},
                        {name: 'ACTIVETYPENAME', label: '动作类型', width: 80},
                        {name: 'ITEMTYPE', label: '单据类型', width: 80, formatter: me.formatItemType},
                        {
                            name: 'RESOURCES',
                            label: '单据来源',
                            width: 80,
                            align: 'left',
                            formatter: me.formatResourcesName
                        },
                        {name: 'DISPATCH_ORDER_NO', label: '调度单编号', width: 120, align: 'center'},
                        {name: 'DISPATCH_TITLE', label: '调度单标题', width: 200},
                        {name: 'ALARM_DATE', label: '环节预警时间', width: 160, align: 'center'},
                        {name: 'REQ_FIN_DATE', label: '环节要求完成时间', width: 160, align: 'center'},
                        {name: 'DEAL_DATE', label: '工单处理时间', width: 160, align: 'center'},
                    ]
                }else if (this.queryObj.queryTypeLocal == 'dispConfirm') { //已完成
                    this.girdMdel = [
                        //默认展示字段
                        {name: 'WO_COMPLETE_STATE', label: '单据状态', width: 80, hidden: true},
                        {name: 'WOORDERBACKFLAGS', label: '是否退单', width: 80 , formatter : me.ifBackOrder },

                        {name: 'CUST_NAME_CHINESE', label: '客户名称', width: 160},
                        {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 120, align: 'center'},
                        {name: 'APPLY_ORD_ID', label: '申请单编码', width: 120,},
                        {name: 'APPLY_ORD_NAME', label: '申请单标题', width: 200},
                        {name: 'SPC_TAC_NAME', label: '环节名称', width: 120, align: 'left'},
                        {name: 'COUNTS', label: '电路数量', width: 80, align: 'center'},
                        {name: 'SERVICE_ID', label: '产品类型', width: 80, formatter: me.formatServiceId},
                        {name: 'ACTIVE_TYPE', label: '动作类型', width: 80, formatter: me.formatActiveType},
                        {name: 'ORDER_TYPE', label: '单据类型', width: 80, formatter: me.formatItemType},
                        {
                            name: 'RESOURCES',
                            label: '单据来源',
                            width: 80,
                            align: 'left',
                            formatter: me.formatResourcesName
                        },
                        {name: 'DISPATCH_ORDER_NO', label: '调度单编号', width: 120, align: 'center'},
                        {name: 'DISPATCH_TITLE', label: '调度单标题', width: 200},
                        {name: 'ALARM_DATE', label: '环节预警时间', width: 160, align: 'center'},
                        {name: 'REQ_FIN_DATE', label: '环节要求完成时间', width: 160, align: 'center'},
                        {name: 'DEAL_DATE', label: '工单处理时间', width: 160, align: 'center'},
                        /*{name: 'CST_ORD_ID', label: '客户Id', width: 80, sortable: false, hidden: true},
                        {name: 'WO_IDS', label: '工单号集', width: 80, sortable: false, hidden: true},
                        {name: 'SRV_ORD_IDS', label: '业务定单信息Id', width: 80, sortable: false, hidden: true},
                        {name: 'ORDER_CODE', label: '流程定单编码', width: 80, sortable: false, hidden: true},
                        {name: 'COMP_USER_ID', label: '处理人', width: 80, sortable: false, hidden: true},
                        {name: 'SRV_ORD_ID', label: '业务定单信息ID', width: 240, sortable: false, hidden: true},
                        {name: 'PS_ID', label: 'PS_ID', width: 240, sortable: false, hidden: true},
                        {name: 'ORDER_ID', label: '定单ID', width: 240, sortable: false, hidden: true},
                        {name: 'WO_STATE', label: '工单状态', width: 240, sortable: false, hidden: true},
                        {name: 'WO_ID', label: '工单ID', width: 240, sortable: false, hidden: true, align: 'center'},
                        {name: 'TACHE_ID', label: '环节ID', width: 240, sortable: false, hidden: true},
                        {name: 'REGION_ID', label: '区域ID', width: 240, sortable: false, hidden: true},
                        {name: 'SPECIALTY_CODE', label: '专业Code', width: 240, sortable: false, hidden: true},
                        {name: 'DISPOBJTYEVALUE', label: '岗位/部门值', width: 240, sortable: false, hidden: true},
                        {name: 'DISPOBJTYE', label: '派发类型', width: 240, sortable: false, hidden: true},*/
                    ]
                } else {
                    this.girdMdel = [
                        //默认展示字段
                        {name: 'WO_COMPLETE_STATE', label: '单据状态', width: 80, hidden: false},
                        {name: 'WOORDERBACKFLAGS', label: '是否退单', width: 80 , formatter : me.ifBackOrder },
                        {name: 'CUST_NAME_CHINESE', label: '客户名称', width: 160},
                        {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 120, align: 'center'},
                        {name: 'APPLY_ORD_ID', label: '申请单编码', width: 120,},
                        {name: 'APPLY_ORD_NAME', label: '申请单标题', width: 200},
                        //{name: 'SERVICETYPE', label: '产品类型', width: 200, hidden: true},
                        {name: 'SPC_TAC_NAME', label: '环节名称', width: 100, align: 'left'},
                        {name: 'COUNTS', label: '电路数量', width: 80, align: 'center'},
                        {name: 'SERVICE_ID', label: '产品类型', width: 80, formatter: me.formatServiceId},
                        {name: 'ACTIVE_TYPE', label: '动作类型', width: 80, formatter: me.formatActiveType},
                        {name: 'ORDER_TYPE', label: '单据类型', width: 80, formatter: me.formatItemType},
                        {
                            name: 'RESOURCES',
                            label: '单据来源',
                            width: 80,
                            align: 'left',
                            formatter: me.formatResourcesName
                        },
                        {name: 'DISPATCH_ORDER_NO', label: '调度单编号', width: 120, align: 'center'},
                        {name: 'DISPATCH_TITLE', label: '调度单标题', width: 200},
                        {name: 'ALARM_DATE', label: '环节预警时间', width: 160, align: 'center'},
                        {name: 'REQ_FIN_DATE', label: '环节要求完成时间', width: 160, align: 'center'},
                        /*{name: 'LEVEL_ID', label: '异常单等级', width: 120, hidden: true},
                        {name: 'CHG_VERSION', label: '异常单版本', width: 120, hidden: true},
                        {name: 'CST_ORD_ID', label: '客户Id', width: 80, sortable: false, hidden: true},
                        {name: 'WO_IDS', label: '工单号集', width: 80, sortable: false, hidden: true},
                        {name: 'SRV_ORD_IDS', label: '业务定单信息Id', width: 80, sortable: false, hidden: true},
                        {name: 'ORDER_CODE', label: '流程定单编码', width: 80, sortable: false, hidden: true},
                        {name: 'COMP_USER_ID', label: '处理人', width: 80, sortable: false, hidden: true},
                        {name: 'SRV_ORD_ID', label: '业务定单信息ID', width: 240, sortable: false, hidden: true},
                        {name: 'PS_ID', label: 'PS_ID', width: 240, sortable: false, hidden: true},
                        {name: 'ORDER_ID', label: '流程定单ID', width: 240, sortable: false, hidden: true},
                        {name: 'WO_STATE', label: '工单状态', width: 240, sortable: false, hidden: true},
                        {name: 'WO_ID', label: '工单ID', width: 240, sortable: false, hidden: true, align: 'center'},
                        {name: 'TACHE_ID', label: '环节ID', width: 240, sortable: false, hidden: true},
                        {name: 'REGION_ID', label: '区域ID', width: 240, sortable: false, hidden: true},
                        {name: 'SPECIALTY_CODE', label: '专业Code', width: 240, sortable: false, hidden: true},
                        {name: 'DISPOBJTYEVALUE', label: '岗位/部门值', width: 240, sortable: false, hidden: true},
                        {name: 'DISPOBJTYE', label: '派发类型', width: 240, sortable: false, hidden: true},*/
                    ]
                }
                var queryWorkOrdersGroup = $.proxy(this.queryWorkOrdersGroup, this); //函数作用域改变
                $("#orderDeal-grid").grid({
                    datatype: "json",
                    colModel: this.girdMdel,
                    autowidth: true,
                    curPageSort: true,
                    rowNum: 10,
                    rowList: [5, 10, 15, 20, 50, 100, 200, 500],
                    pager: true,
                    recordtext: "{0}-{1} 共{2}条",
                    pgtext: " 第{0}页/共{1}页",
                    rowtext: "每页{0}条",
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    multiselect: true,
                    shrinkToFit: false,
                    autoResizable: true,
                    // showColumnsFeature: true, //允许用户自定义列展示设置
                    cached: true, //把用户自定义的列展示设置缓存在本地
                    pageData: queryWorkOrdersGroup,
                    afterInsertRow: function (e, rowid, pageData) {
                        $("#orderDeal-grid").grid('setCell', rowid, 'FLOWTRACE', '', {color: '#6DCC4A'});
                        $("#orderDeal-grid").grid('setCell', rowid, 'SOURCENETWORK', '', {color: '#6DCC4A'});
                        switch (pageData.WO_COMPLETE_STATE) {
                            case '正常单':
                                $("#orderDeal-grid").grid('setCell', rowid, 'WO_COMPLETE_STATE', '', {color: '#6DCC4A'});
                                break;
                            case '预警单':
                                $("#orderDeal-grid").grid('setCell', rowid, 'WO_COMPLETE_STATE', '', {color: '#F4C70D'});

                                break;
                            case '超时单':
                                $("#orderDeal-grid").grid('setCell', rowid, 'WO_COMPLETE_STATE', '', {color: '#FF5858'});
                                break;
                        }
                        // modify by zmp [1865797]
                      /*  if( me.queryObj.queryTypeLocal != 'dispConfirm'&& me.queryObj.queryTypeLocal != 'abnormalOrder' && me.queryObj.queryTypeLocal != 'ccOrder'){
                            if(pageData.ISNORMAL > -1) {//超时
                                $("#orderDeal-grid").grid("setRowData", rowid, null, {"color":"#c75450"});
                            }else if(pageData.ISNORMAL <= -1 && pageData.ISNORMAL > -4){//预警
                                $("#orderDeal-grid").grid("setRowData", rowid, null, {"color":"#FFCE66"});
                            }
                         }
                     */
                        },
                    onCellSelect: function (e, rowid, iCol, cellcontent, colName, cellval) {//选中单元格的事件

                        // console.log("onCellSelect---->rowid:" + rowid + ", iCol:" + iCol + ", cellcontent:" + cellcontent + "  ....." + cellval + "--" + colName);
                    },
                    onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                        var dataParent = $("#orderDeal-grid").grid('getRowData', rowid);
                        me.orderDetailViewGroup(dataParent);
                    },
                    gridComplete: function () {
                        $('.gotext').html("<span>跳转至<input class=\"ui-pagination-input\"></span>");
                    },
                    //展开的subGrid  -- start
                    onSelectAll: function (e, status) {
                        console.log('grid parent row selectALL');
                        $('.ui-subgrid').each(function () {
                            var $subGrid = $(this).find('.ui-jqgrid');
                            $subGrid.grid('setAllCheckRows', status);
                        });
                        // debugger;
                    },
                    onSelectRow: function (e, rowId, state, prowchecked) {
                        console.log('grid row selectRow');
                        var row = $("#orderDeal-grid").grid('getGridRowById', rowId);
                        var $next = $(row).next();
                        // debugger;
                        if ($next.hasClass('ui-subgrid')) {
                            var subGridId = this.id + '_' + rowId;
                            var $subGrid = $('#' + subGridId + ' .ui-jqgrid');
                            $subGrid.grid('setAllCheckRows', prowchecked);
                        }
                    },
                    subGrid: true,
                    subGridOptions: {
                        reloadOnExpand: true
                    },
                    subGridBeforeExpand: function (e, subGridId, rowId) {
                        console.log('subGridBeforeExpand', e, subGridId, rowId);
                    },
                    subGridRowExpanded: function (e, subGridId, parentRowId) {
                        console.log('subGridRowExpanded', e, subGridId, parentRowId);
                        me.initorderDealSubGridGroup(subGridId, parentRowId, me);
                    },
                    subGridRowColapsed: function (e, subGridId, rowId) {
                        console.log('subGridRowColapsed', e, subGridId, rowId);
                    }
                    //展开的subGrid  -- end
                });
                this.resize();
            },
            ifBackOrder : function (value){
                var ifBackOrderFlag = '否';
                if (value!= '' && '1'.indexOf(value) != -1){
                    ifBackOrderFlag = '是';
                }
                return ifBackOrderFlag;
            },
            isNormal : function (value){
                var ifBackOrderFlag = '正常单';
                return ifBackOrderFlag;
            },
            formatResourcesName: function (value) {
                var resourceName;
                switch (value) {
                    case 'jike':
                        resourceName = '政企中台';
                        break;
                    case 'onedry':
                        resourceName = '一干调度';
                        break;
                    case 'secondary':
                        resourceName = '二干调度';
                        break;
                    case 'localBuild':
                        resourceName = '本地调度';
                        break;
                    case 'cloudNetwork':
                        resourceName = '云网协同';
                        break;
                    default:
                        resourceName = '';
                        break;
                }
                return resourceName;
            },
            //单据类型转换
            formatItemType: function (value) {
                var itemTypeMap = {
                        '101':'开通单',
                        '102':'核查单',
                        '104':'追单',
                        '108':'加急',
                        '109':'延期',
                        '110':'挂起',
                        '111':'解挂',
                        '112':'撤单'
                    };
                return itemTypeMap[value];
            },
            //动作类型
            formatActiveType: function (value) {
                var activeTypeMap = {
                    '101' : '新开',
                    '103' : '变更',
                    '102' : '拆机',
                    '105' : '复机',
                    '106' : '移机',
                    '104' : '停机'

                };
                return activeTypeMap[value];
            },
            //产品类型
            formatServiceId: function (value) {
                var serviceIdMap = {
                    '20181211001' : '语音中继电路',
                    '10000008' : 'MPLS-VPN',
                    '10000001' : '数字电路',
                    '20181221002' : '裸光纤',
                    '20181221003' : '基础数据(ATM)',
                    '20181221004' : '基础数据(FR)',
                    '10000002' : '以太网专线',
                    '10000011' : '互联网专线(DIA)',
                    '20181221005' : '基础数据(DDN)',
                    '20181221006' : '局内中继电路',
                    '80000466' : '政企精品网业务',
                    '10003406' : 'SD-WAN智选专线'
                };
                return serviceIdMap[value];
            },
            //mainGrid下的subGrid
            initorderDealSubGridGroup: function (subGridId, parentRowId, meTemp) {
                var subModel;
                //获取父节点row data
                var dataSub = $("#orderDeal-grid").grid('getRowData', parentRowId);
                if (meTemp.queryObj.queryTypeLocal != 'dispConfirm') {
                    subModel = [
                        //默认展示字段
                        {name: 'FLOWTRACE', label: '流程跟踪', width: 100 ,formatter: function () { return  '点击查看'}},
                        {name: 'SOURCENETWORK', label: '网络拓扑图', width: 100,formatter: function () { return  '点击查看'}},
                        {name: 'WO_COMPLETE_STATE', label: '单据状态', width: 80, sortable: false, hidden: false},
                        {name: 'CST_ORD_ID', label: '客户Id', width: 80, sortable: false, hidden: true},
                        {name: 'ORDER_CODE', label: '流程订单编码', width: 80, sortable: false, hidden: true},
                        {name: 'SERVICE_ID', label: '业务类型', width: 80, sortable: false, hidden: true},
                        {name: 'COMP_USER_ID', label: '处理人', width: 80, sortable: false, hidden: true},
                        {name: 'SRV_ORD_ID', label: '业务订单信息ID', width: 240, sortable: false, hidden: true},
                        {name: 'PS_ID', label: 'PS_ID', width: 240, sortable: false, hidden: true},
                        {name: 'ORDER_ID', label: '流程订单ID', width: 240, sortable: false, hidden: true},
                        {name: 'WO_STATE', label: '工单状态', width: 240, sortable: false, hidden: true},
                        {name: 'WO_ID', label: '工单ID', width: 240, sortable: false, hidden: true, align: 'center'},
                        {name: 'TACHE_ID', label: '环节ID', width: 240, sortable: false, hidden: true},
                        {name: 'REGION_ID', label: '区域ID', width: 240, sortable: false, hidden: true},
                        {name: 'CUST_NAME_CHINESE', label: '客户名称', width: 200},
                        {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 120, align: 'center'},
                        {name: 'TRADE_ID', label: '业务订单号', width: 120, align: 'center'},
                        {name: 'SERIAL_NUMBER', label: '业务号码', width: 120, align: 'center'},
                        {name: 'ATTR_VALUE', label: '电路编码', width: 120, align: 'center'},
                        {name: 'TACHE_NAME', label: '环节名称', width: 120, align: 'left'},
                        {name: 'REGION_NAME', label: '区域', width: 120, align: 'center'},
                        {name: 'PUB_DATE_NAME', label: '专业', width: 120, align: 'center'},
                        {name: 'DISPATCH_ORDER_NO', label: '调度单编号', width: 120, align: 'center'},
                        {name: 'DISPATCH_TITLE', label: '调度单标题', width: 200},
                        {name: 'APPLY_ORD_NAME', label: '申请单标题', width: 200},
                        {name: 'ALARM_DATE', label: '环节预警时间', width: 110, align: 'center'},
                        {name: 'REQ_FIN_DATE', label: '环节要求完成时间', width: 160, align: 'center'},
                        {name: 'ONE_REQ_FIN_DATE', label: '本省端要求完成时间', width: 160, align: 'center'},
                        {name: 'REQ_FIN_DATE_ORDER', label: '全程要求完成时间', width: 160, align: 'center'},
                    ]
                } else {
                    subModel = [
                        //默认展示字段
                        {name: 'FLOWTRACE', label: '流程跟踪', width: 100 ,formatter: function () { return  '点击查看'}},
                        {name: 'SOURCENETWORK', label: '网络拓扑图', width: 100,formatter: function () { return  '点击查看'}},
                        {name: 'WO_COMPLETE_STATE', label: '单据状态', width: 80, sortable: false, hidden: true},
                        {name: 'CST_ORD_ID', label: '客户Id', width: 80, sortable: false, hidden: true},
                        {name: 'ORDER_CODE', label: '流程订单编码', width: 80, sortable: false, hidden: true},
                        {name: 'SERVICE_ID', label: '业务类型', width: 80, sortable: false, hidden: true},
                        {name: 'PS_ID', label: 'PS_ID', width: 240, sortable: false, hidden: true},
                        {name: 'ORDER_ID', label: '订单ID', width: 240, sortable: false, hidden: true},
                        {name: 'WO_STATE', label: '工单状态', width: 240, sortable: false, hidden: true},
                        {name: 'WO_ID', label: '工单ID', width: 240, sortable: false, hidden: true, align: 'center'},
                        {name: 'TACHE_ID', label: '环节ID', width: 240, sortable: false, hidden: true},
                        {name: 'COMP_USER_ID', label: '处理人', width: 80, sortable: false, hidden: true},
                        {name: 'SRV_ORD_ID', label: '业务订单信息ID', width: 240, sortable: false, hidden: true},
                        {name: 'REGION_ID', label: '区域ID', width: 240, sortable: false, hidden: true},
                        {name: 'CUST_NAME_CHINESE', label: '客户名称', width: 200},
                        {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 120, align: 'center'},
                        {name: 'TRADE_ID', label: '业务订单号', width: 120, align: 'center'},
                        {name: 'SERIAL_NUMBER', label: '业务号码', width: 120, align: 'center'},
                        {name: 'ATTR_VALUE', label: '电路编码', width: 120, align: 'center'},
                        {name: 'TACHE_NAME', label: '环节名称', width: 120, align: 'left'},
                        {name: 'REGION_NAME', label: '区域', width: 120, align: 'center'},
                        {name: 'PUB_DATE_NAME', label: '专业', width: 120, align: 'center'},
                        {name: 'DISPATCH_ORDER_NO', label: '调度单编号', width: 120, align: 'center'},
                        {name: 'DISPATCH_TITLE', label: '调度单标题', width: 200},
                        {name: 'APPLY_ORD_NAME', label: '申请单标题', width: 200},
                        {name: 'ALARM_DATE', label: '环节预警时间', width: 110, align: 'center'},
                        {name: 'REQ_FIN_DATE', label: '环节要求完成时间', width: 160, align: 'center'},
                        {name: 'ONE_REQ_FIN_DATE', label: '本省端要求完成时间', width: 160, align: 'center'},
                        {name: 'REQ_FIN_DATE_ORDER', label: '全程要求完成时间', width: 160, align: 'center'},
                    ]
                }
                // 这里可以构建不同列模型的表格
                var subgrid_table_id = subGridId + '_t';
                $("#" + subGridId).html("<table id='" + subgrid_table_id + "'></table>");
                var $subGrid = $("#" + subgrid_table_id).grid({
                    colModel: subModel,
                    autowidth: true,
                    rowNum: 500,
                    height: 'auto',
                    curPageSort: false,
                    multiselect: true,
                    pager: false,
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    shrinkToFit: false,
                    autoResizable: true,
                    cached: true, //把用户自定义的列展示设置缓存在本地
                    afterInsertRow: function (e, rowid, pageData) {
                        $("#" + subgrid_table_id).grid('setCell', rowid, 'FLOWTRACE', '', {color: '#6DCC4A'});
                        $("#" + subgrid_table_id).grid('setCell', rowid, 'SOURCENETWORK', '', {color: '#6DCC4A'});
                        switch (pageData.WO_COMPLETE_STATE) {
                            case '正常单':
                                $("#" + subgrid_table_id).grid('setCell', rowid, 'WO_COMPLETE_STATE', '', {color: '#6DCC4A'});
                                break;
                            case '预警单':
                                $("#" + subgrid_table_id).grid('setCell', rowid, 'WO_COMPLETE_STATE', '', {color: '#F4C70D'});
                                break;
                            case '超时单':
                                $("#" + subgrid_table_id).grid('setCell', rowid, 'WO_COMPLETE_STATE', '', {color: '#FF5858'});
                                break;
                        }

                        // modify by zmp [1865797]
                      /*  if( meTemp.queryObj.queryTypeLocal != 'dispConfirm'&& meTemp.queryObj.queryTypeLocal != 'abnormalOrder' && meTemp.queryObj.queryTypeLocal != 'ccOrder'){
                            if(pageData.ORDERISNORMAL > -1) {//超时
                                $("#" + subgrid_table_id).grid("setRowData", rowid, null, {"color":"#c75450"});
                            }else if(pageData.ORDERISNORMAL <= -1 && pageData.ORDERISNORMAL > -4){//预警
                                $("#" + subgrid_table_id).grid("setRowData", rowid, null, {"color":"#FFCE66"});
                            }
                        }
                   */
                    },
                    onCellSelect: function (e, rowid, iCol, cellcontent, colName, cellval) {//选中单元格的事件
                        // console.log("onCellSelect---->rowid:" + rowid + ", iCol:" + iCol + ", cellcontent:" + cellcontent + "  ....." + cellval + "--" + colName);
                        var dataCell = $("#" + subgrid_table_id).grid("getRowData",rowid);
                        if(iCol == 1){
                            debugger
                            var popFile = fish.popupView({
                                url: 'module/UnicomLocalNet/resmaster/portal/flowChart/views/flowChartRepView',
                                width: "99%",
                                height: "95%",
                                title: "二干调度流程跟踪图",
                                viewOption: {
                                    orderId: dataCell.ORDER_ID,
                                    srvOrdId: dataCell.SRV_ORD_ID,
                                    psId: dataCell.PS_ID,
                                    woState: '',

                                },
                                callback: function (popup, view) {
                                    // debugger
                                    popup.result.then(function (e) {

                                    }, function (e) {
                                        console.log('关闭了', e);
                                    });
                                }
                            });

                        }

                        if(iCol == 2){
                            debugger
                            var paramsRes={
                                objId:'1111111111111111111111111',
                                objType:2559,
                                objParam: {'MULTI_DATA_SOURCE_CONFIG_REGION_CODE_FOR_TOPO':meTemp.crmRegion,
                                            'OUTER_SYS_PASS_CIRCUIT_NO_VALUE':dataCell.ATTR_VALUE},
                                objName:dataCell.ATTR_VALUE,
                                topoDefId:230004,
                                isReaderCache:true,
                                viewPathId:'1901201',
                                topoName:dataCell.ATTR_VALUE
                            };
                            window.open(meTemp.resNetworkUrl+"&params="+fish.TripleDES.encrypt(JSON.stringify(paramsRes),'zte-soft'));
                        }

                    },
                    onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                        // debugger;
                        meTemp.orderDetailViewGroupView(dataSub, meTemp);
                    },
                    onSelectAll: function (e, status) {
                        console.log('subGrid row selectAll:' + status);
                        $("#orderDeal-grid").grid('setCheckRows', [parentRowId], status);
                        // debugger
                    },
                    onSelectRow: function (e, rowId, state, prowchecked) {
                        console.log('subGrid row selectRow:' + state);
                        //debugger;
                        if (prowchecked) { // 检查父类是否check了
                            var data = $("#" + subgrid_table_id).grid('getCheckRows');
                            if (data.length === $("#" + subgrid_table_id).grid('getRowData').length) {
                                $("#" + subgrid_table_id).grid('setAllCheckRows', true);
                                $("#orderDeal-grid").grid('setCheckRows', [parentRowId], true);
                            }
                        } else {
                            $("#orderDeal-grid").grid('setCheckRows', [parentRowId], false);
                        }

                    },

                });
                // debugger;
                var collQueryObj = new Object();
                var cstOrdId = dataSub.CST_ORD_ID;//客户Id
                var tacheId = dataSub.TACHE_ID;//环节Id
                var regionId = dataSub.REGION_ID;//区域Id
                var specialtyCode = dataSub.SPECIALTY_CODE;//专业
                var srvOrdIds = dataSub.SRV_ORD_IDS;//业务订单Id合集
                var dispObjTyeValue = dataSub.DISPOBJTYEVALUE;//岗位、部门、个人值
                var dispObjTye = dataSub.DISPOBJTYE;//业务订单Id合集

                var compUserId = meTemp.queryObj.compUserId;//
                var woState = meTemp.queryObj.woState;
                var dispType = meTemp.queryObj.dispType;
                var staffId = meTemp.queryObj.staffId;
                var dealUserId = meTemp.queryObj.dealUserId;
                var resourceType = meTemp.queryObj.resourceType;

                collQueryObj.queryTypeLocal = meTemp.queryObj.queryTypeLocal;
                collQueryObj.cstOrdId = cstOrdId;
                debugger
                collQueryObj.srvOrdIds=srvOrdIds; //add ren.jiahang 抄送单选择后要按选中抄送的电路展示。
                collQueryObj.tacheId = tacheId;
                collQueryObj.regionId = regionId;
                collQueryObj.specialtyCode = specialtyCode;
                collQueryObj.resourceType = resourceType;
                collQueryObj.dispObjTyeValue = dispObjTyeValue;
                collQueryObj.dispObjTye = dispObjTye;

                collQueryObj.compUserId = compUserId;
                collQueryObj.woState = woState;
                collQueryObj.dispType = dispType;
                collQueryObj.staffId = staffId;
                collQueryObj.dealUserId = dealUserId;
                debugger;
                orderStandbyAction.querySubOrderInfoColl(collQueryObj, function (data) {
                    // debugger;
                    if (data.messages = "success") {
                        $("#" + subgrid_table_id).grid("reloadData", data.data);
                    } else {
                        fish.toast("error", "获取数据失败");
                    }
                })
                // meTemp.queryWorkOrdersGroupSub(dataSub.CST_ORD_ID,dataSub.WO_ID,$("#" + subgrid_table_id));

            },
            //查询工单方法
            queryWorkOrdersGroup: function (page, rowNum, sortname, sortorder) {
                var srvOrdId = $("#srvOrdId").val();//申请单编号
                var subscribeId = $("#subscribeId").val();//客户订单号
                var orderTitle = $("#orderTitle").val();//申请单标题
                var dispatchOrderId = $("#dispatchOrderId").val();//调单编号
                var serialNumber = $("#serialNumber").val();//调单编号
            //    var teacheName = $("#teacheName").val();//环节名称
                var finishDate = $("#finishDate").val();//环节要求完成时间
                var endDate = $("#endDate").val();//环节完成时间
                var custName = $("#custName").val();//客户名称
                var resourceType = $("#resourceType").val();//数据来源
                var itemType = $("#itemType").val(); //单据类型
                var woOrderBackFlag = $("#ifWoOrderBack").val(); //是否退单
                var localFinishDate = $("#localFinishDate").val();//本地要求完成时间
                var localEndDate = $("#localEndDate").val();//
                var qcFinishDate = $("#qcFinishDate").val();//全程调测时间
                var qcEndDate = $("#qcEndDate").val();//

                this.queryObj.finishDate = finishDate;
                this.queryObj.endDate = endDate;
                this.queryObj.srvOrdId = srvOrdId;
                this.queryObj.subscribeId = subscribeId;
                this.queryObj.orderTitle = orderTitle;
                this.queryObj.dispatchOrderId = dispatchOrderId;
                this.queryObj.serialNumber = serialNumber;
                this.queryObj.tacheIds = $("#teacheName").val() != null ? $("#teacheName").val().join(',') : '';
                this.queryObj.custName = custName;
                this.queryObj.resourceType = resourceType;
                this.queryObj.itemType = itemType;
                this.queryObj.woOrderBackFlag = woOrderBackFlag;
                this.queryObj.localFinishDate = localFinishDate;
                this.queryObj.localEndDate = localEndDate;
                this.queryObj.qcFinishDate = qcFinishDate;
                this.queryObj.qcEndDate = qcEndDate;

                // debugger;
                rowNum = (rowNum != '' && rowNum != undefined) ? rowNum : this.localStandyNum;
                page = (page != '' && page != undefined) ? page : this.localStandyPage;
                fish.store.set('orderDeal-grid-rowNum', rowNum); //记录用户选择的每页记录数

                this.queryObj.pageIndex = page + '';
                this.queryObj.pageSize = rowNum + '';
                // debugger;
                //获取表单信息
                var formValue = $('#orderDeal-form').form("value");
                //调用后台方法
                $.blockUI();
                this.standbyOrderInfoGroup(this);

            },
            //去除遮罩层
            cleanShadeFun: function (meSa){
                if(meSa.deptStandny&&meSa.jobStandby&&meSa.staffStandby
                    &&meSa.dealOrder&&meSa.dispConfirm&&meSa.abnormalOrder&&meSa.ccOrder) {
                    //遮罩层
                    $.unblockUI();
                    meSa.deptStandny = false;
                    meSa.jobStandby = false;
                    meSa.staffStandby = false;
                    meSa.dealOrder = false;
                    meSa.dispConfirm = false;
                    meSa.abnormalOrder = false;
                    meSa.ccOrder = false;
                }
            },
            standbyOrderCountGroup: function (meCount) {
                    var queryObjCount = meCount.queryObj;
                        orderStandbyAction.queryStandbyOrderEachCount(queryObjCount, function (datas) {
                            if (datas.messages == "success") {
                                $('#deptStandny').text(datas.deptStandny);
                                $('#jobStandby').text(datas.jobStandby);
                                $('#staffStandby').text(datas.staffStandby);
                                $('#dealOrder').text(datas.dealOrder);
                                $('#dispConfirm').text(datas.dispConfirm);
                                $('#abnormalOrder').text(datas.abnormalOrder);
                                $('#ccOrder').text(datas.ccOrder);
                            }else{
                                fish.toast("error", "获取数据失败:"+datas.message);
                            }
                        })

            },
            standbyOrderInfoGroup: function (meInfo) {
                var me = this;
                orderStandbyAction.qryCstOrdList(meInfo.queryObj, function (data) {
                    // debugger
                    switch (meInfo.queryObj.queryTypeLocal) {
                        case 'deptStandny':
                            meInfo.deptStandny = true;
                            break;
                        case 'jobStandby':
                            meInfo.jobStandby = true;
                            break;
                        case 'staffStandby':
                            meInfo.staffStandby = true;
                            break;
                        case 'dealOrder':
                            meInfo.dealOrder = true;
                            break;
                        case 'dispConfirm':
                            meInfo.dispConfirm = true;
                            break;
                        case 'abnormalOrder':
                            meInfo.abnormalOrder = true;
                            break;
                        case 'ccOrder':
                            meInfo.ccOrder = true;
                            break;
			    case 'applyOrder':
                            meInfo.applyOrder = true;
                            break;
                    }
                    meInfo.cleanShadeFun(meInfo);
                    if (data.messages = "success") {
                        switch (meInfo.queryObj.queryTypeLocal) {
                            case 'deptStandny':
                                $('#deptStandny').text(data.dataLength);
                                break;
                            case 'jobStandby':
                                $('#jobStandby').text(data.dataLength);
                                break;
                            case 'staffStandby':
                                $('#staffStandby').text(data.dataLength);
                                break;
                            case 'dealOrder':
                                $('#dealOrder').text(data.dataLength);
                                break;
                            case 'dispConfirm': //完成确认单
                                $('#dispConfirm').text(data.dataLength);
                                break;
                            case 'ccOrder':
                                $('#ccOrder').text(data.dataLength);
                                break;
                            case 'abnormalOrder':
                                $('#abnormalOrder').text(data.dataLength);
                                break;
				    case 'applyOrder':
                                $('#applyOrder').html(data.dataLength);
                                break;
                        }
                        var gridData = {
                            "rows": data.data,
                            "page": data.pageIndex,
                            "records": data.dataLength,
                            "rowNum": data.rowNum,
                            "total": data.total
                        };
                        // debugger
                        // $("#orderDeal-grid").grid('expandAllSubGridRow',true);
                        $("#orderDeal-grid").grid('setGridParam', {
                            showSubgridBtn: function (rowdata) {
                                return (meInfo.queryObj.queryTypeLocal == 'abnormalOrder') ? false : true;
                            }
                        });
                        var colArr = ['ACTIVETYPENAME','SERVICETYPE','COUNTS'];
                        var abnormalColArr = ['CHG_VERSION'];
                        if (meInfo.queryObj.queryTypeLocal == 'abnormalOrder') {
                            $("#orderDeal-grid").grid('hideCol', colArr);
                            $("#orderDeal-grid").grid('showCol', abnormalColArr);
                        } else {
                            $("#orderDeal-grid").grid('showCol', colArr);
                            $("#orderDeal-grid").grid('hideCol', abnormalColArr);
                        }
                        $("#orderDeal-grid").grid("reloadData", gridData);
                        //$.unblockUI();
                    } else {
                        fish.toast("error", "获取数据失败");
                    }
                }).done(function () {
                    me.standbyOrderCountGroup(me);
                }).always(function () {
                    $.unblockUI();
                });
            },
            touchGet: function () {        //签收
                var woOrderIds = new Array();
                var srvOrdIds = new Array();
                var queryParams = new Object();
                var selarrowJson = {};//存在已遍历过的wo_id
                var selarrrow = $("#orderDeal-grid").grid("getCheckRows");
                // debugger
                var subLength = 0;
                $('.ui-subgrid').each(function () {
                    var $subGrid = $(this).find('.ui-jqgrid');
                    var subCheck = $subGrid.grid("getCheckRows");
                    $.each(subCheck, function (p) {
                        var sewoId = subCheck[p].WO_ID;
                        if (selarrowJson[sewoId] == ''
                            || selarrowJson[sewoId] == undefined) {
                            selarrowJson[sewoId] = sewoId;
                            subLength++;
                            woOrderIds.push(sewoId);
                        }
                    });
                });
                $.each(selarrrow, function (p) {
                    // console.log('关闭了'+p);
                    var seP = selarrrow[p];
                    var tacheId = seP.tacheId;
                    var resources = seP.RESOURCES;
                    if (seP == undefined) {
                        return true;
                    }
                    var seWoIds = seP.WO_IDS;
                    if (seWoIds != ''
                        && seWoIds != undefined) {
                        var splitWoIds = seWoIds.split(",");
                        $.each(splitWoIds, function (index, obj) {
                            woOrderIds.push(obj);
                        });
                    }
                    var sesrvOrdIds = seP.SRV_ORD_IDS;
                    if (sesrvOrdIds != ''
                        && sesrvOrdIds != undefined) {
                        //只有一干电路下发的单子且是电路调度环节才存取 ，通知一干需要
                        if(tacheId == "510101040" && resources == "onedry"){
                            var splitSrvOrdIds = sesrvOrdIds.split(",");
                            $.each(splitSrvOrdIds, function (index, obj) {
                                srvOrdIds.push(obj)
                            });
                        }
                    }

                });
                // debugger;
                if (woOrderIds.length <= 0) {
                    fish.warn("请选择一条或多条数据");
                    return;
                }
                var touMe = this;
                touMe.queryObj.standbyType = [
                    'dealOrder'
                ];
                queryParams.woOrderIds = woOrderIds;
                queryParams.srvOrdIds = srvOrdIds;
                queryParams.actionType = 'get';
                // debugger;
                orderStandbyAction.getFreeWoOrder(queryParams, function (data) {
                    if (data.success) {
                        touMe.standbyOrderInfoGroup(touMe);
                        touMe.standbyOrderCountGroup(touMe);
                        fish.toast("warn", data.message);
                    } else {
                        fish.toast("warn", data.message);
                    }
                });

            },
            cc_confirm:function () {
                var me =this;
                var selarrrow = $("#orderDeal-grid").grid("getCheckRows");
                if(selarrrow.length < 1){
                    fish.warn("请选择数据！");
                }else{
                    fish.confirm('确认后此抄送单将不再显示，确定么？').result.then(function() {
                        for(var i =0;i < selarrrow.length; i++){
                            var srvOrdIds= selarrrow[i].SRV_ORD_IDS;
                            var woId= selarrrow[i].WO_ID;
                            var staffId=me.userInfo.userId;
                            orderStandbyAction.updateCC({'woId':woId,'state':1,'dispObjType':'260000003','dispObjId': staffId,'srvOrdIds': srvOrdIds},function (res) {
                              //
                            })
                        }
                        fish.toast('info','抄送单已确认');
                        //回调下查询方法
                        me.agClick();
                    });
                }

            },
            releaseTouchGet: function () {//释放签收
                var woOrderIds = new Array()
                var queryParams = new Object();
                var selarrowJson = {};//存在已遍历过的wo_id
                var selarrrow = $("#orderDeal-grid").grid("getCheckRows");
                // debugger
                var subLength = 0;
                $('.ui-subgrid').each(function () {
                    var $subGrid = $(this).find('.ui-jqgrid');
                    var subCheck = $subGrid.grid("getCheckRows");
                    $.each(subCheck, function (p) {
                        var sewoId = subCheck[p].WO_ID;
                        if (selarrowJson[sewoId] == ''
                            || selarrowJson[sewoId] == undefined) {
                            selarrowJson[sewoId] = sewoId;
                            subLength++;
                            woOrderIds.push(sewoId);
                        }
                    });
                });
                $.each(selarrrow, function (p) {
                    var seP = selarrrow[p];
                    if (seP == undefined) {
                        return true;
                    }
                    var seWoIds = seP.WO_IDS;
                    if (seWoIds != ''
                        && seWoIds != undefined) {
                        var splitWoIds = seWoIds.split(",");
                        $.each(splitWoIds, function (index, obj) {
                            woOrderIds.push(obj);
                        });
                    }
                });
                // debugger;
                if (woOrderIds.length <= 0) {
                    fish.warn("请选择一条或多条数据");
                    return;
                }
                var touMe = this;
                touMe.queryObj.standbyType = [
                    'deptStandny',
                    'jobStandby',
                    'staffStandby'
                                 ];
                queryParams.woOrderIds = woOrderIds;
                queryParams.actionType = 'free';
                // debugger;
                orderStandbyAction.getFreeWoOrder(queryParams, function (data) {
                    if (data.success) {
                        touMe.standbyOrderInfoGroup(touMe);
                        touMe.standbyOrderCountGroup(touMe);
                        fish.toast("warn", data.message);
                    } else {
                        fish.toast("warn", data.message);
                    }
                });

            },
            aaClick: function () {
                $("#touchGet").show();
                $("#collapsible").show();
                $("#releaseTouchGet").hide();
                $("#touchConfirm").hide();
                $("#applyCheck").hide();

                //  $("#touchGet").css("style","display:block; float: left;");
                /**
                 * 部门待办 正常单
                 */
                this.showActionButtons = 'deptStandny';
                this.queryObj.queryTypeLocal = 'deptStandny';
                this.queryObj.compUserId = '';
                this.queryObj.dealUserId = '';
                this.queryObj.staffId = this.staffId;
                this.queryObj.dispType = '260000001';
                this.queryObj.woState = '290000002';
                this.queryObj.standbyType = standbyType;
                this.initorderDealGridGroup();
                this.queryWorkOrdersGroup();
            },
            abClick: function () {
                //$("#touchGet").css("style","display:block; float: left;");
                $("#touchGet").show();
                $("#collapsible").show();
                $("#releaseTouchGet").hide();
                $("#touchConfirm").hide();
                $("#applyCheck").hide();

                /**
                 * 岗位待办
                 */
                this.showActionButtons = 'jobStandby';
                this.queryObj.queryTypeLocal = 'jobStandby';
                this.queryObj.compUserId = '';
                this.queryObj.dealUserId = '';
                this.queryObj.staffId = this.staffId;
                this.queryObj.dispType = '260000002';
                this.queryObj.woState = '290000002';
                this.initorderDealGridGroup();
                this.queryWorkOrdersGroup();

            },
            acClick: function () {
                // $("#touchGet").css("style","display:block; float: left;");
                $("#touchGet").show();
                $("#collapsible").show();
                $("#releaseTouchGet").hide();
                $("#touchConfirm").hide();
                $("#applyCheck").hide();

                /**
                 * 个人待办
                 */
                this.showActionButtons = 'staffStandby';
                this.queryObj.queryTypeLocal = 'staffStandby';
                this.queryObj.compUserId = '';
                this.queryObj.dealUserId = '';
                this.queryObj.staffId = this.staffId;
                this.queryObj.dispType = '260000003';
                this.queryObj.woState = '290000002';
                this.queryObj.standbyType = standbyType;
                this.initorderDealGridGroup();
                this.queryWorkOrdersGroup();
            },
            adClick: function () {
                $("#touchGet").hide();
                $("#touchConfirm").hide();
                $("#collapsible").show();
                $("#resetSelect").show();
                $("#releaseTouchGet").show();
                $("#applyCheck").hide();

                /**
                 * 处理中
                 */
                this.showActionButtons = 'dealOrder';
                this.queryObj.queryTypeLocal = 'dealOrder';
                this.queryObj.compUserId = '';
                this.queryObj.dealUserId = this.staffId;
                this.queryObj.staffId = '';
                this.queryObj.dispType = '260000003';
                this.queryObj.woState = '290000002'; //处理中
                this.queryObj.standbyType = standbyType;
                this.initorderDealGridGroup();
                this.queryWorkOrdersGroup();
            },
            aeClick: function () {
                $("#touchGet").hide();
                $("#collapsible").show();
                $("#releaseTouchGet").hide();
                $("#touchConfirm").hide();
                $("#applyCheck").hide();

                /**
                 * 确认完成单
                 */
                this.showActionButtons = 'dispConfirm';
                this.queryObj.queryTypeLocal = 'dispConfirm';
                this.queryObj.compUserId = this.staffId;
                this.queryObj.dealUserId = '';
                this.queryObj.staffId = '';
                this.queryObj.dispType = '260000004';
                this.queryObj.woState = '290000004';
                //this.queryObj.standbyType = standbyType;
                this.initorderDealGridGroup();
                this.queryWorkOrdersGroup();
            },
            afClick: function () {
                $("#touchGet").hide();
                $("#collapsible").hide();
                $("#releaseTouchGet").hide();
                $("#exportExcel").hide();
                $("#resetSelect").hide();
                $("#touchConfirm").hide();
                $("#applyCheck").hide();

                // showActionButtons = 'dealOrder';
                this.showActionButtons = 'abnormalOrder';
                this.queryObj.queryTypeLocal = 'abnormalOrder';
                this.queryObj.compUserId = '';
                this.queryObj.dealUserId = this.staffId;
                this.queryObj.staffId = this.staffId;
                this.queryObj.dispType = '';
                this.queryObj.woState = '290000002'; //处理中
                this.queryObj.standbyType = standbyType;
                // queryObj.chgType = '104'; //追单
                this.initorderDealGridGroup();
                this.queryWorkOrdersGroup();
            },
            agClick: function () { //已废弃
                $("#touchGet").hide();
                $("#touchConfirm").show();
                $("#resetSelect").show();
                $("#applyCheck").hide();

                /**
                 * 抄送单
                 */
                this.showActionButtons = 'ccOrder';
                this.queryObj.queryTypeLocal='ccOrder';
                this.queryObj.staffId=this.staffId;
                this.queryObj.dispType='260000005';
                this.queryObj.woState=''; //工单状态，不需要按状态查询
                this.queryObj.standbyType = standbyType;
                this.initorderDealGridGroup();
                this.queryWorkOrdersGroup();
            },
        ahClick: function () {  //延期申请单
                $("#touchGet").hide();
                $("#exceptionAffirm").hide();
                $("#touchConfirm").hide();
                $("#resetSelect").show();
                $("#applyCheck").show();
                $("#collapsible").hide();
                $("#exportExcel").hide();

                /**
                 * 抄送单
                 */
                this.showActionButtons = 'applyOrder';
                this.queryObj.queryTypeLocal='applyOrder';
                this.queryObj.staffId=this.staffId;
                this.queryObj.dispType='260000005';
                this.queryObj.woState=''; //工单状态，不需要按状态查询
                this.queryObj.standbyType = standbyType;
                this.initorderDealGridGroup();
                this.queryWorkOrdersGroup();
            },
            showMore: function () {  //更多
                $("#showMore").hide();
                $("#retract").show();
                $("#more1").show();
                $("#more2").show();
              /*   $("#more3").show();*/
            },
            retract: function () {  //收起
                $("#more1").hide();
                $("#more2").hide();
              /*    $("#more3").hide();*/
                $("#retract").hide();
                $("#showMore").show();
            },
            initQueryBut: function () { //查询
                var startDate = $("#finishDate").val();//环节要求完成时间
                var endDate = $("#endDate").val();//环节完成时间
                var localStartDate = $("#localFinishDate").val();//本地要求完成时间
                var localEndDate = $("#localEndDate").val();//
                var qcStartDate = $("#qcFinishDate").val();//全程调测完成时间
                var qcEndDate = $("#qcEndDate").val();//
                if ((startDate && endDate && startDate >= endDate) || ( localStartDate && localEndDate && localStartDate >= localEndDate) || (qcStartDate && qcEndDate && qcStartDate >= qcEndDate)) {
                    fish.warn("开始时间不能大于完成时间！");
                    return;
                } else if( (startDate != '' && endDate == '') || (startDate == '' && endDate != '')||
                    (localStartDate != '' && localEndDate == '') || (localStartDate == '' && localEndDate != '')||
                    (qcStartDate != '' && qcEndDate == '') || (qcStartDate == '' && qcEndDate != '')) {
                    fish.warn("请选择时间段！");
                    return;
                } else {
                    switch (this.queryObj.queryTypeLocal) {
                        case 'deptStandny':
                            this.aaClick();
                            break;
                        case 'jobStandby':
                            this.abClick();
                            break;
                        case 'staffStandby':
                            this.acClick();
                            break;
                        case 'dealOrder':
                            this.adClick();
                            break;
                        case 'dispConfirm': //完成确认单
                            this.aeClick();
                            break;
                        case 'abnormalOrder':
                            this.afClick();
                            break;
                        case 'ccOrder':
                            this.agClick();
                            break;
                        case 'applyOrder':
                            this.ahClick();
                            break;
                    }
                }
            },
            orderDetailViewGroup: function (dataParent) { //详情页面
                var me = this;
                if (dataParent != null
                    && dataParent != ''
                    && (dataParent.SRV_ORD_IDS != null
                        && dataParent.SRV_ORD_IDS != ''
                        || dataParent.ABNORMAL_ORDER == 'abnormalOrder')) {
                    me.orderDetailViewGroupView(dataParent, me);
                }
            },
            orderDetailViewGroupView: function (dataView, meTemp) {
                    this.queryObj.standbyType = [
                        'deptStandny',
                        'jobStandby',
                        'staffStandby',
                        'dealOrder',
                        'dispConfirm',
                        //'exceptionOrder',
                        'abnormalOrder',
                        'ccOrder'
                    ];
                if (dataView.ABNORMAL_ORDER == 'abnormalOrder') { //异常单
                    var chgType = dataView.ITEMTYPE;
                    if (['104', '108', '109'].indexOf(chgType) > -1) { //追单，加急, 延期
                        var pop = fish.popupView({
                            url: 'module/UnicomLocalNet/resmaster/portal/orderAbnormal/view/orderAppendView',
                            width: "99%",
                            height: "100%",
                            title: "定单详情",
                            viewOption: {
                                chgType: chgType,
                                orderId: dataView.ORDER_ID,
                                orderIds: dataView.ORDER_IDS,
                                woIds: dataView.WO_IDS,
                                tacheId: dataView.TACHE_ID,
                                serviceId: dataView.SERVICE_ID,
                                RESOURCES: dataView.RESOURCES,
                                tacheCode: dataView.TACHE_CODE,
                                woId: dataView.WO_ID,
                                woCode: dataView.WO_CODE,
                                chgVersion: dataView.CHG_VERSION,
                                applyOrdId: dataView.APPLY_ORD_ID,
                                cstOrdId: dataView.CST_ORD_ID, //客户订单id
                                dealUserId: dataView.DEAL_USER_ID, //处理人
                                selectType: 'localStandy',
                                userInfo: meTemp.userInfo,
                                levelId: dataView.LEVEL_ID
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (e) {
                                    meTemp.queryWorkOrdersGroup();
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        });
                    }else if (['110', '111', '112'].indexOf(chgType) > -1) {//挂起解挂撤单
                        var cstOrdId = dataView.CST_ORD_ID + '';
                        var srvOrdIds = '';
                        var ret = orderStandbyAction.qrySrvOrdIds(cstOrdId).responseJSON.data;
                        if(ret.success){
                            srvOrdIds = ret.srvOrdIds;
                        }else {
                            fish.toast('warn', res.message);
                            return;
                        }
                        var pop = fish.popupView({
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/orderDetailsView',
                            width: "99%",
                            height: "100%",
                            title: "工单详情",
                            viewOption: {
                                chgType: chgType,
                                dealUserId: dataView.DEAL_USER_ID,
                                woIds: dataView.WO_IDS,
                                srvOrdStat : dataView.SRV_ORD_STAT,
                                orderState : dataView.ORDER_STATE,
                                userInfo: meTemp.userInfo,
                                RESOURCES: dataView.RESOURCES,
                                srvOrdId: srvOrdIds,
                                orderId: '',
                                cstOrdId: cstOrdId //客户订单id
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (e) {
                                    meTemp.queryWorkOrdersGroup();
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        });
                    }
                } else {
                    var pop = fish.popupView({
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/orderDetailsView',
                        width: "100%",
                        height: "100%",
                        title: "工单详情",
                        modal: false,
                        viewOption: {
                            buttonState: meTemp.showActionButtons,
                            psId: dataView.PS_ID,
                            orderId: dataView.ORDER_IDS.split(',')[0],
                            orderIdSelect: '',
                            orderIds: dataView.ORDER_IDS,
                            woState: dataView.WO_STATES.split(',')[0],
                            woId: dataView.WO_IDS.split(',')[0],
                            applyWoId: dataView.WO_ID, //延期申请工单标识
                            woIds: dataView.WO_IDS,
                            RESOURCES: dataView.RESOURCES,
                            srvOrdIds: dataView.SRV_ORD_IDS,
                            serviceId: dataView.SERVICE_ID,
                            srvOrdId: dataView.SRV_ORD_IDS.split(',')[0], //业务订单id
                            cstOrdId: dataView.CST_ORD_ID, //客户订单id
                            sender: dataView.SENDER, //一干内部/外系统标识
                            reginonId: dataView.REGION_ID, //区域id
                            tacheId: dataView.TACHE_ID,
                            specialtyCode: dataView.SPECIALTY_CODE, //专业code
                            woStateCir: meTemp.queryObj.woState, //工单状态(过滤电路信息使用)
                            compUserId: meTemp.queryObj.compUserId,
                            dispType: meTemp.queryObj.dispType,
                            staffId: meTemp.queryObj.staffId,
                            dealUserId: meTemp.queryObj.dealUserId, //处理人
                            dispObjTyeValue: dataView.DISPOBJTYEVALUE,
                            dispObjTye: dataView.DISPOBJTYE,
                            activeType: dataView.ACTIVE_TYPE,
                            orderType: dataView.ITEMTYPE,
                            selectType: 'localStandy',
                            userInfo: meTemp.userInfo,
                            srvOrdStat : dataView.SRV_ORD_STATS.split(',')[0],
                            orderState : dataView.ORDER_STATES.split(',')[0],
                            woOrderBackFlags: dataView.WOORDERBACKFLAGS
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (e) {
                                meTemp.queryWorkOrdersGroup();
                            }, function (e) {
                                console.log('关闭了', e);
                                // $("#touchGet").css("style","display:block; float: left;");
                                // $("#touchGet").show();
                            });
                        }
                    })
                }


            },
            collapsible: function () {     //填写阶段性意见
                var selarrrow = $("#orderDeal-grid").grid("getCheckRows");
                var fileId = new Date().getTime();
                var selrowCollapSible;
                var updateParams = new Object();
                updateParams.selarrrow = selarrrow;
                if (selarrrow.length < 1) {
                    fish.warn("请选择一条数据");
                    return;
                }
                var $log = $('.well')
                function appendLog(message) {
                    $log.append(message + '<br>')
                }

                // debugger;
                var cstOrdId = selarrrow[0].CST_ORD_ID;//客户Id
                var tacheId = selarrrow[0].TACHE_ID;//环节Id
                var regionId = selarrrow[0].REGION_ID;//区域Id
                var specialtyCode = selarrrow[0].SPECIALTY_CODE;//专业
                var srvOrdIds = selarrrow[0].SRV_ORD_IDS;//业务订单Id合集
                var dispObjTyeValue = selarrrow[0].DISPOBJTYEVALUE;//岗位、部门、个人值
                var dispObjTye = selarrrow[0].DISPOBJTYE;//业务订单Id合集
                debugger
                var compUserId = this.queryObj.compUserId;//
                var dealUserId = this.queryObj.dealUserId;
                var staffId = this.queryObj.staffId;
                var dispType = this.queryObj.dispType;
                var woState = this.queryObj.woState;
                var resourceType = this.queryObj.resourceType;
                var meTempCo = this;

                // debugger
                var girdCollMdel;
                if (this.queryObj.queryTypeLocal != 'dispConfirm') {
                    girdCollMdel = [
                        //默认展示字段
                        {name: 'WO_COMPLETE_STATE', label: '单据状态', width: 80, sortable: false, hidden: false},
                        {name: 'CST_ORD_ID', label: '客户Id', width: 80, sortable: false, hidden: true},
                        {name: 'ORDER_CODE', label: '流程订单编码', width: 80, sortable: false, hidden: true},
                        {name: 'SERVICE_ID', label: '业务类型', width: 80, sortable: false, hidden: true},
                        {name: 'COMP_USER_ID', label: '处理人', width: 80, sortable: false, hidden: true},
                        {name: 'SRV_ORD_ID', label: '业务订单信息ID', width: 240, sortable: false, hidden: true},
                        {name: 'PS_ID', label: 'PS_ID', width: 240, sortable: false, hidden: true},
                        {name: 'ORDER_ID', label: '流程订单ID', width: 240, sortable: false, hidden: true},
                        {name: 'WO_STATE', label: '工单状态', width: 240, sortable: false, hidden: true},
                        {name: 'WO_ID', label: '工单ID', width: 240, sortable: false, hidden: true, align: 'center'},
                        {name: 'TACHE_ID', label: '环节ID', width: 240, sortable: false, hidden: true},
                        {name: 'CUST_NAME_CHINESE', label: '客户名称', width: 200},
                        {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 120, align: 'center'},
                        {name: 'TRADE_ID', label: '业务订单号', width: 120, align: 'center'},
                        {name: 'ATTR_VALUE', label: '电路编码', width: 120, align: 'center'},
                        {name: 'TACHE_NAME', label: '环节名称', width: 120, align: 'left'},
                        {name: 'REGION_NAME', label: '区域', width: 120, align: 'center'},
                        {name: 'PUB_DATE_NAME', label: '专业', width: 120, align: 'center'},
                        {name: 'DISPATCH_ORDER_NO', label: '调度单编号', width: 120, align: 'center'},
                        {name: 'DISPATCH_TITLE', label: '调度单标题', width: 200},
                        {name: 'APPLY_ORD_NAME', label: '申请单标题', width: 200},
                        {name: 'ALARM_DATE', label: '环节预警时间', width: 160, align: 'center'},
                        {name: 'REQ_FIN_DATE', label: '环节要求完成时间', width: 160, align: 'center'},
                    ]
                } else {
                    girdCollMdel = [
                        //默认展示字段
                        {name: 'WO_COMPLETE_STATE', label: '单据状态', width: 80, sortable: false, hidden: true},
                        {name: 'CST_ORD_ID', label: '客户Id', width: 80, sortable: false, hidden: true},
                        {name: 'ORDER_CODE', label: '流程订单编码', width: 80, sortable: false, hidden: true},
                        {name: 'SERVICE_ID', label: '业务类型', width: 80, sortable: false, hidden: true},
                        {name: 'PS_ID', label: 'PS_ID', width: 240, sortable: false, hidden: true},
                        {name: 'ORDER_ID', label: '订单ID', width: 240, sortable: false, hidden: true},
                        {name: 'WO_STATE', label: '工单状态', width: 240, sortable: false, hidden: true},
                        {name: 'WO_ID', label: '工单ID', width: 240, sortable: false, hidden: true, align: 'center'},
                        {name: 'TACHE_ID', label: '环节ID', width: 240, sortable: false, hidden: true},
                        {name: 'COMP_USER_ID', label: '处理人', width: 80, sortable: false, hidden: true},
                        {name: 'SRV_ORD_ID', label: '业务订单信息ID', width: 240, sortable: false, hidden: true},
                        {name: 'CUST_NAME_CHINESE', label: '客户名称', width: 200},
                        {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 120, align: 'center'},
                        {name: 'TRADE_ID', label: '业务订单号', width: 120, align: 'center'},
                        {name: 'ATTR_VALUE', label: '电路编码', width: 120, align: 'center'},
                        {name: 'TACHE_NAME', label: '环节名称', width: 120, align: 'left'},
                        {name: 'REGION_NAME', label: '区域', width: 120, align: 'center'},
                        {name: 'PUB_DATE_NAME', label: '专业', width: 120, align: 'center'},
                        {name: 'DISPATCH_ORDER_NO', label: '调度单编号', width: 120, align: 'center'},
                        {name: 'DISPATCH_TITLE', label: '调度单标题', width: 200},
                        {name: 'APPLY_ORD_NAME', label: '申请单标题', width: 200},
                        {name: 'ALARM_DATE', label: '环节预警时间', width: 160, align: 'center'},
                        {name: 'REQ_FIN_DATE', label: '环节要求完成时间', width: 160, align: 'center'},
                    ]
                }
                // debugger;
                var popFile = fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/views/LocalStandbyUpload',
                    width: "85%",
                    height: "96%",
                    title: "阶段意见",
                    viewOption: {
                        queryObj: this.queryObj,
                        selArrrow: selarrrow,
                        girdCollMdel: girdCollMdel,
                        URl: this.URl,
                        staffIdColl: this.userInfo.userId

                    },
                    callback: function (popup, view) {
                        // debugger
                        popup.result.then(function (e) {
                            meTempCo.queryWorkOrdersGroup();
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                });

            },
            exportExcel: function () {  //导出
                // debugger;
                var selarrrow = $("#orderDeal-grid").grid("getCheckRows");
                this.queryObj.selarrrow = selarrrow;
                orderStandbyAction.exportStandbyOrderData(this.URl + '/localScheduleLT/orderStandbyController/exportStandbyOrderData.spr', this.queryObj);
            },
            resetSelect: function () {
                $("#orderDeal-grid").grid("setAllCheckRows", false);
            },
            //浏览器窗口大小改变事件
            resize: function () {
                var frameHeight = $(parent.window).height();
                $("#orderDeal-grid").grid("setGridHeight", frameHeight - 363);
                // $("#orderDeal-grid").grid("resize",true);
            },
            standbyOrderCount: function () {
                orderStandbyAction.queryStandbyOrderCount(this.queryObj, function (datas) {
                    if (datas.messages = "success") {
                        switch (this.queryObj.queryTypeLocal) {
                            case 'deptStandny':
                                $('#jobStandby').text(datas.jobStandby);
                                $('#staffStandby').text(datas.staffStandby);
                                $('#dealOrder').text(datas.dealOrder);
                                $('#dispConfirm').text(datas.dispConfirm);
                                $('#ccOrder').text(datas.ccOrder);
                                $('#applyOrder').text(datas.applyOrder);
                                break;
                            case 'jobStandby':
                                $('#deptStandny').text(datas.deptStandny);
                                $('#staffStandby').text(datas.staffStandby);
                                $('#dealOrder').text(datas.dealOrder);
                                $('#dispConfirm').text(datas.dispConfirm);
                                $('#ccOrder').text(datas.ccOrder);
                                $('#applyOrder').text(datas.applyOrder);
                                break;
                            case 'staffStandby':
                                $('#deptStandny').text(datas.deptStandny);
                                $('#jobStandby').text(datas.jobStandby);
                                $('#dealOrder').text(datas.dealOrder);
                                $('#dispConfirm').text(datas.dispConfirm);
                                $('#ccOrder').text(datas.ccOrder);
                                $('#applyOrder').text(datas.applyOrder);
                                break;
                            case 'dealOrder':
                                $('#deptStandny').text(datas.deptStandny);
                                $('#jobStandby').text(datas.jobStandby);
                                $('#staffStandby').text(datas.staffStandby);
                                $('#dispConfirm').text(datas.dispConfirm);
                                $('#ccOrder').text(datas.ccOrder);
                                $('#applyOrder').text(datas.applyOrder);
                                break;
                            case 'dispConfirm': //完成确认单
                                $('#deptStandny').text(datas.deptStandny);
                                $('#jobStandby').text(datas.jobStandby);
                                $('#staffStandby').text(datas.staffStandby);
                                $('#dealOrder').text(datas.dealOrder);
                                $('#ccOrder').text(datas.ccOrder);
                                $('#applyOrder').text(datas.applyOrder);
                                break;
                            case 'ccOrder':
                                $('#deptStandny').text(datas.deptStandny);
                                $('#jobStandby').text(datas.jobStandby);
                                $('#staffStandby').text(datas.staffStandby);
                                $('#dealOrder').text(datas.dealOrder);
                                $('#dispConfirm').text(datas.dispConfirm);
                                $('#ccOrder').text(datas.ccOrder);
                                $('#applyOrder').text(datas.applyOrder);
                                break;
                        }
                    } else {
                        fish.toast("error", "获取数据失败");
                    }
                })
            },
            standbyOrderInfo: function () {
                orderStandbyAction.qryCstOrdList(this.queryObj, function (data) {
                    if (data.messages = "success") {
                        switch (this.queryObj.queryTypeLocal) {
                            case 'deptStandny':
                                $('#deptStandny').text(data.data.length);
                                break;
                            case 'jobStandby':
                                $('#jobStandby').text(data.data.length);
                                break;
                            case 'staffStandby':
                                $('#staffStandby').text(data.data.length);
                                break;
                            case 'dealOrder':
                                $('#dealOrder').text(data.data.length);
                                break;
                            case 'dispConfirm': //完成确认单
                                $('#dispConfirm').text(data.data.length);
                                break;
                            case 'ccOrder':
                                $('#ccOrder').text(data.data.length);
                                break;
                            case 'applyOrder':
                                $('#applyOrder').text(data.data.length);
                                break;
                        }
                    } else {
                        fish.toast("error", "获取数据失败");
                    }
                    $("#orderDeal-grid").grid("reloadData", data.data);
                })
            }

        }); //fish.View.extend END
    }); //ALL END