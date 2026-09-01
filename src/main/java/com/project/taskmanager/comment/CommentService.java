package com.project.taskmanager.comment;

import com.project.taskmanager.exception.AccessDeniedException;
import com.project.taskmanager.exception.ResourceNotFoundException;
import com.project.taskmanager.security.UserPrincipal;
import com.project.taskmanager.task.Task;
import com.project.taskmanager.task.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
    }

    public CommentResponse createComment(String content, Long taskId, UserPrincipal currentUser){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!task.getProject().getOrganization().getId().equals(currentUser.getUser().getOrganization().getId())){
            throw new AccessDeniedException("Access denied");
        }

        Comment comment = new Comment(content, task, currentUser.getUser());
        Comment savedComment = commentRepository.save(comment);

        return toResponse(savedComment);
    }

    public List<CommentResponse> getCommentsForTask(Long taskId, UserPrincipal currentUser){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if(!task.getProject().getOrganization().getId().equals(currentUser.getUser().getOrganization().getId())){
            throw new AccessDeniedException("Access denied");
        }

        List<Comment> comments = commentRepository.findByTaskId(taskId);

        return comments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteComment(Long id, UserPrincipal currentUser) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getTask().getProject().getOrganization().getId().equals(currentUser.getUser().getOrganization().getId())) {
            throw new AccessDeniedException("Access denied");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse toResponse(Comment comment){
        String authorName = comment.getAuthor().getFirstName() + " " + comment.getAuthor().getLastName();

        return new CommentResponse(
                comment.getId(),
                authorName,
                comment.getCreatedAt(),
                comment.getContent()
        );
    }
}