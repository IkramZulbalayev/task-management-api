package com.project.taskmanager.organization;

import org.springframework.stereotype.Service;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Organization createOrganization(String name){
        Organization organization = new Organization(name);
        return organizationRepository.save(organization);
    }

    public OrgLookupResult findOrCreateOrganization(String name) {
        return organizationRepository.findByName(name)
                .map(org -> new OrgLookupResult(org, false))
                .orElseGet(() -> new OrgLookupResult(createOrganization(name), true));
    }
}