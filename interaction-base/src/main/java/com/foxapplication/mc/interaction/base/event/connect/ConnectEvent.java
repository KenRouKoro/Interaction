package com.foxapplication.mc.interaction.base.event.connect;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 连接事件类
 */
@Data
@AllArgsConstructor
public class ConnectEvent {
    /**
     * 连接事件的ID
     */
    String id;

    /**
     * 连接状态
     */
    ConnectStatus status;
}
