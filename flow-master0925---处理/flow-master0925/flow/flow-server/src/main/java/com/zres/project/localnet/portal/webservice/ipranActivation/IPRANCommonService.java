package com.zres.project.localnet.portal.webservice.ipranActivation;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class IPRANCommonService {

    /**
     * 登录认证
     * 受理系统需要调用协同器接口时使用，认证成功后，返回用于生成token的串码
     * @return
     */
    public Map<String, Object> ipranLogin(){
        Map<String, Object> resultMap = new HashMap<>();

        return resultMap;
    }

    /**
     * 注销会话
     * 受理系统注销当前已经登录的会话
     * @return
     */
    public Map<String, Object> ipranLogout(){
        Map<String, Object> resultMap = new HashMap<>();

        return resultMap;
    }
}
