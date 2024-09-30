package com.bb.Incident.mgmt.response;


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

    private String severity;

    private String state;

    private String device;

    private String location;

    private String priority;

    private String sha256;

    private LocalDateTime dateReported;

    private LocalDateTime dateResolved;

    private String reportedByTenantId;

    private String assignedToUserId;
}
