import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import api from "../api";
import Ring from "../components/Ring.jsx";

function Delta({ value }) {
  const cls = value > 0 ? "up" : value < 0 ? "down" : "flat";
  return (
    <span className={"delta " + cls}>
      {value > 0 ? "+" : ""}{value.toFixed(1)}
    </span>
  );
}

export default function WhatIf() {
  const [params, setParams] = useSearchParams();
  const [roles, setRoles] = useState([]);
  const [base, setBase] = useState(null);
  const [levels, setLevels] = useState({});
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
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

  useEffect(() => {
    if (!roleId) return;
    setBase(null);
    setResult(null);
    api.get(`/analysis/${roleId}`)
      .then((r) => {
        setBase(r.data);
        const m = {};
        [...r.data.missing, ...r.data.weak, ...r.data.strong].forEach((i) => {
          m[i.skillId] = i.studentLevel;
        });
        setLevels(m);
      })
      .catch(() => setError("Could not load this role."));
  }, [roleId]);

  useEffect(() => {
    if (!base) return;
    const all = [...base.missing, ...base.weak, ...base.strong];
    const changes = all
      .filter((i) => levels[i.skillId] !== undefined && levels[i.skillId] !== i.studentLevel)
      .map((i) => ({ skillId: i.skillId, level: levels[i.skillId] }));
    const t = setTimeout(() => {
      api.post(`/analysis/${roleId}/whatif`, changes)
        .then((r) => setResult(r.data))
        .catch(() => setError("Could not run the simulation."));
    }, 250);
    return () => clearTimeout(t);
  }, [levels, base, roleId]);

  const items = base ? [...base.missing, ...base.weak, ...base.strong] : [];

  const reset = () => {
    const m = {};
    items.forEach((i) => { m[i.skillId] = i.studentLevel; });
    setLevels(m);
  };

  return (
    <>
      <div className="head-row">
        <div>
          <h1>What-if Simulator</h1>
          <p>Change the levels below to see how your scores would move. Nothing here is saved.</p>
        </div>
        <select value={roleId || ""} onChange={(e) => setParams({ role: e.target.value })}>
          {roles.map((r) => (
            <option key={r.id} value={r.id}>{r.title}</option>
          ))}
        </select>
      </div>

      {error && <div className="error">{error}</div>}
      {!base && !error && <p>Loading...</p>}

      {base && (
        <>
          <div className="card">
            <div className="compare">
              <Ring value={result ? result.readinessBefore : base.readinessScore} label="now" size={130} />
              <span className="arrow">&#8594;</span>
              <Ring value={result ? result.readinessAfter : base.readinessScore} label="what-if" size={130} />
              <div>
                <div className="rank-sub">Readiness change</div>
                <Delta value={result ? result.readinessChange : 0} />
                <div className="rank-sub" style={{ marginTop: 12 }}>Alignment change</div>
                <Delta value={result ? result.alignmentChange : 0} />
                {result && (
                  <div className="rank-sub">
                    Alignment {result.alignmentBefore.toFixed(1)}% to {result.alignmentAfter.toFixed(1)}%
                  </div>
                )}
              </div>
            </div>
          </div>

          <div className="card">
            <div className="head-row">
              <h2>{base.roleTitle}: skills this role needs</h2>
              <button className="btn ghost" onClick={reset}>Reset</button>
            </div>
            {items.map((i) => (
              <div className="skill-row" key={i.skillId}>
                <div>
                  <span className="skill-name">{i.skill}</span>
                  <span className="tag plain" style={{ marginLeft: 10 }}>Need {i.requiredLevel}</span>
                </div>
                <div className="levels">
                  {[0, 1, 2, 3, 4, 5].map((n) => (
                    <button
                      key={n}
                      className={levels[i.skillId] === n ? "on" : ""}
                      onClick={() => setLevels({ ...levels, [i.skillId]: n })}
                    >
                      {n}
                    </button>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </>
  );
}
