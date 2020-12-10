package com.zres.project.localnet.portal.initApplOrderDetail.dao;

import java.util.List;
import java.util.Map;

public interface QueryAttachmentDao {
    List<Map<String, Object>> queryAttachmentId(Map<String, String> param);
}
