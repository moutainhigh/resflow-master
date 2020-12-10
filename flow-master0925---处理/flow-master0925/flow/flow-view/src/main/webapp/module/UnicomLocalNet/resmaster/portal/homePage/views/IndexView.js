/**
 * by ma.furong
 * 2018-10-12
 */
define(["text!module/UnicomLocalNet/resmaster/portal/homePage/templates/IndexView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        "module/component/views/TabView",
        "module/UnicomLocalNet/resmaster/portal/homePage/action/qryAction",
        "css!module/UnicomLocalNet/resmaster/portal/homePage/css/IndexView.css"],
    function(template,portalViewi18n,TabView,qryAction) {
        var routeInfo;
        var userInfo;
        var MainView = ngc.View.extend({
            template : ngc.compile(template),
            events:{
              'click #resMange':'clickResMange'
            },
            initialize : function() {
                this.render();
                //查询数据库当前用户有没有资源补录的权限
                //获取当前用户信息
                userInfo = qryAction.queryStaffInfo().responseJSON.data;
                //查询当前登录用户是否有权限
                var param = {};
                param.staffId = userInfo.userId;
                param.codeType = 'ROUTE_PRESENTATION';

                routeInfo = qryAction.queryRouteInfoUrl(param).responseJSON.data;
                if('0'===userInfo.isShow){
                    this.data=[
                        {
                            title: "首页",
                            id: "a1",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-home",
                            "url": "indexPage.html?navType=no"
                        },
                        {
                            title: "工单待办",
                            id: "a11",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-list-alt",
                            "url": "UnicomLocalNetStandby.html?navType=no"
                        },
                        {
                            title: "本地业务申请",
                            id: "a20",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-edit",
                            "url": "unicomLocalOrderIndex.html?navType=no"
                        },
                        {
                            title: "事务调单",
                            id: "a26",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-paperclip",
                            "url": "gomAffairDispatcherIndex.html?navType=no"
                        },
                        {
                            title: "搜索查询",
                            id: "a27",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-search",
                            subMenus: [
                                {
                                    title: "工单查询",
                                    id: "a21",
                                    hash: "javascript:;",
                                    "url": "gomListIndex.html?navType=no"
                                },
                                {
                                    title: "个人历史工单查询",
                                    id: "a66",
                                    hash: "javascript:;",
                                    "url": "hisGomWoIndex.html?navType=no"
                                },
                                {
                                    title: "定单查询",
                                    id: "a24",
                                    hash: "javascript:;",
                                    "url": "gomOrderListIndex.html?navType=no"
                                },
                                {
                                    title: "调单查询",
                                    id: "a22",
                                    hash: "javascript:;",
                                    "url": "gomDispatcherListIndex.html?navType=no"
                                },
                                {
                                    title: "电路查询",
                                    id: "a25",
                                    hash: "javascript:;",
                                    "url": "gomHisCircuitListIndex.html?navType=no"
                                }
                            ]
                        },
                        {
                            title: "业务报表",
                            id: "a34",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-signal",
                            subMenus: [
                                {
                                    title: "产品类型统计表",
                                    id: "a40",
                                    hash: "#bb1",
                                    "url": "dispatchOrderStatisticsIndex.html?navType=no"
                                },
                                {
                                    title: "开通及时率统计",
                                    id: "a41",
                                    hash: "#bb2",
                                    "url": "openTimeRateStatisticsIndex.html?navType=no"
                                },
                                {
                                    title: "报竣未起租电路统计",
                                    id: "a42",
                                    hash: "#bb3",
                                    "url": "completionNotRentedIndex.html?navType=no"
                                },
                                {
                                    title: "电路明细表",
                                    id: "a43",
                                    hash: "#bb4",
                                    "url": "disOrderDetailStatisticsIndex.html?navType=no"
                                },
                                {
                                    title: "业务网络稽核查询",
                                    id: "a44",
                                    hash: "#bb5",
                                    "url": "businessNetworkVerificationIndex.html?navType=no"
                                }
                            ]
                        },
                        {
                            title: "定制报表",
                            id: "a35",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-indent-left",
                            subMenus: [
                                {
                                    title: "未完成环节工单明细",
                                    id: "a45",
                                    hash: "#bb6",
                                    "url": "undoneTacheIndex.html?navType=no"
                                },
                                {
                                    title: "未完成环节工单汇总",
                                    id: "a46",
                                    hash: "#bb7",
                                    "url": "undoneTacheStatisticIndex.html?navType=no"
                                },
                                {
                                    title: "超时未报竣电路明细",
                                    id: "a47",
                                    hash: "#bb8",
                                    "url": "overtimeUnfinishedListIndex.html?navType=no"
                                },
                                {
                                    title: "电路完工及时率汇总",
                                    id: "a48",
                                    hash: "#bb9",
                                    "url": "finishTimeRateStatisticsIndex.html?navType=no"
                                },
                                {
                                    title: "资源分配未入库明细",
                                    id: "a49",
                                    hash: "#bb10",
                                    "url": "resAllocateUnStorageListIndex.html?navType=no"
                                },
                                {
                                    title: "资源分配未入库汇总",
                                    id: "a50",
                                    hash: "#bb11",
                                    "url": "resAllocateUnStorageStatisticsIndex.html?navType=no"
                                },
                                {
                                    title: "电路汇总表",
                                    id: "a51",
                                    hash: "#bb12",
                                    "url": "circuitSummaryListIndex.html?navType=no"
                                }
                            ]
                        },
                        {
                            title: "资源补录",
                            id: "a36",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-aaa",
                            subMenus: [
                                {
                                    title: "资源补录发起",
                                    id: "a53",
                                    hash: "javascript:;",
                                    "url": "resourceInitiateViewIndex.html?navType=no"
                                },
                                {
                                    title: "资源补录待办",
                                    id: "a54",
                                    hash: "#bb1",
                                    "url": "resourceInitiateViewStandbyIndex.html?navType=no"
                                }
                            ]
                        },
                        {
                            title: "资源综合呈现",
                            id: "a37",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-indent-left",
                            subMenus: [
                                {
                                    title: "业务分析",
                                    id: "a55",
                                    hash: "javascript:;",
                                    subMenus:[
                                        {
                                            title: "业务电路",
                                            id: "a56",
                                            hash: "#a561",
                                            "url": "/businessAnalysis/route"
                                        },
                                        {
                                            title: "光交箱列表",
                                            id: "a57",
                                            hash: "#a562",
                                            "url": "/businessAnalysis/boxList"
                                        },
                                        {
                                            title: "业务电路统计",
                                            id: "a58",
                                            hash: "#a563",
                                            "url": "/businessAnalysis/circuitStatistics"
                                        }
                                    ]
                                },
                                {
                                    title: "资源监控",
                                    id: "a59",
                                    hash: "javascript:;",
                                    subMenus:[
                                        {
                                            title: "局向资源监控",
                                            id: "a60",
                                            hash: "#a591",
                                            "url": "/resourceMonitoring/map"
                                        }

                                    ]
                                },
                                {
                                    title: "统计分析",
                                    id: "a61",
                                    hash: "javascript:;",
                                    subMenus:[
                                        {
                                            title: "传输资源统计",
                                            id: "a62",
                                            hash: "#a611",
                                            class: "1111",
                                            "url": "/statisticalAnalysis/resource"
                                        }

                                    ]
                                }
                            ]
                        },
                        {
                            title: "消息列表",
                            id: "a38",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-aaa",
                            "url": "messageListViewIndex.html?navType=no"
                        }
                        /**,
                        {
                            title: "各地市测试联系人",
                            id: "a39",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-list-alt",
                            subMenus: [
                                {
                                    title: "测试联系人名单",
                                    id: "a64",
                                    hash: "javascript:;",
                                    "url": "testContactListViewIndex.html?navType=no"
                                }
                            ]
                        }*/
                    ];
                }else{
                    this.data=[
                        {
                            title: "首页",
                            id: "a1",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-home",
                            "url": "indexPage.html?navType=no"
                        },
                        {
                            title: "工单待办",
                            id: "a11",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-list-alt",
                            "url": "UnicomLocalNetStandby.html?navType=no"
                        },
                        {
                            title: "本地业务申请",
                            id: "a20",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-edit",
                            "url": "unicomLocalOrderIndex.html?navType=no"
                        },
                        {
                            title: "事务调单",
                            id: "a26",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-paperclip",
                            "url": "gomAffairDispatcherIndex.html?navType=no"
                        },
                        {
                            title: "搜索查询",
                            id: "a27",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-search",
                            subMenus: [
                                {
                                    title: "工单查询",
                                    id: "a21",
                                    hash: "javascript:;",
                                    "url": "gomListIndex.html?navType=no"
                                },
                                {
                                    title: "个人历史工单查询",
                                    id: "a66",
                                    hash: "javascript:;",
                                    "url": "hisGomWoIndex.html?navType=no"
                                },
                                {
                                    title: "定单查询",
                                    id: "a24",
                                    hash: "javascript:;",
                                    "url": "gomOrderListIndex.html?navType=no"
                                },
                                {
                                    title: "调单查询",
                                    id: "a22",
                                    hash: "javascript:;",
                                    "url": "gomDispatcherListIndex.html?navType=no"
                                },
                                {
                                    title: "电路查询",
                                    id: "a25",
                                    hash: "javascript:;",
                                    "url": "gomHisCircuitListIndex.html?navType=no"
                                }
                            ]
                        },
                        {
                            title: "业务报表",
                            id: "a34",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-signal",
                            subMenus: [
                                {
                                    title: "产品类型统计表",
                                    id: "a40",
                                    hash: "#bb1",
                                    "url": "dispatchOrderStatisticsIndex.html?navType=no"
                                },
                                {
                                    title: "开通及时率统计",
                                    id: "a41",
                                    hash: "#bb2",
                                    "url": "openTimeRateStatisticsIndex.html?navType=no"
                                },
                                {
                                    title: "报竣未起租电路统计",
                                    id: "a42",
                                    hash: "#bb3",
                                    "url": "completionNotRentedIndex.html?navType=no"
                                },
                                {
                                    title: "电路明细表",
                                    id: "a43",
                                    hash: "#bb4",
                                    "url": "disOrderDetailStatisticsIndex.html?navType=no"
                                },
                                {
                                    title: "业务网络稽核查询",
                                    id: "a44",
                                    hash: "#bb5",
                                    "url": "businessNetworkVerificationIndex.html?navType=no"
                                }
                            ]
                        },
                        {
                            title: "定制报表",
                            id: "a35",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-indent-left",
                            subMenus: [
                                {
                                    title: "未完成环节工单明细",
                                    id: "a45",
                                    hash: "#bb6",
                                    "url": "undoneTacheIndex.html?navType=no"
                                },
                                {
                                    title: "未完成环节工单汇总",
                                    id: "a46",
                                    hash: "#bb7",
                                    "url": "undoneTacheStatisticIndex.html?navType=no"
                                },
                                {
                                    title: "超时未报竣电路明细",
                                    id: "a47",
                                    hash: "#bb8",
                                    "url": "overtimeUnfinishedListIndex.html?navType=no"
                                },
                                {
                                    title: "电路完工及时率汇总",
                                    id: "a48",
                                    hash: "#bb9",
                                    "url": "finishTimeRateStatisticsIndex.html?navType=no"
                                },
                                {
                                    title: "资源分配未入库明细",
                                    id: "a49",
                                    hash: "#bb10",
                                    "url": "resAllocateUnStorageListIndex.html?navType=no"
                                },
                                {
                                    title: "资源分配未入库汇总",
                                    id: "a50",
                                    hash: "#bb11",
                                    "url": "resAllocateUnStorageStatisticsIndex.html?navType=no"
                                },
                                {
                                    title: "电路汇总表",
                                    id: "a51",
                                    hash: "#bb12",
                                    "url": "circuitSummaryListIndex.html?navType=no"
                                }
                            ]
                        },
                        {
                            title: "资源补录",
                            id: "a36",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-aaa",
                            subMenus: [
                                {
                                    title: "资源补录发起",
                                    id: "a53",
                                    hash: "javascript:;",
                                    "url": "resourceInitiateViewIndex.html?navType=no"
                                },
                                {
                                    title: "资源补录待办",
                                    id: "a54",
                                    hash: "#bb1",
                                    "url": "resourceInitiateViewStandbyIndex.html?navType=no"
                                }
                            ]
                        },
                        {
                            title: "消息列表",
                            id: "a38",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-aaa",
                            "url": "messageListViewIndex.html?navType=no"
                        }
                        /**,
                        {
                            title: "各地市测试联系人",
                            id: "a39",
                            hash: "javascript:;",
                            "icon": "glyphicon glyphicon-list-alt",
                            subMenus: [
                                {
                                    title: "测试联系人名单",
                                    id: "a64",
                                    hash: "javascript:;",
                                    "url": "testContactListViewIndex.html?navType=no"
                                }
                            ]
                        }*/
                    ];
                }
            },
            render : function() {
            	this.$el.append(this.template(portalViewi18n));
            },
            afterRender : function() {
                this.requireView({
                    selector:"#navDiv",
                    url:"module/UnicomLocalNet/resmaster/portal/homePage/views/NavView"
                });
                this.$("#tabContent").height($(window).height()-42);
                this.$(".mainDiv .left").height($(window).height()-42);
                this.initSidebar();
                this.initResTree();
                this.initTab();

                $(window).resize(function () {          //当浏览器大小变化时
                    this.$("#tabContent").height($(window).height() - 42);
                    this.$(".mainDiv .left").height($(window).height() - 42);
                });
            },
            initSidebar:function () {
                var that =this;
                var data = this.data;
                $('#sidebar').pagesidebar({
                    data: data,
                    width:200,
                    minWidth :50,
                    openFirst: true,// false
                    showToggleBtn: true,
                    children: "subMenus",
                    subMenuMode:"inline",
                    select: function (e,data) {
                        that.openTab(data);
                    }
                });

            },
            initResTree:function () {

            },
            clickResMange:function () {
                var dom = $(".mainDiv .left .resTree");
                if(dom.css("display")=="none"){
                    dom.css("display","block");
                    $("#res_arrow").removeClass("glyphicon-menu-right");
                    $("#res_arrow").addClass("glyphicon-menu-down");
                }else{
                    dom.css("display","none");
                    $("#res_arrow").removeClass("glyphicon-menu-down");
                    $("#res_arrow").addClass("glyphicon-menu-right");
                }
            },
            initTab:function () {
                var that = this;
                var _tabView = new TabView();
                this.setView("#tabContent", _tabView);
                this.renderViews("#tabContent");
                window.contentTabView = _tabView;
                _tabView.on("viewRenderAfter", function() {
                    _tabView.openFrameView('a1', '首页', 'indexPage.html?navType=no', true, false, null, false);
                });
            },

            openTab:function (data) {
                var id = data.id;
                //如果是表单一级菜单不打开
                if (id === "a27" || id === "a34" || id === "a35" || id === "a37"
                    || id === "a59" || id === "a55" || id==="a61" || id === "a36"
                    || id === "a39") {
                             return;
                }
                var title = data.title;
                var url = data.url;
                var context = ngc.getContext();
                var origin = window.location.origin;
                var _url = origin+context+"/"+url;
                var routeInfoUrl = '';
                if(id === "a62" || id === "a60" || id === "a56" || id === "a57" || id === "a58"){
                    for (var i = 0; i < routeInfo.length; i++) {
                       var mm= routeInfo[i].CODE_VALUE;
                       if(url==mm){
                           routeInfoUrl = routeInfo[i].CODE_CONTENT+'&code='+userInfo.regionId+'&userCode＝'+userInfo.name;
                       }
                    }
                    if(!!window.ActiveXObject || "ActiveXObject" in window){
                         window.open(routeInfoUrl,'_blank','menubar=no,toolbar=no, status=no,scrollbars=yes');
                    }else{
                        window.open(routeInfoUrl);
                    }
                  }else{
                    this.getView("#tabContent").openFrameView(id, title, _url, true, true, null, false);
                }
            }
        });
        return MainView;
    });

