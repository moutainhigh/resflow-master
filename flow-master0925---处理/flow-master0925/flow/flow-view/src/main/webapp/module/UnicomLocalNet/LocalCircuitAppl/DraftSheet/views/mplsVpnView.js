define(['module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/action/digitalCircutAction.js',
    'text!module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/templates/mplsVpnView.html',
    'i18n!module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/i18n/digitalCircuit.i18n',
    'css!module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/css/digitalCircuit.css'
], function(digitalCircutAction,mplsVpnView,i18n,css) {
    var $grid;
    var productType;
    var productTypeText;
    var actType;//动作类型 （新开，变更，关闭。。。。）
    var orderType;//单据类型 ：（核查单，开通单）    var queryType;
    var actTypeText;
    var srvOrdId;
    var custOrdId;
    var flag;
    var fileList = null,
        maxFileSize = 50*1024*1024,                //文件大小限制
        maxFileCount = 5 ;              //文件个数限制
    var upLoadResult = new Array();
    var fileData; // 文件数组
    return fish.View.extend({
        isFileFinsh : "success",
        isReleCreateApplication:false, //是否关联新开单
        template: fish.compile(mplsVpnView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #submit': 'submit',
            'click #save': 'save',
            'click #cancel': 'cancel',
            'click #reset': 'reset',
            'click #UpdateSave' : 'UpdateSave',
            'click .js-select-file': 'fileSelect',

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
            /* digitalCircutAction.queryEnum('10001302',function (data) {

                 $('#circuitType').combobox('option', 'dataSource', data);
             });*/
            /* 页面所有需要枚举字段通用初始化方法。一次完成，需要在表单中class 后加menu+枚举值priperty_Id
              例如：<input id="circuitType"  name="circuitType" class="form-control menu 10000517" >
              该方法会获取所有表单一次性初始化枚举值 ren.jiahang
              */
            var menu = $(".menu");
            for(var i =0;i<menu.length;i++){
                var objClassName = menu[i].className;
                var objId = menu[i].id;
                var objName = menu[i].name;
                var startSub = objClassName.indexOf('menu')+5;
                var enumType = objClassName.substring(startSub,objClassName.length);
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
                        } else if (obj.selector == ".isnetmanage" || obj.selector == ".EnableMulticast" || obj.selector == ".10000176") {
                            $(obj).combobox('value', 1); //是否 默认是。
                        }
                        $("#proPriDeg").combobox('value', 1);
                        $("#slaFlag").combobox('value', 1);
                        $("#cirLeaseRange").combobox('value', 1);
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
                active: 1,//此属性为false时,collapsible=true时,默认不展开页签;collapsible=false时,默认展开第一个页签 此属性为数值时,表示默认展开页签的索引,从0开始 此属性为负值时,表示默认展开页签的索引,从最后一个往前数 *要修改多个属性时，请先销毁组件，再重新初始化
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
            var me=this;
            $("input[name$='BelongRegin']").popedit({

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
                    var areaId = $("#PE_BelongProv").popedit("getValue").value;
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
                                    digitalCircutAction.queryCustInfoByAppId(res.applyOrdId,function (result) {
                                        queryType='10F';
                                        me.isReleCreateApplication=true;
                                        me.querySelectedInf(result.CST_ORD_ID);
                                    })
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
            $("input[name$='RELE_CREATE_APPLICATION']").popedit({
                open:function(e) {
                    var _this = $(this);
                    var options = {
                        url: 'module/UnicomLocalNet/resmaster/portal/orderQuery/views/GomReleOrderListView',
                        height: '100%',
                        width: '100%',
                        modal: false,
                        draggable: true,
                        resizable:true,
                        autoResizable: true,
                        viewOption: {
                            flag : "org",
                            productType :productType,
                            orderType:orderType //单据类型

                        },

                        callback: function (popup, view) {
                            popup.result.then(function (res) {
                                _this.popedit('setValue', {name:res.APPLY_ORD_ID,value:res.APPLY_ORD_ID});
                                queryType='10F';
                                me.isReleCreateApplication=true;

                                me.querySelectedInf(res.CST_ORD_ID);


                            }, function (e) {
                                console.log('关闭了', e);
                            });
                        }
                    };
                    var popup = fish.popupView(options);
                }

            });
            $("input[name$='PE_BelongProv']").popedit({

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
                    if($("#PE_BelongProv").val()!=""){
                        areaId = $("#PE_BelongProv").popedit("getValue").value;
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
                    $("#PE_BelongCounty").val("");
                },

            });
            $("input[name$='PE_BelongCounty']").popedit({
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
            $("input[name$='PE_oldBelongCounty']").popedit({
                open:function(e) {
                    var _this = $(this);
                    var areaId = $("#PE_BelongProv").popedit("getValue").value;
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

            $("input[name$='CE_BelongProv']").popedit({

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
                    if($("#CE_BelongProv").val()!=""){
                        areaId = $("#CE_BelongProv").popedit("getValue").value;
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
                    $("#CE_BelongCounty").val("");
                },

            });
            $("input[name$='CE_BelongCounty']").popedit({
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
            $("input[name$='CE_oldBelongCounty']").popedit({
                open:function(e) {
                    var _this = $(this);
                    var areaId = $("#PE_BelongProv").popedit("getValue").value;
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
                $("input[name$='PE_BelongProv']").popedit('setValue', {name:data[0].name, value:data[0].id});
                $("input[name$='CE_BelongProv']").popedit('setValue', {name:data[0].name, value:data[0].id});
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
                    { name: 'SRV_ORD_ID',label: '业务表ID',width: 1,sorttype: "string",align:"center",hideCol:true},
                    { name: 'INSTANCE_ID',label: '实例id',width: 150,sorttype: "string",align:"center",hidden:true},

                    { name: 'tradeId',label: '业务订单号',width: 160,sorttype: "string",align:"center"},
                    {name: 'serialNumber',label: '业务号码',width: 100,sorttype: "string",align:"center",},
                    {name: 'serviceId',label: '产品类型',width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'serviceName',label: '产品类型',width: 100,sorttype: "string",align:"center",},
                    {name: 'circuitReq', label: '电路要求', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'circuitReqName', label: '电路要求', width: 100,sorttype: "string",align:"center",},
                    { name: 'circuitCode',label: '电路编号',width: 100,sorttype: "string",align:"center",},
                    { name: 'oldCircuitCode',label: '原电路编号',width: 100, sorttype: "string",align:"center",hidden:true},

                    {name: 'custManager',label: '客户经理', width: 100,sorttype: "string",align:"center",},
                    {name: 'custManaPhone',label: '客户经理联系电话', width: 140,sorttype: "string",align:"center",},
                    {name: 'requFineTime',label: '全程要求完成时间',width: 160,sorttype: "string",align:"center",},
                    //{name: 'accepTime',label: '受理时间',width: 160,sorttype: "string",align:"center",},
                    {name: 'createTime',label: '创建时间',align:"center",width: 160,sorttype: "string",},
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
                    },
                    {name: 'slaFlag', label: 'SLA标识', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'slaServOpen', label: 'SLA业务开通', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'slaNetQuAss', label: 'SLA网络质量保证', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'slaSaleServ', label: 'SLA售后服务', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'proPriDeg', label: '工程缓急程度', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'cirLeaseRange', label: '电路租用范围', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'BILLID', label: '计费ID',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'cir_remark', label: '备注',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'qos', label: 'QOS',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'qosTpye', label: 'QOS类型',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'isUseInInternet', label: '是否接入在用网络',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'ExistCircuitNum', label: '已有电路编号',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'ExistServNum', label: '已有业务号码',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'isGradeFlowMarkObeyCU', label: '等级流量标识是否服从CU',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'routing_protocol', label: '备注备注备注备注',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'intranetAddrOrMask', label: '内网地址/掩码/',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'AsNumbe', label: 'AsNumber号(对端AS号/协议类型)',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'EnableMulticast', label: '开启组播功能',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'isnetmanage', label: '是否组播源',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'IPAddrType', label: 'IP地址类型',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'PE_BelongProv', label: 'PE端归属省',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'A_belong_city', label: 'PE端归属地市',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'PE_BelongCounty', label: 'PE端归属区县',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'PE_BelongRegin', label: 'PE端归属区域',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'PE_PortRange', label: 'PE端口范围',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'PE_PortRate', label: 'PE端口速率',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'PE_Addr', label: 'PE地址',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'PE_interfaceType', label: 'PE端接口类型',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'PE_IPV4Mask', label: 'IPv4互联IP/掩码(PE)',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'PE_IPV6Mask', label: 'IPv6互联IP/掩码(PE)',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'PE_oldBelongRegin', label: 'PE端原归属区域',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'PE_oldBelongCounty', label: 'PE端原归属区县',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'PE_oldAddr', label: 'PE端原地址',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'CE_BelongProv', label: 'CE端归属省',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'Z_belong_city', label: 'CE端归属地市',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'CE_BelongCounty', label: 'CE端归属区县',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'CE_BelongRegin', label: 'CE端归属区域',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'CE_CustName', label: 'CE端客户名称',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'CE_CustInstall', label: 'CE端安装地址',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'CE_interfaceType', label: 'CE端接口类型',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'CE_IPV4Mask', label: 'IPv4互联IP/掩码(CE)',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'CE_IPV6Mask', label: 'IPv6互联IP/掩码(CE)',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'speed', label: '接入带宽',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'AccessCircuitType', label: '接入电路类型',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'AccessOperator', label: '接入运营商',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'CE_oldBelongRegin', label: 'CE端原归属区域',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'CE_oldBelongCounty', label: 'CE端原归属区县',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'CE_oldAddr', label: 'CE端原安装地址',  width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'PE_BelongRegin_id', label: 'A端归属区域', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'PE_oldBelongRegin_id', label: 'A端原归属区域', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'CE_BelongRegin_id', label: 'Z端归属区域', width: 1,sorttype: "string",align:"center",hidden:true},
                    {name: 'CE_oldBelongRegin_id', label: 'Z端原归属区域', width: 1,sorttype: "string",align:"center",hidden:true},
                    ]
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
                    url: 'module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/updateMplsVpnView',
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
                        url: 'module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/views/updateMplsVpnView',
                        width: "95%",
                        height:"90%",
                        modal:true,
                        canClose:true,
                        resizable:true,
                        viewOption:{
                            rowData:$grid.grid("getSelection"),
                            flag:"view"
                        },
                        callback:function(popup,view){
                            popup.result.then(function (ret) {
                                var selrow = $grid.grid("getSelection");

                            },function (e) {
                                console.log('关闭了',e);

                            });
                        }
                    });
            });
            //添加电路按钮
             $("#addAndSelectRow").off('click').on('click',function(e) {
                $(this).attr("disabled",true);
                setTimeout("$('#addAndSelectRow').attr('disabled', false)",1500); //延迟1.5S
                if (actType!="101" && actType!="107"){
                    $('#mplsVpn-Form').validator('setIgnoreField', 'CUST_TEL,CUST_NAME_CHINESE,HANDLE_CITY,HANDLE_MAN_NAME,HANDLE_MAN_TEL,HANDLE_DEP,CUST_CONTACT_MAN_NAME', true);
                }else{
                    $('#mplsVpn-Form').validator('setIgnoreField', 'CUST_TEL,CUST_NAME_CHINESE,circuitCode,HANDLE_CITY,HANDLE_MAN_NAME,HANDLE_MAN_TEL,HANDLE_DEP,CUST_CONTACT_MAN_NAME', true);
                }
                var checkNull = $("#mplsVpn-Form").isValid();
                if (actType!="101" && actType!="107"){
                    $('#mplsVpn-Form').validator('setIgnoreField', 'CUST_TEL,CUST_NAME_CHINESE,HANDLE_CITY,HANDLE_MAN_NAME,HANDLE_MAN_TEL,HANDLE_DEP,CUST_CONTACT_MAN_NAME', false);
                }else{
                    $('#mplsVpn-Form').validator('setIgnoreField', 'CUST_TEL,CUST_NAME_CHINESE,circuitCode,HANDLE_CITY,HANDLE_MAN_NAME,HANDLE_MAN_TEL,HANDLE_DEP,CUST_CONTACT_MAN_NAME', false);
                }
                if(!checkNull){
                    fish.error({title:'提示',message:'检查必填属性不能为空'})
                    return;
                }
                /**
                 新开时电路编号不能与现有电路编号重复；非新开时，电路编号与原电路编号保持一致，并且电路编号需要在数据库已开通的电路中能查到
                 */
                var circuitCode = $("#circuitCode").val();
                var param = new Object();
                param.circuitCode = circuitCode;
                // param.oldCircuitCode = oldCircuitCode;
                param.actType = actType;
                param.productType = productType;
                 if(digitalCircutAction.checkCircuitCodeBefore(circuitCode,$("#gridId").grid("getRowData"))){
                     fish.warn("已经存在相同电路编号的电路");
                     return;
                 }
                digitalCircutAction.checkTradeId({tradeId:$("#tradeId").val(),serialNumber:$("#serialNumber").val(),actType:actType,orderType:orderType.value},function (res) {
                    if (res.result!='success') {
                        fish.error({title: '提示', message: res.result})
                        return;
                    }
                    //校验电路编号
                    digitalCircutAction.checkCircuitCode(param, function(res){
                        if(res.success){
                            if(res.choice){
                                fish.confirm(res.message,function(){},function(){return;});
                            }
                            //var formValue = $('#digitalCir-Form').form("value");
                            var circuitInfo = {};
                            var circuitCodeArea =['tradeId','serialNumber','serviceId','speedName','circuitReq','circuitCode','oldCircuitCode','custManager','custManaPhone','requFineTime','accepTime','createTime','slaFlag', 'slaServOpen', 'slaNetQuAss', 'slaSaleServ', 'proPriDeg', 'cirLeaseRange','serviceName','circuitReqName','BILLID', 'cir_remark', 'qos', 'qosTpye', 'isUseInInternet', 'ExistCircuitNum', 'ExistServNum', 'isGradeFlowMarkObeyCU', 'routing_protocol', 'intranetAddrOrMask', 'AsNumbe', 'EnableMulticast', 'isnetmanage', 'IPAddrType', 'PE_BelongProv', 'A_belong_city', 'PE_BelongCounty', 'PE_BelongRegin', 'PE_PortRange', 'PE_PortRate', 'PE_Addr', 'PE_interfaceType', 'PE_IPV4Mask', 'PE_IPV6Mask', 'PE_oldBelongRegin', 'PE_oldBelongCounty', 'PE_oldAddr', 'CE_BelongProv', 'Z_belong_city', 'CE_BelongCounty', 'CE_BelongRegin', 'CE_CustName', 'CE_CustInstall', 'CE_interfaceType', 'CE_IPV4Mask', 'CE_IPV6Mask', 'speed', 'AccessCircuitType', 'AccessOperator', 'CE_oldBelongRegin', 'CE_oldBelongCounty', 'CE_oldAddr','PE_BelongRegin_id','PE_oldBelongRegin_id','CE_BelongRegin_id','CE_oldBelongRegin_id','INSTANCE_ID'];

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
                                else if("circuitReqName"==circuitCodeArea[i]){
                                    var key = circuitCodeArea[i];
                                    var value = $("#circuitReq").combobox('text');
                                    circuitInfo[""+key]=""+value;
                                }
                                else if("speedName"==circuitCodeArea[i]){
                                    var key = circuitCodeArea[i];
                                    var value = $("#speed").combobox('text');
                                    circuitInfo[""+key]=""+value;
                                }
                                else if("PE_BelongRegin_id"==circuitCodeArea[i]){
                                    var key = circuitCodeArea[i];
                                    if($("#PE_BelongRegin").popedit('getValue').value!=undefined){
                                        var value = $("#PE_BelongRegin").popedit('getValue').value;
                                        circuitInfo[""+key]=""+value;
                                    }
                                }
                                else if("PE_oldBelongRegin_id"==circuitCodeArea[i]){
                                    var key = circuitCodeArea[i];
                                    if($("#PE_oldBelongRegin").val()!=""){
                                        var value = $("#PE_oldBelongRegin").popedit('getValue').value;
                                        circuitInfo[""+key]=""+value;
                                    }
                                }
                                else if("CE_BelongRegin_id"==circuitCodeArea[i]){
                                    var key = circuitCodeArea[i];
                                    if($("#CE_BelongRegin").popedit('getValue').value!=undefined){
                                        var value = $("#CE_BelongRegin").popedit('getValue').value;
                                        circuitInfo[""+key]=""+value;
                                    }
                                }
                                else if("CE_oldBelongRegin_id"==circuitCodeArea[i]){
                                    var key = circuitCodeArea[i];
                                    if($("#CE_oldBelongRegin").val()!=""){
                                        var value = $("#CE_oldBelongRegin").popedit('getValue').value;
                                        circuitInfo[""+key]=""+value;
                                    }
                                }
                                else {
                                    var key = circuitCodeArea[i];
                                    var value = $("#"+circuitCodeArea[i]).val();
                                    if(value!=null|| value!=undefined){
                                        circuitInfo[""+key]=""+value;
                                    }
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
                                $("input[name$='PE_BelongProv']").popedit('setValue', {name:data[0].name, value:data[0].id});
                                $("input[name$='CE_BelongProv']").popedit('setValue', {name:data[0].name, value:data[0].id});
                            });
                            //初始化地市
                            digitalCircutAction.queryProvienceTree2({flag:'city'},function (data) {
                                if(data.length==1){
                                    $("input[name$='A_belong_city']").popedit('setValue', {name:data[0].name, value:data[0].id});
                                    $("input[name$='Z_belong_city']").popedit('setValue', {name:data[0].name, value:data[0].id});
                                    $("input[name$='HANDLE_CITY']").popedit('setValue', {name:data[0].name, value:data[0].id});
                                }});
                            //初始化下拉框默认值
                            // me.initCombobox();
                            $("#proPriDeg").combobox('value', 1);
                            $("#slaFlag").combobox('value', 1);
                            $("#cirLeaseRange").combobox('value', 1);
                            $("#slaServOpen").combobox('value', 4);  // 定义默认值
                            $("#slaNetQuAss").combobox('value', 4);  // 定义默认值
                            $("#slaSaleServ").combobox('value', 4);  // 定义默认
                        } else {
                            fish.error({title:'提示',message:res.message});
                            return;
                        }
                    });
                });
            });
            //表格数据为空时显示中文提示，默认英文
            $(".ui-jqgrid-tip").empty();
            $(".ui-jqgrid-tip").text('无数据显示');
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
                //任何产品的新开，【电路编号】【原电路编号】应该置灰
                $("#circuitCode").attr("readonly","readonly");
                $('#circuitCode').attr('disabled',true);
                $("#circuitCode").css("background-color","#eeeeee");
                $("#oldCircuitCode").attr("readonly","readonly");
                $("#oldCircuitCode").css("background-color","#eeeeee");
                $("#oldCircuitCode").hide();
                $("#oldCircuitCode_code").hide();
                $("#PE_old_Belong_Regin").hide();
                $("#PE_old_Belong_County").hide();
                $("#PE_old_Addr").hide();
                $("#CE_old_Belong_Regin").hide();
                $("#CE_old_Belong_County").hide();
                $("#CE_old_Addr").hide();
                $("#RELE_CREATE_APPLICATION_DIV").hide();
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
               // $("#circuit-Info-Form").hide();
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
                $("#APPLY_ORD_ID").css("background-color","#eeeeee");

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
                        $("input[name$='Z_belong_city']").popedit('setValue', {name:data[0].name, value:data[0].id});
                        $("input[name$='A_belong_city']").popedit('setValue', {name:data[0].name, value:data[0].id});
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
            var me=this;
            var params = new Object();
            params.CustId=CustId;
            params.cgFlag =  queryType;
            var initGred = $.proxy(this.initGred,this); //函数作用域改变
            digitalCircutAction.querySelectedInfo(params, function(data){
                // 修改页面
                console.log(data);
                var activeTypeName = $('#ACTIVE_TYPE').combobox('text');
                var activeType = $('#ACTIVE_TYPE').combobox('value');
                var srvOrdData = data.servCustInfo;
                var circuitData = data.circuitInfo;
                var srvOrdDataJson = JSON.parse(JSON.stringify(srvOrdData));
                var circuitDataJson =  JSON.stringify(circuitData);
                if(me.isReleCreateApplication){
                    srvOrdDataJson.APPLY_ORD_NAME=srvOrdData.CUST_NAME_CHINESE+'-'+activeTypeName;
                    srvOrdDataJson.ACTIVE_TYPE=activeType;
                    delete srvOrdDataJson.APPLY_ORD_ID;
                    delete srvOrdDataJson.RELE_CREATE_APPLICATION;
                    delete srvOrdDataJson.RELE_O_INSPECT_ORDER;
                    for (var item in circuitData){
                        circuitData[item].circuitReq=activeType;
                        circuitData[item].circuitReqName=activeTypeName;
                        circuitData[item].createTime = digitalCircutAction.formatDate(new Date().getTime());
                        delete circuitData[item].业务实例创建接口返回结果;
                        delete circuitData[item].业务实例归档接口返回结果;
                        delete circuitData[item].业务电路汇总接口返回结果;
                    }
                }
                for (var item in srvOrdDataJson){
                    //console.log(item+"==>"+srvOrdDataJson[item]);
                    $("#"+item).val(""+srvOrdDataJson[item]);
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
                //由于MV核查单和开通单字段标识不一样，避免一个字段入库两次，这里对电路信息进行下特殊处理
                var circuitDataArr = me.dealSrvOrdAttrInfo(circuitData);
                //将关联的O域核查单电路信息填充到grid中
                $("#gridId").grid("reloadData", circuitDataArr);
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
            var formValue = $('#mplsVpn-Form').form("value");

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
            if(this.isFileFinsh=="wait"){
                fish.warn("附件上传中，请稍后!");
                return;
            }
            else if(this.isFileFinsh=="error"){
                fish.error("附件上传失败，请重新上传!");
                this.isFileFinsh='';
                return;
            }
            $('#mplsVpn-Form').validator('setIgnoreField', 'tradeId,serialNumber,circuitCode,CE_BelongRegin,PE_BelongRegin,speed' +
                ',CE_interfaceType,PE_interfaceType,cirLeaseRange,PE_BelongProv,CE_BelongProv,A_belong_city,Z_belong_city,CE_CustInstall,requFineTime', true);
            var checkNull = $("#mplsVpn-Form").isValid();
            $('#mplsVpn-Form').validator('setIgnoreField', 'tradeId,serialNumber,circuitCode,CE_BelongRegin,PE_BelongRegin,speed' +
                ',CE_interfaceType,PE_interfaceType,cirLeaseRange,PE_BelongProv,CE_BelongProv,A_belong_city,Z_belong_city,CE_CustInstall,requFineTime', false);
            if(!checkNull){
                // fish.toast("success","");
                // debugger;
                fish.error({title:'提示',message:'请检查必填属性及联系方式'})
                return;
            }
            function clear(key) {
                $('#'+key+'_msg_holder').html('');
            }
            function setErrors(ret) {
                var html = '<p class="red">'+ ret.msg +'</p>';
                $('#'+ret.key+'_msg_holder').html(html);
            }

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
            var formValue = $('#mplsVpn-Form').form("value");
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
            $("#mplsVpn-Form").blockUI({message: '请稍后...'}).data('blockui-content', true);
            digitalCircutAction.orderInfoSubmit(orderInfoObj,function(data) {
                // fish.info("保存成功")
                var arrdata=new Array();
                arrdata=data.split(',');
                if(arrdata[0]=='success'){
                    upLoadResult=new Array(); //全局数值变量手动置空

                    if(flag  == "edit" ){
                        fish.info("提交成功!");
                    }else {
                        fish.info("提交成功! 申请单号：【"+arrdata[1]+"】");
                    }
                    // fish.toast("success","");
                    $("#mplsVpn-Form").unblockUI().data('blockui-content', false);
                    _this.popup.close();
                }else{
                    $("#mplsVpn-Form").unblockUI().data('blockui-content', false);
                    fish.error("提交失败！"+arrdata[2])
                }
            }); },
        save:function () {
            if(this.isFileFinsh=="wait"){
                fish.warn("附件上传中，请稍后!");
                return;
            }
            else if(this.isFileFinsh=="error"){
                fish.error("附件上传失败，请重新上传!");
                this.isFileFinsh='';
                return;
            }
            $('#mplsVpn-Form').validator('setIgnoreField', 'tradeId,serialNumber,circuitCode,CE_BelongRegin,PE_BelongRegin,speed' +
                ',CE_interfaceType,PE_interfaceType,cirLeaseRange,PE_BelongProv,CE_BelongProv,A_belong_city,Z_belong_city,CE_CustInstall,requFineTime', true);
            var checkNull = $("#mplsVpn-Form").isValid();
            $('#mplsVpn-Form').validator('setIgnoreField', 'tradeId,serialNumber,circuitCode,CE_BelongRegin,PE_BelongRegin,speed' +
                ',CE_interfaceType,PE_interfaceType,cirLeaseRange,PE_BelongProv,CE_BelongProv,A_belong_city,Z_belong_city,CE_CustInstall,requFineTime', false);
            if(!checkNull){
                // fish.toast("success","");
                // debugger;
                fish.error({title:'提示',message:'请检查必填属性及联系方式'})
                return;
            }

            // 判断电路信息是否为空
            var data = $grid.grid("getRowData");//获取所有数据,如果传入参数rowid,则获取那一条记录
            if (data.length == 0){
                fish.toast('warn', '电路信息不能为空');
                return;
            }
            var formValue = $('#mplsVpn-Form').form("value");
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
            $("#mplsVpn-Form").blockUI({message: '保存中'}).data('blockui-content', true);
            digitalCircutAction.orderInfoSave(orderInfoObj,function(data) {
                // fish.info("保存成功")
                var arrdata=new Array();
                arrdata=data.split(',');
                if(arrdata[0]=='success'){
                    upLoadResult=new Array();//全局数值变量手动置空

                    fish.info("保存草稿成功! 申请单号：【"+arrdata[1]+"】");
                    $("#mplsVpn-Form").unblockUI().data('blockui-content', false);
                    _this.popup.close();
                }else{
                    $("#mplsVpn-Form").unblockUI().data('blockui-content', false);
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
            if(this.isFileFinsh=="wait"){
                fish.warn("附件上传中，请稍后!");
                return;
            }
            else if(this.isFileFinsh=="error"){
                fish.error("附件上传失败，请重新上传!");
                this.isFileFinsh='';
                return;
            }
            $('#mplsVpn-Form').validator('setIgnoreField', 'tradeId,serialNumber,circuitCode,CE_BelongRegin,PE_BelongRegin,speed' +
                ',CE_interfaceType,PE_interfaceType,cirLeaseRange,PE_BelongProv,CE_BelongProv,A_belong_city,Z_belong_city,CE_CustInstall,requFineTime', true);
            var checkNull = $("#mplsVpn-Form").isValid();
            $('#mplsVpn-Form').validator('setIgnoreField', 'tradeId,serialNumber,circuitCode,CE_BelongRegin,PE_BelongRegin,speed' +
                ',CE_interfaceType,PE_interfaceType,cirLeaseRange,PE_BelongProv,CE_BelongProv,A_belong_city,Z_belong_city,CE_CustInstall,requFineTime', false);
            if(!checkNull){
                // fish.toast("success","");
                fish.error({title:'提示',message:'请检查必填数据或数据格式'});
                return;
            }
            var data = $grid.grid("getRowData");//获取所有数据,如果传入参数rowid,则获取那一条记录
            var formValue = $('#mplsVpn-Form').form("value");
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
            $("#mplsVpn-Form").blockUI({message: '保存中'}).data('blockui-content', true);
            digitalCircutAction.orderInfoUpdate(orderInfoObj,function(data) {
                // fish.info("保存成功")
                if(data=='success'){
                    upLoadResult=new Array(); //全局数值变量手动置空

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
                $("#mplsVpn-Form").unblockUI().data('blockui-content', false);
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
                        this.isFileFinsh = "success";
                    }
                    else{
                        $("#attaceFinsh").attr("style","color:#eb4a4b");
                        $('#progress .progress-bar').css(
                            'background','#eb4a4b'
                        );
                        $("#attaceFinsh").text("上传失败");
                        this.isFileFinsh = "error";
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
        dealSrvOrdAttrInfo:function(circuitData){
            var circuitDataArr = new Array();
            if (circuitData.length > 0){
                for(var i= 0; i < circuitData.length; i++){
                    var param = {};
                    var circuitMap = circuitData[i];
                    //遍历map
                    for (var key in circuitMap){
                        if (key == 'tradeId'){
                            param.tradeId = circuitMap[key];
                        }else if (key == 'serialNumber'){
                            param.serialNumber = circuitMap[key];
                        }else if (key == 'serviceId'){
                            param.serviceId = circuitMap[key];
                        }else if (key == 'circuitReq'){
                            param.circuitReq = circuitMap[key];
                        }else if (key == 'circuitCode'){
                            param.circuitCode = circuitMap[key];
                        }else if (key == 'speed'){
                            param.speed = circuitMap[key];
                        }else if (key == 'cirLeaseRange'){
                            param.cirLeaseRange = circuitMap[key];
                        }else if (key == 'slaFlag'){
                            param.slaFlag = circuitMap[key];
                        }else if (key == 'slaServOpen'){
                            param.slaServOpen = circuitMap[key];
                        }else if (key == 'slaNetQuAss'){
                            param.slaNetQuAss = circuitMap[key];
                        }else if (key == 'slaSaleServ'){
                            param.slaSaleServ = circuitMap[key];
                        }else if (key == 'requFineTime'){
                            param.requFineTime = circuitMap[key];
                        }else if (key == 'proPriDeg'){
                            param.proPriDeg = circuitMap[key];
                        }else if (key == 'createTime'){
                            param.createTime = circuitMap[key];
                        }else if (key == 'oldCircuitCode'){
                            param.oldCircuitCode = circuitMap[key];
                        }else if (key == 'PE_BelongProv'){
                            param.PE_BelongProv = circuitMap[key];
                        }else if (key == 'A_belong_city'){
                            param.A_belong_city = circuitMap[key];
                        }else if (key == 'PE_BelongCounty'){
                            param.PE_BelongCounty = circuitMap[key];
                        }else if (key == 'PE_BelongRegin'){
                            param.PE_BelongRegin = circuitMap[key];
                        }else if (key == 'A_customer_name'){
                            param.A_customer_name = circuitMap[key];
                        }
                        else if (key == 'PE_interfaceType'){
                            param.PE_interfaceType = circuitMap[key];
                        }
                        else if (key == 'PE_Addr'){
                            param.PE_Addr = circuitMap[key];
                        }
                        else if (key == 'PE_oldBelongRegin'){
                            param.PE_oldBelongRegin = circuitMap[key];
                        }
                        else if (key == 'PE_oldBelongCounty'){
                            param.PE_oldBelongCounty = circuitMap[key];
                        }
                        else if (key == 'PE_oldAddr'){
                            param.PE_oldAddr = circuitMap[key];
                        }
                        else if (key == 'CE_BelongProv'){
                            param.CE_BelongProv = circuitMap[key];
                        }else if (key == 'Z_belong_city'){
                            param.Z_belong_city = circuitMap[key];
                        }else if (key == 'CE_BelongCounty'){
                            param.CE_BelongCounty = circuitMap[key];
                        }else if (key == 'CE_BelongRegin'){
                            param.CE_BelongRegin = circuitMap[key];
                        }else if (key == 'CE_CustName'){
                            param.CE_CustName = circuitMap[key];
                        }else if (key == 'CE_interfaceType'){
                            param.CE_interfaceType = circuitMap[key];
                        }else if (key == 'CE_CustInstall'){
                            param.CE_CustInstall = circuitMap[key];
                        }else if (key == 'CE_oldBelongRegin'){
                            param.CE_oldBelongRegin = circuitMap[key];
                        }else if (key == 'CE_oldBelongCounty'){
                            param.CE_oldBelongCounty = circuitMap[key];
                        }else if (key == 'Z_old_installed_add'){
                            param.Z_old_installed_add = circuitMap[key];
                        }else if (key == 'INSTANCE_ID'){
                            param.INSTANCE_ID = circuitMap[key];
                        }else if (key == 'cir_remark'){
                            param.cir_remark = circuitMap[key];
                        } else if (key == 'serviceName'){
                            param.serviceName = circuitMap[key];
                        }

                    }
                    circuitDataArr.push(param);
                }
            }
            return circuitDataArr;
        }
    }); //fish.View.extend END
}); //ALL END