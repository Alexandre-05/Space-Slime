import { NavLink, Outlet, useNavigate } from 'react-router-dom';

export default function Layout() {
  const navigate = useNavigate();
  const username = localStorage.getItem('username') || 'Staff';
  const role = localStorage.getItem('role') || '';

  function logout() {
    localStorage.clear();
    navigate('/login');
  }

  return (
    <div className="layout">
      <aside className="sidebar">
        <h1>Admin Panel</h1>
        <NavLink to="/" end className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
          Dashboard
        </NavLink>
        <NavLink to="/players" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
          Joueurs
        </NavLink>
        <div style={{ marginTop: 'auto', paddingTop: '2rem' }}>
          <p className="muted" style={{ fontSize: '0.8rem' }}>Horaires affichés en UTC</p>
          <p className="muted">{username}</p>
          <p className="muted">{role}</p>
          <button className="secondary" style={{ width: '100%', marginTop: '0.5rem' }} onClick={logout}>
            Déconnexion
          </button>
        </div>
      </aside>
      <main className="main">
        <Outlet />
      </main>
    </div>
  );
}
