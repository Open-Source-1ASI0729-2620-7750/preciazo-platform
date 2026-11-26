package com.preciazo.platform.iam.domain.model.aggregates;

import com.preciazo.platform.iam.domain.model.entities.Role;
import com.preciazo.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import org.jspecify.annotations.Nullable;
import java.time.Instant;
import java.util.*;

/** User aggregate root for IAM, independent of JPA and Spring Security. */
public final class User extends AbstractDomainAggregateRoot<User> {
    private @Nullable Long id;
    private final String email;
    private final String fullName;
    private final String passwordHash;
    private final Set<Role> roles;
    private final Instant createdAt;

    private User(@Nullable Long id, String email, String fullName, String passwordHash, Set<Role> roles, Instant createdAt) {
        if (email == null || email.isBlank() || !email.contains("@") || email.length() > 160) {
            throw new IllegalArgumentException("Valid email is required");
        }
        if (fullName == null || fullName.isBlank() || fullName.length() > 160) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash is required");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("At least one role is required");
        }
        this.id = id;
        this.email = email.strip().toLowerCase(Locale.ROOT);
        this.fullName = fullName.strip();
        this.passwordHash = passwordHash;
        this.roles = Collections.unmodifiableSet(new LinkedHashSet<>(roles));
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static User create(String email, String fullName, String passwordHash, Collection<Role> roles, Instant createdAt) {
        return new User(null, email, fullName, passwordHash, new LinkedHashSet<>(roles), createdAt);
    }

    public static User restore(long id, String email, String fullName, String passwordHash, Set<Role> roles, Instant createdAt) {
        return new User(id, email, fullName, passwordHash, roles, createdAt);
    }

    public @Nullable Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPasswordHash() { return passwordHash; }
    public Set<Role> getRoles() { return roles; }
    public Instant getCreatedAt() { return createdAt; }
    public List<String> getRoleNames() { return roles.stream().map(Role::getStringName).sorted().toList(); }
}
