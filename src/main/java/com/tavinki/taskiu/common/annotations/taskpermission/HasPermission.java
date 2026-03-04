package com.tavinki.taskiu.common.annotations.taskpermission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RequirePermission(value = {})  // Will be specially handled by Aspect
public @interface HasPermission {
    String value();
}
