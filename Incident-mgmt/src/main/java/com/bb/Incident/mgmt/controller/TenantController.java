package com.bb.Incident.mgmt.controller;

import com.bb.Incident.mgmt.entity.Tenant;
import com.bb.Incident.mgmt.request.UpdateTenantRequest;
import com.bb.Incident.mgmt.response.TenantResponse;
import com.bb.Incident.mgmt.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/v1/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

//    @Operation(summary = "Get all tenants", description = "Returns a list of all tenants")
//    @GetMapping
//    public List<TenantResponse> getAllTenants() {
//        return tenantService.getAllTenants();
//    }

//    @Operation(summary = "Get all tenants", description = "Returns a paginated list of all tenants")
//    @GetMapping
//    public Page<TenantResponse> getAllTenants(@PageableDefault(size = 5) Pageable pageable) {
//        return tenantService.getAllTenants(pageable);
//    }

    @Operation(summary = "Get all tenants", description = "Returns a paginated list of all tenants")
    @GetMapping
    public Map<String, Object> getAllTenants(@PageableDefault(size = 5) Pageable pageable) {
        Page<TenantResponse> page = tenantService.getAllTenants(pageable);
        Map<String, Object> response = new HashMap<>();
        response.put("tenants", page.getContent());
        response.put("currentPage", page.getNumber());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());

        if (page.hasNext()) {
            response.put("nextPage", "/v1/tenants?page=" + (page.getNumber() + 1) + "&size=" + page.getSize());
        }
        if (page.hasPrevious()) {
            response.put("previousPage", "/v1/tenants?page=" + (page.getNumber() - 1) + "&size=" + page.getSize());
        }

        return response;
    }

    @Operation(summary = "Get tenant with UUID", description = "Returns the tenant with UUID")
    @GetMapping("/{uuid}")
    public TenantResponse getTenantByUuid(@PathVariable String uuid) {
        return tenantService.getTenantByUuid(uuid);
    }

    @Operation(summary = "Create Tenant", description = "Creates a tenant")
    @PostMapping
    public TenantResponse createTenant(@Valid @RequestBody Tenant tenant) {
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
