import { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../auth.jsx";

export default function Login() {
  const { user, login, register } = useAuth();
  const nav = useNavigate();
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      if (mode === "login") await login(form.email, form.password);
      else await register(form.name, form.email, form.password);
      nav("/");
    } catch (err) {
      const s = err.response && err.response.status;
      if (s === 401) setError("Invalid email or password.");
      else if (s === 409) setError("This email is already registered.");
      else if (s === 400) setError("Please check your details (password needs at least 6 characters).");
      else setError("Cannot reach the server. Is the backend running?");
    }
    setBusy(false);
  };

  return (
    <div className="auth-wrap">
      <div className="card auth-card">
        <div className="brand">&#9670; SkillGap</div>
        <div className="auth-title">Find your fit.</div>
        <p>See how your skills match real careers and what to learn next.</p>

        <div className="tabs">
          <button className={mode === "login" ? "on" : ""} onClick={() => setMode("login")} type="button">Log in</button>
          <button className={mode === "register" ? "on" : ""} onClick={() => setMode("register")} type="button">Sign up</button>
        </div>

        {error && <div className="error">{error}</div>}

        <form onSubmit={submit}>
          {mode === "register" && (
            <>
              <label>Name</label>
              <input type="text" value={form.name} onChange={set("name")} required />
            </>
          )}
          <label>Email</label>
          <input type="email" value={form.email} onChange={set("email")} required />
          <label>Password</label>
          <input type="password" value={form.password} onChange={set("password")} required minLength={6} />
          <button className="btn" style={{ width: "100%" }} disabled={busy}>
            {busy ? "Please wait..." : mode === "login" ? "Log in" : "Create account"}
          </button>
        </form>
      </div>
    </div>
  );
}
