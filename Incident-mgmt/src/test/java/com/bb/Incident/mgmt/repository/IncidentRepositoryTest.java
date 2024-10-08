package com.bb.Incident.mgmt.repository;

import com.bb.Incident.mgmt.entity.Incident;
import com.bb.Incident.mgmt.entity.Tenant;
import com.bb.Incident.mgmt.entity.User;
import com.bb.Incident.mgmt.entity.IncidentEnums; // Ensure you import the enums
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Prevent using embedded database
@ActiveProfiles("test") // Use the 'test' profile for PostgreSQL
public class IncidentRepositoryTest {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository; // Add UserRepository for user instance

    private Incident incident;
    private Tenant tenant;
    private User user; // To assign a user to the incident

    @BeforeEach
    public void setUp() {
        // Create a new Tenant instance
        tenant = new Tenant();
        tenant.setUuid("test-tenant-uuid");
        tenant.setName("Test Tenant");
        tenant.setParentSocTenantId("parent-soc-id");
        tenant.setUsername("tenantuser");
        tenant.setPassword("password123");
        tenant.setRoles("ROLE_USER");

        // Save the tenant
        tenant = tenantRepository.save(tenant);

        // Create a new User instance
        user = new User();
        user.setUuid("test-user-uuid");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setTenant(tenant); // Associate user with the tenant

        // Save the user
        user = userRepository.save(user);

        // Create a new Incident instance
        incident = new Incident();
        incident.setUuid("test-incident-uuid");
        incident.setIncidentType("Network Issue");
        incident.setDescription("Network is down in the office.");
        incident.setSeverity(IncidentEnums.Severity.High); // Set severity
        incident.setState(IncidentEnums.State.Open); // Set state
        incident.setSha256("somehashvalue");
        incident.setDevice("Router");
        incident.setLocation("Office");
        incident.setPriority(IncidentEnums.Priority.High); // Set priority
        incident.setReportedByTenant(tenant); // Associate the tenant
        incident.setAssignedToUser(user); // Associate the user

        // Save the incident
        incidentRepository.save(incident);
    }

    @Test
    public void testFindByUuid() {
        Incident foundIncident = incidentRepository.findByUuid(incident.getUuid());
        assertThat(foundIncident).isNotNull();
        assertThat(foundIncident.getUuid()).isEqualTo(incident.getUuid());
    }

    @Test
    public void testFindByReportedByTenantUuid() {
        List<Incident> incidents = incidentRepository.findByReportedByTenantUuid(tenant.getUuid());
        assertThat(incidents).isNotEmpty();
        assertThat(incidents.get(0).getReportedByTenant().getUuid()).isEqualTo(tenant.getUuid());
    }
}
