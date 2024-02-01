package com.foxapplication.mc.interaction.base.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 服务器类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Server implements Serializable {

    /**
     * 序列化版本号
     */
    @Serial
    private static final long serialVersionUID = 1145141919810L;

    /**
     * 服务器ID
     */
    String id;

    /**
     * 服务器地址
     */
    String address;

    /**
     * 是否使用SSL
     */
    boolean useSSL = false;

    /**
     * 服务器端口
     * -1表示不使用
     */
    int port = -1;

    /**
     * 服务列表
     */
    List<String> service = new CopyOnWriteArrayList<>();

}
