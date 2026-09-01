package com.project.taskmanager.comment;

import com.project.taskmanager.security.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public CommentResponse createComment(@RequestParam String content,
                                         @RequestParam Long taskId,
                                         @AuthenticationPrincipal UserPrincipal currentUser){

        return commentService.createComment(content, taskId, currentUser);
    }

    @GetMapping
    public List<CommentResponse> getCommentsForTask(@RequestParam Long taskId,
                                                    @AuthenticationPrincipal UserPrincipal currentUser){

        return commentService.getCommentsForTask(taskId, currentUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable Long id,
                              @AuthenticationPrincipal UserPrincipal currentUser) {
        commentService.deleteComment(id, currentUser);
    }
}