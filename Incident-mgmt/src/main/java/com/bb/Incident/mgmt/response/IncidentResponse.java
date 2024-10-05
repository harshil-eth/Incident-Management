package com.bb.Incident.mgmt.response;


import com.bb.Incident.mgmt.entity.IncidentEnums;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class IncidentResponse {

    private String uuid;

    private String incidentType;

    private String description;

    @Enumerated(EnumType.STRING)
    private IncidentEnums.Severity severity;

    @Enumerated(EnumType.STRING)
    private IncidentEnums.State state;

    private String device;

    private String location;

    @Enumerated(EnumType.STRING)
    private IncidentEnums.Priority priority;

    private String sha256;

    private LocalDateTime dateReported;

    private LocalDateTime dateResolved;

    private String reportedByTenantId;

    private String assignedToUserId;
}
