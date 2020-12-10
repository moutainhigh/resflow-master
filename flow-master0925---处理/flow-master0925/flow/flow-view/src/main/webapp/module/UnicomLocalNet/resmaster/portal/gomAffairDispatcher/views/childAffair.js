define(["text!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/templates/childAffair.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/action/affairDispatcherAction',
        "css!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/styles/fqAffair.css"],
    function (template, portalViewi18n, affairDispatcherAction, css) {
        return ngc.View.extend({
            template: ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #child-commit': 'commitFrom',
                'click #child-cancel': 'cancelFrom'
            },
            initialize: function () {
                this.render();
                FILES = null;
            },
            render: function () {           //渲染页面
                this.$el.html(this.template(this.i18nData));
            },
            //初始化fish组件
            afterRender: function () {
                //初始化调单处理进度表格
                this.initChildDisposeAffairGrid();
                this.writeBackChildDisposeInfo();
                if (this.options.isReject == 1) {
                    $("#child-commit").show();
                    $("#child-reject-div").show();
                } else {
                    $("#child-commit").hide();
                    $("#child-reject-div").hide();
                }
            },
            // 初始化事务处理进度表格
            initChildDisposeAffairGrid: function () {
                var me = this;
                $("#childAffairGrid").grid({
                    height: 'auto',
                    multiselect: false,
                    colModel: [
                        {name: 'WO_ID', label: '工单ID', width: 40, sorttype: "int", key: true},
                        {
                            name: 'DEAL_STAFF_NAME', label: '处理人', width: 40, formatter: function (value) {
                                if (value) {
                                    return value;
                                }
                                return me.options.createUser;
                            }
                        },
                        {name: 'ORDER_ID', label: '定单ID', hidden: true},
                        {name: 'DEAL_DATE', label: '处理日期', width: 55},
                        {name: 'TACHE_NAME', label: '环节', width: 40},
                        {name: 'WO_STATE', label: '状态', width: 50, formatter: me.formatWoState},
                        {name: 'REMARK', label: '处理意见', width: 90},
                        {
                            name: 'FILES', label: '处理附件', width: 100, formatter: function (value) {
                                var html = "";
                                //循环回写附件标签
                                $.each(value, function (index, val) {
                                    html += "<a class=\"downloadChildAffairFile\" name=\"checkFile" +
                                        index +
                                        "\" fileName='" + val.FILE_NAME + "' fileId ='" + val.FILE_ID + "' fileType = '" + val.FILE_TYPE + "'>" +
                                        val.FILE_NAME +
                                        "</a><br>";
                                });
                                return html;
                            }
                        }
                    ],
                    autoResizable: true
                });
            },
            // 回写事务处理信息
            writeBackChildDisposeInfo: function () {
                var _this = this;
                affairDispatcherAction.getChildAffairDisposeList(this.options.orderId, function (data) {
                    if (data && data.length > 0) {
                        $("#childAffairGrid").grid("reloadData", data);
                        //下载监听
                        $('.downloadChildAffairFile').off('click').on('click', function (e) {
                            var fileName = $(this).attr("fileName");
                            var fileId = $(this).attr("fileId");
                            var fileType = $(this).attr("fileType");
                            var param = {
                                fileName: fileName,
                                filePath: 'createbuss',
                                fileId: fileId + "." + fileType
                            };
                            affairDispatcherAction.downLoadAttachMent("localScheduleLT/orderDetails/fileDownload.spr", param);
                        });
                    }
                });
            },
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
                            return '<span style="color: red">重新确认中</span>';
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
            // 驳回
            commitFrom: function () {
                var _this = this;
                var rejectRemark = $("#clildAffair-rejectRemark").val();
                if (rejectRemark) {
                    var parameters = new Array();
                    var obj = {};
                    obj.orderId = this.options.orderId;
                    obj.remark = rejectRemark;
                    parameters.push(obj);
                    $("#child-body-div").blockUI({message: '提交中...'}).data('blockui-content', true);
                    affairDispatcherAction.affairProcessRollBackWo(parameters, function (data) {
                        $("#child-body-div").unblockUI().data('blockui-content', false);
                        if (data) {
                            if (data.code == 'SUCCESS') {
                                ngc.info(data.message);
                                _this.cancelFrom();
                            } else {
                                ngc.error(data.message);
                            }
                        } else {
                            ngc.error("提交失败！");
                        }
                    });
                } else {
                    ngc.error("请先填写驳回原因！");
                }
            },
            // 取消
            cancelFrom: function () {
                this.closeView(null);
            },
            // 关闭视图
            closeView: function (data) {
                this.trigger('returnData', data);
                this.popup.close();
            }
        })
    });