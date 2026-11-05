import React from 'react';
import { useSiteSettings } from '../../app/providers/SiteSettingsContext';
import './Footer.css';

export const Footer: React.FC = () => {
  const { settings } = useSiteSettings();

  const copyrightText =
    settings.copyright_text || `© ${new Date().getFullYear()} Melodick Recs. All rights reserved.`;

  // Filter valid URLs only (no # and no empty string)
  const isValidUrl = (url?: string) => {
    if (!url) return false;
    const trimmed = url.trim();
    return trimmed.length > 0 && trimmed !== '#' && (trimmed.startsWith('http://') || trimmed.startsWith('https://'));
  };

  const socials = [
    { label: 'SPOTIFY', url: settings.footer_spotify_url },
    { label: 'YOUTUBE', url: settings.footer_youtube_url },
    { label: 'INSTAGRAM', url: settings.footer_instagram_url },
    { label: 'TIKTOK', url: settings.footer_tiktok_url },
  ].filter((item) => isValidUrl(item.url));

  return (
    <footer className="public-footer">
      <div className="copyright">{copyrightText}</div>
      {socials.length > 0 && (
        <div className="social-links">
          {socials.map((social) => (
            <a
              key={social.label}
              href={social.url}
              target="_blank"
              rel="noopener noreferrer"
              aria-label={social.label}
            >
              {social.label}
            </a>
          ))}
        </div>
      )}
    </footer>
  );
};

