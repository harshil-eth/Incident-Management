package com.bb.Incident.mgmt.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Setter
@Table(name = "incident")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true)
    private String uuid;

    @JsonProperty("incident_type")
    @Column(name = "incident_type", nullable = false)
    private String incidentType;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private IncidentEnums.Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private IncidentEnums.State state;

    @Column(name = "sha256")
    private String sha256;

    @JsonProperty("date_reported")
    @Column(name = "date_reported", nullable = false)
    private LocalDateTime dateReported;

    @Column(name = "device", nullable = false)
    private String device;

    @Column(name = "location")
    private String location;

    @JsonProperty("date_resolved")
    @Column(name = "date_resolved")
    private LocalDateTime dateResolved;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private IncidentEnums.Priority priority;

    @ManyToOne
    @JoinColumn(name = "reported_by_tenant_id", nullable = false)
    private Tenant reportedByTenant;

    @ManyToOne
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedToUser;

    @PrePersist
    public void prePersist() {
        if(uuid == null || uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString();
        }
        if(dateReported == null) {
            dateReported = LocalDateTime.now();
        }
    }
}
