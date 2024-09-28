package com.bb.Incident.mgmt.controller;

import com.bb.Incident.mgmt.entity.User;
import com.bb.Incident.mgmt.request.UpdateUserRequest;
import com.bb.Incident.mgmt.response.UserResponse;
import com.bb.Incident.mgmt.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Get all users", description = "Returns a list of all users")
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Get user with UUID", description = "Returns the user with UUID")
    @GetMapping("/{uuid}")
    public UserResponse getUserById(@PathVariable String uuid) {
        return userService.getUserById(uuid);
    }

    @Operation(summary = "Create User", description = "Creates a user")
    @PostMapping
    public UserResponse createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @Operation(summary = "Update the user with UUID", description = "Updates the user with UUID")
    @PutMapping("/{uuid}")
    public UserResponse updateUser(@PathVariable String uuid, @Valid @RequestBody UpdateUserRequest  updateUserRequest) {
        return userService.updateUser(uuid, updateUserRequest);
    }

    @Operation(summary = "Delete the user with UUID", description = "Deletes a user with UUID")
    @DeleteMapping("/{uuid}")
    public void deleteUser(@PathVariable String uuid) {
        userService.deleteUser(uuid);
    }
}
