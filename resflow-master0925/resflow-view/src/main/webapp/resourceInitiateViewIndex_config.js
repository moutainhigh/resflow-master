/**
 * 主页require配置文件主入口
 */
require.config({
    waitSeconds:0
});
require(['frame/ext/ngc'],function(n){
    require(["module/component/utils/LoginChange"],function(k){
        require(['module/UnicomLocalNet/resmaster/portal/resourceInitiate/views/resourceInitiateView.js',
            'i18n!module/UnicomLocalNet/resmaster/portal/resourceInitiate/i18n/resourceInitiateView.i18n'
        ], function (MainView,portalViewi18n) {
            this.$("#title").html(portalViewi18n.GOM_CIRCUITCODE_ORDER);
            new MainView({el:'#main_index_content'});
        })
    })
});
