define(['module/UnicomLocalNet/resmaster/portal/homePage/action/AdminInfoAction',
    'text!module/UnicomLocalNet/resmaster/portal/systemSetup/templates/addAdminInfo.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/taskmanagement.css'
], function(adminInfoAction,adminSetting,i18n,css) {
    var paramsMap = new Object();
    var URl;
    var userId;

    return fish.View.extend({
        resNetworkUrl: '',
        crmRegion: '',
        userInfo: new Object(),
        template: fish.compile(adminSetting),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #save-button': 'saveButton',
            'click #cancel-button': 'cancelButton',
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
            userInfo = adminInfoAction.queryStaffInfo().responseJSON.data;
            //"350002000000000042766427" 47274" "448401"
            $("#inputDemo").popedit({
                initialData: {
                    'name': '请选择添加人员！',
                    'value': ''
                },
                open:function(e) {
                    var _this = $(this);
                    var options = {
                        url: 'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/views/transferView',
                        height: 460,
                        width: 700,
                        modal: false,
                        draggable: false,
                        autoResizable: true,
                        viewOption: {
                            flag : "transferStaff",
                            currentAreaId : userInfo.areaId,
                            currentOrgId : userInfo.orgId,
                            currentUserId : userInfo.userId,
                            currentType : "addAdmin",
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                var orgNames = '';
                                var orgIds = '';
                                var objType = '';

                                res.forEach(function(val,i){
                                    if (i == 0) {
                                        orgNames = val.name ;
                                    }
                                    orgIds = val.id;
                                    objType = val.objType;
                                })
                                _this.popedit('setValue', {name:orgNames, value:orgIds, objType:objType});
                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    };
                    var popup = fish.popupView(options);
                }
            });
        },
        saveButton:function(){
            var obj = $("#inputDemo").popedit('getValue');
            if(obj.value==null || obj.value==''){
                fish.toast('info', '请选择添加人员');
            }else{
                var params =new Object();
                params.staffId=obj.value;
                params.userName=obj.name;
                params.userId=userId;
                adminInfoAction.addAdminInfo(params,function (datas) {
                    var result = datas.result;
                    var message = datas.message;
                    fish.toast('info', message);
                });
                this.popup.close();
            }
        },
        cancelButton:function(){
            this.popup.close();
        },
        //浏览器窗口大小改变事件
        resize: function() {
            //$("#orderDeal-grid").grid("resize",true);
            var frameHeight = document.documentElement.scrollHeight;
            $("#orderDeal-grid").grid("setGridHeight", frameHeight - 235);
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
    });
});