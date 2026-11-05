-- ===================================================
-- MELODICK RECS. - V2__seed_initial_data.sql
-- ===================================================

-- Initial Site Settings
INSERT INTO site_settings (setting_key, setting_value) VALUES
('brand_name', 'MELODICK RECS.'),
('hero_title', 'MELODICK RECS.'),
('tagline', 'Architects of the underground sound.'),
('submit_description', 'Looking for new signatures.'),
('submit_button_label', 'SUBMIT DEMO'),
('submit_url', 'https://forms.gle/Zj4QpgXX8MuMcGhRA'),
('copyright_text', '© 2026 Melodick Recs. All rights reserved.'),
('footer_spotify_url', ''),
('footer_youtube_url', ''),
('footer_instagram_url', ''),
('footer_tiktok_url', '')
ON DUPLICATE KEY UPDATE setting_key=setting_key;

-- Initial Artists (3 artists from reference HTML)
INSERT INTO artists (id, name, slug, image_url, bio, published, display_order) VALUES
(1, 'MIRAGEMFXNK', 'miragemfxnk', NULL, 'Underground sound artist', TRUE, 1),
(2, '411N:))', '411n', NULL, 'Electronic & phonk producer', TRUE, 2),
(3, 'SPIDEX', 'spidex', NULL, 'Experimental bass producer', TRUE, 3)
ON DUPLICATE KEY UPDATE id=id;

-- Initial Artist Platform Links (Placeholders with empty URLs to be updated via CMS)
INSERT INTO artist_links (artist_id, platform, url, display_order) VALUES
-- MIRAGEMFXNK: Spotify, SoundCloud, Instagram
(1, 'Spotify', '', 1),
(1, 'SoundCloud', '', 2),
(1, 'Instagram', '', 3),
-- 411N:)): Spotify, YouTube
(2, 'Spotify', '', 1),
(2, 'YouTube', '', 2),
-- SPIDEX: Spotify, Instagram
(3, 'Spotify', '', 1),
(3, 'Instagram', '', 2);

-- Initial Admin Account (username: admin, default password hash for Admin@123456)
-- Managed and updated dynamically at startup by AdminBootstrapInitializer
INSERT INTO admin_users (id, username, password_hash, display_name, active) VALUES
(1, 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Super Admin', TRUE)
ON DUPLICATE KEY UPDATE username=username;

