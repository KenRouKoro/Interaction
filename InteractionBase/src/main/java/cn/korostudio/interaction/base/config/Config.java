package cn.korostudio.interaction.base.config;

import cn.korostudio.interaction.base.data.Server;
import lombok.Data;

@Data
public class Config {
    public static Server centerServer = new Server(){
        {
            setId("Center");
            setAddress("127.0.0.1");
            setPort(8080);
            setUseSSL(false);
        }
    };
    public static boolean enableCenter = true;
    public static String ConnectToken="HelloWorld";
    public static boolean isCenter = false;


}
