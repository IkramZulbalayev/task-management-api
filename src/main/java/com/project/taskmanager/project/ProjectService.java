package com.project.taskmanager.project;

import com.project.taskmanager.exception.AccessDeniedException;
import com.project.taskmanager.exception.ProjectNotEmptyException;
import com.project.taskmanager.exception.ResourceNotFoundException;
import com.project.taskmanager.security.UserPrincipal;
import com.project.taskmanager.task.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public ProjectService(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public ProjectResponse createProject(String name, String description, UserPrincipal currentUser){
        Project project = new Project(
                name,
                description,
                currentUser.getUser().getOrganization(),
                currentUser.getUser()
        );

        Project savedProject = projectRepository.save(project);

        return toResponse(savedProject);
    }

    public List<ProjectResponse> getProjectsForCurrentOrg(UserPrincipal currentUser){

        List<Project> projects = projectRepository.findByOrganizationId(currentUser.getUser().getOrganization().getId());

        return projects.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse updateProject(Long id, String name, String description, UserPrincipal currentUser) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!project.getOrganization().getId().equals(currentUser.getUser().getOrganization().getId())) {
            throw new AccessDeniedException("Access denied");
        }

        if (name != null) {
            project.setName(name);
        }
        if (description != null) {
            project.setDescription(description);
        }

        Project savedProject = projectRepository.save(project);

        return toResponse(savedProject);
    }

    public void deleteProject(Long id, UserPrincipal currentUser) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!project.getOrganization().getId().equals(currentUser.getUser().getOrganization().getId())) {
            throw new AccessDeniedException("Access denied");
        }

        if (taskRepository.existsByProjectId(id)) {
            throw new ProjectNotEmptyException("Cannot delete project with existing tasks");
        }

        projectRepository.delete(project);
    }

    private ProjectResponse toResponse(Project project){
        String createdByName = project.getCreatedBy().getFirstName() + " " + project.getCreatedBy().getLastName();

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                createdByName
        );
    }
}