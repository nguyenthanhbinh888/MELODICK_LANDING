import React from 'react';
import { useSiteSettings } from '../../app/providers/SiteSettingsContext';
import './HomePage.css';

export const HomePage: React.FC = () => {
  const { settings } = useSiteSettings();

  const heroTitle = settings.hero_title || settings.brand_name || 'MELODICK RECS.';
  const tagline = settings.tagline || 'Architects of the underground sound.';

  return (
    <section className="public-page-section" id="home">
      <div className="hero-container">
        <h1 className="hero-title hero-title-flicker">
          {heroTitle}
        </h1>
        <p className="hero-tagline">{tagline}</p>
      </div>
    </section>
  );
};

