import { createContext, useContext, useEffect, useState } from "react";
import * as authApi from "../api/auth";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("shopsphere_user");
    return stored ? JSON.parse(stored) : null;
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // Keep localStorage in sync whenever `user` changes (login, logout).
    if (user) {
      localStorage.setItem("shopsphere_user", JSON.stringify(user));
    } else {
      localStorage.removeItem("shopsphere_user");
    }
  }, [user]);

  async function login(credentials) {
    setLoading(true);
    try {
      const auth = await authApi.login(credentials);
      localStorage.setItem("shopsphere_token", auth.accessToken);
      setUser({ id: auth.userId, fullName: auth.fullName, email: auth.email, role: auth.role });
      return auth;
    } finally {
      setLoading(false);
    }
  }

  async function register(details) {
    setLoading(true);
    try {
      const auth = await authApi.register(details);
      localStorage.setItem("shopsphere_token", auth.accessToken);
      setUser({ id: auth.userId, fullName: auth.fullName, email: auth.email, role: auth.role });
      return auth;
    } finally {
      setLoading(false);
    }
  }

  function logout() {
    localStorage.removeItem("shopsphere_token");
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
