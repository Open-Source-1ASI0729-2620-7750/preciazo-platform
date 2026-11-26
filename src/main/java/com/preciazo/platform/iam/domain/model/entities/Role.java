package com.preciazo.platform.iam.domain.model.entities;

import com.preciazo.platform.iam.domain.model.valueobjects.Roles;
import org.jspecify.annotations.Nullable;
import java.util.Objects;

/** Role domain entity without JPA coupling. */
public final class Role {
    private final @Nullable Long id;
    private final Roles name;

    public Role(@Nullable Long id, Roles name) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Role name is required");
    }

    public static Role create(Roles name) {
        return new Role(null, name);
    }

    public static Role restore(long id, Roles name) {
        return new Role(id, name);
    }

    public @Nullable Long getId() { return id; }
    public Roles getName() { return name; }
    public String getStringName() { return name.name(); }

    @Override public boolean equals(Object o) {
        return this == o || (o instanceof Role other && name == other.name);
    }
    @Override public int hashCode() { return Objects.hash(name); }
}
