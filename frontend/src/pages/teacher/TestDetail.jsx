import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api/axios.js';

const BLANK_OPTIONS = [
  { optionText: '', correct: true },
  { optionText: '', correct: false },
  { optionText: '', correct: false },
  { optionText: '', correct: false },
];

export default function TestDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [test, setTest] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  // New-question form state
  const [questionText, setQuestionText] = useState('');
  const [marks, setMarks] = useState(1);
  const [options, setOptions] = useState(BLANK_OPTIONS.map((o) => ({ ...o })));
  const [tagInput, setTagInput] = useState(''); // comma-separated, optional
  const [imageFile, setImageFile] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  function loadTest() {
    setLoading(true);
    api.get(`/tests/${id}`).then((res) => setTest(res.data)).finally(() => setLoading(false));
  }

  useEffect(() => {
    loadTest();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  function updateOptionText(index, value) {
    setOptions((opts) => opts.map((o, i) => (i === index ? { ...o, optionText: value } : o)));
  }

  function markCorrect(index) {
    setOptions((opts) => opts.map((o, i) => ({ ...o, correct: i === index })));
  }

  function resetForm() {
    setQuestionText('');
    setMarks(1);
    setOptions(BLANK_OPTIONS.map((o) => ({ ...o })));
    setTagInput('');
    setImageFile(null);
  }

  async function handleAddQuestion(e) {
    e.preventDefault();
    setError('');

    if (!questionText.trim() && !imageFile) {
      setError('Add question text and/or an image.');
      return;
    }
    if (options.some((o) => !o.optionText.trim())) {
      setError('All 4 options need text.');
      return;
    }

    setSubmitting(true);
    try {
      // tags are entirely optional -- empty input means no tags on this question
      const tagNames = tagInput
        .split(',')
        .map((t) => t.trim())
        .filter((t) => t.length > 0);

      const payload = { questionText, marks: Number(marks), options, tagNames };

      const formData = new FormData();
      formData.append('question', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
      if (imageFile) formData.append('image', imageFile);

      await api.post(`/tests/${id}/questions`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });

      resetForm();
      loadTest();
    } catch (err) {
      setError(err.response?.data?.message || 'Could not add question');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDeleteQuestion(questionId) {
    if (!confirm('Delete this question?')) return;
    await api.delete(`/tests/${id}/questions/${questionId}`);
    loadTest();
  }

  async function handlePublish() {
    await api.put(`/tests/${id}/publish`);
    loadTest();
  }

  async function handleUnpublish() {
    await api.put(`/tests/${id}/unpublish`);
    loadTest();
  }

  if (loading) return <div className="container">Loading...</div>;
  if (!test) return <div className="container">Test not found.</div>;

  return (
    <div className="container">
      <button className="btn secondary" onClick={() => navigate('/teacher')}>&larr; Back</button>
      <h2>{test.title}</h2>
      <p className="muted">{test.description}</p>
      <p className="muted">
        {test.durationMinutes} min &middot; {test.published ? 'Published' : 'Draft'}
      </p>

      {test.published ? (
        <button className="btn danger" onClick={handleUnpublish}>Unpublish</button>
      ) : (
        <button className="btn" onClick={handlePublish} disabled={test.questions.length === 0}>
          Publish test
        </button>
      )}

      <h3 style={{ marginTop: 28 }}>Questions ({test.questions.length})</h3>
      {test.questions.map((q, idx) => (
        <div className="card" key={q.id}>
          <strong>Q{idx + 1}.</strong> {q.questionText}
          {q.questionImageUrl && (
            <div style={{ margin: '8px 0' }}>
              <img src={q.questionImageUrl} alt="question" style={{ maxWidth: '100%', borderRadius: 6 }} />
            </div>
          )}
          <p className="muted">{q.marks} mark(s)</p>
          <ul>
            {q.options.map((o) => (
              <li key={o.id} style={{ color: o.correct ? '#2eb872' : undefined, fontWeight: o.correct ? 600 : 400 }}>
                {o.optionText} {o.correct && '(correct)'}
              </li>
            ))}
          </ul>
          {q.tags.length > 0 && (
            <div>
              {q.tags.map((t) => (
                <span className="tag" key={t}>{t}</span>
              ))}
            </div>
          )}
          {!test.published && (
            <button className="btn danger" onClick={() => handleDeleteQuestion(q.id)}>Delete</button>
          )}
        </div>
      ))}

      {!test.published && (
        <div className="card">
          <h3>Add a question</h3>
          {error && <div className="error">{error}</div>}
          <form onSubmit={handleAddQuestion}>
            <label>Question text (optional if you attach an image)</label>
            <textarea rows={2} value={questionText} onChange={(e) => setQuestionText(e.target.value)} />

            <label>Question image (optional)</label>
            <input type="file" accept="image/png,image/jpeg,image/webp" onChange={(e) => setImageFile(e.target.files[0])} />

            <label>Marks</label>
            <input type="number" min={1} value={marks} onChange={(e) => setMarks(e.target.value)} />

            <label>Options (select the correct one)</label>
            {options.map((o, i) => (
              <div className="row" key={i} style={{ marginBottom: 8, alignItems: 'center' }}>
                <input
                  type="radio"
                  name="correct-option"
                  style={{ width: 'auto', marginBottom: 0 }}
                  checked={o.correct}
                  onChange={() => markCorrect(i)}
                />
                <input
                  placeholder={`Option ${i + 1}`}
                  value={o.optionText}
                  onChange={(e) => updateOptionText(i, e.target.value)}
                  style={{ marginBottom: 0 }}
                />
              </div>
            ))}

            <label>Topic tags (optional, comma-separated -- e.g. "Arrays, Recursion")</label>
            <input
              placeholder="Leave blank for no tags"
              value={tagInput}
              onChange={(e) => setTagInput(e.target.value)}
            />
            <p className="muted" style={{ marginTop: -6 }}>
              Tags are never shown to students during the test -- only in their result after they submit.
            </p>

            <button className="btn" type="submit" disabled={submitting}>
              {submitting ? 'Adding...' : 'Add question'}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
