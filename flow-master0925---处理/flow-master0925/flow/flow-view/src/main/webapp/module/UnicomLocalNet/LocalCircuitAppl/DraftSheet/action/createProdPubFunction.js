
define(['module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/action/digitalCircutAction.js'],
    function(digitalCircutAction){
    return {
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
                var startSub = objClassName.indexOf('menu')+5;
                var enumType = objClassName.substring(startSub,objClassName.length);
                var obj = '#'+objId;

                digitalCircutAction.queryEnum(enumType,function (data) {
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
                    if(obj.selector==".product_code"){
                        $(".product_code").combobox('value',productType);
                        $(".product_code").combobox("disable");

                    }else if (obj.selector==".operate_type"){
                        $(".operate_type").combobox('value',actType);
                        $(".operate_type").combobox('disable');
                    }
                });
            };
        },

    }
});
