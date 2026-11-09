import React, { useEffect, useState, useId } from 'react';
import type { Artist, ArtistLink } from '../../types';
import { publicApi } from '../../services/api';
import {
  isValidUrl,
  getPlatformActionLabel,
  getPlatformDockClass,
  getArtistMonogram,
  getArtistRoleTag,
  resolveCardBadges,
} from './artistUtils';
import './ArtistsPage.css';

// Initial placeholder artists without fake numbers or placeholder stats
const FALLBACK_ARTISTS: Artist[] = [
  {
    id: 1,
    name: 'MIRAGEMFXNK',
    imageUrl: null,
    published: true,
    displayOrder: 1,
    links: [],
  },
  {
    id: 2,
    name: '411N:))',
    imageUrl: null,
    published: true,
    displayOrder: 2,
    links: [],
  },
  {
    id: 3,
    name: 'SPIDEX',
    imageUrl: null,
    published: true,
    displayOrder: 3,
    links: [],
  },
];

/**
 * Platform SVG Icon Component for Micro-Dock
 */
export const PlatformIcon: React.FC<{ platform: string }> = ({ platform }) => {
  const p = platform.trim().toLowerCase();

  if (p.includes('spotify')) {
    return (
      <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
        <path d="M12 2C6.477 2 2 6.477 2 12s4.477 10 10 10 10-4.477 10-10S17.523 2 12 2zm4.586 14.424c-.18.295-.563.387-.857.207-2.35-1.436-5.308-1.76-8.793-.963-.335.077-.67-.133-.746-.469-.077-.334.132-.67.467-.747 3.812-.871 7.085-.506 9.722 1.115.295.18.387.563.207.857zm1.226-2.724c-.226.367-.708.482-1.075.257-2.69-1.653-6.79-2.132-9.971-1.166-.413.125-.85-.107-.975-.52-.125-.414.107-.85.52-.976 3.633-1.102 8.152-.568 11.244 1.33.367.225.482.707.257 1.075zm.106-2.836C14.692 8.95 9.375 8.775 6.297 9.71c-.494.15-1.018-.129-1.168-.623-.15-.494.129-1.019.623-1.169 3.532-1.072 9.404-.866 13.115 1.337.444.263.59.838.327 1.282-.264.443-.838.59-1.282.327z" />
      </svg>
    );
  }

  if (p.includes('soundcloud')) {
    return (
      <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
        <path d="M1.175 12.225c-.04 0-.074.035-.078.077l-.274 2.808.274 2.766c.004.043.038.076.078.076.042 0 .077-.033.08-.076l.307-2.766-.307-2.808a.08.08 0 0 0-.08-.077zm1.18-.673c-.053 0-.097.043-.1.096l-.248 3.447.248 3.393c.003.054.047.097.1.097.054 0 .098-.043.1-.097l.28-3.393-.28-3.447a.102.102 0 0 0-.1-.096zm1.21-.497c-.066 0-.12.052-.122.118l-.208 3.963.208 3.88c.002.066.056.12.122.12.067 0 .12-.054.122-.12l.24-3.88-.24-3.963a.124.124 0 0 0-.122-.118zm1.23-.274c-.078 0-.14.062-.143.14l-.173 4.256.173 4.15c.003.078.065.14.143.14.08 0 .142-.062.144-.14l.205-4.15-.205-4.256a.144.144 0 0 0-.144-.14zm1.246-.245c-.09 0-.164.072-.166.162l-.136 4.501.136 4.395c.002.09.076.163.166.163.09 0 .164-.073.166-.163l.17-4.395-.17-4.501a.167.167 0 0 0-.166-.162zm1.264.043c-.104 0-.187.083-.189.186l-.1 4.458.1 4.352c.002.103.085.186.189.186.103 0 .187-.083.188-.186l.135-4.352-.135-4.458a.189.189 0 0 0-.188-.186zm1.282.493c-.115 0-.21.093-.21.209l-.064 3.95.064 3.856c0 .115.095.209.21.209.116 0 .21-.094.21-.21l.1-3.855-.1-3.95a.21.21 0 0 0-.21-.21zm4.128-2.613c-.237 0-.466.035-.683.098a5.53 5.53 0 0 0-4.664-1.42c-.083.013-.146.082-.146.166v11.77c0 .093.076.17.17.17h9.451c2.474 0 4.48-2.006 4.48-4.48 0-2.435-1.942-4.417-4.364-4.475-.386-1.074-1.41-1.829-2.244-1.829z" />
      </svg>
    );
  }

  if (p.includes('youtube')) {
    return (
      <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
        <path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z" />
      </svg>
    );
  }

  if (p.includes('instagram')) {
    return (
      <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
        <path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838c-3.403 0-6.162 2.759-6.162 6.162s2.759 6.163 6.162 6.163 6.162-2.759 6.162-6.163c0-3.403-2.759-6.162-6.162-6.162zm0 10.162c-2.209 0-4-1.79-4-4 0-2.209 1.791-4 4-4s4 1.791 4 4c0 2.21-1.791 4-4 4zm6.406-11.845c-.796 0-1.441.645-1.441 1.44s.645 1.44 1.441 1.44c.795 0 1.439-.645 1.439-1.44s-.644-1.44-1.439-1.44z" />
      </svg>
    );
  }

  if (p.includes('tiktok')) {
    return (
      <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
        <path d="M12.525.02c1.31-.02 2.61-.01 3.91-.02.08 1.53.63 3.09 1.75 4.17 1.12 1.11 2.7 1.62 4.24 1.79v4.03c-1.44-.05-2.89-.35-4.2-.97-.57-.26-1.1-.59-1.62-.97v7.54c.01 1.87-.63 3.73-1.84 5.14-1.57 1.84-3.97 2.87-6.4 2.78-2.69-.1-5.18-1.63-6.43-4.01-1.29-2.45-1.12-5.55.43-7.85 1.51-2.28 4.14-3.6 6.88-3.42.02 1.37.01 2.74.02 4.11-1.39-.1-2.8.44-3.66 1.52-.92 1.14-.99 2.78-.18 3.99.8 1.18 2.3 1.81 3.72 1.54 1.25-.23 2.27-1.22 2.52-2.46.12-.59.13-1.2.13-1.8V.02h.57z" />
      </svg>
    );
  }

  return (
    <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
      <path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z" />
    </svg>
  );
};

