import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, formatDate, formatPlaytime, PlayerSummary } from '../api';

export default function PlayersPage() {
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [players, setPlayers] = useState<PlayerSummary[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState('');

  useEffect(() => {
    api.players(search, page)
      .then((res) => {
        setPlayers(res.content);
        setTotalPages(res.totalPages);
      })
      .catch((e) => setError(e.message));
  }, [search, page]);

  return (
    <div>
      <div className="page-header">
        <h2>Joueurs</h2>
      </div>

      <div className="card" style={{ marginBottom: '1rem' }}>
        <input
          placeholder="Rechercher par pseudo ou UUID..."
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0); }}
        />
      </div>

      {error && <p className="error">{error}</p>}

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>Pseudo</th>
              <th>Dernière connexion</th>
              <th>Temps de jeu</th>
              <th>Sanctions</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {players.map((p) => (
              <tr key={p.uuid}>
                <td>{p.currentName}</td>
                <td>{formatDate(p.lastSeen)}</td>
                <td>{formatPlaytime(p.totalPlaytime)}</td>
                <td>
                  {p.banned && <span className="badge badge-ban">Ban</span>}{' '}
                  {p.muted && <span className="badge badge-mute">Mute</span>}{' '}
                  {p.warnCount > 0 && <span className="badge badge-warn">{p.warnCount} warn(s)</span>}
                </td>
                <td><Link to={`/players/${p.uuid}`}>Voir</Link></td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="pagination">
          <button className="secondary" disabled={page <= 0} onClick={() => setPage(page - 1)}>Précédent</button>
          <span className="muted">Page {page + 1} / {Math.max(totalPages, 1)}</span>
          <button className="secondary" disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>Suivant</button>
        </div>
      </div>
    </div>
  );
}
