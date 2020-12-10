package com.zres.project.localnet.portal.webservice.oneDry;

import com.zres.project.localnet.portal.util.FtpConfigDto;
import com.zres.project.localnet.portal.util.FtpOper;
import com.zres.project.localnet.portal.util.IomFtpClient;
import com.zres.project.localnet.portal.util.SpringContextHolderUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * Created by jiangdebing on 2019/1/18.
 */
public class HandleAttachment extends Thread {
    private static Logger logger = LoggerFactory.getLogger(HandleAttachment.class);

    private List<Map<String, Object>> obj = null;

    private String downloadFtpInfo = null;

    private String uploadFtpInfo = null;

    private String origin = "";

    private JdbcTemplate springJdbcTemplate = SpringContextHolderUtil.getBean("springJdbcTemplate");
    public HandleAttachment(List<Map<String, Object>> ect, String downloadFtpInfo, String uploadFtpInfo, String origin) {
        obj = ect;
        this.downloadFtpInfo = downloadFtpInfo;
        this.uploadFtpInfo = uploadFtpInfo;
        this.origin = origin;
    }

    @Override
    public void run() {
        IomFtpClient FtpClient = null;
        IomFtpClient JTFtpClient = null;
        try {
            Thread.sleep(1000); //设置推迟时间
            // 需要下载的服务器信息
            FtpConfigDto JTFtpConfig = getFtpConfigInfo(downloadFtpInfo);
            // 需要上传的服务器信息
            FtpConfigDto FtpConfig = getFtpConfigInfo(uploadFtpInfo);
            JTFtpClient = getFtpConfig(JTFtpConfig.getIp(), JTFtpConfig.getPort(), JTFtpConfig.getUsername(),
                JTFtpConfig.getPassword());
            FtpClient = getFtpConfig(FtpConfig.getIp(), FtpConfig.getPort(), FtpConfig.getUsername(),
                FtpConfig.getPassword());
            String newPath = FtpOper.getBaseAttachFilePath(FtpConfig.getLocaldir()); //服务器创建目录
            String holdPath = FtpOper.getBaseAttachFilePath(FtpConfig.getRemotedir()); //数据库保存目录
            for (int i = 0; i < obj.size(); i++) {
                Map<String, Object> attachment = obj.get(i);
                if (attachment != null && attachment.size() > 0) {
                    String srv_ord_id = MapUtils.getString(attachment,"srv_ord_id","");
                    String dispatch_order_id = MapUtils.getString(attachment,"dispatch_order_id","");
                    String cst_ord_id = MapUtils.getString(attachment,"cst_ord_id","");
                    String file_path = MapUtils.getString(attachment,"path",""); //集团附件FILE_PATH
                    String file_id = MapUtils.getString(attachment,"name",""); //集团附件FILE_ID带后缀
                    String file_name = MapUtils.getString(attachment,"value",""); //集团附件FILE_NAME
                    String file_type = MapUtils.getString(attachment,"type",""); //集团附件FILE_NAME
                    /*if("JIKE".equals(origin)){
                        newPath = "/download/";
                    }*/
                    if ("uploadJiKe".equals(origin)) {
                        // 上传集客时，把附件表记录的filePath改成服务器根目录下的路径
                        String rootpath = JTFtpConfig.getLocaldir();
                        String remotePath = JTFtpConfig.getRemotedir();
                        if (rootpath.contains(remotePath)) {
                            file_path = rootpath.replace(remotePath, file_path);
                        }
                        newPath = "/upload/";
                        FtpOper.ftpTransFile(file_path + "/" + file_id, newPath + file_id, JTFtpClient, FtpClient);
                    } else if ("JIKE".equals(origin) || "JIKE_4A".equals(origin) ) {
                        FtpOper.createFolder(newPath, FtpClient);
                        FtpOper.ftpTransFile(
                            JTFtpConfig.getLocaldir() + file_path + JTFtpConfig.getRemotedir() + file_id,
                            newPath + file_id, JTFtpClient, FtpClient); // 下载上传附件
                        // 入库附件记录
                        springJdbcTemplate.update(
                            "insert into GOM_BDW_ATTACH_INFO (ATTACH_INFO_ID, SRV_ORD_ID, FILE_ID, FILE_NAME, FILE_PATH, FILE_TYPE, CREATE_DATE, WO_ORD_ID, ORIGIN) values (SEQ_GOM_BDW_ATTACH_INFO.NEXTVAL+1, "
                                + srv_ord_id + ", '" + file_id.substring(0, file_id.lastIndexOf('.')) + "', '"
                                + file_name + "', '" + holdPath + "', '" + file_type + "', sysdate, 0, '" + origin
                                + "' )");
                    } else if ("ONEDRY".equals(origin)) {
                        FtpOper.createFolder(newPath, FtpClient);
                        FtpOper.ftpTransFile(
                                JTFtpConfig.getLocaldir() + file_path + JTFtpConfig.getRemotedir() + file_id,
                                newPath + file_id, JTFtpClient, FtpClient); // 下载上传附件
                        // 入库附件记录
                        springJdbcTemplate.update(
                                "insert into GOM_BDW_ATTACH_INFO (ATTACH_INFO_ID, SRV_ORD_ID, FILE_ID, FILE_NAME, FILE_PATH, FILE_TYPE, CREATE_DATE, WO_ORD_ID, ORIGIN，DISPATCH_ORDER_ID，CST_ORD_ID) values (SEQ_GOM_BDW_ATTACH_INFO.NEXTVAL+1, "
                                        + srv_ord_id + ", '" + file_id.substring(0, file_id.lastIndexOf('.')) + "', '"
                                        + file_name + "', '" + holdPath + "', '" + file_type + "', sysdate, 0, '" + MapUtils.getString(attachment,"origin","")
                                        + "', " + dispatch_order_id + ", " + cst_ord_id + ")");
                    }
                }
            }
        }
        catch (Exception e) {
            logger.debug("附件处理异常" + e);
        }
        finally {
            FtpOper.disConnect(JTFtpClient);
            FtpOper.disConnect(FtpClient);
        }
    }

