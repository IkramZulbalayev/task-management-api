import type { ReactElement } from "react";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Projects from "./pages/Projects";
import ProjectDetails from "./pages/ProjectDetails";
import TaskDetails from "./pages/TaskDetails";
import Profile from "./pages/Profile";

const guard = (page: ReactElement) => <ProtectedRoute>{page}</ProtectedRoute>;

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/dashboard" element={guard(<Dashboard />)} />
          <Route path="/projects" element={guard(<Projects />)} />
          <Route path="/projects/:id" element={guard(<ProjectDetails />)} />
          <Route path="/projects/:projectId/tasks/:id" element={guard(<TaskDetails />)} />
          <Route path="/profile" element={guard(<Profile />)} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
