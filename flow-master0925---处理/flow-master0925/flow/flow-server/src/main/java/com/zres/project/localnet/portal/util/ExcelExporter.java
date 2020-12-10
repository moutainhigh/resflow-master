package com.zres.project.localnet.portal.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.ztesoft.res.frame.core.exception.ServiceBuizException;
import com.ztesoft.res.frame.core.util.MapUtil;

/**
 * 〈description〉<br>
 * 〈EXCEL导出服务〉POI 3.15 EXCEL 2007+
 *  author sunlb
 *  导出步骤:
 *  1、设置响应处理器 ResponseHandler(包含excel文件名)
 *  2、设置excel的heads、colsIds、datas数据
 *  3、创建SXSSFWorkbook workbook对象
 *  4、根据workbook创建sheet，并自定义名字
 *  5、初始化sheet的head以及head style
 *  6、填充sheet行数据(参考writeSheet方法)以及设置单元格的style
 *  7、导出数据export
 *  注：步骤4-6 可创建多个sheet以及填充数据
 */
public class ExcelExporter<T> {
    private List<String> heads;//excel列
    private List<String> colsIds;//对应sql列名
    private List<T> datas;//存储数据list
    private ResponseHandler handler;//响应处理handler
    private XSSFWorkbook workbook;//excel对象

    public ExcelExporter(List<String> heads, List<String> cols, List<T> datas, ResponseHandler handler) {
        this.heads = heads;
        this.colsIds = cols;
        this.datas = datas;
        this.handler = handler;
        this.handler.setResponse();
        this.initExcel();
    }

    public ExcelExporter(List<String> heads, List<String> cols,ResponseHandler handler) {
        this.heads = heads;
        this.colsIds = cols;
        this.handler = handler;
        this.handler.setResponse();
        this.initExcel();
    }

    public ExcelExporter(ResponseHandler handler) {
        this.handler = handler;
        this.handler.setResponse();
        this.initExcel();
    }

    /**
     * 功能描述: <br>
     * 〈导出，供客户端调用〉
     */
    public void export() {
        OutputStream out = null;
        try {
            out = handler.getOutputstream();
            workbook.write(out);
        }
        catch (IOException e) {
            throw new ServiceBuizException(e.getMessage(), e);
        }
        finally {
            ResEntityUtil.closeSteam(out);
        }
    }

    /**
     * 功能描述: <br>
     * 〈初始化EXCEL对象〉
     */
    public void initExcel() {
        if (workbook != null) {
            return;
        }
        this.workbook = new XSSFWorkbook();
    }

    /**
     * 功能描述: <br>
     * 〈初始化sheet对象〉
     */
    public XSSFSheet initSheet(String sheetName) {
        return workbook.createSheet(sheetName);
    }

    /**
     * 功能描述: <br> 自定义sheet名、表头样式
     * 〈初始化excel标题〉
     */
    public void initHeaders(XSSFSheet sheet,XSSFCellStyle selfCell) {
        Row title = sheet.createRow(0);
        title.setHeight((short) 440);
        for (int i = 0; i < this.colsIds.size(); i++) {
            Cell cell = title.createCell(i);
            sheet.setColumnWidth(i, 5000);
            cell.setCellValue(this.heads.get(i));
            cell.setCellStyle(this.createHeaderStyle(selfCell,null));
        }
    }

    /**
     * 功能描述: <br> 自定义sheet名、表头样式、表头字体
     * 〈初始化excel标题〉
     */
    public void initHeaders(Sheet sheet, XSSFCellStyle selfCell, XSSFFont selfFont) {
        Row title = sheet.createRow(0);
        title.setHeight((short) 440);
        for (int i = 0; i < this.colsIds.size(); i++) {
            Cell cell = title.createCell(i);
            sheet.setColumnWidth(i, 5000);
            cell.setCellValue(this.heads.get(i));
            cell.setCellStyle(this.createHeaderStyle(selfCell,selfFont));
        }
    }

