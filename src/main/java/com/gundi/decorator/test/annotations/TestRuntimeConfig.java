package com.gundi.decorator.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.logging.Level;

/**
 * Created by pai on 12.02.18.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestRuntimeConfig {
    String name();
    String applicationID() default "test_deco_app";
    String testRoot() default "test/cfg.test";
    boolean testOnContainer() default false;
    boolean rollbackAfterExcecution() default  false;
    String environment() default "DEV";
}
