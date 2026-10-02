package com.project.taskmanager.service;

import com.project.taskmanager.exception.AccessDeniedException;
import com.project.taskmanager.exception.ProjectNotEmptyException;
import com.project.taskmanager.exception.ResourceNotFoundException;
import com.project.taskmanager.organization.Organization;
import com.project.taskmanager.project.Project;
import com.project.taskmanager.project.ProjectRepository;
import com.project.taskmanager.project.ProjectService;
import com.project.taskmanager.security.UserPrincipal;
import com.project.taskmanager.task.TaskRepository;
import com.project.taskmanager.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectServiceTest {

    @Mock ProjectRepository projectRepository;
    @Mock TaskRepository taskRepository;
    @InjectMocks ProjectService projectService;

    private UserPrincipal principalFor(long orgId) {
        Organization org = mock(Organization.class);
        when(org.getId()).thenReturn(orgId);
        User user = mock(User.class);
        when(user.getOrganization()).thenReturn(org);
        when(user.getFirstName()).thenReturn("Jane");
        when(user.getLastName()).thenReturn("Doe");
        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getUser()).thenReturn(user);
        return principal;
    }

    private Project projectInOrg(long orgId) {
        Organization org = mock(Organization.class);
        when(org.getId()).thenReturn(orgId);
        User creator = mock(User.class);
        when(creator.getFirstName()).thenReturn("Creator");
        when(creator.getLastName()).thenReturn("Person");
        Project project = mock(Project.class);
        when(project.getOrganization()).thenReturn(org);
        when(project.getCreatedBy()).thenReturn(creator);
        return project;
    }

    @Test
    void createProject_savesProjectWithGivenName() {
        UserPrincipal principal = principalFor(1L);
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        projectService.createProject("Alpha", "First project", principal);

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Alpha");
        assertThat(captor.getValue().getDescription()).isEqualTo("First project");
    }

    @Test
    void getProjectsForCurrentOrg_queriesOnlyCurrentOrganization() {
        UserPrincipal principal = principalFor(1L);
        Project project = projectInOrg(1L);
        when(projectRepository.findByOrganizationId(1L)).thenReturn(List.of(project));

        var result = projectService.getProjectsForCurrentOrg(principal);

        assertThat(result).hasSize(1);
        verify(projectRepository).findByOrganizationId(1L);
    }

    @Test
    void updateProject_changesOnlyProvidedFields() {
        UserPrincipal principal = principalFor(1L);
        Project project = projectInOrg(1L);
        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(project);

        projectService.updateProject(5L, "New name", null, principal);

        verify(project).setName("New name");
        verify(project, never()).setDescription(any());
        verify(projectRepository).save(project);
    }

    @Test
    void updateProject_notFound_throws() {
        UserPrincipal principal = principalFor(1L);
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.updateProject(99L, "x", "y", principal))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProject_otherOrganization_isDenied() {
        UserPrincipal principal = principalFor(1L);
        Project project = projectInOrg(2L);
        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.updateProject(5L, "x", "y", principal))
                .isInstanceOf(AccessDeniedException.class);
        verify(projectRepository, never()).save(any());
    }

    @Test
    void deleteProject_otherOrganization_isDenied() {
        UserPrincipal principal = principalFor(1L);
        Project project = projectInOrg(2L);
        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.deleteProject(5L, principal))
                .isInstanceOf(AccessDeniedException.class);
        verify(projectRepository, never()).delete(any());
    }

    @Test
    void deleteProject_withExistingTasks_throws() {
        UserPrincipal principal = principalFor(1L);
        Project project = projectInOrg(1L);
        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));
        when(taskRepository.existsByProjectId(5L)).thenReturn(true);

        assertThatThrownBy(() -> projectService.deleteProject(5L, principal))
                .isInstanceOf(ProjectNotEmptyException.class);
        verify(projectRepository, never()).delete(any());
    }

    @Test
    void deleteProject_emptyProject_isDeleted() {
        UserPrincipal principal = principalFor(1L);
        Project project = projectInOrg(1L);
        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));
        when(taskRepository.existsByProjectId(5L)).thenReturn(false);

        projectService.deleteProject(5L, principal);

        verify(projectRepository).delete(project);
    }
}