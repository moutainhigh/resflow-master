define(["text!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/templates/fqAffair.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/action/affairDispatcherAction',
        "css!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/styles/fqAffair.css"],
    function (template, portalViewi18n, affairDispatcherAction, css) {
        var fileIds;
        var commitParameters = {};
        return ngc.View.extend({
            template: ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #save': 'saveAffairOrder',
                'click #commit': 'commitAffairOrder',
                'click #cancel': 'cancelFrom',
                'click #disposeStaffBtn': 'selectDisposeStaff',
                'click #copyStaffArrBtn': 'selectCopyStaff'
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
                //初始化是事务状态
                this.initStateComBoBox();
                //初始化事务类型
                this.initTypeComBoBox();
                //初始化是否审核
                this.initIsCheckComBoBox();
                //初始化文件上传
                this.initFileUpdate();
                //初始化受理人选择框
                this.initDisposeStaffPopeEdit();
                this.initCheckStaffPopeEdit();
                this.initCopyStaffPopeEdit();
                //回写数据（重新发起，草稿）
                this.writeBackAffairData();
            },
            //回写事务调单数据
            writeBackAffairData: function () {
                var affairInfo = this.options.affairInfo;
                var affairState = this.options.affairOrderState;
                //回写title和按钮
                $("#fq-model-title").text(affairInfo.fqtitleName);
                $("#commit").text(affairInfo.btnName);
                //如果不是直接发起，回写数据
                if (affairDispatcherAction.AFFAIR_ORDER_STATE.FQSW != affairState) {
                    //如果审核驳回
                    if (affairDispatcherAction.AFFAIR_ORDER_STATE.SHBH == affairState) {
                        //隐藏保存草稿按钮
                        $("#save").hide();
                        //TODO 驳回审核意见回写，暂时隐藏审核意见
                        $("#auditOpinionsDiv").hide();
                    }
                    //回写数据
                    console.log("affairInfo", affairInfo);
                    $("#fq-content").val(affairInfo.CONTENT);
                    $("#fq-title").val(affairInfo.TITLE);
                    $("#fq-code").val(affairInfo.AFFAIR_DISPATCH_ORDER_CODE);
                    $("#disposeStaffGrid").grid("reloadData", JSON.parse(affairInfo.DISPOSE_STAFF_ARR));
                    //回写附件信息
                    if (typeof (affairInfo.FILE_INFO) != undefined && "" != affairInfo.FILE_INFO.trim()) {
                        $("#fileGrid").grid("reloadData", JSON.parse(affairInfo.FILE_INFO));
                    }
                    $("#isCheck").combobox('value', affairInfo.IS_CHECK);
                    affairDispatcherAction.getAffairNoticeStaffArray(affairInfo.AFFAIR_DISPATCH_ORDER_ID + '', function (data) {
                        console.log("data", data);
                        if (data && data.length > 0) {
                            $("#copyStaffArrGrid").grid("reloadData", data);
                        }
                    });
                    //如果审核通过
                    if (affairDispatcherAction.AFFAIR_CHECK_STATE.PASS == affairInfo.IS_CHECK) {
                        $("#checkStaffDiv").show();
                        $("#checkStaff").popedit('setValue', {
                            name: affairInfo.CHECK_STAFF_NAME,
                            value: affairInfo.CHECK_STAFF
                        });
                    }
                }
            },
            // 初始化抄送人
            initCopyStaffPopeEdit: function () {
                $("#copyStaffArrGrid").grid({
                    minHeight: '110',
                    height: 'auto',
                    colModel: [
                        {name: 'name', label: '姓名', width: 50, sortable: false},
                        {name: 'section', label: '部门', width: 100},
                        {name: 'id', label: '用户ID', width: 100, hidden: true,key:true},
                        {
                            name: 'action', label: '操作', width: 50, formatter: 'actions',
                            formatoptions: {
                                editbutton: false,
                                delbutton: true
                            }
                        }
                    ],
                    autoResizable: false
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
                return localhostPaht + projectName;
            },
            selectDisposeStaff: function () {
                this.selectStaffPopeEdit("选择事务调单处理人", "disposeStaffGrid");
            },
            selectCopyStaff: function () {
                this.selectStaffPopeEdit("选择事务调单抄送人", "copyStaffArrGrid");
            },
            selectStaffPopeEdit: function (title, grid) {
                var ids = $("#"+grid).grid("getDataIDs");//获取所有的rowid记录
                var _this = $(this);
                var pop = fish.popupView({
                    url: 'module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/views/userView',
                    height: 490,
                    width: 700,
                    title: title,
                    modal: false,
                    draggable: false,
                    autoResizable: true,
                    viewOption: {
                        flag: "transferStaff",
                        title: title,
                        currentAreaId: userInfo.areaId,
                        currentOrgId: userInfo.orgId,
                        currentUserId: userInfo.userId,
                        ids:ids
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (res) {
                            $("#" + grid).grid("addRowData", res);
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                });

            },
            // 初始化审核人
            initCheckStaffPopeEdit: function () {
                $("#checkStaff").popedit({
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
            // 初始化事务单处理人
            initDisposeStaffPopeEdit: function () {
                $("#disposeStaffGrid").grid({
                    minHeight: '110',
                    height: 'auto',
                    colModel: [
                        {name: 'name', label: '姓名', width: 50, sortable: false},
                        {name: 'section', label: '部门', width: 100},
                        {name: 'id', label: '用户ID', width: 100, hidden: true,key:true},
                        {
                            name: 'action', label: '操作', width: 50, formatter: 'actions',
                            formatoptions: {
                                editbutton: false,
                                delbutton: true
                            }
                        }
                    ],
                    autoResizable: false
                });
            },
            //初始化是事务状态
            initStateComBoBox: function () {
                var $combobox1 = $('#state').combobox({
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
                if (affairDispatcherAction.AFFAIR_ORDER_STATE.CGX == this.options.affairOrderState || affairDispatcherAction.AFFAIR_ORDER_STATE.FQSW == this.options.affairOrderState) {
                    $("#auditOpinionsDiv").hide();
                } else {
                    $("#auditOpinionsDiv").show();
                }
            },

            //初始化是事务单类型
            initTypeComBoBox: function () {
                var dataArray = new Array;
                affairDispatcherAction.qryAffairDispatchOrderType(function(data) {
                    dataArray = data;
                    var $comboboxType = $('#type').combobox({
                        //placeholder: '--请选择事务状态--',
                        dataTextField: 'name',
                        dataValueField: 'value',
                        dataSource: dataArray
                    });
                    $comboboxType.combobox('value',dataArray[0].value);
                });
            },

            //初始化是否审核
            initIsCheckComBoBox: function () {
                $('#isCheck').combobox({
                    placeholder: '--是否审核--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '否', value: '1', disabled: true},
                        {name: '是', value: '0'}
                    ],
                    change: function () {
                        $("#checkStaff").val("");
                        if ($('#isCheck').combobox('value') == '0') {
                            $("#checkStaffDiv").show();
                            $("#checkStaffDiv").addClass("required");
                           // $("#checkStaff").attr("data-rule", "required");
                            $("#checkStaff").resetElement();
                            $("#checkStaff").removeAttr('data-rule-ignore');
                        } else {
                            $("#checkStaffDiv").hide();
                            $("#checkStaffDiv").removeClass("required");
                          //  $("#checkStaff").removeClass("data-rule");
                            $("#checkStaff").resetElement();
                            $("#checkStaff").attr('data-rule-ignore',true);

                        }
                    }
                });
                $('#isCheck').combobox('value',"1")
            },
            //初始化文件上传
            initFileUpdate: function () {
                var me = this;
                $("#fileGrid").grid({
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
                $('#selectFiles').fileupload({
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
                        $("#fileGrid").grid("addRowData", fileObj);
                        if (FILES === null) {
                            FILES = data;
                        } else {
                            FILES.files.push(obj);
                        }
                        $('.inline-remove').off('click').on('click', function (e) {
                            var rowid = $(this).closest("tr.jqgrow").attr("id");
                            var data = $("#fileGrid").grid("getRowData", rowid)
                            $("#fileGrid").grid("delRowData", data);
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
                        fileIds = data.result;
                        me.moreSubmitAffairForm(commitParameters);
                    },
                    fail: function (e, data) {
                        fish.info("附件上传失败！");
                        return;
                    }
                });
            },
            // 附件上传
            fileUpdate: function () {
                FILES.url = this.getRootPath() + "/localScheduleLT/initProdFileUploadController/affairUploadFiles.spr";
                FILES.submit();
            },
            // 保存事务单
            saveAffairOrder: function () {
                var $form = $('#fq-form').form();
                $form.form('option', 'skipNullField', false);
                var parameters = $form.form('value');
                $form.form('option', 'skipNullField', true);
                parameters.state = affairDispatcherAction.AFFAIR_ORDER_STATE.CGX;
                this.submitAffairForm(parameters);
            },
            // 提交事务单
            commitAffairOrder: function () {
                var $form = $('#fq-form').form();
                $form.form('option', 'skipNullField', false);
                var parameters = $form.form('value');
                $form.form('option', 'skipNullField', true);
                parameters.state = affairDispatcherAction.AFFAIR_ORDER_STATE.FQSW;
                this.submitAffairForm(parameters);
            },
            submitAffairForm: function (parameters) {
                var _this = this;
                var result = $("#fq-form").isValid();

                if (typeof (_this.options.affairInfo.AFFAIR_DISPATCH_ORDER_ID) != undefined) {
                    parameters.affairId = _this.options.affairInfo.AFFAIR_DISPATCH_ORDER_ID;
                }
                if (result) {
                    //如果为重新发起
                    if (affairDispatcherAction.AFFAIR_ORDER_STATE.SHBH == _this.options.affairOrderState) {
                        parameters.workOrderId = _this.options.affairInfo.WO_ID;
                        parameters.orderId = _this.options.affairInfo.ORDER_ID;
                    }
                    var disposeStaffArr = $("#disposeStaffGrid").grid("getRowData");
                    if (disposeStaffArr.length <= 0) {
                        fish.info("事务调单处理人不能为空！");
                        return;
                    }
                    parameters.disposeStaffArr = JSON.stringify(disposeStaffArr);
                    commitParameters = parameters;
                    if (FILES != null) {
                        _this.fileUpdate();
                    } else {
                        this.moreSubmitAffairForm(commitParameters);
                    }
                } else {
                    fish.info("表单校验未通过！");
                }
            },
            moreSubmitAffairForm: function (parameters) {
                var _this = this;
                if (parameters.isCheck == "1") {
                    parameters.checkStaff = "";
                }
                parameters.fileInfo = JSON.stringify(fileIds);
                var copyStaffArr = $("#copyStaffArrGrid").grid("getRowData");
                var map = {
                    affairDisOrderInfo: parameters,
                    noticeStaffArray: copyStaffArr
                };
                $("#fq-body-div").blockUI({message: '提交中...'}).data('blockui-content', true);
                affairDispatcherAction.saveAffairDispatchOrder(map, function (data) {
                    $("#fq-body-div").unblockUI().data('blockui-content', false);
                    if (data) {
                        if (data.result == 'success') {
                            ngc.info("保存成功，事务调单编号：" + data.code);
                            _this.cancelFrom();
                        } else {
                            ngc.error("保存失败：" + data.errorInfo);
                        }
                    } else {
                        ngc.error("保存失败！");
                    }

                });
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