package co.com.fuentech.fuentechserver.security.services;

import co.com.fuentech.fuentechserver.security.entities.Role;
import co.com.fuentech.fuentechserver.security.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    public Role saveRole(Role role){
        return roleRepository.save(role);
    }
}
