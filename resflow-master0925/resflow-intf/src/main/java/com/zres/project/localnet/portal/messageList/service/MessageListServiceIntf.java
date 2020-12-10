package com.zres.project.localnet.portal.messageList.service;

import java.util.List;
import java.util.Map;

public interface MessageListServiceIntf {

    List<Map<String, Object>> messageList(Map<String, Object> params);
}
