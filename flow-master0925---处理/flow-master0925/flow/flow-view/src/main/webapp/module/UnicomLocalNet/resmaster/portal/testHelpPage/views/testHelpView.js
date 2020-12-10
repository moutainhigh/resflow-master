define(['module/UnicomLocalNet/resmaster/portal/testHelpPage/action/testHelpAction.js',
        'text!module/UnicomLocalNet/resmaster/portal/testHelpPage/templates/testHelpView.html',
        'i18n!module/UnicomLocalNet/resmaster/portal/i18n/portalView.i18n',
        'css!module/UnicomLocalNet/resmaster/portal/testHelpPage/css/testHelpView.css'
    ],
    function (testHelpAction, testHelpView, i18n, css) {
        return fish.View.extend({
            websocaket: null,
            websocaketDelApp:null,
            template: fish.compile(testHelpView),
            i18nData: fish.extend({}, i18n),
            events: {
                'click #businessRollback': 'businessRollbackBtn',
                'click #delApplication': 'delApplicationBtn',
                'click #submit': "send",
            },
            initialize: function () {
                this.render();
            },
            //渲染页面
            render: function () {
                this.$el.html(this.template(this.i18nData));
            },
            //初始化fish组件
            afterRender: function () {
                this.initAccordion();
                this.pageInit();
            },
            initAccordion: function () {
                $("#accordion").accordion({
                    active: false,//此属性为false时,collapsible=true时,默认不展开页签;collapsible=false时,默认展开第一个页签 此属性为数值时,表示默认展开页签的索引,从0开始 此属性为负值时,表示默认展开页签的索引,从最后一个往前数
                    heightStyle: "content",
                    multiple: "true",
                    collapsible: "false"
                });
            },
            pageInit: function () {
                $('#applicationId').on('mouseover', function () {
                    $('#applicationId').attr('title', $('#applicationId').val())
                });
                testHelpAction.queryApplicationLog(function (data) {
                    var appLogStr1="",appLogStr2="          ";
                    for(var item in data){
                        if(item<2){
                            appLogStr1 += "【IP地址为："+data[item].IP+" 主机名为："+data[item].CUST_NAME +"的用户于"+data[item].DEL_DATE+"删除了申请单："+data[item].APPLICATION_CODE+"】              ";
                        }
                        else{
                            appLogStr2 += "【IP地址为："+data[item].IP+" 主机名为："+data[item].CUST_NAME +"的用户于"+data[item].DEL_DATE+"删除了申请单："+data[item].APPLICATION_CODE+"】              ";
                        }
                    }
                    $('#applicationLog1').text(appLogStr1);
                    $('#applicationLog2').text(appLogStr2);

                });
            },
            businessRollbackBtn: function () {
                var param = new Object();
                param.srvOrdId = $('#instanceId').val();
                $('#returnRollbackJson').val("实例号： "+$('#instanceId').val()+" 任务开始执行...");
                testHelpAction.businessRollback(param, function (data) {
                    $('#returnRollbackJson').val($('#returnRollbackJson').val()+"\n"+data);
                });
            },
            delApplicationBtn: function () {
                var me = this;
                var applicationCode = $('#applicationId').val();
                testHelpAction.delApplicationByApplCode(applicationCode, function (data) {
                    $('#returnDelApplication').val(data);
                });
               /* me.delApplicationWebsocketBtn();
                me.websocaketDelApp.addEventListener('open', function () {
                    me.websocaketDelApp.send(applicationCode);
                });*/

            },
            delApplicationWebsocketBtn: function () {
                var me = this;
                var path="ws://"+window.location.href.substring(7,window.location.href.indexOf("/gadget.html"))+"/WebSocketDelApplication";
                if ('WebSocket' in window) {
                    this.websocaketDelApp = new WebSocket(path);//用于创建 WebSocket 对象。WebSocketTest对应的是java类的注解值
                }
                else {
                    console.log("当前浏览器不支持");
                }
                //连接发生错误的时候回调方法；
                this.websocaketDelApp.onerror = function () {
                    console.log("连接错误");
                }
                //连接成功时建立回调方法；
                this.websocaketDelApp.onopen = function () {
                    console.log("连接成功");
                }
                //收到消息的回调方法
                this.websocaketDelApp.onmessage = function (msg) {
                   // me.setdivInnerHTML(msg.data);
                    document.getElementById('returnDelApplication').innerHTML += msg.data;
                    document.getElementById("returnDelApplication").scrollTop=document.getElementById("returnDelApplication").scrollHeight;
                }
                //连接关闭的回调方法
                this.websocaketDelApp.onclose = function () {
                    console.log("关闭成功");
                }
            },
            websocketTest: function () {
                var me = this;
                var path="ws://"+window.location.href.substring(7,window.location.href.indexOf("/gadget.html"))+"/WebSocketTest";
                if ('WebSocket' in window) {
                    this.websocaket = new WebSocket(path);//用于创建 WebSocket 对象。WebSocketTest对应的是java类的注解值
                }
                else {
                    console.log("当前浏览器不支持");
                }
                //连接发生错误的时候回调方法；
                this.websocaket.onerror = function () {
                    console.log("连接错误");
                }
                //连接成功时建立回调方法；
                this.websocaket.onopen = function () {
                    console.log("连接成功");
                }
                //收到消息的回调方法
                this.websocaket.onmessage = function (msg) {
                    me.setdivInnerHTML(msg.data);
                }
                //连接关闭的回调方法
                this.websocaket.onclose = function () {
                    console.log("关闭成功");
                }
            },
            closea: function () {
                this.websocaket.close();
                console.log("点击关闭");
            },
            setdivInnerHTML: function (innerHTML) {
                document.getElementById('return').innerHTML += innerHTML;
                document.getElementById("return").scrollTop=document.getElementById("return").scrollHeight;
            },
            send: function () {
                var me = this;
                me.websocketTest();
                me.websocaket.addEventListener('open', function () {
                    /*
                    CONNECTING：值为0，表示正在连接；
                    OPEN：值为1，表示连接成功，可以通信了；
                    CLOSING：值为2，表示连接正在关闭；
                    CLOSED：值为3，表示连接已经关闭，或者打开连接失败。
                     */
                    me.websocaket.send("测试WebSocket");//给后台发送数据
                });
            },
        }); //fish.View.extend END
    }); //ALL END