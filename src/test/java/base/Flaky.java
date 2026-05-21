package base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import listeners.RetryAnalyzer;

/**
 * Annotation to mark tests as flaky and enable automatic retries
 * 
 * Usage: @Flaky(maxRetries = 2)
 *        public void testMethod() { }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Flaky {
    int maxRetries() default 2;
    String description() default "Flaky test - prone to intermittent failures";
}
