package cn.korostudio.interaction.service.web;

import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import cn.korostudio.interaction.base.BaseClient;
import cn.korostudio.interaction.base.data.Server;
import cn.korostudio.interaction.base.service.PlatformConnect;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.core.handle.Result;

import java.util.ArrayList;

@Controller()
public class HttpBaseService {
    @Get
    @Mapping( "/get")
    public String getServer(@Param("id")String id){
        return JSONUtil.toJsonStr(PlatformConnect.getServerMap().get(id));
    }
    @Get
    @Mapping( "/list")
    public String getServerList(){
        ArrayList<Server> serverList = new ArrayList<>(PlatformConnect.getServerMap().values());
        return JSONUtil.toJsonStr(serverList);
    }
}
