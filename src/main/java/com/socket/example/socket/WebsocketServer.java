package com.socket.example.socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.socket.example.socket.bean.Message;
import com.socket.example.socket.util.ServerEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Author: antony
 * @Date: 2019-06-13 17:58
 * @description:
 */
@Slf4j
@Component
@ServerEndpoint(value = "/socket/{roomName}/{userName}",encoders = {ServerEncoder.class})

public class WebsocketServer {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。 AZx
    private static int onlineCount = 0;


    /**
     * 使用map来收集session，key为roomName，value为同一个房间的用户集合
     * concurrentMap的key不存在时报错，不是返回null
     */
    private static final Map<String, Set<Session>> rooms = new ConcurrentHashMap();

    /**
     * 链接开启的方法
     */
    @OnOpen
    public void onOpen(@PathParam("roomName") String roomName, Session session, @PathParam("userName") String userName) throws IOException {
        // 将session按照房间名来存储，将各个房间的用户隔离
        if (!rooms.containsKey(roomName)) {

            log.info("【socket】添加信息");
            Set<Session> room = new HashSet<>();
            // 添加用户
            room.add(session);
            rooms.put(roomName, room);
            log.info("【socket】长度" + rooms.get(roomName).size());

        } else {
            // 房间已存在，直接添加用户到相应的房间
//            map.put(userName, map);
            // 添加用户
            log.info("【socket】添加信息");
            rooms.get(roomName).add(session);
            log.info("【socket】长度" + rooms.get(roomName).size());
        }
        Message msg = new Message();
        msg.setData("我上线啦！！");
        msg.setUserName(userName);
        sendMessage(roomName, userName, msg);
        System.out.println("a client has connected!");
    }

    /**
     * 功能描述: 链接关闭的方法
     *
     * @param
     * @return
     * @author mac
     * @date 2019-06-14 09:19
     */
    @OnClose
    public void onClose(@PathParam("roomName") String roomName, Session session) {
        rooms.get(roomName).remove(session);
        System.out.println("a client has disconnected!");
//        log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 功能描述: 受到客户端后调用的方法
     *
     * @param message 接收到的消息
     * @param session
     * @return java.lang.String
     * @author mac
     * @date 2019-06-14 09:33
     */
    @OnMessage
    public void OnMessage(@PathParam("roomName") String roomName, @PathParam("userName") String userName, String message, Session session) {
        // 此处应该有html过滤
        Message msg = new Message();
        msg.setData(message);
        msg.setUserName(userName);
        System.out.println(message);
        // 接收到信息后进行广播
        try {
            sendMessage(roomName, userName, msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void OnError(Session session, Throwable error) {
        log.error("错误发生了");
        error.printStackTrace();

    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String roomName, String userName, Message message) throws IOException {
        for (Session session : rooms.get(roomName)) {
//            session.getBasicRemote().sendText(JSON.toJSONString(message));
            try {
                session.getBasicRemote().sendObject(message);
            } catch (EncodeException e) {
                e.printStackTrace();
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
