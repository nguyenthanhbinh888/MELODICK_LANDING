import {
  getArtistMonogram,
  formatCatalogCode,
  getArtistRoleTag,
  isSyntheticVideo,
  isValidUrl,
  getPlatformDockClass,
  getPlatformActionLabel,
  formatViewCount,
  resolveCardBadge,
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
console.log('✓ formatViewCount passed all 10 tests');

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

console.log('✓ resolveCardBadge passed all 6 test scenarios');

console.log('🎉 ALL UNIT TESTS PASSED SUCCESSFULLY! 🎉');
