package com.zres.project.localnet.portal.until.service;

import java.io.IOException;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

/**
 * @author :ren.jiahang
 * @date:2019/8/11@time:16:40
 */
@ServerEndpoint("/WebSocketTest")
public class WebSoctetTest {
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
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=1;i<=100;i++){

                    try {
                        session.getBasicRemote().sendText("HN-2019-000"+i+"加载成功\n");
                        Thread.sleep(100);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        if(i==100){
                            try {
                                session.close();
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }

            }
        });
        //同步发送 发送第二条时，必须等第一条发送完成
        // this.session.getBasicRemote().sendText("haha");
        //异步发送
        //this.session.getAsyncRemote().sendText(message);
        thread.start();
    }
}