    public FtpConfigDto getFtpConfigInfo(String codeType) throws Exception { // 获取FTP连接
        FtpConfigDto configInfo = null;
        String sql1 = "select CODE_VALUE from gom_BDW_code_info where CODE_TYPE  = '" + codeType + "'and CODE_TYPE_NAME='ip'";
        String sql2 = "select CODE_VALUE from gom_BDW_code_info where CODE_TYPE  = '" + codeType + "'and CODE_TYPE_NAME='port'";
        String sql3 = "select CODE_VALUE from gom_BDW_code_info where CODE_TYPE  = '" + codeType + "'and CODE_TYPE_NAME='username'";
        String sql4 = "select CODE_VALUE from gom_BDW_code_info where CODE_TYPE  = '" + codeType + "'and CODE_TYPE_NAME='password'";
        String sql5 = "select CODE_VALUE,SORT_NO from gom_BDW_code_info where CODE_TYPE  = '" + codeType + "'and CODE_TYPE_NAME='directory'";
        Map<String, Object> retMap = springJdbcTemplate.queryForMap(sql1);
        String ip = retMap.get("CODE_VALUE").toString();
        retMap = springJdbcTemplate.queryForMap(sql2);
        String port = retMap.get("CODE_VALUE").toString();
        retMap = springJdbcTemplate.queryForMap(sql3);
        String name = retMap.get("CODE_VALUE").toString();
        retMap = springJdbcTemplate.queryForMap(sql4);
        String pwd = retMap.get("CODE_VALUE").toString();
        retMap = springJdbcTemplate.queryForMap(sql5);
        String path = retMap.get("CODE_VALUE").toString();
        String holdpath = retMap.get("SORT_NO") + ""; // 保存目录
        configInfo = new FtpConfigDto(ip, port, name, pwd, path, holdpath);
        return configInfo;

    }

    public IomFtpClient getFtpConfig(String ip, String port, String name, String pwd) throws Exception { //获取FTP Client对象
        IomFtpClient ftpClient = IomFtpClient.getFtpClient(ip, port, name, pwd);
        return ftpClient;
    }
}
