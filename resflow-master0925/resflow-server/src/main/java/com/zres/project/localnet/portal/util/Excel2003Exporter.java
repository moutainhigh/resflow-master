package com.zres.project.localnet.portal.util;

import com.ztesoft.res.frame.core.exception.ServiceBuizException;
import com.ztesoft.res.frame.core.util.MapUtil;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 〈description〉<br>
 * 〈EXCEL导出服务〉POI 3.15 EXCEL 2003
 *  author sunlb
 *  导出步骤:
 *  1、设置响应处理器 ResponseHandler(包含excel文件名)
 *  2、设置excel的heads、colsIds、datas数据
 *  3、创建HSSFWorkbook workbook对象
 *  4、根据workbook创建sheet，并自定义名字
 *  5、初始化sheet的head以及head style
 *  6、填充sheet行数据(参考writeSheet方法)以及设置单元格的style
 *  7、导出数据export
 *  注：步骤4-6 可创建多个sheet以及填充数据
 */
public class Excel2003Exporter<T> {
    private List<String> heads;//excel列
    private List<String> colsIds;//对应sql列名
    private List<T> datas;//存储数据list
    private ResponseHandler handler;//响应处理handler
    private HSSFWorkbook workbook;//excel对象


    public Excel2003Exporter(List<String> heads, List<String> cols, List<T> datas, ResponseHandler handler) {
        this.heads = heads;
        this.colsIds = cols;
        this.datas = datas;
        this.handler = handler;
        this.handler.set2003Response();
        this.initExcel();
    }

    public Excel2003Exporter(List<String> heads, List<String> cols, ResponseHandler handler) {
        this.heads = heads;
        this.colsIds = cols;
        this.handler = handler;
        this.handler.set2003Response();
        this.initExcel();
    }

    public Excel2003Exporter(ResponseHandler handler) {
        this.handler = handler;
        this.handler.set2003Response();
        this.initExcel();
    }

    /**
     * 功能描述: <br>
     * 〈导出，供客户端调用〉
     */
    public void export() {
        OutputStream out = null;
        try {
            out = this.handler.getOutputstream();
            this.workbook.write(out);
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
        if (this.workbook != null) {
            return;
        }
        this.workbook = new HSSFWorkbook();
    }

    /**
     * 功能描述: <br>
     * 〈初始化sheet对象〉
     */
    public HSSFSheet initSheet(String sheetName) {
        return this.workbook.createSheet(sheetName);
    }

    /**
     * 功能描述: <br> 自定义sheet名、表头样式
     * 〈初始化excel标题〉
     */
    public void initHeaders(HSSFSheet sheet, HSSFCellStyle selfCell) {
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
    public void initHeaders(HSSFSheet sheet,HSSFCellStyle selfCell,HSSFFont selfFont) {
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
    public HSSFCellStyle createHeaderStyle() {
        HSSFCellStyle style = this.workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
//        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillPattern((short) 1);
        style.setBorderBottom((short)1);
        style.setBorderLeft((short)1);
        style.setBorderRight((short)1);
        style.setBorderTop((short)1);
        HSSFFont font = this.workbook.createFont();
        font.setFontName("黑体");
        style.setFont(font);
        return style;
    }

    /**
     * 功能描述: <br> 自定义表头样式、字体
     * 〈设置Excel标题样式，客户端可复写此方法〉
     */
    public HSSFCellStyle createHeaderStyle(HSSFCellStyle style, HSSFFont font) {
        HSSFCellStyle styleInner = null;
        HSSFFont fontInner = null;
        if(style==null){
            styleInner = this.workbook.createCellStyle();
            styleInner.setFillForegroundColor(IndexedColors.RED.getIndex());
            styleInner.setFillPattern((short) 1);
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
            styleInner .setFont(font);
        }
        return styleInner;
    }

    /**
     * 功能描述: <br>
     * 〈设置Excel正文样式，客户端可重写此方法〉
     */
    public HSSFCellStyle createContextStyle() {
        HSSFCellStyle cellStyle = this.workbook.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        return cellStyle;
    }

    /**
     * 功能描述: <br> 自定义单元格样式、字体
     * 〈设置Excel正文样式，客户端可重写此方法〉
     */
    public HSSFCellStyle createContextStyle(HSSFCellStyle cellStyle,HSSFFont font) {
        HSSFCellStyle cellStyleInner = null;
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

        HSSFSheet sheet = this.initSheet(sheetName);
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
                          HSSFCellStyle headCellStyle,
                          HSSFCellStyle contextStyle) {
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
                          HSSFCellStyle headCellStyle,
                          HSSFFont headFont,
                          HSSFCellStyle contextStyle,
                          HSSFFont contextFont) {
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
                          HSSFCellStyle headCellStyle,
                          HSSFCellStyle contextStyle) {
        HSSFSheet sheet = this.initSheet(sheetName);
        this.initHeaders(sheet,headCellStyle);
        this.writeSheet(sheet, contextStyle);
    }

    /**
     * 功能描述: <br>
     * 〈初始化sheet并填充数据（对客户端隐藏）,自定义sheet名，表头样式，表头字体,表格样式,表格字体〉
     */
    public void fillSheet(String sheetName,
                          HSSFCellStyle headCellStyle,
                          HSSFFont headFont,
                          HSSFCellStyle contextStyle,
                          HSSFFont contextFont) {
        HSSFSheet sheet = this.initSheet(sheetName);
        this.initHeaders(sheet,headCellStyle,headFont);
        this.writeSheet(sheet, contextStyle,contextFont);
    }

    /**
     * 功能描述: <br>
     * 〈向sheet对象写入数据〉
     */
    public void writeSheet(Sheet sheet, HSSFCellStyle contextStyle) {
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
    public void writeSheet(Sheet sheet, HSSFCellStyle contextStyle,HSSFFont selfFont) {
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
     * 功能描述: <br> 自定义sheet名、单元格样式、字体
     * 〈向sheet对象写入数据〉
     */
    public void writeSheet(Sheet sheet, HSSFCellStyle contextStyle,HSSFFont selfFont,List<T> datasList,int lastrow) {
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

    /**
     * 创建Font对象
     * @return
     */
    public HSSFFont getWorkbookFont() {
        HSSFFont font = this.workbook.createFont();
        return font;
    }

    /**
     * 创建CellStyle对象
     * @return
     */
    public HSSFCellStyle getWorkbookCellStyle(){
        HSSFCellStyle cellStyle = this.workbook.createCellStyle();
        return cellStyle;
    }



}
