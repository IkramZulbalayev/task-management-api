import type { BadgeTone } from "../components/ui";

export function priorityTone(priority?: string | null): BadgeTone {
  if (priority === "HIGH") return "danger";
  if (priority === "MEDIUM") return "warning";
  return "neutral";
}

export function formatDate(value?: string | null): string {
  if (!value) return "";
  const d = new Date(value);
  return Number.isNaN(d.getTime()) ? value : d.toLocaleString();
}

export function formatStatus(status: string): string {
  return status.replace(/_/g, " ");
}
