import React, { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  Sliders,
  LogOut,
  User,
  Menu,
  X,
  ExternalLink,
} from 'lucide-react';
import { useAuth } from '../app/providers/AuthContext';
import './AdminLayout.css';

export const AdminLayout: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate('/admin/login');
  };

  const closeSidebar = () => setSidebarOpen(false);

  return (
    <div className="admin-root">
      {/* Mobile Topbar */}
      <header className="admin-mobile-bar">
        <button
          className="admin-menu-toggle"
          onClick={() => setSidebarOpen((prev) => !prev)}
          aria-label="Toggle navigation"
        >
          {sidebarOpen ? <X size={20} /> : <Menu size={20} />}
        </button>
        <span className="admin-mobile-title">MELODICK CMS</span>
        <a
          href="/"
          target="_blank"
          rel="noopener noreferrer"
          className="admin-link-public"
          title="Xem Public Site"
        >
          <ExternalLink size={18} />
        </a>
      </header>

      {/* Sidebar Backdrop on Mobile */}
      {sidebarOpen && (
        <div className="admin-sidebar-backdrop" onClick={closeSidebar} aria-hidden="true" />
      )}

      {/* Sidebar */}
      <aside className={`admin-sidebar ${sidebarOpen ? 'sidebar-open' : ''}`}>
        <div className="sidebar-brand">
          <img src="/logo.png" alt="MELODICK RECS." className="sidebar-logo-img" />
          <span className="brand-badge">CMS</span>
        </div>

        <nav className="sidebar-nav">
          <NavLink
            to="/admin"
            end
            onClick={closeSidebar}
            className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
          >
            <LayoutDashboard size={18} />
            <span>Dashboard</span>
          </NavLink>

          <NavLink
            to="/admin/artists"
            onClick={closeSidebar}
            className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
          >
            <Users size={18} />
            <span>Quản lý Nghệ sĩ</span>
          </NavLink>

          <NavLink
            to="/admin/settings"
            onClick={closeSidebar}
            className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
          >
            <Sliders size={18} />
            <span>Cài đặt Landing Page</span>
          </NavLink>
        </nav>

        <div className="sidebar-footer">
          <div className="admin-user-profile">
            <div className="admin-avatar">
              <User size={18} />
            </div>
            <div className="admin-details">
              <span className="admin-name">{user?.displayName || user?.username || 'Admin'}</span>
              <span className="admin-role">Administrator</span>
            </div>
          </div>

          <div className="sidebar-actions">
            <a
              href="/"
              target="_blank"
              rel="noopener noreferrer"
              className="sidebar-public-btn"
              title="Mở Public Site"
            >
              <ExternalLink size={16} />
              <span>Xem Landing</span>
            </a>

            <button onClick={handleLogout} className="sidebar-logout-btn" title="Đăng xuất">
              <LogOut size={16} />
              <span>Đăng xuất</span>
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="admin-main">
        <div className="admin-container">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

