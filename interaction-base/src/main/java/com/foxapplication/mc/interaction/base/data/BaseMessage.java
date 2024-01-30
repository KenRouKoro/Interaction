package com.foxapplication.mc.interaction.base.data;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BaseMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1145141919810L;
    String service = "NONE";
    byte[] message = new byte[0];
    String form = "NONE";
    String target = "NONE";

    /**
     * 无参构造函数
     */
    public BaseMessage() {
    }

    /**
     * 构造函数
     *
     * @param service 服务名称
     */
    public BaseMessage(String service) {
        this.service = service;
    }

    /**
     * 构造函数
     *
     * @param service 服务名称
     * @param data    消息数据
     */
    public BaseMessage(String service, byte[] data) {
        this(service);
        this.message = data;
    }

    /**
     * 构造函数
     *
     * @param service 服务名称
     * @param target  目标名称
     * @param data    消息数据
     */
    public BaseMessage(String service, String target, byte[] data) {
        this(service);
        this.target = target;
        this.message = data;
    }

    /**
     * 构造函数
     *
     * @param service 服务名称
     * @param target  目标名称
     */
    public BaseMessage(String service, String target) {
        this(service);
        this.target = target;
    }

    /**
     * 构造函数
     *
     * @param service 服务名称
     * @param target  目标名称
     * @param message 消息内容
     */
    public BaseMessage(String service, String target, String message) {
        this(service, target, message.getBytes(CharsetUtil.CHARSET_UTF_8));
    }

    /**
     * 设置消息数据
     *
     * @param message 消息数据
     */
    public void setMessage(byte[] message) {
        this.message = message;
    }

    /**
     * 设置消息数据
     *
     * @param message 消息数据
     */
    public void setMessage(Object message) {
        setMessage(ObjectUtil.serialize(message));
    }

    /**
     * 获取消息数据
     *
     * @return 消息数据
     */
    public Object getMessageByObject() {
        return ObjectUtil.deserialize(message);
    }

    /**
     * 设置消息数据
     *
     * @param message 消息数据
     */
    public void setMessage(String message) {
        setMessage(message.getBytes(CharsetUtil.CHARSET_UTF_8));
    }

    /**
     * 获取消息数据
     *
     * @return 消息数据
     */
    public String getMessageByString() {
        return new String(this.message, CharsetUtil.CHARSET_UTF_8);
    }

}
