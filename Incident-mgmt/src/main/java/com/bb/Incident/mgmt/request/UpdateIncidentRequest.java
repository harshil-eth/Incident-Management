package com.bb.Incident.mgmt.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    private String state;

    private String severity;

    private String sha256;

    private String device;

    private String location;

    private String priority;

    @JsonProperty("incident_type")
    private String incidentType;

    @JsonProperty("date_resolved")
    private LocalDateTime dateResolved;
}

