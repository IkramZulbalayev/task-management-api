package com.project.taskmanager.dashboard;

import java.util.Map;

public class DashboardResponse {

    private Integer totalProjects;
    private Integer totalTasks;
    private Map<String, Long> tasksByStatus;
    private Integer overdueCount;

    public DashboardResponse(){

    }

    public DashboardResponse(Integer totalProjects, Integer totalTasks, Map<String, Long> tasksByStatus, Integer overdueCount) {
        this.totalProjects = totalProjects;
        this.totalTasks = totalTasks;
        this.tasksByStatus = tasksByStatus;
        this.overdueCount = overdueCount;
    }

    public Integer getTotalProjects() {
        return totalProjects;
    }

    public Integer getTotalTasks() {
        return totalTasks;
    }

    public Map<String, Long> getTasksByStatus() {
        return tasksByStatus;
    }

    public Integer getOverdueCount() {
        return overdueCount;
    }
}