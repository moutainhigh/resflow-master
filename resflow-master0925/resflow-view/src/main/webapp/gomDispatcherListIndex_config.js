/**
 * 主页require配置文件主入口
 */
require.config({
    urlArgs: 'v=20201120',
    waitSeconds:0
});
require(['frame/ext/ngc'],function(n){
    require(["module/component/utils/LoginChange"],function(k){
        require(['module/UnicomLocalNet/resmaster/portal/gomDispatcher/views/gomDispatcherListView',
            'i18n!module/UnicomLocalNet/resmaster/portal/gomDispatcher/i18n/gomDispatcherListView.i18n'
        ], function (MainView,portalViewi18n) {
            this.$("#title").html(portalViewi18n.GOM_DISPATCHER_ORDER);
            new MainView({el:'#main_index_content'});
        })
    })
});
