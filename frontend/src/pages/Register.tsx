import { useState, type ChangeEvent, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { getErrorMessage } from "../api/client";
import { Button, ErrorBanner, Input } from "../components/ui";
import type { RegisterRequest } from "../types";

export default function Register() {
  const [form, setForm] = useState<RegisterRequest>({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    organizationName: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const update = (field: keyof RegisterRequest) => (e: ChangeEvent<HTMLInputElement>) =>
    setForm((f) => ({ ...f, [field]: e.target.value }));

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await register(form);
      navigate("/dashboard");
    } catch (err) {
      setError(getErrorMessage(err, "Registration failed."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1>Create your organization</h1>
        <p className="auth-subtitle">Registering creates a new organization with you as admin.</p>
        <ErrorBanner message={error} />

        <label>First name</label>
        <Input value={form.firstName} onChange={update("firstName")} required />
        <label>Last name</label>
        <Input value={form.lastName} onChange={update("lastName")} required />
        <label>Email</label>
        <Input type="email" value={form.email} onChange={update("email")} required />
        <label>Password</label>
        <Input type="password" value={form.password} onChange={update("password")} required />
        <label>Organization name</label>
        <Input value={form.organizationName} onChange={update("organizationName")} required />

        <Button type="submit" disabled={loading}>
          {loading ? "Creating account..." : "Create account"}
        </Button>

        <p className="auth-switch">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </form>
    </div>
  );
}
