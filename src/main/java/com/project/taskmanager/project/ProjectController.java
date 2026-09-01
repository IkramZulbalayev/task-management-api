package com.project.taskmanager.project;

import com.project.taskmanager.security.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponse> getProjects(@AuthenticationPrincipal UserPrincipal currentUser) {
        return projectService.getProjectsForCurrentOrg(currentUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ProjectResponse createProject(@RequestParam String name,
                                         @RequestParam(required = false) String description,
                                         @AuthenticationPrincipal UserPrincipal currentUser) {
        return projectService.createProject(name, description, currentUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ProjectResponse updateProject(@PathVariable Long id,
                                         @RequestParam(required = false) String name,
                                         @RequestParam(required = false) String description,
                                         @AuthenticationPrincipal UserPrincipal currentUser) {
        return projectService.updateProject(id, name, description, currentUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable Long id,
                              @AuthenticationPrincipal UserPrincipal currentUser) {
        projectService.deleteProject(id, currentUser);
    }
}