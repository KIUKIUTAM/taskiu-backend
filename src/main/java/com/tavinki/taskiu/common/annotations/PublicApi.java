package com.tavinki.taskiu.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark public endpoints that do not require login verification (JWT)
 */
@Target({ ElementType.METHOD, ElementType.TYPE }) // 1. Allowed on Methods and Classes
@Retention(RetentionPolicy.RUNTIME) // 2. Important! Must be RUNTIME for interceptors to read via reflection
public @interface PublicApi {
}
