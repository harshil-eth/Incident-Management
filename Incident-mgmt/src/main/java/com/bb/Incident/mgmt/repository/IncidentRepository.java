package com.bb.Incident.mgmt.repository;

import com.bb.Incident.mgmt.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Incident findByUuid(String uuid);

    List<Incident> findByReportedByTenantUuid(String tenantUuid);
}
