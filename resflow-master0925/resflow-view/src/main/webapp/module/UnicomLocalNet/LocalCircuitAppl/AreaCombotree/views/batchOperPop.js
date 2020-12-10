define([
        'module/UnicomLocalNet/resmaster/portal/orderWorkDeal/action/orderDetailsAction',
		'hbs!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/templates/batchOperPop.html',
        'i18n!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/i18n/acceptArea.i18n',
        'module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/css/fish.fileupload',
        'css!module/UnicomLocalNet/LocalCircuitAppl/AreaCombotree/css/fileupload.css'
    ],
		function(orderDetailsAction,temp, i18n) {
	var that;


    var fileInputPopView = fish.View.extend({
    	el: false,
    	template: temp,
        serialize: function() {
            return i18n;
        },
        //点击事件
	    events:{
	    	"click .js-close_Lead" : "closeLeadr",
	    	"click .js-downloadTemplate" : "downloadTemplate",
        },
        afterRender: function(){
            that = this;
        	//that.renderCombobox();
        	that.getLeadrFile();

		},
		renderCombobox : function(){
		},
		getLeadrFile : function(){
			var filepath='';
            var me = this;

            that.$('.js-uploading').fileupload({
				url: 'localScheduleLT/circuitInfoUploadController/upCirInfoExcel.spr',
				dataType: 'json',
                acceptFileTypes: /(\.|\/)(xlsx|xls)$/i,
                add: function(e, data) {
                   //alert("data2.files[0]-"+data.files[0]);
                   var file = data.files[0];
                   that.$('.js-file_path').val(file.name);
                   filepath= that.$('.js-file_path').val();
                    productType = me.options.productType;

                   //调用bssfileshow展示文件图标和文件信息
                   //  var $fileshow = that.$('.js-file-upload').bssfileshow({
                   //      name:file.name,
                   //      url:"",
                   //      size:file.size,
                   //      upprocess:0,
                   //  });
                   //  $fileshow.on("bssfileshow:delfile",function(e,url){
                   //      console.log("delfile event...");
                   //  });
                    that.$(".js-submit-Lead").off('click').on('click', function(e) {
                    	if (filepath==''){
                    		fish.info('导入数据源为空，请选择待导入的数据源！');//
                    		return false;
                    	 }
                    	if (file.name.indexOf(".xls") < 0) {
                    		fish.info('选择的文件类型不符，只能上传表格类型文件！');//
                    		return false;
                    	}
                        var param = {type: 'add',serviceId: productType};
                    	data.formData = param;
                    	//$('.js-submit-Lead').attr('disabled','true');
                        data.submit();

                    });
                    that.$(".js-delete-Lead").off('click').on('click', function(e) {

                    });
                },
                done: function(e, data) {
                	debugger
                	//fish.info("处理结果："+data.result);
                    var circuitData = new Object();
                    circuitData =  data.result;
                    that.popup.close(circuitData);
                },

                progressall: function (e, data) {
                    var progress = parseInt(data.loaded / data.total * 100, 10);
                    //that.$('.js-file-upload').bssfileshow("setFileProcess",progress);
                },
                processalways: function (e, data) {
                	debugger
                    var index = data.index,
                        file = data.files[index];
                    if (file.error && file.error === "File type not allowed" ) {
                        fish.error({message: i18n.FILE_TYPE_UNMATCH_ONLY_EXCEL, modal:true});//选择的文件类型不符，只能上传表格类型文件！
                        return;
                    }
                },
                fail: function (e, data) {
                	debugger
                	fish.error({message : '文件上传失败', modal : true})//
                }
			});

		},

		 dataToTxt : function(errMsgs){
         	var strTxt = errMsgs.toString();
     		var blob =  new Blob([strTxt],{type:"text/plain;charset=utf-8"});
     		saveAs(blob,"errorInfo.txt");
         },

		downloadTemplate : function(){
            var param = new Object();
            param.productType = this.options.productType;
            orderDetailsAction.downFile("localScheduleLT/circuitInfoUploadController/expCirTempExcel.spr",param);
		},

		closeLeadr : function(){
			that.trigger("editview.close",{d:'xxx'});
			that.popup.close();
		},
        close : function(res){
            that.popup.close(res);
        },

	});

    return fileInputPopView;
});