package duzhicheng.spring.mode.annotation;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

@Documented
@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {

}
