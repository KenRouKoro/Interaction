package cn.korostudio.interaction.inject;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface SpringJpaRepository {
}
