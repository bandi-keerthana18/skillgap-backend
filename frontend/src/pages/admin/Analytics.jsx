import { useEffect, useState } from "react";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import api from "../../api";

const tip = { background: "#14142a", border: "1px solid #2a2a4a", borderRadius: 10 };
const tick = { fill: "#9a98c4", fontSize: 12 };

export default function Analytics() {
  const [a, setA] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get("/admin/analytics")
      .then((r) => setA(r.data))
      .catch(() => setError("Could not load analytics."));
  }, []);

  if (error) return <div className="error">{error}</div>;
  if (!a) return <p>Loading analytics...</p>;

  const stats = [
    ["Students", a.students],
    ["Active students", a.activeStudents],
    ["Skills", a.skills],
    ["Career roles", a.roles],
  ];

  return (
    <>

      <h1>Analytics</h1>
      <p>Where students stand, and which skills are hardest to find.</p>

      <div className="stat-grid">
        {stats.map(([label, value]) => (
          <div className="card" key={label}>
            <div className="num">{value}</div>
            <p style={{ margin: 0 }}>{label}</p>
          </div>
        ))}
      </div>

      <div className="card">
        <h2>Most missing skills</h2>
        <p>How many students do not have each skill yet. This shows what to teach next.</p>
        <div style={{ width: "100%", height: 340 }}>
          <ResponsiveContainer>
            <BarChart data={a.mostMissingSkills} layout="vertical" margin={{ left: 10 }}>
              <CartesianGrid stroke="#2a2a4a" horizontal={false} />
              <XAxis type="number" allowDecimals={false} tick={tick} />
              <YAxis type="category" dataKey="skill" width={130} tick={{ fill: "#c9c7ee", fontSize: 12 }} />
              <Tooltip contentStyle={tip} cursor={{ fill: "rgba(139, 92, 246, 0.1)" }} />
              <Bar dataKey="studentsWithoutSkill" name="Students without it" fill="#8b5cf6" radius={[0, 6, 6, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="card">
        <h2>Average readiness by role</h2>
        <p>Averaged over students who have added at least one skill.</p>
        <div style={{ width: "100%", height: 300 }}>

          <ResponsiveContainer>
            <BarChart data={a.averageReadinessByRole} layout="vertical" margin={{ left: 10 }}>
              <CartesianGrid stroke="#2a2a4a" horizontal={false} />
              <XAxis type="number" domain={[0, 100]} tick={tick} />
              <YAxis type="category" dataKey="role" width={150} tick={{ fill: "#c9c7ee", fontSize: 12 }} />
              <Tooltip contentStyle={tip} cursor={{ fill: "rgba(34, 211, 238, 0.1)" }} />
              <Bar dataKey="averageReadiness" name="Average readiness %" fill="#22d3ee" radius={[0, 6, 6, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </>
  );
}
