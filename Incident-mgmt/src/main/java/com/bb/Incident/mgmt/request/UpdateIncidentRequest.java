package com.bb.Incident.mgmt.request;

import com.bb.Incident.mgmt.entity.IncidentEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
public class UpdateIncidentRequest {

    @NotNull
    private String description;

    @Enumerated(EnumType.STRING)
    private IncidentEnums.State state;

    @Enumerated(EnumType.STRING)
    private IncidentEnums.Severity severity;

    private String sha256;

    private String device;

    private String location;

    @Enumerated(EnumType.STRING)
    private IncidentEnums.Priority priority;

    @JsonProperty("incident_type")
    private String incidentType;

    @JsonProperty("date_resolved")
    private LocalDateTime dateResolved;
}

