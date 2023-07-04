package annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface WebSocketController {

    String url() default "/";

    long period() default 1000;

    TimeUnit unit() default TimeUnit.MILLISECONDS;

    boolean sendNullMsg() default false;

}
