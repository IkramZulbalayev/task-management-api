package com.project.taskmanager.service;

import com.project.taskmanager.comment.Comment;
import com.project.taskmanager.comment.CommentRepository;
import com.project.taskmanager.comment.CommentService;
import com.project.taskmanager.exception.AccessDeniedException;
import com.project.taskmanager.exception.ResourceNotFoundException;
import com.project.taskmanager.organization.Organization;
import com.project.taskmanager.project.Project;
import com.project.taskmanager.security.UserPrincipal;
import com.project.taskmanager.task.Task;
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
class CommentServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock TaskRepository taskRepository;
    @InjectMocks CommentService commentService;

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

    private Task taskInOrg(long orgId) {
        Organization org = mock(Organization.class);
        when(org.getId()).thenReturn(orgId);
        Project project = mock(Project.class);
        when(project.getOrganization()).thenReturn(org);
        Task task = mock(Task.class);
        when(task.getProject()).thenReturn(project);
        return task;
    }

    private Comment commentInOrg(long orgId) {
        Task task = taskInOrg(orgId);
        User author = userInOrg(orgId);
        Comment comment = mock(Comment.class);
        when(comment.getTask()).thenReturn(task);
        when(comment.getAuthor()).thenReturn(author);
        return comment;
    }

    @Test
    void createComment_inOwnOrganization_savesComment() {
        UserPrincipal principal = principalFor(1L);
        Task task = taskInOrg(1L);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        commentService.createComment("Looks good", 10L, principal);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("Looks good");
    }

    @Test
    void createComment_taskNotFound_throws() {
        UserPrincipal principal = principalFor(1L);
        when(taskRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment("x", 10L, principal))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createComment_taskInOtherOrganization_isDenied() {
        UserPrincipal principal = principalFor(1L);
        Task task = taskInOrg(2L);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> commentService.createComment("x", 10L, principal))
                .isInstanceOf(AccessDeniedException.class);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void getCommentsForTask_ownOrganization_returnsComments() {
        UserPrincipal principal = principalFor(1L);
        Task task = taskInOrg(1L);
        Comment comment = commentInOrg(1L);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(commentRepository.findByTaskId(10L)).thenReturn(List.of(comment));

        assertThat(commentService.getCommentsForTask(10L, principal)).hasSize(1);
    }

    @Test
    void getCommentsForTask_otherOrganization_isDenied() {
        UserPrincipal principal = principalFor(1L);
        Task task = taskInOrg(2L);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> commentService.getCommentsForTask(10L, principal))
                .isInstanceOf(AccessDeniedException.class);
        verify(commentRepository, never()).findByTaskId(any());
    }

    @Test
    void deleteComment_notFound_throws() {
        UserPrincipal principal = principalFor(1L);
        when(commentRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment(3L, principal))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteComment_otherOrganization_isDenied() {
        UserPrincipal principal = principalFor(1L);
        Comment comment = commentInOrg(2L);
        when(commentRepository.findById(3L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(3L, principal))
                .isInstanceOf(AccessDeniedException.class);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_ownOrganization_isDeleted() {
        UserPrincipal principal = principalFor(1L);
        Comment comment = commentInOrg(1L);
        when(commentRepository.findById(3L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(3L, principal);

        verify(commentRepository).delete(comment);
    }
}