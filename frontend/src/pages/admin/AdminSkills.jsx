import { useEffect, useState } from "react";
import api from "../../api";
import { errText } from "../../errors.js";

export default function AdminSkills() {
  const [skills, setSkills] = useState([]);
  const [form, setForm] = useState({ name: "", category: "", prereq: [] });
  const [msg, setMsg] = useState({ type: "", text: "" });
  const [busy, setBusy] = useState(false);

  const load = () =>
    api.get("/admin/skills")
      .then((r) => setSkills(r.data))
      .catch((e) => setMsg({ type: "error", text: errText(e, "Could not load skills.") }));

  useEffect(() => { load(); }, []);

  const sorted = [...skills].sort(
    (a, b) => (a.category || "").localeCompare(b.category || "") || a.name.localeCompare(b.name)
  );

  const toggle = (id) =>
    setForm({
      ...form,
      prereq: form.prereq.includes(id) ? form.prereq.filter((x) => x !== id) : [...form.prereq, id],
    });

  const add = async (e) => {
    e.preventDefault();
    setBusy(true);
    setMsg({ type: "", text: "" });
    try {
      await api.post("/admin/skills", {
        name: form.name,
        category: form.category,
        prerequisiteIds: form.prereq,
      });
      setForm({ name: "", category: "", prereq: [] });
      setMsg({ type: "ok", text: "Skill added." });
      await load();
    } catch (err) {
      setMsg({ type: "error", text: errText(err, "Could not add the skill.") });
    }
    setBusy(false);
  };

  const remove = async (s) => {
    if (!window.confirm(`Delete the skill "${s.name}"?`)) return;
    setMsg({ type: "", text: "" });
    try {
      await api.delete(`/admin/skills/${s.id}`);
      setMsg({ type: "ok", text: `Deleted ${s.name}.` });
      await load();
    } catch (err) {
      setMsg({ type: "error", text: errText(err, "Could not delete the skill.") });
    }
  };

  return (
    <>
      <h1>Skills library</h1>
      <p>The skills students can rate and roles can require.</p>

      {msg.text && <div className={msg.type}>{msg.text}</div>}

      <div className="card">
        <h2>Add a skill</h2>
        <form onSubmit={add}>
          <div className="form-grid">
            <div>
              <label>Name</label>
              <input type="text" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
            </div>
            <div>
              <label>Category</label>
              <input type="text" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} />
            </div>
            <button className="btn" disabled={busy}>{busy ? "Adding..." : "Add skill"}</button>
          </div>
          <label style={{ marginTop: 14 }}>Prerequisites (optional): skills to learn first</label>
          <div className="checks">
            {sorted.map((s) => (
              <label key={s.id}>
                <input type="checkbox" checked={form.prereq.includes(s.id)} onChange={() => toggle(s.id)} />
                {s.name}
              </label>
            ))}
          </div>
        </form>
      </div>

      <div className="card">
        <h2>All skills ({skills.length})</h2>
        <table className="table">
          <thead>
            <tr><th>Name</th><th>Category</th><th>Prerequisites</th><th></th></tr>
          </thead>
          <tbody>
            {sorted.map((s) => (
              <tr key={s.id}>
                <td>{s.name}</td>
                <td>{s.category}</td>
                <td>{s.prerequisites.join(", ")}</td>
                <td><button className="btn danger" onClick={() => remove(s)}>Delete</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
