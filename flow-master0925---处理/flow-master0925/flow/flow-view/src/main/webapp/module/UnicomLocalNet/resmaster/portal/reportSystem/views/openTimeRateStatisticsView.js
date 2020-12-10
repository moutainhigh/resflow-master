/**
 * 开通及时率
 */
define(["text!module/UnicomLocalNet/resmaster/portal/reportSystem/templates/openTimeRateStatisticsView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/reportSystem/action/reportSystemAction.js',
        "css!module/UnicomLocalNet/resmaster/portal/reportSystem/styles/dispatchOrderStatisticsView.css"],
    function (template, portalViewi18n, reportAction, css) {
        return ngc.View.extend({
            template: ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #openTimeRate-queryBtn': 'query',           // 查询
                'click #openTimeRate-exportBtn': 'exportExcel',              // 导出
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
               /* $('#openTimeRate-tacheName,#openTimeRate-orgName').datetimepicker({
                    buttonIcon: '',
                    viewType: "date",
                    todayBtn: true
                });*/
                $('#openTimeRate-orgName').clearinput();
                $('#openTimeRate-tacheName').clearinput();
                //初始化表格
                this.initGrid();
                //手动查询一次
                // this.query();
                // 增加input清除功能
                $("#openTimeRate-form input").clearinput();
                // 调整窗口大小
                $(window).trigger("resize");

            },
            //初始化表格
            initGrid: function () {
                var me = this;
                $("#openTimeRate-grid").grid({
                    colModel: [
                        {name: 'ORG_NAME', label: '部门', width: 130, sortable: false},
                        {name: 'TACHE_NAME', label: '环节名称', width: 130, sortable: false},
                        {name: 'COUNTNUM', label: '电路数量', width: 100, sortable: false},
                        {name: 'FIN_COUNT', label: '已完成数量', width: 130, sortable: false},
                        {name: 'FIN_NOR', label: '按时完成数量', width: 150, sortable: false},
                        {name: 'FIN_OVER', label: '超时完成数量', width: 150, sortable: false},
                        {name: 'TIMELINESS', label: '业务开通及时率', width: 150, sortable: false},
                        {name: 'AVG_HOUR', label: '完成平均周期（小时）', width: 200, sortable: false},
                        {name: 'UNFIN_COUNT', label: '未完成数量', width: 150, sortable: false},
                        {name: 'UNFIN_NOR', label: '未完成正常数量', width: 150, sortable: false},
                        {name: 'UNFIN_OVER', label: '未完成超时数量', width: 150, sortable: false},
                        {name: 'START_TYPE', label: '跨域范围', width: 100, sortable: false}
                    ],
                    autoResizable: true,
                    height: 500,
                    autowidth: false,
                    gridview:false,
                    shrinkToFit: false,
                    showColumnsFeature: true,
                    afterInsertRow: function (e, rowid, pageData) {
                        if(pageData.FIN_COUNT==0){
                            $("#openTimeRate-grid").grid('setCell', rowid, 'TIMELINESS', '0%');
                            $("#openTimeRate-grid").grid('setCell', rowid, 'AVG_HOUR', '0');
                        }else {
                            //开通及时率
                            var n=(pageData.FIN_NOR/pageData.FIN_COUNT)*100;
                            if(String(n).indexOf(".") + 1) {
                                n=n.toFixed(2);
                            }
                            var s=pageData.DIS_HOUR/pageData.FIN_COUNT ;
                            if(String(s).indexOf(".") + 1) {
                                s=s.toFixed(2);
                            }

                            //var n=Math.ceil((pageData.FIN_NOR/pageData.FIN_COUNT)*100);
                            $("#openTimeRate-grid").grid('setCell', rowid, 'TIMELINESS', n+'%');
                            //平均时间
                            //$("#openTimeRate-grid").grid('setCell', rowid, 'AVG_HOUR', Math.ceil(pageData.DIS_HOUR/pageData.FIN_COUNT));
                            $("#openTimeRate-grid").grid('setCell', rowid, 'AVG_HOUR',s);
                        }

                    }
                    /*onCellSelect: function (e, rowid, iCol, val) {
                        //var data = $("#openTimeRate-grid").grid("getRowData",rowid);
                        //当iCol为0时，选中的是名称不是行数据，则不触发数据回显事件
                        if (0 != iCol) {
                            if ("0" === val) {
                                ngc.info("无调单记录");
                                return;
                            }
                            me.getColInfo(rowid, iCol);
                        }
                    },*/
                });
                this.resize();
            },
            resize: function () {
                $("#openTimeRate-grid").grid("resize", true);
                var frameHeight = document.documentElement.scrollHeight;
                $("#openTimeRate-grid").grid("setGridHeight", frameHeight - 130);
            },



            //查询
            query: function () {
                var parames = {
                    tacheName: $("#openTimeRate-tacheName").val(),
                    orgName: $("#openTimeRate-orgName").val()
                };
               
                $("#openTimeRate-grid").blockUI({message: '加载中...'}).data('blockui-content', true);
                reportAction.openTimeRateStatistics(parames, function (data) {
                    //console.log("data",data);
                    $("#openTimeRate-grid").unblockUI().data('blockui-content', false);
                    if (!data || !data.length > 0) {
                        ngc.info("未查询到数据");
                    }
                    $("#openTimeRate-grid").grid("reloadData", data);
                })
            },
            // 导出excel
            exportExcel: function () {
                var me = this;
                var data = new Object();
                data.tacheName = $("#openTimeRate-tacheName").val() || '';
                data.orgName = $("#openTimeRate-orgName").val() || '';

                reportAction.export(me.getRootPath() + '/localScheduleLT/ReportController/openTimeRateData.spr', data);
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