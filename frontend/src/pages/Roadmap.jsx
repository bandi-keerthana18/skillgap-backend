import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import api from "../api";
import Ring from "../components/Ring.jsx";

export default function Roadmap() {
  const [params, setParams] = useSearchParams();
  const [roles, setRoles] = useState([]);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(null);
  const [toast, setToast] = useState("");
  const roleId = params.get("role");

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

  const load = (id) =>
    api.get(`/roadmap/${id}`)
      .then((r) => setData(r.data))
      .catch(() => setError("Could not load the roadmap."));

  useEffect(() => {
    if (!roleId) return;
    setData(null);
    load(roleId);
  }, [roleId]);

  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => setToast(""), 4000);
    return () => clearTimeout(t);
  }, [toast]);

  const learn = async (step) => {
    setBusy(step.skillId);
    try {
      const res = await api.post("/progress/learn", {
        skillId: step.skillId,
        level: step.targetLevel,
      });
      setToast(`${res.data.skill} learned! +${res.data.xpGained} XP (total ${res.data.totalXp} XP)`);
      await load(roleId);
    } catch {
      setToast("Could not save. Please try again.");

    }
    setBusy(null);
  };

  return (
    <>
      <div className="head-row">
        <div>
          <h1>Learning Roadmap</h1>
          <p>Do these in order. Each step unlocks the next.</p>
        </div>
        <select value={roleId || ""} onChange={(e) => setParams({ role: e.target.value })}>
          {roles.map((r) => (
            <option key={r.id} value={r.id}>{r.title}</option>
          ))}
        </select>
      </div>

      {error && <div className="error">{error}</div>}
      {!data && !error && <p>Building your roadmap...</p>}

      {data && (
        <>
          <div className="card hero">
            <Ring value={data.readinessScore} label="readiness" size={120} />
            <div className="hero-info">
              <div className="big">{data.roleTitle}</div>
              <p style={{ margin: "6px 0 0" }}>
                {data.totalSteps === 0
                  ? "You meet every requirement for this role."
                  : `${data.totalSteps} skills to work on.`}
              </p>

            </div>
          </div>

          {data.steps.map((s) => (
            <div className="card step" key={s.skillId}>
              <div className="step-no">{s.step}</div>
              <div className="step-body">
                <div className="step-head">
                  <span className="step-title">{s.skill}</span>
                  <span className={"tag " + s.status.toLowerCase()}>{s.status}</span>
                  <span className="tag plain">Level {s.currentLevel} to {s.targetLevel}</span>
                </div>
                {s.prerequisites.length > 0 && (
                  <div className="needs">Builds on: {s.prerequisites.join(", ")}</div>
                )}
                <div className="chips">
                  {s.resources.map((r) => (
                    <a className="chip" key={r.url} href={r.url} target="_blank" rel="noreferrer">
                      {r.type}: {r.title}
                    </a>
                  ))}
                </div>
                <button className="btn" disabled={busy === s.skillId} onClick={() => learn(s)}>
                  {busy === s.skillId ? "Saving..." : `Mark as learned (level ${s.targetLevel})`}
                </button>
              </div>
            </div>
          ))}
        </>
      )}

      {toast && <div className="toast">{toast}</div>}

    </>
  );
}
