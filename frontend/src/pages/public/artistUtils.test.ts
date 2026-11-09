import {
  getArtistMonogram,
  formatCatalogCode,
  getArtistRoleTag,
  isSyntheticVideo,
  isValidUrl,
  getPlatformDockClass,
  getPlatformActionLabel,
  formatViewCount,
  formatCompactMetric,
  resolveCardBadge,
  resolveCardBadges,
} from './artistUtils.ts';
import type { Artist } from '../../types/index.ts';

function assertEqual<T>(actual: T, expected: T, message?: string): void {
  if (actual !== expected) {
    throw new Error(
      `Assertion failed: expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}. ${message || ''}`
    );
  }
}

console.log('--- Running Artist Concept 1 Unit Tests ---');

// 1. Monogram Extraction Tests & Edge Cases
console.log('Testing getArtistMonogram...');
assertEqual(getArtistMonogram('MIRAGEMFXNK'), 'MF', 'MIRAGEMFXNK should produce MF');
assertEqual(getArtistMonogram('SPIDEX'), 'SP', 'SPIDEX should produce SP');
assertEqual(getArtistMonogram('Binhnopro'), 'BI', 'Binhnopro should produce BI');
assertEqual(getArtistMonogram('411N:))'), '41', '411N:)) should produce 41');
assertEqual(getArtistMonogram('Mirage Phonk'), 'MP', 'Multi-word should produce MP');
assertEqual(getArtistMonogram('Bass Master 3000'), 'BM', 'Multi-word should produce BM');
assertEqual(getArtistMonogram('DJ Shadow'), 'DS', 'DJ Shadow should produce DS');
assertEqual(getArtistMonogram('Kavinsky'), 'KA', 'Kavinsky should produce KA');
assertEqual(getArtistMonogram('Skrillex'), 'SK', 'Skrillex should produce SK');
assertEqual(getArtistMonogram('Deadmau5'), 'DE', 'Deadmau5 should produce DE');
assertEqual(getArtistMonogram('Đạt G'), 'ĐG', 'Vietnamese Đạt G should produce ĐG');
assertEqual(getArtistMonogram('Nguyễn Phonk'), 'NP', 'Vietnamese Nguyễn Phonk should produce NP');
assertEqual(getArtistMonogram('Đỗ'), 'ĐỖ', 'Vietnamese Đỗ should produce ĐỖ');
assertEqual(getArtistMonogram('Vũ'), 'VŨ', 'Vietnamese Vũ should produce VŨ');
assertEqual(getArtistMonogram('A'), 'A', 'Single character should produce A');
assertEqual(getArtistMonogram(''), 'ML', 'Empty string should fallback to ML');
assertEqual(getArtistMonogram('   '), 'ML', 'Whitespace string should fallback to ML');
assertEqual(getArtistMonogram(':-*'), 'ML', 'Symbols only should fallback to ML');
assertEqual(getArtistMonogram('__--'), 'ML', 'Dashes only should fallback to ML');
console.log('✓ getArtistMonogram passed all 19 tests');

// 2. Archival Catalog Code Formatting Tests
console.log('Testing formatCatalogCode...');
const mockArtist1: Artist = { id: 1, name: 'A1', published: true, displayOrder: 1, links: [] };
const mockArtist2: Artist = { id: 2, name: 'A2', published: true, displayOrder: 9, links: [] };
const mockArtist3: Artist = { id: 3, name: 'A3', published: true, displayOrder: 12, links: [] };
const mockArtistNoOrder: Artist = { id: 4, name: 'A4', published: true, displayOrder: 0, links: [] };

assertEqual(formatCatalogCode(0, mockArtist1), 'MLDK // 01');
assertEqual(formatCatalogCode(1, mockArtist2), 'MLDK // 09');
assertEqual(formatCatalogCode(2, mockArtist3), 'MLDK // 12');
assertEqual(formatCatalogCode(3, mockArtistNoOrder), 'MLDK // 04'); // falls back to index + 1
console.log('✓ formatCatalogCode passed all tests');

// 3. Secondary Role Badge Derivation Tests
console.log('Testing getArtistRoleTag...');
const artistWithShortBio: Artist = {
  id: 1,
  name: 'Producer A',
  published: true,
  displayOrder: 1,
  bio: 'Heavy Phonk Master',
  links: [],
};
assertEqual(getArtistRoleTag(artistWithShortBio), 'HEAVY PHONK MASTER');

