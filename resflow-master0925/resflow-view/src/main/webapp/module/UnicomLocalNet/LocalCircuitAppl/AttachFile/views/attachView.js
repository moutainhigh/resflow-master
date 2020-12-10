/**
 * 多选择树JS
 */
define(['module/UnicomLocalNet/LocalCircuitAppl/AttachFile/action/attachAction.js',
    'text!module/UnicomLocalNet/LocalCircuitAppl/AttachFile/templates/attachView.html',
    'i18n!module/UnicomLocalNet/LocalCircuitAppl/AttachFile/i18n/attach.i18n',
    'css!module/UnicomLocalNet/LocalCircuitAppl/AttachFile/css/attach.css'
], function(attachAction,attachView,i18n,css) {
    return fish.View.extend({
        template: fish.compile(operTreeOrder),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtn': 'submit',
        },
        initialize: function() {
            this.render();
        },
        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },
        //初始化fish组件
        afterRender: function() {
            this.fileDDKSelect();
        },
        fileDDKSelect:function() {
            //URL为文件上传的地址，支持额外参数拼接。例如'batch_task?name=aaa&age=22&height=180'
            var urlUpLoad = this.getRootPath()+"/localScheduleLT/initProdFileUploadController/uploadFiles.spr";
            FILE_LIST_DDK =  '<li class="list-group-item">' +
                '                       <div class="url" style="display: inline-block;width: 90%">' +
                '                           <span class="glyphicon glyphicon-file"></span>' +
                '                           <span class="fileName">{{fileName}}</span>' +
                '                           <span>({{fileSize}})</span>' +
                '                       </div>' +
                '                       <span class="file-remove-ddk glyphicon glyphicon-remove pull-right"></span></li>';
            $('#BigSpeedAttachment').fileupload({
                dataType: 'json',
                autoUpload: false,
                acceptFileTypes: '',
                done: function (e, data) {
                    $.each(data.originalFiles,function(index,file){
                        var fileName = decodeURIComponent(file.name);
                        $('<p/>').text(fileName + '   upload suceess, file.size:' + file.size).appendTo('#files');
                    })
                },
                processfail: function (e, data) {
                    var index = data.index,
                        file = data.files[index];
                    if (file.error && file.error === "File type not allowed" ) {
                        fish.error({message:"选择的文件类型不符，只能上传图片类型文件(以gif|jpg|jpeg|png结尾的文件)", modal:true});
                        return;
                    }
                },
                fail: function (e, data) {
                    $('<p/>').text("文件上传失败  " + data.errorThrown).appendTo('#files');
                },
                always:function(e,data){
                    upDDKLoadResult.push(data.result);
                },
                add: function (e, data) {
                    if (data.files[0].size > maxFileSize) {
                        fish.warn('选择的文件大小超过最大限制，请重新选择');
                        return;
                    }
                    if (fileList != null && fileList.files.length >= maxFileCount) {
                        fish.warn('选择的文件个数过多，请减少文件数');
                        return;
                    }
                    var obj = data.files[0],
                        fileName = decodeURIComponent(obj.name),
                        fileSize = (obj.size / 1024.0).toFixed(2) + "KB";
                    fileList = data;
                    $('#BigSpeedAttachment_down').append(fish.compile(FILE_LIST_DDK)({
                        fileName: fileName,
                        fileSize: fileSize
                    }));

                    $('.file-remove-ddk').off('click').on('click', function (e) {
                        var name = $(e.currentTarget).prev().children(".fileName").text(), pos,pos2;
                        $.each(fileList.files, function (index, file) {
                            if (file.name === name) {
                                pos = index;
                                return false;
                            }
                        });
                        var delFileName="";
                        //获取删除upDDKLoadResult中对应附件信息的索引
                        for (var i = 0; i < upDDKLoadResult.length; i++) {
                            if(upDDKLoadResult[i][0].fileName==name){
                                delFileName=upDDKLoadResult[i][0].fileId+"."+upDDKLoadResult[i][0].fileType;
                                pos2=i;
                            }
                        }

                        fileList.files.splice(pos, 1);
                        upDDKLoadResult.splice(pos2, 1);
                        $(e.currentTarget).parent().remove();
                        var fileInfo = new Object();
                        fileInfo.filePath = 'createbuss';
                        fileInfo.fileName = delFileName;
                        digitalCircutAction.delFileOnFtp(fileInfo);
                    });
                    fileList.url = urlUpLoad;
                    var result = fileList.submit();//自动上传
                }
            })
        },

    });
})