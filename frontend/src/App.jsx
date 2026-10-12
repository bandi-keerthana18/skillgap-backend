import { Navigate, Outlet, Route, Routes } from "react-router-dom";
import { useAuth } from "./auth.jsx";
import Layout from "./components/Layout.jsx";
import Login from "./pages/Login.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import Skills from "./pages/Skills.jsx";
import Gap from "./pages/Gap.jsx";
import Roadmap from "./pages/Roadmap.jsx";
import WhatIf from "./pages/WhatIf.jsx";
import Progress from "./pages/Progress.jsx";
import Report from "./pages/Report.jsx";
import Analytics from "./pages/admin/Analytics.jsx";
import AdminSkills from "./pages/admin/AdminSkills.jsx";
import AdminRoles from "./pages/admin/AdminRoles.jsx";
import AdminResources from "./pages/admin/AdminResources.jsx";

function Protected({ children }) {
  const { user } = useAuth();
  return user ? children : <Navigate to="/login" replace />;
}

function AdminOnly() {
  const { user } = useAuth();
  return user.role === "ADMIN" ? <Outlet /> : <Navigate to="/" replace />;
}

function Home() {
  const { user } = useAuth();
  return user.role === "ADMIN" ? <Navigate to="/admin" replace /> : <Dashboard />;
}


export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route element={<Protected><Layout /></Protected>}>
        <Route index element={<Home />} />
        <Route path="skills" element={<Skills />} />
        <Route path="gap" element={<Gap />} />
        <Route path="roadmap" element={<Roadmap />} />
        <Route path="whatif" element={<WhatIf />} />
        <Route path="progress" element={<Progress />} />
        <Route path="report" element={<Report />} />
        <Route path="admin" element={<AdminOnly />}>
          <Route index element={<Analytics />} />
          <Route path="skills" element={<AdminSkills />} />
          <Route path="roles" element={<AdminRoles />} />
          <Route path="resources" element={<AdminResources />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