const artistWithLongSentenceBio: Artist = {
  id: 2,
  name: 'Producer B',
  published: true,
  displayOrder: 2,
  bio: 'Born in 1998, producing underground beats for over 10 years.',
  links: [{ platform: 'SoundCloud', url: 'https://soundcloud.com/b', displayOrder: 1 }],
};
assertEqual(getArtistRoleTag(artistWithLongSentenceBio), 'UNDERGROUND PRODUCER // BASS');

const artistSpotify: Artist = {
  id: 3,
  name: 'Artist C',
  published: true,
  displayOrder: 3,
  links: [{ platform: 'Spotify', url: 'https://spotify.com/c', displayOrder: 1 }],
};
assertEqual(getArtistRoleTag(artistSpotify), 'EXCLUSIVE ARTIST // ELECTRONIC');

const artistYouTube: Artist = {
  id: 4,
  name: 'Artist D',
  published: true,
  displayOrder: 4,
  links: [{ platform: 'YouTube', url: 'https://youtube.com/@d', displayOrder: 1 }],
};
assertEqual(getArtistRoleTag(artistYouTube), 'AUDIOVISUAL ARTIST // BASS');

const artistDefault: Artist = {
  id: 5,
  name: 'Artist E',
  published: true,
  displayOrder: 5,
  links: [],
};
assertEqual(getArtistRoleTag(artistDefault), 'RESIDENT ARTIST // BASS');
console.log('✓ getArtistRoleTag passed all tests');

// 4. Video & URL Validation Tests
console.log('Testing isSyntheticVideo and isValidUrl...');
assertEqual(isSyntheticVideo('kxpcV6650V4'), true);
assertEqual(isSyntheticVideo('5qap5aO4i9A'), true);
assertEqual(isSyntheticVideo('CevxZvSJLk8'), true);
assertEqual(isSyntheticVideo(null), true);
assertEqual(isSyntheticVideo(undefined), true);
assertEqual(isSyntheticVideo('dQw4w9WgXcQ'), false);

assertEqual(isValidUrl('https://open.spotify.com/artist/123'), true);
assertEqual(isValidUrl('http://soundcloud.com/user'), true);
assertEqual(isValidUrl('#'), false);
assertEqual(isValidUrl(''), false);
assertEqual(isValidUrl('   '), false);
assertEqual(isValidUrl(undefined), false);
assertEqual(isValidUrl('ftp://example.com'), false);
console.log('✓ isSyntheticVideo & isValidUrl passed all tests');

// 5. Platform Dock Classes and Action Labels
console.log('Testing Platform Micro-Dock helpers...');
assertEqual(getPlatformDockClass('Spotify'), 'platform-spotify');
assertEqual(getPlatformDockClass('SoundCloud'), 'platform-soundcloud');
assertEqual(getPlatformDockClass('YouTube'), 'platform-youtube');
assertEqual(getPlatformDockClass('Instagram'), 'platform-instagram');
assertEqual(getPlatformDockClass('TikTok'), 'platform-tiktok');
assertEqual(getPlatformDockClass('Bandcamp'), 'platform-default');

assertEqual(getPlatformActionLabel('Spotify'), 'Nghe trên Spotify');
assertEqual(getPlatformActionLabel('SoundCloud'), 'Nghe trên SoundCloud');
assertEqual(getPlatformActionLabel('YouTube'), 'Kênh YouTube');
assertEqual(getPlatformActionLabel('Instagram'), 'Instagram');
assertEqual(getPlatformActionLabel('TikTok'), 'TikTok');
assertEqual(getPlatformActionLabel('Apple Music'), 'Apple Music');
console.log('✓ Platform helpers passed all tests');

