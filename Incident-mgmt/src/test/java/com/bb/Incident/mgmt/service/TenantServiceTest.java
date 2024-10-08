package com.bb.Incident.mgmt.service;

import com.bb.Incident.mgmt.entity.Tenant;
import com.bb.Incident.mgmt.exception.*;
import com.bb.Incident.mgmt.repository.TenantRepository;
import com.bb.Incident.mgmt.request.UpdateTenantRequest;
import com.bb.Incident.mgmt.response.TenantResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class TenantServiceTest {

    @InjectMocks
    private TenantService tenantService;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private IncidentService incidentService;

    private Tenant tenant;

    private TenantResponse tenantResponse;

    @BeforeEach
    public void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setUuid(UUID.randomUUID().toString());
        tenant.setName("Test Tenant");
        tenant.setDescription("Test Description");
        tenant.setParentSocTenantId("parentSocTenantId");
        tenant.setUsername("testUsername");
        tenant.setPassword("testPassword");
        tenant.setRoles("testRoles");

        tenantResponse = new TenantResponse();
        tenantResponse.setUuid(tenant.getUuid());
        tenantResponse.setName(tenant.getName());
        tenantResponse.setDescription(tenant.getDescription());
        tenantResponse.setParentSocTenantId(tenant.getParentSocTenantId());
        tenantResponse.setUsername(tenant.getUsername());
        tenantResponse.setRoles(tenant.getRoles());
    }

    @Test
    public void testGetAllTenants() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Tenant> tenantPage = new PageImpl<>(Collections.singletonList(tenant), pageable, 1);

        when(tenantRepository.findAll(pageable)).thenReturn(tenantPage);

        Page<TenantResponse> result = tenantService.getAllTenants(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(tenant.getUuid(), result.getContent().get(0).getUuid());
    }

    @Test
    public void testGetAllTenants_DatabaseAccessException() {
        Pageable pageable = PageRequest.of(0, 5);

        when(tenantRepository.findAll(pageable)).thenThrow(new DataAccessException("Database access error") {});

        Exception exception = assertThrows(DatabaseConnectionException.class, () -> {
            tenantService.getAllTenants(pageable);
        });

        assertEquals("Failed to connect to the database.", exception.getMessage());
    }

    @Test
    public void testGetTenantByUuid() {
        when(tenantRepository.findByUuid(tenant.getUuid())).thenReturn(tenant);

        TenantResponse result = tenantService.getTenantByUuid(tenant.getUuid());

        assertNotNull(result);
        assertEquals(tenant.getUuid(), result.getUuid());
    }

    @Test
    public void testGetTenantByUuid_NotFound() {
        when(tenantRepository.findByUuid(tenant.getUuid())).thenReturn(null);

        Exception exception = assertThrows(TenantNotFoundException.class, () -> {
            tenantService.getTenantByUuid(tenant.getUuid());
        });

        assertEquals("Tenant not found with UUID: " + tenant.getUuid(), exception.getMessage());
    }

    @Test
    public void testCreateTenant() {
        when(tenantRepository.existsByUsername(tenant.getUsername())).thenReturn(false);
        when(tenantRepository.findByUuid(tenant.getUuid())).thenReturn(null);
        when(bCryptPasswordEncoder.encode(tenant.getPassword())).thenReturn("encodedPassword");
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        TenantResponse result = tenantService.createTenant(tenant);

        assertNotNull(result);
        assertEquals(tenant.getUuid(), result.getUuid());
    }

    @Test
    public void testCreateTenantWithExistingUsername() {
        when(tenantRepository.existsByUsername(tenant.getUsername())).thenReturn(true);

        Exception exception = assertThrows(TenantAlreadyExistsException.class, () -> {
            tenantService.createTenant(tenant);
        });

        assertEquals("Tenant already exists with username: " + tenant.getUsername(), exception.getMessage());
    }

    @Test
    public void testCreateTenantWithExistingUuid() {
        when(tenantRepository.findByUuid(tenant.getUuid())).thenReturn(tenant);

        Exception exception = assertThrows(TenantAlreadyExistsException.class, () -> {
            tenantService.createTenant(tenant);
        });

        assertEquals("Tenant already exists with UUID: " + tenant.getUuid(), exception.getMessage());
    }

    @Test
    public void testDeleteSocTenant() {
        tenant.setRoles("incident.get incident.create incident.update incident.delete");
        when(tenantRepository.findByUuid(tenant.getUuid())).thenReturn(tenant);

        Exception exception = assertThrows(SocTenantDeletionException.class, () -> {
            tenantService.deleteTenant(tenant.getUuid());
        });

        assertEquals("Cannot delete a SOC Tenant.", exception.getMessage());
    }

    @Test
    public void testDeleteTenantWithOpenIncidents() {
        when(tenantRepository.findByUuid(tenant.getUuid())).thenReturn(tenant);
        when(incidentService.hasOpenIncidents(tenant.getUuid())).thenReturn(true);

        Exception exception = assertThrows(OpenIncidentsException.class, () -> {
            tenantService.deleteTenant(tenant.getUuid());
        });

        assertEquals("Cannot delete a Tenant with open incidents", exception.getMessage());
    }

    @Test
    public void testDeleteNonExistentTenant() {
        when(tenantRepository.findByUuid(tenant.getUuid())).thenReturn(null);

        Exception exception = assertThrows(TenantNotFoundException.class, () -> {
            tenantService.deleteTenant(tenant.getUuid());
        });

        assertEquals("Tenant not found with UUID: " + tenant.getUuid(), exception.getMessage());
    }

    @Test
    public void testUpdateNonExistentTenant() {
        when(tenantRepository.findByUuid(tenant.getUuid())).thenReturn(null);

        UpdateTenantRequest updateTenantRequest = new UpdateTenantRequest();
        updateTenantRequest.setName("Updated Name");

        Exception exception = assertThrows(TenantNotFoundException.class, () -> {
            tenantService.updateTenant(tenant.getUuid(), updateTenantRequest);
        });

        assertEquals("Tenant not found with UUID: " + tenant.getUuid(), exception.getMessage());
    }

    @Test
    public void testCreateNullTenant() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            tenantService.createTenant(null);
        });

        assertEquals("Tenant can not be null.", exception.getMessage());
    }

    @Test
    public void testUpdateTenantWithNullRequest() {
//        when(tenantRepository.findByUuid(tenant.getUuid())).thenReturn(tenant);

        Mockito.lenient().when(tenantRepository.findByUuid(tenant.getUuid())).thenReturn(tenant);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            tenantService.updateTenant(tenant.getUuid(), null);
        });

        assertTrue(exception.getMessage().contains("Update Tenant Request can not be null."));
    }

    @Test
    public void testUpdateTenant() {
        UpdateTenantRequest updateTenantRequest = new UpdateTenantRequest();
        updateTenantRequest.setName("Updated Name");
        updateTenantRequest.setDescription("Updated Description");
        updateTenantRequest.setUsername("updatedUsername");
        updateTenantRequest.setRoles("updatedRoles");

        when(tenantRepository.findByUuid(tenant.getUuid())).thenReturn(tenant);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        TenantResponse result = tenantService.updateTenant(tenant.getUuid(), updateTenantRequest);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("updatedUsername", result.getUsername());
        assertEquals("updatedRoles", result.getRoles());
    }

    @Test
    public void testDeleteTenant() {
        when(tenantRepository.findByUuid(tenant.getUuid())).thenReturn(tenant);
        when(incidentService.hasOpenIncidents(tenant.getUuid())).thenReturn(false);

        tenantService.deleteTenant(tenant.getUuid());

        verify(tenantRepository, times(1)).deleteByUuid(tenant.getUuid());
    }

    @Test
    public void testGetTenantRolesByUsername() {
        when(tenantRepository.findByUsername(tenant.getUsername())).thenReturn(tenant);

        String roles = tenantService.getTenantRolesByUsername(tenant.getUsername());

        assertNotNull(roles);
        assertEquals(tenant.getRoles(), roles);
    }

    @Test
    public void testVerifyPassword() {
        when(bCryptPasswordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

        boolean result = tenantService.verifyPassword("rawPassword", "encodedPassword");

        assertTrue(result);
    }

    @Test
    public void testGetTenantRolesByUsername_WithExistingUsername() {
        String username = "testuser";
        Tenant tenant = new Tenant();
        tenant.setRoles("role1, role2");

        when(tenantRepository.findByUsername(username)).thenReturn(tenant);

        String roles = tenantService.getTenantRolesByUsername(username);

        assertEquals("role1, role2", roles);
    }

    @Test
    public void testGetTenantRolesByUsername_WithNonExistingUsername() {
        String username = "nonexistentuser";

        when(tenantRepository.findByUsername(username)).thenReturn(null);

        String roles = tenantService.getTenantRolesByUsername(username);

        assertNull(roles);
    }

    @Test
    public void testDeleteTenant_WithValidUuid() {
        String uuid = "valid-uuid";
        Tenant tenant = new Tenant();
        tenant.setUuid(uuid);

        when(tenantRepository.findByUuid(uuid)).thenReturn(tenant);
        when(incidentService.hasOpenIncidents(uuid)).thenReturn(false);

        tenantService.deleteTenant(uuid);

        verify(tenantRepository).deleteByUuid(uuid);
    }

    @Test
    public void testDeleteTenant_WithSocTenant() {
        String uuid = "soc-tenant-uuid";
        Tenant tenant = new Tenant();
        tenant.setUuid(uuid);
        tenant.setRoles("incident.get incident.create incident.update incident.delete");

        when(tenantRepository.findByUuid(uuid)).thenReturn(tenant);

        Exception exception = assertThrows(SocTenantDeletionException.class, () -> {
            tenantService.deleteTenant(uuid);
        });

        assertEquals("Cannot delete a SOC Tenant.", exception.getMessage());
    }

    @Test
    public void testDeleteTenant_WithOpenIncidents() {
        String uuid = "tenant-with-open-incidents";
        Tenant tenant = new Tenant();
        tenant.setUuid(uuid);

        when(tenantRepository.findByUuid(uuid)).thenReturn(tenant);
        when(incidentService.hasOpenIncidents(uuid)).thenReturn(true);

        Exception exception = assertThrows(OpenIncidentsException.class, () -> {
            tenantService.deleteTenant(uuid);
        });

        assertEquals("Cannot delete a Tenant with open incidents", exception.getMessage());
    }

    @Test
    public void testDeleteTenant_WithNonExistentUuid() {
        String uuid = "non-existent-uuid";

        when(tenantRepository.findByUuid(uuid)).thenReturn(null);

        Exception exception = assertThrows(TenantNotFoundException.class, () -> {
            tenantService.deleteTenant(uuid);
        });

        assertEquals("Tenant not found with UUID: " + uuid, exception.getMessage());
    }

    @Test
    public void testUpdateTenant_WithNullRequest() {
        String uuid = "tenant-uuid";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            tenantService.updateTenant(uuid, null);
        });

        assertEquals("Update Tenant Request can not be null.", exception.getMessage());
    }

    @Test
    public void testUpdateTenant_WithNonExistentTenant() {
        String uuid = "non-existent-uuid";
        UpdateTenantRequest updateRequest = new UpdateTenantRequest();
        updateRequest.setName("New Name");

        when(tenantRepository.findByUuid(uuid)).thenReturn(null);

        Exception exception = assertThrows(TenantNotFoundException.class, () -> {
            tenantService.updateTenant(uuid, updateRequest);
        });

        assertEquals("Tenant not found with UUID: " + uuid, exception.getMessage());
    }

    @Test
    public void testIsSocTenant() throws Exception {
        // Given a tenant with SOC roles
        Tenant socTenant = new Tenant();
        socTenant.setRoles("incident.get incident.create incident.update incident.delete");

        // Given a tenant with different roles
        Tenant nonSocTenant = new Tenant();
        nonSocTenant.setRoles("incident.get incident.create");

        // Use reflection to access the private method
        Method isSocTenantMethod = TenantService.class.getDeclaredMethod("isSocTenant", Tenant.class);
        isSocTenantMethod.setAccessible(true); // Make the private method accessible

        // When calling the method with a SOC tenant
        boolean result1 = (boolean) isSocTenantMethod.invoke(tenantService, socTenant);
        assertTrue(result1); // Should return true

        // When calling the method with a non-SOC tenant
        boolean result2 = (boolean) isSocTenantMethod.invoke(tenantService, nonSocTenant);
        assertFalse(result2); // Should return false
    }

    @Test
    public void testHasOpenIncidents() throws Exception {
        // Given a tenant
        Tenant tenant = new Tenant();
        tenant.setUuid("tenant-uuid");

        // Mock the incident service's behavior
        when(incidentService.hasOpenIncidents(tenant.getUuid())).thenReturn(true);

        // Use reflection to access the private method
        Method hasOpenIncidentsMethod = TenantService.class.getDeclaredMethod("hasOpenIncidents", Tenant.class);
        hasOpenIncidentsMethod.setAccessible(true); // Make the private method accessible

        // When calling the method
        boolean result = (boolean) hasOpenIncidentsMethod.invoke(tenantService, tenant);
        assertTrue(result); // Should return true for open incidents

        // Now test the case where there are no open incidents
        when(incidentService.hasOpenIncidents(tenant.getUuid())).thenReturn(false);
        result = (boolean) hasOpenIncidentsMethod.invoke(tenantService, tenant);
        assertFalse(result); // Should return false for no open incidents
    }

    @Test
    public void testUpdateTenant_Success() {
        // Given an existing tenant
        Tenant existingTenant = new Tenant();
        existingTenant.setUuid("tenant-uuid");
        existingTenant.setName("Old Name");
        existingTenant.setDescription("Old Description");
        existingTenant.setUsername("oldUsername");
        existingTenant.setRoles("oldRoles");

        // Mock the behavior of tenantRepository
        when(tenantRepository.findByUuid(existingTenant.getUuid())).thenReturn(existingTenant);
        when(tenantRepository.save(existingTenant)).thenReturn(existingTenant); // Simulate save

        // Create an UpdateTenantRequest with new values
        UpdateTenantRequest updateTenantRequest = new UpdateTenantRequest();
        updateTenantRequest.setName("New Name");
        updateTenantRequest.setDescription("New Description");
        updateTenantRequest.setUsername("newUsername");
        updateTenantRequest.setRoles("newRoles");

        // When updating the tenant
        TenantResponse response = tenantService.updateTenant(existingTenant.getUuid(), updateTenantRequest);

        // Then the response should reflect the updated values
        assertEquals("New Name", existingTenant.getName());
        assertEquals("New Description", existingTenant.getDescription());
        assertEquals("newUsername", existingTenant.getUsername());
        assertEquals("newRoles", existingTenant.getRoles());
        assertNotNull(response);
    }

    @Test
    public void testUpdateTenant_NullRequest() {
        // Given an existing tenant
        String uuid = "tenant-uuid";

        // Expecting an IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            tenantService.updateTenant(uuid, null);
        });
    }

    @Test
    public void testUpdateTenant_TenantNotFound() {
        // Given a UUID that does not exist
        String uuid = "non-existent-uuid";
        UpdateTenantRequest updateTenantRequest = new UpdateTenantRequest();

        // Mock the behavior of tenantRepository
        when(tenantRepository.findByUuid(uuid)).thenReturn(null);

        // Expecting a TenantNotFoundException
        assertThrows(TenantNotFoundException.class, () -> {
            tenantService.updateTenant(uuid, updateTenantRequest);
        });
    }

}
