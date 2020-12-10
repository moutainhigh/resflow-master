/**
 * by ma.furong
 * 2018-10-12
 */
define(["text!module/UnicomIDC/resmaster/portal/homePage/templates/MainView.html",
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        "module/UnicomLocalNet/resmaster/portal/homePage/action/qryAction",
        "css!module/UnicomLocalNet/resmaster/portal/homePage/css/IndexView.css"],
    function(template,portalViewi18n,qryAction) {
        var MainView = ngc.View.extend({
            template : ngc.compile(template),
            events:{

            },
            initialize : function() {
                this.render();
            },
            render : function() {
            	this.$el.append(this.template(portalViewi18n));
            },
            afterRender : function() {
                //获取当前月份
                var nowDate = ngc.dateutil.format(new Date(), 'yyyy-mm');
                $(".mainDiv .content").width($(window).width()-$(".mainDiv .left").width());
                this.initTopCountNum();
                this.initMapData();
                this.initLine();
                this.initBarTop();
                this.initFormDate(nowDate);
                this.initPie(nowDate);
                this.initAffiche();
            },
            initTopCountNum:function () {
                var _width = $(".mainDiv .content").width();
                $(".mainDiv .content .item").width((_width-20*5-10)/5);
                qryAction.getEqpCount(function (data) {
                  this.$("#eqpNum").html(data);
                }.bind(this));
            },
            initMapData:function () {
                var map = new Object();
                map.regionId ="";
                qryAction.getRegionRoomsCollectInfos(map,function (data) {
                    var mapDataRoom = new Array();
                    var mapDataRentRoom = new Array();
                    var roomCount = 0;
                    for(var i=0;i<data.length;i++){
                        var objRoom = new Object();
                        var objRentRoom = new Object();
                        var name = data[i].REGIONNO;
                        if(name.substring(name.length-1)=="省"){
                            name = name.substring(0,name.length-1);
                        }
                        objRoom.name = name;
                        objRentRoom.name = name;
                        objRoom.value = data[i].ROOMCOUNT;
                        objRentRoom.value = data[i].RENTAREA;
                        roomCount=roomCount+data[i].ROOMCOUNT;
                        mapDataRoom.push(objRoom);
                        mapDataRentRoom.push(objRentRoom);
                    }
                    this.$("#spaceNum").html(roomCount);
                    //机架
                    qryAction.getRackOccupyInfo("","",function (data) {
                        var mapDataRock = new  Array();
                        var mapDataRockOccupy=new  Array();
                        var rockCount = 0;
                        for(var i=0;i<data.length;i++){
                            var objRock = new Object();
                            var objRentRock = new Object();
                            var name = data[i].REGIONNO;
                            if(name.substring(name.length-1)=="省"){
                                name = name.substring(0,name.length-1);
                            }
                            objRock.name = name;
                            objRentRock.name = name;
                            objRock.value = data[i].rackcount;
                            objRentRock.value = data[i].rackoccupycount;
                            rockCount=roomCount+data[i].rackcount;
                            mapDataRock.push(objRock);
                            mapDataRockOccupy.push(objRentRock);
                        }
                        this.$("#rackNum").html(rockCount);
                        this.initMap(mapDataRoom,mapDataRentRoom,mapDataRock,mapDataRockOccupy);
                    }.bind(this));
                }.bind(this));
            },
            initMap:function (mapDataRoom,mapDataRentRoom,mapDataRock,mapDataRockOccupy) {
                var _width = $(".mainDiv .content").width();
                this.$("#mapId").width(_width-300-50);
                var myChart = echarts.init(document.getElementById('mapId'));
                // 指定图表的配置项和数据
                var option = {
                    title: {
                        x: 10,
                        y:20,
                        text: 'IDC机房全国概况图'
                    },
                    tooltip : {
                        trigger: 'item',
                        formatter: function(params) {
                            var res = params.name+'<br/>';
                            var myseries = option.series;
                            for (var i = 0; i < myseries.length; i++) {
                                for(var j=0;j<myseries[i].data.length;j++){
                                    if(myseries[i].data[j].name==params.name){
                                        res+=myseries[i].name +' : '+myseries[i].data[j].value+'</br>';
                                    }
                                }
                            }
                            return res;
                        }

                    },

                        dataRange: {
                        show:true,
                        min: 0,
                        max: 100,
                        color:['#DB2F2F','#DC4141',"#E67C80","#EEA2A2","#F5C7C7","#FBE3E3"],
                        text:[portalViewi18n.HIGH,portalViewi18n.LOW]
                    },
                    series: [{
                        type: 'map',
                        name: '机房总数量',
                        mapType: 'china',
                        itemStyle: {
                            normal: {
                                label:{show:true,color:"#999999"}
                            },
                            emphasis:{label:{show:true}}
                        },
                        data:mapDataRoom
                    },
                        {
                            type: 'map',
                            name: '租用机房总面积',
                            mapType: 'china',
                            itemStyle: {
                                normal: {
                                    label:{show:true,color:"#999999"}
                                },
                                emphasis:{label:{show:true}}
                            },
                            data:mapDataRentRoom
                        },
                        {
                            type: 'map',
                            name: '机架总数量',
                            mapType: 'china',
                            itemStyle: {
                                normal: {
                                    label:{show:true,color:"#999999"}
                                },
                                emphasis:{label:{show:true}}
                            },
                            data:mapDataRock
                        },
                        {
                            type: 'map',
                            name: '租用机架总数量',
                            mapType: 'china',
                            itemStyle: {
                                normal: {
                                    label:{show:true,color:"#999999"}
                                },
                                emphasis:{label:{show:true}}
                            },
                            data:mapDataRockOccupy
                        }

                    ]
                };
                myChart.setOption(option);
            },
            initLine:function () {
                var _width = $(".mainDiv .content").width();
                this.$("#lineId").width(_width-300-50);
                var myChart = echarts.init(document.getElementById('lineId'));
                // 指定图表的配置项和数据
                var option = {
                    tooltip : {
                        trigger: 'axis'
                    },
                    grid:{
                        y:10,
                        y2:80
                    },
                    legend: {
                        data:['流量','宽带'],
                        y:200
                    },
                    calculable : false,
                    xAxis : [
                        {
                            type : 'category',
                            boundaryGap : false,
                            data : ['2018-5','2018-6','2018-7','2018-8','2018-9','2018-10']
                        }
                    ],
                    yAxis : [
                        {
                            type : 'value',
                            axisTick:false,
                            splitLine:false
                        }
                    ],
                    series : [
                        {
                            name:'流量',
                            type:'line',
                            stack: '总量',
                            itemStyle: {
                                normal: {
                                    color: '#188fe1'
                                }
                            },
                            data:[5, 15, 20, 18, 10, 4]
                        },
                        {
                            name:'宽带',
                            type:'line',
                            stack: '总量',
                            itemStyle: {
                                normal: {
                                    color: '#9BCA63'
                                }
                            },
                            data:[9, 18, 25, 19, 12, 6]
                        }

                    ]
                };
                myChart.setOption(option);
            },
            initBarTop:function () {
                var myChart = echarts.init(document.getElementById('top_bar'));
                var option = {
                    calculable : false,
                    grid:{
                        y:10,
                        y2:20,
                        x:50,
                        x2:50
                    },
                    xAxis : [
                        {
                            type : 'value',
                            show:false
                        }
                    ],
                    yAxis : [
                        {
                            type : 'category',
                            show: true,
                            axisLine:false,
                            axisTick:false,
                            splitLine:false,
                            axisLabel:{
                                textStyle:{
                                    color: '#555'
                                }
                            },
                            data : ['北京','上海','杭州','南京','武汉']
                        }
                    ],
                    series : [
                        {
                            name:'流量',
                            type:'bar',
                            stack: '总量',
                            barWidth:20,
                            itemStyle: {
                                normal: {
                                    color: function(params) {
                                        // build a color map as your need.
                                        var colorList = [
                                            '#E51A1A','#F7953B','#F5D140','#188FE1','#7CF28E'
                                        ];
                                        return colorList[params.dataIndex]
                                    },
                                    label: {
                                        show: true,
                                        position: 'right',
                                        formatter: '{c}'
                                    }
                                }
                            },
                            data:[788,900,1620,1900,2000]
                        }
                    ]
                };
                myChart.setOption(option);
            },
            initFormDate:function (date) {
                var that = this;
                var viewOption = [
                    {id:"startEndTime",type:"date",format : 'yyyy-mm',
                        range:true,placeholder:"起始时间",placeholder_range:"终止时间",
                        viewType : 'month',label:"时间段"
                    }
                ];
                this.requireView({
                    url:'module/component/views/FormView',
                    selector:"#form_date",
                    viewOption:{config:{elements:viewOption,column:0}}
                }).then(function(){
                    this.getView("#form_date").on("inputChange",function (id,obj) {
                        if(id=="startEndTime"){
                            var _startTime = $("#startEndTime").datetimepicker("value");
                            var _endTime = $("#startEndTime_range").datetimepicker("value");
                            if(_startTime){
                                $('#startEndTime_range').datetimepicker('setStartDate',_startTime);
                            }
                            if(_endTime){
                                $('#startEndTime').datetimepicker('setEndDate',_endTime);
                            }

                        }
                    }.bind(this));
                }.bind(this));
                //
                var viewOption_month = [
                    {id:"a_date",type:"date",format : 'yyyy-mm',
                        viewType : 'month',label:"",defaultValue : {
                        value : date
                    }}
                ];
                this.requireView({
                    url:'module/component/views/FormView',
                    selector:"#form_date_month",
                    viewOption:{config:{elements:viewOption_month,column:0}}
                }).then(function () {
                    that.getView("#form_date_month").on("inputChange", function(id,obj) {
                        var date =  that.getView("#form_date_month").getValue("a_date").value;
                        that.initPie(date);
                    });
                });

            },
            initPie:function (date) {
                var _width = $(".mainDiv .content").width();
                $(".mainDiv .content .pieDiv").width(_width- 30);
                $(".mainDiv .content .pieDiv .pie").width((_width- 30)/4);
                this.canvasPie("pie4","租用率","总带宽","租用总流量",0,0,"0%");
                //机架
                qryAction.getRackOccupyInfo("",date,function (data) {
                    var occupy = 0;
                    var allNum =0;
                    for(var i=0;i<data.length;i++){
                        allNum = data[i].rackcount+allNum;
                        occupy = data[i].rackoccupycount+occupy;
                    }
                    var rate = 0+"%";
                    if(allNum>0){
                        rate  = parseInt((occupy/allNum).toFixed(2) *100)+"%" ;
                   }
                    this.canvasPie("pie1","租用率","机架总数量","租用机架数量",allNum,occupy,rate);
                }.bind(this));
                //U位
                qryAction.getRackUOccupyInfo(date,function (data) {
                    var occupy = data.rackuoccupycount;
                    var allNum = data.rackucount;
                    var rate = 0+"%";
                    if(allNum>0){
                        rate  = parseInt((occupy/allNum).toFixed(2) *100)+"%" ;
                    }
                    this.canvasPie("pie3","租用率","U位总数量","租用U位数量",allNum,occupy,rate);
                }.bind(this));
                //机房面积
                var map = new Object();
                map.dateStr = date;
                map.regionId ="";
                qryAction.getRegionRoomsCollectInfos(map,function (data) {
                    var rentarea = 0;
                    var countarea = 0;
                    for(var i=0;i<data.length;i++){
                        rentarea = rentarea+data[i].RENTAREA;
                        countarea = countarea+data[i].COUNTAREA;
                    }
                    var rate = 0+"%";
                    if(countarea>0){
                        rate  = parseInt((rentarea/countarea).toFixed(2) *100)+"%" ;
                    }
                    this.canvasPie("pie2","租用率","机房总面积","租用机房总面积",countarea,rentarea,rate);
                }.bind(this));
            },
            canvasPie:function (id,name,leng1,leng2,allNum,occupy,rate) {
                var x = "41%",y="30%";
                var myChart = echarts.init(document.getElementById(id));
                var option = {
                    tooltip : {
                        trigger: 'item'
                    },
                    legend: {
                        x : "center",
                        y : 200,
                        data:[leng1,leng2]
                    },
                    title : {
                        show : true,
                        x : x ,
                        y : y,
                        text : rate,
                        subtext  : name,
                        textStyle : {
                            color : "#3f3f3f",
                            fontSize : 22,
                            align : "center"
                        },
                        subtextStyle : {
                            color : "#3f3f3f",
                            fontSize : 12,
                            align : "center"
                        }
                    },
                    color:['#77C1F0','#DAF8BC'],
                    series : [
                        {
                            type:'pie',
                            radius : ['40%', '55%'],
                            center : ['50%','40%'],
                            data:[
                                {value:allNum, name:leng1},
                                {value:occupy, name:leng2}
                            ],
                            itemStyle : {
                                normal : {
                                    label : {
                                        show : true,
                                        formatter : function(params,ticket,callback) {
                                            return "\n"+ params.value;
                                        },
                                        textStyle:{
                                            color:"#000"
                                        }

                                    },
                                    labelLine : {
                                        show : true
                                    }
                                }
                            }
                        }
                    ]
                }
                myChart.setOption(option);
            },
            initAffiche:function () {
                qryAction.queryNowAffiche(5,function (data) {
                    var afficheData = data.data;
                    var noticDiv = this.$("#noticDiv");
                    for (var i = 0; i < afficheData.length; i++) {
                        var detail = ngc.subStringByChar(afficheData[i].afficheTitle, 10);
                        var time = afficheData[i].afficheDate;
                        noticDiv.append('<div class="notice-item"><span class="text">detail</span>' +
                            '<span class="time">time</span>' +
                            '</div>')
                    }
                }.bind(this));
            }

        });
        return MainView;
    });

