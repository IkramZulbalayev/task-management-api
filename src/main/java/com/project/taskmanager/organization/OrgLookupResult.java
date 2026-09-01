package com.project.taskmanager.organization;

public class OrgLookupResult {
    private final Organization organization;
    private final boolean newlyCreated;

    public OrgLookupResult(Organization organization, boolean newlyCreated) {
        this.organization = organization;
        this.newlyCreated = newlyCreated;
    }

    public Organization getOrganization() {
        return organization;
    }

    public boolean isNewlyCreated() {
        return newlyCreated;
    }
}