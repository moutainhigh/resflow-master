package org.easy.excel.parsing;

import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.easy.excel.config.FieldValue;

public interface CellDropDownDaConverter {

    /**
     * 操作类型，导出模板使用
     */
    enum DropType {
        EXPORT, IMPORT
    }

    /**
     * 转换cell的值
     * @param sheet sheet
     * @param fieldValue FieldValue信息
     * @param type 导入或导出
     * @return 解析结果对应的value
     * @throws Exception
     */
    public void convertDrop(Sheet sheet, FieldValue fieldValue, CellDropDownDaConverter.DropType type, DataValidationHelper dvHelper) throws Exception;


}
