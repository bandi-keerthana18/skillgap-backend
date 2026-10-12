import { createContext, useContext, useState } from "react";
import api from "./api";


const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem("user");
    return raw ? JSON.parse(raw) : null;
  });

  const save = (data) => {
    localStorage.setItem("token", data.token);
    const u = { name: data.name, email: data.email, role: data.role };
    localStorage.setItem("user", JSON.stringify(u));
    setUser(u);
  };

  const login = async (email, password) => {
    const { data } = await api.post("/auth/login", { email, password });
    save(data);
  };

  const register = async (name, email, password) => {
    const { data } = await api.post("/auth/register", { name, email, password });
    save(data);
  };

  const logout = () => {
    localStorage.clear();
    setUser(null);
  };

  return (

    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
