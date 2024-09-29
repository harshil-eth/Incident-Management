package com.bb.Incident.mgmt.service;

import com.bb.Incident.mgmt.entity.Incident;
import com.bb.Incident.mgmt.entity.Tenant;
import com.bb.Incident.mgmt.entity.User;
import com.bb.Incident.mgmt.exception.DatabaseConnectionException;
import com.bb.Incident.mgmt.exception.IncidentNotFoundException;
import com.bb.Incident.mgmt.exception.InvalidIncidentDataException;
import com.bb.Incident.mgmt.exception.OpenIncidentsException;
import com.bb.Incident.mgmt.repository.IncidentRepository;
import com.bb.Incident.mgmt.repository.TenantRepository;
import com.bb.Incident.mgmt.repository.UserRepository;
import com.bb.Incident.mgmt.request.UpdateIncidentRequest;
import com.bb.Incident.mgmt.response.IncidentResponse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

//    public List<IncidentResponse> getAllIncidents() {
//        try {
//            List<Incident> incidents = incidentRepository.findAll();
//            return incidents.stream().map(this::convertToResponse).collect(Collectors.toList());
//        } catch (DataAccessException ex) {
//            throw new DatabaseConnectionException("Failed to connect to the database.");
//        }
//    }

//    public Page<IncidentResponse> getAllIncidents(Pageable pageable) {
//       try {
//            Page<Incident> incidents = incidentRepository.findAll(pageable);
//            return incidents.map(this::convertToResponse);
//       } catch (DataAccessException ex) {
//           throw new DatabaseConnectionException("Failed to connect to the database.");
//       }
//    }

    public Page<IncidentResponse> getAllIncidents(Pageable pageable, String incidentType, String severity, String state, String priority) {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Incident> criteriaQuery = criteriaBuilder.createQuery(Incident.class);
            Root<Incident> root = criteriaQuery.from(Incident.class);

            List<Predicate> predicates = new ArrayList<>();
            if (incidentType != null) {
                predicates.add(criteriaBuilder.equal(root.get("incidentType"), incidentType));
            }
            if (severity != null) {
                predicates.add(criteriaBuilder.equal(root.get("severity"), severity));
            }
            if (state != null) {
                predicates.add(criteriaBuilder.equal(root.get("state"), state));
            }
            if(priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }

            criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));

            List<Incident> incidents = entityManager.createQuery(criteriaQuery)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            long total = entityManager.createQuery(criteriaQuery).getResultList().size();

            Page<Incident> incidentPage = new PageImpl<>(incidents, pageable, total);

            return incidentPage.map(this::convertToResponse);
        } catch (DataAccessException ex) {
            throw new DatabaseConnectionException("Failed to connect to the database.");
        }
    }

    public IncidentResponse getIncidentByUuid(String uuid) {
        Incident incident = incidentRepository.findByUuid(uuid);
        if(incident == null) {
            throw new IncidentNotFoundException("Incident not Found with UUID: " + uuid);
        }
        return convertToResponse(incident);
    }

    public IncidentResponse convertToResponse(Incident incident) {
        IncidentResponse response = new IncidentResponse();
        response.setUuid(incident.getUuid());
        response.setIncidentType(incident.getIncidentType());
        response.setDescription(incident.getDescription());
        response.setSeverity(incident.getSeverity());
        response.setState(incident.getState());
        response.setDevice(incident.getDevice());
        response.setLocation(incident.getLocation());
        response.setPriority(incident.getPriority());
        response.setSha256(incident.getSha256());
        response.setDateReported(incident.getDateReported());
        response.setDateResolved(incident.getDateResolved());
        response.setReportedByTenantId(incident.getReportedByTenant().getUuid()); // SETTING reportedByTenantId
        response.setAssignedToUserId(incident.getAssignedToUser() != null ? incident.getAssignedToUser().getUuid() : null); // SETTING assignedToUserId
        return response;
    }

    @Transactional // as we are transacting
    public Incident createIncident(Incident incident) {

        if(incident == null || incident.getIncidentType() == null || incident.getDescription() == null) {
            throw new InvalidIncidentDataException("Incident data is invalid.");
        }

        Tenant reportedByTenant = tenantRepository.findByUuid(incident.getReportedByTenant().getUuid());
        User assignedToUser = null;
        if (incident.getAssignedToUser() != null) {
            assignedToUser = userRepository.findByUuid(incident.getAssignedToUser().getUuid());
        }

        incident.setReportedByTenant(reportedByTenant);
        incident.setAssignedToUser(assignedToUser);

        return incidentRepository.save(incident);
    }

    @Transactional
    public IncidentResponse updateIncident(String uuid, UpdateIncidentRequest updateIncidentRequest) {
        Incident existingIncident = incidentRepository.findByUuid(uuid);

        if(existingIncident == null) {
            throw new IncidentNotFoundException("No incident found with UUID: " + uuid);
        }

        // only SOC tenant can update the incident (done by authorization)

        if(updateIncidentRequest.getIncidentType() != null) {
            existingIncident.setIncidentType(updateIncidentRequest.getIncidentType());
        }
        if(updateIncidentRequest.getDateResolved() != null) {
            existingIncident.setDateResolved(updateIncidentRequest.getDateResolved());
        }
        if(updateIncidentRequest.getDevice() != null) {
            existingIncident.setDevice(updateIncidentRequest.getDevice());
        }
        if(updateIncidentRequest.getPriority() != null) {
            existingIncident.setPriority(updateIncidentRequest.getPriority());
        }
        if(updateIncidentRequest.getDescription() != null) {
            existingIncident.setDescription(updateIncidentRequest.getDescription());
        }
        if(updateIncidentRequest.getSha256() != null) {
            existingIncident.setSha256(updateIncidentRequest.getSha256());
        }
        if(updateIncidentRequest.getState() != null) {
            existingIncident.setState(updateIncidentRequest.getState());
        }
        if(updateIncidentRequest.getSeverity() != null) {
            existingIncident.setSeverity(updateIncidentRequest.getSeverity());
        }
        if(updateIncidentRequest.getLocation() != null) {
            existingIncident.setLocation(updateIncidentRequest.getLocation());
        }

        return convertToResponse(incidentRepository.save(existingIncident));
    }

    @Transactional
    public void deleteIncident(String uuid) {
        Incident existingIncident = incidentRepository.findByUuid(uuid);

        if(existingIncident == null) {
            throw new IncidentNotFoundException("Incident not Found with UUID: " + uuid);
        }

        // SOC tenant can only delete the incident, that has been taken care by authorization

        // have to put a check if incident is in open STATE or not, if open you can't delete
        if(Objects.equals(existingIncident.getState(), "Open")) {
            throw new OpenIncidentsException("Incident is in open state, so can not delete.");
        }

        incidentRepository.delete(existingIncident);
    }

    public boolean hasOpenIncidents(String tenantUuid) {
        List<Incident> incidents = incidentRepository.findByReportedByTenantUuid(tenantUuid);
        return incidents.stream().anyMatch(incident -> "Open".equalsIgnoreCase(incident.getState()));
    }

    public List<IncidentResponse> getOpenIncidents() {
        List<Incident> incidents = incidentRepository.findAll();
        List<Incident> openIncidents = incidents.stream().filter(incident -> "Open".equalsIgnoreCase(incident.getState()))
                .toList();

        return openIncidents.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        // one doubt -> what to give if there are no open incidents ??
    }

    public IncidentResponse resolveIncident(String uuid) {
        Incident incident = incidentRepository.findByUuid(uuid);

        if(incident == null) {
            throw new IncidentNotFoundException("Incident not found with UUID: " + uuid);
        }

        incident.setState("Close");
        incident.setDateResolved(LocalDateTime.now());
        incidentRepository.save(incident);

        return convertToResponse(incident);
    }
}