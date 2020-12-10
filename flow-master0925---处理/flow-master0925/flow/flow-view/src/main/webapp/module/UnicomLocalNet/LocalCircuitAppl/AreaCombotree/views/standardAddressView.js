define([
        'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/action/standardAddressAction',
        'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
        'text!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/templates/standardAddress.html',
        'i18n!module/UnicomLocalNet/resmaster/portal/local/i18n/unicomLocalOrderView.i18n',
        'css!module/UnicomLocalNet/resmaster/portal/local/css/unicomLocalOrderView.css'],
    function(standardAddressAction,operOrderAction,localCircuitCodeView,i18n,css) {

        var localConfigNum = 10;
        var localConfigPage = 1;
        var productType ;

        return fish.View.extend({
            template: fish.compile(localCircuitCodeView),
            i18nData: fish.extend({}, i18n),
            events: {
                "click #queryLocalApply" : 'queryResEquipFun',
                'click #resetLocalApply' : 'resetResEquipFun'
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
                        {name: 'addressCode', label: '标准地址编码', width: "300"},
                        {name: 'addressName', label: '标准地址名称', width: "300"}
                        /*,
                        {name: 'equip_name', label: '设备名称', width: 200, align: 'left'},
                        {name: 'equip_type', label: '设备类型', width: 200, align: 'left'},
                        {name: 'equip_model', label: '设备模式', width: 200, align: 'left'}*/
                    ],
                    autowidth: true,
                    curPageSort: false,
                    height: '440',
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
                        me.queryResEquipMentInfo();
                    }.bind(this),
                    onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                        me.equipInfoDblickRowView(e, rowid, iRow, iCol);
                    },
                    gridComplete: function () {
                        $('.gotext').html("<span>跳转至<input class=\"ui-pagination-input\"></span>");
                    }
                });
                this.resize();
            },
            equipInfoDblickRowView: function(e, rowid, iRow, iCol){
                var selrow = $("#Config-draftGri").grid("getSelection"); //获取选中的行数据
                this.popup.close(selrow);
            },


            //电路查询
            queryResEquipMentInfo: function() {
                //电路编号
                var circuitCode = $("#circuitCod").val();
                var queryObject = new Object();
                var addressName = $("#addressName").val();
                queryObject.pageIndex = $('#Config-draftGri').grid("getGridParam", "page");
                queryObject.pageSize = $('#Config-draftGri').grid("getGridParam", "rowNum");
                queryObject.addressName = addressName;
                queryObject.regionCity = $("#regionCity").val();

                if(addressName == undefined || addressName == null || addressName == ""){
                    fish.error({title:'提示',message:'标准地址名称不能为空'});
                    return;
                }
                //调用后台方法
                $("#Config-draftGri").blockUI({message: '正在远程调用资源信息，请稍后...'}).data('blockui-content', true);
                standardAddressAction.queryStandardAddressInfo(queryObject,function(res){
                    if(res[0].RESP_CODE == '失败'){
                        fish.toast("error", res[0].RESP_DESC);
                    }else  if(res[0].RESP_CODE == '其他'){
                        fish.toast("warn", res[0].RESP_DESC);
                    }else{
                        $("#Config-draftGri").grid("reloadData", res);
                    }
                }).always(function () {
                    $("#Config-draftGri").unblockUI().data('blockui-content', false);
                });
            },

            //查询
            queryResEquipFun:function(){
                this.queryResEquipMentInfo();
            },
            //重置
            resetResEquipFun:function(){
               /* $("#circuitCod").val("");
                $("#accNbr").val("");*/
            },

            //浏览器窗口大小改变事件
            resize: function() {
            },

            initPageParam:function () {
                $('#addressNameDiv').addClass("requireds");
                var me = this;
                standardAddressAction.queryAlongArea(function (data) {
                    $("#regionCity").combobox({
                        placeholder: '--请选择归属地市--',
                        dataTextField: 'NAME',
                        dataValueField: 'VALUE',
                        dataSource: data
                    });
                });

            }
        });
    });