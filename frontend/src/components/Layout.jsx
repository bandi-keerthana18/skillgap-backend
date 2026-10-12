import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../auth.jsx";

const student = [
  ["/", "Dashboard", true],
  ["/skills", "My Skills"],

  ["/gap", "Skill Gap"],
  ["/roadmap", "Roadmap"],
  ["/whatif", "What-if"],
  ["/progress", "Progress"],
  ["/report", "Report"],
];

const admin = [
  ["/admin", "Analytics", true],
  ["/admin/skills", "Skills"],
  ["/admin/roles", "Roles"],
  ["/admin/resources", "Resources"],
];

export default function Layout() {
  const { user, logout } = useAuth();
  const nav = useNavigate();
  const links = user.role === "ADMIN" ? admin : student;

  const out = () => {
    logout();
    nav("/login");
  };

  return (
    <>
      <header className="nav">
        <div className="brand">&#9670; SkillGap</div>
        <nav className="nav-links">
          {links.map(([to, label, end]) => (
            <NavLink key={to} to={to} end={end}>{label}</NavLink>
          ))}

        </nav>
        <div className="nav-user">
          <span>{user.name}</span>
          <button className="btn ghost" onClick={out}>Log out</button>
        </div>
      </header>
      <main className="page">
        <Outlet />
      </main>
    </>
  );
}
