define([
        'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/action/localOrderSelectAction',
        'text!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/templates/resourceSelectHcView.html',
        'i18n!module/UnicomLocalNet/resmaster/portal/local/i18n/unicomLocalOrderView.i18n',
        'css!module/UnicomLocalNet/resmaster/portal/local/css/unicomLocalOrderView.css'],
    function(localOrderSelectAction,resourceSelectView,i18n,css) {

        var localConfigNum = 10;
        var localConfigPage = 1;
        var productType ;

        return fish.View.extend({
            template: fish.compile(resourceSelectView),
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
                //$('#localResApplyTab').tabs();
                //初始化tab
                //1$('#localResApplyTab').tabs();
                //初始化草稿单表格
                this.initorderDraftGrid();
                //默认选中待接单区
                $('#local_apply_tabs_draft_link').click();
                //this.queryLocalResourceApplyList();

            },

            initorderDraftGrid:function() {
                var me = this;
                var queryLocalResourceApplyList = $.proxy(this.queryLocalResourceApplyList,this); //函数作用域改变
                $("#Config-draft").grid({
                    colModel: [
                        //默认展示字段
                        {name: 'ROWNO',label:'序号',width:60 },
                        {name: 'MACROOM_NUM',label:'机房编号',width:170 },
                        {name: 'MACROOM_ADD',label:'机房地址',width:300, align: 'left' },
                        {name: 'MACROOM_NAME',label:'机房名称',width:200, align: 'left'},
                        {name: 'MACROOM_LEVEL',label:'机房等级',width:150, align: 'left'},
                    ],
                    datatype: "json",
                    autowidth: true,
                    curPageSort: true,
                    // height: 300,
                    // maxHeight: 300,
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
                    pageData: queryLocalResourceApplyList,
                    onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件
                        me.orderApplyFormView(e, rowid, iRow, iCol);
                    },
                    gridComplete: function () {
                        $('.gotext').html("<span>跳转至<input class=\"ui-pagination-input\"></span>");
                    }

                });
                this.resize();
                $('.rowtext .ui-pagination').change(function () {
                        var p1=$(this).children('option:selected').val();//这就是selected的值
                        localConfigNum = p1;
                        localConfigPage = 1;
                        this.queryLocalResourceApplyList();
                    }
                );
                $("input[name$='provinceName']").popedit({

                    open:function(e) {
                        var _this = $(this);
                        var options = {
                            url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/provienceTreeView',
                            height: 500,
                            width: 400,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag : "province" //省：privience 市：city  区县:county
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    _this.popedit('setValue', {name:res.name, value:res.id});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    }

                });
                $("input[name$='cityName']").popedit({
                    open:function(e) {
                        var _this = $(this);
                        var areaId;
                        if($("#provinceName").val()!=""){
                            areaId = $("#provinceName").popedit("getValue").value;
                        }else{
                            fish.warn("请先选择归属省");
                            _this.popedit.close();
                        }

                        var options = {
                            url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/provienceTreeView',
                            height: 500,
                            width: 400,
                            modal: false,
                            draggable: false,
                            autoResizable: true,
                            viewOption: {
                                flag : "city", //省：privience 市：city  区县:county
                                areaId:areaId
                            },
                            callback: function (popup, view) {
                                popup.result.then(function (res) {
                                    _this.popedit('setValue', {name:res.name, value:res.id});
                                }, function (e) {
                                    console.log('关闭了', e);
                                });
                            }
                        };
                        var popup = fish.popupView(options);
                    },
                    change:function(e, data){//可以绑定change事件改变初始值
                        $("#A_belong_county").val("");
                    },

                });
                this.resize();
            },
            orderApplyFormView: function(e, rowid, iRow, iCol){
                var me = this;
                me.completedViewBtn(e, rowid, iRow, iCol);
            },


            //电路查询
            queryLocalResourceApplyList: function(page, rowNum, sortname, sortorder) {


                if($("#provinceName").val()==""){
                    fish.warn("请先选择归属省!");
                    return;
                }

                //电路编号
                var resourceName = $("#resourceName").val();
                var provinceName = $("#provinceName").popedit("getValue").value;
                var cityName = '';
                if($("#cityName").val() == null||""==$("#cityName").val() )
                {
                    cityName = '';
                }else {
                    cityName = $("#cityName").popedit("getValue").value;
                }

                // var beginNum = $("#beginNum").val();
                // var endNum = $("#endNum").val();
                // if(beginNum == '' || endNum =='' ){
                //     fish.toast("warn", "请检查输入的起始行是否有空");
                //     return;
                // }
                // debugger;
                rowNum = (rowNum!=''&&rowNum!=undefined)?rowNum:localConfigNum;
                page = (page!=''&&page!=undefined)?page:localConfigPage;

                var queryObject = new Object();
                queryObject.province = provinceName;
                queryObject.city = cityName;
                //queryObject.productType = this.options.productType;

                queryObject.beginNum = (page-1)*10+1;
                queryObject.endNum = 10;
                queryObject.resourceName = resourceName;

                // console.log($('.rowtext .ui-pagination').val());
                //调用后台方法
                localOrderSelectAction.queryResourceData(queryObject,function(data){
                    $("#Config-draft").unblockUI().data('blockui-content', false);

                    var ret = data.resorceInfoList;
                    if (data.message == "fail") {
                        console.log("fail")
                        var gridData = {
                            "rows": data.resorceInfoList,
                            "page": page,
                            "records": data.pageInfo.rowCount,
                            "rowNum": rowNum,
                            "total": data.pageInfo.pageCount
                        };
                        $('#completespan').text(data.pageInfo.rowCount);
                        $("#Config-draft").grid("reloadData", gridData);
                    } else {
                        var RESP_CODE = ret[0].RESP_CODE;
                        if (RESP_CODE == "0001" || RESP_CODE == "0002" || RESP_CODE == "0003" && data != null) {
                            fish.toast("warn", "资源返回为空");
                            //alert(data[0].RESP_DESC);
                        } else if (RESP_CODE == "0000" && data != null) {
                            var gridData = {
                                "rows": data.resorceInfoList,
                                "page": page,
                                "records": data.pageInfo.rowCount,
                                "rowNum": rowNum,
                                "total": data.pageInfo.pageCount
                            };
                            $('#completespan').text(data.pageInfo.rowCount);
                            $("#Config-draft").grid("reloadData", gridData);
                        }
                        else if (RESP_CODE == "8888") {
                            fish.toast("warn", data[0].errMsg);
                            // alert(RESP_CODE);
                        }
                    }
                });
                localConfigNum = $("#Config-draft").grid("getGridParam","rowNum");
                localConfigPage = $("#Config-draft").grid("getGridParam","page");
                $("#Config-draft").blockUI({message: '加载中'}).data('blockui-content', true);

            },

            //查询
            queryLocalApplyFun:function(){
                this.queryLocalResourceApplyList();
            },
            //重置
            resetLocalApplyFun:function(){
                $("#resourceName").val("");
                $("#provinceName").val("");
                $("#cityName").val("");
            },




            //已完成申请单查看
            completedViewBtn:function(){
                var selrow = $("#Config-draft").grid("getSelection"); //获取选中的行数据
                this.popup.close(selrow);

            },

            //浏览器窗口大小改变事件
            resize: function() {
                // $("#Config-draft").grid("resize",true);
                $("#Config-draft").grid("setGridHeight", 327);
                if($("#provinceName").val()==""){
                    $("input[name$='provinceName']").popedit('setValue', {name:this.options.province, value:this.options.value});
                }

            }

        }); //fish.View.extend END
    }); //ALL END