// 6. Strict Top Video View Count Formatting Tests
console.log('Testing formatViewCount...');
assertEqual(formatViewCount(17400), '17.4K lượt xem', '17,400 should format to 17.4K lượt xem');
assertEqual(formatViewCount(1250000), '1.25M lượt xem', '1,250,000 should format to 1.25M lượt xem');
assertEqual(formatViewCount(1000000), '1M lượt xem', '1,000,000 should format to 1M lượt xem');
assertEqual(formatViewCount(2500000000), '2.5B lượt xem', '2,500,000,000 should format to 2.5B lượt xem');
assertEqual(formatViewCount(950), '950 lượt xem', '950 should format to 950 lượt xem');
assertEqual(formatViewCount(1), '1 lượt xem', '1 should format to 1 lượt xem');
assertEqual(formatViewCount(0), '', '0 views must produce empty string (no badge)');
assertEqual(formatViewCount(-50), '', 'Negative views must produce empty string (no badge)');
assertEqual(formatViewCount(null), '', 'Null views must produce empty string (no badge)');
assertEqual(formatViewCount(undefined), '', 'Undefined views must produce empty string (no badge)');
assertEqual(formatViewCount(NaN), '', 'NaN views must produce empty string (no badge)');
assertEqual(formatViewCount(Infinity), '', 'Infinity views must produce empty string (no badge)');
assertEqual(formatViewCount(-Infinity), '', '-Infinity views must produce empty string (no badge)');
// Boundary rounding checks: must never produce "1000K" or "1000M"
assertEqual(formatViewCount(999949), '999.9K lượt xem', '999,949 should format to 999.9K lượt xem');
assertEqual(formatViewCount(999950), '1M lượt xem', '999,950 must round up to 1M lượt xem, NOT 1000K');
assertEqual(formatViewCount(999999), '1M lượt xem', '999,999 must round up to 1M lượt xem, NOT 1000K');
assertEqual(formatViewCount(999949999), '999.95M lượt xem', '999,949,999 should format to 999.95M lượt xem');
assertEqual(formatViewCount(999950000), '1B lượt xem', '999,950,000 must round up to 1B lượt xem, NOT 1000M');
assertEqual(formatViewCount(14500000000), '14.5B lượt xem', '14,500,000,000 should format to 14.5B lượt xem');
console.log('✓ formatViewCount passed all 19 tests');

// 7. Strict Top Video Card Badge Resolution Tests
console.log('Testing resolveCardBadge...');

// 7a. Verified Top Video with Views > 0: MUST return 'views' badge with formatted string
const artistWithTopVideo: Artist = {
  id: 1,
  name: 'MIRAGEMFXNK',
  published: true,
  displayOrder: 1,
  links: [{ platform: 'YouTube', url: 'https://youtube.com/@mirage', displayOrder: 1 }],
  stats: {
    platform: 'YouTube',
    totalViews: 5000000,
    formattedViews: '5M lượt xem',
    followersCount: 100000,
    formattedFollowers: '100K người theo dõi',
    videoCount: 15,
    topVideo: {
      videoId: 'realVid123',
      title: 'Night Drift',
      viewCount: 17400,
      formattedViews: '17.4K lượt xem',
    },
  },
};
const badge1 = resolveCardBadge(artistWithTopVideo, artistWithTopVideo.links);
assertEqual(badge1.type, 'views', 'Should resolve to views badge');
assertEqual(badge1.text, '17.4K lượt xem', 'Should display top video views formatted');
assertEqual(badge1.tooltip, 'Lượt xem video cao nhất (Night Drift): 17,400 lượt xem');

// 7b. Channel Total Views > 0, BUT Top Video Views = 0: MUST NOT display views badge
const artistZeroTopVideoViews: Artist = {
  id: 2,
  name: '411N:))',
  published: true,
  displayOrder: 2,
  links: [{ platform: 'YouTube', url: 'https://youtube.com/@411n', displayOrder: 1 }],
  stats: {
    platform: 'YouTube',
    totalViews: 800000, // Total channel views exist, but top video is 0
    formattedViews: '800K lượt xem',
    followersCount: 2000,
    formattedFollowers: '2K người theo dõi',
    videoCount: 2,
    topVideo: {
      videoId: 'zeroViewsVid',
      title: 'New Track',
      viewCount: 0,
      formattedViews: undefined,
    },
  },
};
const badge2 = resolveCardBadge(artistZeroTopVideoViews, artistZeroTopVideoViews.links);
assertEqual(badge2.type, 'platform', 'Zero top-video views must fallback to platform badge, NOT total channel views');
assertEqual(badge2.text, 'YouTube');

