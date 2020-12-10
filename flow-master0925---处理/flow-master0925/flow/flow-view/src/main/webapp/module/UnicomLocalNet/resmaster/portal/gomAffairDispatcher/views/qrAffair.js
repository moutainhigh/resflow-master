define(["text!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/templates/qrAffair.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/action/affairDispatcherAction',
        "css!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/styles/fqAffair.css"],
    function (template, portalViewi18n, affairDispatcherAction, css) {

        return ngc.View.extend({
            template: ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #reject': 'reject',
                'click #commit': 'commit',
                'click #cancel': 'cancelFrom',
                'click #tab-li-a': 'resize',
                'click #tab-li-b': 'resize'
            },
            initialize: function () {
                this.render();
                FILES = null;
                fileInfos = [];
            },
            render: function () {           //渲染页面
                this.$el.html(this.template(this.i18nData));
            },
            //初始化fish组件
            afterRender: function () {
                $("#qr-tabs").tabs({
                    canClose: true,
                    paging: true,
                    autoResizable: true
                });
                $('.panel').panel({
                    collapsible: true
                });
                if ("xqAffair" == this.options.type) {
                    $("#qr-modal-title").html("事务详情");
                    $("#reject").hide();
                    $("#commit").hide();
                    $("#qr-panel-div").hide();
                }
                this.fileData = [];
                //初始化下拉框
                this.initComBoBox();
                //初始化调单明细
                this.initAffairOrderInfo(this.options.affairOrder);
                //初始化表格信息
                this.initGrid();
                $("#reject").attr("disabled", "disabled");
                $("#commit").attr("disabled", "disabled");

            },
            //初始化下拉框
            initComBoBox: function () {
                //初始化是事务状态
                this.initStateComBoBox();
                //初始化事务单类型
                this.initTypeComBoBox();
                //初始化是否审核
                this.initIsCheckComBoBox();
            },
            //初始化表格
            initGrid: function () {
                //初始化抄送人表格
                this.initCopyStaffPopeEdit();
                //初始化审核附件表格
                this.initCheckAffairGrid();
                //初始化调单处理人表格
                this.initDisposeStaffGrid();
                //初始化调单处理进度表格
                this.initDisposeAffairGrid();
            },
            //初始化是事务状态
            initStateComBoBox: function () {
                var $combobox1 = $('#affairAffirm-state').combobox({
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
                $combobox1.combobox('value', this.options.affairOrder.STATE);
            },

            //初始化是事务单类型
            initTypeComBoBox: function () {
                var dataArray = new Array;
                var type = this.options.affairOrder.TYPE;
                affairDispatcherAction.qryAffairDispatchOrderType(function(data) {
                    dataArray = data;
                    var $comboboxType = $('#type').combobox({
                        //placeholder: '--请选择事务状态--',
                        dataTextField: 'name',
                        dataValueField: 'value',
                        dataSource: dataArray
                    });
                    $comboboxType.combobox('value', type);
                });
            },

            //初始化是否审核
            initIsCheckComBoBox: function () {
                $('#affairAffirm-isCheck').combobox({
                    placeholder: '--是否审核--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '否', value: '1', disabled: true},
                        {name: '是', value: '0'}
                    ]
                });
            },
            //初始化抄送人信息
            initCopyStaffPopeEdit: function () {
                $("#affairAffirm-copyStaffArrGrid").grid({
                    minHeight: '110',
                    height: 'auto',
                    colModel: [
                        {name: 'name', label: '姓名', width: 50, sortable: false},
                        {name: 'section', label: '部门', width: 100},
                        {name: 'id', label: '用户ID', width: 100, hidden: true},
                    ],
                    autoResizable: false
                });
            },
            // 初始化事务单明细
            initAffairOrderInfo: function (obj) {
                $("#qr-form").form("value", obj);
                $('#affairAffirm-isCheck').combobox('value', obj.IS_CHECK);
                //如果不审核
                if("1"==obj.IS_CHECK){
                    $("#checkStaff-div").hide();
                    $("#checkDetail-div").hide();
                }else{
                    //回写审核信息
                    this.writeBackCheckInfo(obj.AFFAIR_DISPATCH_ORDER_ID + '');
                }
                $("#affairAffirm-disposeStaffGrid").grid("addRowData", JSON.parse(obj.DISPOSE_STAFF_ARR));
                //回写附件信息
                this.writeBackFileInfo(obj.FILE_INFO)
                //回写抄送人信息
                this.writeBackCopyStaffInfo(obj.AFFAIR_DISPATCH_ORDER_ID + '');
                //回写处理进度信息
                this.writeBackDisposeInfo(obj.AFFAIR_DISPATCH_ORDER_ID + '');
            },
            //回写事务调单附件
            writeBackFileInfo: function (fileInfo) {
                if (fileInfo) {
                    var me = this;
                    me.fileData = JSON.parse(fileInfo);
                    //循环回写附件标签
                    $.each(me.fileData, function (index, val) {
                        $("#affairFileInfoDiv").append("<a class=\"downloadFile\" name=\"" +
                            index +
                            "\">" +
                            val.fileName +
                            "</a><br>");
                    });
                    //下载监听
                    $('.downloadFile').off('click').on('click', function (e) {
                        var obj = me.fileData[$(this).attr("name")];
                        var param = {
                            fileName: obj.fileName,
                            filePath: 'createbuss',
                            fileId: obj.fileId + "." + obj.fileType
                        };
                        affairDispatcherAction.downLoadAttachMent("localScheduleLT/orderDetails/fileDownload.spr", param);
                    });
                }
            },
            //回写抄送人信息
            writeBackCopyStaffInfo: function (affairId) {
                affairDispatcherAction.getAffairNoticeStaffArray(affairId, function (data) {
                    if (data && data.length > 0) {
                        $("#affairAffirm-copyStaffArrGrid").grid("reloadData", data);
                    }
                });
            },
            // 初始化事务单处理人
            initDisposeStaffGrid: function () {
                $("#affairAffirm-disposeStaffGrid").grid({
                    minHeight: '110',
                    height: 'auto',
                    colModel: [
                        {name: 'name', label: '姓名', width: 50, sortable: false},
                        {name: 'section', label: '部门', width: 100},
                        {name: 'id', label: '用户ID', width: 100, hidden: true}
                    ],
                    autoResizable: false
                });
            },
            // 初始化事务审核详情
            initCheckAffairGrid: function () {
                var me = this;
                $("#affairAffirm-shAffairGrid").grid({
                    minHeight: '110',
                    height: 'auto',
                    colModel: [
                        {name: 'WO_ID', label: '审核工单', width: 40, sortable: false},
                        {name: 'DEAL_DATE', label: '审核日期', width: 55},
                        {name: 'CHECK_USER_NAME', label: '审核人', width: 50},
                        {name: 'WO_STATE', label: '事务审核', width: 40, formatter: me.formatCheckWoState},
                        {name: 'REMARK', label: '审核意见', width: 90},
                        {
                            name: 'FILES', label: '审核附件', width: 100, formatter: function (value) {
                                var html = "";
                                //循环回写附件标签
                                $.each(value, function (index, val) {
                                    html += "<a class=\"downloadShFile\" name=\"checkFile" +
                                        index +
                                        "\" fileName='" + val.FILE_NAME + "' fileId ='" + val.FILE_ID + "' fileType = '" + val.FILE_TYPE + "'>" +
                                        val.FILE_NAME +
                                        "</a><br>";
                                });
                                return html;
                            }
                        }
                    ],
                    afterInsertRow: function (e, rowid, pageData) {
                        if(pageData.WO_STATE=='290000002' && !!pageData.REMARK) {
                            $("#affairAffirm-shAffairGrid").grid('setCell', rowid, 'WO_STATE', '审核通过');
                        }
                    },
                    autoResizable: true,
                    gridview:false
                });
            },
            // 回写事务审核信息
            writeBackCheckInfo: function (affairId) {
                affairDispatcherAction.getAffairCheckInfoArray(affairId, function (data) {
                    if (data && data.length > 0) {
                        $("#affairAffirm-shAffairGrid").grid("reloadData", data);

                        //下载监听
                        $('.downloadShFile').off('click').on('click', function (e) {
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
            // 获取根路径
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
                return localhostPaht + projectName;
            },
            // 初始化事务处理进度表格
            initDisposeAffairGrid: function () {
                var me = this;
                $("#affairAffirm-clAffairGrid").grid({
                    height: 'auto',
                    multiselect:true,
                    colModel: [
                        {name: 'WO_ID', label: '工单ID', width: 40, sorttype: "int", key: true},
                        {name: 'DEAL_STAFF_NAME', label: '处理人', width: 40},
                        {name: 'ORDER_ID', label: '定单ID', hidden: true},
                        {name: 'DEAL_DATE', label: '处理日期', width: 55},
                        {name: 'WO_STATE', label: '状态', width: 40, formatter: me.formatWoState},
                        {name: 'REMARK', label: '处理意见', width: 90},
                        {
                            name: 'FILES', label: '处理附件', width: 100, formatter: function (value) {
                                var html = "";
                                //循环回写附件标签
                                $.each(value, function (index, val) {
                                    html += "<a class=\"downloadClFile\" name=\"checkFile" +
                                        index +
                                        "\" fileName='" + val.FILE_NAME + "' fileId ='" + val.FILE_ID + "' fileType = '" + val.FILE_TYPE + "'>" +
                                        val.FILE_NAME +
                                        "</a><br>";
                                });
                                return html;
                            }
                        },
                        {
                            name: 'action', label: '操作', width: 70, formatter: function (cellval, opts, rwdat, _act) {
                                if ('xqAffair' == me.options.type) {
                                    return '<div class="btn-group">' +
                                        '<button type="button" name="singleRejectBtn" class="btn btn-link js-delete" isReject="0" orderId="' + rwdat.ORDER_ID + '" style="color: orangered">详情</button>' +
                                        '</div>';
                                }
                                if (rwdat.WO_STATE == '290000004') {
                                    return '<div class="btn-group">' +
                                        '<button type="button" name="singleRejectBtn" class="btn btn-link js-delete" isReject="1" orderId="' + rwdat.ORDER_ID + '" style="color: orangered">驳回</button>' +
                                        '<button type="button" name="singleRejectBtn" class="btn btn-link js-delete" isReject="0" orderId="' + rwdat.ORDER_ID + '" style="color: orangered">详情</button>' +
                                        '</div>';
                                } else {
                                    return '<div class="btn-group">' +
                                        '<button type="button" name="singleRejectBtn" class="btn btn-link js-delete" isReject="0" orderId="' + rwdat.ORDER_ID + '" style="color: orangered">详情</button>' +
                                        '</div>';
                                }
                            }
                        }

                    ],
                    autoResizable: true
                });

            },
            monitorRejectBtn: function () {
                var me = this;
                //驳回监听
                $("button[name='singleRejectBtn']").off('click').on('click', function (e) {
                    var orderId = $(this).attr("orderId");
                    var isReject = $(this).attr("isReject");
                    var createUser = me.options.affairOrder.CREATE_STAFF_NAME;
                    var pop = fish.popupView({
                        url: 'module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/views/childAffair',
                        width: "780px",
                        height: "86%",
                        title: "事务子单",
                        viewOption: {
                            orderId: orderId,
                            isReject: isReject,
                            createUser: createUser
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (e) {
                                me.writeBackDisposeInfo(this.options.affairOrder.AFFAIR_DISPATCH_ORDER_ID + '');

                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    });

                });
            },
            formatWoState: function (cellval, opts, rwdat, _act) {
                switch (cellval) {
                    case '290000003':
                        return '被签出';
                    case '290000005':
                        return '已作废';
                    case '290000004':
                        return '事务提交';
                    case '290000002':
                        if (rwdat.PRIV_FORWARD_WO_ID) {
                            return '<span style="color: red">驳回重新执行中</span>';
                        } else {
                            return '事务处理中';
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
            // 事务审核状态
            formatCheckWoState: function (cellval, opts, rwdat, _act) {
                switch (cellval) {
                    case '290000004':
                        return '审核通过';
                    case '290000002':
                        return '审核中';
                    case '290000006':
                        return '审核不通过';
                    case '290000007':
                        return '审核不通过';
                    default:
                        return cellval;
                }
            },
            // 回写事务处理信息
            writeBackDisposeInfo: function (affairId) {
                var _this = this;
                affairDispatcherAction.getAffairDisposeList(affairId, function (data) {
                    if (data && data.length > 0) {
                        $("#affairAffirm-clAffairGrid").grid("reloadData", data);

                        //下载监听
                        $('.downloadClFile').off('click').on('click', function (e) {
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

                        var selarrrow = new Array();
                        var unAbleRow=new Array();
                        $("#affairAffirm-clAffairGrid").grid("setAllCheckDisabled", true);
                        $.each(data, function (index, val) {
                            if (val.WO_STATE == '290000004') {
                                selarrrow.push(val.WO_ID);
                            }else {
                                unAbleRow.push(val.WO_ID);
                            }
                        });
                        $("#affairAffirm-clAffairGrid").grid("setCheckDisabled", selarrrow, false);
                        if (selarrrow.length > 0) {
                            $("#affairAffirm-clAffairGrid").grid("setAllCheckDisabled", false);
                            $("#reject").removeAttr("disabled");
                            $("#affairAffirm-clAffairGrid").grid("setCheckDisabled",unAbleRow, true);

                        }
                        if (selarrrow.length == data.length) {
                            $("#commit").removeAttr("disabled");
                        }
                        _this.monitorRejectBtn();
                    }
                });
            },
            // 事务驳回
            reject: function () {
                var _this = this;
                var selarrrow = $("#affairAffirm-clAffairGrid").grid("getCheckRows");
                if (selarrrow.length == 0) {
                    ngc.error("请先选择要驳回的事务处理工单！");
                    return;
                }
                var rejectRemark = $("#rejectRemark").val();
                if (rejectRemark) {
                    var parameters = new Array();
                    for (var i = 0; i < selarrrow.length; i++) {
                        var obj = {};
                        obj.workOrderId = selarrrow[i].WO_ID;
                        obj.orderId = selarrrow[i].ORDER_ID;
                        obj.remark = rejectRemark;
                        parameters.push(obj);
                    }
                    $("#qr-body-div").blockUI({message: '提交中...'}).data('blockui-content', true);
                    affairDispatcherAction.affairProcessRollBackWo(parameters, function (data) {
                        $("#qr-body-div").unblockUI().data('blockui-content', false);
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
            // 事务确认
            commit: function () {
                var _this = this;
                var affirmRemark = $("#affirmRemark").val();
                if (affirmRemark) {
                    var parameters = {};
                    parameters.orderId = this.options.affairOrder.ORDER_ID;
                    parameters.workOrderId = this.options.affairOrder.WO_ID;
                    parameters.remark = affirmRemark;
                    parameters.affairId = this.options.affairOrder.AFFAIR_DISPATCH_ORDER_ID;

                    $("#qr-body-div").blockUI({message: '提交中...'}).data('blockui-content', true);
                    affairDispatcherAction.affairAffirmComplateWo(parameters, function (data) {
                        $("#qr-body-div").unblockUI().data('blockui-content', false);
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
                    ngc.error("请先填写事务确认说明！");
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
            },
            // 触发调整窗口大小事件
            resize: function () {
                $(window).trigger("resize");
            }
        })
    });