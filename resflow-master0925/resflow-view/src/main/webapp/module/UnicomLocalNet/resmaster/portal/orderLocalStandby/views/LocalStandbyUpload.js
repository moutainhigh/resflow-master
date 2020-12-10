define(["text!module/UnicomLocalNet/resmaster/portal/orderLocalStandby/templates/LocalStandbyUpload.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'module/UnicomLocalNet/resmaster/portal/orderLocalStandby/action/orderStandbyAction',
        "css!module/UnicomLocalNet/resmaster/portal/orderLocalStandby/styles/localStandby.css"],
    function(template,portalViewi18n,orderStandbyAction,css) {
        var maxFileSize = 50*1024*1024,                //文件大小限制
            maxFileCount = 20;                    //文件个数限制
        // debugger;
        var $log = $('.well');
        var fileId =  new Date().getTime();

        function appendLog(message) {
            $log.append(message + '<br>')
        }

        return ngc.View.extend({
                fileList : null,
                updateParams: new Object(),
                URl: '',
                staffId: '',
                cstOrdId: '',
                tacheId: '',
                regionId: '',
                specialtyCode: '',
                srvOrdIds: '',
                dispObjTyeValue: '',
                dispObjTye: '',
                compUserId: '',
                dealUserId: '',
                staffIdColl: '',
                dispType: '',
                woState: '',
                resourceType: '',
                girdCollMdel: '',
                queryObj: new Object(),
                jsonSelarrrow: new Object(),
                template : ngc.compile(template),
                i18nData: ngc.extend({}, portalViewi18n),
                events: {
                    'click #closeFile': 'closeFile',
                    'click .js-file-upload': 'fileUpload',
                    'click .js-select-file': 'fileSelect',
                    'click #saveFile': 'fileUpload'
                },
            initialize: function() {
                appendLog('PopView initialize...');
            },

            afterRender: function() {
                appendLog('PopView afterRender...');
                var cstOrdId= new Array();
                var tacheId = new Array();
                var specialtyCode = new Array();//专业
                var dispObjTyeValue = new Array();//岗位、部门、个人值
                var dispObjTye = new Array();//业务订单Id合集
                var regionId = new Array();//区域Id

                var selArrrow = this.options.selArrrow;
                for (let i = 0; i < selArrrow.length; i++) {
                    cstOrdId[i] = this.options.selArrrow[i].CST_ORD_ID;//客户Id
                    tacheId[i] = this.options.selArrrow[i].TACHE_ID;//环节Id
                    specialtyCode[i]  = this.options.selArrrow[i].SPECIALTY_CODE;//专业
                    dispObjTyeValue[i]  = this.options.selArrrow[i].DISPOBJTYEVALUE;//岗位、部门、个人值
                    dispObjTye[i]  = this.options.selArrrow[i].DISPOBJTYE;//业务订单Id合集
                    regionId = this.options.selArrrow[i].REGION_ID;//区域Id
                }
                this.queryObj = this.options.queryObj;
                this.cstOrdId = cstOrdId;//客户Id
                this.tacheId = tacheId;//环节Id
                this.specialtyCode = specialtyCode;//专业
                this.dispObjTyeValue = dispObjTyeValue;//岗位、部门、个人值
                this.dispObjTye = dispObjTyeValue;//业务订单Id合集
                // this.cstOrdId = this.options.selArrrow[0].CST_ORD_ID;//客户Id
                // this.tacheId = this.options.selArrrow[0].TACHE_ID;//环节Id
                // this.regionId = this.options.selArrrow[0].REGION_ID;//区域Id
                this.srvOrdIds = this.options.selArrrow[0].SRV_ORD_IDS;//业务订单Id合集
               /* this.specialtyCode = this.options.selArrrow[0].SPECIALTY_CODE;//专业
                this.dispObjTyeValue = this.options.selArrrow[0].DISPOBJTYEVALUE;//岗位、部门、个人值
                this.dispObjTye = this.options.selArrrow[0].DISPOBJTYE;//业务订单Id合集*/
                this.updateParams.selarrrow = this.options.selArrrow;

                this.compUserId = this.options.queryObj.compUserId;//
                this.dealUserId = this.options.queryObj.dealUserId;
                debugger
                this.staffId = this.options.queryObj.staffId;
                this.staffIdColl = this.options.staffIdColl;
                this.dispType = this.options.queryObj.dispType;
                this.woState = this.options.queryObj.woState;
                this.resourceType = this.options.queryObj.resourceType;
                this.girdCollMdel = this.options.girdCollMdel;
                this.URl = this.options.URl;
                // debugger
                this.initOrderDealGridColl();
                this.queryOrderDealCollData();
            },
            closeFile: function() {
                this.trigger('close');
                //弹出的视图实例会注入popup property，用于视图想自己关闭自己
                this.popup.close();
            },
            fileUpload:function () {
                var fileTh = this;
                var remark = $('#remark').val();
                if (remark === null || remark =='' ){
                    fish.warn('请填写意见');
                    return;
                }
                var selarrrowCollSub = $("#orderSubDeal-grid").grid("getCheckRows");
                if(selarrrowCollSub.length <1){
                    fish.toast("warn", "请选择待办信息");
                    return;
                }
                debugger
                fileTh.jsonSelarrrow = JSON.stringify(selarrrowCollSub);
                var fileLength = $('.file-list').find('li').length;
                if(fileLength != 0){
                    FILE_LIST_TPL = '<li class="list-group-item"><span class="glyphicon glyphicon-file"></span>{{fileName}} ({{fileSize}})<span class="file-remove glyphicon glyphicon-remove pull-right"></span></li>';
                    fileTh.fileList.url = fileTh.URl+"/localScheduleLT/orderStandbyController/uploadFiles.spr";
                    // debugger;
                    //如果有需要传入到服务端的业务参数，则设置一下formData属性。
                    fileTh.fileList.formData = {
                        name:fileTh.staffIdColl ,
                        selarrrow:fileTh.jsonSelarrrow,
                        fileId: fileId,
                        remark: remark,
                        sex: 'male'
                    }//服务端有个类似的dto来接受这些参数
                    // debugger;
                    var result = fileTh.fileList.submit();

                }else{
                    fileTh.saveUpload(this);
                }

                function getFileSize() {
                    return this.fileList ? this.fileList.files.length : 0;
                }

            },
            fileSelect:function() {
                var seTh = this;
                //URL为文件上传的地址，支持额外参数拼接。例如'batch_task?name=aaa&age=22&height=180'
                FILE_LIST_TPL = '<li class="list-group-item"><span class="glyphicon glyphicon-file"></span>{{fileName}} ({{fileSize}})<span class="file-remove glyphicon glyphicon-remove pull-right"></span></li>';
                $('.js-select-file').fileupload({
                    dataType: 'json',
                    autoUpload: false,
                    acceptFileTypes: '',
                    always: function (e, data) {
                        console.log(data.textStatus);
                        console.log(data.result);
                        debugger;
                        if('error' == data.textStatus || 0 == data.result.updateCount){
                            fish.toast('info', '上传失败'+data.result.message)
                        }else{
                            seTh.closeFile();
                        }
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
                        // debugger;
                        if (file.error && file.error === "File type not allowed" ) {
                            fish.error({message:"选择的文件类型不符，只能上传图片类型文件(以gif|jpg|jpeg|png结尾的文件)", modal:true});
                            return;
                        }
                    },
                    fail: function (e, data) {
                        // debugger;
                        $('<p/>').text("文件上传失败  " + data.errorThrown).appendTo('#files');
                    },
                    add: function (e, data) {
                        // debugger
                        if (data.files[0].size > maxFileSize) {
                            fish.warn('选择的文件大小超过上限50M，请重新选择');
                            return;
                        }
                        if (seTh.fileList != null && seTh.fileList.length >= maxFileCount) {
                            fish.warn('选择的文件个数超过上限'+maxFileCount+'个，请减少文件数');
                            return;
                        }
                        var obj = data.files[0],
                            fileName = decodeURIComponent(obj.name),
                            fileSize = (obj.size / 1024.0).toFixed(2) + "KB";
                        // debugger;
                        if (seTh.fileList === null) {
                            seTh.fileList = data;
                        } else {
                            seTh.fileList.files.push(obj);
                        }
                        $('.file-list').append(fish.compile(FILE_LIST_TPL)({
                            fileName: fileName,
                            fileSize: fileSize
                        }));

                        $('.file-remove').off('click').on('click', function (e) {
                            var name = $(e.currentTarget).prev().text(), pos;
                            $.each(seTh.fileList.files, function (index, file) {
                                if (file.name === name) {
                                    pos = index;
                                    return false;
                                }
                            });
                            seTh.fileList.files.splice(pos, 1);
                            $(e.currentTarget).parent().remove();
                        });
                    }
                })
            },
            saveUpload:function (meSa) {
                // $('#js-input-file').val('hhhhhhh');
                var remark = $('#remark').val();
                meSa.updateParams.selarrrow = meSa.jsonSelarrrow;
                meSa.updateParams.name = meSa.staffIdColl;
                meSa.updateParams.remark=remark;
                // debugger;
                orderStandbyAction.updateCollapsibleSingle(meSa.updateParams,function (datas) {
                    // debugger;
                    if(1 == datas.updateCount){
                        meSa.trigger('close');
                        meSa.popup.close();
                        fish.toast("success", "阶段性意见提交成功");
                    }else {
                        fish.toast("warn", "阶段性意见提交失败:"+datas.message);
                    }
                });

            },
            //初始化待办自信息
            initOrderDealGridColl:function(){
                $("#orderSubDeal-grid").grid({
                    colModel: this.girdCollMdel,
                    autowidth: true,
                    rowNum: 500,
                    height: 'auto',
                    curPageSort: false,
                    multiselect: true,
                    pager: false,
                    autoPaged: false,
                    gridview: false, //关闭快速加载模式，因为要根据每行记录内容渲染告警状态
                    shrinkToFit: false,
                    autoResizable: true,
                    cached: true, //把用户自定义的列展示设置缓存在本地
                    afterInsertRow: function(e, rowid, pageData){
                        switch (pageData.WO_COMPLETE_STATE) {
                            case '正常单':
                                $("#orderSubDeal-grid").grid('setCell',rowid,'WO_COMPLETE_STATE','',{color:'#6DCC4A'});
                                break;
                            case '预警单':
                                $("#orderSubDeal-grid").grid('setCell',rowid,'WO_COMPLETE_STATE','',{color:'#F4C70D'});
                                break;
                            case '超时单':
                                $("#orderSubDeal-grid").grid('setCell',rowid,'WO_COMPLETE_STATE','',{color:'#FF5858'});
                                break;
                        }
                    }
                });

            },
            //查询填写阶段性意见子信息
            queryOrderDealCollData: function(){
                var collQueryObj = new Object();
                collQueryObj = this.queryObj;
                collQueryObj.cstOrdId = this.cstOrdId;
                collQueryObj.tacheId = this.tacheId;
                collQueryObj.regionId = this.regionId;
                collQueryObj.specialtyCode = this.specialtyCode;
                collQueryObj.resourceType = this.resourceType;
                collQueryObj.dispObjTyeValue = this.dispObjTyeValue;
                collQueryObj.dispObjTye = this.dispObjTye;

                collQueryObj.compUserId = this.compUserId;
                collQueryObj.woState = this.woState;
                collQueryObj.dispType = this.dispType;
                collQueryObj.staffId = this.staffId;
                collQueryObj.dealUserId = this.dealUserId;
                // debugger;
                orderStandbyAction.querySubOrderInfoColl(collQueryObj,function (data) {
                    // debugger;
                    if (data.messages = "success") {
                        $("#orderSubDeal-grid").grid("reloadData", data.data);
                    }else{
                        fish.toast("error", "获取数据失败");
                    }
                })
            },
            cleanup: function() {
                appendLog('PopView cleanup...');
            }


        }); //fish.View.extend END
    }); //ALL END