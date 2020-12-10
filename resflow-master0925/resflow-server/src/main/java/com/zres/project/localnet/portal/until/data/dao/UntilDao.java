package com.zres.project.localnet.portal.until.data.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;


@Repository
public interface UntilDao {
    List<Map<String, Object>> queryConfigParamById();

    void updateConfigById(Map<String, Object> map);

    List<Map<String, Object>>   queryStatisticsByCondition(Map<String, Object> map);

    void insertStatistics(List<Map<String, Object>> list);

    void deleteStatistcsById(List<Map<String, Object>> list);

    void deleteCableOpticById(String parentId);

    void insertCableOptic(List<Map<String, Object>> list);

    void insertCableRate(List<Map<String, Object>> list);

    void deleteCableRate(Map<String, Object> map);

    List<Map<String, Object>> queryLineDataByCondition(Map<String, Object> map);

    String queryHomePageSeq();

    void insertHomePageByCondition(Map<String, Object> map);

    List<Map<String, Object>> queryConfigParamByCondition(Map<String, Object> map);

    void updateConfigByCondition(Map<String, Object> map);

    List<Map<String, Object>> queryYearLineDataByCondition(Map<String, Object> map);

    void deleteConfigById(Map<String, Object> map);
}
