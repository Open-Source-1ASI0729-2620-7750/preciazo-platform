package com.preciazo.platform.iam.infrastructure.authorization.sfs;

import com.preciazo.platform.iam.domain.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository users;
    public UserDetailsServiceImpl(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = users.findByEmail(email.strip().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return UserDetailsImpl.build(user);
    }
}
