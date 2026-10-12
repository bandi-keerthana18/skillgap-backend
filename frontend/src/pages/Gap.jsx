import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import {
  Legend, PolarAngleAxis, PolarGrid, PolarRadiusAxis, Radar, RadarChart,
  ResponsiveContainer, Tooltip,
} from "recharts";
import api from "../api";
import Ring from "../components/Ring.jsx";

function ItemList({ title, items, kind }) {
  if (items.length === 0) return null;
  return (
    <div className="card">
      <h2>{title} <span className={"tag " + kind}>{items.length}</span></h2>
      {items.map((i) => (
        <div className="item-row" key={i.skillId}>
          <div>
            <div className="skill-name">{i.skill}</div>
            <div className="rank-sub">Importance {i.weight.toFixed(1)}</div>
          </div>
          <div className="item-level">
            <span className={"tag " + kind}>You: {i.studentLevel}</span>
            <span className="tag plain">Need: {i.requiredLevel}</span>
          </div>
        </div>
      ))}
    </div>
  );
}


export default function Gap() {
  const [params, setParams] = useSearchParams();
  const [roles, setRoles] = useState([]);
  const [report, setReport] = useState(null);
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
    setReport(null);
    api.get(`/analysis/${roleId}`)
      .then((r) => setReport(r.data))
      .catch(() => setError("Could not load this report."));
  }, [roleId]);

  const data = report
    ? [...report.strong, ...report.weak, ...report.missing].map((i) => ({
        skill: i.skill,

        Required: i.requiredLevel,
        You: i.studentLevel,
      }))
    : [];

  return (
    <>
      <div className="head-row">
        <div>
          <h1>Skill Gap</h1>
          <p>Compare your skills with what the role needs.</p>
        </div>
        <select value={roleId || ""} onChange={(e) => setParams({ role: e.target.value })}>
          {roles.map((r) => (
            <option key={r.id} value={r.id}>{r.title}</option>
          ))}
        </select>
      </div>

      {error && <div className="error">{error}</div>}
      {!report && !error && <p>Loading the report...</p>}

      {report && (
        <>
          <div className="card hero">
            <Ring value={report.readinessScore} label="readiness" />
            <div className="hero-info">
              <p style={{ margin: 0 }}>Target role</p>
              <div className="big">{report.roleTitle}</div>
              <div className="stat-row">
                <span className="pill">{report.alignmentScore.toFixed(1)}% alignment</span>
                <span className="pill cyan">{report.readinessScore.toFixed(1)}% ready</span>

                <span className="tag missing">{report.missing.length} missing</span>
                <span className="tag weak">{report.weak.length} weak</span>
                <span className="tag strong">{report.strong.length} strong</span>
              </div>
              <div className="stat-row">
                <Link to={`/roadmap?role=${report.roleId}`}>
                  <button className="btn">Open my roadmap</button>
                </Link>
              </div>
            </div>
          </div>

          <div className="card">
            <h2>You vs the role</h2>
            <p>Purple is your level, cyan is what the role needs. Where cyan sticks out, there is a gap.</p>
            <div style={{ width: "100%", height: 380 }}>
              <ResponsiveContainer>
                <RadarChart data={data} outerRadius="70%">
                  <PolarGrid stroke="#2a2a4a" />
                  <PolarAngleAxis dataKey="skill" tick={{ fill: "#c9c7ee", fontSize: 12 }} />
                  <PolarRadiusAxis angle={90} domain={[0, 5]} tickCount={6} tick={{ fill: "#9a98c4", fontSize: 10 }} />
                  <Radar name="Required" dataKey="Required" stroke="#22d3ee" fill="#22d3ee" fillOpacity={0.2} />
                  <Radar name="You" dataKey="You" stroke="#8b5cf6" fill="#8b5cf6" fillOpacity={0.45} />
                  <Tooltip contentStyle={{ background: "#14142a", border: "1px solid #2a2a4a", borderRadius: 10 }} />
                  <Legend />
                </RadarChart>
              </ResponsiveContainer>
            </div>
          </div>

          <ItemList title="Missing skills" items={report.missing} kind="missing" />
          <ItemList title="Weak skills" items={report.weak} kind="weak" />

          <ItemList title="Strong skills" items={report.strong} kind="strong" />
        </>
      )}
    </>
  );
}
