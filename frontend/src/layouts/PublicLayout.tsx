import React from 'react';
import { Outlet } from 'react-router-dom';
import { CursorGlow } from '../components/common/CursorGlow';
import { Navbar } from '../components/public/Navbar';
import { Footer } from '../components/public/Footer';
import { SiteSettingsProvider } from '../app/providers/SiteSettingsContext';
import './PublicLayout.css';

export const PublicLayout: React.FC = () => {
  return (
    <SiteSettingsProvider>
      <div className="public-layout-container">
        <CursorGlow />
        <Navbar />
        <main className="public-main-content">
          <Outlet />
        </main>
        <Footer />
      </div>
    </SiteSettingsProvider>
  );
};
