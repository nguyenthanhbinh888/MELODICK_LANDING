import type { Artist, ArtistLink } from '../../types';

const SYNTHETIC_VIDEO_IDS = new Set(['kxpcV6650V4', '5qap5aO4i9A', 'CevxZvSJLk8']);

export const isSyntheticVideo = (videoId?: string | null): boolean => {
  if (!videoId) return true;
  return SYNTHETIC_VIDEO_IDS.has(videoId.trim());
};

export const isValidUrl = (url?: string): boolean => {
  if (!url) return false;
  const trimmed = url.trim();
  return (
    trimmed.length > 0 &&
    trimmed !== '#' &&
    (trimmed.startsWith('http://') || trimmed.startsWith('https://'))
  );
};

/**
 * Formats video view counts into human-readable compact metrics (Vietnamese standard).
 * Strictly used for real single highest-viewed video views (never averages, never fake).
 * Examples:
 * - 17,400 -> "17.4K lượt xem"
 * - 1,250,000 -> "1.25M lượt xem"
 * - 2,500,000,000 -> "2.5B lượt xem"
 * - 950 -> "950 lượt xem"
 * - 0 / null / undefined -> ""
 */
export const formatViewCount = (count?: number | null): string => {
  if (count == null || !Number.isFinite(count) || count <= 0) return '';
  if (count >= 1_000_000_000) {
    const val = (count / 1_000_000_000).toFixed(2).replace(/\.?0+$/, '');
    return `${val}B lượt xem`;
  }
  if (count >= 999_950_000) {
    return '1B lượt xem';
  }
  if (count >= 1_000_000) {
    const val = (count / 1_000_000).toFixed(2).replace(/\.?0+$/, '');
    return `${val}M lượt xem`;
  }
  if (count >= 999_950) {
    return '1M lượt xem';
  }
  if (count >= 1_000) {
    const val = (count / 1_000).toFixed(1).replace(/\.?0+$/, '');
    return `${val}K lượt xem`;
  }
  return `${Math.round(count)} lượt xem`;
};

export interface MetricBadgeItem {
  platform: 'YouTube' | 'Spotify' | 'TikTok' | string;
  metricType: 'top_views' | 'avg_views' | 'followers';
  badgeText: string;
  tooltip: string;
  badgeClass: string;
  dotClass: string;
  icon: 'youtube' | 'spotify' | 'tiktok' | string;
  url?: string;
}

export interface ResolvedBadgesResult {
  type: 'metrics' | 'platform' | 'none';
  badges: MetricBadgeItem[];
  fallbackText?: string;
  fallbackTooltip?: string;
  badgeClass?: string;
  dotClass?: string;
}

/**
 * Compact numeric metric formatter (e.g. 1,250,000 -> "1.25M", 42,800 -> "42.8K", 950 -> "950")
 */
export const formatCompactMetric = (count?: number | null): string => {
  if (count == null || !Number.isFinite(count) || count <= 0) return '0';
  if (count >= 1_000_000_000) {
    const val = (count / 1_000_000_000).toFixed(2).replace(/\.?0+$/, '');
    return `${val}B`;
  }
  if (count >= 999_950_000) {
    return '1B';
  }
  if (count >= 1_000_000) {
    const val = (count / 1_000_000).toFixed(2).replace(/\.?0+$/, '');
    return `${val}M`;
  }
  if (count >= 999_950) {
    return '1M';
  }
  if (count >= 1_000) {
    const val = (count / 1_000).toFixed(1).replace(/\.?0+$/, '');
    return `${val}K`;
  }
  return String(Math.round(count));
};

export interface CardBadgeResult {
  type: 'views' | 'platform' | 'none';
  text?: string;
  tooltip?: string;
  badgeClass?: string;
  dotClass?: string;
}

/**
 * Multi-Platform Metric Display Engine:
 * Resolves 1 of 3, 2 of 3, or all 3 metrics:
 * 1. YouTube: Real view count of the single highest-viewed video (never average, never fake).
 * 2. Spotify: Average plays/streams of the artist's discography/tracks.
 * 3. TikTok: Followers count ("lượt flow" of profile).
 * If none have metrics: cleanly falls back to platform badge or hides.
 */
