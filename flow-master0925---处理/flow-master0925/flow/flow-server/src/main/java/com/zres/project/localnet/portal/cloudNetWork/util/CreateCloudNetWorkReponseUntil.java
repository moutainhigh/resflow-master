package com.zres.project.localnet.portal.cloudNetWork.util;

/**
 * 静态工具类
 *
 * @author caomm on 2020/10/14
 */
public class CreateCloudNetWorkReponseUntil {
    public static String createRespone(String InterfaceCode, String SuccessFlag, String respDesc){
        StringBuilder sb = new StringBuilder(500);
        sb.append("\t{\"UNI_BSS_BODY\": {");
        if ("FinishOrder".equals(InterfaceCode)){
            sb.append("\"FINISH_ORDER_RSP\": {");
        }else if("resAllocate".equals(InterfaceCode)){
            sb.append("\"PROVINCE_RES_SUBMIT_ORDER_RSP\": {");
        }else if ("FinishOrderForDIA".equals(InterfaceCode)){
            sb.append("\"PROVINCE_FINISH_ORDER_RSP\": {");
        }else if ("RentOrder".equals(InterfaceCode)){
            sb.append("\"YZWRENT_ORDER_RSP\": {");
        }else if ("ChangeOrder".equals(InterfaceCode)){
            sb.append("\"YZWCHANGE_ORDER_RSP\": {");
        }else if ("BackOrder".equals(InterfaceCode)){
            sb.append("\"BACK_ORDER_RSP\": {");
        }else if("QueryOrder".equals(InterfaceCode)){
            sb.append("\"QUERY_ORDER_RSP\": {");
        }else if ("FullRouteQuery".equals(InterfaceCode)){
            sb.append("\"FULLROUTE_API_RSP\":{");
        }else if ("ApplyOrder".equals(InterfaceCode)){
            sb.append("\"YZWAPPLY_ORDER_RSP\": {");
        }else{
            sb.append("\"").append(InterfaceCode).append("\": {");
        }
        sb.append("\"RESP_CODE\": \"").append(SuccessFlag).append("\",");
        sb.append("\"RESP_DESC\": \"").append(respDesc).append("\",");
        sb.append("\"PARA\":  {");
        sb.append("\"PARA_ID\": \"").append("").append("\",");
        sb.append("\"PARA_VALUE\": \"").append("").append("\"");
        sb.append("}");
        sb.append("}");
        sb.append("}");
        sb.append("}");
        return sb.toString();
    }
}