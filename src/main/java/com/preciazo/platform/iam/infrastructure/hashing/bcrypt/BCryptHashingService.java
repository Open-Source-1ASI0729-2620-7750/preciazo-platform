package com.preciazo.platform.iam.infrastructure.hashing.bcrypt;

import com.preciazo.platform.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BCryptHashingService implements HashingService, PasswordEncoder {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override public String encode(CharSequence rawPassword) { return encoder.encode(rawPassword); }
    @Override public boolean matches(CharSequence rawPassword, String encodedPassword) { return encoder.matches(rawPassword, encodedPassword); }
}
