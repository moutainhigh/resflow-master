package com.zres.project.localnet.portal.local.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zres.project.localnet.portal.local.PostponementServiceIntf;
import com.zres.project.localnet.portal.local.dao.PostponementDao;

@Service
public class PostponementService implements PostponementServiceIntf {
    @Autowired
    private PostponementDao postponementDao;
    @Override
    public List<Map<String, Object>> queryPostponementInfoBySrvId(String srvOrdId) {
        return postponementDao.queryPostponementInfoBySrvId(srvOrdId);
    }
}
