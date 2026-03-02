package com.tavinki.taskiu.common.annotations.taskpermission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RequirePermission(value = {})  // 會被 Aspect 特殊處理
public @interface HasPermission {
    String value();
}
