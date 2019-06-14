package com.socket.example.socket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Author: antony
 * @Date: 2019-06-13 17:58
 * @description:
 */
@Slf4j
@Component
@ServerEndpoint("/socket/{sid}")
public class WebsocketServer {
    private Session session;
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<WebsocketServer> webSocketSet = new CopyOnWriteArraySet<WebsocketServer>();

    //接收sid
    private String sid="";
    /**
     * 链接开启的方法
     */
    @OnOpen
    public void onOpen(Session session,@PathParam("sid") String sid){
        this.session = session;
        //加入set中
        webSocketSet.add(this);
        addOnlineCount();           //在线数加1
        log.info("有新窗口开始监听:"+sid+",当前在线人数为" + getOnlineCount());
        this.sid=sid;
        try {
            sendMessage("连接成功");
        } catch (IOException e) {
            log.error("websocket IO异常");
        }
    }

    /**
     * 功能描述: 链接关闭的方法
     * @param
     * @return
     * @author mac
     * @date 2019-06-14 09:19
     */
    @OnClose
    public void onClose(){
        //从set中删除
        webSocketSet.remove(this);
        subOnlineCount();           //在线数减1
        log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 功能描述: 受到客户端后调用的方法
     * @param  message 接收到的消息
     * @param  session
     * @return java.lang.String
     * @author mac
     * @date 2019-06-14 09:33
     */
    @OnMessage
    public void OnMessage(String message,Session session){
        log.info("收到来自窗口"+sid+"的信息:"+message);
        //群发消息
        for (WebsocketServer item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void OnError(Session session, Throwable error){
        log.error("错误发生了");
        error.printStackTrace();

    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }


    /**
     * 群发自定义消息
     * */
    public static void sendInfo(String message,@PathParam("sid") String sid) throws IOException {
        log.info("推送消息到窗口"+sid+"，推送内容:"+message);
        for (WebsocketServer item : webSocketSet) {
            try {
                //这里可以设定只推送给这个sid的，为null则全部推送
                if(sid==null) {
                    item.sendMessage(message);
                }else if(item.sid.equals(sid)){
                    item.sendMessage(message);
                }
            } catch (IOException e) {
                continue;
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebsocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebsocketServer.onlineCount--;
    }




}
