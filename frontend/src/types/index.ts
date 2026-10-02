// ASSUMPTION: field names below are guesses. Compare with Swagger / Network tab
// and edit ONLY this file — the compiler will point out everything else that breaks.

export type Role = "ADMIN" | "USER" | "MEMBER" | string;

export const TASK_STATUSES = ["TODO", "IN_PROGRESS", "DONE"] as const;
export const TASK_PRIORITIES = ["LOW", "MEDIUM", "HIGH"] as const;

export type TaskStatus = string;
export type TaskPriority = string;

export interface SessionUser {
  firstName: string;
  lastName: string;
  email: string;
  role: Role;
  organizationName: string;
}

export interface AuthResponse extends SessionUser {
  token: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  organizationName: string;
}

export interface Project {
  id: number;
  name: string;
  description?: string | null;
}

export interface Task {
  id: number;
  title: string;
  description?: string | null;
  status: TaskStatus;
  priority?: TaskPriority | null;
  dueDate?: string | null;
  assigneeId?: number | null;
  assigneeName?: string | null;
  projectId?: number;
}

export interface TaskInput {
  title: string;
  description?: string;
  dueDate?: string;
  priority?: string;
  status?: string;
  assigneeId?: number | null;
  projectId?: number;
}

export interface Comment {
  id: number;
  content: string;
  authorName?: string | null;
  createdAt?: string | null;
}

export interface DashboardStats {
  totalProjects?: number;
  totalTasks?: number;
  completedTasks?: number;
  inProgressTasks?: number;
  overdueTasks?: number;
  tasksByStatus?: Record<string, number>;
}
