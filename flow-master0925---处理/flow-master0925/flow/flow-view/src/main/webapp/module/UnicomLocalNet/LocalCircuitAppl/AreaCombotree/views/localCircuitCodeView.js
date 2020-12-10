define([
        'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/action/localOrderSelectAction',
        'text!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/templates/localCircuitCodeView.html',
        'i18n!module/UnicomLocalNet/resmaster/portal/local/i18n/unicomLocalOrderView.i18n',
        'css!module/UnicomLocalNet/resmaster/portal/local/css/unicomLocalOrderView.css'],
    function(localOrderSelectAction,localCircuitCodeView,i18n,css) {

        var localConfigNum = 10;
        var localConfigPage = 1;
        var productType ;

        return fish.View.extend({
            template: fish.compile(localCircuitCodeView),
            i18nData: fish.extend({}, i18n),
            events: {
                "click #queryLocalApply" : 'queryLocalApplyFun',
                'click #resetLocalApply' : 'resetLocalApplyFun',

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
                //初始化草稿单表格
                this.initorderDraftGrid();
                this.initPageParam();
            },

            initorderDraftGrid:function() {
                var me = this;
                $("#Config-draftGri").grid({
                    colModel: [
                        //默认展示字段
                        {name: 'circuitId', label: '电路id', width: 200},
                        {name: 'prodInstId', label: '产品实例号', width: 100, align: 'left'},
                        {name: 'circuitCode', label: '电路编号', width: 230, align: 'left'},
                        {name: 'resType', label: '产品类型', width: 100, align: 'left'},
                        {name: 'accNbr', label: '业务号码', width: 150, align: 'left'},
                        {name: 'oprState', label: '业务状态', width: 100, align: 'left'},
                        {name: 'oprStateId', label: '业务状态Id', width: 100, hidden:true},
                        {name: 'sbOprState', label: '实例状态', width: 150, align: 'left'},
                    ],
                    autowidth: true,
                    curPageSort: false,
                    datatype:'json',
                    height: 310,
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
                        me.queryLocalDraftApplyList();
                    }.bind(this),
                    onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                        me.orderApplyFormView(e, rowid, iRow, iCol);
                    },
                    gridComplete: function () {
                        $('.gotext').html("<span>跳转至<input class=\"ui-pagination-input\"></span>");
                    }
                });
                this.resize();
            },
            orderApplyFormView: function(e, rowid, iRow, iCol){
                var me = this;
                me.completedViewBtn(e, rowid, iRow, iCol);
            },


            //电路查询
            queryLocalDraftApplyList: function() {
                //电路编号
                var circuitCode = $("#circuitCod").val();
                var accNbr = $("#accNbr").val();
                if(circuitCode==""&&accNbr==""){
                    fish.info("电路编号和业务号码不能同时为空，请检查！")
                    return;
                }
                var queryObject = new Object();
                queryObject.pageIndex = $('#Config-draftGri').grid("getGridParam", "page");
                queryObject.pageSize = $('#Config-draftGri').grid("getGridParam", "rowNum");
                queryObject.productType = this.options.productType;
                queryObject.circuitCode = circuitCode;
                queryObject.accNbr = accNbr;
                queryObject.type = '';
                queryObject.isToBeQuery = '0';
                //调用后台方法
                $("#Config-draftGri").blockUI({message: '加载中'}).data('blockui-content', true);
                localOrderSelectAction.queryResData(queryObject,function(res){
                    if(res.success){
                        if (res.data.length > 0) {
                            var pageParam = {};
                            pageParam.rows = res.data;
                            pageParam.page = res.page;
                            pageParam.records = res.circuitTotalCount;
                            $("#Config-draftGri").grid("reloadData", pageParam);
                            // localOrderSelectAction.querySrvOrderState({'instanceId': res.data[0].prodInstId}, function (result) {
                            //     if (result != '') {
                            //         fish.warn(result);
                            //         return;
                            //     }
                            //
                            // });
                        } else{
                            fish.toast("warn", "查不到数据，请确认输入的内容是否正确！");
                        }
                    }else{
                        fish.toast("warn", res.msg);
                    }
                    $("#Config-draftGri").unblockUI().data('blockui-content', false);
                });
            },

            //查询
            queryLocalApplyFun:function(){
                this.queryLocalDraftApplyList();
            },
            //重置
            resetLocalApplyFun:function(){
                $("#circuitCod").val("");
                $("#accNbr").val("");

            },




            //已完成申请单查看
            completedViewBtn:function(){
                var selrow = $("#Config-draftGri").grid("getSelection"); //获取选中的行数据
                if (selrow.oprStateId == '170010'){
                    fish.info("所选电路已拆机，不能发起其它操作！");
                    return;
                }else{
                    var circuitData = new Object();
                    circuitData.circuitCode =  selrow.circuitCode;
                    circuitData.prodInstId =  selrow.prodInstId;
                    circuitData.accNbr =  selrow.accNbr;
                    this.popup.close(circuitData);
                }


            },

            //浏览器窗口大小改变事件
            resize: function() {
            },

            initPageParam:function () {
                $('#circuitCod').clearinput();
                $('#accNbr').clearinput();
                var option = this.options;
                if(this.options.productType=='20181221006'){
                    $("#Config-draftGri").grid("hideCol", 'accNbr');
                    $('#accNbrDiv').hide();
                    $('#circuitCodDiv').attr("class", "col-md-8 col-sm-8");
                }
            }

        });
    });