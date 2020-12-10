package com.zres.project.localnet.portal.util;

import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;

public class ResEntityUtil {


    public ResEntityUtil() {
    }


    /**
     * 功能描述: <br>
     * 〈关闭IO流，注意传参顺序〉
     *
     * @param closeables
     * @return void
     * @Author li.he
     * @Date 2018/11/8 0:07
     * @Modifier
     */
    public static void closeSteam(Closeable... closeables) {
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        }
        catch (IOException e) {
            Logger.getLogger(ExcelExporter.class).error(e.getMessage());
        }
    }


}
