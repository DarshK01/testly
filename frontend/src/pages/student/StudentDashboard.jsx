import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios.js';

export default function StudentDashboard() {
  const [tests, setTests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [classrooms, setClassrooms] = useState([]);
  const [joinCode, setJoinCode] = useState('');
  const [joinError, setJoinError] = useState('');
  const [joining, setJoining] = useState(false);
  const navigate = useNavigate();

  function loadTests() {
    api.get('/tests/available').then((res) => setTests(res.data)).finally(() => setLoading(false));
  }

  function loadClassrooms() {
    api.get('/classrooms/joined').then((res) => setClassrooms(res.data));
  }

  useEffect(() => {
    loadTests();
    loadClassrooms();
  }, []);

  async function startTest(testId) {
    const res = await api.post(`/tests/${testId}/attempt`);
    navigate(`/student/attempt/${res.data.attemptId}`, { state: res.data });
  }

  async function handleJoin(e) {
    e.preventDefault();
    setJoinError('');
    setJoining(true);
    try {
      await api.post('/classrooms/join', { joinCode });
      setJoinCode('');
      loadClassrooms();
      loadTests(); // joining a classroom can reveal new tests restricted to it
    } catch (err) {
      setJoinError(err.response?.data?.message || 'Could not join -- check the code');
    } finally {
      setJoining(false);
    }
  }

  return (
    <div className="container">
      <div className="card">
        <h3>Join a classroom</h3>
        {joinError && <div className="error">{joinError}</div>}
        <form onSubmit={handleJoin} className="row" style={{ alignItems: 'flex-end' }}>
          <div>
            <label>Join code</label>
            <input
              value={joinCode}
              onChange={(e) => setJoinCode(e.target.value.toUpperCase())}
              placeholder="e.g. ABC123"
              required
            />
          </div>
          <div style={{ flex: '0 0 auto' }}>
            <button className="btn" type="submit" disabled={joining} style={{ marginBottom: 12 }}>
              {joining ? 'Joining...' : 'Join'}
            </button>
          </div>
        </form>
        {classrooms.length > 0 && (
          <p className="muted">
            You're in: {classrooms.map((c) => c.name).join(', ')}
          </p>
        )}
      </div>

      <h2>Available tests</h2>
      {loading && <p className="muted">Loading...</p>}
      {!loading && tests.length === 0 && <p className="muted">No tests are open right now. Check back later.</p>}

      {tests.map((t) => (
        <div className="card" key={t.id}>
          <h3>{t.title}</h3>
          <p className="muted">{t.description}</p>
          <p className="muted">
            {t.questionCount} question(s) &middot; {t.durationMinutes} min &middot; closes{' '}
            {new Date(t.endTime).toLocaleString()}
            {t.classroomName && <> &middot; Class: {t.classroomName}</>}
          </p>
          <button className="btn" onClick={() => startTest(t.id)}>Start test</button>
        </div>
      ))}
    </div>
  );
}
