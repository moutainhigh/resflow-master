/**
 * 主页require配置文件主入口
 */
require.config({
    urlArgs: 'v=20201123',
    waitSeconds:0
});
require(['frame/ext/ngc'],function(n){
    require(["module/component/utils/LoginChange"],function(k){
        require(['module/UnicomLocalNet/resmaster/portal/local/views/unicomLocalOrderView',
                 'i18n!module/UnicomLocalNet/resmaster/portal/local/i18n/unicomLocalOrderView.i18n'
                 ], function (MainView,unicomlocalViewi18n) {
        this.$("#title").html(unicomlocalViewi18n.UNICOM_LOCAL_ORDER);
        new MainView({el:'#main_index_content'});
     })
    })
});
