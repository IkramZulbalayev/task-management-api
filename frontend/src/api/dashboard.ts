import apiClient from "./client";
import type { DashboardStats } from "../types";

export const getDashboard = () =>
  apiClient.get<DashboardStats>("/dashboard").then((r) => r.data);
