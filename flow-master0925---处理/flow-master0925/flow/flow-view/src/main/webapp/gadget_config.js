/**
 * 主页require配置文件主入口
 */
require.config({
    waitSeconds:0
});
require(['frame/ext/ngc'],function(n){
    require(["module/component/utils/LoginChange"],function(k){
        require(['module/UnicomLocalNet/resmaster/portal/testHelpPage/views/testHelpView',
            'i18n!module/UnicomLocalNet/resmaster/portal/testHelpPage/i18n/testHelpView.i18n'
        ], function (MainView,testHelpView) {
            this.$("#title").html(testHelpView.RESOURCE_MANAGEMENT_HOMEPAGE);
            new MainView({el:'#main_index_content'});
        })
    })
});