    /**
     * 功能描述: <br>
     * 〈设置Excel标题样式，客户端可复写此方法〉
     */
    public XSSFCellStyle createHeaderStyle() {
        XSSFCellStyle style = this.workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(XSSFCellStyle.ALIGN_CENTER); //水平居中
        style.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER); //垂直居中
        style.setBorderBottom((short)1);
        style.setBorderLeft((short)1);
        style.setBorderRight((short)1);
        style.setBorderTop((short)1);
        XSSFFont font = this.workbook.createFont();
        font.setFontName("黑体");
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); //粗体
        style.setWrapText(true); //自动换行
        style.setFont(font);
        return style;
    }

    /**
     * 功能描述: <br> 自定义表头样式、字体
     * 〈设置Excel标题样式，客户端可复写此方法〉
     */
    public XSSFCellStyle createHeaderStyle(XSSFCellStyle style,XSSFFont font) {
        XSSFCellStyle styleInner = null;
        XSSFFont fontInner = null;
        if(style==null){
            styleInner = this.workbook.createCellStyle();
            styleInner.setFillForegroundColor(IndexedColors.RED.getIndex());
            styleInner.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleInner.setBorderBottom((short)1);
            styleInner.setBorderLeft((short)1);
            styleInner.setBorderRight((short)1);
            styleInner.setBorderTop((short)1);
        }else{
            styleInner = style;
        }
        if(font == null){
            fontInner = this.workbook.createFont();
            fontInner.setFontName("黑体");
            styleInner.setFont(fontInner);
        }else{
            styleInner.setFont(font);
        }
        return styleInner;
    }

    /**
     * 功能描述: <br>
     * 〈设置Excel正文样式，客户端可重写此方法〉
     */
    public XSSFCellStyle createContextStyle() {
        XSSFCellStyle cellStyle = this.workbook.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        cellStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
        cellStyle.setWrapText(true); //自动换行

        return cellStyle;
    }

    /**
     * 功能描述: <br> 自定义单元格样式、字体
     * 〈设置Excel正文样式，客户端可重写此方法〉
     */
    public XSSFCellStyle createContextStyle(XSSFCellStyle cellStyle,XSSFFont font) {
        XSSFCellStyle cellStyleInner = null;
//        Font fontInner = null;
        if(cellStyle == null){
            cellStyleInner = this.workbook.createCellStyle();
            cellStyleInner.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        }else {
            cellStyleInner = cellStyle;
        }
        if(font!=null){
            cellStyleInner.setFont(font);
        }else{
//            fontInner = this.workbook.createFont();
//            fontInner.setFontName("黑体");
//            cellStyleInner.setFont(fontInner);
        }
        return cellStyleInner;
    }



    /**
     * 功能描述: <br>
     * 〈初始化sheet并填充数据（对客户端隐藏）〉
     */
    public void fillSheet(String sheetName) {
        XSSFSheet sheet = this.initSheet(sheetName);
        this.initHeaders(sheet,null);
        this.writeSheet(sheet, null);
    }

    /**
     * 功能描述: <br>
     * 〈初始化sheet并填充数据（客户端可调用）,使用默认表头样式、表格样式〉
     */
    public void fillSheet(String sheetName,
                          List<String> heads,
                          List<String> cols,
                          List<T> datas) {
        this.heads = heads;
        this.colsIds = cols;
        this.datas = datas;
        this.fillSheet(sheetName);

    }

    /**
     * 功能描述: <br>
     * 〈初始化sheet并填充数据（客户端可调用）,自定义sheet名，表头样式，表格样式〉
     */
    public void fillSheet(String sheetName,
                          List<String> heads,
                          List<String> cols,
                          List<T> datas,
                          XSSFCellStyle headCellStyle,
                          XSSFCellStyle contextStyle) {
        this.heads = heads;
        this.colsIds = cols;
        this.datas = datas;
        this.fillSheet(sheetName,headCellStyle,contextStyle);
    }

    /**
     * 功能描述: <br>
     * 〈初始化sheet并填充数据（客户端可调用）,自定义sheet名，表头样式，表头字体,表格样式，表格字体〉
     */
    public void fillSheet(String sheetName,
                          List<String> heads,
                          List<String> cols,
                          List<T> datas,
                          XSSFCellStyle headCellStyle,
                          XSSFFont headFont,
                          XSSFCellStyle contextStyle,
                          XSSFFont contextFont) {
        this.heads = heads;
        this.colsIds = cols;
        this.datas = datas;
        this.fillSheet(sheetName,headCellStyle,headFont,contextStyle,contextFont);
    }

    /**
     * 功能描述: <br>
     * 〈初始化sheet并填充数据（对客户端隐藏）,自定义sheet名，表头样式，表格样式〉
     */
    public void fillSheet(String sheetName,
                          XSSFCellStyle headCellStyle,
                          XSSFCellStyle contextStyle) {
        XSSFSheet sheet = this.initSheet(sheetName);
        this.initHeaders(sheet,headCellStyle);
        this.writeSheet(sheet, contextStyle);
    }

    /**
     * 功能描述: <br>
     * 〈初始化sheet并填充数据（对客户端隐藏）,自定义sheet名，表头样式，表头字体,表格样式,表格字体〉
     */
    public void fillSheet(String sheetName,
                          XSSFCellStyle headCellStyle,
                          XSSFFont headFont,
                          XSSFCellStyle contextStyle,
                          XSSFFont contextFont) {
        XSSFSheet sheet = this.initSheet(sheetName);
        this.initHeaders(sheet,headCellStyle,headFont);
        this.writeSheet(sheet, contextStyle,contextFont);
    }

    /**
     * 功能描述: <br>
     * 〈向sheet对象写入数据〉
     */
    public void writeSheet(XSSFSheet sheet, XSSFCellStyle contextStyle) {
        for (int i = 0; i < this.datas.size(); i++) {
            //设置EXCEL数据
            Row row = sheet.createRow(i + 1);
            row.setHeight((short) 350);
            for (int j = 0; j < this.colsIds.size(); j++) {
                T entity = this.datas.get(i);
                EntityMapConverter<T> converter = new EntityMapConverter<T>();
                Map<String, Object> data = converter.convertEntity(entity);
                String colData = MapUtil.getString(data, this.colsIds.get(j));
                if (StringUtils.hasText(colData)) {
                    Cell dataCell = row.createCell(j);
                    dataCell.setCellValue(colData);
                    dataCell.setCellStyle(this.createContextStyle(contextStyle,null));
                }
            }
        }
    }

    /**
     * 功能描述: <br> 自定义sheet名、单元格样式、字体
     * 〈向sheet对象写入数据〉
     */
    public void writeSheet(XSSFSheet sheet, XSSFCellStyle contextStyle,XSSFFont selfFont) {
        for (int i = 0; i < this.datas.size(); i++) {
            //设置EXCEL数据
            Row row = sheet.createRow(i + 1);
            row.setHeight((short) 350);
            for (int j = 0; j < this.colsIds.size(); j++) {
                T entity = this.datas.get(i);
                EntityMapConverter<T> converter = new EntityMapConverter<T>();
                Map<String, Object> data = converter.convertEntity(entity);
                String colData = MapUtil.getString(data, this.colsIds.get(j));
                if (StringUtils.hasText(colData)) {
                    Cell dataCell = row.createCell(j);
                    dataCell.setCellValue(colData);
                    dataCell.setCellStyle(this.createContextStyle(contextStyle,selfFont));
                }
            }
        }
    }

    /**
     * 功能描述: <br> 自定义sheet名、单元格样式、字体,自定义数据，自定义从第N行写入数据
     * 〈向sheet对象写入数据〉
     */
    public void writeSheet(XSSFSheet sheet, XSSFCellStyle contextStyle,XSSFFont selfFont,List<T> datasList,int lastrow) {
        if(!CollectionUtils.isEmpty(datasList)){
            for (int i = 0; i < datasList.size(); i++) {
                //设置EXCEL数据
                Row row = sheet.createRow(lastrow + i + 1);
                row.setHeight((short) 350);
                for (int j = 0; j < this.colsIds.size(); j++) {
                    T entity = datasList.get(i);
                    EntityMapConverter<T> converter = new EntityMapConverter<T>();
                    Map<String, Object> data = converter.convertEntity(entity);
                    String colData = MapUtil.getString(data, this.colsIds.get(j));
                    if (StringUtils.hasText(colData)) {
                        Cell dataCell = row.createCell(j);
                        dataCell.setCellValue(colData);
                        dataCell.setCellStyle(this.createContextStyle(contextStyle,selfFont));
                    }
                }
            }
        }

    }

    /**
     * 创建Font对象
     * @return
     */
    public XSSFFont getWorkbookFont() {
        XSSFFont font = this.workbook.createFont();
        return font;
    }

    /**
     * 创建CellStyle对象
     * @return
     */
    public XSSFCellStyle getWorkbookCellStyle(){
        XSSFCellStyle cellStyle = this.workbook.createCellStyle();
        return cellStyle;
    }



}