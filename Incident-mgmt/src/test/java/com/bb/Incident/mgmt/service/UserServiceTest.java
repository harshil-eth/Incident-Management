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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllUsers() {
        Pageable pageable = PageRequest.of(0, 5);
        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setFirstName("Test");
        user.setLastName("User");

        Page<User> userPage = new PageImpl<>(Collections.singletonList(user));
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserResponse> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    public void testGetUserById_UserExists() {
        String uuid = UUID.randomUUID().toString();
        User user = new User();
        user.setUuid(uuid);
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setFirstName("Test");
        user.setLastName("User");

        when(userRepository.findByUuid(uuid)).thenReturn(user);

        UserResponse result = userService.getUserById(uuid);

        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        verify(userRepository, times(1)).findByUuid(uuid);
    }

    @Test
    public void testGetUserById_UserNotExists() {
        String uuid = UUID.randomUUID().toString();
        when(userRepository.findByUuid(uuid)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(uuid));
        verify(userRepository, times(1)).findByUuid(uuid);
    }

    @Test
    public void testCreateUser_Success() {
        String tenantUuid = UUID.randomUUID().toString();
        Tenant tenant = new Tenant();
        tenant.setUuid(tenantUuid);

        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setTenant(tenant);

        when(tenantRepository.findByUuid(tenantUuid)).thenReturn(tenant);
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse result = userService.createUser(user);

        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        verify(tenantRepository, times(1)).findByUuid(tenantUuid);
        verify(userRepository, times(1)).existsByUsername(user.getUsername());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testCreateUser_UserAlreadyExists() {
        // Arrange
        Tenant tenant = new Tenant();
        tenant.setUuid("valid-tenant-uuid");

        User user = new User();
        user.setUsername("testuser");
        user.setTenant(tenant);

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(user));
    }


    @Test
    public void testUpdateUser_UserExists() {
        String uuid = UUID.randomUUID().toString();
        User existingUser = new User();
        existingUser.setUuid(uuid);
        existingUser.setUsername("testuser");
        existingUser.setEmail("testuser@example.com");
        existingUser.setFirstName("Test");
        existingUser.setLastName("User");

        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setUsername("updateduser");
        updateUserRequest.setEmail("updateduser@example.com");
        updateUserRequest.setFirstName("Updated");
        updateUserRequest.setLastName("User");

        when(userRepository.findByUuid(uuid)).thenReturn(existingUser);
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserResponse result = userService.updateUser(uuid, updateUserRequest);

        assertNotNull(result);
        assertEquals(updateUserRequest.getUsername(), result.getUsername());
        verify(userRepository, times(1)).findByUuid(uuid);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    public void testUpdateUser_UserNotExists() {
        String uuid = UUID.randomUUID().toString();
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();

        when(userRepository.findByUuid(uuid)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(uuid, updateUserRequest));
        verify(userRepository, times(1)).findByUuid(uuid);
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    public void testDeleteUser_UserExists() {
        String uuid = UUID.randomUUID().toString();
        User user = new User();
        user.setUuid(uuid);

        when(userRepository.findByUuid(uuid)).thenReturn(user);

        userService.deleteUser(uuid);

        verify(userRepository, times(1)).findByUuid(uuid);
        verify(userRepository, times(1)).deleteByUuid(uuid);
    }

    @Test
    public void testDeleteUser_UserNotExists() {
        String uuid = UUID.randomUUID().toString();

        when(userRepository.findByUuid(uuid)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(uuid));
        verify(userRepository, times(1)).findByUuid(uuid);
        verify(userRepository, times(0)).deleteByUuid(uuid);
    }

    @Test
    public void testDatabaseConnectionException() {
        Pageable pageable = PageRequest.of(0, 5);
        when(userRepository.findAll(pageable)).thenThrow(new DataAccessException("...") {});

        assertThrows(DatabaseConnectionException.class, () -> userService.getAllUsers(pageable));
        verify(userRepository, times(1)).findAll(pageable);
    }
}
