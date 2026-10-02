import { useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import Layout from "../components/Layout";
import TaskFormModal from "../components/TaskFormModal";
import { Badge, Button, Card, ConfirmDialog, ErrorBanner, Input } from "../components/ui";
import { useAuth } from "../context/AuthContext";
import { getErrorMessage } from "../api/client";
import * as tasksApi from "../api/tasks";
import * as commentsApi from "../api/comments";
import type { TaskInput } from "../types";
import { formatDate, formatStatus, priorityTone } from "../utils/format";

export default function TaskDetails() {
  const params = useParams();
  const projectId = Number(params.projectId);
  const taskId = Number(params.id);
  const { isAdmin } = useAuth();
  const navigate = useNavigate();
  const qc = useQueryClient();

  const [newComment, setNewComment] = useState("");
  const [showEdit, setShowEdit] = useState(false);
  const [showDelete, setShowDelete] = useState(false);

  // The API has no "get one task" endpoint, so the task is found in its project's list.
  const { data: tasks = [], isLoading: tasksLoading, error: tasksError } = useQuery({
    queryKey: ["tasks", projectId],
    queryFn: () => tasksApi.getTasksForProject(projectId),
  });
  const task = tasks.find((t) => t.id === taskId);

  const { data: comments = [], error: commentsError } = useQuery({
    queryKey: ["comments", taskId],
    queryFn: () => commentsApi.getCommentsForTask(taskId),
  });

  const refreshTasks = () => {
    qc.invalidateQueries({ queryKey: ["tasks", projectId] });
    qc.invalidateQueries({ queryKey: ["dashboard"] });
  };

  const update = useMutation({
    mutationFn: (values: TaskInput) => tasksApi.updateTask(taskId, values),
    onSuccess: () => {
      setShowEdit(false);
      refreshTasks();
    },
  });

  const remove = useMutation({
    mutationFn: () => tasksApi.deleteTask(taskId),
    onSuccess: () => {
      refreshTasks();
      navigate(`/projects/${projectId}`);
    },
  });

  const addComment = useMutation({
    mutationFn: (content: string) => commentsApi.createComment(content, taskId),
    onSuccess: () => {
      setNewComment("");
      qc.invalidateQueries({ queryKey: ["comments", taskId] });
    },
  });

  const removeComment = useMutation({
    mutationFn: (id: number) => commentsApi.deleteComment(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["comments", taskId] }),
  });

  const handleAddComment = (e: FormEvent) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    addComment.mutate(newComment.trim());
  };

  return (
    <Layout>
      <p className="breadcrumb">
        <Link to="/projects">Projects</Link> / <Link to={`/projects/${projectId}`}>Project</Link> / Task
      </p>

      <ErrorBanner message={tasksError ? getErrorMessage(tasksError, "Could not load task.") : null} />
      {tasksLoading && <p>Loading...</p>}
      {!tasksLoading && !task && !tasksError && <p className="empty-state">Task not found.</p>}

      {task && (
        <Card className="task-detail-card">
          <div className="page-header">
            <h1 className="page-title">{task.title}</h1>
            {isAdmin && (
              <div className="card-actions">
                <Button
                  variant="ghost"
                  onClick={() => {
                    update.reset();
                    setShowEdit(true);
                  }}
                >
                  Edit
                </Button>
                <Button variant="danger-ghost" onClick={() => setShowDelete(true)}>
                  Delete
                </Button>
              </div>
            )}
          </div>
          {task.description && <p>{task.description}</p>}
          <div className="profile-row">
            <span className="profile-label">Status</span>
            <Badge tone="accent">{formatStatus(task.status)}</Badge>
          </div>
          <div className="profile-row">
            <span className="profile-label">Priority</span>
            {task.priority ? <Badge tone={priorityTone(task.priority)}>{task.priority}</Badge> : "—"}
          </div>
          <div className="profile-row">
            <span className="profile-label">Due date</span>
            <span>{task.dueDate ? task.dueDate.slice(0, 10) : "—"}</span>
          </div>
          <div className="profile-row">
            <span className="profile-label">Assignee</span>
            <span>{task.assigneeName ?? "Unassigned"}</span>
          </div>
        </Card>
      )}

      <Card className="comments-card">
        <h3>Comments</h3>
        <ErrorBanner message={commentsError ? getErrorMessage(commentsError, "Could not load comments.") : null} />
        <ErrorBanner
          message={removeComment.error ? getErrorMessage(removeComment.error, "Could not delete comment.") : null}
        />

        <div className="comment-list">
          {comments.map((comment) => (
            <div className="comment" key={comment.id}>
              <div className="comment-header">
                <span className="comment-author">{comment.authorName ?? "Unknown"}</span>
                {comment.createdAt && <span className="comment-date">{formatDate(comment.createdAt)}</span>}
                {isAdmin && (
                  <button className="comment-delete" onClick={() => removeComment.mutate(comment.id)}>
                    Delete
                  </button>
                )}
              </div>
              <p>{comment.content}</p>
            </div>
          ))}
          {comments.length === 0 && <p className="empty-state">No comments yet.</p>}
        </div>

        <ErrorBanner
          message={addComment.error ? getErrorMessage(addComment.error, "Could not post comment.") : null}
        />
        <form className="comment-form" onSubmit={handleAddComment}>
          <Input
            placeholder="Add a comment..."
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
          />
          <Button type="submit" disabled={addComment.isPending}>
            Post
          </Button>
        </form>
      </Card>

      {showEdit && task && (
        <TaskFormModal
          task={task}
          onClose={() => setShowEdit(false)}
          onSubmit={(v) => update.mutate({ ...v, assigneeId: task.assigneeId })}
          busy={update.isPending}
          error={update.error ? getErrorMessage(update.error, "Could not update task.") : null}
        />
      )}

      {showDelete && (
        <ConfirmDialog
          message="Delete this task? This cannot be undone."
          busy={remove.isPending}
          onConfirm={() => remove.mutate()}
          onCancel={() => setShowDelete(false)}
        />
      )}
      {remove.error && <ErrorBanner message={getErrorMessage(remove.error, "Could not delete task.")} />}
    </Layout>
  );
}
