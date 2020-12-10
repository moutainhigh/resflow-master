define(["text!module/UnicomLocalNet/resmaster/portal/reportSystem/templates/disOrderDetailView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/reportSystem/action/reportSystemAction.js',
        "css!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/styles/fqAffair.css"],
    function (template, portalViewi18n, reportAction, css) {
        var localStandyNum = 10;                  //页面展示条数
        var localStandyPage = 1;                  //页数
        return ngc.View.extend({
            template: ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #disOrderDetail-cancel': 'closeView',//关闭视图
                'click #disOrderDetail-exportBtn': 'exportExcel',//导出
            },
            initialize: function () {
                this.render();

            },
            render: function () {           //渲染页面
                this.$el.html(this.template(this.i18nData));
            },
            //初始化fish组件
            afterRender: function () {
                //初始化表格
                this.initDisDetailGrid();
                //查询数据
                this.queryDisOrderList();
                // 调整窗口大小
                $(window).trigger("resize");
            },
            initDisDetailGrid: function () {
                var queryDisOrderList = $.proxy(this.queryDisOrderList, this);
                $("#disOrder-grid").grid({
                    datatype: "json",
                    colModel: [
                        //默认展示字段
                        {name: 'SRV_ORD_ID', label: '业务定单信息ID', width: 120, align: 'center',hidden:true },
                        {name: 'SUBSCRIBE_ID', label: '客户订单号', width: 150, align: 'left', sortable: false},
                        {name: 'APPLY_ORD_ID', label: '申请单编号', width: 200, align: 'left' , sortable: false},
                        {name: 'APPLY_ORD_NAME', label: '申请单标题', width: 200, align: 'left' , sortable: false},
                        {name: 'DISPATCH_ORDER_NO', label: '调度单编号', width: 200, align: 'left', sortable: false},
                        {name: 'SERIAL_NUMBER', label: '业务号码', width: 120, align: 'left', sortable: false},
                        {name: 'CIRCUITCODE', label: '电路编号', width: 150, align:'left', sortable: false},
                        {name: 'SERVICETYPE', label: '产品类型', width: 100, align:'left', sortable: false},
                        {name: 'OPERTYPE', label: '动作类型', width: 100, align:'left', sortable: false},
                        {name: 'TACHE_NAME', label: '当前环节', width: 120, align:'left', sortable: false},
                        {name: 'REQ_FIN_DATE', label: '环节要求完成时间', width: 160, align:'left', sortable: false},
                        {name: 'CREATE_DATE', label: '创建时间', width: 160, align:'left', sortable: false},
                        {name: 'USER_REAL_NAME', label: '当前处理人', width: 160, align:'left', sortable: false},
                        {name: 'ORG_NAME', label: '处理人所属分公司', width: 160, align:'left', sortable: false}
                    ],
                    autowidth: true,
                    rowNum: 10,
                    rowList: [10, 15, 20, 50, 100, 200, 500],
                    pager: true,
                    recordtext: "{0}-{1} 共{2}条",
                    pgtext: " 第{0}页/共{1}页",
                    rowtext: "每页{0}条",
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    autoResizable: true,
                    shrinkToFit: false,
                    pageData: queryDisOrderList,
                    height: 350
                });
                this.resize();
            },
            resize: function () {
                $("#disOrder-grid").grid("resize", true);
                var frameHeight = document.documentElement.scrollHeight;
                $("#disOrder-grid").grid("setGridHeight", frameHeight - 225);
            },
            //查询
            queryDisOrderList: function (page, rowNum, isReject) {
                var param={};
                param.operateType = this.options.operateType;
                param.productCode = this.options.productCode;
                param.endDate = this.options.endDate;
                param.beginDate = this.options.beginDate;
                param.pageIndex = $('#disOrder-grid').grid("getGridParam", "page");
                param.pageSize = $('#disOrder-grid').grid("getGridParam", "rowNum");
                $("#disOrder-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                reportAction.queryDisOrderList(param, function (res) {
                    if (res.success){
                        $("#disOrder-grid").grid("reloadData", res.data);
                    }
                    $("#disOrder-grid").unblockUI().data('blockui-content', false);
                });
            },
            // 关闭视图
            closeView: function (data) {
                this.popup.close();
            },
            //导出
            exportExcel: function () {
                var param={};
                param.operateType = this.options.operateType;
                param.productCode = this.options.productCode;
                param.endDate = this.options.endDate;
                param.beginDate = this.options.beginDate;
                param.pageIndex = 1;
                param.pageSize = 1000;
                reportAction.export(this.getRootPath() + '/localScheduleLT/ReportController/exportDisOrderDetailData.spr',  param);
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

        })
    });