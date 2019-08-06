package duzhicheng.spring.mode.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
public @interface Controller {
	
	String Value() default "";

}
