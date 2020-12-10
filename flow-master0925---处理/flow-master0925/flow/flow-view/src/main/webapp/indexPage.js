/**
 * Created by zr on 2019/5/15.
 */
/**
 * 主页require配置文件主入口
 */
require.config({
    waitSeconds:0
});
require(['frame/ext/ngc'],function(n){
    require(["module/component/utils/LoginChange"],function(k){
        require(['module/UnicomLocalNet/resmaster/portal/subIndexPage/views/subIndexPage',
            'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n'
        ], function (MainView,portalViewi18n) {
            this.$("#title").html(portalViewi18n.RESOURCE_MANAGEMENT_HOMEPAGE);
            new MainView({el:'#index_content'});
        })
    })
});