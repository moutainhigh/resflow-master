package com.zres.project.localnet.portal.orderAbnormal.exception;

import com.ztesoft.res.frame.flow.common.constant.FlowCommonEnum;
import com.ztesoft.res.frame.flow.common.exception.FlowException;

public class OrderAbnormalException extends FlowException {

    public OrderAbnormalException(FlowCommonEnum.ErrorCode errorCode, String sErrorMessage) {
        super(errorCode, sErrorMessage);
    }
}
