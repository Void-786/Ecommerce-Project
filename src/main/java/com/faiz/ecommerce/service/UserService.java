package com.faiz.ecommerce.service;

import com.faiz.ecommerce.exception.UserAlreadyExistsException;
import com.faiz.ecommerce.exception.UserNotFoundException;
import com.faiz.ecommerce.model.Role;
import com.faiz.ecommerce.model.User;
import com.faiz.ecommerce.repository.RoleRepository;
import com.faiz.ecommerce.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public void addUser(User user) {
        if(userRepo.existsUserByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException(user.getEmail() + " already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        System.out.println(user.getPassword());
        Role userRole = roleRepo.findByName("ROLE_USER").get();
        user.setRoles(Collections.singletonList(userRole));
        userRepo.save(user);
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserByEmail(String email) {
        return userRepo.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Transactional
    public void deleteUserByEmail(String email) {
        User theUser = getUserByEmail(email);
        if(theUser != null) {
            userRepo.delete(theUser);
        }
    }
}
