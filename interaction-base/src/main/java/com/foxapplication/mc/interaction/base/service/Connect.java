package com.foxapplication.mc.interaction.base.service;

/**
 * 连接接口
 */
public interface Connect {
    /**
     * 发送消息
     *
     * @param data 消息数据
     */
    void sendMessage(byte[] data);

    /**
     * 关闭连接
     */
    void close();
}
