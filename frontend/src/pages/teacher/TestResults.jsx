import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api/axios.js';

export default function TestResults() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get(`/tests/${id}/results`).then((res) => setResults(res.data)).finally(() => setLoading(false));
  }, [id]);

  return (
    <div className="container">
      <button className="btn secondary" onClick={() => navigate('/teacher')}>&larr; Back</button>
      <h2>Results</h2>

      {loading && <p className="muted">Loading...</p>}
      {!loading && results.length === 0 && <p className="muted">No attempts yet.</p>}

      {results.map((r) => (
        <div className="card" key={r.attemptId}>
          <strong>{r.studentName}</strong>
          <p className="muted">
            Started: {new Date(r.startTime).toLocaleString()}
            {r.submittedTime && <> &middot; Submitted: {new Date(r.submittedTime).toLocaleString()}</>}
          </p>
          <p>
            {r.submittedTime
              ? <>Score: <strong>{r.score}</strong></>
              : <span className="muted">In progress...</span>}
          </p>
        </div>
      ))}
    </div>
  );
}
