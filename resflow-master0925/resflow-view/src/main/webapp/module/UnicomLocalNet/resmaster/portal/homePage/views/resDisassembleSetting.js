define(['module/UnicomLocalNet/resmaster/portal/homePage/action/AdminInfoAction',
    'text!module/UnicomLocalNet/resmaster/portal/homePage/templates/resDisassembleSetting.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/taskmanagement.css'
], function(adminInfoAction,resDisassembleSetting,i18n,css) {
    var paramsMap = new Object();
    var URl;
    var dataLength =0;
    var total=1;
    var userId;
    var userInfo;
    var localGomNum = 10;
    var localGomPage = 1;

    return fish.View.extend({
        resNetworkUrl: '',
        crmRegion: '',
        userInfo: new Object(),
        template: fish.compile(resDisassembleSetting),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #delSet': 'delSet',
            'click #updateDisTime': 'updateDisTime',
            'click #addDisTime': 'addDisTime',
            'click #refreshSetting': 'refreshSetting',


        },
        initialize: function() {
            this.render();
            userInfo = adminInfoAction.queryStaffInfo().responseJSON.data;
            userId=this.userInfo.userId;

        },
        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },
        //初始化fish组件
        afterRender: function() {

            URl=this.getRootPath();
            //初始化表格
            this.initorderDealGrid();

            this.queryDisassemble();
            //控制lock按钮功能
            $("#orderDeal-grid").on("click", ".inline-lock", function (e) {
                var rowid = $(this).closest("tr.jqgrow").attr("id");
                var num = $("#orderDeal-grid").grid("getCell",rowid,5);
                $("#orderDeal-grid").grid("editRow",rowid);

                $("#jLockButton_" + rowid, "#orderDeal-grid").hide();
                $("#jUnLockButton_" + rowid, "#orderDeal-grid").show();
                // $("#orderDeal-grid").grid("reloadData");
            });

            $("#orderDeal-grid").on("click", ".inline-unlock", function (e) {
                var params = new Object();
                var rowid = $(this).closest("tr.jqgrow").attr("id");
                $("#orderDeal-grid").grid("saveRow",rowid);
                var num = $("#orderDeal-grid").grid("getCell",rowid,5);
                var data = $("#orderDeal-grid").grid("getRowData",rowid);
                params.areaId = data.AREAID;
                params.serviceId = data.SERVICEID;
                params.spinner = num;

                if (!(/(^[1-9]\d*$)/.test(num))) {
                    fish.toast('warn', '请输入正整数');
                    var gridData={};
                    adminInfoAction.queryDisassemble(params,function (datas) {
                        var gridData = {
                            "rows": datas.data,
                            "records": datas.dataLength,
                        };
                        $("#orderDeal-grid").grid("reloadData", gridData);
                        $("#orderDeal-grid").grid("editRow",rowid);
                    })
                }else {
                    console.log("editRow:" + rowid);
                    adminInfoAction.updateDisassemble(params, function (datas) {
                            if(datas.result) {
                                fish.toast('success', datas.message);
                            }else{
                                fish.toast('error', '修改失败，请稍后再试');
                                $("#orderDeal-grid").grid("restoreRow",rowid);
                            }

                    });
                    console.log("unlock:" + rowid);
                    $("#jUnLockButton_" + rowid, "#orderDeal-grid").hide();
                    $("#jLockButton_" + rowid, "#orderDeal-grid").show();

                }
            });
        },

        initorderDealGrid: function() {
            var me = this;
            var disassemble = $.proxy(this.queryDisassemble,this); //函数作用域改变
            $("#orderDeal-grid").grid({
                colModel: [
                    //默认展示字段
                    {name: 'CODEID', label: 'ID', width: 220, align: 'left',hidden:true },
                    {name: 'SERVICEID', label: '产品类型ID', width: 220, align: 'left',hidden:true },
                    {name: 'AREAID', label: '区域', width: 220, align: 'left',hidden:true },
                    {name: 'PRODUCTTYPE', label: '产品类型', width: 220, align: 'left', hidden:false },
                    {name: 'SPINNER', label: '时长', width: 220, align: 'left',hidden:false, editable: true },
                    {name: 'STATE', label: '生效状态', width: 220, align: 'left', hidden:false },
                    { name: '操作类型', formatter: 'actions',
                        formatoptions: {
                            editbutton: false ,//默认开启编辑功能
                            delbutton: true,  //默认开启删除功能
                            inlineButtonAdd: [{ //可以给actions类型添加多个icon图标,事件自行控制
                                id: "jLockButton", //每个图标的id规则为id+"_"+rowid
                                className: "inline-lock",//每个图标的class
                                icon: "glyphicon glyphicon-pencil",//图标的样子
                                title: "编辑"//鼠标移上去显示的内容
                            }, {
                                id: "jUnLockButton",
                                className: "inline-unlock",
                                icon: "glyphicon glyphicon-floppy-disk",//awesome的图标
                                title: "保存",
                                hidden: true //默认隐藏
                            }]
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
                pageData: disassemble,
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
                    fish.confirm('确认是否删除该配置').result.then(function() {
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
                    var param = new Object();
                    param.areaId = data.AREAID;
                    param.serviceId = data.SERVICEID;
                    param.areaId = data.AREAID;
                    param.codeType = 'spinner';
                    adminInfoAction.deleteDisassemble(param);
                    //删除后台数据
                },
            });

            this.resize();
        },

        //查询工单方法
        queryDisassemble: function(page, rowNum, sortname, sortorder) {
            var params= new Object();
            //分页信息
            rowNum = (rowNum!=''&&rowNum!=undefined)?rowNum:localGomNum;
            page = (page!=''&&page!=undefined)?page:localGomPage;

            params.pageIndex = 0+'';
            params.pageSize = 20+'';
            params.areaId= userInfo.areaId;

            //调用后台方法
            $("#orderDeal-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            adminInfoAction.queryDisassemble(params,function (datas) {
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
        delSet:function(){ //查询
            var datas = $("#orderDeal-grid").grid("getRowData", rowid);
            if (datas.length < 1) {
                fish.toast('warn', "请选择一条电路信息")
                return;
            }else{
                adminInfoAction.deleteDisassemble(paramsMap,function (datas) {
                })
            }
        },
        //add
        addDisTime :function(){
            var me = this;
            fish.popupView({
                url: 'module/UnicomLocalNet/resmaster/portal/systemSetup/views/addDisassembleSet',
                width: '70%',
                height: '50%',
                title: "添加预占资源释放时长",
                modal: false,
                viewOption:{
                    selectType: 'gomQuery',

                },
                callback:function(popup,view){
                    popup.result.then(function (e) {
                        setTimeout(function() {
                            me.queryDisassemble();
                        }, 2000);

                    },function (e) {
                        console.log('关闭了',e);
                        // $("#touchGet").css("style","display:block; float: left;");
                        // $("#touchGet").show();
                    });
                }
            })

        },

        refreshSetting  :function(){
            var me = this;
            this.queryDisassemble();

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