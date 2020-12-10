define([
        'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/action/custnameSelectViewAction',
        'text!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/templates/custnameSelectView.html',
        'i18n!module/UnicomLocalNet/resmaster/portal/local/i18n/unicomLocalOrderView.i18n',
        'css!module/UnicomLocalNet/resmaster/portal/local/css/unicomLocalOrderView.css'],
    function(custnameSelectViewAction,custnameSelectView,i18n,css) {

        var localConfigNum = 10;
        var localConfigPage = 1;

        return fish.View.extend({
            template: fish.compile(custnameSelectView),
            i18nData: fish.extend({}, i18n),
            events: {
                "click #queryCust" : 'queryCust',
                'click #resetCust' : 'resetCust'
            },
            initialize: function() {
                this.render();
            },
            //渲染页面
            render: function() {
                this.$el.html(this.template(this.i18nData));
                return this;
            },
            //初始化fish组件
            afterRender: function() {
                //初始化存量客户列表
                this.initorderDraftGrid();

            },
            initorderDraftGrid:function() {
                var me = this;
                var queryCustList = $.proxy(this.queryCustList,this); //函数作用域改变
                $("#Config-draft").grid({
                    colModel: [
                        //默认展示字段
                        {name: 'custId',label:'客户ID',width:200 },
                        {name: 'crmCustCode',label:'客户编码',width:180, align: 'left' },
                        {name: 'custName',label:'客户名称',width:350, align: 'left'}
                    ],
                    datatype: "json",
                    autowidth: true,
                    curPageSort: true,
                    recordtext:"{0}-{1} 共{2}条",
                    pgtext: " 第{0}页/共{1}页",
                    rowtext: "每页{0}条",
                    rowNum: 10,
                    rowList: [5,10,20,50,100,200,500],
                    pager: true,
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    multiselect: false,
                    shrinkToFit: false,
                    autoResizable: true,
                    showColumnsFeature: false, //允许用户自定义列展示设置
                    cached: true, //把用户自定义的列展示设置缓存在本地
                    pageData: function (e, rowid, iRow, iCol) {
                        me.queryCustList(e, rowid, iRow, iCol);
                    }.bind(this),
                    onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                        me.orderApplyFormView(e, rowid, iRow, iCol);
                    }
                });
                this.resize();
            },
            orderApplyFormView: function(e, rowid, iRow, iCol){
                var me = this;
                me.completedViewBtn(e, rowid, iRow, iCol);
            },
            completedViewBtn:function(){
                var selrow = $("#Config-draft").grid("getSelection"); //获取选中的行数据
                this.popup.close(selrow);

            },
            //客户查询
            queryCustList: function(page, rowNum, sortname, sortorder) {
                //客户名称、客户编码
                var custName = $("#custName").val();
                var custNo = $("#custNo").val();
                if(""==custName&&""==custNo){
                    fish.info("请填写客户名称或者客户编号进行查询！")
                    return;
                }
                rowNum = (rowNum!=''&&rowNum!=undefined)?rowNum:localConfigNum;
                page = (page!=''&&page!=undefined)?page:localConfigPage;

                var queryObject = new Object();
                queryObject.page = page;
                queryObject.rowNum = rowNum;
                queryObject.custName = custName;
                queryObject.custNo = custNo;
                $("#Config-draft").blockUI({message: '加载中'}).data('blockui-content', true);
                //调用后台方法
                custnameSelectViewAction.queryCustNameFromBizData(queryObject,function(data){
                    $("#Config-draft").unblockUI().data('blockui-content', false);
                    if (!data.success) {
                        fish.toast("warn", data.msg);
                        $('#comSpan').text("0");
                        $("#Config-draft").grid("reloadData", "");
                    } else {
                        var gridData = {
                            "rows": data.data.rows,
                            "page": page,
                            "records": data.data.records,
                            "rowNum": rowNum,
                            "total":data.data.page
                        };
                        $('#comSpan').text(data.data.records);
                        $("#Config-draft").grid("reloadData", gridData);
                    }
                });
            },
            //查询
            queryCust:function(){
                this.queryCustList();
            },
            //重置 条件置空
            resetCust:function(){
                $("#custName").val("");
                $("#custNo").val("");
            },
            //浏览器窗口大小改变事件
            resize: function() {
                $("#Config-draft").grid("setGridHeight", 327);
            }

        });
    });