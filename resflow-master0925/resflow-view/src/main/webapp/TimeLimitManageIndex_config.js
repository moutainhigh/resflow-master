/**
 * 主页require配置文件主入口
 */
require.config({
    urlArgs: 'v=20201120',
    waitSeconds:0
});
require(['frame/ext/ngc'],function(n){

    ngc.getLanguageFun(function (lang) {
        lang = lang == "${frame.message.language}"? 'zh_CN':lang;
        fish.cookies.set('frame_language', lang);
        var _lang = lang;
        if(lang.indexOf('_') != -1){
            _lang = lang.substring(0, lang.indexOf('_'));
        }
        fish.setLanguage(_lang);
        require(['frm/fish-desktop/locale/fish-desktop-locale.'+_lang], function () {
            fish.setLanguage(_lang);
            require(['i18n!frame/i18n/Constant.i18n',], function (queryI18n) {
                ngc.constant = function (key) {
                    return queryI18n[key];
                }
            }) });

        require(["module/component/utils/LoginChange"],function(k){
        require(['module/flow/spec/time/limit/view/TimeLimitManage',
            'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n'
        ], function (MainView,portalViewi18n) {
            this.$("#title").html(portalViewi18n.RESOURCE_MANAGEMENT_HOMEPAGE);
            new MainView({el:'#main_index_content'}).render();
        })
        })
    })
});
