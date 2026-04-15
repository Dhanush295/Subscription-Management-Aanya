import { useEffect, useState } from 'react';
import { getMovies } from '../api.js';
import ErrorModal from '../components/ErrorModal.jsx';

const GENRES    = ['', 'Action', 'Drama', 'Thriller', 'Comedy', 'Romance'];
const LANGUAGES = ['', 'Kannada', 'English'];

// Hardcoded external image links based on DB mappings
const POSTER_LINKS = {
  'm01': '/images/media__1776290158790.jpg',
  'm07': '/images/media__1776290316449.jpg',
  'm06': '/images/media__1776290280354.jpg',
  'm02': '/images/media__1776290207351.jpg',
  'm03': '/images/media__1776290230064.jpg',
  'm04': '/images/media__1776292153836.png',
  'm05': '/images/media__1776292131822.png',
  'm08': '/images/media__1776292609587.png',
  'm09': 'https://upload.wikimedia.org/wikipedia/en/8/81/ShawshankRedemptionMoviePoster.jpg',
  'm10': 'https://upload.wikimedia.org/wikipedia/en/6/68/Seven_%28movie%29_poster.jpg',
  'm11': '/images/media__1776292391975.jpg',
  'm12': '/images/media__1776293028306.jpg',
  'm13': 'https://upload.wikimedia.org/wikipedia/en/2/2e/Inception_%282010%29_theatrical_poster.jpg',
  'm14': '/images/media__1776292301620.jpg',
  'm15': 'https://upload.wikimedia.org/wikipedia/en/9/98/John_Wick_TeaserPoster.jpg'
};

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

  const trendingIds = ['m14', 'm02', 'm12', 'm11', 'm04', 'm08'];
  const trendingMovies = trendingIds.map(id => movies.find(m => m.id === id)).filter(Boolean);

  const getPoster = (m) => POSTER_LINKS[m.id] || `https://placehold.co/400x600/222/FFF?text=${encodeURIComponent(m.title)}`;

  return (
    <div className="page netflix-browse">
      <h2 className="trending-title">Trending now</h2>
      <div className="trending-carousel">
        {trendingMovies.map((m, idx) => (
          <div className="trending-card" key={idx}>
            <div className="trending-number">{idx + 1}</div>
            <div className="trending-poster-wrapper">
              <img 
                src={getPoster(m)} 
                alt={m.title} 
                className="trending-poster"
                onError={(e) => { e.target.src = `https://placehold.co/400x600/333/FFF?text=${encodeURIComponent(m.title)}` }} 
              />
              <div className="n-logo">N</div>
            </div>
          </div>
        ))}
      </div>

      <h2 className="reasons-title">More reasons to join</h2>
      <div className="reasons-grid">
        <div className="reason-card gradient-1">
          <h3>Enjoy on your TV</h3>
          <p>Watch on smart TVs, PlayStation, Xbox, Chromecast, Apple TV, Blu-ray players and more.</p>
        </div>
        <div className="reason-card gradient-2">
          <h3>Download your series to watch offline</h3>
          <p>Save your favourites easily and always have something to watch.</p>
        </div>
        <div className="reason-card gradient-3">
          <h3>Watch everywhere</h3>
          <p>Stream unlimited films and series on your phone, tablet, laptop and TV.</p>
        </div>
        <div className="reason-card gradient-4">
          <h3>Create profiles for children</h3>
          <p>Send children on adventures with their favourite characters in a space made just for them — free with your membership.</p>
        </div>
      </div>

      <div className="full-catalog">
        <h3 className="reasons-title" style={{marginTop: '40px'}}>Full Catalog</h3>
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
              <div className="poster-catalog">
                 <img src={getPoster(m)} alt={m.title} onError={(e) => { e.target.src = `https://placehold.co/400x600/222/FFF?text=${encodeURIComponent(m.title)}` }} />
              </div>
              <div className="title">{m.title}</div>
              <div className="meta">{m.language} · {m.genre} · {m.year}</div>
              <div className="meta">⭐ {m.rating} · min {m.minPlan}</div>
            </div>
          ))}
          {movies.length === 0 && <div className="muted">No movies match those filters.</div>}
        </div>
      </div>
      <ErrorModal message={error} onClose={() => setError(null)} />
    </div>
  );
}
