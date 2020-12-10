define([
        'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/action/localOrderSelectAction',
        'text!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/templates/localCircuitCodeNewView.html',
        'i18n!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/i18n/gomCircuitCodeListView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/taskmanagement.css'],
    function(localOrderSelectAction,localCircuitCodeNewView,i18n,css) {

        var localConfigNum = 10;
        var localConfigPage = 1;
        var productType ;

        return fish.View.extend({
            template: fish.compile(localCircuitCodeNewView),
            i18nData: fish.extend({}, i18n),
            events: {
                "click #queryLocalApply" : 'queryLocalApplyFun',
              //  'click #resetLocalApply' : 'resetLocalApplyFun',
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
                //初始化tab
                $('#localApplyTabs').tabs();
                //初始化草稿单表格
                this.initorderDraftGrid();

                //默认选中待接单区
                $('#local_apply_tabs_draft_link').click();
                // this.queryLocalDraftApplyList();
            },

            initorderDraftGrid:function() {
                var me = this;
                $("#Config-draftG").grid({
                    colModel: [
                        //默认展示字段
                        {name: 'CUST_NAME_CHINESE',label:'客户名称',width:170 },
                        {name: 'CIRCUITCODE',label:'电路编号',width:170, align: 'left' },
                        {name: 'SERIAL_NUMBER',label:'业务号码',width:170, align: 'left'},
                        {name: 'CODE_CONTENT',label:'产品类型',width:170 },
                        {name: 'ACT_TYPE',label:'动作',width:170, align: 'left' },
                        {name: 'OPRSTATE',label:'电路状态',width:170, align: 'left'},
                        {name: 'ROUTEINFO',label:'业务路由',width:700, align: 'left'},
                        {name: 'SRVORDID',label:'srvOrdId',width:170, align: 'left',hidden:true},
                        {name: 'SERVICEID',label:'serviceId',width:170, align: 'left',hidden:true},
                    ],
                    //autowidth: true,
                    curPageSort: false,
                    // height:240,
                    recordtext:"{0}-{1} 共{2}条",
                    pgtext: " 第{0}页/共{1}页",
                    rowtext: "每页{0}条",
                    rowNum: 15,
                    rowList: [10, 15, 20, 50, 100, 200, 500],
                    pager: true,
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    multiselect: false,
                    shrinkToFit: false,
                    autoResizable: true,
                    showColumnsFeature: false, //允许用户自定义列展示设置
                    cached: true, //把用户自定义的列展示设置缓存在本地
                    pageData: null,
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
            queryLocalDraftApplyList: function(page, rowNum, sortname, sortorder) {
                //电路编号
                var circuitCode = $("#circuitCo").val();
                var custNameChinese = $("#custNam").val();
                var serialNumber = $("#serialNumb").val();
                // debugger;
                rowNum = (rowNum!=''&&rowNum!=undefined)?rowNum:localConfigNum;
                page = (page!=''&&page!=undefined)?page:localConfigPage;
                // rowNum = rowNum || localConfigNum;
                // page = page || localConfigPage;
                if (page == undefined) {
                    page = 1;
                }
                if (rowNum == undefined) {
                    rowNum = 10;
                }
                var queryObject = new Object();

                queryObject.type = 'res';
                queryObject.circuitCode = circuitCode;
                queryObject.custNameChinese = custNameChinese;
                queryObject.serialNumber = serialNumber;
                // console.log($('.rowtext .ui-pagination').val());
                //调用后台方法
                $("#Config-draftG").blockUI({message: '加载中'}).data('blockui-content', true);
                localOrderSelectAction.queryResData(queryObject,function(data){
                    if (data != null) {
                        $("#Config-draftG").grid("reloadData", data);
                    }else {
                        fish.toast("warn", "查询电路信息异常，请联系管理员！");
                    }
                });
                localConfigNum = $("#Config-draftG").grid("getGridParam","rowNum");
                localConfigPage = $("#Config-draftG").grid("getGridParam","page");
                $("#Config-draftG").unblockUI().data('blockui-content', false);
            },

            //查询
            queryLocalApplyFun:function(){
                this.queryLocalDraftApplyList();
            },





            //已完成申请单查看
            completedViewBtn:function(){
                var selrow = $("#Config-draftG").grid("getSelection"); //获取选中的行数据
                var circuitData = new Object();
                circuitData =  selrow;
                var _this = $(this);
                var options = {
                    url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/circuitCodeInfoView',
                    height: '99%',
                    width: "95%",
                    modal: false,
                    draggable: true,
                    resizable:true,
                    autoResizable: true,
                    viewOption: {
                        flag : "org",
                        selrow : selrow
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (res) {
                            //_this.popedit('setValue', {name:res.circuitCode, value:res.circuitCode});
                            //$('input[name="serialNumber"]').val(res.serialNumber);
                            //$('#serialNumber').attr('disabled',true);
                            //$('input[name="tradeId"]').val(res.tradeId);
                            //res.serviceName = $("#SERVICE_ID").combobox('text');
                            //$("#gridId").grid("addRowData",1, res, 'last');
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                };
                var popup = fish.popupView(options);

            },

            //浏览器窗口大小改变事件
            resize: function() {
                var frameHeight = document.documentElement.scrollHeight;
                $("#Config-draftG").grid("resize", true);
                $("#Config-draftG").grid("setGridHeight", frameHeight - 120);
            }

        }); //fish.View.extend END
    }); //ALL END