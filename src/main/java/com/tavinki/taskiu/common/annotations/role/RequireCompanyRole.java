package com.tavinki.taskiu.common.annotations.role;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.tavinki.taskiu.common.enums.role.CompanyRole;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireCompanyRole {
    CompanyRole[] value();
}
