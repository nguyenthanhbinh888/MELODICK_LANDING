import {
  getArtistMonogram,
  formatCatalogCode,
  getArtistRoleTag,
  isSyntheticVideo,
  isValidUrl,
  getPlatformDockClass,
  getPlatformActionLabel,
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

console.log('🎉 ALL UNIT TESTS PASSED SUCCESSFULLY! 🎉');
