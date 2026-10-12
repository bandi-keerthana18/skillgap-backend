import { useEffect, useState } from "react";
import api from "../../api";
import { errText } from "../../errors.js";

export default function AdminResources() {
  const [skills, setSkills] = useState([]);
  const [filter, setFilter] = useState("");
  const [list, setList] = useState([]);
  const [form, setForm] = useState({ skillId: "", title: "", url: "", type: "ARTICLE" });
  const [msg, setMsg] = useState({ type: "", text: "" });
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    api.get("/skills").then((r) => {
      const s = [...r.data].sort((a, b) => a.name.localeCompare(b.name));
      setSkills(s);
      if (s.length > 0) setForm((f) => ({ ...f, skillId: String(s[0].id) }));
    });
  }, []);

  const load = () =>
    api.get("/admin/resources", { params: filter ? { skillId: filter } : {} })
      .then((r) => setList(r.data))
      .catch((e) => setMsg({ type: "error", text: errText(e, "Could not load resources.") }));

  useEffect(() => { load(); }, [filter]);

  const add = async (e) => {
    e.preventDefault();
    setBusy(true);
    setMsg({ type: "", text: "" });
    try {
      await api.post("/admin/resources", {
        skillId: Number(form.skillId),
        title: form.title,
        url: form.url,
        type: form.type,
      });
      setForm({ ...form, title: "", url: "" });
      setMsg({ type: "ok", text: "Resource added." });
      await load();
    } catch (err) {
      setMsg({ type: "error", text: errText(err, "Could not add the resource.") });
    }
    setBusy(false);
  };

  const remove = async (r) => {
    setMsg({ type: "", text: "" });
    try {
      await api.delete(`/admin/resources/${r.id}`);
      setMsg({ type: "ok", text: "Resource deleted." });
      await load();
    } catch (err) {
      setMsg({ type: "error", text: errText(err, "Could not delete the resource.") });
    }
  };

  return (
    <>
      <h1>Learning resources</h1>
      <p>The links students see on their roadmap.</p>

      {msg.text && <div className={msg.type}>{msg.text}</div>}

      <div className="card">
        <h2>Add a resource</h2>
        <form onSubmit={add}>
          <div className="form-grid">
            <div>
              <label>Skill</label>
              <select value={form.skillId} onChange={(e) => setForm({ ...form, skillId: e.target.value })}>
                {skills.map((s) => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
            </div>
            <div>
              <label>Title</label>
              <input type="text" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required />
            </div>
            <div>
              <label>Link (https://...)</label>
              <input type="text" value={form.url} onChange={(e) => setForm({ ...form, url: e.target.value })} required />
            </div>
            <div>
              <label>Type</label>
              <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
                <option value="ARTICLE">ARTICLE</option>
                <option value="VIDEO">VIDEO</option>
                <option value="COURSE">COURSE</option>
              </select>
            </div>
            <button className="btn" disabled={busy}>{busy ? "Adding..." : "Add resource"}</button>
          </div>
        </form>
      </div>

      <div className="card">
        <div className="head-row">
          <h2>Resources ({list.length})</h2>
          <select value={filter} onChange={(e) => setFilter(e.target.value)}>
            <option value="">All skills</option>
            {skills.map((s) => (
              <option key={s.id} value={s.id}>{s.name}</option>
            ))}
          </select>
        </div>
        <table className="table">
          <thead>
            <tr><th>Skill</th><th>Title</th><th>Type</th><th>Link</th><th></th></tr>
          </thead>
          <tbody>
            {list.map((r) => (
              <tr key={r.id}>
                <td>{r.skill}</td>
                <td>{r.title}</td>
                <td>{r.type}</td>
                <td><a href={r.url} target="_blank" rel="noreferrer" style={{ color: "#67e8f9" }}>Open</a></td>
                <td><button className="btn danger" onClick={() => remove(r)}>Delete</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
