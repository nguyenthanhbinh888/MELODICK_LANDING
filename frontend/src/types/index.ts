export interface ArtistLink {
  id?: number;
  artistId?: number;
  platform: string;
  url: string;
  displayOrder: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface Artist {
  id: number;
  name: string;
  slug?: string;
  imageUrl?: string | null;
  bio?: string | null;
  published: boolean;
  displayOrder: number;
  links: ArtistLink[];
  createdAt?: string;
  updatedAt?: string;
}

export interface ArtistInput {
  name: string;
  slug?: string;
  imageUrl?: string;
  bio?: string;
  published: boolean;
  displayOrder: number;
  links: {
    id?: number;
    platform: string;
    url: string;
    displayOrder: number;
  }[];
}

export interface SiteSettings {
  brand_name?: string;
  hero_title?: string;
  tagline?: string;
  submit_description?: string;
  submit_button_label?: string;
  submit_url?: string;
  copyright_text?: string;
  footer_spotify_url?: string;
  footer_youtube_url?: string;
  footer_instagram_url?: string;
  footer_tiktok_url?: string;
  [key: string]: string | undefined;
}

export interface AdminUser {
  id: number;
  username: string;
  displayName: string;
  active: boolean;
  lastLoginAt?: string;
}

export interface AuthResponse {
  token: string;
  user: AdminUser;
}

export interface DashboardStats {
  totalArtists: number;
  publishedArtists: number;
  hiddenArtists: number;
}

export interface ApiResponse<T = unknown> {
  success: boolean;
  message?: string;
  data?: T;
  errors?: Record<string, string>;
}
