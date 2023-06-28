package cn.korostudio.interaction.service.web;

import cn.hutool.json.JSONUtil;
import cn.korostudio.interaction.base.data.Server;
import cn.korostudio.interaction.base.service.PlatformConnect;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Param;

import java.util.ArrayList;

@Controller()
public class HttpBaseService {
    @Get
    @Mapping("/get")
    public String getServer(@Param("id") String id) {
        return JSONUtil.toJsonStr(PlatformConnect.getServerMap().get(id));
    }

    @Get
    @Mapping("/list")
    public String getServerList() {
        ArrayList<Server> serverList = new ArrayList<>(PlatformConnect.getServerMap().values());
        return JSONUtil.toJsonStr(serverList);
    }
}
