import { useEffect, useMemo, useRef, useState } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import api from '../../api/axios.js';

export default function AttemptTest() {
  const { attemptId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();

  const [data] = useState(location.state || null);
  const [answers, setAnswers] = useState({}); // questionId -> optionId
  const [secondsLeft, setSecondsLeft] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const autoSubmitted = useRef(false);

  const hardDeadline = useMemo(() => (data ? new Date(data.hardDeadline).getTime() : null), [data]);

  useEffect(() => {
    if (!data) return; // e.g. page was refreshed and we lost navigation state
    function tick() {
      const remaining = Math.max(0, Math.floor((hardDeadline - Date.now()) / 1000));
      setSecondsLeft(remaining);
      if (remaining <= 0 && !autoSubmitted.current) {
        autoSubmitted.current = true;
        handleSubmit();
      }
    }
    tick();
    const interval = setInterval(tick, 1000);
    return () => clearInterval(interval);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [data]);

  if (!data) {
    return (
      <div className="container">
        <p className="error">
          This test session isn't loaded (perhaps the page was refreshed). Please go back and start it again.
        </p>
        <button className="btn" onClick={() => navigate('/student')}>Back to dashboard</button>
      </div>
    );
  }

  async function selectOption(questionId, optionId) {
    setAnswers((a) => ({ ...a, [questionId]: optionId }));
    try {
      await api.post(`/attempts/${attemptId}/answer`, { questionId, selectedOptionId: optionId });
    } catch {
      // autosave failures are non-fatal; the selection still gets sent again on next change/submit attempt
    }
  }

  async function handleSubmit() {
    setSubmitting(true);
    try {
      await api.post(`/attempts/${attemptId}/submit`);
      navigate(`/student/result/${attemptId}`);
    } catch (err) {
      alert(err.response?.data?.message || 'Could not submit');
      setSubmitting(false);
    }
  }

  function formatTime(s) {
    const m = Math.floor(s / 60).toString().padStart(2, '0');
    const sec = (s % 60).toString().padStart(2, '0');
    return `${m}:${sec}`;
  }

  return (
    <div className="container">
      <div className="row" style={{ alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>{data.testTitle}</h2>
        <div style={{ textAlign: 'right' }}>
          <span className="timer">{secondsLeft !== null ? formatTime(secondsLeft) : '--:--'}</span>
        </div>
      </div>

      {data.questions.map((q, idx) => (
        <div className="card" key={q.id}>
          <strong>Q{idx + 1}.</strong> {q.questionText}
          {q.questionImageUrl && (
            <div style={{ margin: '8px 0' }}>
              <img src={q.questionImageUrl} alt="question" style={{ maxWidth: '100%', borderRadius: 6 }} />
            </div>
          )}
          <p className="muted">{q.marks} mark(s)</p>
          {q.options.map((o) => (
            <div
              key={o.id}
              className={`option-row ${answers[q.id] === o.id ? 'selected' : ''}`}
              onClick={() => selectOption(q.id, o.id)}
            >
              <input type="radio" readOnly checked={answers[q.id] === o.id} style={{ width: 'auto', marginBottom: 0 }} />
              {o.optionText}
              {o.optionImageUrl && (
                <img src={o.optionImageUrl} alt="option" style={{ maxHeight: 60, marginLeft: 8 }} />
              )}
            </div>
          ))}
        </div>
      ))}

      <button className="btn" onClick={handleSubmit} disabled={submitting}>
        {submitting ? 'Submitting...' : 'Submit test'}
      </button>
    </div>
  );
}
