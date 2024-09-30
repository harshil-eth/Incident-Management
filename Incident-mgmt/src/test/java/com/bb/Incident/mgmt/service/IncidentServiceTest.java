package com.bb.Incident.mgmt.service;

import com.bb.Incident.mgmt.entity.Incident;
import com.bb.Incident.mgmt.entity.Tenant;
import com.bb.Incident.mgmt.entity.User;
import com.bb.Incident.mgmt.exception.*;
import com.bb.Incident.mgmt.repository.IncidentRepository;
import com.bb.Incident.mgmt.repository.TenantRepository;
import com.bb.Incident.mgmt.repository.UserRepository;
import com.bb.Incident.mgmt.request.UpdateIncidentRequest;
import com.bb.Incident.mgmt.response.IncidentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IncidentServiceTest {

    @InjectMocks
    private IncidentService incidentService;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<Incident> criteriaQuery;

    @Mock
    private Root<Incident> root;

    @Mock
    private TypedQuery<Incident> typedQuery;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllIncidents() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Incident> incidents = new ArrayList<>();
        incidents.add(new Incident());
        Page<Incident> incidentPage = new PageImpl<>(incidents, pageable, incidents.size());

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Incident.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Incident.class)).thenReturn(root);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);

        // Ensure the TypedQuery returns itself when setFirstResult and setMaxResults are called
        when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(incidents);

        Page<IncidentResponse> result = incidentService.getAllIncidents(pageable, null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }


    @Test
    public void testGetIncidentByUuid() {
        String uuid = "test-uuid";
        Incident incident = new Incident();
        incident.setUuid(uuid);

        when(incidentRepository.findByUuid(uuid)).thenReturn(incident);

        IncidentResponse response = incidentService.getIncidentByUuid(uuid);

        assertNotNull(response);
        assertEquals(uuid, response.getUuid());
    }

//    @Test
//    public void testCreateIncident() {
//        // Create Tenant and User objects
//        Tenant tenant = new Tenant();
//        tenant.setUuid("tenant-uuid");
//        tenant.setName("Tenant Name");
//        tenant.setDescription("Tenant Description");
//        tenant.setParentSocTenantId("parent-soc-tenant-id");
//        tenant.setUsername("tenant-username");
//        tenant.setPassword("tenant-password");
//
//        User user = new User();
//        user.setUuid("user-uuid");
//        user.setUsername("user-username");
//        user.setEmail("user@example.com");
//        user.setFirstName("First");
//        user.setLastName("Last");
//        user.setTenant(tenant);
//
//        // Mock the repository methods
//        when(tenantRepository.findByUuid("tenant-uuid")).thenReturn(tenant);
//        when(userRepository.findByUuid("user-uuid")).thenReturn(user);
//        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // Create Incident object
//        Incident incident = new Incident();
//        incident.setIncidentType("Network Issue");
//        incident.setDescription("Internet connectivity is down");
//        incident.setSeverity("High");
//        incident.setState("Open");
//        incident.setDateReported(LocalDateTime.now());
//        incident.setDevice("Router");
//        incident.setLocation("Office");
//        incident.setPriority("P1");
//        incident.setSha256("some-sha256-hash");
//
//        // Set the Tenant and User objects
//        incident.setReportedByTenant(tenant);
//        incident.setAssignedToUser(user);
//
//        // Call the createIncident method
//        Incident createdIncident = incidentService.createIncident(incident);
//
//        // Assertions
//        assertNotNull(createdIncident);
//        assertNotNull(createdIncident.getId());
//        assertEquals("Network Issue", createdIncident.getIncidentType());
//        assertEquals("Internet connectivity is down", createdIncident.getDescription());
//        assertEquals("High", createdIncident.getSeverity());
//        assertEquals("Open", createdIncident.getState());
//        assertEquals("Router", createdIncident.getDevice());
//        assertEquals("Office", createdIncident.getLocation());
//        assertEquals("P1", createdIncident.getPriority());
//        assertEquals("some-sha256-hash", createdIncident.getSha256());
//        assertEquals("tenant-uuid", createdIncident.getReportedByTenant().getUuid());
//        assertEquals("user-uuid", createdIncident.getAssignedToUser().getUuid());
//    }


    @Test
    public void testUpdateIncident() {
        String uuid = "test-uuid";
        Incident incident = new Incident();
        incident.setUuid(uuid);

        UpdateIncidentRequest request = new UpdateIncidentRequest();
        request.setDescription("Updated description");

        when(incidentRepository.findByUuid(uuid)).thenReturn(incident);
        when(incidentRepository.save(any(Incident.class))).thenReturn(incident);

        IncidentResponse response = incidentService.updateIncident(uuid, request);

        assertNotNull(response);
        assertEquals("Updated description", response.getDescription());
    }

    @Test
    public void testDeleteIncident() {
        String uuid = "test-uuid";
        Incident incident = new Incident();
        incident.setUuid(uuid);
        incident.setState("Closed");

        when(incidentRepository.findByUuid(uuid)).thenReturn(incident);

        incidentService.deleteIncident(uuid);

        verify(incidentRepository, times(1)).delete(incident);
    }

    @Test
    public void testResolveIncident() {
        String uuid = "test-uuid";
        Incident incident = new Incident();
        incident.setUuid(uuid);
        incident.setState("Open");

        when(incidentRepository.findByUuid(uuid)).thenReturn(incident);
        when(incidentRepository.save(any(Incident.class))).thenReturn(incident);

        IncidentResponse response = incidentService.resolveIncident(uuid);

        assertNotNull(response);
        assertEquals("Close", response.getState());
    }

    @Test
    public void testAssignUser() {
        String uuid = "test-uuid";
        Incident incident = new Incident();
        incident.setUuid(uuid);

        User user = new User();
        user.setUuid("user-uuid");

        when(incidentRepository.findByUuid(uuid)).thenReturn(incident);
        when(userService.getRandomUser()).thenReturn("user-uuid");
        when(userRepository.findByUuid("user-uuid")).thenReturn(user);
        when(incidentRepository.save(any(Incident.class))).thenReturn(incident);

        IncidentResponse response = incidentService.assignUser(uuid);

        assertNotNull(response);
        assertEquals("user-uuid", response.getAssignedToUserId());
    }
}

