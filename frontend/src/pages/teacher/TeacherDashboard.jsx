import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axios.js';

export default function TeacherDashboard() {
  const [tests, setTests] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/tests/my').then((res) => setTests(res.data)).finally(() => setLoading(false));
  }, []);

  return (
    <div className="container">
      <div className="row" style={{ alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>Your tests</h2>
        <div style={{ textAlign: 'right' }}>
          <Link to="/teacher/classrooms" className="btn secondary" style={{ marginRight: 8 }}>Classrooms</Link>
          <Link to="/teacher/create" className="btn">+ New test</Link>
        </div>
      </div>

      {loading && <p className="muted">Loading...</p>}
      {!loading && tests.length === 0 && <p className="muted">You haven't created any tests yet.</p>}

      {tests.map((t) => (
        <div className="card" key={t.id}>
          <h3>{t.title}</h3>
          <p className="muted">{t.description}</p>
          <p className="muted">
            {t.questionCount} question(s) &middot; {t.durationMinutes} min &middot;{' '}
            {t.published ? 'Published' : 'Draft'} &middot; {t.classroomName ? `Class: ${t.classroomName}` : 'Open to everyone'}
          </p>
          <div className="row">
            <Link to={`/teacher/tests/${t.id}`} className="btn secondary">Manage questions</Link>
            <Link to={`/teacher/tests/${t.id}/results`} className="btn secondary">View results</Link>
          </div>
        </div>
      ))}
    </div>
  );
}
