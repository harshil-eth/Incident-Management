package com.bb.Incident.mgmt.service;

import com.bb.Incident.mgmt.entity.Tenant;
import com.bb.Incident.mgmt.exception.*;
import com.bb.Incident.mgmt.repository.TenantRepository;
import com.bb.Incident.mgmt.request.UpdateTenantRequest;
import com.bb.Incident.mgmt.response.TenantResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private IncidentService incidentService;

//    public List<TenantResponse> getAllTenants() {
//        try {
//            List<Tenant> tenants = tenantRepository.findAll();
//            return tenants.stream().map(this::convertToResponse).collect(Collectors.toList());
//        } catch (DataAccessException ex) {
//            throw new DatabaseConnectionException("Failed to connect to the database.");
//        }
//    }

    public Page<TenantResponse> getAllTenants(Pageable pageable) {
        try {
            Page<Tenant> tenants = tenantRepository.findAll(pageable);
            return tenants.map(this::convertToResponse);
        } catch (DataAccessException ex) {
            throw new DatabaseConnectionException("Failed to connect to the database.");
        }
    }

    public TenantResponse getTenantByUuid(String uuid) {
        Tenant tenant = tenantRepository.findByUuid(uuid);
        if(tenant == null) {
            throw new TenantNotFoundException("Tenant not found with UUID: " + uuid);
        }
        return convertToResponse(tenant);
    }

    public TenantResponse convertToResponse(Tenant tenant) {
        TenantResponse response = new TenantResponse();
        response.setUuid(tenant.getUuid());
        response.setName(tenant.getName());
        response.setDescription(tenant.getDescription());
        response.setParentSocTenantId(tenant.getParentSocTenantId());
        response.setUsername(tenant.getUsername());
        response.setRoles(tenant.getRoles());
        return response;
    }

    @Transactional
    public TenantResponse createTenant(Tenant tenant) {

        if(tenant == null) {
            throw new IllegalArgumentException("Tenant can not be null.");
        }

        if(tenantRepository.existsByUsername(tenant.getUsername())) {
            throw new TenantAlreadyExistsException("Tenant already exists with username: " + tenant.getUsername());
        }

        if(tenantRepository.findByUuid(tenant.getUuid()) != null) {
            throw new TenantAlreadyExistsException("Tenant already exists with UUID: " + tenant.getUuid());
        }
        tenant.setPassword(bCryptPasswordEncoder.encode(tenant.getPassword()));

        return convertToResponse(tenantRepository.save(tenant));
    }

    public String getTenantRolesByUsername(String username) {
        Tenant tenant = tenantRepository.findByUsername(username);
        String roles = tenant != null ? tenant.getRoles() : null;
        System.out.println("Fetched roles for username " + username + ": " + roles);
        return roles;
    }


    public Tenant findByUsername(String username) {
        return tenantRepository.findByUsername(username);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
    }

    @Transactional
    public void deleteTenant(String uuid) {

        // catches ->
        // make sure that it is not a SOC tenant that is getting deleted.
        // and what if an incident is in open state, and issue is not solved yet, and you are deleting that tenant

        Tenant tenant = tenantRepository.findByUuid(uuid);
        if (tenant == null) {
            throw new TenantNotFoundException("Tenant not found with UUID: " + uuid);
        }

        if(isSocTenant(tenant)) {
            throw new SocTenantDeletionException("Cannot delete a SOC Tenant.");
        }

        if(hasOpenIncidents(tenant)) {
            throw new OpenIncidentsException("Cannot delete a Tenant with open incidents");
        }

        tenantRepository.deleteByUuid(uuid);
    }

    private boolean isSocTenant(Tenant tenant) {

        String roles = tenant.getRoles();
        return Objects.equals(roles, "incident.get incident.create incident.update incident.delete");
    }

    private boolean hasOpenIncidents(Tenant tenant) {
        // have to check with Incident service that the incident is open or not
        return incidentService.hasOpenIncidents(tenant.getUuid());
    }

    // better way to update tenants
    @Transactional
    public TenantResponse updateTenant(String uuid, @Valid UpdateTenantRequest updateTenantRequest) {
        if(updateTenantRequest == null) {
            throw new IllegalArgumentException("Update Tenant Request can not be null.");
        }

        Tenant existingTenant = tenantRepository.findByUuid(uuid);
        if (existingTenant == null) {
            throw new TenantNotFoundException("Tenant not found with UUID: " + uuid);
        }

        Map<String, BiConsumer<Tenant, Object>> fieldUpdaters = new HashMap<>();
        fieldUpdaters.put("name", (tenant, value) -> tenant.setName((String) value));
        fieldUpdaters.put("description", (tenant, value) -> tenant.setDescription((String) value));
        fieldUpdaters.put("username", (tenant, value) -> tenant.setUsername((String) value));
        fieldUpdaters.put("roles", (tenant, value) -> tenant.setRoles((String) value));

        updateTenantRequestFields(updateTenantRequest, existingTenant, fieldUpdaters);

        return convertToResponse(tenantRepository.save(existingTenant));
    }

    private void updateTenantRequestFields(UpdateTenantRequest updateTenantRequest, Tenant existingTenant, Map<String, BiConsumer<Tenant, Object>> fieldUpdaters) {
        fieldUpdaters.forEach((field, updater) -> {
            try {
                Field requestField = UpdateTenantRequest.class.getDeclaredField(field);
                requestField.setAccessible(true);
                Object value = requestField.get(updateTenantRequest);
                if (value != null) {
                    updater.accept(existingTenant, value);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Error updating field: " + field, e);
            }
        });
    }
}
