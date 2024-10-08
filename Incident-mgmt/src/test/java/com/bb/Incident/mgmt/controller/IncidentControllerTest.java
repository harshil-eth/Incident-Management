package com.bb.Incident.mgmt.controller;

import com.bb.Incident.mgmt.entity.Incident;
import com.bb.Incident.mgmt.request.UpdateIncidentRequest;
import com.bb.Incident.mgmt.response.IncidentResponse;
import com.bb.Incident.mgmt.security.JwtUtil;
import com.bb.Incident.mgmt.service.IncidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentController.class)
public class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IncidentService incidentService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void testGetAllIncidents() throws Exception {
        IncidentResponse incident1 = new IncidentResponse();
        incident1.setUuid("1");

        IncidentResponse incident2 = new IncidentResponse();
        incident2.setUuid("2");

        Pageable pageable = PageRequest.of(0, 5);
        Page<IncidentResponse> incidentPage = new PageImpl<>(Arrays.asList(incident1, incident2), pageable, 2);

        given(incidentService.getAllIncidents(eq(pageable), any(), any(), any(), any())).willReturn(incidentPage);

        mockMvc.perform(get("/v1/incidents")
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incidents").isArray())
                .andExpect(jsonPath("$.incidents", hasSize(2)))
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    @WithMockUser(authorities = "incident.get")
    public void testGetIncidentById() throws Exception {
        IncidentResponse incidentResponse = new IncidentResponse();
        incidentResponse.setUuid(UUID.randomUUID().toString());

        when(incidentService.getIncidentByUuid(anyString())).thenReturn(incidentResponse);

        mockMvc.perform(get("/v1/incidents/{uuid}", incidentResponse.getUuid())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(incidentResponse.getUuid()));

        verify(incidentService, times(1)).getIncidentByUuid(anyString());
    }

    @Test
    @WithMockUser(authorities = "incident.get")
    public void testGetOpenIncidents() throws Exception {
        IncidentResponse incidentResponse = new IncidentResponse();
        incidentResponse.setUuid(UUID.randomUUID().toString());
        List<IncidentResponse> openIncidents = Arrays.asList(incidentResponse);

        when(incidentService.getOpenIncidents()).thenReturn(openIncidents);

        mockMvc.perform(get("/v1/incidents/open")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].uuid").value(incidentResponse.getUuid()));

        verify(incidentService, times(1)).getOpenIncidents();
    }

    @Test
    @WithMockUser(authorities = "incident.create")
    public void testCreateIncident() throws Exception {
        Incident incident = new Incident();
        incident.setUuid(UUID.randomUUID().toString());
        IncidentResponse incidentResponse = new IncidentResponse();
        incidentResponse.setUuid(incident.getUuid());

        when(incidentService.createIncident(any(Incident.class))).thenReturn(incident);
        when(incidentService.convertToResponse(any(Incident.class))).thenReturn(incidentResponse);

        mockMvc.perform(post("/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"incidentType\": \"Type\", \"description\": \"Description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(incident.getUuid()));

        verify(incidentService, times(1)).createIncident(any(Incident.class));
    }

    @Test
    @WithMockUser(authorities = "incident.update")
    public void testUpdateIncident() throws Exception {
        UpdateIncidentRequest updateIncidentRequest = new UpdateIncidentRequest();
        updateIncidentRequest.setIncidentType("Type");
        IncidentResponse incidentResponse = new IncidentResponse();
        incidentResponse.setUuid(UUID.randomUUID().toString());

        when(incidentService.updateIncident(anyString(), any(UpdateIncidentRequest.class))).thenReturn(incidentResponse);

        mockMvc.perform(put("/v1/incidents/{uuid}", incidentResponse.getUuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"incidentType\": \"Type\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(incidentResponse.getUuid()));

        verify(incidentService, times(1)).updateIncident(anyString(), any(UpdateIncidentRequest.class));
    }

    @Test
    @WithMockUser(authorities = "incident.delete")
    public void testDeleteIncident() throws Exception {
        String uuid = UUID.randomUUID().toString();

        doNothing().when(incidentService).deleteIncident(anyString());

        mockMvc.perform(delete("/v1/incidents/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(incidentService, times(1)).deleteIncident(anyString());
    }

    @Test
    @WithMockUser(authorities = "incident.update")
    public void testResolveIncident() throws Exception {
        IncidentResponse incidentResponse = new IncidentResponse();
        incidentResponse.setUuid(UUID.randomUUID().toString());

        when(incidentService.resolveIncident(anyString())).thenReturn(incidentResponse);

        mockMvc.perform(put("/v1/incidents/resolve/{uuid}", incidentResponse.getUuid())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(incidentResponse.getUuid()));

        verify(incidentService, times(1)).resolveIncident(anyString());
    }

    @Test
    @WithMockUser(authorities = "incident.update")
    public void testAssignUser() throws Exception {
        IncidentResponse incidentResponse = new IncidentResponse();
        incidentResponse.setUuid(UUID.randomUUID().toString());

        when(incidentService.assignUser(anyString())).thenReturn(incidentResponse);

        mockMvc.perform(put("/v1/incidents/assignUser/{uuid}", incidentResponse.getUuid())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(incidentResponse.getUuid()));

        verify(incidentService, times(1)).assignUser(anyString());
    }
}