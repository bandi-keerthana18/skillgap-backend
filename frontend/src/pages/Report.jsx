import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import api from "../api";
import { useAuth } from "../auth.jsx";

export default function Report() {
  const { user } = useAuth();
  const [params, setParams] = useSearchParams();
  const [roles, setRoles] = useState([]);
  const [ranking, setRanking] = useState(null);
  const [gap, setGap] = useState(null);
  const [road, setRoad] = useState(null);
  const [error, setError] = useState("");
  const roleId = params.get("role");

  useEffect(() => {
    Promise.all([api.get("/roles"), api.get("/analysis/ranking")])
      .then(([a, b]) => {
        setRoles(a.data);
        setRanking(b.data);
        if (!roleId && b.data.length > 0) setParams({ role: b.data[0].roleId }, { replace: true });
      })
      .catch(() => setError("Could not load the report. Is the backend running?"));
  }, []);

  useEffect(() => {
    if (!roleId) return;
    setGap(null);
    setRoad(null);
    Promise.all([api.get(`/analysis/${roleId}`), api.get(`/roadmap/${roleId}`)])
      .then(([g, r]) => {

        setGap(g.data);
        setRoad(r.data);
      })
      .catch(() => setError("Could not load the report."));
  }, [roleId]);

  const items = gap ? [...gap.missing, ...gap.weak, ...gap.strong] : [];

  return (
    <>
      <div className="head-row no-print">
        <div>
          <h1>My Career Report</h1>
          <p>Choose a role, then print it or save it as a PDF.</p>
        </div>
        <div className="stat-row" style={{ marginTop: 0 }}>
          <select value={roleId || ""} onChange={(e) => setParams({ role: e.target.value })}>
            {roles.map((r) => (
              <option key={r.id} value={r.id}>{r.title}</option>
            ))}
          </select>
          <button className="btn" onClick={() => window.print()}>Print / Save as PDF</button>
        </div>
      </div>

      {error && <div className="error">{error}</div>}

      <div className="card">
        <h2>Skill Gap and Career Alignment Report</h2>
        <p style={{ margin: 0 }}>
          Student: {user.name} ({user.email}) | Date: {new Date().toLocaleDateString()}
        </p>

      </div>

      {ranking && (
        <div className="card">
          <h2>1. Career matches</h2>
          <table className="table">
            <thead>
              <tr><th>#</th><th>Career</th><th>Alignment</th><th>Readiness</th></tr>
            </thead>
            <tbody>
              {ranking.map((r, i) => (
                <tr key={r.roleId}>
                  <td>{i + 1}</td>
                  <td>{r.title}</td>
                  <td>{r.alignmentPercent.toFixed(1)}%</td>
                  <td>{r.readinessPercent.toFixed(1)}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {gap && (
        <div className="card">
          <h2>2. Skill gap for {gap.roleTitle}</h2>
          <div className="stat-row" style={{ marginTop: 0, marginBottom: 12 }}>
            <span className="pill">{gap.alignmentScore.toFixed(1)}% alignment</span>
            <span className="pill cyan">{gap.readinessScore.toFixed(1)}% ready</span>
          </div>
          <table className="table">
            <thead>

              <tr><th>Skill</th><th>Your level</th><th>Needed</th><th>Status</th></tr>
            </thead>
            <tbody>
              {items.map((i) => (
                <tr key={i.skillId}>
                  <td>{i.skill}</td>
                  <td>{i.studentLevel}</td>
                  <td>{i.requiredLevel}</td>
                  <td>{i.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {road && (
        <div className="card">
          <h2>3. Learning roadmap</h2>
          {road.steps.length === 0 ? (
            <p>You meet every requirement for this role.</p>
          ) : (
            <table className="table">
              <thead>
                <tr><th>Step</th><th>Skill</th><th>Level</th><th>Builds on</th></tr>
              </thead>
              <tbody>
                {road.steps.map((s) => (
                  <tr key={s.skillId}>
                    <td>{s.step}</td>
                    <td>{s.skill}</td>
                    <td>{s.currentLevel} to {s.targetLevel}</td>

                    <td>{s.prerequisites.join(", ")}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </>
  );
}
