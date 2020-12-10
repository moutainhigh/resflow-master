/**
 * sd_wan 终端信息填写页面
 */
define(['text!module/UnicomLocalNet/resmaster/portal/sdwan/templates/configWANView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'module/UnicomLocalNet/resmaster/portal/sdwan/action/sdwanAction',
    "css!module/UnicomLocalNet/resmaster/portal/orderLocalStandby/styles/localStandby.css"
], function(configWANView,portalViewi18n,sdwanAction,css) {
    var level;
    return fish.View.extend({
        template: fish.compile(configWANView),
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
            this.initCombobox();
        },
        initCombobox:function(){
            var params = new Object();
            var factory, model= null;
            var srvOrdId = this.options.srvOrdId;
            //查询设备档次 和设备厂家以及 型号  赋值
            params.srvOrdId = srvOrdId;
            var ii = sdwanAction.queryWanInfo(params,function (data) {
                var userAcct = data[0].USERACCT;
                var userPwd = data[0].USERPWD;
                var WANTypeName = data[0].WANTYPENAME;
                var IPAddr = data[0].IPADDR;
                var gatewayAddr = data[0].GATEWAYADDR;

                //初始化用户名，密码，wan配置，IP地址，网关地址
                $('#userAcct').val(userAcct);
                $('#userPwd').val(userPwd);
                $('#WANType').val(WANTypeName);
                $('#IPAddr').val(IPAddr);
                $('#gatewayAddr').val(gatewayAddr);

                params.enumType='WANType'
                //设置wan口配置
                sdwanAction.queryEnum(params,function (data) {
                    var array = new Array();
                    array =data;
                    var enumTypeMap = new Map();
                    enumTypeMap = array[array.length-1];
                    var enumTypeB = "."+enumTypeMap.enumCode;
                    var obj = $(enumTypeB);
                    data.splice(data.length-1,1);//删除list中最后一个元素。
                    var $WANType = $(obj).combobox({
                        placeholder: WANTypeName,
                        dataSource:data,
                        editable:true,
                    });

                    //add by wang.gang2 DHCP 需要将别的文本框置灰清空
                    $WANType.on('combobox:change', function () {
                        console.log($WANType.combobox('value'))
                        if($WANType.combobox('value')==='4'){
                            $('#userAcct').clearinput();
                            $('#userAcct').attr("disabled",true);
                            $('#userPwd').clearinput();
                            $('#userPwd').attr("disabled",true);
                            $('#IPAddr').clearinput();
                            $('#IPAddr').attr("disabled",true);
                            $('#gatewayAddr').clearinput();
                            $('#gatewayAddr').attr("disabled",true);
                        }else{
                            $('#userAcct').attr("disabled",false);
                            $('#userPwd').attr("disabled",false);
                            $('#IPAddr').attr("disabled",false);
                            $('#gatewayAddr').attr("disabled",false);
                        }
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
            params.userAcct=formData.userAcct;
            params.userPwd=formData.userPwd;
            params.WANType=formData.WANType;
            params.IPAddr=formData.IPAddr;
            params.gatewayAddr=formData.gatewayAddr;
            $("#orderOper-form").blockUI({message: '保存中...'}).data('blockui-content', true);
            sdwanAction.saveWanInfo(params,function (res) {
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
