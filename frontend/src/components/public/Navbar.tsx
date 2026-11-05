import React, { useState } from 'react';
import { NavLink, Link } from 'react-router-dom';
import { Menu, X } from 'lucide-react';
import { useSiteSettings } from '../../app/providers/SiteSettingsContext';
import './Navbar.css';

export const Navbar: React.FC = () => {
  const { settings } = useSiteSettings();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const brandName = settings.brand_name || 'MELODICK RECS.';

  const closeMenu = () => setMobileMenuOpen(false);

  return (
    <header className="public-header">
      <nav className="public-nav">
        <Link to="/" className="nav-brand" onClick={closeMenu}>
          {brandName}
        </Link>

        {/* Desktop Links */}
        <div className="nav-links">
          <NavLink
            to="/"
            end
            className={({ isActive }) => `nav-item ${isActive ? 'active-link' : ''}`}
          >
            Home
          </NavLink>
          <NavLink
            to="/artists"
            className={({ isActive }) => `nav-item ${isActive ? 'active-link' : ''}`}
          >
            Artists
          </NavLink>
          <NavLink
            to="/submit"
            className={({ isActive }) => `nav-item ${isActive ? 'active-link' : ''}`}
          >
            Submit
          </NavLink>
        </div>

        {/* Mobile menu toggle button */}
        <button
          className="mobile-toggle"
          onClick={() => setMobileMenuOpen((prev) => !prev)}
          aria-label={mobileMenuOpen ? 'Close menu' : 'Open menu'}
        >
          {mobileMenuOpen ? <X size={22} /> : <Menu size={22} />}
        </button>
      </nav>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <div className="mobile-nav-drawer">
          <NavLink
            to="/"
            end
            onClick={closeMenu}
            className={({ isActive }) => `mobile-nav-item ${isActive ? 'active-link' : ''}`}
          >
            Home
          </NavLink>
          <NavLink
            to="/artists"
            onClick={closeMenu}
            className={({ isActive }) => `mobile-nav-item ${isActive ? 'active-link' : ''}`}
          >
            Artists
          </NavLink>
          <NavLink
            to="/submit"
            onClick={closeMenu}
            className={({ isActive }) => `mobile-nav-item ${isActive ? 'active-link' : ''}`}
          >
            Submit
          </NavLink>
        </div>
      )}
    </header>
  );
};
