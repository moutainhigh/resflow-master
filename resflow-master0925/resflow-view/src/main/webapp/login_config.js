/**
 * 主页require配置文件主入口
 */
require.config({ 
    waitSeconds:0
});
require(['frame/ext/ngc'],function(n){
		require(["module/portal/login/views/LoginView"], function(LoginView) {
			
			var login_params = {
					md5 : false,
					// local:[{key:'ltjt',text:'雄安'}],
					showPassOnOutService:true,
					/**
					 * @param 获取是否启用验证码URL
					 */
					//captchaFlagUrl: "login.spr?method=getCaptchaFlag",
					
					/**
					 * @param 获取用户登录状态URL
					 */
					loginCheckUrl: "login.spr?method=getCurrentLoginUser",
					
						/**
					 * @param 获取验证码的Url
					 */
					//captchaUrl: "login.spr?method=captcha",					
					/**
					 * 登陆页面登陆成功回调函数配置
					 * @method loginSuccess
					 * @param data 登陆成功之后返回的data数据
					 * @param reload 切换用户重新登陆
					 * @param loaded 本次登陆之前是否已经登陆
					 */
					loginSuccess:function(data, reload ,loaded){
							var l = ngc.getUrlParam("_lurl");
							if(l==null||l==''){
								window.location.href="gomIndex.html";
							}else {
								window.location.href = l;
							}
					}					
					
			};
			
			//获取登陆用户
			 new LoginView({el:'#login_form',config:login_params}).render();	
		});  
});

