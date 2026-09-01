package com.project.taskmanager.dashboard;

import com.project.taskmanager.project.Project;
import com.project.taskmanager.project.ProjectRepository;
import com.project.taskmanager.security.UserPrincipal;
import com.project.taskmanager.task.Task;
import com.project.taskmanager.task.TaskRepository;
import com.project.taskmanager.task.TaskStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public DashboardService(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    public DashboardResponse getDashboard(UserPrincipal currentUser) {
        Long organizationId = currentUser.getUser().getOrganization().getId();

        List<Project> myProjects = projectRepository.findByOrganizationId(organizationId);

        List<Task> tasks = myProjects.stream()
                .flatMap(project -> taskRepository.findByProjectId(project.getId()).stream())
                .collect(Collectors.toList());

        Map<String, Long> tasksByStatus = tasks.stream()
                .collect(Collectors.groupingBy(
                        task -> task.getStatus().toString(),
                        Collectors.counting()
                ));

        int totalTasks = tasks.size();

        int overdueCount = (int) tasks.stream()
                .filter(task -> task.getDueDate() != null
                        && task.getDueDate().isBefore(LocalDate.now())
                        && task.getStatus() != TaskStatus.DONE)
                .count();

        int totalProjects = myProjects.size();

        return new DashboardResponse(totalProjects, totalTasks, tasksByStatus, overdueCount);
    }
}