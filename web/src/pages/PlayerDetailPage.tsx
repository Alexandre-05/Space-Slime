import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  api,
  formatDate,
  formatDurationLabel,
  formatEpochMs,
  formatPlaytime,
  formatSanctionDuration,
  formatSanctionRemaining,
  isSanctionActive,
  PlayerDetail,
  Sanction,
} from '../api';

type Tab = 'bans' | 'mutes' | 'kicks' | 'warns' | 'notes' | 'sessions';
type DurationPreset = '30m' | '1h' | '1d' | '7d' | '30d' | 'perm' | 'custom';
type Feedback = { type: 'success' | 'error'; text: string };

const DURATION_PATTERN = /^\d+[smhdwy]$/i;

function resolveDuration(preset: DurationPreset, custom: string): string {
  if (preset === 'perm') return 'perm';
  if (preset === 'custom') return custom.trim();
  return preset;
}

function isValidDuration(value: string): boolean {
  if (!value || value.toLowerCase() === 'perm') return true;
  return DURATION_PATTERN.test(value.trim());
}

export default function PlayerDetailPage() {
  const { uuid } = useParams<{ uuid: string }>();
  const [player, setPlayer] = useState<PlayerDetail | null>(null);
  const [tab, setTab] = useState<Tab>('bans');
  const [error, setError] = useState('');
  const [reason, setReason] = useState('');
  const [durationPreset, setDurationPreset] = useState<DurationPreset>('1d');
  const [customDuration, setCustomDuration] = useState('');
  const [note, setNote] = useState('');
  const [feedback, setFeedback] = useState<Feedback | null>(null);

  function reload() {
    if (!uuid) return;
    api.player(uuid).then(setPlayer).catch((e) => setError(e.message));
  }

  useEffect(reload, [uuid]);

  function durationForSanction(): string | null {
    const duration = resolveDuration(durationPreset, customDuration);
    if (!isValidDuration(duration)) {
      setFeedback({ type: 'error', text: 'Durée invalide. Exemples : 45m, 3h, 14d, 2w, perm' });
      return null;
    }
    return duration;
  }

  async function runAction(action: () => Promise<void>, successMessage: string) {
    setFeedback(null);
    try {
      await action();
      setFeedback({ type: 'success', text: successMessage });
      setReason('');
      setNote('');
      reload();
    } catch (e) {
      setFeedback({ type: 'error', text: e instanceof Error ? e.message : 'Erreur' });
    }
  }

  async function runTimedAction(
    action: (duration: string) => Promise<void>,
    successMessage: (duration: string) => string,
  ) {
    const duration = durationForSanction();
    if (duration === null) return;
    await runAction(() => action(duration), successMessage(duration));
  }

  if (error) return <p className="error">{error}</p>;
  if (!player) return <p className="muted">Chargement...</p>;

  const isFounder = localStorage.getItem('role') === 'FONDATEUR';
  const activeBan = player.bans.find(isSanctionActive);
  const activeMute = player.mutes.find(isSanctionActive);
  const activeWarnCount = player.warns.filter((w) => w.active).length;

  return (
    <div>
      <div className="page-header">
        <div>
          <Link to="/players" className="muted">← Retour</Link>
          <h2 style={{ marginTop: '0.5rem' }}>{player.currentName}</h2>
        </div>
      </div>

      <div className="card" style={{ marginBottom: '1rem' }}>
        <p><span className="muted">UUID :</span> {player.uuid}</p>
        <p><span className="muted">Première connexion :</span> {formatDate(player.firstSeen)}</p>
        <p><span className="muted">Dernière connexion :</span> {formatDate(player.lastSeen)}</p>
        <p><span className="muted">Temps de jeu :</span> {formatPlaytime(player.totalPlaytime)}</p>
        {isFounder && player.ip && <p><span className="muted">IP :</span> {player.ip}</p>}
      </div>

      {(activeBan || activeMute || activeWarnCount > 0) && (
        <div className="card" style={{ marginBottom: '1rem' }}>
          <h3 style={{ marginBottom: '1rem' }}>Sanctions en cours</h3>
          <div className="active-sanctions-list">
            {activeBan && <ActiveSanctionRow type="Ban" badgeClass="badge-ban" sanction={activeBan} />}
            {activeMute && <ActiveSanctionRow type="Mute" badgeClass="badge-mute" sanction={activeMute} />}
            {activeWarnCount > 0 && (
              <div className="active-sanction">
                <span className="badge badge-warn">Warn</span>
                <span>{activeWarnCount} avertissement{activeWarnCount > 1 ? 's' : ''} actif{activeWarnCount > 1 ? 's' : ''}</span>
              </div>
            )}
          </div>
        </div>
      )}

      <div className="card" style={{ marginBottom: '1rem' }}>
        <h3 style={{ marginBottom: '1rem' }}>Actions</h3>
        <input placeholder="Raison" value={reason} onChange={(e) => setReason(e.target.value)} />
        <div className="duration-row">
          <select
            value={durationPreset}
            onChange={(e) => setDurationPreset(e.target.value as DurationPreset)}
          >
            <option value="30m">30 minutes</option>
            <option value="1h">1 heure</option>
            <option value="1d">1 jour</option>
            <option value="7d">7 jours</option>
            <option value="30d">30 jours</option>
            <option value="perm">Permanent</option>
            <option value="custom">Personnalisé</option>
          </select>
          {durationPreset === 'custom' && (
            <input
              placeholder="Ex: 45m, 3h, 14d, 2w"
              value={customDuration}
              onChange={(e) => setCustomDuration(e.target.value)}
            />
          )}
        </div>
        {durationPreset === 'custom' && (
          <p className="muted duration-hint">
            Format : nombre + unité — s (sec), m (min), h (heure), d (jour), w (semaine), y (année)
          </p>
        )}
        <div className="actions">
          <button
            className="danger"
            onClick={() => runTimedAction(
              (duration) => api.ban(player.uuid, reason || 'Ban panel', duration),
              (duration) => duration.toLowerCase() === 'perm'
                ? 'Ban permanent appliqué.'
                : `Ban appliqué pour ${formatDurationLabel(duration)}.`,
            )}
          >
            Ban
          </button>
          <button
            className="secondary"
            onClick={() => runAction(() => api.unban(player.uuid), 'Ban retiré.')}
          >
            Unban
          </button>
          <button
            onClick={() => runTimedAction(
              (duration) => api.mute(player.uuid, reason || 'Mute panel', duration),
              (duration) => duration.toLowerCase() === 'perm'
                ? 'Mute permanent appliqué.'
                : `Mute appliqué pour ${formatDurationLabel(duration)}.`,
            )}
          >
            Mute
          </button>
          <button
            className="secondary"
            onClick={() => runAction(() => api.unmute(player.uuid), 'Mute retiré.')}
          >
            Unmute
          </button>
          <button
            onClick={() => runAction(() => api.warn(player.uuid, reason || 'Warn panel'), 'Avertissement ajouté.')}
          >
            Warn
          </button>
        </div>
        <textarea placeholder="Note staff..." value={note} onChange={(e) => setNote(e.target.value)} rows={3} />
        <button onClick={() => runAction(() => api.note(player.uuid, note), 'Note ajoutée.')}>Ajouter note</button>
        {feedback && (
          <p className={feedback.type === 'success' ? 'success' : 'error'}>{feedback.text}</p>
        )}
      </div>

      <div className="tabs">
        {(['bans', 'mutes', 'kicks', 'warns', 'notes', 'sessions'] as Tab[]).map((t) => (
          <button key={t} className={`tab${tab === t ? ' active' : ''}`} onClick={() => setTab(t)}>
            {t.charAt(0).toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      <div className="card">
        {tab === 'bans' && <SanctionTable items={player.bans} />}
        {tab === 'mutes' && <SanctionTable items={player.mutes} />}
        {tab === 'kicks' && (
          <table>
            <thead><tr><th>Raison</th><th>Staff</th><th>Date</th><th>Source</th></tr></thead>
            <tbody>
              {player.kicks.map((k) => (
                <tr key={k.id}><td>{k.reason}</td><td>{k.staffName}</td><td>{formatDate(k.createdAt)}</td><td>{k.source}</td></tr>
              ))}
            </tbody>
          </table>
        )}
        {tab === 'warns' && (
          <table>
            <thead><tr><th>Raison</th><th>Staff</th><th>Date</th><th>Actif</th></tr></thead>
            <tbody>
              {player.warns.map((w) => (
                <tr key={w.id}><td>{w.reason}</td><td>{w.staffName}</td><td>{formatDate(w.createdAt)}</td><td>{w.active ? 'Oui' : 'Non'}</td></tr>
              ))}
            </tbody>
          </table>
        )}
        {tab === 'notes' && (
          <table>
            <thead><tr><th>Note</th><th>Staff</th><th>Date</th></tr></thead>
            <tbody>
              {player.notes.map((n) => (
                <tr key={n.id}><td>{n.content}</td><td>{n.staffName}</td><td>{formatDate(n.createdAt)}</td></tr>
              ))}
            </tbody>
          </table>
        )}
        {tab === 'sessions' && (
          <table>
            <thead><tr><th>Connexion</th><th>Déconnexion</th>{isFounder && <th>IP hash</th>}</tr></thead>
            <tbody>
              {player.sessions.map((s) => (
                <tr key={s.id}>
                  <td>{formatDate(s.joinAt)}</td>
                  <td>{s.quitAt ? formatDate(s.quitAt) : '—'}</td>
                  {isFounder && <td>{s.ipHash || '—'}</td>}
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

function ActiveSanctionRow({
  type,
  badgeClass,
  sanction,
}: {
  type: string;
  badgeClass: string;
  sanction: Sanction;
}) {
  const remaining = formatSanctionRemaining(sanction);
  const isPermanent = remaining === 'Permanent';

  return (
    <div className="active-sanction">
      <span className={`badge ${badgeClass}`}>{type}</span>
      <div className="active-sanction-timing">
        <span>
          {isPermanent ? 'Sanction permanente' : `Temps restant : ${remaining}`}
        </span>
        {!isPermanent && sanction.expiresAtEpochMs != null && (
          <span className="muted">Fin le {formatEpochMs(sanction.expiresAtEpochMs)}</span>
        )}
      </div>
      <span className="active-sanction-reason">Raison : {sanction.reason}</span>
      {sanction.staffName && <span className="muted">par {sanction.staffName}</span>}
    </div>
  );
}

function SanctionTable({ items }: { items: Sanction[] }) {
  return (
    <table>
      <thead><tr><th>Raison</th><th>Staff</th><th>Date</th><th>Durée</th><th>Expire</th><th>Actif</th><th>Source</th></tr></thead>
      <tbody>
        {items.map((s) => (
          <tr key={s.id}>
            <td>{s.reason}</td>
            <td>{s.staffName}</td>
            <td>{formatDate(s.createdAt)}</td>
            <td>{formatSanctionDuration(s)}</td>
            <td>{s.expiresAtEpochMs != null ? formatEpochMs(s.expiresAtEpochMs) : 'Permanent'}</td>
            <td>{s.active ? 'Oui' : 'Non'}</td>
            <td>{s.source}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