export const resolveCardBadges = (
  artist: Artist,
  validLinks: ArtistLink[] = []
): ResolvedBadgesResult => {
  const stats = artist.stats;
  const badges: MetricBadgeItem[] = [];

  // Helper to find platform link url
  const findLinkUrl = (platformPattern: string): string | undefined => {
    const link = validLinks.find((l) =>
      (l.platform || '').toLowerCase().includes(platformPattern) ||
      (l.url || '').toLowerCase().includes(platformPattern)
    );
    return link?.url;
  };

  // Priority 1: If backend pre-calculated badges are present, use them with link validation
  if (stats?.badges && stats.badges.length > 0) {
    for (const b of stats.badges) {
      const pLower = (b.platform || '').toLowerCase();
      const linkUrl = findLinkUrl(pLower);
      if (linkUrl) {
        const platformName: 'YouTube' | 'Spotify' | 'TikTok' = pLower.includes('youtube')
          ? 'YouTube'
          : pLower.includes('spotify')
          ? 'Spotify'
          : 'TikTok';

        const badgeClass = `badge-${platformName.toLowerCase()}`;
        const dotClass = `dot-${platformName.toLowerCase()}`;
        const icon = platformName.toLowerCase() as 'youtube' | 'spotify' | 'tiktok';

        badges.push({
          platform: platformName,
          metricType: (b.metricType as 'top_views' | 'avg_views' | 'followers') || 'top_views',
          badgeText: b.formattedBadge || `${b.formattedValue} ${b.label}`,
          tooltip: b.tooltip,
          badgeClass,
          dotClass,
          icon,
          url: b.url || linkUrl,
        });
      }
    }

    if (badges.length > 0) {
      return {
        type: 'metrics',
        badges,
      };
    }
  }

  // 1. YouTube Metric: Single highest-viewed video views
  // Must have an attached YouTube link AND verified non-synthetic top video views > 0
  const ytUrl = findLinkUrl('youtube') || findLinkUrl('youtu.be');
  const topVideo = stats?.topVideo;
  const topVideoViews = topVideo?.viewCount ?? stats?.topVideoViews ?? 0;
  const isSynthetic = Boolean(topVideo?.videoId && isSyntheticVideo(topVideo.videoId));
  const isYouTubeValid = Boolean(ytUrl) && !isSynthetic && topVideoViews > 0;

  if (isYouTubeValid) {
    const valStr = formatCompactMetric(topVideoViews);
    const badgeText = `${valStr} view`;
    const tooltip = topVideo?.title
      ? `Lượt xem video cao nhất (${topVideo.title}): ${topVideoViews.toLocaleString()} lượt xem`
      : `Lượt xem video cao nhất: ${topVideoViews.toLocaleString()} lượt xem`;

    badges.push({
      platform: 'YouTube',
      metricType: 'top_views',
      badgeText,
      tooltip,
      badgeClass: 'badge-youtube',
      dotClass: 'dot-youtube',
      icon: 'youtube',
      url: topVideo?.url || ytUrl,
    });
  }

  // 2. Spotify Metric: Average streams of discography/tracks
  // Must have an attached Spotify link AND verified average streams >= 0
  const spUrl = findLinkUrl('spotify');
  const hasSpStat = typeof stats?.spotifyAvgStreams === 'number' && stats.spotifyAvgStreams >= 0;
  if (Boolean(spUrl) && hasSpStat) {
    const spotifyAvg = stats!.spotifyAvgStreams!;
    const valStr = formatCompactMetric(spotifyAvg);
    const badgeText = `${valStr} nghe TB`;
    const tooltip = `Lượt nghe trung bình Spotify: ${spotifyAvg.toLocaleString()} stream/bài`;

    badges.push({
      platform: 'Spotify',
      metricType: 'avg_views',
      badgeText,
      tooltip,
      badgeClass: 'badge-spotify',
      dotClass: 'dot-spotify',
      icon: 'spotify',
      url: spUrl,
    });
  }

  // 3. TikTok Metric: Followers count ("lượt flow")
  // Must have an attached TikTok link AND verified follower count >= 0
  const ttUrl = findLinkUrl('tiktok');
  const hasTtStat = typeof stats?.tiktokFollowers === 'number' && stats.tiktokFollowers >= 0;
  if (Boolean(ttUrl) && hasTtStat) {
    const tiktokFollowers = stats!.tiktokFollowers!;
    const valStr = formatCompactMetric(tiktokFollowers);
    const badgeText = `${valStr} flow`;
    const tooltip = `Lượt flow TikTok: ${tiktokFollowers.toLocaleString()} người theo dõi`;

    badges.push({
      platform: 'TikTok',
      metricType: 'followers',
      badgeText,
      tooltip,
      badgeClass: 'badge-tiktok',
      dotClass: 'dot-tiktok',
      icon: 'tiktok',
      url: ttUrl,
    });
  }

  // If any of the 1, 2, or 3 platform metrics are present
  if (badges.length > 0) {
    return {
      type: 'metrics',
      badges,
    };
  }

  // Fallback: Primary Platform Badge if artist has links
  if (validLinks.length > 0) {
    const primaryPlatform = (stats?.platform || validLinks[0].platform || '').trim().toLowerCase();

    if (primaryPlatform.includes('spotify')) {
      return {
        type: 'platform',
        badges: [],
        fallbackText: 'Spotify Artist',
        fallbackTooltip: 'Nghệ sĩ trên Spotify',
        badgeClass: 'spotify-badge',
        dotClass: 'spotify-dot',
      };
    }
    if (primaryPlatform.includes('soundcloud')) {
      return {
        type: 'platform',
        badges: [],
        fallbackText: 'SoundCloud Creator',
        fallbackTooltip: 'Nghệ sĩ trên SoundCloud',
        badgeClass: 'soundcloud-badge',
        dotClass: 'soundcloud-dot',
      };
    }
    if (primaryPlatform.includes('youtube')) {
      return {
        type: 'platform',
        badges: [],
        fallbackText: 'YouTube',
        fallbackTooltip: 'Kênh YouTube',
        badgeClass: 'youtube-badge',
        dotClass: 'youtube-dot',
      };
    }
    if (primaryPlatform.includes('tiktok')) {
      return {
        type: 'platform',
        badges: [],
        fallbackText: 'TikTok Creator',
        fallbackTooltip: 'TikTok',
        badgeClass: 'tiktok-badge',
        dotClass: 'tiktok-dot',
      };
    }
    if (primaryPlatform.includes('instagram')) {
      return {
        type: 'platform',
        badges: [],
        fallbackText: 'Instagram',
        fallbackTooltip: 'Instagram',
        badgeClass: 'instagram-badge',
        dotClass: 'instagram-dot',
      };
    }

    return {
      type: 'platform',
      badges: [],
      fallbackText: validLinks[0].platform,
      fallbackTooltip: validLinks[0].platform,
      badgeClass: 'default-badge',
      dotClass: 'default-dot',
    };
  }

  return { type: 'none', badges: [] };
};

