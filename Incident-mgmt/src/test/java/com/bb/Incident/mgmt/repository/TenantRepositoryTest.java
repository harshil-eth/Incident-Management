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
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class TenantRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant tenant;

    @BeforeEach
    public void setUp() {
        tenant = new Tenant();

        tenant.setUuid("test-tenant-uuid");
        tenant.setName("Test Tenant");
        tenant.setParentSocTenantId("parent-soc-id");
        tenant.setUsername("tenantuser");
        tenant.setPassword("password123");
        tenant.setRoles("ROLE_USER");

        tenantRepository.save(tenant);
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
        assertThat(deletedTenant).isEmpty();
    }

    @Test
    public void testNotFoundByUuid() {
        Tenant foundTenant = tenantRepository.findByUuid("non-existent-uuid");
        assertThat(foundTenant).isNull();
    }

    @Test
    public void testNotExistsByUsername() {
        boolean exists = tenantRepository.existsByUsername("non-existent-username");
        assertThat(exists).isFalse();
    }
}
