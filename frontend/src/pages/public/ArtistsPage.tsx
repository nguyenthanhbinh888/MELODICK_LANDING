import React, { useEffect, useState, useCallback } from 'react';
import type { Artist, ArtistLink } from '../../types';
import { publicApi } from '../../services/api';
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

const SYNTHETIC_VIDEO_IDS = new Set(['kxpcV6650V4', '5qap5aO4i9A', 'CevxZvSJLk8']);

const isSyntheticVideo = (videoId?: string | null): boolean => {
  if (!videoId) return true;
  return SYNTHETIC_VIDEO_IDS.has(videoId.trim());
};

const isValidUrl = (url?: string): boolean => {
  if (!url) return false;
  const trimmed = url.trim();
  return (
    trimmed.length > 0 &&
    trimmed !== '#' &&
    (trimmed.startsWith('http://') || trimmed.startsWith('https://'))
  );
};

const getPlatformActionLabel = (platform: string): string => {
  const p = platform.trim().toLowerCase();
  if (p === 'spotify') return 'Nghe trên Spotify';
  if (p === 'soundcloud') return 'Nghe trên SoundCloud';
  if (p === 'youtube') return 'Kênh YouTube';
  if (p === 'instagram') return 'Instagram';
  if (p === 'tiktok') return 'TikTok';
  if (p === 'apple music') return 'Apple Music';
  return platform;
};

const getPlatformClass = (platform: string): string => {
  const p = platform.trim().toLowerCase();
  if (p.includes('spotify')) return 'platform-link-spotify';
  if (p.includes('soundcloud')) return 'platform-link-soundcloud';
  if (p.includes('youtube')) return 'platform-link-youtube';
  if (p.includes('instagram')) return 'platform-link-instagram';
  if (p.includes('tiktok')) return 'platform-link-tiktok';
  return 'platform-link-default';
};

