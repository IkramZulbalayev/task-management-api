package com.project.taskmanager.task;

import com.project.taskmanager.project.Project;
import com.project.taskmanager.project.ProjectRepository;
import com.project.taskmanager.security.UserPrincipal;
import com.project.taskmanager.user.User;
import com.project.taskmanager.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public TaskResponse createTask(String title, String description, LocalDate dueDate,
                                   TaskPriority priority, Long assigneeId, Long projectId,
                                   UserPrincipal currentUser) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOrganization().getId().equals(currentUser.getUser().getOrganization().getId())) {
            throw new RuntimeException("Access denied");
        }

        User assignee = null;
        if (assigneeId != null) {
            assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
        }

        Task task = new Task(title, description, dueDate, project, assignee, currentUser.getUser());

        if (priority != null) {
            task.setPriority(priority);
        }

        Task savedTask = taskRepository.save(task);

        return toResponse(savedTask);
    }

    public List<TaskResponse> getTasksForProject(Long projectId, UserPrincipal currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOrganization().getId().equals(currentUser.getUser().getOrganization().getId())) {
            throw new RuntimeException("Access denied");
        }

        List<Task> tasks = taskRepository.findByProjectId(projectId);

        return tasks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse updateTask(Long id, String title, String description, LocalDate dueDate,
                                   TaskPriority priority, TaskStatus status, Long assigneeId,
                                   UserPrincipal currentUser) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getProject().getOrganization().getId().equals(currentUser.getUser().getOrganization().getId())) {
            throw new RuntimeException("Access denied");
        }

        if (title != null) {
            task.setTitle(title);
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (dueDate != null) {
            task.setDueDate(dueDate);
        }
        if (priority != null) {
            task.setPriority(priority);
        }
        if (status != null) {
            task.setStatus(status);
        }
        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            task.setAssignee(assignee);
        }

        Task savedTask = taskRepository.save(task);

        return toResponse(savedTask);
    }

    public void deleteTask(Long id, UserPrincipal currentUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getProject().getOrganization().getId().equals(currentUser.getUser().getOrganization().getId())) {
            throw new RuntimeException("Access denied");
        }

        taskRepository.delete(task);
    }

    private TaskResponse toResponse(Task task) {
        String assigneeName = task.getAssignee() != null
                ? task.getAssignee().getFirstName() + " " + task.getAssignee().getLastName()
                : null;
        String createdByName = task.getCreatedBy().getFirstName() + " " + task.getCreatedBy().getLastName();

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                assigneeName,
                createdByName,
                task.getCreatedAt()
        );
    }
}