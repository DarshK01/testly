import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api/axios.js';

export default function AttemptResult() {
  const { attemptId } = useParams();
  const navigate = useNavigate();
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get(`/attempts/${attemptId}/result`).then((res) => setResult(res.data)).finally(() => setLoading(false));
  }, [attemptId]);

  if (loading) return <div className="container">Loading...</div>;
  if (!result) return <div className="container">Result not found.</div>;

  const tagEntries = Object.entries(result.tagWiseScore || {});

  return (
    <div className="container">
      <button className="btn secondary" onClick={() => navigate('/student')}>&larr; Back to dashboard</button>
      <h2>{result.testTitle} - Result</h2>
      <div className="card">
        <h3>
          Score: {result.score} / {result.maxScore}
        </h3>
        <p className="muted">Submitted: {new Date(result.submittedTime).toLocaleString()}</p>
      </div>

      {tagEntries.length > 0 && (
        <div className="card">
          <h3>Topic-wise performance</h3>
          {tagEntries.map(([tag, s]) => (
            <p key={tag}>
              <span className="tag">{tag}</span> {s.correct} / {s.total} correct
            </p>
          ))}
        </div>
      )}

      {result.questions.length === 0 && (
        <p className="muted">Your teacher has not enabled answer review for this test.</p>
      )}

      {result.questions.map((q, idx) => (
        <div className="card" key={q.id}>
          <strong>Q{idx + 1}.</strong> {q.questionText}
          {q.questionImageUrl && (
            <div style={{ margin: '8px 0' }}>
              <img src={q.questionImageUrl} alt="question" style={{ maxWidth: '100%', borderRadius: 6 }} />
            </div>
          )}
          <p className={q.correct ? 'muted' : 'error'} style={{ fontWeight: 600 }}>
            {q.correct ? `Correct (+${q.marksAwarded})` : 'Incorrect (+0)'}
          </p>
          {q.options.map((o) => {
            let cls = 'option-row';
            if (o.id === q.correctOptionId) cls += ' correct';
            else if (o.id === q.selectedOptionId) cls += ' incorrect';
            return (
              <div key={o.id} className={cls}>
                {o.optionText}
                {o.id === q.selectedOptionId && ' (your answer)'}
                {o.id === q.correctOptionId && ' (correct answer)'}
              </div>
            );
          })}
          {q.tags.length > 0 && (
            <div style={{ marginTop: 8 }}>
              {q.tags.map((t) => (
                <span className="tag" key={t}>{t}</span>
              ))}
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
