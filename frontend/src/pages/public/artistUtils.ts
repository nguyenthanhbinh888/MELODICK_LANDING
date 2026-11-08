import type { Artist } from '../../types';

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
