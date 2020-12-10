define(['module/UnicomLocalNet/resmaster/portal/homePage/action/AdminInfoAction',
    'text!module/UnicomLocalNet/resmaster/portal/systemSetup/templates/addDisassembleSet.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/taskmanagement.css'
], function(adminInfoAction,addDisassembleSet,i18n,css) {
    var paramsMap = new Object();
    var URl;
    var userId;
    var userInfo;

    return fish.View.extend({
        resNetworkUrl: '',
        crmRegion: '',
        userInfo: new Object(),
        template: fish.compile(addDisassembleSet),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #save-button': 'saveButton',
            'click #cancel-button': 'cancelButton',
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
            userInfo = adminInfoAction.queryStaffInfo().responseJSON.data;

            $('#spinner').spinner({
                page:10, //默认步长是1,可以由键盘上下键控制;这里的page支持键盘pgup与pgdn键,每次步长10
                min:0,
            });
            $('#productType').combobox({
                placeholder: '--请选择产品类型--',
                dataTextField: 'name',
                dataValueField: 'value',
                dataSource: [
                    {name: '语音中继电路', value: '20181211001'},
                    {name: 'MPLS-VPN', value: '10000008'},
                    {name: '数字电路', value: '10000001'},
                    {name: '裸光纤', value: '20181221002'},
                    {name: '基础数据(ATM)', value: '20181221003'},
                    {name: '基础数据(FR)', value: '20181221004'},
                    {name: '以太网专线', value: '10000002'},
                    {name: '互联网专线(DIA)', value: '10000011'},
                    {name: '基础数据(DDN)', value: '20181221005'},
                    {name: '局内中继电路', value: '20181221006'},
                ]
            });

        },
        saveButton:function(){
            var product= $("#productType").combobox('getSelectedItem');
            var productType = product.name;
            var serviceId = product.value;
            var spinner =$("#spinner").val();
            if(productType == null || productType =='' || spinner == '' || spinner == null){
                fish.toast('info', '请选择产品类型及时长');
            }else{
                var params =new Object();
                params.productType = productType;
                params.serviceId = serviceId;
                params.spinner = spinner;
                params.areaId= userInfo.areaId;
                adminInfoAction.addDisassemble(params,function (datas) {
                    var result = datas.result;
                    var message = datas.message;
                    fish.toast('info', message);
                });
                this.popup.close();
            }
        },
        cancelButton:function(){
            this.popup.close();
        },
        //浏览器窗口大小改变事件
        resize: function() {
            //$("#orderDeal-grid").grid("resize",true);
            var frameHeight = document.documentElement.scrollHeight;
            $("#orderDeal-grid").grid("setGridHeight", frameHeight - 235);
        },
        getRootPath:function (){
            //获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
            var curWwwPath=window.document.location.href;
            //获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
            var pathName=window.document.location.pathname;
            var pos=curWwwPath.indexOf(pathName);
            //获取主机地址，如： http://localhost:8083
            var localhostPaht=curWwwPath.substring(0,pos);
            //获取带"/"的项目名，如：/uimcardprj
            var projectName=pathName.substring(0,pathName.substr(1).indexOf('/')+1);
            return (localhostPaht+projectName);
        },
    });
});