import type { ReactNode } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../context/AuthContext";

export default function Layout({ children }: { children: ReactNode }) {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const handleLogout = () => {
    logout();
    queryClient.clear();
    navigate("/login");
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">Task Manager</div>
        <nav className="sidebar-nav">
          <NavLink to="/dashboard" className="nav-link">
            Dashboard
          </NavLink>
          <NavLink to="/projects" className="nav-link">
            Projects
          </NavLink>
          <NavLink to="/profile" className="nav-link">
            Profile
          </NavLink>
        </nav>
        <button className="sidebar-logout" onClick={handleLogout}>
          Log out
        </button>
      </aside>

      <div className="app-main">
        <header className="topbar">
          <div className="topbar-org">{user?.organizationName || "Organization"}</div>
          <div className="topbar-user">
            <span>
              {user?.firstName} {user?.lastName}
            </span>
            <span className="topbar-role">{isAdmin ? "Admin" : "Member"}</span>
          </div>
        </header>
        <main className="app-content">{children}</main>
      </div>
    </div>
  );
}
