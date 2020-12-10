define(['module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/action/acceptAreaAction.js',
    'text!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/templates/acceptAreaView.html',
    'i18n!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/i18n/acceptArea.i18n',
    'css!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/css/acceptArea.css'
], function(acceptAreaAction,acceptAreaView,i18n,css) {
    return fish.View.extend({
        template: fish.compile(acceptAreaView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submit': 'submit',
        },

        initialize: function() {
            this.render();
        },

        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },

        //初始化fish组件
        afterRender: function() {
            var options = {
                placeholder: "请选择发生地区",
                check: {
                    enable: true,
                    chkboxType: {"Y":"", "N":""}
                },
                dropdownWidth:1000,
                searchFilter:true,
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onCheck: onCheck
                },
                fNodes:[
                    {id:1, pId:0, name:"北京"},
                    {id:2, pId:0, name:"天津"},
                    {id:3, pId:0, name:"上海"},
                    {id:6, pId:0, name:"重庆"},
                    {id:4, pId:0, name:"河北省", open:true, nocheck:true},
                    {id:41, pId:4, name:"石家庄"},
                    {id:42, pId:4, name:"保定"},
                    {id:43, pId:4, name:"邯郸"},
                    {id:44, pId:4, name:"承德"},
                    {id:5, pId:0, name:"广东省", open:true, nocheck:true},
                    {id:51, pId:5, name:"广州"},
                    {id:52, pId:5, name:"深圳"},
                    {id:53, pId:5, name:"东莞"},
                    {id:54, pId:5, name:"佛山"},
                    {id:6, pId:0, name:"福建省", open:true, nocheck:true},
                    {id:61, pId:6, name:"福州"},
                    {id:62, pId:6, name:"厦门"},
                    {id:63, pId:6, name:"泉州"},
                    {id:64, pId:6, name:"三明"}
                ]
            };

            $('#citySel').combotree(options);

            function onCheck(e, treeNode) {
                console.log("onClick defined in options. " + treeNode.name);
            }

            $('button').click(function (e) {
                var $target = $(e.target);

                switch ($target.attr('id')) {
                    case 'btn1':
                        console.log($('#citySel').combotree('value'));
                        break;
                    case 'btn2':
                        $('#citySel').combotree('value', ['上海','北京','广州']);
                        break;
                    case 'btn3':
                        $('#citySel').combotree('clear');
                        break;
                }
            });
        },
    }); //fish.View.extend END
}); //ALL END