/**
 * 常用联系人多选择树JS
 */
define(['module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/operOrderAction',
    'text!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/templates/freContactsSelectView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n'
], function(operOrderAction,freContactsSelectView,i18n) {
        var meTsel;
        var currentAreaIdTsel;
        var currentOrgIdTsel;
        var currentUserIdTsel;
        var isSearchOrgPer;

    return fish.View.extend({
        template: fish.compile(freContactsSelectView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submitBtnSel': 'submit',
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
            meTsel = this;
            currentAreaIdTsel = this.options.currentAreaId;
            currentUserIdTsel = this.options.currentUserId;
            isSearchOrgPer = false;
            meTsel.initsearchContactPerGrid();
            meTsel.querysearchContactPerGrid();
            // meTsel.searchInputSel("260000003");
            $("#searchOrgPerSel").keyup(function (event) {
                if(event.keyCode == 13){
                    // debugger
                    meTsel.querysearchContactPerGrid();
                }
            });

        },
        initsearchContactPerGrid: function() { //初始化常用联系人
            var querysearchContactPerGrid = $.proxy(this.querysearchContactPerGrid,this);
            $conperGrid =$("#searchContactGrid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'ID', label: '联系人人员Id', hidden: true},
                    {name: 'ORG_ID', label: '联系人归属部门Id', hidden: true},
                    {name: 'OBJTYPE', label: '派单类型', hidden: true},
                    {name: 'NAME', label: '联系人', width: 170, align: 'left'},
                    {name: 'ORG_NAME', label: '归属部门', width: 370, align: 'left'}
                ],
                // datatype: "json",
                autowidth: true,
                height:290,
                rowNum: 10000,
                curPageSort: true,
                rowList: [5,10,15,20,50],
                pager: false,
                recordtext:"{0}-{1} 共{2}条",
                pgtext: " 第{0}页/共{1}页",
                rowtext: "每页{0}条",
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                multiselect: true,
                shrinkToFit: false,
                autoResizable: true,
                showColumnsFeature: false, //允许用户自定义列展示设置
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: querysearchContactPerGrid,
                gridComplete: function () {
                    // $('#searchOrgPerGrid .gotext').html("<span>跳转至<input class=\"ui-pagination-input\"></span>");
                },
                onCellSelect:function(e, rowid, iCol, cellcontent){

                },

            });

        },
        querysearchContactPerGrid: function(page, rowNum, sortname, sortorder) { //人员
            rowNum = 5000;
            page = 1;
            // debugger
            var paramsMap = new Object();
            var searchOrgPerName = $("#searchOrgPerSel").val();//人员搜索内容
            if(searchOrgPerName == ""
                || searchOrgPerName == undefined){
                rowNum = 500;
                page = 1;
            }
            //分页信息
            paramsMap.pageIndex = page+'';
            paramsMap.pageSize = rowNum+'';
            paramsMap.searchOrgPerName = searchOrgPerName;
            paramsMap.currentUserId = currentUserIdTsel;
            paramsMap.currentAreaId = currentAreaIdTsel;
            paramsMap.orgPerDeTypeVal = '260000003';
            // debugger;
            //调用后台方法
            $("#searchContactGrid").blockUI({message: '加载中'}).data('blockui-content', true);
            // debugger
            operOrderAction.qrySearchOrgPerDepSingleDown(paramsMap,function (data) {
                if (data.message == "success") {
                    $("#searchContactGrid").grid("reloadData", data.data);
                    $("#searchContactGrid").unblockUI({message: '加载中'}).data('blockui-content', false);
                }else{
                    fish.toast("warn", "获取数据失败");
                    $("#searchContactGrid").unblockUI({message: '加载中'}).data('blockui-content', false);
                }
            });

        },

        searchInputSel: function(orgPerDeTypeVal){//搜索框自动查询下拉数据
            // $('#searchOrgPer').combotree('destroy');
             if("260000003" == orgPerDeTypeVal){ //人员
                if(!isSearchOrgPer){
                    var searchObj = new Object();
                    searchObj.currentAreaId = currentAreaIdTsel;
                    searchObj.currentUserId = currentUserIdTsel;
                    operOrderAction.qrySearchContactsSel(searchObj, function (data) {
                        var options = {
                            placeholder: "请选择联系人",
                            dropdownWidth: 350,
                            check: {
                                enable: true,
                                chkboxType: {"Y":"", "N":""}
                            },
                            data: {
                                simpleData: {
                                    enable: true
                                }
                            },
                            searchFilter: true,
                            callback: {
                                // onClick: meTsel.onwClick
                            },
                            fNodes : data
                        };
                        $('#searchOrgPerSel').combotree(options);
                        isSearchOrgPer = true;
                    });

                }
            }

        },

        submit:function () {
            var selTreeVal = $("#searchContactGrid").grid("getCheckRows");
            // var selTreeVal = $('#searchOrgPerSel').combotree('value');
            if (selTreeVal == undefined
                || selTreeVal == null
                || selTreeVal == ""
                || selTreeVal.length == 0) {
                fish.info("请选择一个或多个联系人员！");
                return;
            }
            // debugger
            var paramObj = new Object();
            var searchArr = new Array();
            fish.each(selTreeVal, function (selVal) {
                // debugger
                var searchObj = new Object();
                searchObj.user_id = selVal.ID;
                searchObj.parent_user_id = currentUserIdTsel;
                searchObj.system_resource = 'second-schedule-lt';
                searchArr.push(searchObj);
            })
            paramObj.data = searchArr;
            // debugger
            operOrderAction.addSearchContacts(paramObj, function (data) {
                // debugger
                if(data.message == 'fail'){
                    fish.toast("warn", "添加常用联系人失败:"+data.errorMessage);
                    return;
                }else{
                    fish.toast("warn", "添加常用联系人成功");
                    meTsel.popup.close();
                }
            });


        },


    });
})