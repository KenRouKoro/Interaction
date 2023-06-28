package cn.korostudio.interaction.service.web;

import cn.korostudio.interaction.data.KVData;
import cn.korostudio.interaction.data.jpa.KVDataRepository;
import cn.korostudio.interaction.inject.SpringJpaRepository;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Result;

import java.util.List;

@Mapping("/data")
@Controller()
//用来代替NoSQL，性能很差
public class DataService {
    @SpringJpaRepository
    private KVDataRepository dataRepository;

    @Get
    @Mapping("/get")
    public Result<String> get(@Param("key") String key) {
        KVData data = dataRepository.findByKVKey(key);
        if (data == null) {
            return Result.failure(501, "key不存在");
        }
        return Result.succeed(data.getKVData(), "ok");
    }

    @Get
    @Mapping("/getall")
    public Result<List<KVData>> getALL() {
        List<KVData> data = dataRepository.findAll();
        return Result.succeed(data, "ok");
    }

    @Post
    @Mapping("/put")
    public Result put(@Param("key") String key, @Param("data") String data) {
        if (key == null || data == null) {
            return Result.failure(501, "key或data为空");
        }
        KVData kvData = new KVData();
        kvData.setKVKey(key);
        kvData.setKVData(data);
        dataRepository.saveAndFlush(kvData);
        return Result.succeed(null, "ok");
    }

    @Delete
    @Mapping("/delete")
    public Result delete(@Param("key") String key) {
        dataRepository.deleteByKVKey(key);
        return Result.succeed();
    }

}
