package com.zres.project.localnet.portal.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jiangdebing on 2020/3/23.
 */
public class HandyTool {
    /**
     * 判断字符是否是中文
     *
     * @param c
     *            字符
     * @return 是否是中文
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * 判断字符串是否是乱码
     *
     * @param strName
     *            字符串
     * @return 是否是乱码
     */
    public static boolean isMessyCode(String strName) {
        Pattern p = Pattern.compile("\\s*|t*|r*|n*");
        Matcher m = p.matcher(strName);
        String after = m.replaceAll("");
        String temp = after.replaceAll("\\p{P}", "");
        char[] ch = temp.trim().toCharArray();
        float chLength = ch.length;
        float count = 0;
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (!Character.isLetterOrDigit(c)) {
                if (!isChinese(c)) {
                    count = count + 1;
                }
            }
        }
        float result = count / chLength;
        if (result > 0.4 || strName.indexOf("??") != -1
                || strName.indexOf("？？") != -1) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 判断字符串是否为纯数字`
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        if(str==null||"".equals(str)||"null".equals(str)){
            return false;
        }else{
            Pattern pattern = Pattern.compile("[0-9]*");
            return pattern.matcher(str).matches();
        }
    }

    /**
     * 判空方法
     * @param judge
     * @return
     */
    public static Boolean judgeNULL(Object judge){
        if(judge==null){
            return false;
        }else if(judge instanceof String){
            if ("".equals(judge.toString())){
                return false;
            }else if ("null".equals(judge.toString())){
                return false;
            }else if ("NULL".equals(judge.toString())){
                return false;
            }
        }else if(judge instanceof Map){
            if(((Map)judge).size()==0){
                return false;
            }
        }else if(judge instanceof List){
            if(((List)judge).size()==0){
                return false;
            }
        }
        return true;
    }

    /**
     * 拼接SQL方法
     * @param condition
     * @param splicingSql
     * @param originalSql
     */
    public static void splicingSql(Object condition,String splicingSql,StringBuffer originalSql){
        if(HandyTool.judgeNULL(condition)){
            originalSql.append(" "+splicingSql);
        }
    }

    /**
     * 字符串以逗号分隔去重转成LISTl类型
     * @param convertData
     * @return
     */
    public static List convertList(Object convertData){
        StringBuffer compare = new StringBuffer("start");
        if(HandyTool.judgeNULL(convertData)){
            String[] res = convertData.toString().split(",");
            List<String> returnList = new ArrayList<String>();
            for(int s=0; s<res.length; s++){
                if(compare.toString().indexOf(res[s])==-1){
                    compare.append("("+res[s]+")");
                    returnList.add(res[s]);
                }
            }
            compare.append("end");
            return returnList;
        }else{
            return null;
        }
    }
    /**
     * 获取服务名称
     */
    public static String GetServiceName(){
//	  String nodeName=null;
//        String serverName=null;
//	  String name=null;
        String ip=null;
        String str=System.getProperty("sun.java.command");
        if(str!=null){
            String[] s=str.split("\\s+");
//	   nodeName=s[s.length-2];
//            serverName=s[s.length-1];
        }else{
//	   nodeName="defaultNodeName";
//            serverName="defaultServerName";
        }
//			logger.info("defaultNodeName:" + nodeName);
//			logger.info("defaultServerName:" + serverName);
        try {
//			 name = InetAddress.getLocalHost().getHostName();
            ip = InetAddress.getLocalHost().getHostAddress();
//			 logger.info("计算机名：" + name);
//			 logger.info("IP地址：" + ip);
        }catch (UnknownHostException e)
        {
            System.out.println("异常：" + e);
            e.printStackTrace();
        }
        return ip;
    }

    /**
     * 正则表达式
     * @param checkParameter
     * @param expression
     * @return
     */
    public static boolean regularExpression(String checkParameter,String expression){
        if(judgeNULL(checkParameter)&&judgeNULL(expression)){
            Pattern p = Pattern.compile(expression);
            Matcher m = p.matcher(checkParameter);
            return m.matches();
        }
        return false;
    }
}
