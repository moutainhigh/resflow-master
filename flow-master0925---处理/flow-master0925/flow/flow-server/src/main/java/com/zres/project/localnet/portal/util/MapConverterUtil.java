package com.zres.project.localnet.portal.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zres.project.localnet.portal.webservice.dto.FieldMeta;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @Description:转换器：map和javabean的互相转换，本类如果要通用需要去掉FieldMeta相关
 * @Author:zhang.kaigang
 * @Date:2019/5/16 16:12
 * @Version:1.0
 */
public class MapConverterUtil {

    private static Logger logger = LoggerFactory.getLogger(MapConverterUtil.class);
    /**
     * map转bean
     * @param source   map属性
     * @param instance 要转换成的备案
     * @return 该bean
     */
    public static <T> T convertMap2Bean(Map<String, Object> source, Class<T> instance) {
        try {
            T object = instance.newInstance();
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                FieldMeta fieldMeta = field.getAnnotation(FieldMeta.class);
                if (fieldMeta != null && StringUtils.isNotEmpty(fieldMeta.enumColumn())) {
                    // 特殊处理枚举值对应描述
                    Object objTemp = getFieldValue(object, field.getName());
                    String newValue;
                    if (objTemp == null) {
                        newValue = "";
                    }
                    else {
                        newValue = objTemp.toString();
                    }
                    field.set(object, newValue);
                }
                else {
                    field.set(object, source.get(field.getName()));
                }
            }
            return object;
        }
        catch (InstantiationException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * List<Map>转List<Javabean>
     * @param listMap
     * @param instance
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> convertListMap2ListBean(List<Map<String, Object>> listMap, Class<T> instance) {
        try {
            List<T> beanList = new ArrayList<T>();
            for (int i = 0, n = listMap.size(); i < n; i++) {
                Map<String, Object> map = listMap.get(i);
                //路由信息   一干单独入到别的表
                if("oldSimpleRoute".equals(MapUtils.getString(map,"code"))
                        || "simpleRoute".equals(MapUtils.getString(map,"code"))
                        || "oldFullRoute".equals(MapUtils.getString(map,"code"))
                        || "fullRoute".equals(MapUtils.getString(map,"code"))
                        || "route".equals(MapUtils.getString(map,"code"))
                ){
                    continue;
                }else {
                    T bean = convertMap2Bean(map, instance);
                    beanList.add(bean);
                }
            }
            return beanList;
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取Obj对象的fieldName属性的值
     * @param obj
     * @param fieldName
     * @return
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        Object fieldValue = null;
        if (null == obj) {
            return null;
        }
        Method[] methods = obj.getClass().getDeclaredMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (!methodName.startsWith("get")) {
                continue;
            }
            if (methodName.startsWith("get") && methodName.substring(3).toUpperCase().equals(fieldName.toUpperCase())) {
                try {
                    fieldValue = method.invoke(obj, new Object[] {});
                }
                catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    continue;
                }
            }
        }
        return fieldValue;
    }

    /**
     * 将变更信息表中的json数组转为map
     * @param jsonArrayTemp
     * @return
     */
    public static Map<String, String> convertJsonArrayToMap(String jsonArrayTemp) {
        String jsonArray = jsonArrayTemp.replace('\n', ' ');
        jsonArray = jsonArray.replace('\r', ' ');
        JSONArray array = JSONArray.fromObject(jsonArray);
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            map.put(String.valueOf(jsonObject.get("key")), String.valueOf(jsonObject.get("newValue")));
        }
        return map;
    }

}
