package com.preciazo.platform.iam.infrastructure.authorization.sfs;

import com.preciazo.platform.iam.domain.model.aggregates.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Objects;

public final class UserDetailsImpl implements UserDetails {
    private final long id;
    private final String email;
    private final String fullName;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserDetailsImpl(long id, String email, String fullName, String password,
                            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        var authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getStringName()))
                .toList();
        return new UserDetailsImpl(Objects.requireNonNull(user.getId()), user.getEmail(),
                user.getFullName(), user.getPasswordHash(), authorities);
    }

    public long getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return Long.toString(id); }
}
