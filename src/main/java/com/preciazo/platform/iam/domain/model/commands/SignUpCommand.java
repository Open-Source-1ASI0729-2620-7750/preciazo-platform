package com.preciazo.platform.iam.domain.model.commands;

import java.util.List;

public record SignUpCommand(String email, String fullName, String password, List<String> roles) { }
