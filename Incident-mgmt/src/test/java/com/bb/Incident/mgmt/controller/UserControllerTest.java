package com.bb.Incident.mgmt.controller;

import com.bb.Incident.mgmt.entity.User;
import com.bb.Incident.mgmt.request.UpdateUserRequest;
import com.bb.Incident.mgmt.response.UserResponse;
import com.bb.Incident.mgmt.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testGetAllUsers() throws Exception {
        UserResponse userResponse = new UserResponse();
        userResponse.setUuid("123");
        userResponse.setUsername("testuser");

        List<UserResponse> users = Collections.singletonList(userResponse);
        Page<UserResponse> page = new PageImpl<>(users);

        Pageable pageable = PageRequest.of(0, 5); // Create a specific Pageable instance

        when(userService.getAllUsers(pageable)).thenReturn(page);

        mockMvc.perform(get("/v1/users")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].uuid").value("123"))
                .andExpect(jsonPath("$.users[0].username").value("testuser"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(userService, times(1)).getAllUsers(pageable);
    }

    @Test
    void testGetUserById() throws Exception {
        UserResponse userResponse = new UserResponse();
        userResponse.setUuid("123");
        userResponse.setUsername("testuser");

        when(userService.getUserById(anyString())).thenReturn(userResponse);

        mockMvc.perform(get("/v1/users/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value("123"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService, times(1)).getUserById(anyString());
    }

    @Test
    void testCreateUser() throws Exception {
        User user = new User();
        user.setUuid("123");
        user.setUsername("newuser");

        UserResponse userResponse = new UserResponse();
        userResponse.setUuid("123");
        userResponse.setUsername("newuser");

        when(userService.createUser(any(User.class))).thenReturn(userResponse);

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"123\", \"username\":\"newuser\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value("123"))
                .andExpect(jsonPath("$.username").value("newuser"));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    void testUpdateUser() throws Exception {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setUsername("updateduser");

        UserResponse userResponse = new UserResponse();
        userResponse.setUuid("123");
        userResponse.setUsername("updateduser");

        when(userService.updateUser(anyString(), any(UpdateUserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(put("/v1/users/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"updateduser\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value("123"))
                .andExpect(jsonPath("$.username").value("updateduser"));

        verify(userService, times(1)).updateUser(anyString(), any(UpdateUserRequest.class));
    }

    @Test
    void testDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(anyString());

        mockMvc.perform(delete("/v1/users/123"))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(anyString());
    }
}
