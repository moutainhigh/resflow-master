define(['i18n!modules/index/i18n/index'], function(i18n) {
    // begin 以下代码作为示例提供
    // portal.appEvent.onDomReady(function($def){
    //     console.log("onPortalDomReady trigger ......");
    //     $def.resolve();
    // });

    // portal.appEvent.onWorkspaceInit(function($def){
    //     console.log("onWorkspaceInit trigger ......");
    //     $def.resolve();
    // });
    //
    //

    // portal.appEvent.onIframeMenuOpen(function($def,oldUrl){
    //     // portal.appGlobal.get("currentMenu") 可以获取到对应的菜单,根据菜单编码来干一些事情
    //     console.log("onIframeMenuOpen trigger ......");
    //     // $def.resolve(new url);
    //     // do something about url&token
    //     $def.reject("new url"); //可以通过reject(),不再走框架的默认逻辑
    // });
    // end

    var IndexView = portal.BaseView.extend({
        initialize: function() {
            portal.appGlobal.on('change:currentStatus', this.currentStatusChange, this); // 监听menuView的菜单加载完的事件
            document.title = i18n.PROJECT_TITLE;
            this.portalView = null;
        },

        index: function() {
            var that = this;
            // debugger;
            fish.get('logged', function(data) {
                if (!data.userCode) {
                    // 根据配置项加载不同的登录页
                    require([data.loginPage], function(LoginView) {
                        new LoginView().render();
                    });
                } else {
                    portal.appGlobal.set('userId', data.userId);
                    portal.appGlobal.set('userCode', data.userCode);
                    portal.appGlobal.set('userName', data.userName);
                    portal.appGlobal.set('portalId', data.portalId);
                    portal.appGlobal.set('leftMenu', data.leftMenu);
                    portal.appGlobal.set('defaultUnfold', data.defaultUnfold);
                    if (fish.cookies.get('INIT_PORTAL') !== undefined) {
                        portal.appGlobal.set('portalId', fish.cookies.get('INIT_PORTAL'));
                        portal.appGlobal.set('extraUrl', fish.cookies.get('PORTAL_EXTRAL_URL'));
                        fish.cookies.remove('INIT_PORTAL');
                        fish.cookies.remove('PORTAL_EXTRAL_URL');
                    } else {
                        portal.appGlobal.set('portalId', data.portalId);
                        portal.appGlobal.set('extraUrl', data.extraUrl);
                    }

                    if (data._csrf && data._csrf.token) {
                        portal.appGlobal.set('_csrf', data._csrf.token);
                        portal.appGlobal.set('_csrf_header', data._csrf.headerName);
                        portal.appGlobal.set('_csrf_parameterName', data._csrf.parameterName);
                    }

                    // 将staffjob的选择提前，避免每次选门户的时候触发
                    portal.appEvent.triggerEnterBefore().done(function() {
                        //初始化触发一次，这样切换portal刷新页面的时候，方便业务做些事情。
                        portal.appGlobal.trigger('change:currentStatus');
                    });
                }
            });
        },

        currentStatusChange: function() {
            // debugger;
            // 登录状态改变
            var portalId;
            var extraUrl;
            if (portal.appGlobal.get('currentStatus') === 'sessionTimeOut') {
                fish.store.set('reLogin', i18n.SESSION_TIME_OUT_REASON);
                window.location.href = portal.appGlobal.get('webroot');
            } else if (portal.appGlobal.get('currentStatus') === 'beenKickedFromLogin') {
                fish.store.set('reLogin', i18n.SESSION_TIME_OUT_BEEN_KICKED);
                window.location.href = portal.appGlobal.get('webroot');
            } else {
                //第一次进入portal应该取请求中的extraUrl，后面不是刷新切换门户时，则从对应的jquery元素中取
                if (!portal.appGlobal.has('extraUrl')) {
                    portalId = portal.appGlobal.get('portalId');
                    extraUrl = $('.portalMenu')
                    .find('[data-id=' + portalId + ']')
                    .data('extraurl');
                }
                else {
                    extraUrl = portal.appGlobal.get('extraUrl');
                    portal.appGlobal.unset('extraUrl');
                }
                this.mainViewRender({ "extraUrl": extraUrl });
            }
        },

        mainViewRender: function(data) {
            // debugger;
            var that = this;
            // extraUrl字段不用到此处了，后面考虑通过门户编码来实现
            // var portalId = portal.appGlobal.get("portalId");
            // 根据portalId获取extraUrl; TODO 第一个门户,extraUrl取不到,不应该通过portalMenu来取,而在设置默认portalId的同时查出来
            // var extraurl = $(".portalMenu").find("[data-id="+portalId+"]").data("extraurl");
            /*if (data.extraUrl && data.extraUrl !== 'undefined') {
                require([data.extraUrl], function() {
                    require(['modules/portal/views/PortalView'], function(PortalView) {
                        if (that.portalView !== null) {
                            that.portalView.remove();
                            that.portalView = new PortalView(data)
                            $('body').html(that.portalView.$el);
                            that.portalView.render();
                        } else {
                            that.portalView = new PortalView(data);
                            $('body').html(that.portalView.$el);
                            that.portalView.render();
                        }
                    });
                });
            } else {*/

            window.location.href = "UnicomLocalNetIndex.html";
/*                require(['modules/portal/views/PortalView'], function(PortalView) {
                    if (that.portalView !== null) {
                        that.portalView.remove();
                        that.portalView = new PortalView(data)
                        $('body').html(that.portalView.$el);
                        that.portalView.render();
                    } else {
                        that.portalView = new PortalView(data);
                        $('body').html(that.portalView.$el);
                        that.portalView.render();
                    }
                });*/
            }
        }
    //}
    );

    return IndexView;
});
