/**
 * 主页require配置文件主入口
 */
require.config({
    paths: {
        echarts: 'frame/fish/libs/echarts'
    },
    waitSeconds:0
});
require(['frame/ext/ngc'],function(n){
    require(["module/component/utils/LoginChange"],function(k){
        require(['module/UnicomLocalNet/resmaster/portal/networkAudit/views/networkAuditView.js',
                 'i18n!module/UnicomLocalNet/resmaster/portal/networkAudit/i18n/networkAuditView.i18n'
                 ], function (MainView,networkAuditViewi18n) {
        this.$("#title").html(networkAuditViewi18n.UNICOM_NETWORK_AUDIT);
        new MainView({el:'#main_index_content'});
     })
    })
});
