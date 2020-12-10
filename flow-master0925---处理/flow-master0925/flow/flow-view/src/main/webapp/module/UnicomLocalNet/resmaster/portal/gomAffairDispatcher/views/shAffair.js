define(["text!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/templates/shAffair.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/action/affairDispatcherAction',
        "css!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/styles/fqAffair.css"],
    function (template, portalViewi18n, affairDispatcherAction, css) {
        var fileIds;
        return ngc.View.extend({
            template: ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #commit': 'SubmitCheckForm',
                'click #cancel': 'cancel'
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
                this.fileData=[];
                //初始化下拉框
                this.initComBoBox();
                //初始化调单明细
                this.initAffairOrderInfo(this.options.affairOrder);
                //初始化表格信息
                this.initGrid();

            },
            //初始化下拉框
            initComBoBox:function(){
                //初始化是事务状态
                this.initStateComBoBox();
                //初始化事务单类型
                this.initTypeComBoBox();
                //初始化是否审核
                this.initIsCheckComBoBox();
            },
            //初始化表格
            initGrid:function (){
                //初始化抄送人表格
                this.initCopyStaffPopeEdit();
                //初始化审核附件表格
                this.initCheckFileGrid();
                //初始化调单处理人表格
                this.initDisposeStaffGrid();
            },
            //初始化是事务状态
            initStateComBoBox: function () {
                var $combobox1 = $('#affairCheck-state').combobox({
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
                $('#affairCheck-isCheck').combobox({
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
                $("#affairCheck-copyStaffArrGrid").grid({
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
                $("#affairCheck-form").form("value",obj);
                $('#affairCheck-isCheck').combobox('value', obj.IS_CHECK);
                $("#affairCheck-disposeStaffGrid").grid("addRowData",JSON.parse(obj.DISPOSE_STAFF_ARR) );
                //回写附件信息
                this.writeBackFileInfo(obj.FILE_INFO)
                //回写抄送人信息
                this.writeBackCopyStaffInfo(obj.AFFAIR_DISPATCH_ORDER_ID + '');
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
                            "</a>;&nbsp ");
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
            writeBackCopyStaffInfo: function (affairId) {
                affairDispatcherAction.getAffairNoticeStaffArray(affairId, function (data) {
                    if (data && data.length > 0) {
                        $("#affairCheck-copyStaffArrGrid").grid("reloadData", data);
                    }
                });
            },

            //初始化附件表格
            initCheckFileGrid: function() {
                var me = this;
                $("#affairCheck-shfileGrid").grid({
                    minHeight: '110',
                    height: 'auto',
                    colModel: [
                        {name: 'fileName', label: '文件名称', width: 250, sortable: false },
                        {name: 'fileSize', label: '大小', width: 100 },
                        {name: 'fileType', label: '类型', width: 100 , sortable: false },
                        {name: 'action', label: '操作', width: 120, formatter: 'actions',
                            formatoptions: {
                                editbutton: false,
                                delbutton: true
                            }
                        }
                    ],
                    autoResizable: false
                });
                $('#affairCheck-selectFiles').fileupload({
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
                        $("#affairCheck-shfileGrid").grid("addRowData", fileObj);
                        if (FILES === null) {
                            FILES = data;
                        } else {
                            FILES.files.push(obj);
                        }
                        $('.inline-remove').off('click').on('click', function (e) {
                            var rowid = $(this).closest("tr.jqgrow").attr("id");
                            var data = $("#affairCheck-shfileGrid").grid("getRowData", rowid)
                            $("#affairCheck-shfileGrid").grid("delRowData", data);
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
                        me.commit();
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
            // 初始化事务单处理人
            initDisposeStaffGrid: function () {
                $("#affairCheck-disposeStaffGrid").grid({
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
            //提交
            SubmitCheckForm: function () {
                var me=this;
                if (FILES != null) {
                    //先上传
                    me.fileUpdate();
                } else {
                    me.commit();
                }
            },
            //实际提交
            commit: function () {
                var _this = this;
                var $form = $('#affairCheck-form').form();
                $form.form('option', 'skipNullField', false);
                $form.form('option', 'skipNullField', true);
                var parameters = $form.form('value');
                if (parameters.isPass == '1') {
                    if (!parameters.auditOpinions) {
                        ngc.error("审核不通过时，审核意见必填！");
                        return;
                    }
                }
                var affairOrder = this.options.affairOrder;
                var map = {
                    workOrderId: affairOrder.WO_ID,
                    affairId: affairOrder.AFFAIR_DISPATCH_ORDER_ID,
                    orderId: affairOrder.ORDER_ID,
                    isPass: parameters.isPass,
                    remark: parameters.auditOpinions,
                    fileInfos: fileIds
                };

                $("#sh-body-div").blockUI({message: '请稍等...'}).data('blockui-content', true);
                affairDispatcherAction.affairDispatchOrderCheck(map, function (data) {
                    $("#sh-body-div").unblockUI().data('blockui-content', false);
                    if (data) {
                        if (data.code == 'SUCCESS') {
                            ngc.info(data.message);
                            _this.cancel();
                        } else {
                            ngc.error(data.message);
                        }
                    } else {
                        ngc.error("保存失败！");
                    }
                });
            },
            cancel: function () {
                this.popup.close();
            }

        });
    });