// 7c. Synthetic Video ID: MUST NOT display views badge even if views > 0
const artistSyntheticVideo: Artist = {
  id: 3,
  name: 'SynthArtist',
  published: true,
  displayOrder: 3,
  links: [{ platform: 'YouTube', url: 'https://youtube.com/@synth', displayOrder: 1 }],
  stats: {
    platform: 'YouTube',
    totalViews: 999999,
    formattedViews: '999K lượt xem',
    followersCount: 1000,
    formattedFollowers: '1K người theo dõi',
    videoCount: 1,
    topVideo: {
      videoId: 'kxpcV6650V4', // Synthetic video ID!
      title: 'Fake Video',
      viewCount: 120000,
      formattedViews: '120K lượt xem',
    },
  },
};
const badge3 = resolveCardBadge(artistSyntheticVideo, artistSyntheticVideo.links);
assertEqual(badge3.type, 'platform', 'Synthetic video must be rejected from views badge');

// 7d. Spotify artist without YouTube: displays Spotify Artist badge
const artistSpotifyOnly: Artist = {
  id: 4,
  name: 'SPIDEX',
  published: true,
  displayOrder: 4,
  links: [{ platform: 'Spotify', url: 'https://open.spotify.com/artist/spidex', displayOrder: 1 }],
};
const badge4 = resolveCardBadge(artistSpotifyOnly, artistSpotifyOnly.links);
assertEqual(badge4.type, 'platform');
assertEqual(badge4.text, 'Spotify Artist');
assertEqual(badge4.badgeClass, 'spotify-badge');

// 7e. SoundCloud artist: displays SoundCloud Creator badge
const artistSoundCloudOnly: Artist = {
  id: 5,
  name: 'BassLord',
  published: true,
  displayOrder: 5,
  links: [{ platform: 'SoundCloud', url: 'https://soundcloud.com/basslord', displayOrder: 1 }],
};
const badge5 = resolveCardBadge(artistSoundCloudOnly, artistSoundCloudOnly.links);
assertEqual(badge5.type, 'platform');
assertEqual(badge5.text, 'SoundCloud Creator');
assertEqual(badge5.badgeClass, 'soundcloud-badge');

// 7f. Artist with no links: displays NO badge ({ type: 'none' })
const artistNoLinks: Artist = {
  id: 6,
  name: 'Unknown Producer',
  published: true,
  displayOrder: 6,
  links: [],
};
const badge6 = resolveCardBadge(artistNoLinks, []);
assertEqual(badge6.type, 'none', 'Artist with no links must display no badge');

// 7g. Robustness: topVideoViews present directly on stats even if topVideo object is null
const artistTopVideoViewsOnStatsOnly: Artist = {
  id: 7,
  name: 'AudioPhonk',
  published: true,
  displayOrder: 7,
  links: [{ platform: 'YouTube', url: 'https://youtube.com/@audiophonk', displayOrder: 1 }],
  stats: {
    platform: 'YouTube',
    totalViews: 600000,
    formattedViews: '600K lượt xem',
    topVideoViews: 45000,
    formattedTopVideoViews: '45K lượt xem',
    followersCount: 1200,
    formattedFollowers: '1.2K người theo dõi',
    videoCount: 5,
    topVideo: undefined, // omitted topVideo object
  },
};
const badge7 = resolveCardBadge(artistTopVideoViewsOnStatsOnly, artistTopVideoViewsOnStatsOnly.links);
assertEqual(badge7.type, 'views', 'Should resolve to views badge even when topVideo object is omitted');
assertEqual(badge7.text, '45K lượt xem');

// 7h. Robustness: topVideo present with viewCount but videoId omitted
const artistTopVideoWithoutId: Artist = {
  id: 8,
  name: 'BassEcho',
  published: true,
  displayOrder: 8,
  links: [{ platform: 'YouTube', url: 'https://youtube.com/@bassecho', displayOrder: 1 }],
  stats: {
    platform: 'YouTube',
    totalViews: 300000,
    formattedViews: '300K lượt xem',
    followersCount: 500,
    formattedFollowers: '500 người theo dõi',
    videoCount: 3,
    topVideo: {
      title: 'Deep Bass Track',
      viewCount: 28000,
    },
  },
};
const badge8 = resolveCardBadge(artistTopVideoWithoutId, artistTopVideoWithoutId.links);
assertEqual(badge8.type, 'views', 'Should resolve to views badge when videoId is not set');
assertEqual(badge8.text, '28K lượt xem');

