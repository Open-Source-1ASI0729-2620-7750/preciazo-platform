package com.preciazo.platform.iam.application.commandservices;

import com.preciazo.platform.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {
    void handle(SeedRolesCommand command);
}
