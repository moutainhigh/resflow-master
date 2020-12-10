package com.zres.project.localnet.portal.report.utils;

import com.ztesoft.res.frame.user.ThreadLocalInfoHolder;

/**
 * 人员相关工具类
 * @author  PangHao
 * @date    2019/5/21 : 9:53
 */
public class UserUtil {
    /**
     * 得到当前操作人标识
     *
     * @return 操作人标识
     * @author PangHao
     * @date 2019/4/25 : 16:32
     */
    public static String getOperatorId() {
        String operatorId;
        if (ThreadLocalInfoHolder.getLoginUser() == null) {
            operatorId = "11";
        } else {
            // 获取用户id
            operatorId = ThreadLocalInfoHolder.getLoginUser().getUserId();
        }
        return operatorId;
    }
}
