package co.com.fuentech.fuentechserver.utils;

import co.com.fuentech.fuentechserver.security.entities.Role;
import co.com.fuentech.fuentechserver.security.roles.Roles;
import co.com.fuentech.fuentechserver.security.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CreateRoles implements CommandLineRunner {
    @Autowired
    RoleService rolService;

    @Override
    public void run(String... args) throws Exception {
        Role rolAdmin = new Role(Roles.ROLE_ADMIN);
        Role rolMode = new Role(Roles.ROLE_MODERATOR);
        Role rolUser = new Role(Roles.ROLE_USER);
        rolService.saveRole(rolAdmin);
        rolService.saveRole(rolMode);
        rolService.saveRole(rolUser);
    }
}
