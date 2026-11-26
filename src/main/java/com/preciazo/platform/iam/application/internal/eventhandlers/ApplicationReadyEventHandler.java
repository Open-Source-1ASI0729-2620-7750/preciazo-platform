package com.preciazo.platform.iam.application.internal.eventhandlers;

import com.preciazo.platform.iam.application.commandservices.RoleCommandService;
import com.preciazo.platform.iam.application.commandservices.UserCommandService;
import com.preciazo.platform.iam.domain.model.commands.SeedRolesCommand;
import com.preciazo.platform.iam.domain.model.commands.SignUpCommand;
import com.preciazo.platform.iam.domain.repositories.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import java.util.List;

/** Seeds default roles and demo test users for Buyer and Merchant segments on startup. */
@Service
public class ApplicationReadyEventHandler {
    private final RoleCommandService roleCommandService;
    private final UserCommandService userCommandService;
    private final UserRepository userRepository;

    public ApplicationReadyEventHandler(RoleCommandService roleCommandService,
                                        UserCommandService userCommandService,
                                        UserRepository userRepository) {
        this.roleCommandService = roleCommandService;
        this.userCommandService = userCommandService;
        this.userRepository = userRepository;
    }

    @Order(1)
    @EventListener(ApplicationReadyEvent.class)
    public void on(ApplicationReadyEvent event) {
        roleCommandService.handle(new SeedRolesCommand());
        seedUserIfAbsent("carlos.mendoza@preciazo.pe", "Carlos Mendoza", "Preciazo2026!", List.of("ROLE_BUYER"));
        seedUserIfAbsent("alberto.ramos@preciazo.pe", "Alberto Ramos", "Preciazo2026!", List.of("ROLE_MERCHANT"));
        seedUserIfAbsent("admin@preciazo.pe", "PeruTech Admin", "Preciazo2026!", List.of("ROLE_ADMIN", "ROLE_BUYER", "ROLE_MERCHANT"));
    }

    private void seedUserIfAbsent(String email, String fullName, String password, List<String> roles) {
        if (!userRepository.existsByEmail(email)) {
            userCommandService.handle(new SignUpCommand(email, fullName, password, roles));
        }
    }
}
