package com.foxapplication.mc.interaction.base.config;

import com.foxapplication.mc.core.config.interfaces.FieldAnnotation;
import com.foxapplication.mc.core.config.interfaces.FileType;
import com.foxapplication.mc.core.config.interfaces.FileTypeInterface;
import com.foxapplication.mc.interaction.base.data.Server;
import lombok.Data;

@Data
@FileTypeInterface(type = FileType.TOML)
public class InteractionBaseConfig {
    @FieldAnnotation(name = "中央节点ID",value = "中央节点ID,不过这个值不会验证就是了。")
    public String centerID = "Center";
    @FieldAnnotation(name = "中央节点IP",value = "中央节点IP地址。")
    public String centerAddress = "127.0.0.1";
    @FieldAnnotation(name = "中央节点端口",value = "-1为不使用端口。")
    public int centerPort = 8080;
    @FieldAnnotation(name = "中央节点是否使用SSL",value = "启用的话会使用WSS链接。")
    public boolean centerUseSSL = false;
    @FieldAnnotation(name = "是否启用中央节点",value = "关闭的话在初始化时不会链接中央节点。")
    public boolean enableCenter = true;
    @FieldAnnotation(name = "网络Token",value = "当前网络的Token，别整花里胡哨的。")
    public String ConnectToken = "HelloWorld";
    @FieldAnnotation(name = "当前节点是否使用其它框架提供服务",value = "保持false就行了，不要乱动！不然不会启动内置的WebSocketServer。")
    public boolean isOtherServer = false;
}
