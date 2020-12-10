package com.zres.project.localnet.portal.until.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.zres.project.localnet.portal.flowdealinfo.data.dao.OrderDealDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.DelOrderInfoDao;
import com.zres.project.localnet.portal.initApplOrderDetail.dao.EditDraftDao;
import com.zres.project.localnet.portal.initApplOrderDetail.service.DelOrderInfoService;
import com.zres.project.localnet.portal.webservice.res.BusinessRollbackServiceIntf;

import com.ztesoft.res.frame.core.util.MapUtil;

/**
 * @author :ren.jiahang
 * @date:2019/8/11@time:16:40
 */
@ServerEndpoint("/WebSocketDelApplication")
public class WebSocketDelApplication {
    Logger logger = LoggerFactory.getLogger(WebSocketDelApplication.class);
    @Autowired
    private static BusinessRollbackServiceIntf businessRollbackServiceIntf;
    @Autowired
    private static EditDraftDao editDraftDao;
    @Autowired
    private static OrderDealDao orderDealDao;
    @Autowired
    private static DelOrderInfoDao delOrderInfoDao;
    @Autowired
    private static DelOrderInfoService delOrderInfoService;
    private Session session;
    @OnOpen//打开连接执行
    public void onOpw(Session session) {
        this.session=session;
        System.out.println("打开了连接");
    }
    @OnMessage//收到消息执行
    public void onMessage(String message,Session session) {
        System.out.println(message);
        try {
            sendMessage(message);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @OnClose//关闭连接执行
    public void onClose(Session session) {
        System.out.println("关闭连接");
    }
    @OnError//连接错误的时候执行
    public void onError(Throwable error,Session session) {
        System.out.println("错误的时候执行");
        error.printStackTrace();
    }
    /*
    websocket  session发送文本消息有两个方法：getAsyncRemote()和
   getBasicRemote()  getAsyncRemote()和getBasicRemote()是异步与同步的区别，
   大部分情况下，推荐使用getAsyncRemote()。
  */
    public void sendMessage(String message) throws IOException{

        String[] applicationCodes = message.split(",");
        for(String applicationCode:applicationCodes){
            StringBuilder result=new StringBuilder();
            List<String> CustOrdIdList = editDraftDao.queryCustOrdIdByApplicationCode(applicationCode);
            if (CustOrdIdList.isEmpty()){
                result.append(applicationCode+"-:信息已经删除，不必再次删除！");
            }else{
                for (String custOrdId:CustOrdIdList) {
                    List<String> srvOrdIdList = editDraftDao.querySrvOrdIdByCustId(custOrdId,"");
                    if (!srvOrdIdList.isEmpty()) {
                        try {
                            /**
                             * 如果是退单过来的单子，草稿箱删除要回滚实例
                             * 1,先查询是否调用过创建实例接口
                             * 2，再查询是否成功调用多回滚接口
                             */
                            for (int i = 0; i < srvOrdIdList.size(); i++) {
                                Map<String, Object> param = new HashMap<String, Object>();
                                // 调用资源回滚接口
                                param.put("srvOrdId", srvOrdIdList.get(i));// gom_bdw_srv_ord_info.srv_ord_id
                                param.put("rollbackDesc", "草稿箱删除。。。"); // 回滚原因
                                Map retmap =  businessRollbackServiceIntf.resRollBack(param);
                                if(!MapUtils.getBoolean(retmap,"success")){
                                    result.append(applicationCode+"-->")
                                            .append(MapUtil.getString(retmap,"message"))
                                            .append("--调用资源回滚接口失败,可能没有创建资源，但是申请单信息已删除\n");

                                    logger.error("调用资源回滚接口失败！！！" + MapUtils.getString(retmap, "message"));
                                }
                            }
                            //删除业务订单对应的电路信息
                            // this.delOrdAttrInfoBySrvId(srvOrdIdList);
                            delOrderInfoDao.delOrdAttrInfoAll(srvOrdIdList);
                            //删除业务订单信息
                            delOrderInfoService.delOrderInfoBySrvId(srvOrdIdList);
                            //删除客户信息
                            delOrderInfoService.delCustomerInfo(custOrdId);
                        }
                        catch (Exception e) {
                            result.append(e.getMessage()) ;
                            logger.info(e.getMessage());
                            // e.printStackTrace();
                        }
                    }
                }
                result.append(applicationCode).append("删除成功\r\r");
            }
            this.session.getBasicRemote().sendText(result.toString());
        }
        //同步发送 发送第二条时，必须等第一条发送完成
        // this.session.getBasicRemote().sendText("haha");
        //异步发送
        //this.session.getAsyncRemote().sendText(message);
        session.close();
    }
}
