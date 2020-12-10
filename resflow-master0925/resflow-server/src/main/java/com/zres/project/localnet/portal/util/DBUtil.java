package com.zres.project.localnet.portal.util;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DBUtil {
    private static Connection con;
    private static String url;
    private static String user;
    private static String pwd;
    private static Properties prop;
    public DBUtil() {

    }
    static {
        try {
            InputStream is = DBUtil.class.getResourceAsStream("/application.properties");
            prop=new Properties();
            prop.load(is);
            Class.forName(prop.getProperty("ftf.datasource.druid.driver-class-name"));
            url=prop.getProperty("ftf.datasource.druid.url");
            user=prop.getProperty("ftf.datasource.druid.username");
            pwd=prop.getProperty("ftf.datasource.druid.password");
            con = DriverManager.getConnection(url, user, pwd);
        }catch (Exception e){
        }
    }
    public static ResultSet find(String sql){
        con=getCon();
        try {
            Statement smt=con.createStatement();
            ResultSet rs=smt.executeQuery(sql);
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static ResultSet find(String sql,Object ...pram){//...pram数组
        con=getCon();
        try {
            PreparedStatement smt=con.prepareStatement(sql);
            for (int i=0;i<pram.length;i++){
                smt.setObject(i+1,pram[i]);
            }
            ResultSet rs=smt.executeQuery();
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void insert(String sql,Object ...pram){//...pram数组
        con=getCon();
        try {
            PreparedStatement smt=con.prepareStatement(sql);
            for (int i=0;i<pram.length;i++){
                smt.setObject(i+1,pram[i]);
            }
            smt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static Connection getCon(){
        try {
            if(con==null||con.isClosed())
                con = DriverManager.getConnection(url, user, pwd);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }
    public static void close(){
        try {
            if(con !=null)con.close();
        } catch (Exception e) {
            e.getMessage();
        }
    }
}