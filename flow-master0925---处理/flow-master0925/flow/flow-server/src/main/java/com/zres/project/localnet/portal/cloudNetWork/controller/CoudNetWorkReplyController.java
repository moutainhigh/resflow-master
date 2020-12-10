package com.zres.project.localnet.portal.cloudNetWork.controller;

import com.alibaba.fastjson.JSONObject;
import com.zres.project.localnet.portal.cloudNetWork.PortInfoSyncServiceIntf;
import com.zres.project.localnet.portal.cloudNetWork.TerminalBoxOnLineFeedBackServiceIntf;
import com.zres.project.localnet.portal.cloudNetWork.TerminalBoxPortSyncServiceIntf;
import com.zres.project.localnet.portal.cloudNetWork.service.*;
import com.ztesoft.zsmart.pot.annotation.IgnoreSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 终端信息处理controller
 *
 * @author caomm on 2020/10/19
 */
@RestController
@RequestMapping("/cloudNetWork/reply")
public class CoudNetWorkReplyController {

    @Autowired
    private TerminalInfoSyncService terminalInfoSyncService;
    @Autowired
    private TerminalBoxOnLineFeedBackServiceIntf terminalBoxFeedBackService;
    @Autowired
    private TerminalBoxPortSyncServiceIntf terminalBoxPortSyncService;
    @Autowired
    private PortInfoSyncServiceIntf portInfoSyncService;
    @Autowired
    private CloudNetConfigReplyService cloudNetConfigReplyService;
    @Autowired
    private CloudNetWholeTestReplyService cloudNetWholeTestReplyService;
    @Autowired
    private CloudNetIpranPortReplyService cloudNetIpranPortReplyService;
    @Autowired
    private CloudNetIpranRouteReplyService cloudNetIpranRouteReplyService;
    @Autowired
    private CloudNetIpranActiveReplyService cloudNetIpranActiveReplyService;
    @Autowired
    private CloudNetIpranInactiveReply cloudNetIpranInactiveReply;

    /**
     * 终端信息同步接口
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/terminalInfoSync.spr", produces = "application/json;charset=UTF-8")
    public Map<String, Object> terminalInfoSync(@RequestBody String request){
        String ret = terminalInfoSyncService.terminalInfoSync(request);
        JSONObject jasonObject = JSONObject.parseObject(ret);
        Map<String, Object> retMap = (Map<String, Object>) jasonObject;
        return retMap;
    }

    /**
     * 终端盒上线结果反馈接口
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/onLineFeedBack.spr", produces = "application/json;charset=UTF-8")
    public Map<String, Object> onlineFeedBack(@RequestBody String request){
        String ret = terminalBoxFeedBackService.onLineFeedBack(request);
        JSONObject jasonObject = JSONObject.parseObject(ret);
        Map<String, Object> retMap = (Map<String, Object>) jasonObject;
        return retMap;
    }

    /**
     * 终端盒下线结果反馈接口
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/downLineFeedBack.spr", produces = "application/json;charset=UTF-8")
    public Map<String, Object> downLineFeedBack(@RequestBody String request){
        String ret = terminalBoxFeedBackService.onLineFeedBack(request);
        JSONObject jasonObject = JSONObject.parseObject(ret);
        Map<String, Object> retMap = (Map<String, Object>) jasonObject;
        return retMap;
    }

    /**
     * 终端盒端口业务占用同步接口
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/terminalBoxPortSync.spr", produces = "application/json;charset=UTF-8")
    public Map<String, Object> terminalBoxPortSync(@RequestBody String request){
        String ret = terminalBoxPortSyncService.terminalBoxPortSync(request);
        JSONObject jasonObject = JSONObject.parseObject(ret);
        Map<String, Object> retMap = (Map<String, Object>) jasonObject;
        return retMap;
    }

    /**
     * 端口信息同步接口
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/portInfoSync.spr", produces = "application/json;charset=UTF-8")
    public Map<String, Object> portInfoSync(@RequestBody String request){
        String retMsg = portInfoSyncService.protInfoSync(request);
        JSONObject jasonObject = JSONObject.parseObject(retMsg);
        Map<String, Object> retMap = (Map<String, Object>) jasonObject;
        return retMap;
    }

    /**
     * 业务配置下发结果通知接口
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/configReply.spr", produces = "application/json;charset=UTF-8")
    public Map<String, Object> configReply(@RequestBody String request){
        return cloudNetConfigReplyService.configReply(request);
    }

    /**
     * 全程业务测试反馈接口
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/wholeTestReply.spr", produces = "application/json;charset=UTF-8")
    public Map<String, Object> wholeTestReply(@RequestBody String request){
        return cloudNetWholeTestReplyService.wholeTestReply(request);
    }

    /**
     * IPRAN业务路由配置下发结果反馈接口
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/ipranRouteReply.spr", produces = "application/json;charset=UTF-8")
    public Map<String, Object> ipranRouteReply(@RequestBody String request){
        return cloudNetIpranRouteReplyService.ipranRouteReply(request);
    }

    /**
     * IPRAN端口限速配置下发结果反馈接口
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/ipranPortReply.spr", produces = "application/json;charset=UTF-8")
    public Map<String, Object> ipranPortReply(@RequestBody String request){
        return cloudNetIpranPortReplyService.ipranPortReply(request);
    }

    /**
     * IPRan自动激活结果反馈接口
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/ipranActiveReply.spr", produces = "application/json;charset=UTF-8")
    public Map<String, Object> ipranActiveReply(@RequestBody String request){
        return cloudNetIpranActiveReplyService.ipranActiveReply(request);
    }

    /**
     * IPRan自动激活结果反馈接口
     * @param request
     * @return
     */
    @IgnoreSession
    @PostMapping(value="/interfaceBDW/ipranInactiveReply.spr", produces = "application/json;charset=UTF-8")
    public Map<String, Object> ipranInActiveReply(@RequestBody String request){
        return cloudNetIpranInactiveReply.ipranInactiveReply(request);
    }
}