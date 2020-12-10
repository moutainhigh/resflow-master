package com.zres.project.localnet.portal.common;

import java.util.Map;

/**
 * @Classname CloudFlowFeedbackIntf
 * @Description 云组网反馈接口
 * @Author guanzhao
 * @Date 2020/11/23 11:49
 */
public interface CloudFlowFeedbackIntf {

    Map<String, Object> feedbackDoSomething(Map<String, Object> toOrderTacheDoSomeMap) throws Exception;

}
