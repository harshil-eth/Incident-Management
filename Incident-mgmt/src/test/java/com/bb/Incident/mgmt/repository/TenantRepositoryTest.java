package com.bb.Incident.mgmt.repository;

import com.bb.Incident.mgmt.entity.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Prevent using embedded database
@ActiveProfiles("test") // Use the 'test' profile for PostgreSQL
public class TenantRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant tenant;

    @BeforeEach
    public void setUp() {
        // Create a new Tenant instance
        tenant = new Tenant();

        // Set required properties for the Tenant
        tenant.setUuid("test-tenant-uuid"); // UUID
        tenant.setName("Test Tenant"); // Name (required)
        tenant.setParentSocTenantId("parent-soc-id"); // Parent SOC Tenant ID (required)
        tenant.setUsername("tenantuser"); // Username (required)
        tenant.setPassword("password123"); // Password (required)
        tenant.setRoles("ROLE_USER"); // Set roles as needed

        // Save the tenant
        tenantRepository.save(tenant); // Save tenant to the database
    }

    @Test
    public void testFindByUuid() {
        Tenant foundTenant = tenantRepository.findByUuid(tenant.getUuid());
        assertThat(foundTenant).isNotNull();
        assertThat(foundTenant.getUsername()).isEqualTo(tenant.getUsername());
    }

    @Test
    public void testFindByUsername() {
        Tenant foundTenant = tenantRepository.findByUsername(tenant.getUsername());
        assertThat(foundTenant).isNotNull();
        assertThat(foundTenant.getUuid()).isEqualTo(tenant.getUuid());
    }

    @Test
    public void testExistsByUsername() {
        boolean exists = tenantRepository.existsByUsername(tenant.getUsername());
        assertThat(exists).isTrue();
    }

    @Test
    public void testDeleteByUuid() {
        tenantRepository.deleteByUuid(tenant.getUuid());
        Optional<Tenant> deletedTenant = Optional.ofNullable(tenantRepository.findByUuid(tenant.getUuid()));
        assertThat(deletedTenant).isEmpty(); // Confirm that the tenant no longer exists
    }

    @Test
    public void testNotFoundByUuid() {
        Tenant foundTenant = tenantRepository.findByUuid("non-existent-uuid");
        assertThat(foundTenant).isNull(); // Should return null for non-existent UUID
    }

    @Test
    public void testNotExistsByUsername() {
        boolean exists = tenantRepository.existsByUsername("non-existent-username");
        assertThat(exists).isFalse(); // Should return false for non-existent username
    }
}
