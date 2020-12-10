package com.zres.project.localnet.portal.webservice.oneDry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import org.apache.commons.collections4.MapUtils;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zres.project.localnet.portal.flowdealinfo.service.OrderDealService;
import com.zres.project.localnet.portal.util.AnalysisXML;
import com.zres.project.localnet.portal.webservice.data.dao.WebServiceDao;

import com.ztesoft.zsmart.pot.annotation.IgnoreSession;

/**
 * Created by jiangdebing on 2019/3/8.
 */
@Controller
@RequestMapping("/postponementApplyBackServiceIntf")
public class PostponementApplyBackService implements PostponementApplyBackServiceIntf {

    private static Logger logger = LoggerFactory.getLogger(PostponementApplyBackService.class);
    @Autowired
    private OrderDealService orderDealService;
    @Autowired
    private WebServiceDao wsd;
    @Autowired
    private OrderDealDao orderDealDao;

    @IgnoreSession
    @ResponseBody
    @RequestMapping(value = "/interfaceBDW/postponementApplyBack.spr", method = RequestMethod.POST, produces = "application/xml;charset=UTF-8")
    public String postponementApplyBack(@RequestBody String xml) {
        Map<String, Object> interflog = new HashMap<String, Object>(); //记录接口日志
        StringBuffer returnValue =  new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><resultFlag>");
        String resultFlag = "0";
        String resultContent = "延期成功";

        try {
            int logId = wsd.querySequence("SEQ_GOM_BDW_INTERF_LOG_INFO.NEXTVAL"); //获取接口日志信息表序列
            interflog.put("URL", "/postponementApplyBackServiceIntf/interfaceBDW/postponementApplyBack.spr");
            interflog.put("CONTENT", xml);
            interflog.put("ID", logId);
            interflog.put("REMARK", "接收一干延期反馈报文");
            logger.info("延期反馈报文：" + xml);
            Map<String, Object> rentdto = AnalysisXML.analysis(xml);
            //modify by wang.g2
            Map body = MapUtils.getMap(rentdto, "body");
            List< Map<String, Object>> prodsInfo = (List< Map<String, Object>>)MapUtils.getObject(body, "prodsInfo");
            for (Map<String, Object> prodInfo : prodsInfo) {
                Map applyInfo = new HashMap();
                String woId = MapUtils.getString(prodInfo, "woOrderId");//工单id
                String reason = MapUtils.getString(prodInfo, "reason");//审核说明
                String actType = "301".equals(MapUtils.getString(prodInfo, "actType")) ? "290000020" : "290000021";//审核意见 301同意 /302
                applyInfo.put("woId", woId);
                applyInfo.put("applyBack", reason);
                applyInfo.put("applyState", "290000004");//本地审核通过

                //通过工单id查询延期申请记录
                List<Map<String, Object>> postponementApply = orderDealDao.queryPostponementApply(applyInfo);
                if (postponementApply.size() > 0) {

                    String cstOrdId = MapUtils.getString(postponementApply.get(0), "CST_ORD_ID");
                    String srvOrdId = MapUtils.getString(postponementApply.get(0), "SRV_ORD_ID");
                    String newTime = MapUtils.getString(postponementApply.get(0), "POSTPONEMENT");
                    //更新延期申请状态
                    applyInfo.put("cstOrdId", cstOrdId);
                    applyInfo.put("applyState", actType); //更新一干返回状态
                    orderDealDao.updatePostponementApply(applyInfo);
                    //更新时间
                    Map attrInfo = new HashMap();
                    attrInfo.put("attrCode", "20000132");
                    attrInfo.put("attrValue", newTime);
                    attrInfo.put("srvOrdId", srvOrdId);
                    orderDealDao.updataCircuitInfo(attrInfo);

                }else{
                      resultFlag = "1";
                      resultContent = "失败，请检查woOrderId是否正确或已审核";
                }
            }
        } catch (Exception e) {
            resultFlag = "1";
            resultContent = e.getMessage();
            logger.debug("处理失败：" + e.getMessage());
        }

        returnValue.append(resultFlag)
                .append("</resultFlag><resultContent>")
                .append(resultContent).append("</resultContent></root>");
        interflog.put("RETURNCONTENT", returnValue);
        wsd.updateInterfLog(interflog); //修改日志表返回结果
        return returnValue.toString();
    }
}
