define(['module/UnicomLocalNet/resmaster/portal/homePage/action/AdminInfoAction',
    'text!module/UnicomLocalNet/resmaster/portal/homePage/templates/AdminSetting.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/taskmanagement.css'
], function(adminInfoAction,adminSetting,i18n,css) {
    var paramsMap = new Object();
    var URl;
    var dataLength =0;
    var total=1;
    var userId;
    var localGomNum = 10;
    var localGomPage = 1;

    return fish.View.extend({
        resNetworkUrl: '',
        crmRegion: '',
        userInfo: new Object(),
        template: fish.compile(adminSetting),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #queryOrder': 'initQueryBut',
            'click #excelOrder': 'excelOrder',
            'click #addAdminInfo': 'addAdminInfo',

        },
        initialize: function() {
            this.render();
            this.userInfo = adminInfoAction.queryStaffInfo().responseJSON.data;
            userId=this.userInfo.userId;
            crmRegionMap = adminInfoAction.qryMsmSwitchByArea(this.userInfo.areaId).responseJSON.data;
            if(crmRegionMap !=''
                && crmRegionMap != undefined){
                this.crmRegion = crmRegionMap.CRM_REGION;
            }
           },
        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },
        //初始化fish组件
        afterRender: function() {
            $('#staffName').clearinput();
            $('#staffId').clearinput();
            $('#loginName').clearinput();
            URl=this.getRootPath();

            $('.rowtext .ui-pagination').change(function () {
                    var p1=$(this).children('option:selected').val();//这就是selected的值
                    localGomNum = p1;
                    localGomPage = 1;
                    this.queryAdminInfo();
                }
            );
            //初始化表格
            this.initorderDealGrid();
            this.queryAdminInfo();

            $("#orderDeal-grid").on("click", ".inline-lock", function (e) {
                var queryParam =new Object();
                var rowid = $(this).closest("tr.jqgrow").attr("id");
                console.log("lock:" + rowid);
                var dataCell = $("#orderDeal-grid").grid("getRowData",rowid);
                queryParam.staffId=dataCell.STAFFID;
                queryParam.userId=dataCell.userId;
                queryParam.isShow="0";
                adminInfoAction.updateAdminInfo(queryParam,function (datas) {
                     if(datas.result){
                         $("#jLockButton_" + rowid, "#orderDeal-grid").hide();
                         $("#jUnLockButton_" + rowid, "#orderDeal-grid").show();
                     }
                 });
            });

            $("#orderDeal-grid").on("click", ".inline-unlock", function (e) {
                var queryParam =new Object();
                var rowid = $(this).closest("tr.jqgrow").attr("id");
                console.log("unlock:" + rowid);
                var dataCell = $("#orderDeal-grid").grid("getRowData",rowid);
                queryParam.staffId=dataCell.STAFFID;
                queryParam.userId=dataCell.userId;
                queryParam.isShow="1";
                adminInfoAction.updateAdminInfo(queryParam,function (datas) {
                    if(datas.result) {
                        $("#jUnLockButton_" + rowid, "#orderDeal-grid").hide();
                        $("#jLockButton_" + rowid, "#orderDeal-grid").show();
                    }
                });
            });
        },

        initorderDealGrid: function() {
            var me = this;
            var queryAdminInfo = $.proxy(this.queryAdminInfo,this); //函数作用域改变
            $("#orderDeal-grid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'LEVELID', label: '级别', width: 120, align: 'left',hidden:true },
                    {name: 'ISSHOW', label: '是否禁用', width: 120, align: 'left',hidden:true },
                    {name: 'STAFFID', label: '登录账号', width: 120, align: 'left',hidden:false },
                    {name: 'STAFFNAME', label: '用户名', width: 120, align: 'left',hidden:false },
                    {name: 'LOGINNAME', label: '登录名', width: 120, align: 'left',hidden:false },
                    {name: 'EMAIL', label: '邮箱', width: 180, align: 'left',hidden:false },
                    {name: 'PHONENO', label: '电话', width: 120, align: 'left',hidden:false},
                    {name: 'ORGNAME', label: '组织机构', width: 320, align: 'left',hidden:false},
                    {name: 'DELETESTAT', label: '状态', width: 120,align: 'left', hidden:false,formatter: me.formatDeleteStat},
                    {name: '编辑', formatter: 'actions',
                        formatoptions: {
                            editbutton: false, //默认开启编辑功能
                            // delbutton: true,  //默认开启删除功能
                            delbutton: function (rowdata) {
                                return rowdata.LEVELID == "1" ? false : true;
                            },
                            inlineButtonAdd: function (rowdata) {
                               var flag = rowdata.ISSHOW == "0" ? true : false;
                              var inlineButton1 = [{ //可以给actions类型添加多个icon图标,事件自行控制
                                    id: "jLockButton", //每个图标的id规则为id+"_"+rowid
                                    className: "inline-lock",//每个图标的class
                                    icon: "glyphicon glyphicon-lock",//图标的样子
                                    title: "禁用",//鼠标移上去显示的内容
                                  hidden: true //默认隐藏
                                }, {
                                    id: "jUnLockButton",
                                    className: "inline-unlock",
                                    icon: "fa fa-unlock",//awesome的图标
                                    title: "启用",

                                }];
                                var inlineButton2 = [{ //可以给actions类型添加多个icon图标,事件自行控制
                                    id: "jLockButton", //每个图标的id规则为id+"_"+rowid
                                    className: "inline-lock",//每个图标的class
                                    icon: "glyphicon glyphicon-lock",//图标的样子
                                    title: "禁用"//鼠标移上去显示的内容
                                }, {
                                    id: "jUnLockButton",
                                    className: "inline-unlock",
                                    icon: "fa fa-unlock",//awesome的图标
                                    title: "启用",
                                    hidden: true //默认隐藏
                                }];
                                if(flag){return inlineButton1;}else{return inlineButton2;}
                            }
                        }
                    }

                ],
                datatype: "json",
                autowidth: true,
                rowNum:10,
                rowList: [10,15,20,50,100,200,500,1000],
                pager: true,
                curPageSort: true,
                recordtext:"{0}-{1} 共{2}条",
                pgtext: " 第{0}页/共{1}页",
                rowtext: "每页{0}条",
                gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                multiselect: true,
                shrinkToFit: false,
                autoResizable: true,
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: queryAdminInfo,
                afterInsertRow: function (e, rowid, pageData) {
                    $("#orderDeal-grid").grid('setCell', rowid, 'FLOWTRACE', '', {color: '#6DCC4A'});
                    $("#orderDeal-grid").grid('setCell', rowid, 'SOURCENETWORK', '', {color: '#6DCC4A'});
                },

                onCellSelect: function (e, rowid, iCol, cellcontent, colName, cellval) {//选中单元格的事件
                    // console.log("onCellSelect---->rowid:" + rowid + ", iCol:" + iCol + ", cellcontent:" + cellcontent + "  ....." + cellval + "--" + colName);
                    var dataCell = $("#orderDeal-grid").grid("getRowData",rowid);
                },
                gridComplete: function () {
                    $('.gotext').html("<span>跳转至<input class=\"ui-pagination-input\"></span>");
                },
                beforeDeleteRow: function (e, rowid, data, option) {
                    var flag = false;
                    console.log("inlineBeforeDeleteRow:" + rowid);
                    fish.confirm('确认是否删除该用户').result.then(function() {
                        flag = true;
                        fish.success('删除成功');
                    }
                    );
                        var defer = $.Deferred();
                        setTimeout(function() {
                               defer.resolve(flag);
                           }, 4000);
                        return defer.promise();
                },
                 afterDeleteRow: function (e, rowid, data, option) {
                    console.log("inlineAfterDeleteRow:" + rowid);
                    var staffId = data.STAFFID;
                    adminInfoAction.deleteAdminInfo(data);
                    //删除后台数据
                },
            });
            this.resize();
        },

        //查询工单方法
        queryAdminInfo: function(page, rowNum, sortname, sortorder) {
            //分页信息
            rowNum = (rowNum!=''&&rowNum!=undefined)?rowNum:localGomNum;
            page = (page!=''&&page!=undefined)?page:localGomPage;

            paramsMap.pageIndex = page+'';
            paramsMap.pageSize = rowNum+'';
            var staffName = $('#staffName').val();
            var staffId = $('#staffId').val();
            var loginName = $('#loginName').val();
            paramsMap.staffName = staffName;
            paramsMap.userId = userId;
            paramsMap.staffId = staffId;
            paramsMap.loginName = loginName;
            // debugger;
            //调用后台方法

            $("#orderDeal-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            adminInfoAction.queryAdminInfo(paramsMap,function (datas) {
                var gridData = {
                    "rows": datas.data,
                    "page": page,
                    "records": datas.dataLength,
                    "rowNum": rowNum,
                    "total":datas.total
                };
                $("#orderDeal-grid").grid("reloadData", gridData);
                $("#orderDeal-grid").unblockUI().data('blockui-content', false);
            });
        },
        initQueryBut:function(){ //查询
            this.queryAdminInfo();
        },
        //add
        addAdminInfo :function(){
            var me = this;
            fish.popupView({
                url: 'module/UnicomLocalNet/resmaster/portal/systemSetup/views/addAdminInfo.js',
                width: "40%",
                height: "25%",
                title: "添加管理员",
                modal: false,
                viewOption:{
                    selectType: 'gomQuery',

                },
                callback:function(popup,view){
                    popup.result.then(function (e) {
                        setTimeout(function() {
                            me.queryAdminInfo();
                        }, 2000);
                    },function (e) {
                        console.log('关闭了',e);
                        // $("#touchGet").css("style","display:block; float: left;");
                        // $("#touchGet").show();
                    });
                }
            })


        },


        formatDeleteStat: function(value){
            var deleteStat;
            switch (value) {
                case 0:
                    deleteStat = '启用';
                    break;
                case 1:
                    deleteStat = '禁用';
                    break;
                default:
                    deleteStat = '';
                    break;
            }
            return deleteStat;
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
    }); //fish.View.extend END
}); //ALL END