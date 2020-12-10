define(['module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/action/digitalCircutAction.js',
    'text!module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/templates/bareFiberView.html',
    'i18n!module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/i18n/digitalCircuit.i18n',
    'css!module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/css/digitalCircuit.css'
], function(digitalCircutAction,bareFiberView,i18n,css) {
    var $grid;
    var productType;
    var productTypeText;

    var actType;//动作类型 （新开，变更，关闭。。。。）
    var orderType;//单据类型 ：（核查单，开通单）
    var queryType;
    var actTypeText;
    var srvOrdId;
    var custOrdId;
    var flag;
    var fileList = null,
        maxFileSize = 50*1024*1024,                //文件大小限制
        maxFileCount = 5 ,              //文件个数限制
        isFileFinsh = "success"
    ;
    var upLoadResult = new Array();
    var fileData; // 文件数组
    return fish.View.extend({
        template: fish.compile(bareFiberView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submit': 'submit',
            'click #save': 'save',
            'click #cancel': 'cancel',
            'click #reset': 'reset',
            'click #UpdateSave' : 'UpdateSave',
            'click .js-select-file': 'fileSelect',
            'click #addAndSelectRow': 'addAndSelectRow',
        },

        initialize: function() {

            this.render();
            this.workOrderState = "";
            this.showActionButtons = false;
        },

        //渲染页面
        render: function() {
            this.$el.html(this.template(this.i18nData));
        },

        //初始化fish组件
        afterRender: function() {
            //初始化下拉框
            this.initCombobox();
            //获取父页面传递过来的数据
            this.initDigPage();
            //初始化时间控件 时间表单都需要	放到<div class="input-group"></>中，要不位置会错乱
            var yesterday = new Date();
            yesterday.setDate(yesterday.getDate()-1);
            yesterday.setHours('23');
            yesterday.setMinutes('59');
            yesterday.setSeconds('59');
            $('.date').datetimepicker({
                orientation:{y:'bottom'},
                startDate:yesterday,
                defaultDate: new Date(),
                container:'#accordion'
            });
            var date = new Date();
            $("#CREATE_DATE").datetimepicker("value", date);
            $("#CREATE_DATE").datetimepicker("disable");
            $("#createTime").datetimepicker("value", date,"disable");
            $("#createTime").datetimepicker("disable");

            $(document).find('.modal-body').css('overflow-x','hidden');

            //初始化定时查询
            //this.initAutoQry();
            this.initDocumentation();
            this.initTree();
            this.initPageParam();

        },
        initCombobox:function(){
            /* 页面所有需要枚举字段通用初始化方法。一次完成，需要在表单中class 后加menu+枚举值priperty_Id
              例如：<input id="circuitType"  name="circuitType" class="form-control menu 10000517" >
              该方法会获取所有表单一次性初始化枚举值 ren.jiahang
              */
            var menu = $(".menu");
            for(var i =0;i<menu.length;i++){
                var objClassName = menu[i].className;
                var objId = menu[i].id;
                var objName = menu[i].name;
                var startSub = objClassName.indexOf('menu')+5,endSub;
                var checkSub = objClassName.indexOf(' n-'); //必填的字段class后会有n-
                var comboxSub = objClassName.indexOf(' ui-'); //下拉的字段class后会有ui-
                if(checkSub>0){
                    endSub = checkSub;
                }else if(comboxSub>0){
                    endSub = comboxSub;
                }else{
                    endSub=objClassName.length;
                }
                var enumType = objClassName.substring(startSub,endSub);
                var obj = '#'+objId;
                if(enumType=='10000506'){
                    digitalCircutAction.queryEnum3(enumType,function (data) {
                        var array = new Array();
                        array =data;
                        var enumTypeMap = new Map();
                        enumTypeMap = array[array.length-1];
                        var enumTypeB = "."+enumTypeMap.enumCode;
                        var obj = $(enumTypeB);
                        data.splice(data.length-1,1);//删除list中最后一个元素。
                        $(obj).combobox({
                            placeholder: '--请选择--',
                            dataSource:data,
                            editable:true
                        });
                    });
                }else {
                    digitalCircutAction.queryEnum(enumType, function (data) {
                        var array = new Array();
                        array = data;
                        var enumTypeMap = new Map();
                        enumTypeMap = array[array.length - 1];
                        var enumTypeB = "." + enumTypeMap.enumCode;
                        var obj = $(enumTypeB);
                        data.splice(data.length - 1, 1);//删除list中最后一个元素。
                        $(obj).combobox({
                            placeholder: '--请选择--',
                            dataSource: data,
                            editable: true
                        });
                        if (obj.selector == ".product_code") {
                            $(".product_code").combobox('value', productType);
                            $(".product_code").combobox("disable");

                        } else if (obj.selector == ".operate_type") {
                            $(".operate_type").combobox('value', actType);
                            $(".operate_type").combobox('disable');
                        }
                        $("#proPriDeg").combobox('value', 1);
                        $("#slaFlag").combobox('value', 1);
                        $("#cirLeaseRange").combobox('value', 5);
                        $("#slaServOpen").combobox('value', 4);  // 定义默认值
                        $("#slaNetQuAss").combobox('value', 4);  // 定义默认值
                        $("#slaSaleServ").combobox('value', 4);  // 定义默认

                    });
                }
            };
        },
        initDocumentation: function () {
            $("#accordion").accordion({
                active: 0,//此属性为false时,collapsible=true时,默认不展开页签;collapsible=false时,默认展开第一个页签 此属性为数值时,表示默认展开页签的索引,从0开始 此属性为负值时,表示默认展开页签的索引,从最后一个往前数
                heightStyle:"content",
                multiple:"true",
                collapsible:"false"
            });
            $("#accordion").accordion({
                active: 1,//此属性为false时,collapsible=true时,默认不展开页签;collapsible=false时,默认展开第一个页签 此属性为数值时,表示默认展开页签的索引,从0开始 此属性为负值时,表示默认展开页签的索引,从最后一个往前数
                heightStyle:"content",
                multiple:"true",
                collapsible:"false"
            });
            $("#accordion").accordion({
                active: 2,//此属性为false时,collapsible=true时,默认不展开页签;collapsible=false时,默认展开第一个页签 此属性为数值时,表示默认展开页签的索引,从0开始 此属性为负值时,表示默认展开页签的索引,从最后一个往前数
                heightStyle:"content",
                multiple:"true",
                collapsible:"false"
            });
            $("#accordion").on("accordion:activate", function(event, ui) {
                var parentWidth = $("#operTaskManagement-searchCodeDiv3").width();
                $("#gridId").grid("setGridWidth", parentWidth, false);
                $("#gridId").grid("setGridHeight", "260", false);
                // 添加冻结列
                $grid.grid('setFrozenColumns', -1);

            });
        },
        initTree:function () {
            //初始化区域
            $("input[name$='_region']").popedit({

                open:function(e) {
                    var _this = $(this);
                    var options = {
                        url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/operTreeOrderView',
                        height: 500,
                        width: 400,
                        modal: false,
                        draggable: false,
                        autoResizable: true,
                        viewOption: {
                            flag : "org"
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
            $("input[name$='_CITY']").popedit({
                open:function(e) {
                    var _this = $(this);
                    var areaId = $("#Z_belong_province").popedit("getValue").value;
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
                }

            });
            $("input[name$='_DEP']").popedit({

                open:function(e) {
                    var _this = $(this);
                    var options = {
                        url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/operTreeOrderView',
                        height: 500,
                        width: 400,
                        modal: false,
                        draggable: false,
                        autoResizable: true,
                        viewOption: {
                            flag : "org"
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
            $("input[name$='_O_INSPECT_ORDER']").popedit({

                open:function(e) {
                    var _this = $(this);
                    var options = {
                        url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/localOrderSelectView',
                        height: '80%',
                        width: '80%',
                        modal: false,
                        draggable: true,
                        resizable:true,
                        autoResizable: true,
                        viewOption: {
                            flag : "org",
                            productType :productType
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                _this.popedit('setValue', {name:res.applyOrdId, value:res.applyOrdId});
                                //var initGred = $.proxy(this.initGred,this); //函数作用域改变
                                digitalCircutAction.querySelectInfo(res.applyOrdId, function(data){
                                    // 修改页面
                                    var circuitData = data.circuitInfo;
                                    var gridData = $("#gridId").grid("getRowData");
                                    for (var circuitRow in circuitData){
                                        var tradeId = circuitData[circuitRow].tradeId;
                                        for(var gridRow in gridData){
                                            if(tradeId == gridData[gridRow].tradeId) {
                                                fish.warn("已经存在电路信息，不要重复选择！");
                                                return;
                                            }
                                        }
                                    }
                                    $("#gridId").grid("addRowData",1, circuitData, 'last');

                                })

                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    };
                    var popup = fish.popupView(options);
                }

            });
            $("input[name$='circuitCode']").popedit({
                open:function(e) {
                    var _this = $(this);
                    var options = {
                        url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/localCircuitCodeView',
                        height: '85%',
                        width: '80%',
                        modal: false,
                        draggable: true,
                        resizable:true,
                        autoResizable: true,
                        viewOption: {
                            flag : "org",
                            productType :productType //产品实例号
                        },
                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                _this.popedit('setValue', {name:res.circuitCode, value:res.circuitCode});
                                $('#oldCircuitCode').val(res.circuitCode); //原电路编号

                                $('input[name="INSTANCE_ID"]').val(res.prodInstId); //产品实例号
                                $('#serialNumber').val(res.accNbr); //业务号码
                                $("#serialNumber").attr("readonly","readonly");
                                $("#serialNumber").css("background-color","#eeeeee");
                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    };
                    var popup = fish.popupView(options);
                }
            });
            if(actType=="101"){
                $("#circuitCode").popedit('disable');
            };
            $("input[name$='A_belong_province']").popedit({

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
            $("input[name$='A_belong_city']").popedit({
                open:function(e) {
                    var _this = $(this);
                    var areaId;
                    if($("#A_belong_province").val()!=""){
                        areaId = $("#A_belong_province").popedit("getValue").value;
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
            $("input[name$='A_belong_county']").popedit({
                open:function(e) {
                    var _this = $(this);
                    var areaId;
                    if($("#A_belong_city").val()!=""){
                        areaId = $("#A_belong_city").popedit("getValue").value;
                    }else{
                        fish.warn("请先选择归属地市");
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
                }

            });
            $("input[name$='A_old_belong_county']").popedit({
                open:function(e) {
                    var _this = $(this);
                    var areaId = $("#A_belong_province").popedit("getValue").value;
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
                }

            });


            $("input[name$='Z_belong_province']").popedit({

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
            $("input[name$='Z_belong_city']").popedit({
                open:function(e) {
                    var _this = $(this);
                    var areaId;
                    if($("#Z_belong_province").val()!=""){
                        areaId = $("#Z_belong_province").popedit("getValue").value;
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
                    $("#Z_belong_county").val("");
                },

            });
            $("input[name$='Z_belong_county']").popedit({
                open:function(e) {
                    var _this = $(this);
                    var areaId;
                    if($("#Z_belong_city").val()!=""){
                        areaId = $("#Z_belong_city").popedit("getValue").value;
                    }else{
                        fish.warn("请先选择归属地市");
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
                }

            });
            $("input[name$='Z_old_belong_county']").popedit({
                open:function(e) {
                    var _this = $(this);
                    var areaId = $("#Z_belong_province").popedit("getValue").value;
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
                }

            });

            //根据登陆人初始化默认省
            digitalCircutAction.queryProvienceTree(function (data){
                $("input[name$='A_belong_province']").popedit('setValue', {name:data[0].name, value:data[0].id});
                $("input[name$='Z_belong_province']").popedit('setValue', {name:data[0].name, value:data[0].id});
            });
        },
        initGred:function (mydata) {
            var me =this;
            $("#gridId").grid("setGridHeight", "260", false);
            var opt = {
                rowattr: function(rowData,rowId){
                    if(rowData.orderType=='102'){
                        return {
                            style: 'background-color:#eeeabd;'
                        }
                    }
                },
                data: mydata,
                width: 800,
                height: 400,
                autowidth:true,
                showColumnsFeature: false, //允许用户自定义列展示设置
                cached: false, //把用户自定义的列展示设置缓存在本地
                //gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                multiselect: false,
                // shrinkToFit: false,
                autoResizable: true,
                rownumbers:true,
                shrinkToFit:false, // 表格列宽是否按比例缩放，默认true
                colModel: [
                    { name: 'SRV_ORD_ID',label: '业务表ID',width: 1,sorttype: "string",align:"center",hidden:true},
                    { name: 'INSTANCE_ID',label: '实例id',width: 150,sorttype: "string",align:"center",hidden:true},

                    { name: 'tradeId',label: '业务订单号',width: 150,sorttype: "string",align:"center"},
                    {name: 'serialNumber',label: '业务号码',width: 100,sorttype: "string",align:"center",},
                    {name: 'serviceId',label: '产品类型',width: 100,sorttype: "string",align:"center",hidden:true},
                    {name: 'serviceName',label: '产品类型',width: 100,sorttype: "string",align:"center",},
                    {name: 'circuitReq', label: '电路要求', width: 100,sorttype: "string",align:"center",hidden:true},
                    {name: 'circuitReqName', label: '电路要求', width: 100,sorttype: "string",align:"center",},
                    { name: 'circuitCode',label: '电路编号',width: 100,sorttype: "string",align:"center",},
                    { name: 'oldCircuitCode',label: '原电路编号',width: 100, sorttype: "string",align:"center",hidden:true},
                    {name: 'requFineTime',label: '全程要求完成时间',width: 160,sorttype: "string",align:"center",},
                    //{name: 'accepTime',label: '受理时间',width: 160,sorttype: "string",align:"center",},
                    {name: 'createTime',label: '创建时间',align:"center",width: 160,sorttype: "string",},
                    {name: 'slaFlag', label: 'SLA标识', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'slaServOpen', label: 'SLA业务开通', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'slaNetQuAss', label: 'SLA网络质量保证', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'slaSaleServ', label: 'SLA售后服务', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'proPriDeg', label: '工程缓急程度', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'cirLeaseRange', label: '电路租用范围', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'cirUse', label: '电路用途', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'cir_remark', label: '备注',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'RentCoreCount', label: '租用芯数',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'FiberLength', label: '光纤长度',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'FiberLengthUnit', label: '光纤长度单位',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'A_belong_province', label: 'A端归属省', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'A_belong_city', label: 'A端归属地市', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'A_belong_county', label: 'A端归属区县', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'A_belong_region', label: 'A端归属区域', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'A_belong_region_id', label: 'A端归属区域', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'A_customer_name', label: 'A端客户名称', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'A_installed_add', label: 'A端装机地址', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'A_contact_man', label: 'A端联系人', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'A_contact_tel', label: 'A端联系电话', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'A_customer_manager', label: 'A端客户经理', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'A_customer_manager_tel', label: 'A端客户经理联系方式', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'A_old_belong_region', label: 'A端原归属区域', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'A_old_belong_region_id', label: 'A端原归属区域', width: 1,sorttype: "string",align:"center",hidden:true},

                    {name: 'A_old_belong_county', label: 'A端原归属区县', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'A_old_installed_add', label: 'A端原装机地址', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'Z_belong_province', label: 'Z端归属省', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'Z_belong_city', label: 'Z端归属地市', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'Z_belong_county', label: 'Z端归属区县', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'Z_belong_region', label: 'Z端归属区域', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'Z_belong_region_id', label: 'Z端归属区域', width: 1,sorttype: "string",align:"center",hidden:true},

                    {name: 'Z_customer_name', label: 'Z端客户名称', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'Z_installed_add', label: 'Z端装机地址', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'Z_contact_man', label: 'Z端联系人', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'Z_contact_tel', label: 'Z端联系电话', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'Z_customer_manager', label: 'Z端客户经理', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'Z_customer_manager_tel', label: 'Z端客户经理联系方式', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'Z_old_belong_region', label: 'Z端原归属区域', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'Z_old_belong_region_id', label: 'Z端原归属区域', width: 1,sorttype: "string",align:"center",hidden:true},

                    {name: 'Z_old_belong_county', label: 'Z端原归属区县', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'Z_old_installed_add', label: 'Z端原装机地址', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'operationAfter',label: '操作',align:"center",width: 200,sorttype: "string",
                        formatter: function(cellval, opts, rwdat, _act) {
                            if (flag  == "view"){
                                return '<div class="btn-group">' +
                                    '<button type="button" class="btn btn-link js-view " title="查看"><span class="glyphicon glyphicon glyphicon-unchecked" style="font-size:15px;color: #eb4a4b"></span></button>' +
                                    '</div>'
                            }else{
                                return '<div class="btn-group">' +
                                    '<button type="button" class="btn btn-link js-delete " title="删除"> <span class="glyphicon glyphicon glyphicon-remove" style="font-size:15px;color: #eb4a4b"></span></button>' +
                                    '<button type="button" class="btn btn-link js-edit " title="编辑"><span class="glyphicon glyphicon glyphicon-edit" style="font-size:15px;color: #eb4a4b"></span></button>' +
                                    '</div>'
                            }
                        },
                    }]
            };

            $grid = $("#gridId").grid(opt);
            // 隐藏业务表ID列
            $grid.grid("hideCol",'SRV_ORD_ID');
            $grid.on('click', '.js-delete', function() {
                fish.confirm('确认是否删除该选项').result.then(function() {
                    var selrow = $grid.grid("getSelection");
                    $grid.grid("delRowData", selrow);//删除记录
                    //  fish.success('删除成功');
                });

            }).on('click', '.js-edit', function() {
                var popup = fish.popupView({
                    url: 'module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/updateBareFiberView',
                    width: "95%",
                    height:"90%",
                    modal:true,
                    canClose:true,
                    resizable:true,
                    viewOption:{
                        rowData:$grid.grid("getSelection"),
                        productType : productType,
                        actType:actType
                    },
                    callback:function(popup,view){
                        popup.result.then(function (ret) {
                            var selrow = $grid.grid("getSelection");
                            for(var key in ret){
                                selrow[""+key]=""+ret[key];
                            }
                            $grid.grid("setRowData", selrow);
                            fish.toast('success', '修改完成，点击更新或提交可保存本次操作');
                        },function (e) {
                            console.log('关闭了',e);


                        });
                    }
                });
            }).on('click', '.js-view', function() {
                    var popup = fish.popupView({
                        url: 'module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/updateBareFiberView',
                        width: "95%",
                        height:"90%",
                        modal:true,
                        canClose:true,
                        resizable:true,
                        viewOption:{
                            rowData:$grid.grid("getSelection"),
                            flag:"view",
                            productType : productType,
                            actType:actType
                        },
                        callback:function(popup,view){
                            popup.result.then(function (ret) {
                                var selrow = $grid.grid("getSelection");

                            },function (e) {
                                console.log('关闭了',e);

                            });
                        }
                    });
            })

            //表格数据为空时显示中文提示，默认英文
            $(".ui-jqgrid-tip").empty();
            $(".ui-jqgrid-tip").text('无数据显示');
        },
        //添加电路按钮
        addAndSelectRow:function() {
        var me=this;
        $(this).attr("disabled",true);
        setTimeout("$('#addAndSelectRow').attr('disabled', false)",1500); //延迟1.5S
        if (actType!="101" && actType!="107"){
            $('#bareFiber-Form').validator('setIgnoreField', 'CUST_EMAIL,CUST_NAME_CHINESE,HANDLE_CITY,HANDLE_MAN_NAME,HANDLE_MAN_TEL,HANDLE_DEP,CUST_CONTACT_MAN_NAME,CUST_TEL', true);
        }else{
            $('#bareFiber-Form').validator('setIgnoreField', 'CUST_EMAIL,CUST_NAME_CHINESE,circuitCode,HANDLE_CITY,HANDLE_MAN_NAME,HANDLE_MAN_TEL,HANDLE_DEP,CUST_CONTACT_MAN_NAME,CUST_TEL', true);
        }
        var checkNull = $("#bareFiber-Form").isValid();
        if (actType!="101" && actType!="107"){
            $('#bareFiber-Form').validator('setIgnoreField', 'CUST_EMAIL,CUST_NAME_CHINESE,HANDLE_CITY,HANDLE_MAN_NAME,HANDLE_MAN_TEL,HANDLE_DEP,CUST_CONTACT_MAN_NAME,CUST_TEL', false);
        }else{
            $('#bareFiber-Form').validator('setIgnoreField', 'CUST_EMAIL,CUST_NAME_CHINESE,circuitCode,HANDLE_CITY,HANDLE_MAN_NAME,HANDLE_MAN_TEL,HANDLE_DEP,CUST_CONTACT_MAN_NAME,CUST_TEL', false);
        }
        if(!checkNull){
            fish.error({title:'提示',message:'请检查必填数据或数据格式'})
            return;
        }
        /**
         新开时电路编号不能与现有电路编号重复；非新开时，电路编号与原电路编号保持一致，并且电路编号需要在数据库已开通的电路中能查到
         */
        var circuitCode = $("#circuitCode").val();
        /*var oldCircuitCode = $("#oldCircuitCode").val();
        if(actType != "101"){
            if(circuitCode!=oldCircuitCode){
                fish.error({title:'提示',message:'电路编号与原电路编号不一致'});
                return;
            }
        }*/
        var param = new Object();
        param.circuitCode = circuitCode;
        // param.oldCircuitCode = oldCircuitCode;
        param.actType = actType;
        param.productType = productType;
        //校验新开时多条电路业务订单号和业务号码
        var currCircuitData = $grid.grid("getRowData");
        if(currCircuitData.length>0 && $('#tradeId').val() != ""){
            for(var gridRow in currCircuitData){
                if($('#tradeId').val() == currCircuitData[gridRow].tradeId) {
                    fish.error({title:'提示',message:"不能添加重复的'业务订单号'"})
                    return;
                }
                if($('#serialNumber').val() == currCircuitData[gridRow].serialNumber) {
                    fish.error({title:'提示',message:"不能添加重复的'业务号码'！"})
                    return;
                }
            }
        }
        digitalCircutAction.checkTradeId({tradeId:$("#tradeId").val(),serialNumber:$("#serialNumber").val(),actType:actType,orderType:orderType.value},function (res) {
            if (res.result!='success') {
                fish.error({title: '提示', message: res.result})
                return;
            }
            //校验电路编号
            digitalCircutAction.checkCircuitCode(param, function(res){
                // debugger;
                if(res.success){
                    if(res.choice){
                        fish.confirm(res.message,function(){},function(){return;});
                    }
                    var circuitInfo = {};
                    var circuitCodeArea =['tradeId','serialNumber','serviceId','circuitReq','circuitCode','oldCircuitCode','requFineTime','accepTime','createTime','slaFlag', 'slaServOpen', 'slaNetQuAss', 'slaSaleServ', 'proPriDeg', 'cirLeaseRange', 'cirUse', 'serviceName','circuitReqName', 'cir_remark', 'RentCoreCount', 'FiberLength', 'FiberLengthUnit', 'A_belong_province', 'A_belong_city', 'A_belong_county', 'A_belong_region', 'A_customer_name', 'A_installed_add', 'A_contact_man', 'A_contact_tel', 'A_customer_manager', 'A_customer_manager_tel', 'A_old_belong_region', 'A_old_belong_county', 'A_old_installed_add', 'Z_belong_province', 'Z_belong_city', 'Z_belong_county', 'Z_belong_region', 'Z_customer_name', 'Z_installed_add', 'Z_contact_man', 'Z_contact_tel', 'Z_customer_manager', 'Z_customer_manager_tel', 'Z_old_belong_region', 'Z_old_belong_county', 'Z_old_installed_add','A_belong_region_id','Z_belong_region_id','A_old_belong_region_id','Z_old_belong_region_id','INSTANCE_ID','A_belong_city_id','Z_belong_city_id'];

                    for(var i=0;i<circuitCodeArea.length;i++){
                        //需要在表格中展示的枚举类型字段
                        if (actType == '101'){
                            if("INSTANCE_ID"==circuitCodeArea[i]){
                                var key = circuitCodeArea[i];
                                var value = '';
                                circuitInfo[""+key]=""+value;
                            }
                        }
                        if("serviceName"==circuitCodeArea[i]){
                            var key = circuitCodeArea[i];
                            var value = $("#serviceId").combobox('text');
                            circuitInfo[""+key]=""+value;
                        }
                        if("circuitReqName"==circuitCodeArea[i]){
                            var key = circuitCodeArea[i];
                            var value = $("#circuitReq").combobox('text');
                            circuitInfo[""+key]=""+value;
                        }
                        if("speedName"==circuitCodeArea[i]){
                            var key = circuitCodeArea[i];
                            var value = $("#speed").combobox('text');
                            circuitInfo[""+key]=""+value;
                        }

                        if("A_belong_region_id"==circuitCodeArea[i]){
                            var key = circuitCodeArea[i];
                            if($("#A_belong_region").popedit('getValue').value!=undefined){
                                var value = $("#A_belong_region").popedit('getValue').value;
                                circuitInfo[""+key]=""+value;
                            }
                        }
                        if("A_old_belong_region_id"==circuitCodeArea[i]){
                            var key = circuitCodeArea[i];
                            if($("#A_old_belong_region").val()!=""){
                                var value = $("#A_old_belong_region").popedit('getValue').value;
                                circuitInfo[""+key]=""+value;
                            }
                        }
                        if("Z_belong_region_id"==circuitCodeArea[i]){
                            // debugger;
                            var key = circuitCodeArea[i];
                            if($("#Z_belong_region").val()!=""){
                                var value = $("#Z_belong_region").popedit('getValue').value;
                                circuitInfo[""+key]=""+value;
                            }
                        }
                        if("Z_old_belong_region_id"==circuitCodeArea[i]){
                            var key = circuitCodeArea[i];
                            if($("#Z_old_belong_region").val()!=""){
                                var value = $("#Z_old_belong_region").popedit('getValue').value;
                                circuitInfo[""+key]=""+value;
                            }
                        }
                        if("A_belong_city_id"==circuitCodeArea[i]){
                            var key = circuitCodeArea[i];
                            if($("#A_belong_city").val()!=""){
                                var value = $("#A_belong_city").popedit('getValue').value;
                                circuitInfo[""+key]=""+value;
                            }
                        }if("Z_belong_city_id"==circuitCodeArea[i]){
                            var key = circuitCodeArea[i];
                            if($("#Z_belong_city").val()!=""){
                                var value = $("#Z_belong_city").popedit('getValue').value;
                                circuitInfo[""+key]=""+value;
                            }
                        }
                        var key = circuitCodeArea[i];
                        var value = $("#"+circuitCodeArea[i]).val();
                        if(value!=null|| value!=undefined){
                            circuitInfo[""+key]=""+value;
                        }
                    }
                    //   var rowid = newData.id;
                    $grid.grid("addRowData", circuitInfo);
                    $grid.grid("setSelection", circuitInfo);
                    // $("#circuit_info")
                    $(':input','#circuit-Info-Form')
                        .not(':button, :submit, :reset, :hidden, :disabled')
                        .val('')
                        .removeAttr('checked')
                        .removeAttr('selected');//批量清空电路信息表单
                    var date = new Date();
                    $("#createTime").datetimepicker("value", date,"disable");
                    //根据登陆人初始化默认省
                    digitalCircutAction.queryProvienceTree(function (data){
                        $("input[name$='A_belong_province']").popedit('setValue', {name:data[0].name, value:data[0].id});
                        $("input[name$='Z_belong_province']").popedit('setValue', {name:data[0].name, value:data[0].id});
                    });
                    //初始化地市信息
                    digitalCircutAction.queryProvienceTree2({flag:'city'},function (data) {
                        if(data.length==1){
                            $("input[name$='A_belong_city']").popedit('setValue', {name:data[0].name, value:data[0].id});
                            $("input[name$='Z_belong_city']").popedit('setValue', {name:data[0].name, value:data[0].id});
                            $("input[name$='HANDLE_CITY']").popedit('setValue', {name:data[0].name, value:data[0].id});
                        }});
                    //初始化下拉框默认值
                    //me.initCombobox();
                    $("#proPriDeg").combobox('value', 1);
                    $("#slaFlag").combobox('value', 1);
                    $("#cirLeaseRange").combobox('value',5);
                    $("#slaServOpen").combobox('value', 4);  // 定义默认值
                    $("#slaNetQuAss").combobox('value', 4);  // 定义默认值
                    $("#slaSaleServ").combobox('value', 4);  // 定义默认
                } else {
                    fish.error({title:'提示',message:res.message});
                    return;
                }
            });
        });
    },
        initPageParam:function () {
            $("#CUST_NAME_CHINESE").change(function (e) {
                var bussActive = $("#ACTIVE_TYPE").combobox('text');
                var custName = $("#CUST_NAME_CHINESE").val();
                if(custName==""){
                    $("#APPLY_ORD_NAME").val("");
                }
                else {
                    $("#APPLY_ORD_NAME").val(custName+"-"+bussActive);
                }
                $("#APPLY_ORD_NAME").attr("readonly","readonly");
                $("#APPLY_ORD_NAME").off('click');
            });
            $("#APPLY_ORD_NAME").click(function () {
                $("#accordion").accordion("option", "active", 1);
                $("#CUST_NAME_CHINESE").focus();
                fish.toast('info', '请先选择客户名称！');            });
            if(actType!="101" && actType!="107" ){
                var str = '<span style="color: red">*</span>电路编号';
                document.getElementById('dynamicRequired').innerHTML=str;
            };
            $("#circuitCode").change(function () {
                $("#oldCircuitCode").val($("#circuitCode").val());
            });
            if(actType=="101"){
                $("#oldCircuitCode").attr("readonly","readonly");
                $("#oldCircuitCode").css("background-color","#eeeeee");
                $("#old_Circuit_Code").hide();
                $("#A_old_belong_region_code").hide();
                $("#Z_old_belong_region_code").hide();
                $("#A_old_belong_county_code").hide();
                $("#Z_old_belong_county_code").hide();
                $("#A_old_installed_add_code").hide();
                $("#Z_old_installed_add_code").hide();
            };
        },

        initDigPage:function () {
            //获取父页面参数
            productType = this.options.productType.value;
            productTypeText = this.options.productType.name;
            actType = this.options.actType.value;
            actTypeText = this.options.actType.name;
            srvOrdId = this.options.srvOrdId;
            custOrdId = this.options.custOrdId;
            flag = this.options.flag;
            queryType = this.options.queryType; //草稿单/已完成/已提交
            if(queryType=='draftorder'){
                queryType = '10C';
            }else if(queryType=='completeorder'){
                queryType = '10F';
            }else if(queryType=='submitorder'){
                queryType = '10N';
            }else if(queryType=='wholeorder'){
                queryType = "";
            }
             orderType = this.options.itemTypeStart;
            $("#applicat-Page-Title").html(productTypeText+"-"+actTypeText);

            //编辑草稿，隐藏电路信息的form

            console.log(flag);
            if(flag  == "edit" ){
                //$("#add-circuit-button").hide();
                //$("#circuit-Info-Form").hide();
                $("#save").hide();
                $("#UpdateSave").show();
                this.querySelectedInf(custOrdId);
                this.queryAttachment(custOrdId);
                $("#ORDER_TYPE").combobox({ dataSource: [{value:orderType}]});
                $("#ORDER_TYPE").combobox("value",orderType);
            }else if(flag  == "start") {
                //发起页面
                $("#ORDER_TYPE").combobox({ dataSource: [{name: orderType.name, value:orderType.value}]});
                $("#ORDER_TYPE").combobox("value",orderType.value);
                //$("#APPLY_ORD_ID_CODE").hide();
                $("#APPLY_ORD_ID").attr("readonly","readonly");
                $('#APPLY_ORD_ID').attr('disabled',true);
                $("#APPLY_ORD_ID").css("background-color","#eeeeee");

                //modify by wang.g2
                $("#APPLY_ORD_NAME").attr("readonly","readonly");
                $('#APPLY_ORD_NAME').attr('disabled',true);
                $("#APPLY_ORD_NAME").css("background-color","#eeeeee");
                
                $("#HANDLE_TIME").datetimepicker("value", new Date());
                //初始化受理部门默认值
                digitalCircutAction.queryOperStaffInfo(function (data) {
                    $("input[name$='HANDLE_DEP']").popedit('setValue', {name:data.ORG_NAME, value:data.ORG_ID});
                });
                digitalCircutAction.queryProvienceTree2({flag:'city'},function (data) {
                    if(data.length==1){
                        $("input[name$='A_belong_city']").popedit('setValue', {name:data[0].name, value:data[0].id});
                        $("input[name$='Z_belong_city']").popedit('setValue', {name:data[0].name, value:data[0].id});
                        $("input[name$='HANDLE_CITY']").popedit('setValue', {name:data[0].name, value:data[0].id});
                    }});
                this.initGred();
            }else if (flag  == "view"){
                this.querySelectedInf(custOrdId);
                this.queryAttachment(custOrdId);
                $(".modal-footer").hide();
                $("#add-circuit-button").hide();
                $("#circuit-Info-Form").hide();
            } else{
                this.initGred();
            }

        },
        //查询草稿工单的信息 业务订单信息 客户订单信息 电路信息
        querySelectedInf :function(CustId){
            var params = new Object();
            params.CustId=CustId;
            params.cgFlag =  queryType;
            var initGred = $.proxy(this.initGred,this); //函数作用域改变
            digitalCircutAction.querySelectedInfo(params, function(data){
                // 修改页面
                console.log(data);
                var srvOrdData = data.servCustInfo;
                var circuitData = data.circuitInfo;

                var srvOrdDataJson = JSON.parse(JSON.stringify(srvOrdData));
                var circuitDataJson =  JSON.stringify(circuitData);
                for (var item in srvOrdDataJson){
                    //console.log(item+"==>"+srvOrdDataJson[item]);
                    $("#"+item).val(""+srvOrdDataJson[item])
                    // 因为不知道什么原因，客户类型在上边方法赋值后仍不能显示，所以使用fish提供的方法为客户类型赋值
                    if (item == "CUST_TYPE") {
                        $('#CUST_TYPE').combobox('value',srvOrdDataJson[item]);
                    }
                    if (item == "IS_GROUP_CUST") {
                        $('#IS_GROUP_CUST').combobox('value',srvOrdDataJson[item]);
                    }
                    if(item == "HANDLE_DEP"){
                        $('#HANDLE_DEP').popedit('setValue', {name:srvOrdDataJson['HANDLE_DEP'], value:srvOrdDataJson['HANDLE_DEP_ID']});
                    }
                    if(item == "HANDLE_CITY"){
                        $('#HANDLE_CITY').popedit('setValue', {name:srvOrdDataJson['HANDLE_CITY'], value:srvOrdDataJson['HANDLE_CITY_ID']});
                    }
                }
                initGred(circuitData);

            })
        },
        //查询附件方法
        queryAttachment:function(CustId){
            digitalCircutAction.queryAttachment(CustId, function (data) {
                fileData = data;
                if (fileData.length != 0) {
                    $('#ANNEX_down').show();
                    var FILE_LIST_TPL = '<li class="list-group-item">' +
                        '                       <div class="url" style="display: inline-block;width: 90%">' +
                        '                           <span class="glyphicon glyphicon-file"></span>' +
                        '                           <span>{{fileName}}({{fileSize}})</span>' +
                        '                           <span class="fileId" style="display:none">{{fileId}}</span>' +
                        '                       </div>' +
                        '                       <span class="file-remove glyphicon glyphicon-remove pull-right"></span></li>';
                    $.each(fileData, function (i,obj) {
                        // 展示数据库中存的附件
                        $('.file-list').append(fish.compile(FILE_LIST_TPL)({fileName: obj.FILE_NAME,fileId: obj.FILE_ID,fileSize: obj.FILE_SIZE + "KB"}));
                        // 如果是查看申请单，则隐藏删除按钮
                        if (flag  == "view"){
                            $('.file-remove').hide();
                        }
                    })
                    $('.file-remove').off('click').on('click', function(e) {
                        fish.confirm('确认是否删除该附件').result.then(function() {
                            // 获取FILE_ID
                            var fileId = $(e.currentTarget).prev().children(".fileId").text(), pos;
                            // 创建需要传入删除附件的参数
                            var fileInfo = new Object();

                            // 找到点击的FILE
                            $.each(fileData, function(index, j) {
                                if (j.FILE_ID === fileId) {
                                    pos = index;

                                    // 设置所选删除附件的信息
                                    fileInfo.fileId = j.FILE_ID;
                                    fileInfo.filePath = j.FILE_PATH;
                                    fileInfo.fileName = j.FILE_ID + '.' + j.FILE_TYPE;
                                    return false;
                                }
                            });
                            // 从数组中删除点击的元素
                            fileData.splice(pos, 1);
                            // 删除ftp和数据库中的附件
                            digitalCircutAction.delFileOnFtp(fileInfo);
                            // 从视图上移除file
                            $(e.currentTarget).parent().remove();

                            upLoadResult = new Array();
                        });
                    });
                    $('.url').off('click').on('click', function(e) {
                        var fileId = $(e.currentTarget).children(".fileId").text();
                        $.each(fileData, function(index, obj) {
                            if (obj.FILE_ID === fileId) {
                                //调用下载的方法
                                digitalCircutAction.downLoadAttachMent("localScheduleLT/ApplOrder/attachmentDownload.spr", obj);
                            }
                        });

                    });

                }
            })
        },
        //查询工单方法
        queryWorkOrders: function(page, rowNum, sortname, sortorder) {
            rowNum = rowNum || this.$("#orderDeal-grid").grid("getGridParam", "rowNum");
            fish.store.set('orderDeal-grid-rowNum', rowNum); //记录用户选择的每页记录数
            page = page || 1;

            //登陆人信息
            var paramsMap = {};

            //分页信息
            paramsMap.pageIndex = page+'';
            paramsMap.pageSize = rowNum+'';

            //排序信息
            if (sortname && sortorder) {
                paramsMap.sortCol = this.camelToUnderline(sortname);
                paramsMap.sortOrder = sortorder.toUpperCase();;
            }

            //获取表单信息
            var formValue = $('#bareFiber-Form').form("value");

            //任务管理必要查询条件
            paramsMap.workOrderState = this.workOrderState;
            paramsMap.orderClass = '1OA';

            //调用后台方法
            $("#orderDeal-grid").blockUI({message: '加载中'}).data('blockui-content', true);
            orderDealAction.queryStaffInfo(function (data) {

                alert(data);
                //$("#orderDeal-grid").grid("reloadData", data);
            }.bind(this));
            $("#orderDeal-grid").unblockUI().data('blockui-content', false);
        },
        submit:function () {
            if(isFileFinsh=="wait"){
                fish.warn("附件上传中，请稍后!");
                return;
            }
            else if(isFileFinsh=="error"){
                fish.error("附件上传失败，请重新上传!");
                return;
            }
            $('#bareFiber-Form').validator('setIgnoreField', 'tradeId,serialNumber,circuitCode,A_belong_region,Z_belong_region,requFineTime,Z_installed_add' +
                ',cirLeaseRange,cirUse,A_belong_province,A_belong_city,Z_belong_province,Z_belong_city,A_installed_add,A_contact_man,A_contact_tel' +
                ',Z_contact_man,Z_contact_tel,Z_installed_add', true);

            var checkNull = $("#bareFiber-Form").isValid();
            $('#bareFiber-Form').validator('setIgnoreField', 'tradeId,serialNumber,circuitCode,A_belong_region,Z_belong_region,requFineTime,Z_installed_add' +
                ',cirLeaseRange,cirUse,A_belong_province,A_belong_city,Z_belong_province,Z_belong_city,A_installed_add,A_contact_man,A_contact_tel' +
                ',Z_contact_man,Z_contact_tel,Z_installed_add', false);
            if(!checkNull){
                // fish.toast("success","");
                fish.error({title:'提示',message:'请检查必填数据或数据格式'})
                return;
            }
            // 判断电路信息是否为空
            var flag = false;
            var data = $grid.grid("getRowData");//获取所有数据,如果传入参数rowid,则获取那一条记录
            if (data.length == 0){
                fish.toast('warn', '电路信息不能为空');
                flag = true;
            } else {
                data.forEach(function(val, i){
                    if (val.serialNumber == null) {
                        fish.toast('warn', '存在电路未添加业务号码，请添加！');
                        flag = true;
                    }
                });
            }
            if (flag){
                return;
            }
            var formValue = $('#bareFiber-Form').form("value");
            //部门和地市
            formValue.HANDLE_DEP = $("#HANDLE_DEP").popedit('getValue').name;
            formValue.HANDLE_DEP_ID = $("#HANDLE_DEP").popedit('getValue').value;
            formValue.HANDLE_CITY = $("#HANDLE_CITY").popedit('getValue').name;
            formValue.HANDLE_CITY_ID = $("#HANDLE_CITY").popedit('getValue').value;

            var orderInfoObj = new Object();
            orderInfoObj.cirData = data;
            orderInfoObj.OrderCustmInfo = formValue;
            orderInfoObj.upLoadResult=upLoadResult;
            orderInfoObj.CUST_ID =custOrdId;

            var _this =this;
            $("#bareFiber-Form").blockUI({message: '请稍后...'}).data('blockui-content', true);
            $("#submit").attr("disabled",true);
            digitalCircutAction.orderInfoSubmit(orderInfoObj,function(data) {
                $("#submit").attr("disabled",false);
                // fish.info("保存成功")
                var arrdata=new Array();
                arrdata=data.split(',');
                if(arrdata[0]=='success'){
                    upLoadResult=new Array();//全局数值变量手动置空

                    if(flag  == "edit" ){
                        fish.info("提交成功!");
                    }else {
                        fish.info("提交成功! 申请单号：【"+arrdata[1]+"】");
                    }
                    // fish.toast("success","");
                    $("#bareFiber-Form").unblockUI().data('blockui-content', false);
                    _this.popup.close();
                }else{
                    $("#bareFiber-Form").unblockUI().data('blockui-content', false);
                    fish.error("提交失败！"+arrdata[2])
                }
            }); },
        save:function () {
            if(isFileFinsh=="wait"){
                fish.warn("附件上传中，请稍后!");
                return;
            }
            else if(isFileFinsh=="error"){
                fish.error("附件上传失败，请重新上传!");
                return;
            }
            $('#bareFiber-Form').validator('setIgnoreField', 'tradeId,serialNumber,circuitCode,A_belong_region,Z_belong_region,requFineTime,Z_installed_add' +
                ',cirLeaseRange,cirUse,A_belong_province,A_belong_city,Z_belong_province,Z_belong_city,A_installed_add,A_contact_man,A_contact_tel' +
                ',Z_contact_man,Z_contact_tel', true);
            var checkNull = $("#bareFiber-Form").isValid();
            $('#bareFiber-Form').validator('setIgnoreField', 'tradeId,serialNumber,circuitCode,A_belong_region,Z_belong_region,requFineTime,Z_installed_add' +
                ',cirLeaseRange,cirUse,A_belong_province,A_belong_city,Z_belong_province,Z_belong_city,A_installed_add,A_contact_man,A_contact_tel' +
                ',Z_contact_man,Z_contact_tel', false);
            if(!checkNull){
                // fish.toast("success","");
                fish.error({title:'提示',message:'请检查必填数据或数据格式'})
                return;
            }

            // 判断电路信息是否为空
            var data = $grid.grid("getRowData");//获取所有数据,如果传入参数rowid,则获取那一条记录
            if (data.length == 0){
                fish.toast('warn', '电路信息不能为空');
                return;
            }
            var formValue = $('#bareFiber-Form').form("value");
            //部门和地市
            formValue.HANDLE_DEP = $("#HANDLE_DEP").popedit('getValue').name;
            formValue.HANDLE_DEP_ID = $("#HANDLE_DEP").popedit('getValue').value;
            formValue.HANDLE_CITY = $("#HANDLE_CITY").popedit('getValue').name;
            formValue.HANDLE_CITY_ID = $("#HANDLE_CITY").popedit('getValue').value;

            var orderInfoObj = new Object();
            orderInfoObj.cirData = data;
            orderInfoObj.OrderCustmInfo = formValue;
            orderInfoObj.upLoadResult=upLoadResult;

            var _this =this;
            $("#bareFiber-Form").blockUI({message: '保存中'}).data('blockui-content', true);
            digitalCircutAction.orderInfoSave(orderInfoObj,function(data) {
                // fish.info("保存成功")
                var arrdata=new Array();
                arrdata=data.split(',');
                if(arrdata[0]=='success'){
                    upLoadResult=new Array(); //全局数值变量手动置空

                    fish.info("保存草稿成功! 申请单号：【"+arrdata[1]+"】");
                    $("#bareFiber-Form").unblockUI().data('blockui-content', false);
                    _this.popup.close();
                }else{
                    $("#bareFiber-Form").unblockUI().data('blockui-content', false);
                    fish.error("保存草稿失败！"+arrdata[2])
                }
            });
        },
        cancel:function () {
            upLoadResult=new Array(); //全局数值变量手动置空

            this.popup.close();
        },
        reset:function () {
            $grid.grid("clearGridData"); //清空表格数据
            $('input')
                .not(':button, :submit, :reset, :disabled')
                .val('')
                .removeAttr('checked')
                .removeAttr('selected');//批量清空表单
            //初始化下拉框
            this.initCombobox();
            fish.toast('warn', '已重置');
        },
        UpdateSave : function(){
            if(isFileFinsh=="wait"){
                fish.warn("附件上传中，请稍后!");
                return;
            }
            else if(isFileFinsh=="error"){
                fish.error("附件上传失败，请重新上传!");
                return;
            }
            $('#bareFiber-Form').validator('setIgnoreField', 'tradeId,serialNumber,circuitCode,A_belong_region,Z_belong_region,requFineTime,Z_installed_add' +
                ',cirLeaseRange,cirUse,A_belong_province,A_belong_city,Z_belong_province,Z_belong_city,A_installed_add,A_contact_man,A_contact_tel' +
                ',Z_contact_man,Z_contact_tel,Z_installed_add,Z_installed_add', true);
            var checkNull = $("#bareFiber-Form").isValid();
            $('#bareFiber-Form').validator('setIgnoreField', 'tradeId,serialNumber,circuitCode,A_belong_region,Z_belong_region,requFineTime,Z_installed_add' +
                ',cirLeaseRange,cirUse,A_belong_province,A_belong_city,Z_belong_province,Z_belong_city,A_installed_add,A_contact_man,A_contact_tel' +
                ',Z_contact_man,Z_contact_tel,Z_installed_add,Z_installed_add', false);
            if(!checkNull){
                // fish.toast("success","");
                fish.error({title:'提示',message:'请检查必填数据或数据格式'})
                return;
            }
            var data = $grid.grid("getRowData");//获取所有数据,如果传入参数rowid,则获取那一条记录
            var formValue = $('#bareFiber-Form').form("value");
            //部门和地市
            formValue.HANDLE_DEP = $("#HANDLE_DEP").popedit('getValue').name;
            formValue.HANDLE_DEP_ID = $("#HANDLE_DEP").popedit('getValue').value;
            formValue.HANDLE_CITY = $("#HANDLE_CITY").popedit('getValue').name;
            formValue.HANDLE_CITY_ID = $("#HANDLE_CITY").popedit('getValue').value;

            var orderInfoObj = new Object();
            //电路信息
            orderInfoObj.cirData = data;

            //客户与订单信息
            orderInfoObj.OrderCustmInfo = formValue;
            orderInfoObj.CUST_ID =custOrdId;
            orderInfoObj.upLoadResult=upLoadResult;

            var _this =this;
            $("#bareFiber-Form").blockUI({message: '保存中'}).data('blockui-content', true);
            digitalCircutAction.orderInfoUpdate(orderInfoObj,function(data) {
                // fish.info("保存成功")
                if(data=='success'){
                    upLoadResult=null; //全局数值变量手动置空

                    fish.toast("success","保存草稿成功");
                    _this.popup.close();
                    //   fish.info({title:"提示",message:"保存草稿成功！"});
                    /*fish.confirm("保存成功，是否继续编辑？").result.then(function (flag) {
                     if(!flag){
                     _this.popup.close();
                     }
                     })*/
                }else{
                    fish.error("保存草稿失败！")
                }
                $("#bareFiber-Form").unblockUI().data('blockui-content', false);
            });
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

        fileSelect:function() {
            //URL为文件上传的地址，支持额外参数拼接。例如'batch_task?name=aaa&age=22&height=180'
            $('#progress .progress-bar').css('width', 0 + '%');
            $('#progress .progress-bar').show();
            $("#attaceFinsh").text("");
            var urlUpLoad = this.getRootPath()+"/localScheduleLT/initProdFileUploadController/uploadFiles.spr";
            FILE_LIST_TPL =  '<li class="list-group-item">' +
                '                       <div class="url" style="display: inline-block;width: 90%">' +
                '                           <span class="glyphicon glyphicon-file"></span>' +
                '                           <span class="fileName">{{fileName}}</span>' +
                '                           <span>({{fileSize}})</span>' +
                '                       </div>' +
                '                       <span class="file-remove glyphicon glyphicon-remove pull-right"></span></li>';
            $('.js-select-file').fileupload({
                dataType: 'json',
                autoUpload: false,
                acceptFileTypes: '',
                done: function (e, data) {
                    $.each(data.originalFiles,function(index,file){
                        var fileName = decodeURIComponent(file.name);
                        $('<p/>').text(fileName + '   upload suceess, file.size:' + file.size).appendTo('#files');
                    })
                },
                progressall: function (e, data) {
                    var progress = parseInt(data.loaded / data.total * 100, 10);
                    $('#progress .progress-bar').css(
                        'width',
                        progress + '%'
                    );
                },
                processfail: function (e, data) {
                    var index = data.index,
                        file = data.files[index];
                    if (file.error && file.error === "File type not allowed" ) {
                        fish.error({message:"选择的文件类型不符，只能上传图片类型文件(以gif|jpg|jpeg|png结尾的文件)", modal:true});
                        return;
                    }
                },
                fail: function (e, data) {
                    $('<p/>').text("文件上传失败  " + data.errorThrown).appendTo('#files');
                },
                always:function(e,data){
                    //upLoadResult = data.result;
                    upLoadResult.push(data.result)
                    if(data.result!="error"){
                        $("#attaceFinsh").text("上传完成");
                        $('#progress .progress-bar').hide();
                        $('#progress').hide();
                        isFileFinsh = "success";
                    }
                    else{
                        $("#attaceFinsh").attr("style","color:#eb4a4b");
                        $('#progress .progress-bar').css(
                            'background','#eb4a4b'
                        );
                        $("#attaceFinsh").text("上传失败");
                        isFileFinsh = "error";
                    }
                },
                add: function (e, data) {
                    if (data.files[0].size > maxFileSize) {
                        fish.warn('选择的文件大小超过上限50M，请重新选择');
                        return;
                    }
                    if (fileList != null && fileList.files.length >= maxFileCount) {
                        fish.warn('选择的文件个数超过上限5个，请减少文件数');
                        return;
                    }
                    var obj = data.files[0],
                        fileName = decodeURIComponent(obj.name),
                        fileSize = (obj.size / 1024.0).toFixed(2) + "KB";

                  /*  if (fileList === null) {
                        fileList = data;
                    } else {
                        fileList.files.push(obj);
                    }*/
                    fileList = data;
                    $('.file-list').append(fish.compile(FILE_LIST_TPL)({
                        fileName: fileName,
                        fileSize: fileSize
                    }));

                    $('.file-remove').off('click').on('click', function (e) {
                        var name = $(e.currentTarget).prev().children(".fileName").text(), pos,pos2;
                        $.each(fileList.files, function (index, file) {
                            if (file.name === name) {
                                pos = index;
                                return false;
                            }
                        });
                        var delFileName="";
                        //获取删除upLoadResult中对应附件信息的索引
                        for (var i = 0; i < upLoadResult.length; i++) {
                            if(upLoadResult[i][0].fileName==name){
                                delFileName=upLoadResult[i][0].fileId+"."+upLoadResult[i][0].fileType;
                                pos2=i;
                            }
                        }

                        fileList.files.splice(pos, 1);
                        upLoadResult.splice(pos2, 1);
                        $(e.currentTarget).parent().remove();
                        var fileInfo = new Object();
                        fileInfo.filePath = 'createbuss';
                        fileInfo.fileName = delFileName;
                        digitalCircutAction.delFileOnFtp(fileInfo);
                    });
                    fileList.url = urlUpLoad;
                    var result = fileList.submit();//自动上传
                }
            })
        },

    }); //fish.View.extend END
}); //ALL END