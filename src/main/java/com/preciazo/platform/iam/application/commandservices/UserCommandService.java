package com.preciazo.platform.iam.application.commandservices;

import com.preciazo.platform.iam.domain.model.aggregates.User;
import com.preciazo.platform.iam.domain.model.commands.SignInCommand;
import com.preciazo.platform.iam.domain.model.commands.SignUpCommand;
import com.preciazo.platform.shared.application.result.ApplicationError;
import com.preciazo.platform.shared.application.result.Result;

public interface UserCommandService {
    record AuthenticatedUser(User user, String token) { }
    Result<User, ApplicationError> handle(SignUpCommand command);
    Result<AuthenticatedUser, ApplicationError> handle(SignInCommand command);
}
