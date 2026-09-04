import { useEffect, useState } from 'react';
import api from '../../api/axios.js';

export default function Classrooms() {
  const [classrooms, setClassrooms] = useState([]);
  const [name, setName] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [copiedId, setCopiedId] = useState(null);

  function load() {
    setLoading(true);
    api.get('/classrooms/my').then((res) => setClassrooms(res.data)).finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
  }, []);

  async function handleCreate(e) {
    e.preventDefault();
    setError('');
    setCreating(true);
    try {
      await api.post('/classrooms', { name });
      setName('');
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create classroom');
    } finally {
      setCreating(false);
    }
  }

  function copyCode(code, id) {
    navigator.clipboard.writeText(code);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 1500);
  }

  return (
    <div className="container">
      <h2>Your classrooms</h2>
      <p className="muted">
        Share a classroom's join code with students. When creating a test, you can optionally restrict it to one
        classroom -- otherwise it stays open to everyone, like before.
      </p>

      <div className="card">
        <h3>Create a classroom</h3>
        {error && <div className="error">{error}</div>}
        <form onSubmit={handleCreate}>
          <label>Name</label>
          <input value={name} onChange={(e) => setName(e.target.value)} placeholder="e.g. Period 3 - Data Structures" required />
          <button className="btn" type="submit" disabled={creating}>
            {creating ? 'Creating...' : 'Create classroom'}
          </button>
        </form>
      </div>

      {loading && <p className="muted">Loading...</p>}
      {!loading && classrooms.length === 0 && <p className="muted">No classrooms yet.</p>}

      {classrooms.map((c) => (
        <div className="card" key={c.id}>
          <h3>{c.name}</h3>
          <p className="muted">{c.studentCount} student(s) joined</p>
          <div className="row" style={{ alignItems: 'center' }}>
            <div>
              <label style={{ marginBottom: 4 }}>Join code</label>
              <div style={{ fontSize: 22, fontWeight: 700, letterSpacing: 3 }}>{c.joinCode}</div>
            </div>
            <div style={{ textAlign: 'right' }}>
              <button className="btn secondary" onClick={() => copyCode(c.joinCode, c.id)}>
                {copiedId === c.id ? 'Copied!' : 'Copy code'}
              </button>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
