import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Navbar from "./components/Navbar";

import Landing from "./pages/Landing";
import Register from "./pages/Register";
import RegisterSociety from "./pages/RegisterSociety";
import RegisterResident from "./pages/RegisterResident";
import Login from "./pages/Login";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";
import Dashboard from "./pages/Dashboard";
import Residents from "./pages/Residents";
import Staff from "./pages/Staff";
import Complaints from "./pages/Complaints";
import Notices from "./pages/Notices";
import Bills from "./pages/Bills";
import Payments from "./pages/Payments";
import Notifications from "./pages/Notifications";
import AddUser from "./pages/AddUser";
import Approvals from "./pages/Approvals";
import SuperAdminSocieties from "./pages/SuperAdminSocieties";

function Layout({ children }) {
  return (
    <>
      <Navbar />
      <main className="content">{children}</main>
    </>
  );
}

// SUPER_ADMIN has no society, so the regular Dashboard (society stats) does
// not apply to them — send them straight to society management instead.
function Home() {
  const { user } = useAuth();
  if (user?.role === "ROLE_SUPER_ADMIN") {
    return <Navigate to="/super-admin/societies" replace />;
  }
  return <Dashboard />;
}

// The public marketing page doubles as the "/" route. A logged-in user
// landing on "/" (e.g. via a bookmark) is sent straight to their dashboard
// instead of seeing the marketing copy again.
function LandingOrDashboard() {
  const { user } = useAuth();
  if (user) return <Navigate to="/dashboard" replace />;
  return <Landing />;
}

function AppRoutes() {
  return (
    <Routes>
      {/* Public: marketing + self-service registration + login */}
      <Route path="/" element={<LandingOrDashboard />} />
      <Route path="/register" element={<Register />} />
      <Route path="/register/society" element={<RegisterSociety />} />
      <Route path="/register/user" element={<RegisterResident />} />
      <Route path="/login" element={<Login />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />

      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <Layout><Home /></Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/residents"
        element={
          <ProtectedRoute roles={["ROLE_ADMIN"]}>
            <Layout><Residents /></Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/staff"
        element={
          <ProtectedRoute roles={["ROLE_ADMIN", "ROLE_STAFF"]}>
            <Layout><Staff /></Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/complaints"
        element={
          <ProtectedRoute>
            <Layout><Complaints /></Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/notices"
        element={
          <ProtectedRoute>
            <Layout><Notices /></Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/bills"
        element={
          <ProtectedRoute roles={["ROLE_ADMIN", "ROLE_RESIDENT"]}>
            <Layout><Bills /></Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/payments"
        element={
          <ProtectedRoute>
            <Layout><Payments /></Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/notifications"
        element={
          <ProtectedRoute>
            <Layout><Notifications /></Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/users/add"
        element={
          <ProtectedRoute roles={["ROLE_ADMIN"]}>
            <Layout><AddUser /></Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/approvals"
        element={
          <ProtectedRoute roles={["ROLE_ADMIN"]}>
            <Layout><Approvals /></Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/super-admin/societies"
        element={
          <ProtectedRoute roles={["ROLE_SUPER_ADMIN"]}>
            <Layout><SuperAdminSocieties /></Layout>
          </ProtectedRoute>
        }
      />

      {/* Anything unrecognised: send logged-in users home, guests to landing */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}
