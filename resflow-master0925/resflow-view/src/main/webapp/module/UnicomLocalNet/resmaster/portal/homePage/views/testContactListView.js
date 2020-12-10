define(['module/UnicomLocalNet/resmaster/portal/homePage/action/TestContactAction.js',
    'text!module/UnicomLocalNet/resmaster/portal/homePage/templates/testContactListView.html',
    'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
    'css!module/UnicomLocalNet/resmaster/portal/orderWorkDeal/styles/taskmanagement.css'
], function(testContactAction,testContactListView,i18n,css) {
    var paramsMap = new Object();
    var URl;
    var dataLength =0;
    var total=1;
    var userId;
    var localGomNum = 30;
    var localGomPage = 1;

    return fish.View.extend({
        resNetworkUrl: '',
        crmRegion: '',
        userInfo: new Object(),
        template: fish.compile(testContactListView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #queryOrder': 'initQueryBut',

            'click #excelOrder': 'excelOrder',

        },
        initialize: function() {
            this.render();
            this.userInfo = testContactAction.queryStaffInfo().responseJSON.data;
            userId=this.userInfo.userId;
                   },
        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },
        //初始化fish组件
        afterRender: function() {
            $('#regionName').clearinput();
            this.initTestContactGrid();

        },

        initTestContactGrid: function() {
            var me = this;
            var queryTestContact = $.proxy(this.queryTestContact,this); //函数作用域改变
            $("#testContact-grid").grid({

                colModel: [
                    //默认展示字段
                    {name: 'ORG', label: '单位', width: 80, align: 'left' },
                    {name: 'INTERFACE_DEPT', label: '部门', width: 110, align: 'left' },
                    {name: 'INTERFACE_NAME', label: '姓名', width: 110, align: 'left'},
                    {name: 'INTERFACE_TEL', label: '电话', width: 110, align: 'left'},
                    {name: 'INTERFACE_EMAIL', label: '邮箱', width: 110, align:'left'},
                    {name: 'WECHAT', label: '微信', width: 110, align:'left'},
                    {name: 'COORDINATOR_DEPT', label: '部门', width: 110, align:'left'},
                    {name: 'COORDINATOR_NAME', label: '姓名', width: 110, align: 'left'},
                    {name: 'COORDINATOR_TEL', label: '电话', width: 110, align: 'left'},
                    {name: 'COORDINATOR_EMAIL', label: '邮箱', width: 110, align:'left'},
                    {name: 'DEPUTY_MANAGER_NAME', label: '副总经理', width: 110, align:'left'},
                    {name: 'DEPUTY_MANAGER_TEL', label: '副总经理电话', width: 110, align:'left'},
                    {name: 'DEPUTY_MANAGER_EMAIL', label: '副总经理邮箱', width: 110, align:'left'},
                    {name: 'REMARK', label: '备注', width: 110, align:'left'}

                ],

                datatype: "json",
                autowidth: true,
                rowNum:30,
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
                // showColumnsFeature: true, //允许用户自定义列展示设置
                cached: true, //把用户自定义的列展示设置缓存在本地
                pageData: queryTestContact,
                afterInsertRow: function (e, rowid, pageData) {

                 },
                onDblClickRow: function (e, rowid, iRow, iCol) {//双击行事件

                },

                gridComplete: function () {
                    $('.gotext').html("<span>跳转至<input class=\"ui-pagination-input\"></span>");
                }
            });
            $("#testContact-grid").grid('setGroupHeaders', {
                    useColSpanStyle: true,
                    groupHeaders: [
                        {startColumnName: 'INTERFACE_DEPT', numberOfColumns: 5, titleText: '测试接口人'},
                        {startColumnName: 'COORDINATOR_DEPT', numberOfColumns: 7, titleText: '测试协调（升级）人'}
                    ]
                }
            ),
            this.resize();
            //多级表头居中
            $("#testContact-grid .ui-th-column-header").css('text-align','center');
        },

        //查询工单方法
        queryTestContact: function(page, rowNum, sortname, sortorder) {
            //分页信息
            rowNum = (rowNum!=''&&rowNum!=undefined)?rowNum:localGomNum;
            page = (page!=''&&page!=undefined)?page:localGomPage;
            paramsMap.pageIndex = page+'';
            paramsMap.pageSize = rowNum+'';

            var regionName = $("#regionName").val();//申请单编码
            paramsMap.regionName= regionName;

            // debugger;
            //调用后台方法
            $("#testContact-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            testContactAction.queryTestContact(paramsMap,function (data) {
                var gridData = {
                    "rows": data.data,
                    "page": page,
                    "records": data.dataLength,
                    "rowNum": rowNum,
                    "total": data.total
                };
                $("#testContact-grid").grid("reloadData", gridData);
                $("#testContact-grid").unblockUI().data('blockui-content', false);
            });
        },

        initQueryBut:function(){ //查询
            this.queryTestContact();
        },

        //浏览器窗口大小改变事件
        resize: function() {
            // $("#testContact-grid").grid("resize",true);
            var frameHeight = document.documentElement.scrollHeight;
            $("#testContact-grid").grid("setGridHeight", frameHeight - 235);
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