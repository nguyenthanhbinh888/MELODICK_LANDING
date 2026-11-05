import React, { useEffect, useState } from 'react';
import type { Artist } from '../../types';
import { publicApi } from '../../services/api';
import './ArtistsPage.css';

// Initial fallback data matching legacy HTML placeholder in case API is not yet loaded
const FALLBACK_ARTISTS: Artist[] = [
  {
    id: 1,
    name: 'MIRAGEMFXNK',
    imageUrl: null,
    published: true,
    displayOrder: 1,
    links: [
      { platform: 'Spotify', url: '', displayOrder: 1 },
      { platform: 'SoundCloud', url: '', displayOrder: 2 },
      { platform: 'Instagram', url: '', displayOrder: 3 },
    ],
  },
  {
    id: 2,
    name: '411N:))',
    imageUrl: null,
    published: true,
    displayOrder: 2,
    links: [
      { platform: 'Spotify', url: '', displayOrder: 1 },
      { platform: 'YouTube', url: '', displayOrder: 2 },
    ],
  },
  {
    id: 3,
    name: 'SPIDEX',
    imageUrl: null,
    published: true,
    displayOrder: 3,
    links: [
      { platform: 'Spotify', url: '', displayOrder: 1 },
      { platform: 'Instagram', url: '', displayOrder: 2 },
    ],
  },
];

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
          setArtists(data);
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

  const handleCardClick = (id: number) => {
    setActiveCardId((prev) => (prev === id ? null : id));
  };

  const handleImageError = (id: number) => {
    setImageErrors((prev) => ({ ...prev, [id]: true }));
  };

  const isValidUrl = (url?: string) => {
    if (!url) return false;
    const trimmed = url.trim();
    return trimmed.length > 0 && trimmed !== '#' && (trimmed.startsWith('http://') || trimmed.startsWith('https://'));
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
            const hasImage = artist.imageUrl && !imageErrors[artist.id];

            return (
              <div
                key={artist.id}
                className={`artist-card ${isActive ? 'active-card' : ''}`}
                onClick={() => handleCardClick(artist.id)}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    handleCardClick(artist.id);
                  }
                }}
                aria-expanded={isActive}
              >
                <div className="artist-avatar">
                  {hasImage ? (
                    <img
                      src={artist.imageUrl!}
                      alt={artist.name}
                      loading="lazy"
                      onError={() => handleImageError(artist.id)}
                      className="artist-avatar-img"
                    />
                  ) : (
                    <span>[ AVATAR ]</span>
                  )}
                </div>

                <div className="artist-name">{artist.name}</div>

                <div className="artist-links-overlay">
                  {validLinks.length > 0 ? (
                    validLinks.map((link, idx) => (
                      <a
                        key={idx}
                        href={link.url}
                        className="platform-link"
                        onClick={(e) => e.stopPropagation()}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        {link.platform}
                      </a>
                    ))
                  ) : (
                    <span className="no-links-hint">Chưa cấu hình liên kết</span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </section>
  );
};

