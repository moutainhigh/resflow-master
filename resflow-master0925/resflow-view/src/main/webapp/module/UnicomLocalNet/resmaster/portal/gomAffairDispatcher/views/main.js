define(["text!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/templates/main.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/action/affairDispatcherAction',
        "css!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/styles/affairDispatcherAction.css"],
    function (template, portalViewi18n, affairDispatcherAction, css) {
        var path;
        var queryObj = new Object();
        var fileList = null,
            maxFileSize = 1000000,                //文件大小限制
            maxFileCount = 20;                    //文件个数限制
        var localStandyNum = 10;                  //页面展示条数
        var localStandyPage = 1;                  //页数

        return ngc.View.extend({
            initTab:this.dialogArguments._src ? this.dialogArguments._src : "",
            template: ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #queryBtn': 'queryOrder',              // 查询
                'click #tabs-a-a-link': 'aaClick',
                'click #tabs-a-b-link': 'abClick',
                'click #tabs-a-c-link': 'acClick',
                'click #tabs-a-h-link': 'ahClick', //事务审查
                'click #tabs-a-d-link': 'adClick',
                'click #tabs-a-e-link': 'aeClick',
                'click #tabs-a-f-link': 'afClick',
                'click #tabs-a-g-link': 'agClick',
                'click #fqAffairBtn': 'fqAffairFun',
                'click #initiateBtn': 'initiateFun',
                'click #shAffairBtn': 'shAffairFun',
                'click #clAffairBtn': 'clAffairFun',
                'click #scAffairBtn': 'scAffairFun', //事务审查
                'click #qrAffairBtn': 'qrAffairFun',
                'click #exportExcelBtn': 'exportExcel',
                'click #resetSelectBtn': 'resetSelect',
                'click #anewInitAffairBtn': 'anewInitAffair',//重新发起事务
                'click #rejectNum1': 'qryShRejectOrder',
                'click #rejectNum2': 'qryClRejectOrder',
                'click #closeAffairBtn': 'closeAffairBtnFun',//关闭事务
                'click #delAffairBtn': 'closeAffairBtnFun',//删除草稿
            },
            initialize: function () {
                this.render();
            },
            // 渲染页面
            render: function () {
                this.$el.html(this.template(this.i18nData));
            },
            // 初始化fish组件
            afterRender: function () {
                path = this.getRootPath();
                // 初始化日期空间
                $('#beginDate,#endDate').datetimepicker({
                    buttonIcon: '',
                    viewType: "date",
                    todayBtn: true
                });
                $('#orderState').combobox({
                    placeholder: '--请选择事务状态--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '草稿箱', value: '290000112'},
                        {name: '发起事务', value: '290000113'},
                        {name: '事务审核驳回', value: '290000114'},
                        {name: '事务审核', value: '290000115'},
                        {name: '事务处理中', value: '290000116'},
                        {name: '事务处理驳回', value: '290000117'},
                        {name: '事务已处理', value: '290000118'},
                        {name: '事务确认', value: '290000119'},
                        {name: '已完成', value: '290000120'},
                        {name: '已关闭', value: '290000121'}
                    ]
                });

                //初始化事务单类型
                var dataArray = new Array;
                affairDispatcherAction.qryAffairDispatchOrderType(function(data) {
                    dataArray = data;
                    var $comboboxType = $('#orderType').combobox({
                        placeholder: '--请选择事务状态--',
                        dataTextField: 'name',
                        dataValueField: 'value',
                        dataSource: dataArray
                    });
                });

                // 初始化tab
                $("#orderDeal-tab").tabs();
                // 初始化表格
                this.initAffairOrderGrid();
                if (this.initTab != "" && this.initTab != null) {
                    $('#' + this.initTab).click();
                }else {
                    // 默认选中发起事务
                    $('#tabs-a-a-link').click();
                }

                $('.rowtext .ui-pagination').change(function () {
                        var p1 = $(this).children('option:selected').val();//这就是selected的值
                        localStandyNum = p1;
                        localStandyPage = 1;
                        this.queryWorkOrdersGroup();
                    }
                );

                // 增加input清除功能
                $("#affairDispatcher-Qryform input").clearinput();
                // 调整窗口大小
                $(window).trigger("resize");

            },
            // 获取根目录
            getRootPath: function () {
                // 获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
                var curWwwPath = window.document.location.href;
                // 获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
                var pathName = window.document.location.pathname;
                var pos = curWwwPath.indexOf(pathName);
                // 获取主机地址，如： http://localhost:8083
                var localhostPaht = curWwwPath.substring(0, pos);
                // 获取带"/"的项目名，如：/uimcardprj
                var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
                return (localhostPaht + projectName);
            },
            // 初始化事务单表格
            initAffairOrderGrid: function () {
                var me = this;
                this.girdMdel = [
                    //默认展示字段
                    {name: 'WO_COMPLETE_STATE', label: '单据状态', width: 80, hidden: true},
                    {name: 'AFFAIR_DISPATCH_ORDER_ID', label: '事务单ID', width: 80, hidden: true},
                    {name: 'ORDER_ID', label: '定单ID', width: 80, hidden: true},
                    {name: 'AFFAIR_DISPATCH_ORDER_CODE', label: '事务调单编码', width: 160, hidden: false},
                    {name: 'TITLE', label: '事务调单标题', width: 180, hidden: false},
                    {name: 'CREATE_STAFF_NAME', label: '创建人', width: 100, hidden: false},
                    {name: 'CREATE_DATE', label: '创建时间', width: 140, hidden: false},
                    {name: 'DISPOSE_STAFF_ARR', label: '调单处理人', width: 120, hidden: false, formatter: me.formatStaff},
                    {name: 'CREATE_STAFF_ID', label: '调单创建人ID', width: 80, hidden: true},
                    {name: 'IS_CHECK', label: '是否审核', width: 100, hidden: false, formatter: me.formatIsCheck},
                    {name: 'CHECK_STAFF_NAME', label: '审核人', width: 100, hidden: false},
                    {name: 'TYPE', label: '事务单类型', width: 120, hidden: false, formatter: me.formatType},
                    {name: 'STATE', label: '事务单状态', width: 120, hidden: false, formatter: me.formatState},
                    {name: 'CHECK_STAFF', label: '审核人ID', width: 80, hidden: true},
                    {name: 'FILE_INFO', label: '附件', width: 80, hidden: true},
                    {name: 'TACHE_NAME', label: '流程环节', width: 100, hidden: false, formatter: me.formatTacheName},
                    {name: 'WO_STATE', label: '工单状态', width: 100, hidden: false, formatter: me.formatWoState},
                    {name: 'WO_CODE', label: '工单编码', width: 80, hidden: true},
                    {name: 'WO_ID', label: '工单ID', width: 80, hidden: true},
                    {name: 'PRIV_FORWARD_WO_ID', label: '驳回工单ID', width: 80, hidden: true},
                    {name: 'CONTENT', label: '内容', width: 80, hidden: true},
                    {name: 'REQ_FIN_DATE', label: '环节要求处理时间', width: 160, hidden: false}
                ];

                var queryWorkOrdersGroup = $.proxy(this.queryWorkOrdersGroup, this); //函数作用域改变
                $("#orderDeal-grid").grid({
                    datatype: "json",
                    colModel: this.girdMdel,
                    autowidth: true,
                    curPageSort: true,
                    rowNum: 10,
                    rowList: [10, 15, 20, 50, 100, 200, 500],
                    pager: true,
                    recordtext: "{0}-{1} 共{2}条",
                    pgtext: " 第{0}页/共{1}页",
                    rowtext: "每页{0}条",
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    multiselect: true,
                    shrinkToFit: false,
                    autoResizable: true,
                    showColumnsFeature: true, //允许用户自定义列展示设置
                    cached: true, //把用户自定义的列展示设置缓存在本地
                    pageData: queryWorkOrdersGroup,
                    afterInsertRow: function (e, rowid, pageData) {
                        switch (pageData.woCompleteState) {
                            case '正常单':
                                $("#orderDeal-grid").grid('setCell', rowid, 'woCompleteState', '', {color: '#6DCC4A'});
                                break;
                            case '预警单':
                                $("#orderDeal-grid").grid('setCell', rowid, 'woCompleteState', '', {color: '#F4C70D'});
                                break;
                            case '超时单':
                                $("#orderDeal-grid").grid('setCell', rowid, 'woCompleteState', '', {color: '#FF5858'});
                                break;
                        }
                    },
                    onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                        var dataParent = $("#orderDeal-grid").grid('getRowData', rowid);
                        me.orderDetailViewGroup(dataParent);
                    },
                    gridComplete: function () {
                        $('.gotext').html("<span>跳转至<input class=\"ui-pagination-input\"></span>");
                    }
                });
                this.resize();

            },
            // 工单状态解析
            formatWoState: function (cellval, opts, rwdat, _act) {
                switch (cellval) {
                    case '290000003':
                        return '被签出';
                    case '290000005':
                        return '已作废';
                    case '290000004':
                        return '已完成';
                    case '290000002':
                        if (rwdat.PRIV_FORWARD_WO_ID) {
                            return '<span style="color: red">驳回重新执行中</span>';
                        } else {
                            return '执行中';
                        }
                    case '290000009':
                        return '待解挂';
                    case '290000006':
                        return '主动驳回';
                    case '290000007':
                        return '被动驳回';
                    case '290000001':
                        return '未派发';
                    case '290000008':
                        return '挂起';
                    case '290000110':
                        return '已启子流程';
                    case '290000111':
                        return '等一干通知';
                    default:
                        return cellval;
                }
            },
            //人员转换
            formatIsCheck: function (value) {
                switch (value) {
                    case '0':
                        return '是';
                    case '1':
                        return '否';
                    default:
                        return value;
                }
            },
            //人员转换
            formatStaff: function (value) {
                var jsObj = JSON.parse(value);
                var staffs = "";
                for (var i = 0; i < jsObj.length; i++) {
                    // staffs += jsObj[i].section + "-" + jsObj[i].name + ";";
                    staffs += jsObj[i].name + ",";
                }
                return staffs.substring(0, staffs.length - 1);
            },
            //类型转换
            formatType: function (value) {
                switch (value) {
                    case affairDispatcherAction.AFFAIR_ORDER_TYPE.GENERAL:
                        return '通用';
                    case affairDispatcherAction.AFFAIR_ORDER_TYPE.EXCHANGE:
                        return '交换';
                    case affairDispatcherAction.AFFAIR_ORDER_TYPE.DATA:
                        return '数据';
                    case affairDispatcherAction.AFFAIR_ORDER_TYPE.TRANSMISSION:
                        return '传输';
                    case affairDispatcherAction.AFFAIR_ORDER_TYPE.POWER:
                        return '动力';
                    default:
                        return value;
                }
            },
            //状态转换
            formatState: function (value) {
                switch (value) {
                    case affairDispatcherAction.AFFAIR_ORDER_STATE.CGX:
                        return '草稿箱';
                    case affairDispatcherAction.AFFAIR_ORDER_STATE.FQSW:
                        return '发起事务';
                    case affairDispatcherAction.AFFAIR_ORDER_STATE.SHBH:
                        return '事务审核驳回';
                    case affairDispatcherAction.AFFAIR_ORDER_STATE.SWSH:
                        return '事务审核';
                    case affairDispatcherAction.AFFAIR_ORDER_STATE.SWCLZ:
                        return '事务处理中';
                    case affairDispatcherAction.AFFAIR_ORDER_STATE.SWCLBH:
                        return '事务处理驳回';
                    case affairDispatcherAction.AFFAIR_ORDER_STATE.SWYCL:
                        return '事务已处理';
                    case affairDispatcherAction.AFFAIR_ORDER_STATE.SWQR:
                        return '事务确认';
                    case affairDispatcherAction.AFFAIR_ORDER_STATE.YWC:
                        return '已完成';
                    case affairDispatcherAction.AFFAIR_ORDER_STATE.YGB:
                        return '已关闭';
                    default:
                        return value;
                }
            },
            //环节名转换
            formatTacheName: function (cellval, opts, rwdat, _act) {
                if (rwdat.STATE == affairDispatcherAction.AFFAIR_ORDER_STATE.YWC && cellval == "事务确认" && queryObj.queryType != 'lsOrder') {
                    return '结束';
                }
                return cellval;
            },
            queryWorkOrdersGroup: function (page, rowNum, isReject) {
                var $form = $('#affairDispatcher-Qryform').form();
                $form.form('option', 'skipNullField', false);
                var obj = $form.form('value');
                $form.form('option', 'skipNullField', true);
                if (obj.beginDate && obj.endDate && obj.beginDate > obj.endDate) {
                    fish.warn("开始时间必须小于结束时间！");
                    return;
                }
                if (isReject) {
                    obj.isReject = "true";
                } else {
                    obj.isReject = "false";
                }
                page = (page != '' && page != undefined) ? page : localStandyPage;
                rowNum = (rowNum != '' && rowNum != undefined) ? rowNum : localStandyNum;
                fish.store.set('orderDeal-grid-rowNum', rowNum); //记录用户选择的每页记录数
                queryObj.parameters = obj;
                queryObj.pageIndex = page + '';
                queryObj.pageSize = rowNum + '';
                //$("#orderDeal-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                this.affairOrderCount();
                this.affairOrderList();
                //$("#orderDeal-grid").unblockUI().data('blockui-content', false);
            },
            affairOrderCount: function () {
                affairDispatcherAction.countVariousAffairOrder(queryObj, function (data) {
                    if (data.code == 'SUCCESS') {
                        var obj = data.counts;
                        $("#fqAmount").html(obj.fqOrder);
                        $("#cgAmount").html(obj.cgOrder);
                        $("#shAmount").html(obj.shOrder);
                        $("#clAmount").html(obj.clOrder);
                        $("#scAmount").html(obj.scOrder);
                        $("#qrAmount").html(obj.qrOrder);
                        $("#lsAmount").html(obj.lsOrder);
                        $("#tzAmount").html(obj.tzOrder);
                    }
                });
            },
            affairOrderList: function () {
                $("#orderDeal-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                affairDispatcherAction.queryAffairOrderList(queryObj, function (data) {
                    $("#orderDeal-grid").unblockUI().data('blockui-content', false);
                    var gridData = {
                        "rows": data.data,
                        "page": data.pageIndex,
                        "records": data.dataLength,
                        "rowNum": data.rowNum,
                        "total": data.total
                    };
                    $("#orderDeal-grid").grid("reloadData", gridData);
                });
            },
            // 条件查询
            queryOrder: function () {
                this.queryWorkOrdersGroup();
            },
            // 查询事务审核驳回单子
            qryShRejectOrder: function () {
                $('#orderState').combobox('value', '290000114');
                this.queryWorkOrdersGroup(localStandyPage, localStandyNum, true);
            },
            // 查询事务处理被驳回单子
            qryClRejectOrder: function () {
                this.queryWorkOrdersGroup(localStandyPage, localStandyNum, true);
            },
            hideAllBtn: function () {
                $("#fqAffairBtn").hide();
                $("#anewInitAffairBtn").hide();
                $("#shAffairBtn").hide();
                $("#initiateBtn").hide();
                $("#clAffairBtn").hide();
                $("#scAffairBtn").hide();
                $("#qrAffairBtn").hide();
                $("#rejectNumDiv1").hide();
                $("#rejectNumDiv2").hide();
                $("#closeAffairBtn").hide();
                $("#delAffairBtn").hide();
            }
            ,
            // 初始化发起事务tab
            aaClick: function () {
                $('#orderState').combobox({
                    placeholder: '--请选择事务状态--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '发起事务', value: '290000113'},
                        {name: '事务审核驳回', value: '290000114'},
                        {name: '事务审核', value: '290000115'},
                        {name: '事务处理中', value: '290000116'},
                        {name: '事务处理驳回', value: '290000117'},
                        {name: '事务确认', value: '290000119'},
                        {name: '已完成', value: '290000120'},
                        {name: '已关闭', value: '290000121'}
                    ]
                });

                queryObj.queryType = 'fqOrder';
                this.hideAllBtn();
                $("#anewInitAffairBtn").show();
                $("#fqAffairBtn").show();
                $("#rejectNumDiv1").show();
                $("#closeAffairBtn").show();
                this.queryWorkOrdersGroup();
                this.queryShRejectNum();
            },

            // 初始化事务审核tab
            abClick: function () {
                $('#orderState').combobox({
                    placeholder: '--请选择事务状态--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '事务审核', value: '290000115', disabled: true}
                    ]
                });
                $('#orderState').combobox('value', '290000115');
                queryObj.queryType = 'shOrder';
                this.hideAllBtn();
                $("#shAffairBtn").show();
                this.queryWorkOrdersGroup();
            },
            // 初始化事务处理tab
            acClick: function () {
                $('#orderState').combobox({
                    placeholder: '--请选择事务状态--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '事务处理中', value: '290000116', disabled: true},
                        {name: '事务确认', value: '290000119'},
                    ]
                });
                queryObj.queryType = 'clOrder';
                this.hideAllBtn();
                $("#clAffairBtn").show();
                $("#rejectNumDiv2").show();
                this.queryWorkOrdersGroup();
                this.queryClRejectNum();
            },
            // 初始化事务审查tab
            ahClick: function () {
                $('#orderState').combobox({
                    placeholder: '--请选择事务状态--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '事务审查中', value: '', disabled: true}
                    ]
                });
                queryObj.queryType = 'scOrder';
                this.hideAllBtn();
                $("#scAffairBtn").show();
                this.queryWorkOrdersGroup();
            },
            // 初始化事务确认tab
            adClick: function () {
                $('#orderState').combobox({
                    placeholder: '--请选择事务状态--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '事务确认', value: '290000119', disabled: true}
                    ]
                });
                $('#orderState').combobox('value', '290000119');
                queryObj.queryType = 'qrOrder';
                this.hideAllBtn();
                $("#qrAffairBtn").show();
                this.queryWorkOrdersGroup();
            },
            // 初始化抄送通知
            aeClick: function () {
                $('#orderState').combobox({
                    placeholder: '--请选择事务状态--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '发起事务', value: '290000113'},
                        {name: '事务审核驳回', value: '290000114'},
                        {name: '事务审核', value: '290000115'},
                        {name: '事务处理中', value: '290000116'},
                        {name: '事务处理驳回', value: '290000117'},
                        {name: '事务确认', value: '290000119'},
                        {name: '已完成', value: '290000120'},
                        {name: '已关闭', value: '290000121'}
                    ]
                });

                queryObj.queryType = 'tzOrder';
                this.hideAllBtn();
                this.queryWorkOrdersGroup();
            },
            // 历史事务
            afClick: function () {
                $('#orderState').combobox({
                    placeholder: '--请选择事务状态--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '事务审核驳回', value: '290000114'},
                        {name: '事务审核', value: '290000115'},
                        {name: '事务处理中', value: '290000116'},
                        {name: '事务处理驳回', value: '290000117'},
                        {name: '事务确认', value: '290000119'},
                        {name: '已完成', value: '290000120'},
                        {name: '已关闭', value: '290000121'}
                    ]
                });

                queryObj.queryType = 'lsOrder';
                this.hideAllBtn();
                this.queryWorkOrdersGroup();
            },
            // 草稿箱
            agClick: function () {
                $('#orderState').combobox({
                    placeholder: '--请选择事务状态--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '草稿箱', value: '290000112', disabled: true}
                    ]
                });
                $('#orderState').combobox('value', '290000112');

                queryObj.queryType = 'cgOrder';
                this.hideAllBtn();
                $("#initiateBtn").show();
                $("#delAffairBtn").show();
                this.queryWorkOrdersGroup();
            },
            getOrderDealGridGrid: function () {
                return this.getView("#orderDeal-grid");
            },
            // 事务调单详情
            orderDetailViewGroup: function (data) {
                var me = this;
                var pop = fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/views/qrAffair',
                    width: "850px",
                    height: "96%",
                    title: "completionNotRented",
                    viewOption: {
                        type: "xqAffair",
                        affairOrderState: affairDispatcherAction.AFFAIR_ORDER_STATE.SWCLZ,
                        affairOrder: data
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (e) {
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                });
            },
            //重新发起事务
            anewInitAffair: function () {
                var rows = $("#orderDeal-grid").grid("getCheckRows");
                console.log("rows", rows);
                if (rows.length == 0) {
                    ngc.warn("请选择一条事务调单记录！");
                    return;
                }
                var rowData = rows[0];
                console.log("rowData", rowData);
                if (affairDispatcherAction.AFFAIR_ORDER_STATE.SHBH != rowData.STATE) {
                    ngc.warn("请选择事务状态为【事务审核驳回】的事务单进行重新发起！");
                    return;
                }
                console.log("rowData", rowData);
                this.fqAffairFun(rowData);
            },

            // 发起事务
            fqAffairFun: function (data) {
                //存储信息
                var affairInfo = {};
                var affairOrderState = "";
                var me = this;
                if (typeof (data) != undefined && affairDispatcherAction.AFFAIR_ORDER_STATE.SHBH == data.STATE) {
                    //审核驳回重新发起
                    affairInfo = data;
                    affairInfo.fqtitleName = "重新发起事务";
                    affairInfo.btnName = "重新发起";
                    affairOrderState = affairDispatcherAction.AFFAIR_ORDER_STATE.SHBH;
                } else if (typeof (data) != undefined && affairDispatcherAction.AFFAIR_ORDER_STATE.CGX == data.STATE) {
                    //草稿箱
                    affairInfo = data;
                    affairInfo.fqtitleName = "编辑事务";
                    affairInfo.btnName = "发起事务";
                    affairOrderState = affairDispatcherAction.AFFAIR_ORDER_STATE.CGX;
                } else {
                    //直接发起
                    affairInfo.fqtitleName = "发起事务";
                    affairInfo.btnName = "发起事务";
                    affairOrderState = affairDispatcherAction.AFFAIR_ORDER_STATE.FQSW;
                }

                var pop = fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/views/fqAffair',
                    width: "800px",
                    height: "96%",
                    title: "发起事务",
                    viewOption: {
                        affairInfo: affairInfo,
                        affairOrderState: affairOrderState
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (e) {
                            me.queryWorkOrdersGroup();
                            me.queryShRejectNum();
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                })
            },

            // 草稿箱发起事务
            initiateFun: function () {
                // 草稿箱编辑事务，复用发起页面
                var rows = $("#orderDeal-grid").grid("getCheckRows");
                if (rows.length == 0) {
                    ngc.warn("请选择一条事务调单记录！");
                    return;
                }
                var rowData = rows[0];
                if (affairDispatcherAction.AFFAIR_ORDER_STATE.CGX != rowData.STATE) {
                    ngc.warn("请选择事务状态为【草稿箱】的事务单进行编辑！");
                    return;
                }
                this.fqAffairFun(rowData);
            },
            // 审核事务
            shAffairFun: function () {
                var me = this;
                var checkRows = $("#orderDeal-grid").grid('getCheckRows');
                if (checkRows.length == 0) {
                    ngc.warn("请选择一条事务调单审核工单进行审核！");
                    return;
                }
                var pop = fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/views/shAffair',
                    width: "800px",
                    height: "96%",
                    title: "审核事务",
                    viewOption: {
                        affairOrderState: affairDispatcherAction.AFFAIR_ORDER_STATE.SWSH,
                        affairOrder: checkRows[0]
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (e) {
                            me.queryWorkOrdersGroup();
                            me.queryShRejectNum();
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                });

            },
            // 处理事务
            clAffairFun: function () {
                var me = this;
                var checkRows = $("#orderDeal-grid").grid('getCheckRows');
                if (checkRows.length == 0) {
                    ngc.warn("请选择一条事务调单处理工单进行处理！");
                    return;
                }
                var pop = fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/views/clAffair',
                    width: "850px",
                    height: "96%",
                    title: "事务处理",
                    viewOption: {
                        type: "clAffair",
                        affairOrderState: affairDispatcherAction.AFFAIR_ORDER_STATE.SWCLZ,
                        affairOrder: checkRows[0]
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (e) {
                            me.queryWorkOrdersGroup();
                            me.queryShRejectNum();
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                });
            },
            // 审查事务
            scAffairFun: function () {
                var me = this;
                var checkRows = $("#orderDeal-grid").grid('getCheckRows');
                if (checkRows.length == 0) {
                    ngc.warn("请选择一条事务调单审核工单进行审查！");
                    return;
                }
                var pop = fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/views/scAffair',
                    width: "800px",
                    height: "96%",
                    title: "审查事务",
                    viewOption: {
                        affairOrderState: affairDispatcherAction.AFFAIR_ORDER_STATE.SWSC,
                        affairOrder: checkRows[0]
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (e) {
                            me.queryWorkOrdersGroup();
                            me.queryShRejectNum();
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                });

            },
            // 确认事务
            qrAffairFun: function () {
                var me = this;
                var checkRows = $("#orderDeal-grid").grid('getCheckRows');
                if (checkRows.length == 0) {
                    ngc.warn("请选择一条事务调单确认工单进行处理！");
                    return;
                }

                var pop = fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/views/qrAffair',
                    width: "850px",
                    height: "95%",
                    title: "确认事务",
                    viewOption: {
                        affairOrderState: affairDispatcherAction.AFFAIR_ORDER_STATE.SWQR,
                        affairOrder: checkRows[0]
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (e) {
                            me.queryWorkOrdersGroup();
                            me.queryShRejectNum();
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                })
            },
            // 查询审核驳回待处理条数
            queryShRejectNum: function () {
                affairDispatcherAction.queryShRejectNum(queryObj, function (data) {
                    if (data.code == 'SUCCESS') {
                        $("#rejectNum1").html(data.counts);
                    }
                });
            },
            // 查询审核驳回待处理条数
            queryClRejectNum: function () {
                affairDispatcherAction.queryClRejectNum(queryObj, function (data) {
                    if (data.code == 'SUCCESS') {
                        $("#rejectNum2").html(data.counts);
                    }
                });
            },
            // 导出excel
            exportExcel: function () {
                affairDispatcherAction.exportAffairOrderData(path + '/localScheduleLT/affairOrderController/exportAffairOrderData.spr', queryObj);
            },
            // 取消选择
            resetSelect: function () {
                $("#orderDeal-grid").grid("setAllCheckRows", false);
            },
            // 浏览器窗口大小改变事件
            resize: function () {
                $("#orderDeal-grid").grid("resize", true);
                var frameHeight = document.documentElement.scrollHeight;
                $("#orderDeal-grid").grid("setGridHeight", frameHeight - 200);
            },
            // 关闭事务
            closeAffairBtnFun: function () {
                var me = this;
                var checkRows = $("#orderDeal-grid").grid('getCheckRows');
                if (checkRows.length == 0) {
                    ngc.warn("请选择一条事务调单确认工单进行处理！");
                    return;
                } else {
                    var row = checkRows[0];
                    if (!(row.STATE == affairDispatcherAction.AFFAIR_ORDER_STATE.SHBH || row.STATE == affairDispatcherAction.AFFAIR_ORDER_STATE.CGX)) {
                        ngc.warn("只能关闭草稿箱事务调单和被审核驳回的事务调单！");
                        return;
                    }
                }
                var row = checkRows[0];
                var state = row.STATE;
                console.log("closeRow", row);
                if (state === affairDispatcherAction.AFFAIR_ORDER_STATE.YWC) {
                    ngc.warn("无法关闭已完成的事务调单！");
                    return;
                }
                if (state === affairDispatcherAction.AFFAIR_ORDER_STATE.YGB) {
                    ngc.warn("该事务调单已关闭！");
                    return;
                }
                ngc.confirm('确认是否关闭该事务调单').result.then(function () {
                    var parameters = {
                        affairId: row.AFFAIR_DISPATCH_ORDER_ID,
                        orderId: row.ORDER_ID,
                        workOrderId: row.WO_ID
                    };
                    $("#orderDeal-grid").blockUI({message: '提交中...'}).data('blockui-content', true);
                    affairDispatcherAction.closeAffair(parameters, function (data) {
                        $("#orderDeal-grid").unblockUI().data('blockui-content', false);
                        me.queryWorkOrdersGroup();
                        me.queryShRejectNum();
                        if (data) {
                            if (data.code == 'SUCCESS') {
                                ngc.info(data.message);
                            } else {
                                ngc.error(data.message);
                            }
                        } else {
                            ngc.error("提交失败！");
                        }
                    });
                });


            }
        }); //fish.View.extend END
    }); //ALL END