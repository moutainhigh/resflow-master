define(["text!module/UnicomLocalNet/resmaster/portal/orderLocalStandby/templates/applyCheck.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
        "css!module/UnicomLocalNet/resmaster/portal/orderLocalStandby/styles/localStandby.css"],
    function(template,portalViewi18n,orderStandbyAction,css) {
        return fish.View.extend({
            template: fish.compile(template),
            i18nData: fish.extend({}, portalViewi18n),
            events: {
                'click #submitBtn': 'submit',
                'click #closeFile': 'closeFile',
            },
            initialize: function() {
                this.render();
                URL = "";
                FILES = null; //附件
                userInfo = orderStandbyAction.queryStaffInfo().responseJSON.data; //用户信息
                this.delFiles = new Array();
            },
            //渲染页面
            render: function() {
                this.$el.html(this.template(this.i18nData));
            },

            //初始化fish组件
            afterRender: function() {
                URL=this.getRootPath();
                var me = this;
                this.initFish();
                //初始化附件表格
                this.initFileUpdate();
            },

            initFish: function(){
                //审核不通过说明必填
                $('#checkResult').bind('click',function(){
                    if ($("input[name='checkRadio']:checked").val() == '0') { //审核通过 说明不限制
                        $("#remarkGoRoll").removeClass("requireds");
                        var str = '备注';
                        document.getElementById('remarkGoRoll').innerHTML=str;
                    } else if ($("input[name='checkRadio']:checked").val() == '1') { //审核不通过 说明必填 不显示报竣时间
                        $("#remarkGoRoll").addClass("requireds");
                        var str = '<span style="color: red">*</span>备注';
                        document.getElementById('remarkGoRoll').innerHTML=str;
                    }
                });
            },
            submit: function(){
                debugger;
                var me = this;
                var params = new Object();
                var remark =  $("#remark").val();
                if ($("input[name='checkRadio']:checked").val() == '1' && (remark == null || remark == "" || remark == undefined)){
                    fish.warn("备注不可为空！");
                    return;
                }
                params.circuitData = this.options.selArrrow;
                params.agreeOrNot = $("input[name='checkRadio']:checked").val();
                params.remark = remark;
                params.btnFlg='submit';

                var fileData = $("#fileGrid").grid("getRowData");
                params.fileData = fileData;
                var delFilesStr = "";

                for (var j = 0; j < this.delFiles.length; j++) {
                    var temp = false;
                    for (var i = 0; i < fileData.length; i++) {
                        if (this.delFiles[j] == fileData[i].attachInfoId) {
                            temp = true;
                            break;
                        }
                    }
                    if (!temp) {
                        delFilesStr += this.delFiles[j] + ",";
                    }
                }
                if (delFilesStr) {
                    params.delFiles = delFilesStr.substring(0, delFilesStr.length - 1);
                }


                if (FILES) {
                    var vernier = 0;
                    var fileLength = FILES.files.length;
                    for (var index = 0; index < fileLength; index++) {
                        var file = FILES.files[index - vernier];
                        var isRemove = true;
                        for (var i = 0; i < fileData.length; i++) {
                            var data = fileData[i];
                            if (file.name == data.fileName + "." + data.fileType) {
                                isRemove = false;
                            }
                        }
                        if (isRemove) {
                            FILES.files.splice(index - vernier, 1);
                            vernier++;
                        }
                    }
                }
                if (FILES === null) {
                    me.submitOrder(params);
                } else {
                    me.fileUpdate(params); //上传附件并回单
                }


            },

            submitOrder : function(params){
                var me = this;
                params.origin = 'SH';
                params.action = 'postponementApply';
                params.woId = me.options.woId;
                orderStandbyAction.feedBackToOneDryBatch(params, function (res) {
                    $.unblockUI();
                    if (res.success) {
                        fish.toast('success', res.message);
                        me.popup.close();
                        me.pop.hide();
                    }else{
                        fish.toast('error', res.message);
                    }

                });
            },
  /*          submitOrder : function(params){
                var me = this;
                params.origin = 'SH';
                params.action = 'postponementApply';
                params.woId = me.options.woId;

                $("#orderOper-form").blockUI({message: '派单中...'}).data('blockui-content', true);
                orderStandbyAction.tacheDoSomethingList(params, function (data)  {
                    if(data.success){
                        $("#orderOper-form").unblockUI().data('blockui-content', false);
                        fish.toast('success', data.message);
                        hi.closeFile();
                    }else {
                        $("#orderOper-form").unblockUI().data('blockui-content', false);
                        fish.toast('error', data.message);
                        hi.closeFile();
                    }
                }).always(function () { //一定会执行
                    hi.closeFile();
                    $.unblockUI();
                });

            },*/
            //初始化附件表格
            initFileUpdate : function() {
                $("#fileGrid").grid({
                    colModel: [
                        {name: 'fileName', label: '文件名称', width: 160, sortable: false },
                        {name: 'fileSize', label: '大小', width: 40 },
                        {name: 'fileType', label: '类型', width: 40 , sortable: false },
                        {name: 'action', label: '操作', width: 100, formatter: 'actions',
                            formatoptions: {
                                editbutton: false,
                                delbutton: false,
                                inlineButtonAdd: function (rowdata) {
                                    return [{ //可以给actions类型添加多个icon图标,事件自行控制
                                        id: "jRemoveButton", //每个图标的id规则为id+"_"+rowid
                                        className: "inline-remove",//每个图标的class
                                        icon: "glyphicon glyphicon-remove-circle",//图标的样子
                                        title: "删除"//鼠标移上去显示的内容
                                    }]
                                }
                            }
                        }
                    ],
                    width:516
                });
                var me = this;
                me.$('#selectFiles').fileupload({
                    dataType: 'json',
                    autoUpload: false,
                    add: function(e, data) {
                        var fileObj = {};
                        var obj = data.files[0];
                        fileObj.fileName = obj.name.split(".")[0];
                        fileObj.fileSize = (obj.size / 1024.0).toFixed(2) + "KB";
                        fileObj.fileType = obj.name.split(".")[1];
                        if ((obj.size / 1024.0).toFixed(2) >= 20*1024 ){
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
                            var data = $("#fileGrid").grid("getRowData",rowid)
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
                    always: function (e, data) {
                        if (data.result.success) {
                            $("#orderOper-form").unblockUI().data('blockui-content', false);
                            fish.toast('success', data.result.message);
                            me.popup.close();
                        }else {
                            $("#orderOper-form").unblockUI().data('blockui-content', false);
                            fish.toast('error', data.result.message);
                        }
                    },
                });
            },
            fileUpdate :function (params){
                params.origin = 'SH';
                params.action = 'postponementApplyBatch';
                params.woId = this.options.woId;
                FILES.url = URL+"/localScheduleLT/FlieUpdateController/uploadFiles.spr";
                FILES.formData = {
                    params : JSON.stringify(params)
                };
                if (!this.isSubmit) {
                    this.isSubmit = true;
                    FILES.submit();
                } else {
                    fish.toast('error', "请勿重复提交！");
                }
            },
            getRootPath:function (){
                //获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
                var curWwwPath=window.document.location.href;
                //获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
                var pathName=window.document.location.pathname;
                var pos=curWwwPath.indexOf(pathName);
                //获取主机地址，如： http://localhost:8083
                var localhostPaht=curWwwPath.substring(0,pos);
                //获取带"/"的项目名，如：/uimcardprj
                var projectName=pathName.substring(0,pathName.substr(1).indexOf('/')+1);
                return (localhostPaht+projectName);
            },
            closeFile: function() {
                this.trigger('close');
                //弹出的视图实例会注入popup property，用于视图想自己关闭自己
                this.popup.close();
            },

        });
    });