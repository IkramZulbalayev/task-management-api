import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import Layout from "../components/Layout";
import { Button, Card, ConfirmDialog, ErrorBanner, Input, Modal } from "../components/ui";
import { useAuth } from "../context/AuthContext";
import { getErrorMessage } from "../api/client";
import * as projectsApi from "../api/projects";
import type { Project } from "../types";

export default function Projects() {
  const { isAdmin } = useAuth();
  const navigate = useNavigate();
  const qc = useQueryClient();

  const [editing, setEditing] = useState<Project | "new" | null>(null);
  const [form, setForm] = useState({ name: "", description: "" });
  const [deleteTarget, setDeleteTarget] = useState<Project | null>(null);

  const { data: projects = [], error, isLoading } = useQuery({
    queryKey: ["projects"],
    queryFn: projectsApi.getProjects,
  });

  const done = () => {
    qc.invalidateQueries({ queryKey: ["projects"] });
    qc.invalidateQueries({ queryKey: ["dashboard"] });
  };

  const save = useMutation({
    mutationFn: () =>
      editing && editing !== "new"
        ? projectsApi.updateProject(editing.id, form.name, form.description)
        : projectsApi.createProject(form.name, form.description),
    onSuccess: () => {
      setEditing(null);
      done();
    },
  });

  const remove = useMutation({
    mutationFn: (id: number) => projectsApi.deleteProject(id),
    onSuccess: () => {
      setDeleteTarget(null);
      done();
    },
  });

  const openCreate = () => {
    setForm({ name: "", description: "" });
    save.reset();
    setEditing("new");
  };

  const openEdit = (p: Project) => {
    setForm({ name: p.name, description: p.description ?? "" });
    save.reset();
    setEditing(p);
  };

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    save.mutate();
  };

  return (
    <Layout>
      <div className="page-header">
        <h1 className="page-title">Projects</h1>
        {isAdmin && <Button onClick={openCreate}>New Project</Button>}
      </div>

      <ErrorBanner message={error ? getErrorMessage(error, "Could not load projects.") : null} />
      {isLoading && <p>Loading...</p>}

      <div className="card-grid">
        {projects.map((project) => (
          <Card key={project.id} className="project-card">
            <h3>{project.name}</h3>
            <p className="project-description">{project.description}</p>
            <div className="card-actions">
              <Button variant="ghost" onClick={() => navigate(`/projects/${project.id}`)}>
                Open
              </Button>
              {isAdmin && (
                <>
                  <Button variant="ghost" onClick={() => openEdit(project)}>
                    Edit
                  </Button>
                  <Button variant="danger-ghost" onClick={() => setDeleteTarget(project)}>
                    Delete
                  </Button>
                </>
              )}
            </div>
          </Card>
        ))}
        {!isLoading && projects.length === 0 && !error && (
          <p className="empty-state">No projects yet. {isAdmin && "Create one to get started."}</p>
        )}
      </div>

      {editing && (
        <Modal title={editing === "new" ? "New Project" : "Edit Project"} onClose={() => setEditing(null)}>
          <form onSubmit={handleSubmit}>
            <ErrorBanner message={save.error ? getErrorMessage(save.error, "Could not save project.") : null} />
            <label>Name</label>
            <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
            <label>Description</label>
            <Input
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
            <div className="modal-actions">
              <Button type="button" variant="ghost" onClick={() => setEditing(null)}>
                Cancel
              </Button>
              <Button type="submit" disabled={save.isPending}>
                {save.isPending ? "Saving..." : "Save"}
              </Button>
            </div>
          </form>
        </Modal>
      )}

      {deleteTarget && (
        <ConfirmDialog
          message={`Delete project "${deleteTarget.name}"? This cannot be undone.`}
          busy={remove.isPending}
          onConfirm={() => remove.mutate(deleteTarget.id)}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
      {remove.error && <ErrorBanner message={getErrorMessage(remove.error, "Could not delete project.")} />}
    </Layout>
  );
}
