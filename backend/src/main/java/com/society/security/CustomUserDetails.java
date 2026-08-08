package com.society.security;

import com.society.entity.Society;
import com.society.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {
    private final Long   userId;
    private final String email;
    private final String password;
    private final String role;
    /** Null only for ROLE_SUPER_ADMIN, which is not scoped to any society. */
    private final Long   societyId;
    private final boolean userActive;
    private final boolean societyActive;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.userId     = user.getUserId();
        this.email      = user.getEmail();
        this.password   = user.getPassword();
        this.role       = user.getRole().getRoleName();
        this.userActive = Boolean.TRUE.equals(user.getActive());

        Society society = user.getSociety();
        this.societyId     = society != null ? society.getSocietyId() : null;
        // SUPER_ADMIN has no society, so it is always considered "active" here.
        this.societyActive = society == null || society.getStatus() == Society.Status.ACTIVE;

        this.authorities = List.of(new SimpleGrantedAuthority(role));
    }

    @Override public String getUsername()  { return email; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return userActive; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    /** A disabled user, or a user whose society is not ACTIVE, cannot authenticate. */
    @Override public boolean isEnabled()               { return userActive && societyActive; }
}
