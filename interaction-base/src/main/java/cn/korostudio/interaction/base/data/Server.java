package cn.korostudio.interaction.base.data;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class Server implements Serializable {

    @Serial
    private static final long serialVersionUID = 1145141919810L;

    String id;
    String address;
    boolean useSSL = false;
    int port;//-1为不使用
    List<String> service = new CopyOnWriteArrayList<>();


}
