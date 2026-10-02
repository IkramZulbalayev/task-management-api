import apiClient from "./client";
import type { AuthResponse, LoginRequest, RegisterRequest } from "../types";

export const login = (body: LoginRequest) =>
  apiClient.post<AuthResponse>("/auth/login", body).then((r) => r.data);

export const register = (body: RegisterRequest) =>
  apiClient.post<AuthResponse>("/auth/register", body).then((r) => r.data);
