package com.foxapplication.mc.interaction.base.event.connect;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConnectEvent {
    String id;
    ConnectStatus status;
}
