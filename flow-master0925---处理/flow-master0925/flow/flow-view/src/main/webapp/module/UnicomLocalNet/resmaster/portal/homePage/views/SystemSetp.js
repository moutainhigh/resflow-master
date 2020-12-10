define(["text!module/UnicomLocalNet/resmaster/portal/homePage/templates/SystemSetp.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        "module/component/views/TabView",
        "module/UnicomLocalNet/resmaster/portal/homePage/action/qryAction",
        "css!module/UnicomLocalNet/resmaster/portal/homePage/css/IndexView.css"],
    function(template,portalViewi18n,TabView,qryAction) {
        var MainView = ngc.View.extend({
            template : ngc.compile(template),
            events:{
                'click #resMange':'clickResMange'
            },
            initialize : function() {
                this.render();
                this.data=[
                    {
                        title: "派发规则管理",
                        id: "ab10",
                        hash: "javascript:;",
                        "icon": "glyphicon glyphicon-edit",
                        "url": "DispatchRulesManagementIndex.html?navType=no"
                    },
                    {
                        title: "时限预警管理",
                        id: "ab11",
                        hash: "javascript:;",
                        "icon": "glyphicon glyphicon-edit",
                        "url": "TimeLimitManageIndex.html?navType=no"
                    },
                    {
                        title: "用户管理",
                        id: "ab12",
                        hash: "javascript:;",
                        "icon": "glyphicon glyphicon-edit",
                        "url": "userManageIndex.html?navType=no"
                    },
                    {
                        title: "权限组管理",
                        id: "ab13",
                        hash: "javascript:;",
                        "icon": "glyphicon glyphicon-edit",
                        "url": "userGroupManageIndex.html?navType=no"
                    },
                    {
                        title: "权限管理",
                        id: "ab14",
                        hash: "javascript:;",
                        "icon": "glyphicon glyphicon-edit",
                        "url": "authmanageIndex.html?navType=no"
                    },
                    {
                        title: "工作日管理",
                        id: "ab15",
                        hash: "javascript:;",
                        "icon": "glyphicon glyphicon-edit",
                        "url": "WorkingDateManageIndex.html?navType=no"
                    },
                    {
                        title: "管理员设置",
                        id: "ab16",
                        hash: "javascript:;",
                        "icon": "glyphicon glyphicon-edit",
                        "url": "AdminSettingIndex.html?navType=no"
                    },
                    {
                        title: "资源预占自动释放时长设置",
                        id: "ab17",
                        hash: "javascript:;",
                        "icon": "glyphicon glyphicon-edit",
                        "url": "resDisassembleIndex.html?navType=no"
                    },
                    {
                        title: "业务申请权限管理",
                        id: "ab17",
                        hash: "javascript:;",
                        "icon": "glyphicon glyphicon-edit",
                        "url": "bussRoleApplyIndex.html?navType=no"
                    }
                ];
            },
            render : function() {
                this.$el.append(this.template(portalViewi18n));
                //获取用户信息
                var userInfo = qryAction.queryStaffInfo().responseJSON.data;
                this.$("#userName").html(userInfo.userName);
                var URL = this.getRootPath();
                //校验是否是管理员
                var parms = qryAction.queryAdminInfo(userInfo.userId).responseJSON.data
                if(!parms.result){
                    window.location.href=URL + "/UnicomLocalNetIndex.html";
                }
            },
            afterRender : function() {
                this.requireView({
                    selector:"#navDiv",
                    url:"module/UnicomLocalNet/resmaster/portal/homePage/views/NavView"
                });
                this.$("#tabContent").height($(window).height()-42);
                this.$(".mainDiv .left").height($(window).height()-42);
                this.initSidebar();
                this.initResTree();
                this.initTab();

                $(window).resize(function () {          //当浏览器大小变化时
                    this.$("#tabContent").height($(window).height() - 42);
                    this.$(".mainDiv .left").height($(window).height() - 42);
                });
            },
            initSidebar:function () {
                var that =this;
                var data = this.data;
                $('#sidebar').pagesidebar({
                    data: data,
                    width:200,
                    minWidth :50,
                    openFirst: true,// false
                    showToggleBtn: true,
                    children: "subMenus",
                    subMenuMode:"inline",
                    select: function (e,data) {
                        that.openTab(data);
                    }
                });

            },
            initResTree:function () {

            },
            clickResMange:function () {
                var dom = $(".mainDiv .left .resTree");
                if(dom.css("display")=="none"){
                    dom.css("display","block");
                    $("#res_arrow").removeClass("glyphicon-menu-right");
                    $("#res_arrow").addClass("glyphicon-menu-down");
                }else{
                    dom.css("display","none");
                    $("#res_arrow").removeClass("glyphicon-menu-down");
                    $("#res_arrow").addClass("glyphicon-menu-right");
                }
            },
            initTab:function () {
                var that = this;
                var _tabView = new TabView();
                this.setView("#tabContent", _tabView);
                this.renderViews("#tabContent");
                window.contentTabView = _tabView;
                _tabView.on("viewRenderAfter", function() {
                    _tabView.openFrameView('ab10', '派发规则管理', 'DispatchRulesManagementIndex.html?navType=no', true, false, null, false);
                });
            },
            openTab:function (data) {
                var id = data.id;
                //如果是表单一级菜单不打开
                if (id === "a27" || id === "a34" || id === "a35") {
                    return;
                }
                var title = data.title;
                var url = data.url;
                var context = ngc.getContext();
                var origin = window.location.origin;
                var _url = origin+context+"/"+url;
                this.getView("#tabContent").openFrameView(id, title, _url, true, true, null, false);
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

