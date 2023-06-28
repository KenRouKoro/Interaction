package cn.korostudio.interaction.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = {"cn.korostudio.interaction"})
@EnableJpaRepositories(basePackages = {"cn.korostudio.interaction.data"})
public class AppConfig {
}