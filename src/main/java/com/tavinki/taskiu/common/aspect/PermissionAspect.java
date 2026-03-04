package com.tavinki.taskiu.common.aspect;


import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.tavinki.taskiu.common.annotations.taskpermission.HasPermission;
import com.tavinki.taskiu.common.annotations.taskpermission.RequireAllPermissions;
import com.tavinki.taskiu.common.annotations.taskpermission.RequireAnyPermission;
import com.tavinki.taskiu.common.annotations.taskpermission.RequirePermission;
import com.tavinki.taskiu.common.enums.LogicalOperator;
import com.tavinki.taskiu.common.exceptions.UnauthorizedException;

import lombok.extern.slf4j.Slf4j;


@Aspect
@Component
@Slf4j
public class PermissionAspect {

    @Around("@annotation(com.tavinki.taskiu.common.annotation.RequirePermission) || " +
            "@annotation(com.tavinki.taskiu.common.annotation.HasPermission) || " +
            "@annotation(com.tavinki.taskiu.common.annotation.RequireAllPermissions) || " +
            "@annotation(com.tavinki.taskiu.common.annotation.RequireAnyPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        String[] requiredPermissions;
        LogicalOperator operator;
        
        // Prioritize HasPermission, as it is a simplified version of RequirePermission
        if (method.isAnnotationPresent(HasPermission.class)) {
            HasPermission annotation = method.getAnnotation(HasPermission.class);
            requiredPermissions = new String[]{annotation.value()};
            operator = LogicalOperator.OR;
            
        } else if (method.isAnnotationPresent(RequireAllPermissions.class)) {
            RequireAllPermissions annotation = method.getAnnotation(RequireAllPermissions.class);
            requiredPermissions = annotation.value();
            operator = LogicalOperator.AND;
            
        } else if (method.isAnnotationPresent(RequireAnyPermission.class)) {
            RequireAnyPermission annotation = method.getAnnotation(RequireAnyPermission.class);
            requiredPermissions = annotation.value();
            operator = LogicalOperator.OR;
            
        } else {
            RequirePermission annotation = method.getAnnotation(RequirePermission.class);
            requiredPermissions = annotation.value();
            operator = annotation.operator();
        }
        
        return performPermissionCheck(joinPoint, requiredPermissions, operator);
    }

    private Object performPermissionCheck(ProceedingJoinPoint joinPoint, String[] requiredPermissions, LogicalOperator operator) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        
        Set<String> userPermissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        
        boolean hasPermission;
        if (operator == LogicalOperator.AND) {
            hasPermission = Arrays.stream(requiredPermissions).allMatch(userPermissions::contains);
        } else {
            hasPermission = Arrays.stream(requiredPermissions).anyMatch(userPermissions::contains);
        }
        
        if (!hasPermission) {
            throw new UnauthorizedException("Insufficient permissions");
        }
        
        return joinPoint.proceed();
    }
}
