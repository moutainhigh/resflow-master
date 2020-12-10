package com.zres.project.localnet.portal.messageList.service;

import com.zres.project.localnet.portal.messageList.dao.MessageListDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class MessageListService implements MessageListServiceIntf {

    @Autowired
    private MessageListDao messageListDao;

    @Override
    public List<Map<String, Object>> messageList(Map<String, Object> params) {
        return   messageListDao.exportrderList(params);
    }

}
