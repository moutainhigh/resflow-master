/**
 * sd_wan 终端信息填写页面
 */
define(['text!module/UnicomLocalNet/resmaster/portal/sdwan/templates/terminalSynchView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'module/UnicomLocalNet/resmaster/portal/sdwan/action/sdwanAction',
    "css!module/UnicomLocalNet/resmaster/portal/orderLocalStandby/styles/localStandby.css"
], function(terminalSynchView,portalViewi18n,sdwanAction,css) {
    var level;
    return fish.View.extend({
        template: fish.compile(terminalSynchView),
        i18nData: fish.extend({}, portalViewi18n),
        events: {
            'click #submitBtn': 'submit',
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
            $("#terminalSynchViewTitle").text("填写终端");
            this.initCombobox();

        },
        initCombobox:function(){
            var params = new Object();
            var factory, model= null;
            var srvOrdId = this.options.srvOrdId;
            //查询设备档次 和设备厂家以及 型号  赋值
            params.srvOrdId = srvOrdId;
            var ii = sdwanAction.queryCircuitInfo(params,function (data) {
                level = data[0].DEVICELEVEL;
                var factory = data[0].DEVICEFACTORY;
                var model = data[0].DEVICEMODEL;
                var levelName = data[0].LEVELNAME;
                var factoryName = data[0].FACTORYNAME;
                var modelName = data[0].MODELNAME;
                var BSN = data[0].BSN;

                //初始化设置设备标准
                $('#deviceLevel').val(levelName);
                //初始化序列号
                $('#BSN').val(BSN);
                // $('#deviceLevel').combobox('disable');

                params.sortNo=level;
                params.relatedSign=level;
                params.enumType='deviceFactory2'
                //设置厂商
                sdwanAction.queryEnum(params,function (data) {
                    var array = new Array();
                    array =data;
                    var enumTypeMap = new Map();
                    enumTypeMap = array[array.length-1];
                    var enumTypeB = "."+enumTypeMap.enumCode;
                    var obj = $(enumTypeB);
                    data.splice(data.length-1,1);//删除list中最后一个元素。
                    $(obj).combobox({
                        placeholder: factoryName,
                        dataSource:data,
                        editable:true,
                    });
                });

               //初始化设备序号

                params.sortNo=factory;
                params.relatedSign=level;
                params.enumType='deviceModel'
                sdwanAction.queryEnum(params,function (data) {
                    var array = new Array();
                    array =data;
                    var enumTypeMap = new Map();
                    enumTypeMap = array[array.length-1];
                    var enumTypeB = "."+enumTypeMap.enumCode;
                    var obj = $(enumTypeB);
                    data.splice(data.length-1,1);//删除list中最后一个元素。
                    $(obj).combobox({
                        placeholder: modelName,
                        dataSource:data,
                        editable:true,
                    });
                });

           });
            // 修改厂商要重新加载设备型号
            $('#deviceFactory').on('combobox:change', function () {
                var value = $('#deviceFactory').combobox("value");
                console.log(value);
                params.sortNo=value;
                params.relatedSign=level;
                params.enumType='deviceModel'
                sdwanAction.queryEnum(params,function (data) {
                    var array = new Array();
                    array =data;
                    var enumTypeMap = new Map();
                    enumTypeMap = array[array.length-1];
                    var enumTypeB = "."+enumTypeMap.enumCode;
                    var obj = $(enumTypeB);
                    data.splice(data.length-1,1);//删除list中最后一个元素。
                    $(obj).combobox({
                        placeholder: "---请选择---",
                        dataSource:data,
                        editable:true,
                    });
                });

            });
        },

        //保存
        submit:function () {
            var me = this;
            var formData = $('#orderOper-form').form('value');
            var params = new Object();
            params.woId = me.options.woId;
            params.orderId = me.options.orderId;
            params.tacheId = me.options.tacheId;
            params.srvOrdId=me.options.srvOrdId;
            //设备标准是不允许变得
            params.deviceModel=formData.deviceModel;
            params.deviceFactory=formData.deviceFactory;
            params.BSN=formData.BSN;
            $("#orderOper-form").blockUI({message: '保存中...'}).data('blockui-content', true);
            sdwanAction.saveDeviceInfo(params,function (res) {
                if(res.success){
                    $("#orderOper-form").unblockUI().data('blockui-content', false);
                    fish.toast('success', res.message);
                    me.popup.close();
                }else {
                    $("#orderOper-form").unblockUI().data('blockui-content', false);
                    fish.toast('error', res.message);
                }
            });

        }
    });
});
