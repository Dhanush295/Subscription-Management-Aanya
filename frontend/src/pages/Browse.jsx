import { useEffect, useState } from 'react';
import { getMovies } from '../api.js';
import ErrorModal from '../components/ErrorModal.jsx';

const GENRES    = ['', 'Action', 'Drama', 'Thriller', 'Comedy', 'Romance'];
const LANGUAGES = ['', 'Kannada', 'English'];

export default function Browse() {
  const [movies, setMovies] = useState([]);
  const [lang, setLang]     = useState('');
  const [genre, setGenre]   = useState('');
  const [error, setError]   = useState(null);

  useEffect(() => {
    (async () => {
      const res = await getMovies(lang, genre);
      if (res.error) setError(res.error);
      else setMovies(Array.isArray(res) ? res : []);
    })();
  }, [lang, genre]);

  return (
    <div className="page">
      <h2>Browse Movies</h2>
      <div className="filters">
        <label>Language
          <select value={lang} onChange={(e) => setLang(e.target.value)}>
            {LANGUAGES.map((l) => <option key={l} value={l}>{l || 'All'}</option>)}
          </select>
        </label>
        <label>Genre
          <select value={genre} onChange={(e) => setGenre(e.target.value)}>
            {GENRES.map((g) => <option key={g} value={g}>{g || 'All'}</option>)}
          </select>
        </label>
      </div>
      <div className="grid">
        {movies.map((m) => (
          <div className="movie-card" key={m.id}>
            <div className="poster">{m.title.charAt(0)}</div>
            <div className="title">{m.title}</div>
            <div className="meta">{m.language} · {m.genre} · {m.year}</div>
            <div className="meta">⭐ {m.rating} · min {m.minPlan}</div>
          </div>
        ))}
        {movies.length === 0 && <div className="muted">No movies match those filters.</div>}
      </div>
      <ErrorModal message={error} onClose={() => setError(null)} />
    </div>
  );
}
