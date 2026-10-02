import apiClient from "./client";
import type { Project } from "../types";

// NOTE: the backend takes name/description as query params (as in the original frontend).
// If Swagger shows a JSON request body instead, change `null, { params }` to `{ name, description }`.

export const getProjects = () =>
  apiClient.get<Project[]>("/projects").then((r) => r.data);

export const createProject = (name: string, description: string) =>
  apiClient
    .post<Project>("/projects", null, { params: { name, description } })
    .then((r) => r.data);

export const updateProject = (id: number, name: string, description: string) =>
  apiClient
    .put<Project>(`/projects/${id}`, null, { params: { name, description } })
    .then((r) => r.data);

export const deleteProject = (id: number) =>
  apiClient.delete(`/projects/${id}`).then((r) => r.data);
