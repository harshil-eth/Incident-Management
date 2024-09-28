package com.bb.Incident.mgmt.controller;

import com.bb.Incident.mgmt.entity.Incident;
import com.bb.Incident.mgmt.request.UpdateIncidentRequest;
import com.bb.Incident.mgmt.response.IncidentResponse;
import com.bb.Incident.mgmt.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/incidents")
public class IncidentController {

    @Autowired
    private IncidentService incidentService;

    @Operation(summary = "Get all incidents", description = "Returns a list of all incidents")
    @GetMapping
    @PreAuthorize("hasAuthority('incident.get')")
    public List<IncidentResponse> getAllIncidents() {
        return incidentService.getAllIncidents();
    }

    @Operation(summary = "Get incident with UUID", description = "Returns the incident with UUID")
    @GetMapping("/{uuid}")
    @PreAuthorize("hasAuthority('incident.get')")
    public IncidentResponse getIncidentById(@PathVariable String uuid) {
        return incidentService.getIncidentByUuid(uuid);
    }

    @Operation(summary = "Get Open Incidents", description = "Returns a list of all Open Incidents")
    @GetMapping("/open")
    @PreAuthorize("hasAuthority('incident.get')")
    public List<IncidentResponse> getOpenIncidents() {
        return incidentService.getOpenIncidents();
    }

    @Operation(summary = "Create Incident", description = "Creates an incident")
    @PostMapping
    @PreAuthorize("hasAuthority('incident.create')")
    public IncidentResponse createIncident(@RequestBody Incident incident) {
        Incident createdIncident = incidentService.createIncident(incident);
        System.out.println("Created Incident, " + createdIncident);
        return incidentService.convertToResponse(createdIncident);
    }

    @Operation(summary = "Update an incident with UUID", description = "Updates an incident with UUID")
    @PutMapping("/{uuid}")
    @PreAuthorize("hasAuthority('incident.update')")
    public IncidentResponse updateIncident(@PathVariable String uuid, @Valid @RequestBody UpdateIncidentRequest updateIncidentRequest) {
        return incidentService.updateIncident(uuid, updateIncidentRequest);
    }

    @Operation(summary = "Deletes an incident with UUID", description = "Deletes an incident with UUID")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAuthority('incident.delete')")
    public void deleteIncident(@PathVariable String uuid) {
        incidentService.deleteIncident(uuid);
    }

    @Operation(summary = "Resolve an incident", description = "Converts the state of incident to close and updates date resolved")
    @PutMapping("/resolve/{uuid}")
    @PreAuthorize("hasAuthority('incident.update')")
    public IncidentResponse resolveIncident(@PathVariable String uuid) {
        return incidentService.resolveIncident(uuid);
    }
}
