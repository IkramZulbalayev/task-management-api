package com.project.taskmanager.service;

import com.project.taskmanager.organization.Organization;
import com.project.taskmanager.project.Project;
import com.project.taskmanager.project.ProjectRepository;
import com.project.taskmanager.security.UserPrincipal;
import com.project.taskmanager.task.Task;
import com.project.taskmanager.task.TaskRepository;
import com.project.taskmanager.task.TaskService;
import com.project.taskmanager.task.TaskStatus;
import com.project.taskmanager.user.User;
import com.project.taskmanager.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
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
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock ProjectRepository projectRepository;
    @Mock UserRepository userRepository;
    @InjectMocks TaskService taskService;

    // NOTE: helpers build every nested object into a variable BEFORE calling when(...)

    private User userInOrg(long orgId) {
        Organization org = mock(Organization.class);
        when(org.getId()).thenReturn(orgId);
        User user = mock(User.class);
        when(user.getOrganization()).thenReturn(org);
        when(user.getFirstName()).thenReturn("Jane");
        when(user.getLastName()).thenReturn("Doe");
        return user;
    }

    private UserPrincipal principalFor(long orgId) {
        User user = userInOrg(orgId);
        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getUser()).thenReturn(user);
        return principal;
    }

    private Project projectInOrg(long orgId) {
        Organization org = mock(Organization.class);
        when(org.getId()).thenReturn(orgId);
        Project project = mock(Project.class);
        when(project.getOrganization()).thenReturn(org);
        return project;
    }

    private Task taskInOrg(long orgId) {
        Project project = projectInOrg(orgId);
        User creator = userInOrg(orgId);
        Task task = mock(Task.class);
        when(task.getProject()).thenReturn(project);
        when(task.getCreatedBy()).thenReturn(creator);
        return task;
    }

    // ---------- create ----------

    @Test
    void createTask_inOwnOrganization_savesTask() {
        UserPrincipal principal = principalFor(1L);
        Project project = projectInOrg(1L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        taskService.createTask("Implement login", "JWT auth", LocalDate.now(), null, null, 10L, principal);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Implement login");
    }

    @Test
    void createTask_projectNotFound_throws() {
        UserPrincipal principal = principalFor(1L);
        when(projectRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                taskService.createTask("T", "D", null, null, null, 10L, principal))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Project not found");
    }

    @Test
    void createTask_projectInOtherOrganization_isDenied() {
        UserPrincipal principal = principalFor(1L);
        Project project = projectInOrg(2L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() ->
                taskService.createTask("Secret", "D", null, null, null, 10L, principal))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied");
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_unknownAssignee_throws() {
        UserPrincipal principal = principalFor(1L);
        Project project = projectInOrg(1L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                taskService.createTask("T", "D", null, null, 77L, 10L, principal))
                .hasMessage("Assignee not found");
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_assigneeFromOtherOrganization_isDenied() {
        UserPrincipal principal = principalFor(1L);
        Project project = projectInOrg(1L);
        User outsider = userInOrg(2L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findById(77L)).thenReturn(Optional.of(outsider));

        assertThatThrownBy(() ->
                taskService.createTask("T", "D", null, null, 77L, 10L, principal))
                .hasMessage("Access denied");
        verify(taskRepository, never()).save(any());
    }

    // ---------- read ----------

    @Test
    void getTasksForProject_otherOrganization_isDenied() {
        UserPrincipal principal = principalFor(1L);
        Project project = projectInOrg(2L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> taskService.getTasksForProject(10L, principal))
                .hasMessage("Access denied");
        verify(taskRepository, never()).findByProjectId(any());
    }

    @Test
    void getTasksForProject_ownOrganization_returnsTasks() {
        UserPrincipal principal = principalFor(1L);
        Project project = projectInOrg(1L);
        Task task = taskInOrg(1L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(10L)).thenReturn(List.of(task));

        assertThat(taskService.getTasksForProject(10L, principal)).hasSize(1);
    }

    // ---------- update ----------

    @Test
    void updateTask_changesOnlyProvidedFields() {
        UserPrincipal principal = principalFor(1L);
        Task task = taskInOrg(1L);
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        taskService.updateTask(5L, "New title", null, null, null, TaskStatus.DONE, null, principal);

        verify(task).setTitle("New title");
        verify(task).setStatus(TaskStatus.DONE);
        verify(task, never()).setDescription(any());
        verify(task, never()).setAssignee(any());
        verify(taskRepository).save(task);
    }

    @Test
    void updateTask_notFound_throws() {
        UserPrincipal principal = principalFor(1L);
        when(taskRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                taskService.updateTask(5L, "x", null, null, null, null, null, principal))
                .hasMessage("Task not found");
    }

    @Test
    void updateTask_otherOrganization_isDenied() {
        UserPrincipal principal = principalFor(1L);
        Task task = taskInOrg(2L);
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() ->
                taskService.updateTask(5L, "x", null, null, null, null, null, principal))
                .hasMessage("Access denied");
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_assigneeFromOtherOrganization_isDenied() {
        UserPrincipal principal = principalFor(1L);
        Task task = taskInOrg(1L);
        User outsider = userInOrg(2L);
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
        when(userRepository.findById(77L)).thenReturn(Optional.of(outsider));

        assertThatThrownBy(() ->
                taskService.updateTask(5L, null, null, null, null, null, 77L, principal))
                .hasMessage("Access denied");
        verify(task, never()).setAssignee(any());
        verify(taskRepository, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    void deleteTask_otherOrganization_isDenied() {
        UserPrincipal principal = principalFor(1L);
        Task task = taskInOrg(2L);
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.deleteTask(5L, principal))
                .hasMessage("Access denied");
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void deleteTask_ownOrganization_isDeleted() {
        UserPrincipal principal = principalFor(1L);
        Task task = taskInOrg(1L);
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));

        taskService.deleteTask(5L, principal);

        verify(taskRepository).delete(task);
    }
}