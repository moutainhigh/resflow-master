/**
 * by ma.furong
 * 2018-10-12
 */
define(["text!module/UnicomLocalNet/resmaster/portal/homePage/templates/helpFileView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        "module/UnicomLocalNet/resmaster/portal/homePage/action/qryAction",
        "css!module/UnicomLocalNet/resmaster/portal/homePage/css/NavView.css"],
    function(template,portalViewi18n,qryAction) {
        var MainView = ngc.View.extend({
            template : ngc.compile(template),
            events:{
                'click #help01':'download01',
                'click #help02':'download02',
                'click #help03':'download03',
                'click #help04':'download04',
                'click #help_duanxin':'help_duanxin',
            },
            initialize : function() {
                this.render();
            },
            render : function() {
            	this.$el.append(this.template(portalViewi18n));
            },
            afterRender : function() {

            },
            download01:function () {
                var fileData = {
                    FILE_NAME:'OSS2.0资源调度系统操作手册-本地调度系统分册.V1（省分）.docx',
                    FILE_PATH:'helpfile',
                    FILE_ID:'help01',
                    FILE_TYPE:'docx'
                }
                qryAction.downLoadAttachMent("localScheduleLT/ApplOrder/attachmentDownload.spr", fileData);
            },
            download02:function () {
                var fileData = {
                    FILE_NAME:'OSS2.0资源调度系统操作手册-二干调度系统分册.V1（省分）.docx',
                    FILE_PATH:'helpfile',
                    FILE_ID:'help02',
                    FILE_TYPE:'docx'
                }
                qryAction.downLoadAttachMent("localScheduleLT/ApplOrder/attachmentDownload.spr", fileData);
            },
            download03:function () {
                var fileData = {
                    FILE_NAME:'V8本地网调度流程V1.4.pdf',
                    FILE_PATH:'helpfile',
                    FILE_ID:'help03',
                    FILE_TYPE:'pdf'
                }
                qryAction.downLoadAttachMent("localScheduleLT/ApplOrder/attachmentDownload.spr", fileData);
            },
            download04:function () {
                var fileData = {
                    FILE_NAME:'V8二干调度流程图V1.8.pdf',
                    FILE_PATH:'helpfile',
                    FILE_ID:'help04',
                    FILE_TYPE:'pdf'
                }
                qryAction.downLoadAttachMent("localScheduleLT/ApplOrder/attachmentDownload.spr", fileData);
            },
            help_duanxin:function () {
                var fileData = {
                    FILE_NAME:'短信开通配置文档.docx',
                    FILE_PATH:'helpfile',
                    FILE_ID:'help_duanxin',
                    FILE_TYPE:'docx'
                }
                qryAction.downLoadAttachMent("localScheduleLT/ApplOrder/attachmentDownload.spr", fileData);
            },
        });
        return MainView;
    });

