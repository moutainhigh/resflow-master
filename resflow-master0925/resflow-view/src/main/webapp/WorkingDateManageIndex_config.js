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
            require(['module/flow/tool/workingDate/view/WorkingDateManage',
            'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n'
        ], function (MainView,portalViewi18n) {
            this.$("#title").html(portalViewi18n.RESOURCE_MANAGEMENT_HOMEPAGE);
            new MainView({el:'#main_index_content'}).render();
        })
        })
    })
});
