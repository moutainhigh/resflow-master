/**
 * 路由信息
 */
define([
    'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/action/localOrderSelectAction',
    'text!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/templates/routingInfoView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/css/routingInfoView.css'
], function(localOrderSelectAction,routingInfoView,i18n,css) {

    return fish.View.extend({
        resNetworkUrl: '',
        crmRegion: '',
        template: fish.compile(routingInfoView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #close': 'close',

        },
        initialize: function() {
            this.render();
        },
        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },
        afterRender: function() {
            //初始化grid
            this.initGrid();
            //查询路由信息
            this.initRouteInfo();

        },


        initGrid:function(){
            var me = this;
            $("#routeGrid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'ROUTE_NO',width:300,label:'路由编号' },
                    {name: 'ROUTE_NAME',width:300,label:'路由名称', align: 'left' },
                    {name: 'ROUTE_TYPE',width:300,label:'路由类型', align: 'left'}
                ],
                curPageSort: false,
                height:240,
                // datatype: 'json',
                recordtext:"{0}-{1} 共{2}条",
                pgtext: " 第{0}页/共{1}页",
                rowtext: "每页{0}条",
                rowNum: 10,
                rowList: [10,20,50,100,200,500],
                pager: true,
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                multiselect: false,
                shrinkToFit: false,
                autoResizable: true,
                showColumnsFeature: false, //允许用户自定义列展示设置
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: function (e, rowid, iRow, iCol) {
                    me.queryRouteInfo(e, rowid, iRow, iCol);
                }.bind(this),
            });
        },
        initRouteInfo:function(){
            //填充grid
            $('#routeGrid').grid("reloadData", this.options.res.souteInfo);
            //填充form
            this.insertPropertyForm(this.options.res.propertyInfo);
        },
        close: function() {
            this.popup.close();
        },
        insertPropertyForm:function(param){
            document.getElementById("CUST_NAME").innerText=param.CUST_NAME;
            document.getElementById("LINK_TELE").innerText=param.LINK_TELE;
            document.getElementById("ADDRESS").innerText=param.ADDRESS;
            document.getElementById("LINK_MAN").innerText=param.LINK_MAN;
            document.getElementById("LINK_TELE2").innerText=param.LINK_TELE;
            document.getElementById("PRODUCT_NO").innerText=param.PRODUCT_NO;
            document.getElementById("BUSINESS_IDENTITY").innerText=param.BUSINESS_IDENTITY;
            document.getElementById("CIRCUIT_NO").innerText=param.CIRCUIT_NO;
            document.getElementById("CIRCUIT_RATE").innerText=param.CIRCUIT_RATE;
            document.getElementById("A_RESISTANCE").innerText=param.A_RESISTANCE;
            document.getElementById("A_ADDRESS").innerText=param.A_ADDRESS;
            document.getElementById("LONG_INTER_PORT").innerText=param.LONG_INTER_PORT;
            document.getElementById("A_LINK_MAN").innerText=param.A_LINK_MAN;
            document.getElementById("A_LINK_TELE").innerText=param.A_LINK_TELE;
            document.getElementById("SYS_EMS_NAME").innerText=param.SYS_EMS_NAME;
            document.getElementById("Z_ADDRESS").innerText=param.Z_ADDRESS;
            document.getElementById("SYS_NETWORK_GW").innerText=param.SYS_NETWORK_GW;
            document.getElementById("Z_LINK_MAN").innerText=param.Z_LINK_MAN;
            document.getElementById("Z_LINK_TELE").innerText=param.Z_LINK_TELE;
        }
    });
});