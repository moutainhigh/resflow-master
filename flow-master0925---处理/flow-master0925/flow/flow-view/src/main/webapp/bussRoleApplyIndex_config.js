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
                })
            });
            require(['module/UnicomLocalNet/resmaster/portal/bussApply/views/personManage'],
                function (MainView) {
                    new MainView({el:'#main_index_content_1'}).render();
            });

            require(['module/UnicomLocalNet/resmaster/portal/bussApply/views/groupManage'],
                function (MainView) {
                    new MainView({el:'#main_index_content_2'}).render();
            });
        });
    })
});
