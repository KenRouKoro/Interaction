package cn.korostudio.interaction;

import cn.korostudio.interaction.base.BaseClient;
import cn.korostudio.interaction.base.config.Config;
import cn.korostudio.interaction.config.AppConfig;
import cn.korostudio.interaction.config.PersistenceJPAConfig;
import cn.korostudio.interaction.inject.SpringJpaRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.annotation.*;
import org.noear.solon.web.cors.CrossFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;


@SolonMain
@Controller
@Slf4j
@ComponentScan(basePackageClasses = {AppConfig.class, PersistenceJPAConfig.class})
public class App {

    @Getter
    @SuppressWarnings("resource")
    private static ApplicationContext context;

    public static void main(String[] args) {
        context = new AnnotationConfigApplicationContext(App.class);//优先载入SpringDataJPA
        Solon.start(App.class, args,app->{
            app.filter(-1, new CrossFilter().allowedOrigins("*")); //加-1 优先级更高
            app.enableWebSocket(true);
            app.enableWebSocketMvc(false);
            app.enableWebSocketD(false);
            Solon.context().beanInjectorAdd(SpringJpaRepository.class,((fwT, anno) -> {
                fwT.required(false);
                Class<?> type= fwT.getType();
                if (type==null){
                    throw new NoClassDefFoundError("注入不存在的类?这不应该,你是怎么通过编译的?");
                }
                if (!JpaRepository.class.isAssignableFrom(type)){
                    throw new UnsupportedOperationException("请不要错误的使用SpringJpaRepository注解");
                }
                fwT.setValue(context.getBean(type));
            }));
        });
        Config.isCenter=true;

        BaseClient.init(Config.centerServer);

    }
}

