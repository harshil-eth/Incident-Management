//package com.bb.Incident.mgmt.service;
//
//import com.bb.Incident.mgmt.entity.Tenant;
//import com.bb.Incident.mgmt.exception.*;
//import com.bb.Incident.mgmt.repository.TenantRepository;
//import com.bb.Incident.mgmt.request.UpdateTenantRequest;
//import com.bb.Incident.mgmt.response.TenantResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.springframework.dao.DataAccessException;
//import org.springframework.data.domain.*;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
//public class TenantServiceTest {
//
//    @InjectMocks
//    private TenantService tenantService;
//
//    @Mock
//    private TenantRepository tenantRepository;
//
//    @Mock
//    private BCryptPasswordEncoder bCryptPasswordEncoder;
//
//    @Mock
//    private IncidentService incidentService;
//
//    private Tenant tenant;
//
//    private TenantResponse tenantResponse;
//
//    @BeforeEach
//    public void setUp() {
//        tenant = new Tenant();
//        tenant.setId(1L);
//        tenant.setUuid(UUID.randomUUID().toString());
//        tenant.setName("Test Tenant");
//        tenant.setDescription("Test Description");
//        tenant.setParentSocTenantId("parentSocTenantId");
//        tenant.setUsername("testUsername");
//        tenant.setPassword("testPassword");
//        tenant.setRoles("testRoles");
//
//        tenantResponse = new TenantResponse();
//        tenantResponse.setUuid(tenant.getUuid());
//        tenantResponse.setName(tenant.getName());
//        tenantResponse.setDescription(tenant.getDescription());
//        tenantResponse.setParentSocTenantId(tenant.getParentSocTenantId());
//        tenantResponse.setUsername(tenant.getUsername());
//        tenantResponse.setRoles(tenant.getRoles());
//    }
//
//    @Test
//    public void testGetAllTenants() {
//        Pageable pageable = PageRequest.of(0, 5);
//        Page<Tenant> tenantPage = new PageImpl<>(Collections.singletonList(tenant), pageable, 1);
//
//        when(tenantRepository.findAll(pageable)).thenReturn(tenantPage);
//
//        Page<TenantResponse> result = tenantService.getAllTenants(pageable);
//
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        assertEquals(tenant.getUuid(), result.getContent().get(0).getUuid());
//    }
//
//    @Test
//    public void testGetTenantByUuid() {
//        when(tenantRepository.findByUuid(tenant.getUuid())).thenReturn(tenant);
//
//        TenantResponse result = tenantService.getTenantByUuid(tenant.getUuid());
//
//        assertNotNull(result);
//        assertEquals(tenant.getUuid(), result.getUuid());
//    }
//
//    @Test
//    public void testCreateTenant() {
//        when(tenantRepository.existsByUsername(tenant.getUsername())).thenReturn(false);
//        when(tenantRepository.findByUuid(tenant.getUuid())).thenReturn(null);
//        when(bCryptPasswordEncoder.encode(tenant.getPassword())).thenReturn("encodedPassword");
//        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
//
//        TenantResponse result = tenantService.createTenant(tenant);
//
//        assertNotNull(result);
//        assertEquals(tenant.getUuid(), result.getUuid());
//    }
//
//    @Test
//    public void testUpdateTenant() {
//        UpdateTenantRequest updateTenantRequest = new UpdateTenantRequest();
//        updateTenantRequest.setName("Updated Name");
//        updateTenantRequest.setDescription("Updated Description");
//        updateTenantRequest.setUsername("updatedUsername");
//        updateTenantRequest.setRoles("updatedRoles");
//
//        when(tenantRepository.findByUuid(tenant.getUuid())).thenReturn(tenant);
//        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
//
//        TenantResponse result = tenantService.updateTenant(tenant.getUuid(), updateTenantRequest);
//
//        assertNotNull(result);
//        assertEquals("Updated Name", result.getName());
//        assertEquals("Updated Description", result.getDescription());
//        assertEquals("updatedUsername", result.getUsername());
//        assertEquals("updatedRoles", result.getRoles());
//    }
//
//    @Test
//    public void testDeleteTenant() {
//        when(tenantRepository.findByUuid(tenant.getUuid())).thenReturn(tenant);
//        when(incidentService.hasOpenIncidents(tenant.getUuid())).thenReturn(false);
//
//        tenantService.deleteTenant(tenant.getUuid());
//
//        verify(tenantRepository, times(1)).deleteByUuid(tenant.getUuid());
//    }
//
//    @Test
//    public void testGetTenantRolesByUsername() {
//        when(tenantRepository.findByUsername(tenant.getUsername())).thenReturn(tenant);
//
//        String roles = tenantService.getTenantRolesByUsername(tenant.getUsername());
//
//        assertNotNull(roles);
//        assertEquals(tenant.getRoles(), roles);
//    }
//
//    @Test
//    public void testVerifyPassword() {
//        when(bCryptPasswordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);
//
//        boolean result = tenantService.verifyPassword("rawPassword", "encodedPassword");
//
//        assertTrue(result);
//    }
//}
