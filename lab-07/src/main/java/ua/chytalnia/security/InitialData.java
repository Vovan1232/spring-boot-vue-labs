package ua.chytalnia.security;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class InitialData implements ApplicationRunner {
    private final RoleRepository roles;
    private final UserRepository users;
    private final UserService userService;
    private final Environment environment;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String name : List.of(Role.USER, Role.ADMIN, Role.MANAGER)) {
            roles.findByName(name).orElseGet(() -> roles.save(new Role(name)));
        }
        if (environment.acceptsProfiles(Profiles.of("demo"))) {
            demoUser("demo_user", Role.USER);
            demoUser("demo_admin", Role.ADMIN);
            demoUser("demo_manager", Role.MANAGER);
        }
    }

    private void demoUser(String username, String role) {
        if (!users.existsByUsername(username)) {
            userService.save(username, "DemoLab7!", roles.findByName(role).orElseThrow());
        }
    }
}
