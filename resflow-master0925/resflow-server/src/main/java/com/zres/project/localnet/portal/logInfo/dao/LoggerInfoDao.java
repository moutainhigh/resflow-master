package com.zres.project.localnet.portal.logInfo.dao;

import java.util.Map;

import com.zres.project.localnet.portal.logInfo.entry.ResRollbackLog;
import org.springframework.stereotype.Repository;

import com.zres.project.localnet.portal.logInfo.entry.ResCreateInstanceFlag;
import com.zres.project.localnet.portal.logInfo.entry.ResInterfaceLog;
import com.zres.project.localnet.portal.logInfo.entry.ToKafkaTacheLog;

@Repository
public interface LoggerInfoDao {

    /**
     * 资源接口交互日志入库
     * @param resInterfaceLog
     * @return
     */
    int saveResInterfaceInfo(ResInterfaceLog resInterfaceLog);

    /**
     * 创建实例成功后，添加入库标识
     * @param resCreateInstanceFlag
     * @return
     */
    int insertResCreateInstanceFlag(ResCreateInstanceFlag resCreateInstanceFlag);


    /**
     * 删除已创建实例记录
     * @param resRollbackLog
     */
    void deleteResAttrCode(ResRollbackLog resRollbackLog);

    /**
     * 查询二干单信息--推送kafka
     * @param qryMap
     * @return
     */
    ToKafkaTacheLog qryOrderInfo(Map<String, String> qryMap);

    /**
     * 查询二干下本地单信息--推送kafka
     * @param qryMap
     * @return
     */
    ToKafkaTacheLog qryOrderInfoSec(Map<String, String> qryMap);

}