/**
 * Dubplate Vinyl Monogram Placeholder Component
 * Renders concentric sound grooves with a brushed aluminum center hub,
 * 2-letter monogram and archival watermark stamping.
 * Separates the rotating disc body from stationary specular sheen for physical realism.
 */
export const DubplateVinylPlaceholder: React.FC<{ name: string; artistId: number }> = ({
  name,
  artistId,
}) => {
  const monogram = getArtistMonogram(name);
  const rawId = useId();
  const safeId = `${artistId}-${rawId.replace(/[^a-zA-Z0-9_-]/g, '')}`;
  const gradId = `vinyl-hub-grad-${safeId}`;
  const shineId = `vinyl-shine-${safeId}`;
  const shineCrossId = `vinyl-shine-cross-${safeId}`;

  return (
    <div
      className="dubplate-vinyl-container"
      aria-label={`Bản ghi đĩa than archival của nghệ sĩ ${name}`}
    >
      <svg
        viewBox="0 0 200 200"
        className="dubplate-vinyl-svg"
        xmlns="http://www.w3.org/2000/svg"
        role="img"
      >
        <defs>
          {/* Brushed metallic aluminum hub gradient */}
          <radialGradient id={gradId} cx="42%" cy="38%" r="65%">
            <stop offset="0%" stopColor="#32323e" />
            <stop offset="45%" stopColor="#1e1e26" />
            <stop offset="85%" stopColor="#121218" />
            <stop offset="100%" stopColor="#0a0a0e" />
          </radialGradient>

          {/* Anisotropic vinyl sheen reflection highlights (Stationary room light) */}
          <linearGradient id={shineId} x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="rgba(255, 255, 255, 0.08)" />
            <stop offset="35%" stopColor="transparent" />
            <stop offset="65%" stopColor="transparent" />
            <stop offset="100%" stopColor="rgba(255, 255, 255, 0.08)" />
          </linearGradient>

          {/* Cross sheen with subtle violet tint */}
          <linearGradient id={shineCrossId} x1="100%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stopColor="rgba(168, 85, 247, 0.07)" />
            <stop offset="40%" stopColor="transparent" />
            <stop offset="60%" stopColor="transparent" />
            <stop offset="100%" stopColor="rgba(168, 85, 247, 0.07)" />
          </linearGradient>
        </defs>

        {/* 🔄 Rotating Physical Disc Body (Turntable Rotation) */}
        <g className="vinyl-rotating-body">
          {/* Outer vinyl disc surface */}
          <circle cx="100" cy="100" r="95" className="vinyl-disc-body" />

          {/* Concentric sound grooves: rich micro-groove texture across acoustic bands */}
          <g className="vinyl-grooves" fill="none">
            {/* Outer lead-in lip & run-in */}
            <circle cx="100" cy="100" r="93.5" stroke="rgba(255, 255, 255, 0.12)" strokeWidth="0.75" />
            <circle cx="100" cy="100" r="91" stroke="rgba(255, 255, 255, 0.08)" strokeWidth="0.5" />

            {/* Track 1 Acoustic Micro-Grooves */}
            <circle cx="100" cy="100" r="88" stroke="rgba(255, 255, 255, 0.05)" strokeWidth="0.6" />
            <circle cx="100" cy="100" r="86" stroke="rgba(255, 255, 255, 0.08)" strokeWidth="0.5" />
            <circle cx="100" cy="100" r="84" stroke="rgba(255, 255, 255, 0.06)" strokeWidth="0.6" />
            <circle cx="100" cy="100" r="82" stroke="rgba(255, 255, 255, 0.09)" strokeWidth="0.5" />
            <circle cx="100" cy="100" r="80" stroke="rgba(255, 255, 255, 0.05)" strokeWidth="0.6" />
            <circle cx="100" cy="100" r="78" stroke="rgba(255, 255, 255, 0.08)" strokeWidth="0.5" />
            <circle cx="100" cy="100" r="76" stroke="rgba(255, 255, 255, 0.06)" strokeWidth="0.6" />
            <circle cx="100" cy="100" r="74" stroke="rgba(255, 255, 255, 0.09)" strokeWidth="0.5" />

            {/* Track 1 Silence Cut Marker Gap */}
            <circle cx="100" cy="100" r="72" stroke="rgba(255, 255, 255, 0.13)" strokeWidth="0.6" strokeDasharray="6 3" />

            {/* Track 2 Acoustic Micro-Grooves */}
            <circle cx="100" cy="100" r="69" stroke="rgba(255, 255, 255, 0.05)" strokeWidth="0.6" />
            <circle cx="100" cy="100" r="67" stroke="rgba(255, 255, 255, 0.08)" strokeWidth="0.5" />
            <circle cx="100" cy="100" r="65" stroke="rgba(255, 255, 255, 0.06)" strokeWidth="0.6" />
            <circle cx="100" cy="100" r="63" stroke="rgba(255, 255, 255, 0.09)" strokeWidth="0.5" />
            <circle cx="100" cy="100" r="61" stroke="rgba(255, 255, 255, 0.05)" strokeWidth="0.6" />
            <circle cx="100" cy="100" r="59" stroke="rgba(255, 255, 255, 0.08)" strokeWidth="0.5" />
            <circle cx="100" cy="100" r="57" stroke="rgba(255, 255, 255, 0.06)" strokeWidth="0.6" />
            <circle cx="100" cy="100" r="55" stroke="rgba(255, 255, 255, 0.09)" strokeWidth="0.5" />
            <circle cx="100" cy="100" r="53" stroke="rgba(255, 255, 255, 0.05)" strokeWidth="0.6" />

            {/* Track 2 Silence Cut Marker Gap */}
            <circle cx="100" cy="100" r="51" stroke="rgba(255, 255, 255, 0.13)" strokeWidth="0.6" strokeDasharray="5 2.5" />

            {/* Lead-out / run-out groove spiraling into center hub */}
            <circle cx="100" cy="100" r="48" stroke="rgba(255, 255, 255, 0.07)" strokeWidth="0.6" />
            <circle cx="100" cy="100" r="45" stroke="rgba(255, 255, 255, 0.09)" strokeWidth="0.6" />
            <circle cx="100" cy="100" r="42" stroke="rgba(255, 255, 255, 0.06)" strokeWidth="0.6" />
            <circle cx="100" cy="100" r="39" stroke="rgba(255, 255, 255, 0.08)" strokeWidth="0.6" />
            <circle cx="100" cy="100" r="36.5" stroke="rgba(255, 255, 255, 0.11)" strokeWidth="0.7" strokeDasharray="4 2" />
          </g>

          {/* Brushed aluminum center hub / clamp puck */}
          <circle
            cx="100"
            cy="100"
            r="34"
            fill={`url(#${gradId})`}
            stroke="rgba(255, 255, 255, 0.26)"
            strokeWidth="1.2"
            className="vinyl-center-hub"
          />

          {/* Neon accent ring around hub */}
          <circle
            cx="100"
            cy="100"
            r="31"
            fill="none"
            stroke="rgba(168, 85, 247, 0.45)"
            strokeWidth="0.8"
          />

          {/* Micro-milled aluminum circular textures */}
          <circle
            cx="100"
            cy="100"
            r="25"
            fill="none"
            stroke="rgba(255, 255, 255, 0.08)"
            strokeWidth="0.5"
          />
          <circle
            cx="100"
            cy="100"
            r="18"
            fill="none"
            stroke="rgba(255, 255, 255, 0.06)"
            strokeWidth="0.5"
          />

          {/* Watermark: MELODICK RECS. */}
          <text
            x="100"
            y="82"
            textAnchor="middle"
            className="vinyl-watermark-top"
          >
            MELODICK RECS.
          </text>

          {/* 2-Letter Monogram (Boldly centered on brushed aluminum clamp) */}
          <text
            x="100"
            y="104"
            textAnchor="middle"
            className="vinyl-monogram-text"
          >
            {monogram}
          </text>

          {/* Watermark: • ARCHIVE • */}
          <text
            x="100"
            y="123"
            textAnchor="middle"
            className="vinyl-watermark-bottom"
          >
            • ARCHIVE •
          </text>
        </g>

        {/* 🌟 Specular Sheen Reflection Arcs (Stationary light reflection on rotating surface) */}
        <circle cx="100" cy="100" r="95" fill={`url(#${shineId})`} pointerEvents="none" />
        <circle cx="100" cy="100" r="95" fill={`url(#${shineCrossId})`} pointerEvents="none" />
      </svg>
    </div>
  );
};

