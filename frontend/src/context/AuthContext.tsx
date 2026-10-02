import { createContext, useContext, useState, type ReactNode } from "react";
import * as authApi from "../api/auth";
import { TOKEN_KEY, USER_KEY } from "../api/client";
import type { AuthResponse, LoginRequest, RegisterRequest, SessionUser } from "../types";

interface AuthContextValue {
  user: SessionUser | null;
  isAdmin: boolean;
  login: (body: LoginRequest) => Promise<void>;
  register: (body: RegisterRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function readStoredUser(): SessionUser | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as SessionUser) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<SessionUser | null>(readStoredUser);

  const persist = (data: AuthResponse) => {
    const { token, ...session } = data;
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(session));
    setUser(session);
  };

  const login = async (body: LoginRequest) => persist(await authApi.login(body));
  const register = async (body: RegisterRequest) => persist(await authApi.register(body));

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setUser(null);
  };

  const isAdmin = user?.role === "ADMIN";

  return (
    <AuthContext.Provider value={{ user, isAdmin, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}
