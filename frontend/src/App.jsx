import { Routes, Route, Link, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';

import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';

import TeacherDashboard from './pages/teacher/TeacherDashboard.jsx';
import CreateTest from './pages/teacher/CreateTest.jsx';
import TestDetail from './pages/teacher/TestDetail.jsx';
import TestResults from './pages/teacher/TestResults.jsx';
import Classrooms from './pages/teacher/Classrooms.jsx';

import StudentDashboard from './pages/student/StudentDashboard.jsx';
import AttemptTest from './pages/student/AttemptTest.jsx';
import AttemptResult from './pages/student/AttemptResult.jsx';

function Navbar() {
  const { user, logout } = useAuth();
  return (
    <div className="navbar">
      <Link to="/" className="brand" style={{ color: 'white', textDecoration: 'none' }}>Testly</Link>
      <div>
        {user ? (
          <>
            <span className="muted" style={{ color: '#c9c9e8' }}>{user.name} ({user.role})</span>
            <button onClick={logout}>Log out</button>
          </>
        ) : (
          <>
            <Link to="/login">Log in</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </div>
  );
}

function Home() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={user.role === 'TEACHER' ? '/teacher' : '/student'} replace />;
}

export default function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route path="/teacher" element={<ProtectedRoute role="TEACHER"><TeacherDashboard /></ProtectedRoute>} />
        <Route path="/teacher/classrooms" element={<ProtectedRoute role="TEACHER"><Classrooms /></ProtectedRoute>} />
        <Route path="/teacher/create" element={<ProtectedRoute role="TEACHER"><CreateTest /></ProtectedRoute>} />
        <Route path="/teacher/tests/:id" element={<ProtectedRoute role="TEACHER"><TestDetail /></ProtectedRoute>} />
        <Route path="/teacher/tests/:id/results" element={<ProtectedRoute role="TEACHER"><TestResults /></ProtectedRoute>} />

        <Route path="/student" element={<ProtectedRoute role="STUDENT"><StudentDashboard /></ProtectedRoute>} />
        <Route path="/student/attempt/:attemptId" element={<ProtectedRoute role="STUDENT"><AttemptTest /></ProtectedRoute>} />
        <Route path="/student/result/:attemptId" element={<ProtectedRoute role="STUDENT"><AttemptResult /></ProtectedRoute>} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}
