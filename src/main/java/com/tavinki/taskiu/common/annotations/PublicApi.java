package com.tavinki.taskiu.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用於標記不需要登入驗證 (JWT) 的公開接口
 */
@Target({ ElementType.METHOD, ElementType.TYPE }) // 1. 允許用在「方法」和「類別」上
@Retention(RetentionPolicy.RUNTIME) // 2. 重要！必須是 RUNTIME，攔截器才能透過反射讀取到
public @interface PublicApi {
}
