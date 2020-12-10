package com.zres.project.localnet.portal.messageList.dao;

import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface MessageListDao {
    List<Map<String, Object>> exportrderList(Map<String, Object> params);
}
