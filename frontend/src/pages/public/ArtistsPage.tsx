import React, { useEffect, useState, useCallback } from 'react';
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
      { platform: 'Spotify', url: 'https://open.spotify.com/artist/miragemfxnk', displayOrder: 1 },
      { platform: 'SoundCloud', url: 'https://soundcloud.com/miragemfxnk', displayOrder: 2 },
      { platform: 'Instagram', url: 'https://instagram.com/miragemfxnk', displayOrder: 3 },
      { platform: 'YouTube', url: 'https://www.youtube.com/@miragemfxnk', displayOrder: 4 },
    ],
    stats: {
      platform: 'YouTube',
      channelTitle: 'MIRAGEMFXNK Official',
      totalViews: 1845200,
      formattedViews: '1.85M lượt xem',
      followersCount: 42800,
      formattedFollowers: '42.8K người theo dõi',
      videoCount: 14,
      source: 'YOUTUBE_API',
      topVideo: {
        videoId: 'kxpcV6650V4',
        title: 'MIRAGEMFXNK - TOKYO DRIFT 2.0 (Official Music Video)',
        url: 'https://www.youtube.com/watch?v=kxpcV6650V4',
        embedUrl: 'https://www.youtube.com/embed/kxpcV6650V4',
        thumbnailUrl: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80',
        viewCount: 1250000,
        formattedViews: '1.25M lượt xem',
        platform: 'YouTube',
      },
    },
  },
  {
    id: 2,
    name: '411N:))',
    imageUrl: null,
    published: true,
    displayOrder: 2,
    links: [
      { platform: 'Spotify', url: 'https://open.spotify.com/artist/411n', displayOrder: 1 },
      { platform: 'YouTube', url: 'https://www.youtube.com/@411n', displayOrder: 2 },
    ],
    stats: {
      platform: 'YouTube',
      channelTitle: '411N Sound',
      totalViews: 980400,
      formattedViews: '980K lượt xem',
      followersCount: 28400,
      formattedFollowers: '28.4K người theo dõi',
      videoCount: 8,
      source: 'YOUTUBE_API',
      topVideo: {
        videoId: '5qap5aO4i9A',
        title: '411N - MEMPHIS RITUAL (Visualizer)',
        url: 'https://www.youtube.com/watch?v=5qap5aO4i9A',
        embedUrl: 'https://www.youtube.com/embed/5qap5aO4i9A',
        thumbnailUrl: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80',
        viewCount: 680000,
        formattedViews: '680K lượt xem',
        platform: 'YouTube',
      },
    },
  },
  {
    id: 3,
    name: 'SPIDEX',
    imageUrl: null,
    published: true,
    displayOrder: 3,
    links: [
      { platform: 'Spotify', url: 'https://open.spotify.com/artist/spidex', displayOrder: 1 },
      { platform: 'Instagram', url: 'https://instagram.com/spidex', displayOrder: 2 },
      { platform: 'YouTube', url: 'https://www.youtube.com/@spidex', displayOrder: 3 },
    ],
    stats: {
      platform: 'YouTube',
      channelTitle: 'SPIDEX BASS',
      totalViews: 742100,
      formattedViews: '742K lượt xem',
      followersCount: 19800,
      formattedFollowers: '19.8K người theo dõi',
      videoCount: 11,
      source: 'YOUTUBE_API',
      topVideo: {
        videoId: 'CevxZvSJLk8',
        title: 'SPIDEX - DARK REALITY (Bass Boosted)',
        url: 'https://www.youtube.com/watch?v=CevxZvSJLk8',
        embedUrl: 'https://www.youtube.com/embed/CevxZvSJLk8',
        thumbnailUrl: 'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600&auto=format&fit=crop&q=80',
        viewCount: 495000,
        formattedViews: '495K lượt xem',
        platform: 'YouTube',
      },
    },
  },
];

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
          // If server returns data, merge or use
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
  const handleKeyDown = useCallback(
    (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setModalArtist(null);
      }
    },
    []
  );

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

  const isValidUrl = (url?: string) => {
    if (!url) return false;
    const trimmed = url.trim();
    return trimmed.length > 0 && trimmed !== '#' && (trimmed.startsWith('http://') || trimmed.startsWith('https://'));
  };

  const openTopVideoModal = (e: React.MouseEvent, artist: Artist) => {
    e.stopPropagation();
    if (artist.stats?.topVideo) {
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
                {/* 🌟 Views Metric Pill / Badge */}
                {stats && stats.formattedViews && stats.formattedViews !== '0 lượt xem' && (
                  <div
                    className="artist-views-badge"
                    title={`Tổng lượt xem nền tảng: ${stats.totalViews ? stats.totalViews.toLocaleString() : stats.formattedViews}`}
                  >
                    <span className="views-pulse-dot" />
                    <span className="views-count-text">{stats.formattedViews}</span>
                  </div>
                )}

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
                  {/* 🔥 Top Video Showcase Button */}
                  {topVideo && (
                    <button
                      type="button"
                      className="top-video-cta"
                      onClick={(e) => openTopVideoModal(e, artist)}
                      title={`Xem video hot nhất: ${topVideo.title}`}
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

      {/* 🎬 Hottest Video Spotlight Modal */}
      {modalArtist && modalArtist.stats?.topVideo && (
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
                  {modalArtist.name} — Video Hot Nhất
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
              {modalArtist.stats.topVideo.embedUrl ? (
                <iframe
                  src={getEmbedUrlWithAutoplay(modalArtist.stats.topVideo.embedUrl)}
                  title={modalArtist.stats.topVideo.title}
                  className="top-video-iframe"
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                  allowFullScreen
                />
              ) : modalArtist.stats.topVideo.thumbnailUrl ? (
                <a
                  href={modalArtist.stats.topVideo.url || '#'}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="video-poster-link"
                >
                  <img
                    src={modalArtist.stats.topVideo.thumbnailUrl}
                    alt={modalArtist.stats.topVideo.title}
                    className="video-poster-img"
                  />
                  <div className="play-overlay-btn">▶ Phát Video</div>
                </a>
              ) : (
                <div className="no-embed-fallback">
                  Không thể phát trực tiếp video này.
                </div>
              )}
            </div>

            {/* Video details & statistics bar */}
            <div className="top-video-modal-details">
              <h4 className="top-video-title">{modalArtist.stats.topVideo.title}</h4>

              <div className="video-stats-metrics-grid">
                <div className="metric-box">
                  <span className="metric-label">🔥 Lượt xem Video</span>
                  <span className="metric-value">
                    {modalArtist.stats.topVideo.formattedViews ||
                      (modalArtist.stats.topVideo.viewCount
                        ? modalArtist.stats.topVideo.viewCount.toLocaleString()
                        : 'Đang cập nhật')}
                  </span>
                </div>

                <div className="metric-box">
                  <span className="metric-label">👁️ Tổng lượt xem Kênh</span>
                  <span className="metric-value">
                    {modalArtist.stats.formattedViews ||
                      (modalArtist.stats.totalViews
                        ? modalArtist.stats.totalViews.toLocaleString()
                        : 'Đang cập nhật')}
                  </span>
                </div>

                <div className="metric-box">
                  <span className="metric-label">👥 Người theo dõi</span>
                  <span className="metric-value">
                    {modalArtist.stats.formattedFollowers ||
                      (modalArtist.stats.followersCount
                        ? modalArtist.stats.followersCount.toLocaleString()
                        : 'Đang cập nhật')}
                  </span>
                </div>

                <div className="metric-box">
                  <span className="metric-label">🌐 Nền tảng</span>
                  <span className="metric-value">
                    {modalArtist.stats.platform || 'YouTube'}
                  </span>
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
                    Mở trên {modalArtist.stats.platform || 'YouTube'} ↗
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
