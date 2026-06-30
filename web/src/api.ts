const API = '/api';

export interface LoginResponse {
  token: string;
  username: string;
  role: string;
}

export interface PlayerSummary {
  uuid: string;
  currentName: string;
  firstSeen: string;
  lastSeen: string;
  totalPlaytime: number;
  banned: boolean;
  muted: boolean;
  warnCount: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Sanction {
  id: number;
  reason: string;
  staffName: string;
  createdAt: string;
  expiresAt: string | null;
  active: boolean;
  source: string;
  remainingSeconds: number | null;
  expiresAtEpochMs: number | null;
  durationSeconds: number | null;
}

export interface PlayerDetail {
  uuid: string;
  currentName: string;
  firstSeen: string;
  lastSeen: string;
  totalPlaytime: number;
  ip: string | null;
  nameHistory: { name: string; changedAt: string }[];
  bans: Sanction[];
  mutes: Sanction[];
  kicks: { id: number; reason: string; staffName: string; createdAt: string; source: string }[];
  warns: { id: number; reason: string; staffName: string; createdAt: string; active: boolean; source: string }[];
  notes: { id: number; content: string; staffName: string; createdAt: string }[];
  sessions: { id: number; joinAt: string; quitAt: string | null; ipHash: string | null }[];
}

export interface Dashboard {
  totalPlayers: number;
  activeBans: number;
  activeMutes: number;
  sanctionsLast24h: number;
  recentSanctions: Sanction[];
}

function authHeaders(): HeadersInit {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' } : { 'Content-Type': 'application/json' };
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const res = await fetch(`${API}${path}`, { ...options, headers: { ...authHeaders(), ...options.headers } });
  if (res.status === 401) {
    localStorage.removeItem('token');
    window.location.href = '/login';
    throw new Error('Non authentifié');
  }
  if (!res.ok) {
    const err = await res.text().then((text) => {
      if (!text) return { error: 'Erreur serveur' };
      try {
        return JSON.parse(text);
      } catch {
        return { error: text || 'Erreur serveur' };
      }
    });
    throw new Error(err.error || 'Erreur serveur');
  }
  if (res.status === 204) return undefined as T;
  const text = await res.text();
  if (!text) return undefined as T;
  return JSON.parse(text) as T;
}

export const api = {
  login: (username: string, password: string) =>
    request<LoginResponse>('/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) }),

  dashboard: () => request<Dashboard>('/dashboard'),

  players: (search: string, page: number) =>
    request<PageResponse<PlayerSummary>>(`/players?search=${encodeURIComponent(search)}&page=${page}&size=20`),

  player: (uuid: string) => request<PlayerDetail>(`/players/${uuid}`),

  ban: (uuid: string, reason: string, duration: string) =>
    request<void>(`/sanctions/${uuid}/ban`, { method: 'POST', body: JSON.stringify({ reason, duration }) }),

  unban: (uuid: string) => request<void>(`/sanctions/${uuid}/unban`, { method: 'POST' }),

  mute: (uuid: string, reason: string, duration: string) =>
    request<void>(`/sanctions/${uuid}/mute`, { method: 'POST', body: JSON.stringify({ reason, duration }) }),

  unmute: (uuid: string) => request<void>(`/sanctions/${uuid}/unmute`, { method: 'POST' }),

  warn: (uuid: string, reason: string) =>
    request<void>(`/sanctions/${uuid}/warn`, { method: 'POST', body: JSON.stringify({ reason, duration: null }) }),

  note: (uuid: string, content: string) =>
    request<void>(`/sanctions/${uuid}/note`, { method: 'POST', body: JSON.stringify({ content }) }),
};

const UTC_FORMAT: Intl.DateTimeFormatOptions = {
  timeZone: 'UTC',
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
  hour12: false,
};

export function formatDate(iso: string) {
  return `${new Date(iso).toLocaleString('fr-FR', UTC_FORMAT)} UTC`;
}

/** Date/heure UTC (epoch ms depuis 1970-01-01 00:00:00 UTC). */
export function formatEpochMs(epochMs: number | null | undefined): string {
  if (epochMs == null) return 'Permanent';
  return `${new Date(epochMs).toLocaleString('fr-FR', UTC_FORMAT)} UTC`;
}

export function formatPlaytime(seconds: number) {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  return h > 0 ? `${h}h ${m}m` : `${m}m`;
}

export function formatRemainingSeconds(totalSeconds: number): string {
  if (totalSeconds <= 0) return 'Expiré';

  const days = Math.floor(totalSeconds / 86400);
  const hours = Math.floor((totalSeconds % 86400) / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  if (days > 0) return `${days}j ${hours}h`;
  if (hours > 0) return `${hours}h ${minutes}m`;
  if (minutes > 0) return `${minutes}m ${seconds}s`;
  return `${seconds}s`;
}

export function formatSanctionDuration(sanction: Sanction): string {
  if (sanction.durationSeconds == null) return 'Permanent';
  return formatRemainingSeconds(sanction.durationSeconds);
}

export function formatSanctionRemaining(sanction: Sanction): string {
  if (!sanction.active) return 'Expiré';
  if (sanction.expiresAtEpochMs == null) return 'Permanent';
  const remainingMs = sanction.expiresAtEpochMs - Date.now();
  if (remainingMs <= 0) return 'Expiré';
  return formatRemainingSeconds(Math.floor(remainingMs / 1000));
}

export function formatRemaining(expiresAt: string | null): string {
  if (!expiresAt) return 'Permanent';
  const end = Date.parse(expiresAt);
  const remainingMs = end - Date.now();
  if (remainingMs <= 0) return 'Expiré';

  return formatRemainingSeconds(Math.floor(remainingMs / 1000));
}

export function formatDurationLabel(duration: string): string {
  if (!duration || duration.toLowerCase() === 'perm') return 'permanent';
  const match = duration.trim().match(/^(\d+)([smhdwy])$/i);
  if (!match) return duration;

  const amount = Number(match[1]);
  const unit = match[2].toLowerCase();
  const labels: Record<string, [string, string]> = {
    s: ['seconde', 'secondes'],
    m: ['minute', 'minutes'],
    h: ['heure', 'heures'],
    d: ['jour', 'jours'],
    w: ['semaine', 'semaines'],
    y: ['année', 'années'],
  };
  const [singular, plural] = labels[unit] ?? ['', ''];
  return `${amount} ${amount > 1 ? plural : singular}`;
}

export function isSanctionActive(sanction: Sanction): boolean {
  if (!sanction.active) return false;
  if (sanction.expiresAtEpochMs != null) return sanction.expiresAtEpochMs > Date.now();
  if (!sanction.expiresAt) return true;
  return Date.parse(sanction.expiresAt) > Date.now();
}
