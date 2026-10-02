import apiClient from "./client";
import type { Task, TaskInput } from "../types";

const clean = <T extends object>(obj: T) =>
  Object.fromEntries(
    Object.entries(obj).filter(([, v]) => v !== undefined && v !== null && v !== "")
  );

export const getTasksForProject = (projectId: number) =>
  apiClient.get<Task[]>("/tasks", { params: { projectId } }).then((r) => r.data);

export const createTask = (task: TaskInput) =>
  apiClient
    .post<Task>("/tasks", null, { params: clean(task) })
    .then((r) => r.data);

export const updateTask = (id: number, task: TaskInput) =>
  apiClient
    .put<Task>(`/tasks/${id}`, null, { params: clean({ ...task, projectId: undefined }) })
    .then((r) => r.data);

export const deleteTask = (id: number) =>
  apiClient.delete(`/tasks/${id}`).then((r) => r.data);
