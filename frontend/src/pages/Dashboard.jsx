import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api";
import { useAuth } from "../auth.jsx";
import Ring from "../components/Ring.jsx";

const code = (n) => String.fromCodePoint(n);

const BADGES = [
  { name: "First Steps", info: "Rate your first skill", icon: code(0x1F331), test: (s) => s.skillCount >= 1 },
  { name: "Skill Collector", info: "Rate 8 skills", icon: code(0x1F4DA), test: (s) => s.skillCount >= 8 },
  { name: "Rising Star", info: "Earn 50 XP", icon: code(0x2B50), test: (s) => s.xp >= 50 },
  { name: "Dedicated Learner", info: "Earn 200 XP", icon: code(0x1F525), test: (s) => s.xp >= 200 },
  { name: "Career Ready", info: "Reach 80% readiness for a role", icon: code(0x1F3C6), test: (s) => s.bestReadiness >= 80 },
];

export default function Dashboard() {
  const { user } = useAuth();
  const [ranking, setRanking] = useState(null);
  const [profile, setProfile] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([api.get("/analysis/ranking"), api.get("/me/profile")])
      .then(([r, p]) => {
        setRanking(r.data);
        setProfile(p.data);
      })
      .catch(() => setError("Could not load your data. Is the backend running?"));
  }, []);


  if (error) return <div className="error">{error}</div>;
  if (!ranking || !profile) return <p>Loading your matches...</p>;

  const top = ranking[0];
  const count = profile.skillCount;
  const level = Math.floor(profile.xp / 100) + 1;
  const into = profile.xp % 100;
  const best = Math.max(0, ...ranking.map((r) => r.readinessPercent));
  const stats = { skillCount: count, xp: profile.xp, bestReadiness: best };

  return (
    <>
      <h1>Hi, {user.name} &#128075;</h1>
      <p>Here is how your skills line up with careers.</p>

      <div className="card">
        <div className="head-row">
          <div>
            <div className="rank-sub">Your level</div>
            <div className="big">Level {level}</div>
          </div>
          <span className="pill">{profile.xp} XP</span>
        </div>
        <div className="bar" style={{ marginTop: 12 }}><div style={{ width: into + "%" }} /></div>
        <div className="rank-sub">
          {100 - into} XP to level {level + 1}. You earn 10 XP for every skill level you learn on the Roadmap.
        </div>
        <div className="badges">
          {BADGES.map((b) => (
            <div key={b.name} className={"badge" + (b.test(stats) ? " on" : "")} title={b.info}>
              <span className="badge-icon">{b.icon}</span>
              <span>{b.name}</span>

            </div>
          ))}
        </div>
      </div>

      {count === 0 || !top ? (
        <div className="card empty">
          <h2>Start by adding your skills</h2>
          <p>Rate yourself on the skills you know, and we will rank the careers that fit you best.</p>
          <Link to="/skills"><button className="btn">Add my skills</button></Link>
        </div>
      ) : (
        <>
          <div className="card hero">
            <Ring value={top.readinessPercent} label="readiness" />
            <div className="hero-info">
              <p style={{ margin: 0 }}>Your best match</p>
              <div className="big">{top.title}</div>
              <div className="stat-row">
                <span className="pill">{top.alignmentPercent.toFixed(1)}% alignment</span>
                <span className="pill cyan">{top.readinessPercent.toFixed(1)}% ready</span>
              </div>
              <div className="stat-row">
                <Link to={`/gap?role=${top.roleId}`}><button className="btn">See my skill gap</button></Link>
                <Link to={`/roadmap?role=${top.roleId}`}><button className="btn ghost">View roadmap</button></Link>
                <Link to={`/report?role=${top.roleId}`}><button className="btn ghost">Career report</button></Link>
              </div>
            </div>
          </div>

          <div className="card">
            <h2>All career matches</h2>

            <p>Click a career to see your gap for it.</p>
            {ranking.map((r, i) => (
              <Link className="rank-row" key={r.roleId} to={`/gap?role=${r.roleId}`}>
                <div className={"rank-no" + (i === 0 ? " first" : "")}>{i + 1}</div>
                <div className="rank-body">
                  <div className="rank-title">
                    <span>{r.title}</span>
                    <span>{r.alignmentPercent.toFixed(1)}%</span>
                  </div>
                  <div className="bar"><div style={{ width: r.alignmentPercent + "%" }} /></div>
                  <div className="rank-sub">Readiness {r.readinessPercent.toFixed(1)}%</div>
                </div>
              </Link>
            ))}
          </div>
        </>
      )}
    </>
  );
}
