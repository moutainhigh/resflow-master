/**
 * add by yang.bo
 */
define(function() {
	return {
		_initButton: function(that, _buttons){
	    	that.toolsMenuData = {
	    			right_of_condition: {id: "right_form_tool_content", data: [], show: false},
	    			left_of_center: {id: "left_center_tool_content", data: [], show: false},
	    			right_of_center: {id: "right_center_tool_content", data: [], show: false}
	    	};
 	            //处理用户自定义按钮内容以及显示隐藏
 	   	    	for(var i=0; i<_buttons.length; i++){
 	   	    		if(!that.options.preproMenu){
 	   	    			break;
 	   	    		}
 	   	    		for(var j=0; j<that.options.preproMenu.length; j++){
 	   	 	    		if(_buttons[i]["id"] == that.options.preproMenu[j]["id"]){
 	   	 	    			var _toolsCont = that.options.preproMenu[j];
 	   	 	    			$.each(_toolsCont,function(key, value) {
 	   	 	    				if(key != "id"){
 	   	 	    					if(_toolsCont[key] != undefined){
 	   	 	 	 	    				_buttons[i][key] = _toolsCont[key];
 	   	 	 	 	    			}
 	   	 	    				}
 	   	 	    			}.bind(this));
 	   	 	    			break;
 	   	 	    		}
 	   	 	    	}
 	   	    	}
 	            //把基本功能按钮进行归类到各自所需要到的位置,通过displayPosition属性控制
 	  	    	$.each(_buttons, function(n, value){
 	  	    		that.toolsMenuData[value["displayPosition"]]["data"].push(value);
 	          	}.bind(this));	
 	            //循环处理按钮位置:是否需要显示(5个按钮位置,有数据则显示DIV,反之不显示)
 	  	    	$.each(that.toolsMenuData, function(n, value){
 	  	    		if(value["data"].length > 0){
 	  	    			var index = 0;
 	  	    			for(var i=0; i<value["data"].length; i++){
 	  		    			if(value["data"][i]["show"]){
 	  		    				index++;
 	  		    			}
 	  		    		}
 	  	    			var blag = false;
 	  	    			if(index > 0){ blag = true; }
 	  	    			value["show"] = blag;
 	  	    			that.initPlateData[n] = blag;
 	  	    		}
 	  	    	}.bind(this));
 	  	    	if(that.toolsMenuData.left_of_center.show && that.toolsMenuData.right_of_center.show){
 	  	    		that.initPlateData["allcenter"] = true;
 	  	    		that.initPlateData["left_of_center"] = false;
 	  	    		that.initPlateData["right_of_center"] = false;
 		    		var lefts = 0;
 					$.each(that.toolsMenuData.left_of_center.data, function(i, data){
 						if(!data.parent){
 							lefts ++;
 						}
 					}.bind(this));
 					var rights = 0;
 					$.each(that.toolsMenuData.right_of_center.data, function(i, data){
 						if(!data.parent){
 							rights ++;
 						}
 					}.bind(this));
 					lefts = parseInt((lefts/(lefts+rights))*12);
 					lefts = lefts <= 11? lefts : 11;
 					that.initPlateData["left_center"] = lefts;
 					that.initPlateData["right_center"] = 12-lefts;
 	  	    	}
	    },
	    _initCreateButton: function(that){
        	//将已经归类的按钮对各自的ID进行生成操作
	    	$.each(that.toolsMenuData, function(key, value) {
	    		if(value["show"]){
	    			//处理右侧按钮按行排放,不能全部挤在一块显示
	    			if(value["id"] == "right_form_tool_content"){
						$.each(value["data"], function(i, obj) {
							if(i < 4){
								this._CreateButton(that, new Array(obj), "right_form_tool_content"+i);
							}
						}.bind(this));
	    			}else{
	    				this._CreateButton(that, value["data"], that.toolsMenuData[key]["id"]);
	    			}
	    		}
	        }.bind(this));
	    },
	  //生成按钮
	    _CreateButton: function(that, _buttons, selector){
	    	if(_buttons.length > 0){
	    		that.requireView({selector:"#"+selector,url:"module/component/views/ToolsView", 
		    		viewOption:{config:{group:true,theme:'btn-default'}}}).then(function(){
					var tools = that.getView("#"+selector);
					for(var i=0; i<_buttons.length; i++){
						var _button = _buttons[i];
			 			if(_button["show"]){
			 				(function(_button){
		 						tools.addTool(_button["id"], _button["name"], _button["index"], _button["icon"], _button["parent"], function(){
		 							if(_button["functions"] != undefined){
		 								_button["functions"].call(that, _button["id"]);
		 							}
		 						});
			 				})(_button);
			 			}
					}
				});
	    	}
	    }
	}
});
