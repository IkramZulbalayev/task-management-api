import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import Layout from "../components/Layout";
import TaskFormModal from "../components/TaskFormModal";
import { Badge, Button, Card, ErrorBanner, Select } from "../components/ui";
import { useAuth } from "../context/AuthContext";
import { getErrorMessage } from "../api/client";
import * as tasksApi from "../api/tasks";
import * as projectsApi from "../api/projects";
import { TASK_STATUSES, type Task, type TaskInput } from "../types";
import { formatStatus, priorityTone } from "../utils/format";

export default function ProjectDetails() {
  const projectId = Number(useParams().id);
  const { isAdmin } = useAuth();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);

  const { data: projects = [] } = useQuery({
    queryKey: ["projects"],
    queryFn: projectsApi.getProjects,
  });
  const project = projects.find((p) => p.id === projectId);

  const { data: tasks = [], error, isLoading } = useQuery({
    queryKey: ["tasks", projectId],
    queryFn: () => tasksApi.getTasksForProject(projectId),
  });

  const refresh = () => {
    qc.invalidateQueries({ queryKey: ["tasks", projectId] });
    qc.invalidateQueries({ queryKey: ["dashboard"] });
  };

  const create = useMutation({
    mutationFn: (values: TaskInput) => tasksApi.createTask({ ...values, projectId }),
    onSuccess: () => {
      setShowCreate(false);
      refresh();
    },
  });

  const changeStatus = useMutation({
    mutationFn: ({ task, status }: { task: Task; status: string }) =>
      tasksApi.updateTask(task.id, {
        title: task.title,
        description: task.description ?? undefined,
        dueDate: task.dueDate ?? undefined,
        priority: task.priority ?? undefined,
        assigneeId: task.assigneeId ?? undefined,
        status,
      }),
    onSuccess: refresh,
  });

  return (
    <Layout>
      <p className="breadcrumb">
        <Link to="/projects">Projects</Link> / {project?.name ?? `#${projectId}`}
      </p>
      <div className="page-header">
        <h1 className="page-title">{project?.name ?? "Project"}</h1>
        {isAdmin && (
          <Button
            onClick={() => {
              create.reset();
              setShowCreate(true);
            }}
          >
            New Task
          </Button>
        )}
      </div>
      {project?.description && <p className="project-description">{project.description}</p>}

      <ErrorBanner message={error ? getErrorMessage(error, "Could not load tasks.") : null} />
      <ErrorBanner
        message={changeStatus.error ? getErrorMessage(changeStatus.error, "Could not update task.") : null}
      />
      {isLoading && <p>Loading...</p>}

      <div className="kanban">
        {TASK_STATUSES.map((status) => (
          <div className="kanban-column" key={status}>
            <div className="kanban-column-title">{formatStatus(status)}</div>
            {tasks
              .filter((t) => t.status === status)
              .map((task) => (
                <Card key={task.id} className="task-card" onClick={() => navigate(`/projects/${projectId}/tasks/${task.id}`)}>
                  <div className="task-title">{task.title}</div>
                  {task.priority && <Badge tone={priorityTone(task.priority)}>{task.priority}</Badge>}
                  {task.dueDate && <div className="task-due">Due {task.dueDate.slice(0, 10)}</div>}
                  {task.assigneeName && <div className="task-assignee">{task.assigneeName}</div>}
                  <Select
                    value={task.status}
                    onClick={(e) => e.stopPropagation()}
                    onChange={(e) => changeStatus.mutate({ task, status: e.target.value })}
                  >
                    {TASK_STATUSES.map((s) => (
                      <option key={s} value={s}>
                        {formatStatus(s)}
                      </option>
                    ))}
                  </Select>
                </Card>
              ))}
          </div>
        ))}
      </div>

      {showCreate && (
        <TaskFormModal
          onClose={() => setShowCreate(false)}
          onSubmit={(v) => create.mutate(v)}
          busy={create.isPending}
          error={create.error ? getErrorMessage(create.error, "Could not create task.") : null}
        />
      )}
    </Layout>
  );
}
