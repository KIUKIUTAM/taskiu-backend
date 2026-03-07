// com/tavinki/taskiu/common/aspect/RequireTeamRoleAspect.java
package com.tavinki.taskiu.common.aspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.tavinki.taskiu.common.annotations.attribute.TeamId;
import com.tavinki.taskiu.common.annotations.role.RequireTeamRole;
import com.tavinki.taskiu.modules.user.dto.UserResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RequireTeamRoleAspect {



    @Before("@annotation(requireTeamRole)")
    public void checkTeamRole(JoinPoint joinPoint, RequireTeamRole requireTeamRole) {

        // ── 1. Retrieve the currently authenticated user ──────────────────────
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        String teamPublicId = extractTeamId(joinPoint);

        if (teamPublicId == null) {
            log.error("[RequireTeamRoleAspect] Could not find @TeamId parameter in method: {}",
                    joinPoint.getSignature().getName());
            throw new IllegalArgumentException(
                    "Unable to resolve teamId. Make sure one method parameter is annotated with @TeamId");
        }


    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scans method parameters via reflection to find the one annotated
    // with @TeamId, then returns its runtime value from the join point args.
    // parameterAnnotations[i] and args[i] share the same index,
    // so once @TeamId is found at index i, args[i] is the actual teamId value.
    // ─────────────────────────────────────────────────────────────────────────
    private String extractTeamId(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 2D array: parameterAnnotations[i] = all annotations on the i-th parameter
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        // Actual runtime values of each parameter, aligned by index with parameterAnnotations
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof TeamId) {
                    Object arg = args[i];
                    if (arg == null) {
                        throw new IllegalArgumentException(
                                "Parameter annotated with @TeamId must not be null");
                    }
                    return arg.toString();
                }
            }
        }

        // No parameter annotated with @TeamId was found
        return null;
    }

    private String extractUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserResponseDto user) {
            if (user.getId() == null || user.getId().isBlank()) {
                throw new AccessDeniedException("User id is missing from auth principal");
            }
            return user.getId();
        }

        String name = authentication.getName();
        if (name == null || name.isBlank()) {
            throw new AccessDeniedException("User identity is missing");
        }
        return name;
    }
}
