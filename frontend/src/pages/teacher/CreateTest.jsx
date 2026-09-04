import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios.js';

export default function CreateTest() {
  const navigate = useNavigate();
  const [classrooms, setClassrooms] = useState([]);
  const [form, setForm] = useState({
    title: '',
    description: '',
    durationMinutes: 30,
    startTime: '',
    endTime: '',
    allowReview: true,
    classroomId: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api.get('/classrooms/my').then((res) => setClassrooms(res.data)).catch(() => {});
  }, []);

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const payload = { ...form, classroomId: form.classroomId ? Number(form.classroomId) : null };
      const res = await api.post('/tests', payload);
      navigate(`/teacher/tests/${res.data.id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create test');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container" style={{ maxWidth: 500 }}>
      <h2>Create a new test</h2>
      <form onSubmit={handleSubmit} className="card">
        {error && <div className="error">{error}</div>}
        <label>Title</label>
        <input value={form.title} onChange={(e) => update('title', e.target.value)} required />

        <label>Description</label>
        <textarea rows={3} value={form.description} onChange={(e) => update('description', e.target.value)} />

        <label>Duration (minutes)</label>
        <input
          type="number"
          min={1}
          value={form.durationMinutes}
          onChange={(e) => update('durationMinutes', Number(e.target.value))}
          required
        />

        <div className="row">
          <div>
            <label>Opens at</label>
            <input
              type="datetime-local"
              value={form.startTime}
              onChange={(e) => update('startTime', e.target.value)}
              required
            />
          </div>
          <div>
            <label>Closes at</label>
            <input
              type="datetime-local"
              value={form.endTime}
              onChange={(e) => update('endTime', e.target.value)}
              required
            />
          </div>
        </div>

        <label>Assign to a classroom (optional)</label>
        <select value={form.classroomId} onChange={(e) => update('classroomId', e.target.value)}>
          <option value="">Open to every student</option>
          {classrooms.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>

        <label>
          <input
            type="checkbox"
            style={{ width: 'auto', marginRight: 8 }}
            checked={form.allowReview}
            onChange={(e) => update('allowReview', e.target.checked)}
          />
          Let students review correct answers &amp; tags after submitting
        </label>

        <button className="btn" type="submit" disabled={loading}>
          {loading ? 'Creating...' : 'Create & add questions'}
        </button>
      </form>
    </div>
  );
}
