package com.zres.project.localnet.portal.cloudNetWork.controller;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.cloudNetWork.CloudNetChangeOrderIntf;
import com.zres.project.localnet.portal.cloudNetWork.CloudNetQueryFullRouteIntf;
import com.zres.project.localnet.portal.cloudNetWork.CloudRentOrderServiceIntf;
import com.zres.project.localnet.portal.cloudNetWork.ReceiveOrderServiceIntf;
import com.zres.project.localnet.portal.cloudNetWork.service.CloudNetQueryOrderService;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 云组网工单处理controller
 * @author caomm on 2020/10/12
 */
@RestController
@RequestMapping("/cloudNetWork")
public class CloudNetWorkOrderController {
    @Autowired
    private ReceiveOrderServiceIntf receiveOrderService;
    @Autowired
    private CloudRentOrderServiceIntf cloudRentOrderServiceIntf;
    @Autowired
    private CloudNetChangeOrderIntf changeOrderService;
    @Autowired
    private CloudNetQueryFullRouteIntf cloudNetQueryFullRouteIntf;
    @Autowired
    private CloudNetQueryOrderService cloudNetQueryOrderService;

    /**
     * 收单接口
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/receiveOrder.spr", produces = "application/json;charset=UTF-8")
    public Map<String, Object> receiveOrder(@RequestBody String request){
        String retMsg = receiveOrderService.receiveOrder(request);
        JSONObject jasonObject = JSONObject.parseObject(retMsg);
        Map<String, Object> retMap = (Map<String, Object>) jasonObject;
        return retMap;
    }

    /**
     * 起止租接口
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/rentOrder.spr", produces = "application/json;charset=UTF-8")
    public Map<String, Object> startAndStopRentOrder(@RequestBody String request){
        String retMsg = cloudRentOrderServiceIntf.startAndStopRentOrder(request);
        JSONObject jasonObject = JSONObject.parseObject(retMsg);
        Map<String, Object> retMap = (Map<String, Object>) jasonObject;
        return retMap;
    }

    /**
     * 异常单接口
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/changeOrder.spr", produces = "application/json;charset=UTF-8")
    public Map<String, Object> changeOrder(@RequestBody String request){
        String retMsg = changeOrderService.changeOrder(request);
        JSONObject jasonObject = JSONObject.parseObject(retMsg);
        Map<String, Object> retMap = (Map<String, Object>) jasonObject;
        return retMap;
    }

    /**
     * 全程路由查询接口
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/queryFullRoute.spr", produces = "application/json;charset=UTF-8")
    public Map<String, Object> queryFullRoute(@RequestBody String request){
        String retMsg = cloudNetQueryFullRouteIntf.queryFullRoute(request);
        JSONObject jasonObject = JSONObject.parseObject(retMsg);
        Map<String, Object> retMap = (Map<String, Object>) jasonObject;
        return retMap;
    }
    /**
     * 进度查询接口
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/queryOrder.spr", produces = "application/json;charset=UTF-8")
    public Map<String, Object> queryOrder(@RequestBody String request){
        return cloudNetQueryOrderService.queryOrder(request);
    }
}