/**
 *
 */
define(['text!module/UnicomLocalNet/resmaster/portal/checkFlow/templates/pop/autoCheckView.html',
    'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!src/main/webapp/module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/orderDetailsView.css'
], function(autoCheckView,orderDetailsAction,i18n,css) {
    return fish.View.extend({
        template: fish.compile(autoCheckView),
        i18nData: fish.extend({}, i18n),
        events: {

        },
        initialize: function () {
            this.render();
        },
        //渲染页面
        render: function () {
            this.$el.html(this.template(this.i18nData));
        },
        //初始化fish组件
        afterRender: function () {
            //判断是否具备资源
            var CHK_CODE = 1;
            if(CHK_CODE === 1){

            }else{

            }
            this.initGrid();
        },
        // 资源列表
        initGrid: function (e) {
            var opt = {
                data: this.initCheckInfo(),
                height: 'auto',
                autowidth:true,
                shrinkToFit:false,
                colModel: [
                    {name: 'STD_ADDRESS', label: '标准地址GID', width: 110, sortable: false},
                    {name: 'STD_ADDRESS_FULLNAME', label: '标准地址全称', width: 110, sortable: false},
                    {name: 'RES_TYPE', label: '资源类型',  width: 100,sortable: false},
                    {name: 'RES_NAME', label: '资源名称',  width: 100,sortable: false},
                    {name: 'WGS84_X', label: 'WGS84经度', width: 110, sortable: false},
                    {name: 'WGS84_Y', label: 'WGS84纬度', width: 110, sortable: false},
                    {name: 'LENGTH', label: '皮长公里', width: 100, sortable: false},
                    {name: 'CORE_NUM', label: '纤芯总数', width: 100, sortable: false},
                    {name: 'CORE_FREE_NUM', label: '纤芯可用数', width: 110, sortable: false},
                    {name: 'TERM_NUM', label: '端子总数', width: 100, sortable: false},
                    {name: 'TERM_FREE_NUM', label: '端子可用数', width: 100, sortable: false},
                    {name: 'PORT_NUM', label: '端口总数', width: 100, sortable: false},
                    {name: 'PORT_FREE_NUM', label: '端口可用数', width: 110, sortable: false},
                    {name: 'PORT_MAX_RATE', label: '端口最大速率', width: 110, sortable: false},
                ]
                , rowNum: 10
                , rowList: [5, 10, 20]
                , pager: true
            };
            this.$("#ResGrid").grid(opt);
        },
        // 初始化自动核查页面并查询数据
        initCheckInfo : function () {
            var me = this;
            me.queryCheckInfo();
        },
        queryCheckInfo : function () {
            var me = this;
            var queryParam={};
            queryParam.srvOrdId=me.options.srvOrdId;
            orderDetailsAction.queryAutoCheckInfo(queryParam,function (res) {
                if(res.success){
                    var dataArray = res.data;
                    console.error(dataArray);
                    for(let key in dataArray){
                        var value = dataArray[key];
                        if (key=="CHK_CODE" || key == "CHK_LIMIT_TIME" || key=="CHK_MESS" || key == "CHK_RES_TXT" ){
                            $("#"+key).text(value);
                        }
                    }
                    $("#ResGrid").grid("reloadData", dataArray.chkResList);
                } else{
                    fish.toast('error', res.message);
                }
            });
        }
    })
});