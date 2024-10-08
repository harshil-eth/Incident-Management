package com.bb.Incident.mgmt.repository;

import com.bb.Incident.mgmt.entity.Tenant;
import com.bb.Incident.mgmt.entity.User;
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
@ActiveProfiles("test")  // Use the 'test' profile for PostgreSQL
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private User user;

    @BeforeEach
    public void setUp() {
        // Create a new Tenant instance
        Tenant tenant = new Tenant();

        // Set required properties for the Tenant
        tenant.setUuid("test-tenant-uuid"); // UUID
        tenant.setName("Test Tenant"); // Name (required)
        tenant.setParentSocTenantId("parent-soc-id"); // Parent SOC Tenant ID (required)
        tenant.setUsername("tenantuser"); // Username (required)
        tenant.setPassword("password123"); // Password (required)
        tenant.setRoles("ROLE_USER"); // Set roles as needed

        // Save the tenant
        tenant = tenantRepository.save(tenant); // Save tenant and retrieve the persisted instance

        // Create a new User instance
        user = new User();

        // Set required properties for the User
        user.setUuid("test-uuid"); // UUID
        user.setUsername("testuser"); // Username
        user.setEmail("test@example.com"); // Email
        user.setFirstName("John"); // First Name (required)
        user.setLastName("Doe"); // Last Name (required)

        // Associate the tenant
        user.setTenant(tenant); // Associate the saved tenant

        // Save the user
        userRepository.save(user);
    }

    @Test
    public void testFindByUuid() {
        User foundUser = userRepository.findByUuid(user.getUuid());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    public void testExistsByUsername() {
        boolean exists = userRepository.existsByUsername(user.getUsername());
        assertThat(exists).isTrue();
    }

    @Test
    public void testDeleteByUuid() {
        userRepository.deleteByUuid(user.getUuid());
        Optional<User> deletedUser = Optional.ofNullable(userRepository.findByUuid(user.getUuid()));
        assertThat(deletedUser).isEmpty();
    }
}
