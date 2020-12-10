/**
 * FileName: EntityMapConverter
 */
package com.zres.project.localnet.portal.util;

import com.ztesoft.res.frame.core.exception.ServiceBuizException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 〈description〉<br>
 * 〈实体-Map转化工具〉
 */
public class EntityMapConverter<T> {

    /**
     * 功能描述: <br>
     * 〈实体类转化为Map。实体类必须有属性的get方法〉
     *
     */
    public Map<String, Object> convertEntity(T entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof Map) {
            return Map.class.cast(entity);
        }
        Field[] fields = entity.getClass().getDeclaredFields();
        Map<String, Object> data = new HashMap<String, Object>();
        for (Field field : fields) {
            try {
                String getter = getGetterName(field.getName());
                Method method = entity.getClass().getMethod(getter);
                data.put(field.getName(), method.invoke(entity));
            }
            catch (Exception e) {
                throw new ServiceBuizException(e.getMessage(), e);
            }
        }
        return data;
    }


    /**
     * 功能描述: <br>
     * 〈根据属性名获得其get方法名〉
     *
     */
    private String getGetterName(String fieldName) throws UnsupportedEncodingException {
        byte[] buffer = fieldName.getBytes("UTF-8");
        buffer[0] = (byte) (buffer[0] - 32);
        String name = new String(buffer, "UTF-8");
        return "get" + name;
    }

}