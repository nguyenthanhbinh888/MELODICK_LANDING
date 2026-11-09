-- ===================================================
-- MELODICK RECS. - V5__artist_multi_platform_stats.sql
-- Multi-Platform Metrics Engine: YouTube Top Video Views,
-- Spotify Discography Average Streams, and TikTok Followers ("lượt flow")
-- ===================================================

ALTER TABLE artist_platform_stats
    ADD COLUMN spotify_avg_streams BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN spotify_total_streams BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN spotify_track_count INT NOT NULL DEFAULT 0,
    ADD COLUMN spotify_followers BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN tiktok_followers BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN tiktok_likes BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN tiktok_handle VARCHAR(255) NULL,
    ADD COLUMN tiktok_avatar_url VARCHAR(1000) NULL;
