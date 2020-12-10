/**
 * by ma.furong
 * 2018-10-12
 */
define(["text!module/UnicomLocalNet/resmaster/portal/homePage/templates/NavView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        "module/UnicomLocalNet/resmaster/portal/homePage/action/qryAction",
        "css!module/UnicomLocalNet/resmaster/portal/homePage/css/NavView.css"],
    function(template,portalViewi18n,qryAction) {
        var chackFlag = true; //判断只能单击打开一个窗口 by ren.jiahang
        var MainView = ngc.View.extend({
            template : ngc.compile(template),
            events:{
                'click #logout':'logout',
                'click #systemSetup':'systemSetup',
                'click #helptxt':'helptxt'
            },
            initialize : function() {
                this.render();
            },
            render : function() {
            	this.$el.append(this.template(portalViewi18n));
            },
            afterRender : function() {
                //获取用户信息
                var userInfo = qryAction.queryStaffInfo().responseJSON.data;
                this.$("#userName").html(userInfo.userName);
                //校验是否是管理员
                var parms = qryAction.queryAdminInfo(userInfo.userId).responseJSON.data
                if(parms.result){
                    this.$("#systemSetup").show();
                }else{
                    this.$("#systemSetup").hide();
                }
            },
            logout:function () {
                ngc.confirm('确认是否注销').result.then(function() {
                    ngc.ajax({
                        type: "POST",
                        dataType: "json",
                        showMask: true, //是否采用默认的全局遮罩
                        async: true, //异步方式
                        url: "login.spr?method=loginOut",
                        success: function(data) {
                            debugger
                            if(data){
                                if(data.resultStat==='SUCCESS'){
                                    localStorage.clear();
                                    var _url = data.callBack;
                                    debugger
                                    fish.cookies.remove('INIT_PORTAL');
                                    if(_url == undefined
                                        || _url == ""){
                                        location.href = "";
                                    }else{
                                        location.href = _url;
                                    }
                                }else{
                                    ngc.error('退出失败，失败信息：'+data.mess);
                                }
                            }
                        }
                    });

                });
            },

            helptxt:function (e) {
                if(chackFlag){
                    chackFlag = false;
                    fish.popupView({
                        url: 'module/UnicomLocalNet/resmaster/portal/homePage/views/helpViewView',
                        width: 500,
                        height:180,
                        modal:false,
                        canClose:true,
                        resizable:true,
                        position:{my: "right top", at: "right top+40", of: window},
                        dismiss:function () {
                            chackFlag = true;
                        }
                    });
                }

            },
            systemSetup:function (e) {
                var url =this.getRootPath()+"/SystemSetpIndex.html";
                window.open(url,"_blank");
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
        return MainView;
    });

