package com.bb.Incident.mgmt.controller;

import com.bb.Incident.mgmt.entity.Incident;
import com.bb.Incident.mgmt.exception.CustomAccessDeniedException;
import com.bb.Incident.mgmt.request.UpdateIncidentRequest;
import com.bb.Incident.mgmt.response.IncidentResponse;
import com.bb.Incident.mgmt.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/incidents")
public class IncidentController {

    @Autowired
    private IncidentService incidentService;

    @Operation(summary = "Get all incidents", description = "Returns a paginated list of all incidents")
    @GetMapping
//    @PreAuthorize("hasAuthority('incident.get')")
    public Map<String, Object> getAllIncidents(
            @PageableDefault(size = 5) Pageable pageable,
            @RequestParam(required = false) String incidentType,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String priority
    ) {

        if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("incident.get"))) {
            throw new CustomAccessDeniedException("You need special permissions to get incidents.");
        }

        Page<IncidentResponse> page = incidentService.getAllIncidents(pageable, incidentType, severity, state, priority);
        Map<String, Object> response = new HashMap<>();
        response.put("incidents", page.getContent());
        response.put("currentPage", page.getNumber());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());

        if (page.hasNext()) {
            response.put("nextPage", "/v1/incidents?page=" + (page.getNumber() + 1) + "&size=" + page.getSize());
        }
        if (page.hasPrevious()) {
            response.put("previousPage", "/v1/incidents?page=" + (page.getNumber() - 1) + "&size=" + page.getSize());
        }

        return response;
    }

    @Operation(summary = "Get incident with UUID", description = "Returns the incident with UUID")
    @GetMapping("/{uuid}")
//    @PreAuthorize("hasAuthority('incident.get')")
    public IncidentResponse getIncidentById(@PathVariable String uuid) {

        if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("incident.get"))) {
            throw new CustomAccessDeniedException("You need special permissions to get the incident with UUID: " + uuid);
        }

        return incidentService.getIncidentByUuid(uuid);
    }

    @Operation(summary = "Get Open Incidents", description = "Returns a list of all Open Incidents")
    @GetMapping("/open")
//    @PreAuthorize("hasAuthority('incident.get')")
    public List<IncidentResponse> getOpenIncidents() {

        if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("incident.get"))) {
            throw new CustomAccessDeniedException("You need special permissions to get the open incidents.");
        }

        return incidentService.getOpenIncidents();
    }

    @Operation(summary = "Create Incident", description = "Creates an incident")
    @PostMapping
//    @PreAuthorize("hasAuthority('incident.create')")
    public IncidentResponse createIncident(@RequestBody Incident incident) {

        if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("incident.create"))) {
            throw new CustomAccessDeniedException("You need special permissions to create an incident.");
        }

        Incident createdIncident = incidentService.createIncident(incident);
        System.out.println("Created Incident, " + createdIncident);
        return incidentService.convertToResponse(createdIncident);
    }

    @Operation(summary = "Update an incident with UUID", description = "Updates an incident with UUID")
    @PutMapping("/{uuid}")
//    @PreAuthorize("hasAuthority('incident.update')")
    public IncidentResponse updateIncident(@PathVariable String uuid, @Valid @RequestBody UpdateIncidentRequest updateIncidentRequest) {

        if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("incident.update"))) {
            throw new CustomAccessDeniedException("You need special permissions to update the incident with UUID: " + uuid);
        }

        return incidentService.updateIncident(uuid, updateIncidentRequest);
    }

    @Operation(summary = "Deletes an incident with UUID", description = "Deletes an incident with UUID")
    @DeleteMapping("/{uuid}")
//    @PreAuthorize("hasAuthority('incident.delete')")
    public void deleteIncident(@PathVariable String uuid) {

        if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("incident.delete"))) {
            throw new CustomAccessDeniedException("You need special permissions to delete the incident with UUID: " + uuid);
        }

        incidentService.deleteIncident(uuid);
    }

    @Operation(summary = "Resolve an incident", description = "Converts the state of incident to close and updates date resolved")
    @PutMapping("/resolve/{uuid}")
//    @PreAuthorize("hasAuthority('incident.update')")
    public IncidentResponse resolveIncident(@PathVariable String uuid) {

        if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("incident.update"))) {
            throw new CustomAccessDeniedException("You need special permissions to resolve the incident with UUID: " + uuid);
        }

        return incidentService.resolveIncident(uuid);
    }

    @Operation(summary = "Assign a user to incident", description = "Randomly assigns a user to the incident")
    @PutMapping("/assignUser/{uuid}")
//    @PreAuthorize("hasAuthority('incident.update')")
    public IncidentResponse assignUser(@PathVariable String uuid) {

        if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("incident.update"))) {
            throw new CustomAccessDeniedException("You need special permissions to assign an user to incident with UUID: " + uuid);
        }

        return incidentService.assignUser(uuid);
    }
}
