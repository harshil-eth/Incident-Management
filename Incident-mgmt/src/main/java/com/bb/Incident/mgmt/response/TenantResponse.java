package com.bb.Incident.mgmt.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantResponse {

    private String uuid;

    private String name;

    private String description;

    private String parentSocTenantId;

    private String username;

    private String roles;
}
