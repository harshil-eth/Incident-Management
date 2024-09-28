package com.bb.Incident.mgmt.service;

import com.bb.Incident.mgmt.entity.Tenant;
import com.bb.Incident.mgmt.entity.User;
import com.bb.Incident.mgmt.exception.DatabaseConnectionException;
import com.bb.Incident.mgmt.exception.UserAlreadyExistsException;
import com.bb.Incident.mgmt.exception.UserNotFoundException;
import com.bb.Incident.mgmt.repository.TenantRepository;
import com.bb.Incident.mgmt.repository.UserRepository;
import com.bb.Incident.mgmt.request.UpdateUserRequest;
import com.bb.Incident.mgmt.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

//    public List<UserResponse> getAllUsers() {
//        try {
//            List<User> users = userRepository.findAll();
//            return users.stream().map(this::convertToResponse).collect(Collectors.toList());
//        } catch (DataAccessException ex) {
//            throw new DatabaseConnectionException("Failed to connect to the database.");
//        }
//    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        try {
            Page<User> users = userRepository.findAll(pageable);
            return users.map(this::convertToResponse);
        } catch (DataAccessException ex) {
            throw new DatabaseConnectionException("Failed to connect to the database.");
        }
    }

    public UserResponse getUserById(String uuid) {
        User user = userRepository.findByUuid(uuid);
        if(user == null) {
            throw new UserNotFoundException("User not found with UUID: " + uuid);
        }
        return convertToResponse(user);
    }

    public UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();

        response.setUuid(user.getUuid());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());

        return response;
    }

    @Transactional
    public UserResponse createUser(User user) {
        if(user.getTenant() == null || user.getTenant().getUuid() == null) {
            throw new IllegalArgumentException("Tenant information is missing");
        }

        Tenant tenant = tenantRepository.findByUuid(user.getTenant().getUuid());
        if(tenant == null) {
            throw new IllegalArgumentException("Invalid tenant UUID");
        }

        if(userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("User already exists with the username: " + user.getUsername());
        }

        user.setTenant(tenant);
        User savedUser = userRepository.save(user);

        return convertToResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(String uuid, UpdateUserRequest updateUserRequest) {
        User existingUser = userRepository.findByUuid(uuid);

        if (existingUser == null) {
            throw new UserNotFoundException("User not found with UUID: " + uuid);
        }

        if (existingUser.getUsername() != null) {
            existingUser.setUsername(updateUserRequest.getUsername());
        }
        if (existingUser.getEmail() != null) {
            existingUser.setEmail(updateUserRequest.getEmail());
        }
        if (existingUser.getFirstName() != null) {
            existingUser.setFirstName(updateUserRequest.getFirstName());
        }
        if (existingUser.getLastName() != null) {
            existingUser.setLastName(updateUserRequest.getLastName());
        }

        return convertToResponse(userRepository.save(existingUser));
    }

    @Transactional
    public void deleteUser(String uuid) {

        User user = userRepository.findByUuid(uuid);
        if(user == null) {
            throw new UserNotFoundException("User not found with UUID " + uuid);
        }

        userRepository.deleteByUuid(uuid);
    }
}
