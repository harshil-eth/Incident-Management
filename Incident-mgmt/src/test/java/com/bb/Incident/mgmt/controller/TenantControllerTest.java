package com.bb.Incident.mgmt.controller;

import com.bb.Incident.mgmt.entity.Tenant;
import com.bb.Incident.mgmt.request.UpdateTenantRequest;
import com.bb.Incident.mgmt.response.TenantResponse;
import com.bb.Incident.mgmt.service.TenantService;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TenantControllerTest {

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private TenantController tenantController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(tenantController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testGetAllTenants() throws Exception {
        TenantResponse tenantResponse = new TenantResponse();
        tenantResponse.setUuid("123");
        tenantResponse.setName("Test Tenant");

        List<TenantResponse> tenants = Collections.singletonList(tenantResponse);
        Page<TenantResponse> page = new PageImpl<>(tenants);

        Pageable pageable = PageRequest.of(0, 5); // Create a specific Pageable instance

        when(tenantService.getAllTenants(pageable)).thenReturn(page);

        mockMvc.perform(get("/v1/tenants")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenants[0].uuid").value("123"))
                .andExpect(jsonPath("$.tenants[0].name").value("Test Tenant"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(tenantService, times(1)).getAllTenants(pageable);
    }

    @Test
    void testGetTenantByUuid() throws Exception {
        TenantResponse tenantResponse = new TenantResponse();
        tenantResponse.setUuid("123");
        tenantResponse.setName("Test Tenant");

        when(tenantService.getTenantByUuid(anyString())).thenReturn(tenantResponse);

        mockMvc.perform(get("/v1/tenants/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value("123"))
                .andExpect(jsonPath("$.name").value("Test Tenant"));

        verify(tenantService, times(1)).getTenantByUuid(anyString());
    }

    @Test
    void testCreateTenant() throws Exception {
        Tenant tenant = new Tenant();
        tenant.setUuid("123");
        tenant.setName("New Tenant");

        TenantResponse tenantResponse = new TenantResponse();
        tenantResponse.setUuid("123");
        tenantResponse.setName("New Tenant");

        when(tenantService.createTenant(any(Tenant.class))).thenReturn(tenantResponse);

        mockMvc.perform(post("/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"123\", \"name\":\"New Tenant\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value("123"))
                .andExpect(jsonPath("$.name").value("New Tenant"));

        verify(tenantService, times(1)).createTenant(any(Tenant.class));
    }

    @Test
    void testUpdateTenant() throws Exception {
        UpdateTenantRequest updateTenantRequest = new UpdateTenantRequest();
        updateTenantRequest.setName("Updated Tenant");

        TenantResponse tenantResponse = new TenantResponse();
        tenantResponse.setUuid("123");
        tenantResponse.setName("Updated Tenant");

        when(tenantService.updateTenant(anyString(), any(UpdateTenantRequest.class))).thenReturn(tenantResponse);

        mockMvc.perform(put("/v1/tenants/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Tenant\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value("123"))
                .andExpect(jsonPath("$.name").value("Updated Tenant"));

        verify(tenantService, times(1)).updateTenant(anyString(), any(UpdateTenantRequest.class));
    }

    @Test
    void testDeleteTenant() throws Exception {
        doNothing().when(tenantService).deleteTenant(anyString());

        mockMvc.perform(delete("/v1/tenants/123"))
                .andExpect(status().isOk());

        verify(tenantService, times(1)).deleteTenant(anyString());
    }
}