export const ArtistsPage: React.FC = () => {
  const [artists, setArtists] = useState<Artist[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [activeCardId, setActiveCardId] = useState<number | null>(null);
  const [imageErrors, setImageErrors] = useState<Record<number, boolean>>({});
  const [modalArtist, setModalArtist] = useState<Artist | null>(null);

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

  // Close modal on Escape key
  const handleKeyDown = useCallback((e: KeyboardEvent) => {
    if (e.key === 'Escape') {
      setModalArtist(null);
    }
  }, []);

  useEffect(() => {
    if (modalArtist) {
      window.addEventListener('keydown', handleKeyDown);
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      document.body.style.overflow = '';
    };
  }, [modalArtist, handleKeyDown]);

  const handleCardClick = (id: number) => {
    setActiveCardId((prev) => (prev === id ? null : id));
  };

  const handleImageError = (id: number) => {
    setImageErrors((prev) => ({ ...prev, [id]: true }));
  };

  const openTopVideoModal = (e: React.MouseEvent, artist: Artist) => {
    e.stopPropagation();
    const topVideo = artist.stats?.topVideo;
    if (
      topVideo &&
      topVideo.embedUrl &&
      topVideo.platform === 'YouTube' &&
      !isSyntheticVideo(topVideo.videoId)
    ) {
      setModalArtist(artist);
    }
  };

  const getEmbedUrlWithAutoplay = (rawUrl?: string) => {
    if (!rawUrl) return '';
    if (rawUrl.includes('youtube.com/embed/')) {
      const separator = rawUrl.includes('?') ? '&' : '?';
      return `${rawUrl}${separator}autoplay=1&rel=0`;
    }
    return rawUrl;
  };

  /**
   * Renders the corner header badge:
   * 1. Real verified YouTube views if > 0
   * 2. Otherwise, clean platform badge (e.g. Spotify Artist, SoundCloud Creator)
   * 3. If no links configured: null (no badge)
   */
  const renderCardHeaderBadge = (artist: Artist, validLinks: ArtistLink[]) => {
    const stats = artist.stats;

    // 1. YouTube verified views
    if (stats && stats.totalViews > 0 && stats.formattedViews) {
      return (
        <div
          className="artist-views-badge"
          title={`Tổng lượt xem YouTube: ${stats.totalViews.toLocaleString()}`}
        >
          <span className="views-pulse-dot" />
          <span className="views-count-text">{stats.formattedViews}</span>
        </div>
      );
    }

    // 2. If no verified view count, but artist has platform links
    if (validLinks.length > 0) {
      const hasSpotify = validLinks.some((l) => l.platform.toLowerCase().includes('spotify'));
      const hasSoundCloud = validLinks.some((l) => l.platform.toLowerCase().includes('soundcloud'));
      const hasYouTube = validLinks.some((l) => l.platform.toLowerCase().includes('youtube'));
      const hasInstagram = validLinks.some((l) => l.platform.toLowerCase().includes('instagram'));

      if (hasSpotify) {
        return (
          <div className="artist-platform-badge spotify-badge" title="Nghệ sĩ trên Spotify">
            <span className="platform-dot spotify-dot" />
            <span className="platform-badge-text">Spotify Artist</span>
          </div>
        );
      }
      if (hasSoundCloud) {
        return (
          <div className="artist-platform-badge soundcloud-badge" title="Nghệ sĩ trên SoundCloud">
            <span className="platform-dot soundcloud-dot" />
            <span className="platform-badge-text">SoundCloud Creator</span>
          </div>
        );
      }
      if (hasYouTube) {
        return (
          <div className="artist-platform-badge youtube-badge" title="Kênh YouTube">
            <span className="platform-dot youtube-dot" />
            <span className="platform-badge-text">YouTube</span>
          </div>
        );
      }
      if (hasInstagram) {
        return (
          <div className="artist-platform-badge instagram-badge" title="Instagram">
            <span className="platform-dot instagram-dot" />
            <span className="platform-badge-text">Instagram</span>
          </div>
        );
      }

      // First valid platform
      return (
        <div className="artist-platform-badge default-badge" title={validLinks[0].platform}>
          <span className="platform-dot default-dot" />
          <span className="platform-badge-text">{validLinks[0].platform}</span>
        </div>
      );
    }

    // 3. No links configured: return nothing (do not show fake badge)
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
            const hasImage = artist.imageUrl && !imageErrors[artist.id];
            const stats = artist.stats;
            const topVideo = stats?.topVideo;

            // Only show "Video Hot" button if topVideo is real, from YouTube, has embedUrl, and is not synthetic
            const hasPlayableVideo = Boolean(
              topVideo &&
              topVideo.platform === 'YouTube' &&
              topVideo.embedUrl &&
              !isSyntheticVideo(topVideo.videoId)
            );

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
                {/* 🌟 Header Metric Pill / Platform Badge */}
                {renderCardHeaderBadge(artist, validLinks)}

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

                <div className="artist-name">
                  <span>{artist.name}</span>
                </div>

                {/* Overlay platform links & Top Video button */}
                <div className="artist-links-overlay">
                  {/* 🔥 Top Video Showcase Button: ONLY for verified real YouTube videos */}
                  {hasPlayableVideo && (
                    <button
                      type="button"
                      className="top-video-cta"
                      onClick={(e) => openTopVideoModal(e, artist)}
                      title={`Xem video hot: ${topVideo!.title}`}
                    >
                      <span className="fire-icon">🔥</span>
                      <span className="top-video-cta-text">Video Hot</span>
                    </button>
                  )}

                  {validLinks.length > 0 ? (
                    validLinks.map((link, idx) => (
                      <a
                        key={idx}
                        href={link.url}
                        className={`platform-link ${getPlatformClass(link.platform)}`}
                        onClick={(e) => e.stopPropagation()}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        {getPlatformActionLabel(link.platform)}
                      </a>
                    ))
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
            );
          })}
        </div>
      )}

      {/* 🎬 Hottest Video Spotlight Modal (Only opened when real verified playable video exists) */}
      {modalArtist &&
        modalArtist.stats?.topVideo &&
        modalArtist.stats.topVideo.embedUrl &&
        !isSyntheticVideo(modalArtist.stats.topVideo.videoId) && (
          <div
            className="top-video-modal-backdrop"
            onClick={() => setModalArtist(null)}
            role="dialog"
            aria-modal="true"
            aria-labelledby="top-video-modal-title"
          >
            <div
              className="top-video-modal-card"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="top-video-modal-header">
                <div className="modal-artist-info">
                  <span className="modal-badge-channel">
                    {modalArtist.stats.platform || 'YouTube'}
                    {modalArtist.stats.channelTitle ? ` • ${modalArtist.stats.channelTitle}` : ''}
                  </span>
                  <h3 id="top-video-modal-title" className="modal-artist-title">
                    {modalArtist.name} — Video Nổi Bật
                  </h3>
                </div>
                <button
                  type="button"
                  className="modal-close-btn"
                  onClick={() => setModalArtist(null)}
                  aria-label="Đóng cửa sổ xem video"
                >
                  ✕
                </button>
              </div>

              {/* Video Player */}
              <div className="top-video-player-container">
                <iframe
                  src={getEmbedUrlWithAutoplay(modalArtist.stats.topVideo.embedUrl)}
                  title={modalArtist.stats.topVideo.title || `${modalArtist.name} - Video`}
                  className="top-video-iframe"
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                  allowFullScreen
                />
              </div>

              {/* Video details & statistics bar (Only showing verified non-zero numbers) */}
              <div className="top-video-modal-details">
                <h4 className="top-video-title">{modalArtist.stats.topVideo.title}</h4>

                <div className="video-stats-metrics-grid">
                  {modalArtist.stats.topVideo.viewCount !== undefined &&
                    modalArtist.stats.topVideo.viewCount > 0 && (
                      <div className="metric-box">
                        <span className="metric-label">🔥 Lượt xem Video</span>
                        <span className="metric-value">
                          {modalArtist.stats.topVideo.formattedViews ||
                            modalArtist.stats.topVideo.viewCount.toLocaleString()}
                        </span>
                      </div>
                    )}

                  {modalArtist.stats.totalViews > 0 && (
                    <div className="metric-box">
                      <span className="metric-label">👁️ Tổng lượt xem Kênh</span>
                      <span className="metric-value">
                        {modalArtist.stats.formattedViews ||
                          modalArtist.stats.totalViews.toLocaleString()}
                      </span>
                    </div>
                  )}

                  {modalArtist.stats.followersCount > 0 && (
                    <div className="metric-box">
                      <span className="metric-label">👥 Người theo dõi</span>
                      <span className="metric-value">
                        {modalArtist.stats.formattedFollowers ||
                          modalArtist.stats.followersCount.toLocaleString()}
                      </span>
                    </div>
                  )}

                  <div className="metric-box">
                    <span className="metric-label">🌐 Nền tảng</span>
                    <span className="metric-value">YouTube</span>
                  </div>
                </div>

                {/* Modal footer actions */}
                <div className="modal-actions-bar">
                  {modalArtist.stats.topVideo.url && (
                    <a
                      href={modalArtist.stats.topVideo.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="modal-external-link"
                    >
                      Mở trên YouTube ↗
                    </a>
                  )}
                  <button
                    type="button"
                    className="modal-dismiss-btn"
                    onClick={() => setModalArtist(null)}
                  >
                    Đóng
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
    </section>
  );
};
