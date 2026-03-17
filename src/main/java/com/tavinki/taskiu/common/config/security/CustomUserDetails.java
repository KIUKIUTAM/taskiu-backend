package com.tavinki.taskiu.common.config.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.tavinki.taskiu.common.enums.role.SystemRole;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomUserDetails implements UserDetails {

    private String id;
    private String name;
    private String email;
    private String picture;
    private String password;
    private SystemRole role;
    private boolean verified;
    private boolean banned;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonLocked() { return !banned; }

    @Override
    public boolean isEnabled() { return verified; }

}
