package com.bb.Incident.mgmt.controller;

import com.bb.Incident.mgmt.entity.Tenant;
import com.bb.Incident.mgmt.request.UpdateTenantRequest;
import com.bb.Incident.mgmt.response.TenantResponse;
import com.bb.Incident.mgmt.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/v1/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @Operation(summary = "Get all tenants", description = "Returns a list of all tenants")
    @GetMapping
    public List<TenantResponse> getAllTenants() {
        return tenantService.getAllTenants();
    }

    @Operation(summary = "Get tenant with UUID", description = "Returns the tenant with UUID")
    @GetMapping("/{uuid}")
    public TenantResponse getTenantByUuid(@PathVariable String uuid) {
        return tenantService.getTenantByUuid(uuid);
    }

    @Operation(summary = "Create Tenant", description = "Creates a tenant")
    @PostMapping
    public TenantResponse createTenant(@Valid @RequestBody Tenant tenant) {
        System.out.println("hi");
        return tenantService.createTenant(tenant);
    }

    @Operation(summary = "Update the tenant with UUID", description = "Updates the tenant with UUID")
    @PutMapping("/{uuid}")
    public TenantResponse updateTenant(@PathVariable String uuid, @Valid @RequestBody UpdateTenantRequest updateTenantRequest) {
        return tenantService.updateTenant(uuid, updateTenantRequest);
    }

    @Operation(summary = "Delete a tenant with UUID", description = "Deletes a tenant with UUID")
    @DeleteMapping("/{uuid}")
    public void deleteTenant(@PathVariable String uuid) {
        tenantService.deleteTenant(uuid);
    }
}
