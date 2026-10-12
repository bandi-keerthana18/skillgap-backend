import { useEffect, useState } from "react";
import api from "../../api";
import { errText } from "../../errors.js";

export default function AdminRoles() {
  const [roles, setRoles] = useState([]);
  const [skills, setSkills] = useState([]);
  const [sel, setSel] = useState(null);
  const [reqs, setReqs] = useState([]);
  const [form, setForm] = useState({ title: "", description: "" });
  const [msg, setMsg] = useState({ type: "", text: "" });
  const [busy, setBusy] = useState(false);

  const loadRoles = () => api.get("/roles").then((r) => setRoles(r.data));

  useEffect(() => {
    loadRoles().catch((e) => setMsg({ type: "error", text: errText(e, "Could not load roles.") }));
    api.get("/skills").then((r) =>
      setSkills([...r.data].sort((a, b) => a.name.localeCompare(b.name)))
    );
  }, []);

  const pick = async (id) => {
    setSel(id);
    setMsg({ type: "", text: "" });
    try {
      const r = await api.get(`/admin/roles/${id}/skills`);
      setReqs(r.data.map((x) => ({ skillId: x.skillId, requiredLevel: x.requiredLevel, weight: x.weight })));
    } catch (e) {
      setMsg({ type: "error", text: errText(e, "Could not load the requirements.") });
    }

  };

  const create = async (e) => {
    e.preventDefault();
    setBusy(true);
    setMsg({ type: "", text: "" });
    try {
      const r = await api.post("/admin/roles", form);
      setForm({ title: "", description: "" });
      await loadRoles();
      await pick(r.data.id);
      setMsg({ type: "ok", text: "Role created. Now add the skills it requires and save." });
    } catch (err) {
      setMsg({ type: "error", text: errText(err, "Could not create the role.") });
    }
    setBusy(false);
  };

  const setRow = (idx, key, value) =>
    setReqs(reqs.map((r, i) => (i === idx ? { ...r, [key]: value } : r)));

  const addRow = () => {
    const used = reqs.map((r) => r.skillId);
    const free = skills.find((s) => !used.includes(s.id));
    if (!free) return;
    setReqs([...reqs, { skillId: free.id, requiredLevel: 3, weight: 0.5 }]);
  };

  const removeRow = (idx) => setReqs(reqs.filter((_, i) => i !== idx));

  const save = async () => {
    setBusy(true);

    setMsg({ type: "", text: "" });
    try {
      const body = reqs.map((r) => ({
        skillId: Number(r.skillId),
        requiredLevel: Number(r.requiredLevel),
        weight: Number(r.weight),
      }));
      await api.put(`/admin/roles/${sel}/skills`, body);
      setMsg({ type: "ok", text: "Requirements saved. Students see the new scores right away." });
    } catch (err) {
      setMsg({ type: "error", text: errText(err, "Could not save the requirements.") });
    }
    setBusy(false);
  };

  const removeRole = async () => {
    const current = roles.find((r) => r.id === sel);
    if (!window.confirm(`Delete the role "${current.title}" and its history?`)) return;
    try {
      await api.delete(`/admin/roles/${sel}`);
      setSel(null);
      setReqs([]);
      await loadRoles();
      setMsg({ type: "ok", text: "Role deleted." });
    } catch (err) {
      setMsg({ type: "error", text: errText(err, "Could not delete the role.") });
    }
  };

  const current = roles.find((r) => r.id === sel);

  return (

    <>
      <h1>Career roles</h1>
      <p>Create roles and decide which skills they need, how high, and how important each one is.</p>

      {msg.text && <div className={msg.type}>{msg.text}</div>}

      <div className="card">
        <h2>Add a role</h2>
        <form onSubmit={create}>
          <div className="form-grid">
            <div>
              <label>Title</label>
              <input type="text" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required />
            </div>
            <div>
              <label>Description</label>
              <input type="text" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </div>
            <button className="btn" disabled={busy}>Add role</button>
          </div>
        </form>
      </div>

      <div className="card">
        <h2>Pick a role to edit</h2>
        <div className="chips">
          {roles.map((r) => (
            <button key={r.id} className={"chip" + (sel === r.id ? " on" : "")} onClick={() => pick(r.id)}>
              {r.title}
            </button>
          ))}
        </div>

      </div>

      {current && (
        <div className="card">
          <div className="head-row">
            <h2>{current.title}: required skills</h2>
            <button className="btn danger" onClick={removeRole}>Delete role</button>
          </div>
          <p>Level is how good a student must be (1 to 5). Importance is how much the skill counts (0.1 to 1).</p>

          <div className="req-row" style={{ border: 0 }}>
            <label>Skill</label><label>Level needed</label><label>Importance</label><span></span>
          </div>
          {reqs.map((r, idx) => (
            <div className="req-row" key={idx}>
              <select value={r.skillId} onChange={(e) => setRow(idx, "skillId", Number(e.target.value))}>
                {skills.map((s) => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
              <select value={r.requiredLevel} onChange={(e) => setRow(idx, "requiredLevel", Number(e.target.value))}>
                {[1, 2, 3, 4, 5].map((n) => (
                  <option key={n} value={n}>{n}</option>
                ))}
              </select>
              <input type="number" min="0.1" max="1" step="0.1" value={r.weight} onChange={(e) => setRow(idx, "weight", e.target.value)} />
              <button className="btn danger" onClick={() => removeRow(idx)}>Remove</button>
            </div>
          ))}

          <div className="stat-row">
            <button className="btn ghost" onClick={addRow}>+ Add a skill</button>

            <button className="btn" onClick={save} disabled={busy}>{busy ? "Saving..." : "Save requirements"}</button>
          </div>
        </div>
      )}
    </>
  );
}
