import { useState } from "react";
import { Button, ErrorBanner, Input, Modal, Select } from "./ui";
import { TASK_PRIORITIES, TASK_STATUSES, type Task, type TaskInput } from "../types";

interface Props {
  task?: Task;
  onClose: () => void;
  onSubmit: (values: TaskInput) => void;
  busy?: boolean;
  error?: string | null;
}

export default function TaskFormModal({ task, onClose, onSubmit, busy, error }: Props) {
  const [form, setForm] = useState({
    title: task?.title ?? "",
    description: task?.description ?? "",
    dueDate: task?.dueDate ? task.dueDate.slice(0, 10) : "",
    priority: task?.priority ?? "MEDIUM",
    status: task?.status ?? "TODO",
  });

  const set = (field: keyof typeof form) => (value: string) =>
    setForm((f) => ({ ...f, [field]: value }));

  return (
    <Modal title={task ? "Edit Task" : "New Task"} onClose={onClose}>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          onSubmit(form);
        }}
      >
        <ErrorBanner message={error} />
        <label>Title</label>
        <Input value={form.title} onChange={(e) => set("title")(e.target.value)} required />
        <label>Description</label>
        <Input value={form.description} onChange={(e) => set("description")(e.target.value)} />
        <label>Due date</label>
        <Input type="date" value={form.dueDate} onChange={(e) => set("dueDate")(e.target.value)} />
        <label>Priority</label>
        <Select value={form.priority} onChange={(e) => set("priority")(e.target.value)}>
          {TASK_PRIORITIES.map((p) => (
            <option key={p} value={p}>
              {p}
            </option>
          ))}
        </Select>
        {task && (
          <>
            <label>Status</label>
            <Select value={form.status} onChange={(e) => set("status")(e.target.value)}>
              {TASK_STATUSES.map((s) => (
                <option key={s} value={s}>
                  {s.replace("_", " ")}
                </option>
              ))}
            </Select>
          </>
        )}
        <div className="modal-actions">
          <Button type="button" variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" disabled={busy}>
            {busy ? "Saving..." : task ? "Save" : "Create"}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