console.log('✓ resolveCardBadge passed all 8 test scenarios');

// 8. Multi-Platform Metric Display Engine Tests (1 of 3, 2 of 3, 3 of 3)
console.log('Testing Multi-Platform Metric Engine (resolveCardBadges & formatCompactMetric)...');

// 8a. formatCompactMetric
assertEqual(formatCompactMetric(1250000), '1.25M');
assertEqual(formatCompactMetric(42800), '42.8K');
assertEqual(formatCompactMetric(950), '950');
assertEqual(formatCompactMetric(2500000000), '2.5B');
assertEqual(formatCompactMetric(999950), '1M');
assertEqual(formatCompactMetric(999950000), '1B');
assertEqual(formatCompactMetric(0), '0');
assertEqual(formatCompactMetric(null), '0');

// 8b. 1 Platform attached: YouTube only
const artistOnePlatformYT: Artist = {
  id: 101,
  name: 'YouTubeOnly',
  published: true,
  displayOrder: 1,
  links: [{ platform: 'YouTube', url: 'https://youtube.com/@ytonly', displayOrder: 1 }],
  stats: {
    platform: 'YouTube',
    totalViews: 2000000,
    formattedViews: '2M lượt xem',
    followersCount: 50000,
    formattedFollowers: '50K người theo dõi',
    videoCount: 10,
    topVideo: {
      videoId: 'topVidYt',
      title: 'Solo Drift',
      viewCount: 1200000,
    },
  },
};
const res1 = resolveCardBadges(artistOnePlatformYT, artistOnePlatformYT.links);
assertEqual(res1.type, 'metrics');
assertEqual(res1.badges.length, 1, '1 platform attached must render exactly 1 badge');
assertEqual(res1.badges[0].platform, 'YouTube');
assertEqual(res1.badges[0].badgeText, '1.2M view');
assertEqual(res1.badges[0].icon, 'youtube');

// 8c. 1 Platform attached: Spotify only (average streams)
const artistOnePlatformSpotify: Artist = {
  id: 102,
  name: 'SpotifyOnly',
  published: true,
  displayOrder: 2,
  links: [{ platform: 'Spotify', url: 'https://open.spotify.com/artist/sponly', displayOrder: 1 }],
  stats: {
    platform: 'Spotify',
    totalViews: 854000,
    formattedViews: '854K lượt xem',
    spotifyAvgStreams: 85400,
    followersCount: 12000,
    formattedFollowers: '12K người theo dõi',
    videoCount: 10,
  },
};
const res2 = resolveCardBadges(artistOnePlatformSpotify, artistOnePlatformSpotify.links);
assertEqual(res2.type, 'metrics');
assertEqual(res2.badges.length, 1, '1 Spotify platform attached must render exactly 1 badge');
assertEqual(res2.badges[0].platform, 'Spotify');
assertEqual(res2.badges[0].badgeText, '85.4K nghe TB');
assertEqual(res2.badges[0].icon, 'spotify');

// 8d. 1 Platform attached: TikTok only (followers count / "lượt flow")
const artistOnePlatformTikTok: Artist = {
  id: 103,
  name: 'TikTokOnly',
  published: true,
  displayOrder: 3,
  links: [{ platform: 'TikTok', url: 'https://www.tiktok.com/@tiktokonly', displayOrder: 1 }],
  stats: {
    platform: 'TikTok',
    totalViews: 0,
    formattedViews: '',
    tiktokFollowers: 42800,
    followersCount: 42800,
    formattedFollowers: '42.8K người theo dõi',
    videoCount: 0,
  },
};
const res3 = resolveCardBadges(artistOnePlatformTikTok, artistOnePlatformTikTok.links);
assertEqual(res3.type, 'metrics');
assertEqual(res3.badges.length, 1, '1 TikTok platform attached must render exactly 1 badge');
assertEqual(res3.badges[0].platform, 'TikTok');
assertEqual(res3.badges[0].badgeText, '42.8K flow');
assertEqual(res3.badges[0].icon, 'tiktok');

