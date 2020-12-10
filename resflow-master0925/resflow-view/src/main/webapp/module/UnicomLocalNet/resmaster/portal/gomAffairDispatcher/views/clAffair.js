define(["text!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/templates/clAffair.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/action/affairDispatcherAction',
        "css!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/styles/fqAffair.css"],
    function (template, portalViewi18n, affairDispatcherAction, css) {
        var fileIds;
        return ngc.View.extend({
            template: ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #commit': 'submitForm',
                'click #cancel': 'cancelFrom'
            },
            initialize: function () {
                this.render();
                userInfo = affairDispatcherAction.queryStaffInfo().responseJSON.data;
                FILES = null;
            },
            render: function () {           //渲染页面
                this.$el.html(this.template(this.i18nData));
            },
            //初始化fish组件
            afterRender: function () {
                $("#cl-tabs").tabs({
                    canClose: true,
                    paging: true,
                    autoResizable: true
                });
                $('.panel').panel({
                    collapsible: true
                });
                this.fileData = [];
                //初始化下拉框
                this.initComBoBox();
                //初始化调单明细
                this.initAffairOrderInfo(this.options.affairOrder);
                //初始化表格信息
                this.initGrid();
                //初始化附件表格
                this.initDisposeFileGrid();
                //初始化调单处理进度表格
                this.initChildDisposeAffairGrid();
                this.writeBackChildDisposeInfo();
            },
            //初始化下拉框
            initComBoBox: function () {
                //初始化是事务状态
                this.initStateComBoBox();
                //初始化事务单类型
                this.initTypeComBoBox();
                //初始化是否审核
                this.initIsCheckComBoBox();
                //初始化审查人
                this.initCheckStaffPopeEdit();
            },
            //初始化表格
            initGrid: function () {
                //初始化抄送人表格
                this.initCopyStaffPopeEdit();
                //初始化审核附件表格
                this.initCheckAffairGrid();
                //初始化调单处理人表格
                this.initDisposeStaffGrid();
            },
            //初始化是事务状态
            initStateComBoBox: function () {
                var $combobox1 = $('#affairDispose-state').combobox({
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
                $combobox1.combobox('value', this.options.affairOrderState);
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
                $('#affairDispose-isCheck,#isReviewCheck').combobox({
                    placeholder: '--是否审查--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '否', value: '1', disabled: true},
                        {name: '是', value: '0'}
                    ],
                    change: function () {
                        $("#reviewStaff").val("");
                        if ($('#isReviewCheck').combobox('value') == '0') {
                            $("#reviewStaffDiv").show();
                            $("#reviewStaffDiv").addClass("required");
                            $("#reviewStaff").resetElement();
                            $("#reviewStaff").removeAttr('data-rule-ignore');
                        } else {
                            $("#reviewStaffDiv").hide();
                            $("#reviewStaffDiv").removeClass("required");
                            $("#reviewStaff").resetElement();
                            $("#reviewStaff").attr('data-rule-ignore',true);

                        }
                    }
                });
                $('#isReviewCheck').combobox('value', '1');
            },
            // 初始化审查人
            initCheckStaffPopeEdit: function () {
                $("#reviewStaff").popedit({
                    initialData: {
                        'name': '--请选择审核人--',
                        'value': ''
                    },
                    open: function (e) {
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/transferView',
                            height: 490,
                            width: 650,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag: "transferStaff",
                                currentAreaId: userInfo.areaId,
                                currentOrgId: userInfo.orgId,
                                currentUserId: userInfo.userId
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    var orgNames = '';
                                    var orgIds = '';
                                    var objType = '';
                                    //    var orgIds = new Array();
                                    res.forEach(function (val, i) {
                                        if (i == 0) {
                                            orgNames = val.name;
                                        }
                                        orgIds = val.id;
                                        objType = val.objType;
                                    })
                                    _this.popedit('setValue', {name: orgNames, value: orgIds, objType: objType});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    },
                });
            },
            //初始化抄送人信息
            initCopyStaffPopeEdit: function () {
                $("#affairDispose-copyStaffArrGrid").grid({
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
                $("#cl-form").form("value", obj);
                $('#affairDispose-isCheck').combobox('value', obj.IS_CHECK);
                $("#affairDispose-disposeStaffGrid").grid("addRowData", JSON.parse(obj.DISPOSE_STAFF_ARR));
                //回写附件信息
                this.writeBackFileInfo(obj.FILE_INFO)
                //回写抄送人信息
                this.writeBackCopyStaffInfo(obj.AFFAIR_DISPATCH_ORDER_ID + '');
                //回写审核信息
                this.writeBackCheckInfo(obj.AFFAIR_DISPATCH_ORDER_ID + '');
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
                        $("#affairDispose-copyStaffArrGrid").grid("reloadData", data);
                    }
                });
            },

            // 初始化事务单处理人
            initDisposeStaffGrid: function () {
                $("#affairDispose-disposeStaffGrid").grid({
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
                $("#affairDispose-shAffairGrid").grid({
                    minHeight: '110',
                    height: 'auto',
                    colModel: [
                        {name: 'WO_ID', label: '审核工单', width: 40, sortable: false},
                        {name: 'REQ_FIN_DATE', label: '审核日期', width: 55},
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
                    autoResizable: true
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
            // 事务审核状态
            formatCheckWoState: function (cellval, opts, rwdat, _act) {
                switch (cellval) {
                    case '290000004':
                        return '审核通过';
                    case '290000002':
                        return '审核通过';
                    case '290000006':
                        return '审核不通过';
                    case '290000007':
                        return '审核不通过';
                    default:
                        return cellval;
                }
            },
            // 回写事务审核信息
            writeBackCheckInfo: function (affairId) {
                affairDispatcherAction.getAffairCheckInfoArray(affairId, function (data) {
                    if (data && data.length > 0) {
                        $("#affairDispose-shAffairGrid").grid("reloadData", data);

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
            // 初始化附件表格
            initDisposeFileGrid: function () {
                var me = this;
                $("#affairDispose-clfileGrid").grid({
                    minHeight: '110',
                    height: 'auto',
                    colModel: [
                        {name: 'fileName', label: '文件名称', width: 250, sortable: false},
                        {name: 'fileSize', label: '大小', width: 100},
                        {name: 'fileType', label: '类型', width: 100, sortable: false},
                        {
                            name: 'action', label: '操作', width: 120, formatter: 'actions',
                            formatoptions: {
                                editbutton: false,
                                delbutton: true
                            }
                        }
                    ],
                    autoResizable: false
                });

                $('#affairDispose-selectClFiles').fileupload({
                    dataType: 'json',
                    autoUpload: false,
                    add: function (e, data) {
                        var fileObj = {};
                        var obj = data.files[0];
                        fileObj.fileName = obj.name.split(".")[0];
                        fileObj.fileSize = (obj.size / 1024.0).toFixed(2) + "KB";
                        fileObj.fileType = obj.name.split(".")[1];
                        if ((obj.size / 1024.0).toFixed(2) >= 20 * 1024) {
                            fish.warn('上传文件不能超过20M！');
                            return;
                        }
                        $("#affairDispose-clfileGrid").grid("addRowData", fileObj);
                        if (FILES === null) {
                            FILES = data;
                        } else {
                            FILES.files.push(obj);
                        }
                        $('.inline-remove').off('click').on('click', function (e) {
                            var rowid = $(this).closest("tr.jqgrow").attr("id");
                            var data = $("#affairDispose-clfileGrid").grid("getRowData", rowid)
                            $("#affairDispose-clfileGrid").grid("delRowData", data);
                            var i;
                            $.each(FILES.files, function (index, file) {
                                if (file.name === data.fileName) {
                                    i = index;
                                }
                            });
                            FILES.files.splice(i, 1);
                        });
                    },
                    done: function (e, data) {
                        debugger;
                        fileIds = data.result;
                        me.commitFrom();
                    },
                    fail: function (e, data) {
                        fish.info("附件上传失败！");
                        return;
                    }
                });

            },
            // 初始化事务处理进度表格
            initChildDisposeAffairGrid: function () {
                var me = this;
                $("#clAffairGrid").grid({
                    height: 'auto',
                    multiselect: true,
                    colModel: [
                        {name: 'WO_ID', label: '工单ID', width: 40, sorttype: "int", key: true},
                        {name: 'DEAL_STAFF_NAME', label: '处理人', width: 40},
                        {name: 'ORDER_ID', label: '定单ID', hidden: true},
                        {name: 'DEAL_DATE', label: '处理日期', width: 55},
                        {name: 'WO_STATE', label: '状态', width: 50, formatter: me.formatWoState},
                        {name: 'REMARK', label: '处理意见', width: 90},
                        {name: 'action', label: '操作', width: 70, formatter: function (cellval, opts, rwdat, _act) {
                                    return '<div class="btn-group">' +
                                        '<button type="button" name="singleRejectBtn" class="btn btn-link js-delete" isReject="0" orderId="' + rwdat.ORDER_ID + '" style="color: orangered">详情</button>' +
                                        '</div>';
                            }
                        },
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
                affairDispatcherAction.getAffairDisposeList(this.options.affairOrder.AFFAIR_DISPATCH_ORDER_ID + '', function (data) {
                    if (data && data.length > 0) {
                        $("#clAffairGrid").grid("reloadData", data);
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
                        _this.monitorRejectBtn();
                    }
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
                                // me.writeBackDisposeInfo(this.options.affairOrder.AFFAIR_DISPATCH_ORDER_ID + '');

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
                        return '事务处理完成';
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
            // 附件上传
            fileUpdate: function () {
                FILES.url = this.getRootPath() + "/localScheduleLT/initProdFileUploadController/affairUploadFiles.spr";
                FILES.submit();
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
            submitForm: function () {
                var me=this;
                debugger;
                if (FILES != null) {
                    //先上传
                    me.fileUpdate();
                } else {
                    me.commitFrom();
                }
            },
            // 提交事务处理单
            commitFrom: function () {
                var _this = this;
                var $form = $('#cl-form').form();
                $form.form('option', 'skipNullField', false);
                var parameters = $form.form('value');
                if (parameters.isReviewCheck == '' || parameters.isReviewCheck == null){
                    fish.error("请选择是否审查。。。");
                    return;
                }
                $form.form('option', 'skipNullField', true);
                var result = $("#cl-form").isValid();
                parameters.workOrderId = this.options.affairOrder.WO_ID;
                parameters.orderId = this.options.affairOrder.ORDER_ID;
                parameters.remark = parameters.REMARK;
                parameters.affairId = this.options.affairOrder.AFFAIR_DISPATCH_ORDER_ID;
                parameters.createStaff = this.options.affairOrder.CREATE_STAFF_ID;
                parameters.isCheck = this.options.affairOrder.IS_CHECK;
                parameters.fileInfos=fileIds;
                if (result) {
                    $("#cl-body-div").blockUI({message: '请稍等...'}).data('blockui-content', true);
                    affairDispatcherAction.affairProcessComplateWo(parameters, function (data) {
                        $("#cl-body-div").unblockUI().data('blockui-content', false);
                        if (data) {
                            if (data.code == 'SUCCESS') {
                                ngc.info(data.message);
                                _this.cancelFrom();
                            } else {
                                ngc.error("提交失败"+data.message);
                            }
                        } else {
                            ngc.error("提交失败！");
                        }
                    });

                } else {
                    fish.error("请检查必填项，谢谢！");
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