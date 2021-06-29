package co.com.fuentech.fuentechserver.security.repositories;

import co.com.fuentech.fuentechserver.security.entities.Role;
import co.com.fuentech.fuentechserver.security.roles.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(Roles role);
}
