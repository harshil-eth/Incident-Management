package com.bb.Incident.mgmt.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class UpdateTenantRequest {

    private String name;

    private String description;

    private String username;

    private String roles;
}
