import apiClient from "./client";
import type { Comment } from "../types";

export const getCommentsForTask = (taskId: number) =>
  apiClient.get<Comment[]>("/comments", { params: { taskId } }).then((r) => r.data);

export const createComment = (content: string, taskId: number) =>
  apiClient
    .post<Comment>("/comments", null, { params: { content, taskId } })
    .then((r) => r.data);

export const deleteComment = (id: number) =>
  apiClient.delete(`/comments/${id}`).then((r) => r.data);
