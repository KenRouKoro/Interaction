package cn.korostudio.interaction.base.data;

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

    public BaseMessage() {
    }

    public BaseMessage(String service) {
        this.service = service;
    }

    public BaseMessage(String service, byte[] data) {
        this(service);
        this.message = data;
    }

    public BaseMessage(String service, String target, byte[] data) {
        this(service);
        this.target = target;
        this.message = data;
    }

    public BaseMessage(String service, String target) {
        this(service);
        this.target = target;
    }

    public BaseMessage(String service, String target, String message) {
        this(service, target, message.getBytes(CharsetUtil.CHARSET_UTF_8));
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public void setMessage(Object message) {
        setMessage(ObjectUtil.serialize(message));
    }

    public Object getMessageByObject() {
        return ObjectUtil.deserialize(message);
    }

    public void setMessage(String message) {
        setMessage(message.getBytes(CharsetUtil.CHARSET_UTF_8));
    }

    public String getMessageByString() {
        return new String(this.message, CharsetUtil.CHARSET_UTF_8);
    }

}
