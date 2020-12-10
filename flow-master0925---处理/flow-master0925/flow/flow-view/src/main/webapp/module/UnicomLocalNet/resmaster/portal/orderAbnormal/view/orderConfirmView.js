define(["text!module/UnicomLocalNet/resmaster/portal/orderAbnormal/template/orderConfirmView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
        "css!module/UnicomLocalNet/resmaster/portal/orderLocalStandby/styles/localStandby.css"],
    function(template,portalViewi18n,orderStandbyAction,css) {

        var maxFileSize = 50*1024*1024,                //文件大小限制
            maxFileCount = 20;                    //文件个数限制
        // debugger;
        var $log = $('.well');
        var fileId =  new Date().getTime();
        var URL;

        function appendLog(message) {
            $log.append(message + '<br>')
        }
        return ngc.View.extend({
            fileList : null,
            updateParams: new Object(),

            queryObj: new Object(),
            jsonSelarrrow: new Object(),
            template : ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #closeFile': 'closeFile',
                'click .js-file-upload': 'fileUpload',
                'click .js-select-file': 'fileSelect',
                'click #saveFile': 'fileUpload'
            },
            initialize: function() {
                appendLog('PopView initialize...')
                this.URl= this.getRootPath();
            },

            afterRender: function() {
                appendLog('PopView afterRender...')
                this.orderIds = this.options.orderIds;
                this.cstOrdId = this.options.cstOrdId;//客户Id
                this.chgType = this.options.chgType;//异常单类型
                this.tacheId = this.options.tacheId;//环节Id
                this.woId = this.options.woId;//工单Id
                this.regionId = this.options.orgId;//区域Id
                this.staffId =  this.options.userId;
                this.userName =  this.options.userName;

            },
            closeFile: function() {
                this.trigger('close');
                //弹出的视图实例会注入popup property，用于视图想自己关闭自己
                this.popup.close("fail");
            },
            fileUpload:function () {
                var fileTh = this;
                var remark = $('#remark').val();
                var files = $('#js-input-file').val();
                // debugger
                var fileLength = $('.file-list').find('li').length;
                if(fileLength != 0){
                    FILE_LIST_TPL = '<li class="list-group-item"><span class="glyphicon glyphicon-file"></span>{{fileName}} ({{fileSize}})<span class="file-remove glyphicon glyphicon-remove pull-right"></span></li>';
                    this.fileList.url = this.URl+"/localScheduleLT/orderStandbyController/abnomrmalInfo.spr";
                    // debugger;
                    //如果有需要传入到服务端的业务参数，则设置一下formData属性。
                    this.fileList.formData = {
                        name:this.staffIdColl ,
                        // selarrrow:this.jsonSelarrrow,
                        orderIds:this.orderIds,
                        cstOrdId:this.cstOrdId,
                        chgType:this.chgType,
                        tacheId:this.tacheId,//环节Id
                        woId: this.woId,//工单Id
                        regionId:this.regionId,//区域Id
                        staffId: this.staffId ,
                        userName: this.userName ,
                        fileId: fileId,
                        remark: remark,

                    }
                    //服务端有个类似的dto来接受这些参数
                    // debugger;
                    var result = this.fileList.submit();

                }
                    this.saveUpload(this);
                },
            fileSelect:function() {
                var seTh = this;
                //URL为文件上传的地址，支持额外参数拼接。例如'batch_task?name=aaa&age=22&height=180'
                FILE_LIST_TPL = '<li class="list-group-item" style="margin-left: 17.5%;width: 73%;"><span class="glyphicon glyphicon-file"></span>{{fileName}} ({{fileSize}})<span class="file-remove glyphicon glyphicon-remove pull-right"></span></li>';
                $('.js-select-file').fileupload({
                    dataType: 'json',
                    autoUpload: false,
                    acceptFileTypes: '',
                    always: function (e, data) {
                        console.log(data.textStatus);
                        console.log(data.result);
                        debugger;
                        if('error' == data.textStatus || 0 == data.result.updateCount){
                            fish.toast('info', '上传失败'+data.result.message)
                        }else{
                            seTh.closeFile();
                        }
                    },
                    progressall: function (e, data) {
                        var progress = parseInt(data.loaded / data.total * 100, 10);
                        $('#progress .progress-bar').css(
                            'width',
                            progress + '%'
                        );
                    },
                    processfail: function (e, data) {
                        var index = data.index,
                            file = data.files[index];
                        // debugger;
                        if (file.error && file.error === "File type not allowed" ) {
                            fish.error({message:"选择的文件类型不符，只能上传图片类型文件(以gif|jpg|jpeg|png结尾的文件)", modal:true});
                            return;
                        }
                    },
                    fail: function (e, data) {
                        // debugger;
                        $('<p/>').text("文件上传失败  " + data.errorThrown).appendTo('#files');
                    },
                    add: function (e, data) {
                        // debugger
                        if (data.files[0].size > maxFileSize) {
                            fish.warn('选择的文件大小超过上限50M，请重新选择');
                            return;
                        }
                        if (seTh.fileList != null && seTh.fileList.length >= maxFileCount) {
                            fish.warn('选择的文件个数超过上限'+maxFileCount+'个，请减少文件数');
                            return;
                        }
                        var obj = data.files[0],
                            fileName = decodeURIComponent(obj.name),
                            fileSize = (obj.size / 1024.0).toFixed(2) + "KB";
                        debugger;
                        if (seTh.fileList === null || seTh.fileList == undefined) {
                            seTh.fileList = data;
                        } else {
                            seTh.fileList.files.push(obj);
                        }
                        $('.file-list').append(fish.compile(FILE_LIST_TPL)({
                            fileName: fileName,
                            fileSize: fileSize
                        }));

                        $('.file-remove').off('click').on('click', function (e) {
                            var name = $(e.currentTarget).prev().text(), pos;
                            $.each(seTh.fileList.files, function (index, file) {
                                if (file.name === name) {
                                    pos = index;
                                    return false;
                                }
                            });
                            seTh.fileList.files.splice(pos, 1);
                            $(e.currentTarget).parent().remove();
                        });
                    }
                })
            },
            saveUpload:function (meSa) {
                var files = $('#js-input-file').val();
                var remark = $('#remark').val();
                // meSa.updateParams.selarrrow = meSa.jsonSelarrrow;
                meSa.updateParams.name = meSa.staffIdColl;
                meSa.updateParams.remark=remark;
                if(remark != null && files != null){
                    orderStandbyAction.updateCollapsibleSingle(meSa.updateParams,function (datas) {
                        // debugger;
                        if(1 == datas.updateCount){
                            meSa.trigger('close');

                            meSa.popup.close("success");
                        }else {
                            fish.toast("warn", "提交失败:"+datas.message);
                        }
                    });
                }
                 meSa.popup.close("success");
            },

            cleanup: function() {
                appendLog('PopView cleanup...');
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


        }); //fish.View.extend END
    }); //ALL END