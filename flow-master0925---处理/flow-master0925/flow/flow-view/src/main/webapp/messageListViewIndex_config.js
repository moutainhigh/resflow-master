/**
 * 事务调单主页require配置文件主入口
 */
require(['frame/ext/ngc'], function (n) {
    ngc.getLanguageFun(function (lang) {
        lang = lang == "${frame.message.language}" ? 'zh_CN' : lang;
        fish.cookies.set('frame_language', lang);
        var _lang = lang;
        if (lang.indexOf('_') != -1) {
            _lang = lang.substring(0, lang.indexOf('_'));
        }
        fish.setLanguage(_lang);
        require(['frm/fish-desktop/locale/fish-desktop-locale.' + _lang], function () {
            fish.setLanguage(_lang);
            require(['i18n!frame/i18n/Constant.i18n',], function (queryI18n) {
                ngc.constant = function (key) {
                    return queryI18n[key];
                }
            })
        });
        require(["module/component/utils/LoginStatChange"], function (k) {
            require(['module/UnicomLocalNet/resmaster/portal/messageList/views/messageListView.js',
                'i18n!module/UnicomLocalNet/resmaster/portal/messageList/i18n/messageListView.i18n'
            ], function (patchIndex, messageListViewi18n) {
                this.$("#title").html(messageListViewi18n.MESSAGE_LIST);
                new patchIndex({el: '#main_index_content'});
            })
        })
    })


});
