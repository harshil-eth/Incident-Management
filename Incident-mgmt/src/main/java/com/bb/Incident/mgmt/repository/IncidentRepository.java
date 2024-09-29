package com.bb.Incident.mgmt.repository;

import com.bb.Incident.mgmt.entity.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Incident findByUuid(String uuid);

    List<Incident> findByReportedByTenantUuid(String tenantUuid);

    Page<Incident> findByPriority(String priority, Pageable pageable);

    Page<Incident> findByPriorityAndState(String priority, String state, Pageable pageable);

    Page<Incident> findByPriorityAndStateAndIncidentType(String priority, String state, String incidentType, Pageable pageable);
}
