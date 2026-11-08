-- ===================================================
-- MELODICK RECS. - V4__clean_fake_platform_stats.sql
-- Remove all fake/synthetic seeded platform statistics
-- and revert placeholder URLs that were automatically injected in V3.
-- ===================================================

-- 1. Remove all synthetic/mock stats cached in artist_platform_stats
-- Using WHERE id > 0 ensures compatibility with MySQL sql_safe_updates = 1
DELETE FROM artist_platform_stats WHERE id > 0;

-- 2. Clear placeholder URLs that were seeded in V3 back to empty strings
UPDATE artist_links 
SET url = '' 
WHERE id > 0 AND (
    url IN (
        'https://www.youtube.com/@miragemfxnk',
        'https://open.spotify.com/artist/miragemfxnk',
        'https://soundcloud.com/miragemfxnk',
        'https://www.youtube.com/@411n',
        'https://open.spotify.com/artist/411n',
        'https://www.youtube.com/@spidex',
        'https://open.spotify.com/artist/spidex'
    )
    OR url LIKE '%miragemfxnk%'
    OR url LIKE '%411n%'
    OR url LIKE '%spidex%'
    OR url = '#'
);

