import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import {
  CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from "recharts";
import api from "../api";

const stamp = (t) => t.slice(5, 19).replace("T", " ");

export default function Progress() {
  const [params, setParams] = useSearchParams();
  const [roles, setRoles] = useState([]);
  const [points, setPoints] = useState(null);
  const [error, setError] = useState("");
  const [msg, setMsg] = useState("");
  const [busy, setBusy] = useState(false);
  const roleId = params.get("role");

  const load = (id) =>
    api.get(`/progress/history/${id}`)
      .then((r) => setPoints(r.data))
      .catch(() => setError("Could not load your history."));

  useEffect(() => {
    api.get("/roles")
      .then((r) => setRoles(r.data))
      .catch(() => setError("Could not load roles. Is the backend running?"));
  }, []);

  useEffect(() => {
    if (roleId) return;
    api.get("/analysis/ranking").then((r) => {
      if (r.data.length > 0) setParams({ role: r.data[0].roleId }, { replace: true });
    });
  }, [roleId, setParams]);

  useEffect(() => {
    if (!roleId) return;
    setPoints(null);
    setMsg("");
    load(roleId);
  }, [roleId]);

  const snapshot = async () => {
    setBusy(true);
    setMsg("");
    try {
      await api.post(`/progress/assess/${roleId}`);
      await load(roleId);
      setMsg("Snapshot saved.");
    } catch {
      setMsg("Could not save the snapshot.");
    }
    setBusy(false);
  };

  const data = points
    ? points.map((p) => ({ when: stamp(p.takenAt), Readiness: p.readiness, Alignment: p.alignment }))
    : [];

  let change = null;
  if (points && points.length >= 2) {
    change = points[points.length - 1].readiness - points[0].readiness;
  }

  return (
    <>
      <div className="head-row">
        <div>
          <h1>Progress</h1>
          <p>Save a snapshot now and then, and watch your scores climb.</p>
        </div>
        <select value={roleId || ""} onChange={(e) => setParams({ role: e.target.value })}>
          {roles.map((r) => (
            <option key={r.id} value={r.id}>{r.title}</option>
          ))}
        </select>
      </div>

      {error && <div className="error">{error}</div>}
      {!points && !error && <p>Loading your history...</p>}

      {points && (
        <>
          <div className="card">
            <div className="head-row">
              <div>
                <h2>Snapshots</h2>
                {change !== null && (
                  <p style={{ margin: 0 }}>
                    Since your first snapshot, readiness changed by{" "}
                    <strong>{change > 0 ? "+" : ""}{change.toFixed(1)}</strong> points.
                  </p>
                )}
              </div>
              <button className="btn" onClick={snapshot} disabled={busy}>
                {busy ? "Saving..." : "Save a snapshot now"}
              </button>
            </div>
            {msg && <div className="ok" style={{ marginTop: 14 }}>{msg}</div>}
          </div>

          {points.length === 0 ? (
            <div className="card empty">
              <p>No snapshots yet. Click the button above to record where you are today.</p>
            </div>
          ) : (
            <>
              <div className="card">
                <div style={{ width: "100%", height: 340 }}>
                  <ResponsiveContainer>
                    <LineChart data={data}>
                      <CartesianGrid stroke="#2a2a4a" />
                      <XAxis dataKey="when" tick={{ fill: "#9a98c4", fontSize: 11 }} />
                      <YAxis domain={[0, 100]} tick={{ fill: "#9a98c4", fontSize: 11 }} />
                      <Tooltip contentStyle={{ background: "#14142a", border: "1px solid #2a2a4a", borderRadius: 10 }} />
                      <Legend />
                      <Line type="monotone" dataKey="Readiness" stroke="#22d3ee" strokeWidth={3} />
                      <Line type="monotone" dataKey="Alignment" stroke="#8b5cf6" strokeWidth={3} />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </div>

              <div className="card">
                <table className="table">
                  <thead>
                    <tr><th>When</th><th>Readiness</th><th>Alignment</th></tr>
                  </thead>
                  <tbody>
                    {[...points].reverse().map((p, i) => (
                      <tr key={i}>
                        <td>{stamp(p.takenAt)}</td>
                        <td>{p.readiness.toFixed(1)}%</td>
                        <td>{p.alignment.toFixed(1)}%</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </>
      )}
    </>
  );
}