/**
 * Backward compatibility wrapper returning single badge result
 */
export const resolveCardBadge = (artist: Artist, validLinks: ArtistLink[] = []): CardBadgeResult => {
  const stats = artist.stats;
  const topVideo = stats?.topVideo;
  const topVideoViews = topVideo?.viewCount ?? stats?.topVideoViews ?? 0;
  const isSynthetic = Boolean(topVideo?.videoId && isSyntheticVideo(topVideo.videoId));
  const isTopVideoValid = !isSynthetic && topVideoViews > 0;

  // 1. Strictly the real verified view count of the single highest-viewed video
  if (isTopVideoValid) {
    const displayViews =
      topVideo?.formattedViews ||
      stats?.formattedTopVideoViews ||
      formatViewCount(topVideoViews);

    const tooltip = topVideo?.title
      ? `Lượt xem video cao nhất (${topVideo.title}): ${topVideoViews.toLocaleString()} lượt xem`
      : `Lượt xem video cao nhất: ${topVideoViews.toLocaleString()} lượt xem`;

    return {
      type: 'views',
      text: displayViews,
      tooltip,
    };
  }

  // 2. Clean primary platform badge
  if (validLinks.length > 0) {
    const primaryPlatform = (stats?.platform || validLinks[0].platform || '').trim().toLowerCase();

    if (primaryPlatform.includes('spotify')) {
      return {
        type: 'platform',
        text: 'Spotify Artist',
        tooltip: 'Nghệ sĩ trên Spotify',
        badgeClass: 'spotify-badge',
        dotClass: 'spotify-dot',
      };
    }
    if (primaryPlatform.includes('soundcloud')) {
      return {
        type: 'platform',
        text: 'SoundCloud Creator',
        tooltip: 'Nghệ sĩ trên SoundCloud',
        badgeClass: 'soundcloud-badge',
        dotClass: 'soundcloud-dot',
      };
    }
    if (primaryPlatform.includes('youtube')) {
      return {
        type: 'platform',
        text: 'YouTube',
        tooltip: 'Kênh YouTube',
        badgeClass: 'youtube-badge',
        dotClass: 'youtube-dot',
      };
    }
    if (primaryPlatform.includes('instagram')) {
      return {
        type: 'platform',
        text: 'Instagram',
        tooltip: 'Instagram',
        badgeClass: 'instagram-badge',
        dotClass: 'instagram-dot',
      };
    }

    return {
      type: 'platform',
      text: validLinks[0].platform,
      tooltip: validLinks[0].platform,
      badgeClass: 'default-badge',
      dotClass: 'default-dot',
    };
  }

  return { type: 'none' };
};

