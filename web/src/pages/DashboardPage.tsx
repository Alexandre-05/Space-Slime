import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, Dashboard, formatDate, formatSanctionDuration } from '../api';

export default function DashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.dashboard().then(setData).catch((e) => setError(e.message));
  }, []);

  if (error) return <p className="error">{error}</p>;
  if (!data) return <p className="muted">Chargement...</p>;

  return (
    <div>
      <div className="page-header">
        <h2>Dashboard</h2>
      </div>

      <div className="card-grid">
        <div className="card">
          <div className="stat-label">Joueurs enregistrés</div>
          <div className="stat-value">{data.totalPlayers}</div>
        </div>
        <div className="card">
          <div className="stat-label">Bans actifs</div>
          <div className="stat-value">{data.activeBans}</div>
        </div>
        <div className="card">
          <div className="stat-label">Mutes actifs</div>
          <div className="stat-value">{data.activeMutes}</div>
        </div>
        <div className="card">
          <div className="stat-label">Sanctions (24h)</div>
          <div className="stat-value">{data.sanctionsLast24h}</div>
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginBottom: '1rem' }}>Bans actifs récents</h3>
        {data.recentSanctions.length === 0 ? (
          <p className="muted">Aucun ban actif.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Raison</th>
                <th>Staff</th>
                <th>Date</th>
                <th>Durée</th>
              </tr>
            </thead>
            <tbody>
              {data.recentSanctions.map((s) => (
                <tr key={s.id}>
                  <td>{s.reason}</td>
                  <td>{s.staffName || '—'}</td>
                  <td>{formatDate(s.createdAt)}</td>
                  <td>{formatSanctionDuration(s)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        <div style={{ marginTop: '1rem' }}>
          <Link to="/players">Voir tous les joueurs →</Link>
        </div>
      </div>
    </div>
  );
}
