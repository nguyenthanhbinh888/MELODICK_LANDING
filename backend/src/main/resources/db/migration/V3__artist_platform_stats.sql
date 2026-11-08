-- ===================================================
-- MELODICK RECS. - V3__artist_platform_stats.sql
-- ===================================================

CREATE TABLE IF NOT EXISTS artist_platform_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artist_id BIGINT NOT NULL UNIQUE,
    platform VARCHAR(50) NOT NULL,
    channel_title VARCHAR(255) NULL,
    total_views BIGINT NOT NULL DEFAULT 0,
    followers_count BIGINT NOT NULL DEFAULT 0,
    video_count INT NOT NULL DEFAULT 0,
    top_video_id VARCHAR(100) NULL,
    top_video_title VARCHAR(255) NULL,
    top_video_url VARCHAR(1000) NULL,
    top_video_thumbnail VARCHAR(1000) NULL,
    top_video_views BIGINT NOT NULL DEFAULT 0,
    top_video_embed_url VARCHAR(1000) NULL,
    source VARCHAR(50) NOT NULL DEFAULT 'FALLBACK',
    last_fetched_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_artist_stats_artist FOREIGN KEY (artist_id) REFERENCES artists (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_artist_stats_artist_id ON artist_platform_stats (artist_id);

-- Update placeholder artist links with realistic channels/handles if empty
UPDATE artist_links SET url = 'https://www.youtube.com/@miragemfxnk' WHERE artist_id = 1 AND platform = 'YouTube' AND (url = '' OR url IS NULL);
UPDATE artist_links SET url = 'https://open.spotify.com/artist/miragemfxnk' WHERE artist_id = 1 AND platform = 'Spotify' AND (url = '' OR url IS NULL);
UPDATE artist_links SET url = 'https://soundcloud.com/miragemfxnk' WHERE artist_id = 1 AND platform = 'SoundCloud' AND (url = '' OR url IS NULL);

UPDATE artist_links SET url = 'https://www.youtube.com/@411n' WHERE artist_id = 2 AND platform = 'YouTube' AND (url = '' OR url IS NULL);
UPDATE artist_links SET url = 'https://open.spotify.com/artist/411n' WHERE artist_id = 2 AND platform = 'Spotify' AND (url = '' OR url IS NULL);

UPDATE artist_links SET url = 'https://www.youtube.com/@spidex' WHERE artist_id = 3 AND platform = 'YouTube' AND (url = '' OR url IS NULL);
UPDATE artist_links SET url = 'https://open.spotify.com/artist/spidex' WHERE artist_id = 3 AND platform = 'Spotify' AND (url = '' OR url IS NULL);

-- Seed initial cached platform stats for default artists
INSERT INTO artist_platform_stats (
    artist_id, platform, channel_title, total_views, followers_count, video_count,
    top_video_id, top_video_title, top_video_url, top_video_thumbnail,
    top_video_views, top_video_embed_url, source, last_fetched_at
) VALUES
(
    1, 'YouTube', 'MIRAGEMFXNK Official', 1845200, 42800, 14,
    'kxpcV6650V4', 'MIRAGEMFXNK - TOKYO DRIFT 2.0 (Official Music Video)',
    'https://www.youtube.com/watch?v=kxpcV6650V4',
    'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80',
    1250000, 'https://www.youtube.com/embed/kxpcV6650V4', 'FALLBACK', CURRENT_TIMESTAMP
),
(
    2, 'YouTube', '411N Sound', 980400, 28400, 8,
    '5qap5aO4i9A', '411N - MEMPHIS RITUAL (Visualizer)',
    'https://www.youtube.com/watch?v=5qap5aO4i9A',
    'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80',
    680000, 'https://www.youtube.com/embed/5qap5aO4i9A', 'FALLBACK', CURRENT_TIMESTAMP
),
(
    3, 'YouTube', 'SPIDEX BASS', 742100, 19800, 11,
    'CevxZvSJLk8', 'SPIDEX - DARK REALITY (Bass Boosted)',
    'https://www.youtube.com/watch?v=CevxZvSJLk8',
    'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600&auto=format&fit=crop&q=80',
    495000, 'https://www.youtube.com/embed/CevxZvSJLk8', 'FALLBACK', CURRENT_TIMESTAMP
)
ON DUPLICATE KEY UPDATE artist_id = artist_id;