export const getPlatformActionLabel = (platform: string): string => {
  const p = platform.trim().toLowerCase();
  if (p === 'spotify') return 'Nghe trên Spotify';
  if (p === 'soundcloud') return 'Nghe trên SoundCloud';
  if (p === 'youtube') return 'Kênh YouTube';
  if (p === 'instagram') return 'Instagram';
  if (p === 'tiktok') return 'TikTok';
  if (p === 'apple music') return 'Apple Music';
  return platform;
};

export const getPlatformDockClass = (platform: string): string => {
  const p = platform.trim().toLowerCase();
  if (p.includes('spotify')) return 'platform-spotify';
  if (p.includes('soundcloud')) return 'platform-soundcloud';
  if (p.includes('youtube')) return 'platform-youtube';
  if (p.includes('instagram')) return 'platform-instagram';
  if (p.includes('tiktok')) return 'platform-tiktok';
  return 'platform-default';
};

/**
 * Extracts a distinctive 2-letter monogram for the vinyl dubplate center hub.
 * Supports underground compound handles, Vietnamese names with diacritics, camelCase transitions, and multi-word names.
 * Examples:
 * - "MIRAGEMFXNK" -> "MF"
 * - "SPIDEX" -> "SP"
 * - "Binhnopro" -> "BI"
 * - "411N:))" -> "41"
 * - "Đạt G" -> "ĐG"
 * - "Nguyễn Phonk" -> "NP"
 * - "Đỗ" -> "ĐỖ"
 * - "Vũ" -> "VŨ"
 */
export const getArtistMonogram = (name: string): string => {
  if (!name) return 'ML';
  const trimmed = name.trim();
  if (!trimmed) return 'ML';

  // Canonical mapping for recognized underground compound styles
  const upper = trimmed.toUpperCase();
  if (upper === 'MIRAGEMFXNK') return 'MF';

  // Multi-word names: "Mirage Phonk" -> "MP", "Đạt G" -> "ĐG"
  const parts = trimmed.split(/[\s_\-./:]+/).filter(Boolean);
  if (parts.length >= 2) {
    const first = parts[0].replace(/[^\p{L}\p{N}]/gu, '');
    const second = parts[1].replace(/[^\p{L}\p{N}]/gu, '');
    if (first && second) {
      return (first[0] + second[0]).toUpperCase();
    }
  }

  // CamelCase transitions: e.g. "MirageMfxnk" or "BinhNoPro"
  const camelParts = trimmed.split(/(?=[A-Z][a-z])/).filter(Boolean);
  if (camelParts.length >= 2) {
    const first = camelParts[0].replace(/[^\p{L}\p{N}]/gu, '');
    const second = camelParts[1].replace(/[^\p{L}\p{N}]/gu, '');
    if (first && second) {
      return (first[0] + second[0]).toUpperCase();
    }
  }

  // Fallback: first 2 alphanumeric/Unicode characters
  const alphaNum = trimmed.replace(/[^\p{L}\p{N}]/gu, '');
  if (alphaNum.length >= 2) {
    return alphaNum.slice(0, 2).toUpperCase();
  }
  if (alphaNum.length === 1) {
    return alphaNum.toUpperCase();
  }

  return 'ML';
};

/**
 * @deprecated Removed from UI card header per user design requirements. Kept for utility.
 * Formats the Archival Catalog Code (Boiler Room & Warp Records inspired)
 * e.g. "MLDK // 01", "MLDK // 02"
 */
export const formatCatalogCode = (index: number, artist: Artist): string => {
  const num = artist.displayOrder || (index + 1);
  return `MLDK // ${String(num).padStart(2, '0')}`;
};

/**
 * Derives the secondary role / genre badge blended over the card footer
 * e.g. "RESIDENT ARTIST // BASS", "UNDERGROUND PRODUCER // BASS"
 */
export const getArtistRoleTag = (artist: Artist): string => {
  if (artist.bio) {
    const trimmed = artist.bio.trim();
    // If the bio is a concise tagline/descriptor (<= 25 chars, no punctuation period)
    if (trimmed.length > 0 && trimmed.length <= 25 && !trimmed.includes('.')) {
      return trimmed.toUpperCase();
    }
  }

  const primaryPlatform = (artist.stats?.platform || artist.links?.[0]?.platform || '').trim().toLowerCase();
  if (primaryPlatform.includes('soundcloud')) {
    return 'UNDERGROUND PRODUCER // BASS';
  }
  if (primaryPlatform.includes('spotify')) {
    return 'EXCLUSIVE ARTIST // ELECTRONIC';
  }
  if (primaryPlatform.includes('youtube')) {
    return 'AUDIOVISUAL ARTIST // BASS';
  }
  return 'RESIDENT ARTIST // BASS';
};
