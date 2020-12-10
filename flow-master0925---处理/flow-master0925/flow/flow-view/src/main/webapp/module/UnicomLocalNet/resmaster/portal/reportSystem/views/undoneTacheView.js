/**
 * 未完成环节 工单明细
 */
define(["text!module/UnicomLocalNet/resmaster/portal/reportSystem/templates/undoneTacheView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/reportSystem/action/reportSystemAction.js',
        'module/UnicomLocalNet/resmaster/portal/reportSystem/action/cQreportSystemAction.js',
        "css!module/UnicomLocalNet/resmaster/portal/gomAffairDispatcher/styles/fqAffair.css"],
    function (template, portalViewi18n, reportAction, cQreportAction,css) {
        var localStandyNum = 10;                  //页面展示条数
        var localStandyPage = 1;
        var orgName;
        var cityInfo;
        //页数
        return ngc.View.extend({
            template: ngc.compile(template),
            i18nData: ngc.extend({}, portalViewi18n),
            events: {
                'click #undoneTache-queryBtn': 'query',//查询
                'click #undoneTache-exportBtn': 'exportExcel',//导出
                'click #undoneTache-initStaBtn': 'initStatistics',//生成汇总报表
            },
            initialize: function () {
                this.render();

            },
            render: function () {           //渲染页面
                this.$el.html(this.template(this.i18nData));
            },
            //初始化fish组件
            afterRender: function () {
                //初始化时间插件
                $('#undoneTache-beginDate,#undoneTache-endDate').datetimepicker({
                    orientation:{y:'bottom'},
                    buttonIcon: '',
                    viewType: "date",
                    todayBtn: true
                });

                //初始化下拉框
                this.initMultiselect();
                this.initComboxData();
                //初始化表格
                this.initGrid();
                //查询数据
                //this.queryUndoneTache();
                // 增加input清除功能
                $("#undoneTache-form input").clearinput();
                $('#undoneTache-isTimeOut').clearinput();
                // 调整窗口大小
                $(window).trigger("resize");
                cityInfo = cQreportAction.getCityInfo().responseJSON.data;
                orgName = cQreportAction.getOrgName().responseJSON.data;
                $('#undoneTache-dcStaffOrg,#undoneTache-handelDep,#undoneTache-orgName').multiselect({
                    dataTextField: 'text',
                    dataValueField: 'text',
                    dataSource: cityInfo
                });

                $('#undoneTache-orgName').multiselect({
                    dataTextField: 'text',
                    dataValueField: 'text',
                    dataSource: orgName
                });


            },
            query:function(){
                this.queryUndoneTache(1,undefined,undefined);
            },
            initMultiselect:function(){

                    $('#undoneTache-productType').multiselect({
                        dataTextField: 'text',
                        dataValueField: 'value',
                        dataSource: [
                            {text:'语音中继电路',value:'20181211001'},
                            {text:'MPLS-VPN',value:'10000008'},
                            {text:'数字电路',value:'10000001'},
                            {text:'裸光纤',value:'20181221002'},
                            {text:'基础数据(ATM)',value:'20181221003'},
                            {text:'基础数据(FR)',value:'20181221004'},
                            {text:'以太网专线',value:'10000002'},
                            {text:'互联网专线(DIA)',value:'10000011'},
                            {text:'基础数据(DDN)',value:'20181221005'},
                            {text:'局内中继电路',value:'20181221006'},
                            {text:'政企精品网业务',value:'80000466'},
                            {text:'SD-WAN智选专线',value:'10003406'},

                        ]
                    });

                    $('#undoneTache-operateType').multiselect({
                        dataTextField: 'text',
                        dataValueField: 'value',
                        dataSource: [
                            {text: '新开', value: '101'},
                            {text: '变更', value: '103'},
                            {text: '拆机', value: '102'},
                            {text: '复机', value: '105'},
                            {text: '移机', value: '106'},
                            {text: '停机', value: '104'}

                        ]
                    });

                cQreportAction.getLocalNetworkTache(function (data) {
                    $('#undoneTache-tacheName').multiselect({
                        dataTextField: 'text',
                        dataValueField: 'value',
                        dataSource: data
                    });
                });


                $('#undoneTache-dcStaffOrg,#undoneTache-handelDep,#undoneTache-orgName').multiselect({
                    dataTextField: 'text',
                    dataValueField: 'text',
                    dataSource: cityInfo
                });

                $('#undoneTache-orgName').multiselect({
                            dataTextField: 'text',
                            dataValueField: 'text',
                            dataSource: orgName
                        });

                $('#undoneTache-resources').multiselect({
                    dataTextField:'value',
                    dataValueField:'id',
                    dataSource:  [
                        {value: '政企中台', id: 'jike'},
                        {value: '一干调度', id: 'onedry'},
                        {value: '二干调度', id: 'secondary'},
                        {value: '本地调度', id: 'localBuild'},
                        {value: '云网协同', id: 'cloudNetwork'}
                        ]
                });

                $('#undoneTache-tacheDuration').spinner({
                    step:0.5,
                    min:0
                });
                //环节超时时长
                $('#undoneTache-timeOut').spinner({
                    step:1,
                    min:0
                });
            },

            initComboxData: function () {
                $('#undoneTache-isTimeOut').combobox({
                    placeholder: '--请选择是否超时--',
                    dataTextField: 'name',
                    dataValueField: 'value',
                    dataSource: [
                        {name: '是', value: 1},
                        {name: '否', value: 0}
                    ]
                });
            },
            //初始化表格
            initGrid: function () {
                var queryUndoneTache = $.proxy(this.queryUndoneTache, this);
                $("#undoneTache-grid").grid({
                    datatype: "json",
                    colModel: [
                        //默认展示字段
                        {name: 'SRV_ORD_ID', label: '业务定单信息ID', hidden:true , sortable: false},
                        {name: 'APPLY_ORD_ID', label: '申请单号',width: 150, align: 'center', sortable: false},
                        {name: 'TRADE_ID', label: '业务订单号',width: 150, align: 'center', sortable: false},
                        {name: 'APPLY_ORD_NAME', label: '申请单标题',width: 150, align: 'center', sortable: false},
                        {name: 'TACHE_NAME', label: '当前环节名称',width: 150, align: 'center', sortable: false},
                        {name: 'ATTR_VALUE', label: '电路编号',width: 150, align: 'center', sortable: false},
                        {name: 'CD_ORG', label: '电路调度环节处理人所属分公司',width: 200, align: 'center', sortable: false},
                        {name: 'CD_STAFF', label: '电路调度环节处理人员/角色',width: 200, align: 'center', sortable: false},
                        {name: 'DISP_DEP', label: '当前环节所属分公司',width: 200, align: 'center', sortable: false},
                        {name: 'CREATE_DATE', label: '申请单创建时间',width: 150, align: 'center', sortable: false},
                        {name: 'CUST_NAME_CHINESE', label: '客户名称',width: 150, align: 'center', sortable: false},
                        {name: 'DISPATCH_ORDER_NO', label: '调度单号',width: 150, align: 'center', sortable: false},
                        {name: 'DISP_OBJ_TYE', label: '派发类型',width: 150, align: 'center', sortable: false,formatter: this.formatDispType},
                        {name: 'DISP_ORG_NAME', label: '当前环节单位',width: 180, align: 'center', sortable: false},
                        {name: 'DU_STAFF_NAME', label: '当前处理人',width: 150, align: 'center', sortable: false},
                        {name: 'SU_STAFF_PHONE', label: '当前处理人电话',width: 150, align: 'center', sortable: false},
                        {name: 'HANDLE_DEP', label: '发起单位（分公司）',width: 180, align: 'center', sortable: false},
                        {name: 'OPERATE_VALUE', label: '动作类型',width: 150, align: 'center', sortable: false,formatter: this.formatOperateType},
                        {name: 'ORDER_TYEP', label: '单据类型',width: 150, align: 'center', sortable: false, formatter: this.formatOrderType},
                        {name: 'ARRIVAL_TIME', label: '到单时间',width: 150, align: 'center', sortable: false},
                        {name: 'PRODUCT_VALUE', label: '产品类型',width: 150, align: 'center', sortable: false,formatter: this.formatProductType},
                        {name: 'ISRETURN', label: '是否退单',width: 150, align: 'center', sortable: false},
                        {name: 'RESOURCES_NAME', label: '单据来源',width: 150, align: 'center', sortable: false,formatter: this.formatResourcesName},
                        {name: 'SEND_ORG_NAME', label: '派发岗位',width: 150, align: 'center', sortable: false},
                        {name: 'SERIAL_NUMBER', label: '业务号码',width: 150, align: 'center', sortable: false},
                        {name: 'SUBSCRIBE_ID', label: '客户订单号',width: 150, align: 'center', sortable: false},
                        {name: 'TACHE_DURATION', label: '环节历时',width: 150, align: 'center', sortable: false},
                        {name: 'TACHETIMEOUT', label: '环节超时时长',width: 150, align: 'center', sortable: false},
                        {name: 'ISTIMEOUT', label: '是否超时',width: 150, align: 'center', sortable: false}
                    ],
                    rowNum: 10,
                    rowList: [10, 15, 20, 50, 100, 200, 500],
                    pager: true,
                    recordtext: "{0}-{1} 共{2}条",
                    pgtext: " 第{0}页/共{1}页",
                    rowtext: "每页{0}条",
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    autoResizable: true,
                    pageData: queryUndoneTache,
                    height: 280,
                    autowidth: false,
                    shrinkToFit: false,
                    showColumnsFeature: true
                });
                this.resize();
            },
            resize: function () {
                $("#undoneTache-grid").grid("resize", true);
                var frameHeight = document.documentElement.scrollHeight;
                $("#undoneTache-grid").grid("setGridHeight", frameHeight - 230);
            },
            //查询
            queryUndoneTache: function (page, rowNum, isReject) {
                var queryObj={};
                page = (page != '' && page != undefined) ? page : localStandyPage;
                rowNum = (rowNum != '' && rowNum != undefined) ? rowNum : localStandyNum;
                fish.store.set('undoneTache-grid-rowNum', rowNum); //记录用户选择的每页记录数
                queryObj.parameters ={
                    endDate:$("#undoneTache-endDate").val()||''
                    ,beginDate:$("#undoneTache-beginDate").val()||''
                    ,isTimeOut:$("#undoneTache-isTimeOut").val()||''
                    ,tacheTimeOut:$("#undoneTache-timeOut").val()||''
                    ,resources:$("#undoneTache-resources").multiselect('value')||[]
                    ,tacheCodes:$("#undoneTache-tacheName").multiselect('value')||[]
                    ,productType:$("#undoneTache-productType").multiselect('value')||[]
                    ,operateType:$("#undoneTache-operateType").multiselect('value')||[]
                    ,dcStaffOrg:$("#undoneTache-dcStaffOrg").multiselect('value')||[]
                    ,orgName:$("#undoneTache-orgName").multiselect('value')||[]
                    ,handelDep:$("#undoneTache-handelDep").multiselect('value')||[]
                    ,tacheDuration:$("#undoneTache-tacheDuration").val()||''
                };
                queryObj.pageIndex = page + '';
                queryObj.pageSize = rowNum + '';
                if (queryObj.parameters.beginDate && queryObj.parameters.endDate && queryObj.parameters.beginDate > queryObj.parameters.endDate) {
                    fish.warn("开始时间必须小于结束时间！");
                    return;
                }
                console.log("queryData",queryObj);
                $("#undoneTache-grid").blockUI({message: '加载中'}).data('blockui-content', true);
                cQreportAction.queryUndoneTacheList(queryObj, function (data) {
                    console.log("data",data);
                    $("#undoneTache-grid").unblockUI().data('blockui-content', false);
                    var gridData = {
                        "rows": data.data,
                        "page": data.pageIndex,
                        "records": data.dataLength,
                        "rowNum": data.rowNum,
                        "total": data.total
                    };
                    $("#undoneTache-grid").grid("reloadData", gridData);
                });
            },

            formatDispType: function (value) {
                var dispType;
                switch (value) {
                    case '260000001':
                        dispType = '按岗位派发';
                        break;
                    case '260000002':
                        dispType = '按角色派发';
                        break;
                    case '260000002':
                        dispType = '按人员派发';
                        break;
                    default:
                        dispType = '';
                        break;
                }
                return dispType;
            },
            //单据来源
            formatResourcesName: function (value) {
                var resourceName;
                switch (value) {
                    case 'localBuild':
                        resourceName = '本地调度';
                        break;
                    case 'onedry':
                        resourceName = '一干调度';
                        break;
                    case 'secondary':
                        resourceName = '二干调度';
                        break;
                    case 'jike':
                        resourceName = '政企中台';
                        break;
                    case 'cloudNetwork':
                        resourceName = '云网协同';
                        break;
                    default:
                        resourceName = '';
                        break;
                }
                return resourceName;
            },
            //单据类型转换
            formatOrderType: function (value) {
                var itemTypeMap = {
                    '101':'开通单',
                    '102':'核查单',
                    '104':'追单',
                    '108':'加急',
                    '109':'延期',
                    '110':'挂起',
                    '111':'解挂',
                    '112':'撤单',
                    '114':'全程调测退单'
                };
                return itemTypeMap[value];
            },
            //动作类型
            formatOperateType: function (value) {
                var activeTypeMap = {
                    '101' : '新开',
                    '103' : '变更',
                    '102' : '拆机',
                    '105' : '复机',
                    '106' : '移机',
                    '104' : '停机'

                };
                return activeTypeMap[value];
            },
            //产品类型
            formatProductType: function (value) {
                var serviceIdMap = {
                    '20181211001' : '语音中继电路',
                    '10000008' : 'MPLS-VPN',
                    '10000001' : '数字电路',
                    '20181221002' : '裸光纤',
                    '20181221003' : '基础数据(ATM)',
                    '20181221004' : '基础数据(FR)',
                    '10000002' : '以太网专线',
                    '10000011' : '互联网专线(DIA)',
                    '20181221005' : '基础数据(DDN)',
                    '20181221006' : '局内中继电路',
                    '80000466' : '政企精品网业务',
                    '10003406' : 'SD-WAN智选专线'
                };
                return serviceIdMap[value];
            },

            //导出
            exportExcel: function () {
                var me = this;
                var data ={
                    endDate:$("#undoneTache-endDate").val()||''
                    ,beginDate:$("#undoneTache-beginDate").val()||''
                    ,isTimeOut:$("#undoneTache-isTimeOut").val()||''
                    ,tacheTimeOut:$("#undoneTache-timeOut").val()||''
                    ,resources:$("#undoneTache-resources").multiselect('value')||[]
                    ,tacheCodes:$("#undoneTache-tacheName").multiselect('value')||[]
                    ,productType:$("#undoneTache-productType").multiselect('value')||[]
                    ,operateType:$("#undoneTache-operateType").multiselect('value')||[]
                    ,dcStaffOrg:$("#undoneTache-dcStaffOrg").multiselect('value')||[]
                    ,orgName:$("#undoneTache-orgName").multiselect('value')||[]
                    ,handelDep:$("#undoneTache-handelDep").multiselect('value')||[]
                    ,tacheDuration:$("#undoneTache-tacheDuration").val()||''
                };
                if (data.beginDate && data.endDate && data.beginDate >data.endDate) {
                    fish.warn("开始时间必须小于结束时间！");
                    return;
                }
                reportAction.export(me.getRootPath() + '/localScheduleLT/CqReportController/exportUndoneTacheDate.spr', data);
            },
            // 获取根目录
            getRootPath: function () {
                // 获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
                var curWwwPath = window.document.location.href;
                // 获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
                var pathName = window.document.location.pathname;
                var pos = curWwwPath.indexOf(pathName);
                // 获取主机地址，如： http://localhost:8083
                var localhostPaht = curWwwPath.substring(0, pos);
                // 获取带"/"的项目名，如：/uimcardprj
                var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
                return (localhostPaht + projectName);
            },

            //生成汇总报表
            initStatistics:function () {
                var me = this;
                var  parameters ={
                    endDate:$("#undoneTache-endDate").val()||''
                    ,beginDate:$("#undoneTache-beginDate").val()||''
                    ,isTimeOut:$("#undoneTache-isTimeOut").val()||''
                    ,tacheTimeOut:$("#undoneTache-timeOut").val()||''
                    ,resources:$("#undoneTache-resources").multiselect('value')||[]
                    ,tacheCodes:$("#undoneTache-tacheName").multiselect('value')||[]
                    ,productType:$("#undoneTache-productType").multiselect('value')||[]
                    ,operateType:$("#undoneTache-operateType").multiselect('value')||[]
                    ,dcStaffOrg:$("#undoneTache-dcStaffOrg").multiselect('value')||[]
                    ,orgName:$("#undoneTache-orgName").multiselect('value')||[]
                    ,handelDep:$("#undoneTache-handelDep").multiselect('value')||[]
                    ,tacheDuration:$("#undoneTache-tacheDuration").val()||''
                };
              //  var id = data.id;
                //如果是表单一级菜单不打开

              /*  me.getView("#tabContent").openFrameView("a34",
                    "測試", "undoneTacheStatisticIndex.html?navType=no",true,true,null,false);*/
                var pop = ngc.openView({
                    url: 'module/UnicomLocalNet/resmaster/portal/reportSystem/views/undoneTacheStatisticView',
                    width: "100%",
                    height: "100%",
                    canClose:true,
                    title: "未完成环节工单汇总",
                    viewOption: {
                        data: parameters
                        ,type:'readonly'
                    },
                    callback: function (popup, view) {
                        popup.result.then(function (e) {
                        }, function (e) {
                            console.log('关闭了', e);
                        });
                    }
                });

               /* cQreportAction.queryUndoneTacheStatistics(parameters, function (data) {
                    console.log("data",data);
                });*/
            },

        })
    });