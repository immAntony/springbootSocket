package com.socket.example.socket.util;

import com.alibaba.fastjson.JSON;
import com.socket.example.socket.bean.Message;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * @Author: antony
 * @Date: 2019-06-14 14:46
 * @description:
 */
public class ServerEncoder implements Encoder.Text<Message> {
    @Override
    public String encode(Message message) throws EncodeException {
            return JSON.toJSONString(message);


    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
