package com.bb.Incident.mgmt.repository;

import com.bb.Incident.mgmt.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Tenant findByUuid(String uuid);

    void deleteByUuid(String uuid);

    Tenant findByUsername(String username);

    boolean existsByUsername(String username);
}
