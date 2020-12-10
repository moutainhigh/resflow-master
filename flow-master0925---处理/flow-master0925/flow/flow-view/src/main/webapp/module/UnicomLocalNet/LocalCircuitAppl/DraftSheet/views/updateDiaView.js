define(['module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/action/digitalCircutAction.js',
    'text!module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/templates/updateDiaView.html',
    'i18n!module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/i18n/digitalCircuit.i18n',
    'css!module/UnicomLocalNet/LocalCircuitAppl/DraftSheet/css/digitalCircuit.css'
], function(digitalCircutAction,updateDiaView,i18n,css) {
    var fileList = null,
        maxFileSize = 50*1024*1024,                //文件大小限制
        maxFileCount = 5;            //文件个数限制

    var flag ;

    var upDDKLoadResult = new Array(); //大带宽附件信息

    return fish.View.extend({
        template: fish.compile(updateDiaView),
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
            this.fileDDKSelect();
            var arrayData = new Map();
            arrayData  =this.options.rowData;
            flag = this.options.flag;
           /* var upDDKLoadResult2= new Map();
            upDDKLoadResult2=this.options.upDDKLoadResult;*/
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
            $('.file-remove-ddk').on('click', function (e) {
                var name = $(e.currentTarget).prev().children(".fileName").text(), pos,pos2;
                $.each(fileList.files, function (index, file) {
                    if (file.name === name) {
                        pos = index;
                        return false;
                    }
                });
                var delFileName="";
                //获取删除upDDKLoadResult中对应附件信息的索引
                for (var i = 0; i < upDDKLoadResult.length; i++) {
                    if(upDDKLoadResult[i][0].fileName==name){
                        delFileName=upDDKLoadResult[i][0].fileId+"."+upDDKLoadResult[i][0].fileType;
                        pos2=i;
                    }
                }

                fileList.files.splice(pos, 1);
                upDDKLoadResult.splice(pos2, 1);
                $(e.currentTarget).parent().remove();
                var fileInfo = new Object();
                fileInfo.filePath = 'createbuss';
                fileInfo.fileName = delFileName;
                digitalCircutAction.delFileOnFtp(fileInfo);
            });

            // $("#serialNumber").val("1");
       /* //大带宽审核附件处理
            $('#sub_BigSpeedAttachment').val(upDDKLoadResult2.fileName);*/
            this.initForm(actType);
        },
        sub_save:function () {
            var checkNull = $("#sub_sevMenuForm").isValid();
            if (!checkNull) {
                // fish.toast("success","");
                fish.error({title: '提示', message: '请核查数据必填或正确格式'});
                return;
            }
            if($('#sub_isBigSpeed').val()==1 && upDDKLoadResult.length==0){
                fish.error({title:'提示',message:'必须填写大带宽附件'});
                return;
            }
            //获取form所有字段，包括为空的
            var formValue = {};
            var t = $('#sub_sevMenuForm').serializeArray();
            $.each(t, function() {
                formValue[this.name] = this.value;
            });
            formValue["speedName"]=$('#sub_speed').combobox("text");
            formValue["upDDKLoadResult"]=upDDKLoadResult;
            formValue["DDK"]=$("#sub_BigSpeedAttachment_down").html();
            formValue["fileList"]=fileList;

            //地市区县等取中文名保存
            for(var key in formValue){
                if(key.indexOf("_province")>=0||key.indexOf("_city")>=0||key.indexOf("_county")>=0||key.indexOf("_region")>=0){
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
            if (!arrayData.hasOwnProperty("serialNumber")){
                $("#sub_serialNumber").removeAttr('readonly');
                $("#sub_serialNumber").css({"background-color": "#fff"});
            } else if (!arrayData.hasOwnProperty("tradeId")){
                $("#sub_tradeId").removeAttr('readonly');
                $("#sub_tradeId").css({"background-color": "#fff"});
            }
            for ( var key in arrayData) {
                //通过遍历对象属性的方法，遍历键值对，获得key，然后通过 对象[key]获得对应的值
                var  value = arrayData[key];
                if(key == 'serialNumber' || key == 'tradeId'){
                    if (value == '' || value == null){
                        $("#sub_" + key).removeAttr('readonly');
                        $("#sub_" + key).css({"background-color": "#fff"});
                    }
                }
                if (key=='DDK') {
                    $("#sub_BigSpeedAttachment_down").append(value);
                }
                if(key=="fileList"){
                    fileList = value;
                }
                if(key=="upDDKLoadResult"){
                    upDDKLoadResult = value;
                }
                if(key=="SRV_ORD_ID"){
                    this.queryDDKAttachment(value);
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
                if(key.indexOf("_city")>=0){
                    $("#sub_"+key).popedit({
                        initialData: {
                            'name':arrayData[key] ,
                            'value':""
                        },
                        open:function(e) {
                            var _this = $(this);
                            var areaName = $("#sub_Z_belong_province").popedit("getValue").name;
                            digitalCircutAction.queryAreaIdByName(areaName,function (data) {
                                var options = {
                                    url: 'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/views/provienceTreeView',
                                    height: 500,
                                    width: 400,
                                    modal: false,
                                    draggable: false,
                                    autoResizable: true,
                                    viewOption: {
                                        flag : "city" //省：privience 市：city  区县:county
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
        fileDDKSelect:function() {
            //URL为文件上传的地址，支持额外参数拼接。例如'batch_task?name=aaa&age=22&height=180'
            var urlUpLoad = this.getRootPath()+"/localScheduleLT/initProdFileUploadController/uploadFiles.spr";
            FILE_LIST_DDK =  '<li class="list-group-item">' +
                '                       <div class="url" style="display: inline-block;width: 90%">' +
                '                           <span class="glyphicon glyphicon-file"></span>' +
                '                           <span class="fileName">{{fileName}}</span>' +
                '                           <span>({{fileSize}})</span>' +
                '                       </div>' +
                '                       <span class="file-remove-ddk glyphicon glyphicon-remove pull-right"></span></li>';
            $('#sub_BigSpeedAttachment').fileupload({
                dataType: 'json',
                autoUpload: false,
                acceptFileTypes: '',
                done: function (e, data) {
                    $.each(data.originalFiles,function(index,file){
                        var fileName = decodeURIComponent(file.name);
                        $('<p/>').text(fileName + '   upload suceess, file.size:' + file.size).appendTo('#files');
                    })
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
                    upDDKLoadResult.push(data.result);
                },
                add: function (e, data) {
                    if (data.files[0].size > maxFileSize) {
                        fish.warn('选择的文件大小超过上限50M，请重新选择，请重新选择');
                        return;
                    }
                    var obj = data.files[0],
                        fileName = decodeURIComponent(obj.name),
                        fileSize = (obj.size / 1024.0).toFixed(2) + "KB";
                    fileList = data;
                    $('#sub_BigSpeedAttachment_down').append(fish.compile(FILE_LIST_DDK)({
                        fileName: fileName,
                        fileSize: fileSize
                    }));
                    $('.file-remove-ddk').on('click', function (e) {
                        var name = $(e.currentTarget).prev().children(".fileName").text(), pos,pos2;
                        $.each(fileList.files, function (index, file) {
                            if (file.name === name) {
                                pos = index;
                                return false;
                            }
                        });
                        var delFileName="";
                        var fileId="";
                        //获取删除upDDKLoadResult中对应附件信息的索引
                        for (var i = 0; i < upDDKLoadResult.length; i++) {
                            if(upDDKLoadResult[i][0].fileName==name){
                                delFileName=upDDKLoadResult[i][0].fileId+"."+upDDKLoadResult[i][0].fileType;
                                fileId=upDDKLoadResult[i][0].fileId;
                                pos2=i;
                            }
                        }

                        fileList.files.splice(pos, 1);
                        upDDKLoadResult.splice(pos2, 1);
                        $(e.currentTarget).parent().remove();
                        var fileInfo = new Object();
                        fileInfo.filePath = 'createbuss';
                        fileInfo.fileName = delFileName;
                        fileInfo.fileId = fileId;
                        digitalCircutAction.delFileOnFtp(fileInfo);


                    });

                    fileList.url = urlUpLoad;
                    var result = fileList.submit();//自动上传
                }
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
        //查询大带宽附件方法
        queryDDKAttachment:function(servId){
            digitalCircutAction.queryDDKAttachment(servId,"FQ-CIR", function (data) {
                var fileData = data;
                if (fileData.length != 0) {
                    $('#ANNEX_down').show();
                    var FILE_LIST_TPL = '<li class="list-group-item">' +
                        '                       <div class="url" style="display: inline-block;width: 90%">' +
                        '                           <span class="glyphicon glyphicon-file"></span>' +
                        '                           <span>{{fileName}}({{fileSize}})</span>' +
                        '                           <span class="fileId" style="display:none">{{fileId}}</span>' +
                        '                       </div>' +
                        '                       <span class="file-remove-ddk glyphicon glyphicon-remove pull-right"></span></li>';
                    $.each(fileData, function (i,obj) {
                        // 展示数据库中存的附件
                        $('#sub_BigSpeedAttachment_down').append(fish.compile(FILE_LIST_TPL)({fileName: obj.FILE_NAME,fileId: obj.FILE_ID,fileSize: obj.FILE_SIZE + "KB"}));
                        // 如果是查看申请单，则隐藏删除按钮
                            $('.file-remove-ddk').hide();
                    })
                    $('.file-remove-ddk').off('click').on('click', function(e) {
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
        initForm:function (actType) {
            if(actType=='101'){
                $("#sub_circuitCode").attr("readonly","readonly");
                $('#sub_circuitCode').attr('disabled',true);
                $("#sub_circuitCode").css("background-color","#eeeeee");
                $("#sub_oldCircuitCode").attr("readonly","readonly");
                $("#sub_oldCircuitCode").css("background-color","#eeeeee");
                $("#sub_old_CircuitCode").hide();

                $("#sub_old_belong_region").hide();
                $("#sub_old_belong_county").hide();
                $("#sub_old_installed_add").hide();
            }
        }
    }); //fish.View.extend END
}); //ALL END