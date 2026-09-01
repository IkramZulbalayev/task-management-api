package com.project.taskmanager.task;


import com.project.taskmanager.security.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public TaskResponse createTask(@RequestParam String title,
                                   @RequestParam(required = false) String description,
                                   @RequestParam(required = false)LocalDate dueDate,
                                   @RequestParam(required = false) TaskPriority priority,
                                   @RequestParam(required = false) Long assigneeId,
                                   @RequestParam Long projectId,
                                   @AuthenticationPrincipal UserPrincipal currentUser){

        return  taskService.createTask(title, description, dueDate, priority, assigneeId, projectId, currentUser);
    }

    @GetMapping
    public List<TaskResponse> getTasksForProject(@RequestParam Long projectId,
                                                 @AuthenticationPrincipal UserPrincipal currentUser){
        return taskService.getTasksForProject(projectId, currentUser);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable Long id,
                                   @RequestParam(required = false) String title,
                                   @RequestParam(required = false) String description,
                                   @RequestParam(required = false) LocalDate dueDate,
                                   @RequestParam(required = false) TaskPriority priority,
                                   @RequestParam(required = false) TaskStatus status,
                                   @RequestParam(required = false) Long assigneeId,
                                   @AuthenticationPrincipal UserPrincipal currentUser) {
        return taskService.updateTask(id, title, description, dueDate, priority, status, assigneeId, currentUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id,
                           @AuthenticationPrincipal UserPrincipal currentUser) {
        taskService.deleteTask(id, currentUser);
    }

}
