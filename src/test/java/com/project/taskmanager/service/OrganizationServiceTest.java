package com.project.taskmanager.service;

import com.project.taskmanager.organization.OrgLookupResult;
import com.project.taskmanager.organization.Organization;
import com.project.taskmanager.organization.OrganizationRepository;
import com.project.taskmanager.organization.OrganizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock OrganizationRepository organizationRepository;
    @InjectMocks OrganizationService organizationService;

    @Test
    void findOrCreate_existingOrganization_isReusedAndNotNew() {
        Organization existing = mock(Organization.class);
        when(organizationRepository.findByName("Acme")).thenReturn(Optional.of(existing));

        OrgLookupResult result = organizationService.findOrCreateOrganization("Acme");

        assertThat(result.isNewlyCreated()).isFalse();
        assertThat(result.getOrganization()).isSameAs(existing);
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void findOrCreate_unknownOrganization_isCreatedAndNew() {
        when(organizationRepository.findByName("NewCo")).thenReturn(Optional.empty());
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        OrgLookupResult result = organizationService.findOrCreateOrganization("NewCo");

        assertThat(result.isNewlyCreated()).isTrue();
        assertThat(result.getOrganization().getName()).isEqualTo("NewCo");
        verify(organizationRepository).save(any(Organization.class));
    }
}
