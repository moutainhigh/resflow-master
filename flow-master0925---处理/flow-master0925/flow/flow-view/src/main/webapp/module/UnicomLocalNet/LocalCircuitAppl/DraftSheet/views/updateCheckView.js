﻿define(['module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/action/digitalCircutAction.js',
        'text!module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/templates/updateCheckView.html',
        'i18n!module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/i18n/digitalCircuit.i18n',
        'css!module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/css/digitalCircuit.css'
        ], function(digitalCircutAction,updateCircuitView,i18n,css) {
    return fish.View.extend({
        template: fish.compile(updateCircuitView),
        i18nData: fish.extend({}, i18n),
        events: {
            'click #sub_save-button': 'sub_save',
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
            $('.date').datetimepicker({
                orientation:{y:'bottom'}
            });//初始化时间组件
            $("#sub_createTime").datetimepicker("disable");
            this.initCombobox();
            var arrayData = new Map();
            arrayData  =this.options.rowData;
            var flag = this.options.flag;
            if (flag == "view"){
                $(".modal-footer").hide();
            }

            var actType = this.options.actType;
            this.initTree(arrayData,actType);

            if(actType!="101" && actType!="107" ){
                var str = '<span style="color: red">*</span>电路编号';
                document.getElementById('sub_dynamicRequired').innerHTML=str;
                document.getElementById('sub_circuitCode').setAttribute("data-rule","required");
            }
            this.initForm(actType);
        },
        sub_save:function () {
            var checkNull = $("#sub_sevMenuForm").isValid();
            if (!checkNull) {
                // fish.toast("success","");
                fish.error({title: '提示', message: '请核查数据必填或正确格式'})
                return;
            }
            //获取form所有字段，包括为空的
            var formValue = {};
            var t = $('#sub_sevMenuForm').serializeArray();
            $.each(t, function() {
                formValue[this.name] = this.value;
            });
            formValue["speedName"]=$('#sub_speed').combobox("text");

            if($("#sub_A_belong_region").val()!=""){
                formValue["A_belong_region"]=""+$("#sub_A_belong_region").popedit('getValue').name;
                formValue["A_belong_region_id"]=""+$("#sub_A_belong_region").popedit('getValue').value;
            }
            if($("#sub_Z_belong_region").val()!=""){
                formValue["Z_belong_region"]=""+$("#sub_Z_belong_region").popedit('getValue').name;
                formValue["Z_belong_region_id"]=""+$("#sub_Z_belong_region").popedit('getValue').value;
            }
            if($("#sub_A_old_belong_region").val()!=""){
                formValue["A_old_belong_region"]=""+$("#sub_A_old_belong_region").popedit('getValue').name;
                formValue["A_old_belong_region_id"]=""+$("#sub_A_old_belong_region").popedit('getValue').value;
            }
            if($("#sub_Z_old_belong_region").val()!=""){
                formValue["Z_old_belong_region"]=""+$("#sub_Z_old_belong_region").popedit('getValue').name;
                formValue["Z_old_belong_region_id"]=""+$("#sub_Z_old_belong_region").popedit('getValue').value;
            }
            //地市区县等取中文名保存
            for(var key in formValue){
                if(key.indexOf("_province")>=0||key.indexOf("_city")>=0||key.indexOf("_county")>=0){
                    //formValue[key]=$("#sub_"+key).popedit('getValue').name;
                    formValue[key]=$("#sub_"+key).val();
                }
            }
            var me = this;
            var actType = me.options.actType;
            var productType = this.options.productType;
            var circuitCode = $("#sub_circuitCode").val();
            // var oldCircuitCode = $("#sub_oldCircuitCode").val();
            // if(actType != "101"){
            //     if(circuitCode!=oldCircuitCode){
            //         fish.error({title:'提示',message:'电路编号与原电路编号不一致'});
            //         return;
            //     }
            // }
            var param = new Object();
            param.circuitCode = circuitCode;
            // param.oldCircuitCode = oldCircuitCode;
            param.actType = actType;
            param.productType = productType;
            //校验电路编号
            digitalCircutAction.checkCircuitCode(param, function(res){
                    if(res.success){
                        if(res.choice){
                            fish.confirm(res.message,function(){},function(){return;});
                        }
                        me.popup.close(formValue);
                    } else {
                        fish.error({title:'提示',message:res.message});
                        return;
                    }
                });

           // fish.info("保存成功！")
        },
        initTree:function (arrayData,actType) {
            for ( var key in arrayData) {
                //通过遍历对象属性的方法，遍历键值对，获得key，然后通过 对象[key]获得对应的值
                var  value = arrayData[key];
                var keyCodeEmnu = 'PE_BelongProv,PE_BelongCounty,PE_BelongRegin,PE_interfaceType,PE_Addr,CE_BelongProv,CE_BelongCounty,CE_BelongRegin,CE_interfaceType';
                var ifNeed = false;
                //var keyCodeEmnu = 'A_belong_province, A_belong_county, A_belong_region, A_interface_type, Z_belong_province, Z_belong_county, Z_belong_region, Z_interface_type, Z_installed_add';
                if (key.indexOf(keyCodeEmnu)) {
                    ifNeed = true;
                    if (key == 'CE_BelongProv'){
                        key = 'A_belong_province';
                    }else if (key == 'CE_BelongCounty'){
                        key = 'A_belong_county';
                    }else if (key == 'CE_BelongRegin'){
                        key = 'A_belong_region';
                    }else if (key == 'CE_interfaceType'){
                        key = 'A_interface_type';
                    }else if (key == 'PE_BelongProv'){
                        key = 'Z_belong_province';
                    }else if (key == 'PE_BelongCounty'){
                        key = 'Z_belong_county';
                    }else if (key == 'PE_BelongRegin'){
                        key = 'Z_belong_region';
                    }else if (key == 'PE_interfaceType'){
                        key = 'Z_interface_type';
                    }else if (key == 'PE_Addr'){
                        key = 'Z_installed_add';
                    }
                }
                if(key.indexOf("_province")>=0){
                    $("#sub_"+key).popedit({
                        initialData: {
                            'name':arrayData[key] ,
                            'value':""
                        },
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
                }
                if(key.indexOf("circuitCode")>=0){
                    var productType = this.options.productType;
                    $("#sub_"+key).popedit({
                        initialData: {
                            'name':arrayData[key] ,
                            'value':""
                        },
                        open:function(e) {
                            var _this = $(this);
                            var options = {
                                url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/localCircuitCodeView',
                                height: '80%',
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
                                        $('input[name="sub_oldCircuitCode"]').val(res.circuitCode);

                                        $('input[name="serialNumber"]').val(res.serialNumber);
                                        $('#serialNumber').attr('disabled',true);
                                        $('input[name="tradeId"]').val(res.tradeId);
                                        //res.serviceName = $("#SERVICE_ID").combobox('text');
                                        //$("#gridId").grid("addRowData",1, res, 'last');
                                    }, function (e) {
                                        console.log('关闭了', e);
                                    });
                                }
                            };
                            var popup = fish.popupView(options);
                        }

                    });
                }
                if(actType=="101"){
                    $("#sub_circuitCode").popedit('disable');
                };
                if(key.indexOf("Z_belong_city")>=0){
                    $("#sub_"+key).popedit({
                        initialData: {
                            'name':arrayData[key] ,
                            'value':""
                        },
                        open:function(e) {
                            var _this = $(this);
                            var areaName = $("#sub_Z_belong_province").popedit("getValue").name;
                            digitalCircutAction.queryAreaIdByName(areaName,function (data) {
                                var areaId = data.ID;
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
                            });
                        }

                    });
                }
                if(key.indexOf("A_belong_city")>=0){
                    $("#sub_"+key).popedit({
                        initialData: {
                            'name':arrayData[key] ,
                            'value':""
                        },
                        open:function(e) {
                            var _this = $(this);
                            var areaName = $("#sub_Z_belong_province").popedit("getValue").name;
                            digitalCircutAction.queryAreaIdByName(areaName,function (data) {
                                var areaId = data.ID;
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
                            });
                        }

                    });
                }
                if(key.indexOf("A_belong_county")>=0){
                    $("#sub_"+key).popedit({
                        initialData: {
                            'name':arrayData[key] ,
                            'value':""
                        },
                        open:function(e) {
                            var _this = $(this);
                            var areaName;
                            if($("#sub_A_belong_city").val()!=""){
                                areaName = $("#sub_A_belong_city").popedit("getValue").name;
                            }else{
                                fish.warn("请先选择归属地市");
                                _this.popedit.close();
                            }
                            digitalCircutAction.queryAreaIdByName(areaName,function (data) {
                                var areaId = data.ID;
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
                            });
                        }

                    });
                }
                if(key.indexOf("Z_belong_county")>=0){
                    $("#sub_"+key).popedit({
                        initialData: {
                            'name':arrayData[key] ,
                            'value':""
                        },
                        open:function(e) {
                            var _this = $(this);
                            var areaName;
                            if($("#sub_Z_belong_city").val()!=""){
                                areaName = $("#sub_Z_belong_city").popedit("getValue").name;
                            }else{
                                fish.warn("请先选择归属地市");
                                _this.popedit.close();
                            }
                            digitalCircutAction.queryAreaIdByName(areaName,function (data) {
                                var areaId = data.ID;
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
                            });
                        }

                    });
                }
                if(key.indexOf("_old_belong_county")>=0){
                    $("#sub_"+key).popedit({
                        initialData: {
                            'name':arrayData[key] ,
                            'value':""
                        },
                        open:function(e) {
                            var _this = $(this);
                            var areaName = $("#sub_Z_belong_province").popedit("getValue").name;
                            digitalCircutAction.queryAreaIdByName(areaName,function (data) {
                                var areaId = data.ID;
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
                            });
                        }

                    });
                }
                if(key.indexOf("_region")>=0){
                    var azValue = '';
                    if (ifNeed){
                        if (key == 'A_belong_region_id'){
                            azValue = arrayData["CE_BelongRegin"];
                        }else if (key == 'Z_belong_region_id'){
                            azValue = arrayData["PE_BelongRegin"];
                        }
                    }
                    $("#sub_"+key).popedit({
                        initialData: {
                            'name':arrayData[key] ,
                            'value':azValue == '' ? arrayData[key+"_id"] : azValue
                        },
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
                }
                $("#sub_"+key).val(value);
            }
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
                var startSub = objClassName.indexOf('menu')+9;
                var enumType = objClassName.substring(startSub,objClassName.length);
                var obj = '#'+objId;
                digitalCircutAction.queryEnum(enumType,function (data) {
                    var array = new Array();
                    array =data;
                    var enumTypeMap = new Map();
                    enumTypeMap = array[array.length-1];
                    var enumTypeB = ".sub_"+enumTypeMap.enumCode;
                    var obj = $(enumTypeB);
                    data.splice(data.length-1,1);//删除list中最后一个元素。
                    $(obj).combobox({
                        placeholder: '--请选择--',
                        dataSource:data,
                        editable:true
                    });
                    if(obj.selector==".sub_product_code"){
                        $(".sub_product_code").combobox("disable");

                    }else if (obj.selector==".sub_operate_type"){
                        $(".sub_operate_type").combobox('disable');
                    }
                });
            };
        },
        initForm:function (actType) {
            if(actType=='101'){
                $("#sub_circuitCode").attr("readonly","readonly");
                $('#sub_circuitCode').attr('disabled',true);
                $("#sub_circuitCode").css("background-color","#eeeeee");
                $("#sub_oldCircuitCode_code").hide();
                $("#sub_A_old_belong_region_code").hide();
                $("#sub_Z_old_belong_region_code").hide();
                $("#sub_A_old_belong_county_code").hide();
                $("#sub_Z_old_belong_county_code").hide();
                $("#sub_A_old_installed_add_code").hide();
                $("#sub_Z_old_installed_add_code").hide();
            }
        }
    }); //fish.View.extend END
}); //ALL END