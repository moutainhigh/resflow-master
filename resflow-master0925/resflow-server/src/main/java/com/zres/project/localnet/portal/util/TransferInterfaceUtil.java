package com.zres.project.localnet.portal.util;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferInterfaceUtil {
    private static Logger logger = LoggerFactory.getLogger(TransferInterfaceUtil.class);
    public static Object sendXmlClient(String sendXml, String endpoint, String namespace, String method) { //调用接口方法sendXml是发送报文，endpoint调取的wsdl地址，namespace调取接口命名空间，method调取的方法名
        Service service = new Service();
        Call call;
        Object ret = new Object();
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(new java.net.URL(endpoint)); //wsdl地址
            QName qName = new QName(namespace, method); //命名空间，method是方法名
            call.setOperation(method);
            call.setOperationName(qName);
            ret = (String) call.invoke(new Object[]{sendXml}); //sendXml发送报文，ret返回报文
        }
        catch (Exception e) {
            logger.debug("调用接口失败" + e);
        }
        return ret;
    }
}

