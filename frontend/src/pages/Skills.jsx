import { useEffect, useState } from "react";
import api from "../api";

export default function Skills() {
  const [skills, setSkills] = useState([]);
  const [levels, setLevels] = useState({});
  const [msg, setMsg] = useState({ type: "", text: "" });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    Promise.all([api.get("/skills"), api.get("/me/skills")]).then(([a, b]) => {
      setSkills(a.data);
      const m = {};
      b.data.forEach((s) => { m[s.skillId] = s.level; });
      setLevels(m);
    });
  }, []);

  const groups = {};
  skills.forEach((s) => {
    const key = s.category || "Other";
    if (!groups[key]) groups[key] = [];
    groups[key].push(s);
  });

  const setLevel = (id, level) => setLevels({ ...levels, [id]: level });

  const save = async () => {
    setSaving(true);
    setMsg({ type: "", text: "" });
    try {
      const body = skills.map((s) => ({ skillId: s.id, level: levels[s.id] || 0 }));
      await api.put("/me/skills", body);
      setMsg({ type: "ok", text: "Saved! Open the Dashboard to see your updated matches." });
    } catch {
      setMsg({ type: "error", text: "Could not save. Please try again." });
    }
    setSaving(false);
  };

  return (
    <>
      <h1>My Skills</h1>
      <p className="legend">0 = none &middot; 1 = beginner &middot; 2 = basic &middot; 3 = intermediate &middot; 4 = advanced &middot; 5 = expert</p>

      <div className="card">
        {Object.keys(groups).sort().map((cat) => (
          <div key={cat}>
            <div className="group-title">{cat}</div>
            {groups[cat].map((s) => (
              <div className="skill-row" key={s.id}>
                <span className="skill-name">{s.name}</span>
                <div className="levels">
                  {[0, 1, 2, 3, 4, 5].map((n) => (
                    <button
                      key={n}
                      className={(levels[s.id] || 0) === n ? "on" : ""}
                      onClick={() => setLevel(s.id, n)}
                    >
                      {n}
                    </button>
                  ))}
                </div>
              </div>
            ))}
          </div>
        ))}
      </div>

      <div className="save-bar">
        <button className="btn" onClick={save} disabled={saving}>
          {saving ? "Saving..." : "Save my skills"}
        </button>
        {msg.text && <div className={msg.type} style={{ margin: 0 }}>{msg.text}</div>}
      </div>
    </>
  );
}