export const ArtistsPage: React.FC = () => {
  const [artists, setArtists] = useState<Artist[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [activeCardId, setActiveCardId] = useState<number | null>(null);
  const [imageErrors, setImageErrors] = useState<Record<number, boolean>>({});

  useEffect(() => {
    let isMounted = true;
    const fetchArtists = async () => {
      setLoading(true);
      try {
        const data = await publicApi.getArtists();
        if (isMounted) {
          if (data && data.length > 0) {
            setArtists(data);
          } else {
            setArtists(FALLBACK_ARTISTS);
          }
          setError(null);
        }
      } catch (err: unknown) {
        console.warn('API error, falling back to initial data:', err);
        if (isMounted) {
          setArtists(FALLBACK_ARTISTS);
          setError(null);
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    fetchArtists();
    return () => {
      isMounted = false;
    };
  }, []);

  // Dismiss expanded card when tapping or clicking outside
  useEffect(() => {
    if (activeCardId === null) return;

    const handleOutsideClick = (e: MouseEvent | TouchEvent) => {
      const target = e.target as HTMLElement | null;
      if (!target) return;
      if (!target.closest('.artist-card.active-card')) {
        setActiveCardId(null);
      }
    };

    document.addEventListener('pointerdown', handleOutsideClick);
    return () => {
      document.removeEventListener('pointerdown', handleOutsideClick);
    };
  }, [activeCardId]);

  const handleCardClick = (id: number) => {
    setActiveCardId((prev) => (prev === id ? null : id));
  };

  const handleImageError = (id: number) => {
    setImageErrors((prev) => ({ ...prev, [id]: true }));
  };

  /**
   * Multi-Platform Metric Badges Engine:
   * Displays 1 of 3, 2 of 3, or all 3 metrics:
   * 1. YouTube: Real view count of the single highest-viewed video (never average, never fake).
   * 2. Spotify: Average plays/streams across discography/tracks.
   * 3. TikTok: Followers count ("lượt flow" of profile).
   * Cleanly falls back to single platform badge or hides if no metrics.
   */
  const renderCardHeaderBadge = (artist: Artist, validLinks: ArtistLink[]) => {
    const result = resolveCardBadges(artist, validLinks);

    if (result.type === 'metrics' && result.badges.length > 0) {
      return (
        <div
          className={`artist-badges-bar badges-count-${result.badges.length}`}
          role="group"
          aria-label={`Thống kê của ${artist.name}`}
        >
          {result.badges.map((b, idx) => (
            <div
              key={`${b.platform}-${idx}`}
              className={`artist-metric-pill ${b.badgeClass}`}
              title={b.tooltip}
            >
              <span className={`metric-pulse-dot ${b.dotClass}`} aria-hidden="true" />
              <span className="metric-platform-icon" aria-hidden="true">
                {b.icon === 'youtube' && <span className="icon-glyph">▶</span>}
                {b.icon === 'spotify' && <span className="icon-glyph">🎵</span>}
                {b.icon === 'tiktok' && <span className="icon-glyph">✦</span>}
              </span>
              <span className="metric-count-text">{b.badgeText}</span>
            </div>
          ))}
        </div>
      );
    }

    if (result.type === 'platform') {
      return (
        <div
          className={`artist-platform-badge ${result.badgeClass}`}
          title={result.fallbackTooltip}
        >
          <span className={`platform-dot ${result.dotClass}`} />
          <span className="platform-badge-text">{result.fallbackText}</span>
        </div>
      );
    }

    return null;
  };

  return (
    <section className="public-page-section" id="artists">
      {loading ? (
        <div className="artists-loading">Đang tải danh sách nghệ sĩ...</div>
      ) : error ? (
        <div className="artists-error">{error}</div>
      ) : artists.length === 0 ? (
        <div className="artists-empty">Chưa có nghệ sĩ nào được công khai.</div>
      ) : (
        <div className="artist-grid">
          {artists.map((artist) => {
            const isActive = activeCardId === artist.id;
            const validLinks = (artist.links || []).filter((l) => isValidUrl(l.url));
            const hasImage = Boolean(artist.imageUrl && !imageErrors[artist.id]);

            return (
              <article
                key={artist.id}
                className={`artist-card ${isActive ? 'active-card' : ''}`}
                onClick={() => handleCardClick(artist.id)}
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.target === e.currentTarget && (e.key === 'Enter' || e.key === ' ')) {
                    e.preventDefault();
                    handleCardClick(artist.id);
                  }
                }}
                aria-expanded={isActive}
                aria-labelledby={`artist-name-${artist.id}`}
              >
                {/* 🌟 Top-Right Header Metric Pill (Real Single Highest-Viewed Video Views or Platform Badge) */}
                {renderCardHeaderBadge(artist, validLinks)}

                {/* 🎨 Media Canvas: Dubplate Vinyl Monogram Placeholder or Artist Artwork */}
                <div className="artist-media-canvas">
                  {hasImage ? (
                    <img
                      src={artist.imageUrl!}
                      alt={artist.name}
                      loading="lazy"
                      onError={() => handleImageError(artist.id)}
                      className="artist-avatar-img"
                    />
                  ) : (
                    <DubplateVinylPlaceholder name={artist.name} artistId={artist.id} />
                  )}

                  {/* Gradient Scrim blending media seamlessly into monolithic footer */}
                  <div className="artist-media-scrim" aria-hidden="true" />

                  {/* 🚀 Horizontal Platform Micro-Dock Overlay (Centered over media canvas on hover/focus/tap) */}
                  <div className="artist-links-overlay">
                    {validLinks.length > 0 ? (
                      <div
                        className="platform-micro-dock"
                        role="group"
                        aria-label={`Các nền tảng của ${artist.name}`}
                      >
                        {validLinks.map((link, idx) => (
                          <a
                            key={idx}
                            href={link.url}
                            className={`micro-dock-btn ${getPlatformDockClass(link.platform)}`}
                            onClick={(e) => e.stopPropagation()}
                            target="_blank"
                            rel="noopener noreferrer"
                            aria-label={getPlatformActionLabel(link.platform)}
                            title={getPlatformActionLabel(link.platform)}
                          >
                            <PlatformIcon platform={link.platform} />
                          </a>
                        ))}
                      </div>
                    ) : (
                      <div className="no-links-card">
                        <span className="no-links-brand-tag">MELODICK RECS.</span>
                        {artist.bio ? (
                          <p className="no-links-bio">{artist.bio}</p>
                        ) : (
                          <span className="no-links-hint">Chưa cấu hình liên kết</span>
                        )}
                      </div>
                    )}
                  </div>
                </div>

                {/* 🪨 Monolithic Glass Info Slate (Artist Name + Professional Role Badge) */}
                <div className="artist-info-slate">
                  <h3 id={`artist-name-${artist.id}`} className="artist-name-title">
                    {artist.name}
                  </h3>
                  <span className="artist-role-badge">
                    <span className="role-dot" aria-hidden="true" />
                    {getArtistRoleTag(artist)}
                  </span>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
};
