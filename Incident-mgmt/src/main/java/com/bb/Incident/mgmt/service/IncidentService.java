package com.bb.Incident.mgmt.service;

import com.bb.Incident.mgmt.entity.Incident;
import com.bb.Incident.mgmt.entity.IncidentEnums;
import com.bb.Incident.mgmt.entity.Tenant;
import com.bb.Incident.mgmt.entity.User;
import com.bb.Incident.mgmt.exception.*;
import com.bb.Incident.mgmt.repository.IncidentRepository;
import com.bb.Incident.mgmt.repository.TenantRepository;
import com.bb.Incident.mgmt.repository.UserRepository;
import com.bb.Incident.mgmt.request.UpdateIncidentRequest;
import com.bb.Incident.mgmt.response.IncidentResponse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.converter.json.GsonBuilderUtils;
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

    @Autowired
    private UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    public static String capitalizeFirstLetter(String input) {
        if(input == null || input.isEmpty()) {
            return input;
        }

        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

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
                try {
                    String formattedSeverity = capitalizeFirstLetter(severity);
                    predicates.add(criteriaBuilder.equal(root.get("severity"), IncidentEnums.Severity.valueOf(formattedSeverity)));
                }
                catch (IllegalArgumentException ex) {
                    throw new InvalidFilterException("Invalid Severity value, please chose from these: Critical, High, Medium, Low");
                }
            }
            if (state != null) {
                try {
                    String formattedState = capitalizeFirstLetter(state);
                    predicates.add(criteriaBuilder.equal(root.get("state"), IncidentEnums.State.valueOf(formattedState)));
                }
                catch (IllegalArgumentException ex) {
                    throw new InvalidFilterException("Invalid State value, please chose from these: Open, Close, In_Progress");
                }
            }
            if (priority != null) {
                try {
                    String formattedPriority = capitalizeFirstLetter(priority);
                    predicates.add(criteriaBuilder.equal(root.get("priority"), IncidentEnums.Priority.valueOf(formattedPriority)));
                } catch (IllegalArgumentException ex) {
                    throw new InvalidFilterException("Invalid priority value, please chose from these: High, Medium, Low");
                }
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
        if (incident == null) {
            throw new IllegalArgumentException("Incident cannot be null");
        }

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

        // Error handling for reportedByTenant
        Tenant reportedByTenant = incident.getReportedByTenant();
        if (reportedByTenant != null) {
            response.setReportedByTenantId(reportedByTenant.getUuid());
        } else {
            response.setReportedByTenantId(null); // or handle this case as needed
        }

        // Error handling for assignedToUser
        User assignedToUser = incident.getAssignedToUser();
        if (assignedToUser != null) {
            response.setAssignedToUserId(assignedToUser.getUuid());
        } else {
            response.setAssignedToUserId(null); // or handle this case as needed
        }

        return response;
    }


    @Transactional // as we are transacting
    public Incident createIncident(Incident incident) {

        if(incident == null || incident.getIncidentType() == null || incident.getDescription() == null
                || incident.getReportedByTenant() == null) {
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
        if(existingIncident.getState() == IncidentEnums.State.Open) {
            throw new OpenIncidentsException("Incident is not resolved yet, so can not delete.");
        }

        incidentRepository.delete(existingIncident);
    }

    public boolean hasOpenIncidents(String tenantUuid) {
        List<Incident> incidents = incidentRepository.findByReportedByTenantUuid(tenantUuid);
        return incidents.stream().anyMatch(incident -> incident.getState() == IncidentEnums.State.Open);
    }

    public List<IncidentResponse> getOpenIncidents() {
        List<Incident> incidents = incidentRepository.findAll();
        List<Incident> openIncidents = incidents.stream().filter(incident -> incident.getState() == IncidentEnums.State.Open)
                .toList();

        return openIncidents.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IncidentResponse resolveIncident(String uuid) {
        Incident incident = incidentRepository.findByUuid(uuid);

        if(incident == null) {
            throw new IncidentNotFoundException("Incident not found with UUID: " + uuid);
        }

        incident.setState(IncidentEnums.State.Close);
        incident.setDateResolved(LocalDateTime.now());
        incidentRepository.save(incident);

        return convertToResponse(incident);
    }

    @Transactional
    public IncidentResponse assignUser(String uuid) {
        Incident incident = incidentRepository.findByUuid(uuid);

        if(incident == null) {
            throw new IncidentNotFoundException("Incident not found with UUID: " + uuid);
        }

        // check if the user is already assigned to that incident
        if(incident.getAssignedToUser() != null) {
            throw new UserAlreadyExistsException("User is already assigned to this incident, and the assigned user UUID is: " + incident.getAssignedToUser().getUuid());
        }

        String randomUserUuid = userService.getRandomUser();
        User randomUser = userRepository.findByUuid(randomUserUuid);

        if(randomUser == null) {
            throw new UserNotFoundException("User not found with UUID: " + randomUserUuid);
        }

        incident.setAssignedToUser(randomUser);
        incidentRepository.save(incident);

        return convertToResponse(incident);
    }
}