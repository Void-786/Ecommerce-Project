package com.faiz.ecommerce.service;

import com.faiz.ecommerce.exception.RoleAlreadyExistsException;
import com.faiz.ecommerce.exception.UserAlreadyExistsException;
import com.faiz.ecommerce.model.Role;
import com.faiz.ecommerce.model.User;
import com.faiz.ecommerce.repository.RoleRepository;
import com.faiz.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;

    public List<Role> getRoles() {
        return roleRepo.findAll();
    }

    public Role createRole(Role theRole) {
        String roleName = "ROLE_" + theRole.getName().toUpperCase();
        Role role = new Role(roleName);
        if(roleRepo.existsByName(roleName)) {
            throw new RoleAlreadyExistsException(theRole.getName() + " role already exists");
        }
        return roleRepo.save(role);
    }

    public void deleteRole(Long roleId) {
        this.removeAllUsersFromRole(roleId);
        roleRepo.deleteById(roleId);
    }

    public Role findByName(String name) {
        return roleRepo.findByName(name).get();
    }

    public User removeUserFromRole(Long userId, long roleId) {
        Optional<User> user = userRepo.findById(userId);
        Optional<Role> role = roleRepo.findById(roleId);

        if(role.isPresent() && role.get().getUsers().contains(user.get())) {
            role.get().removeUsersFromRole(user.get());
            roleRepo.save(role.get());
            return user.get();
        }
        throw new UsernameNotFoundException("User not found");
    }

    public User assignRoleToUser(Long userId, Long roleId) {
        Optional<User> user = userRepo.findById(userId);
        Optional<Role> role = roleRepo.findById(roleId);

        if(user.isPresent() && role.get().getUsers().contains(user.get())) {
            throw new UserAlreadyExistsException(user.get().getFirstName() + " is already assigned to role " + role.get().getName()+ " role");
        }
        if(role.isPresent()) {
            role.get().assignRoleToUser(user.get());
            roleRepo.save(role.get());
        }
        return user.get();
    }

    public Role removeAllUsersFromRole(Long roleId) {
        Optional<Role> role = roleRepo.findById(roleId);
        role.ifPresent(Role::removeAllUsersFromRole);
        return roleRepo.save(role.get());
    }
}
