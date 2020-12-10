define(["text!module/UnicomLocalNet/resmaster/portal/reportSystem/templates/dispatchOrderStatisticsView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/reportSystem/action/reportSystemAction.js',
        "css!module/UnicomLocalNet/resmaster/portal/reportSystem/styles/dispatchOrderStatisticsView.css"],
    function (template, portalViewi18n, reportAction, css) {
        return ngc.View.extend({
            template: ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #disOrder-queryBtn': 'query',           // 查询
                'click #disOrder-exportBtn': 'exportExcel',              // 导出
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
                $('#disOrder-beginDate,#disOrder-endDate').datetimepicker({
                    orientation:{y:'bottom'},
                    buttonIcon: '',
                    viewType: "date",
                    todayBtn: true
                });
                //初始化表格
                this.initGrid();
                //手动查询一次
                // this.query();
                // 增加input清除功能
                $("#disOrder-form input").clearinput();
                // 调整窗口大小
                $(window).trigger("resize");

            },
            //初始化表格
            initGrid: function () {
                var me = this;
                $("#disOrderDeal-grid").grid({
                    colModel: [
                        {name: 'TYPE', label: '产品类型', width: 80, sortable: false, key: true},
                        {name: 'ADD', label: '新开', width: 50, sortable: false},
                        {name: 'DEL', label: '拆机', width: 50, sortable: false},
                        {name: 'CHA', label: '变更', width: 50, sortable: false},
                        {name: 'STOP', label: '停机', width: 50, sortable: false},
                        {name: 'REP', label: '复机', width: 50, sortable: false},
                        {name: 'MOVE', label: '移机', width: 50, sortable: false},
                        {name: 'COUNT', label: '总计', width: 50, sortable: false}
                    ],
                    autoResizable: true,
                    height: 500,
                    onCellSelect: function (e, rowid, iCol, val) {
                        //var data = $("#disOrderDeal-grid").grid("getRowData",rowid);
                        //当iCol为0时，选中的是名称不是行数据，则不触发数据回显事件
                        if (0 != iCol) {
                            if ("0" === val) {
                                ngc.info("无调单记录");
                                return;
                            }
                            me.getColInfo(rowid, iCol);
                        }
                    },
                });
                this.resize();
            },
            resize: function () {
                $("#disOrderDeal-grid").grid("resize", true);
                var frameHeight = document.documentElement.scrollHeight;
                $("#disOrderDeal-grid").grid("setGridHeight", frameHeight - 145);
            },
            //得到操作类型编码
            getOperateType: function (colNum) {
                var operateType = '';
                switch (colNum) {
                    case 1    :
                        operateType = '101';
                        break;
                    case 2    :
                        operateType = '102';
                        break;
                    case 3    :
                        operateType = '103';
                        break;
                    case 4    :
                        operateType = '104';
                        break;
                    case 5    :
                        operateType = '105';
                        break;
                    case 6    :
                        operateType = '106';
                        break;
                    default :
                        operateType = '';
                }
                return operateType;
            },

            //得到单元格信息
            getColInfo: function (productName, colNum) {
                var me = this;
                var operateType = me.getOperateType(colNum);
                if ("共计" != productName) {
                    //通过字典名称得到编码
                    var map = {
                        codeContent: productName,
                        codeType: 'product_code'
                    };
                    reportAction.getDicCodeByContent(map, function (data) {
                        me.openDetailWin(data, operateType);
                    });
                } else {
                    me.openDetailWin(null, operateType);
                }
            },


            // 打开详情列表
            openDetailWin: function (productCode, operateType) {
                var pop = fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/reportSystem/views/disOrderDetailView',
                    width: "1100px",
                    height: "550px",
                    title: "电路明细",
                    viewOption: {
                        productCode: productCode,
                        operateType: operateType,
                        beginDate: $("#disOrder-beginDate").val(),
                        endDate: $("#disOrder-endDate").val()
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (e) {
                        }, function (e) {
                            // console.log('关闭了', e);
                        });
                    }
                });

            },

            //查询
            query: function () {

                var parames = {
                    beginDate: $("#disOrder-beginDate").val(),
                    endDate: $("#disOrder-endDate").val()
                };
                if (parames.beginDate && parames.endDate && parames.beginDate > parames.endDate) {
                    fish.warn("开始时间必须小于结束时间！");
                    return;
                }
                $("#disOrderDeal-grid").blockUI({message: '加载中...'}).data('blockui-content', true);
                reportAction.dispatchOrderStatistics(parames, function (res) {
                    if (res.success){
                        $("#disOrderDeal-grid").grid("reloadData", res.data);
                    }
                    $("#disOrderDeal-grid").unblockUI().data('blockui-content', false);
                })
            },
            // 导出excel
            exportExcel: function () {
                var me = this;
                var data = new Object();
                data.beginDate = $("#disOrder-beginDate").val() || '';
                data.endDate = $("#disOrder-endDate").val() || '';
                if (data.beginDate && data.endDate && data.beginDate > data.endDate) {
                    fish.warn("开始时间必须小于结束时间！");
                    return;
                }
                reportAction.export(me.getRootPath() + '/localScheduleLT/ReportController/exportDispatchOrderData.spr', data);
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

        }); //fish.View.extend END
    }); //ALL END