// 8e. 2 Platforms attached: YouTube + Spotify
const artistDualYTSpotify: Artist = {
  id: 104,
  name: 'DualYTSpotify',
  published: true,
  displayOrder: 4,
  links: [
    { platform: 'YouTube', url: 'https://youtube.com/@dual', displayOrder: 1 },
    { platform: 'Spotify', url: 'https://open.spotify.com/artist/dual', displayOrder: 2 },
  ],
  stats: {
    platform: 'YouTube',
    totalViews: 3000000,
    formattedViews: '3M lượt xem',
    followersCount: 60000,
    formattedFollowers: '60K người theo dõi',
    videoCount: 15,
    topVideo: {
      videoId: 'dualTopVid',
      title: 'Neon Drift',
      viewCount: 1500000,
    },
    spotifyAvgStreams: 124500,
  },
};
const res4 = resolveCardBadges(artistDualYTSpotify, artistDualYTSpotify.links);
assertEqual(res4.type, 'metrics');
assertEqual(res4.badges.length, 2, '2 platforms attached must render exactly 2 badges');
assertEqual(res4.badges[0].platform, 'YouTube');
assertEqual(res4.badges[0].badgeText, '1.5M view');
assertEqual(res4.badges[1].platform, 'Spotify');
assertEqual(res4.badges[1].badgeText, '124.5K nghe TB');

// 8f. 2 Platforms attached: YouTube + TikTok
const artistDualYTTikTok: Artist = {
  id: 105,
  name: 'DualYTTikTok',
  published: true,
  displayOrder: 5,
  links: [
    { platform: 'YouTube', url: 'https://youtube.com/@dual2', displayOrder: 1 },
    { platform: 'TikTok', url: 'https://www.tiktok.com/@dual2', displayOrder: 2 },
  ],
  stats: {
    platform: 'YouTube',
    totalViews: 1000000,
    formattedViews: '1M lượt xem',
    followersCount: 20000,
    formattedFollowers: '20K người theo dõi',
    videoCount: 5,
    topVideo: {
      videoId: 'vidYTTikTok',
      title: 'Bass Anthem',
      viewCount: 680000,
    },
    tiktokFollowers: 95400,
  },
};
const res5 = resolveCardBadges(artistDualYTTikTok, artistDualYTTikTok.links);
assertEqual(res5.type, 'metrics');
assertEqual(res5.badges.length, 2, '2 platforms attached must render exactly 2 badges');
assertEqual(res5.badges[0].platform, 'YouTube');
assertEqual(res5.badges[0].badgeText, '680K view');
assertEqual(res5.badges[1].platform, 'TikTok');
assertEqual(res5.badges[1].badgeText, '95.4K flow');

// 8g. 3 Platforms attached: ALL THREE (YouTube + Spotify + TikTok)
const artistTripleAll: Artist = {
  id: 106,
  name: 'TripleAllStars',
  published: true,
  displayOrder: 6,
  links: [
    { platform: 'YouTube', url: 'https://youtube.com/@triple', displayOrder: 1 },
    { platform: 'Spotify', url: 'https://open.spotify.com/artist/triple', displayOrder: 2 },
    { platform: 'TikTok', url: 'https://www.tiktok.com/@triple', displayOrder: 3 },
  ],
  stats: {
    platform: 'YouTube',
    totalViews: 5000000,
    formattedViews: '5M lượt xem',
    followersCount: 150000,
    formattedFollowers: '150K người theo dõi',
    videoCount: 25,
    topVideo: {
      videoId: 'superHitVid',
      title: 'SUPER HIT 2.0',
      viewCount: 2400000,
    },
    spotifyAvgStreams: 175000,
    tiktokFollowers: 320000,
  },
};
const res6 = resolveCardBadges(artistTripleAll, artistTripleAll.links);
assertEqual(res6.type, 'metrics');
assertEqual(res6.badges.length, 3, 'All 3 platforms attached must render all 3 badges harmoniously');
assertEqual(res6.badges[0].platform, 'YouTube');
assertEqual(res6.badges[0].badgeText, '2.4M view');
assertEqual(res6.badges[1].platform, 'Spotify');
assertEqual(res6.badges[1].badgeText, '175K nghe TB');
assertEqual(res6.badges[2].platform, 'TikTok');
assertEqual(res6.badges[2].badgeText, '320K flow');

console.log('✓ resolveCardBadges passed all 1, 2, and 3 platform display engine tests');

console.log('🎉 ALL UNIT TESTS PASSED SUCCESSFULLY! 🎉');

console.log('🎉 ALL UNIT TESTS PASSED SUCCESSFULLY! 🎉');
