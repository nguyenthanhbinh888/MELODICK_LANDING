import type {
  AdminUser,
  Artist,
  ArtistInput,
  AuthResponse,
  DashboardStats,
  SiteSettings,
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_URL || '';

// Token storage in sessionStorage (cleared on tab close, avoiding long-term localStorage)
const TOKEN_KEY = 'melodick_admin_token';

export const tokenStorage = {
  get: (): string | null => sessionStorage.getItem(TOKEN_KEY),
  set: (token: string): void => sessionStorage.setItem(TOKEN_KEY, token),
  clear: (): void => sessionStorage.removeItem(TOKEN_KEY),
};

let onUnauthorizedCallback: (() => void) | null = null;

export const setOnUnauthorizedCallback = (callback: () => void) => {
  onUnauthorizedCallback = callback;
};

async function request<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  const headers = new Headers(options.headers || {});

  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  const token = tokenStorage.get();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    tokenStorage.clear();
    if (onUnauthorizedCallback) {
      onUnauthorizedCallback();
    }
    const errData = await response.json().catch(() => ({}));
    throw new Error(errData.message || 'Phiên đăng nhập đã hết hạn hoặc chưa được xác thực');
  }

  if (response.status === 403) {
    const errData = await response.json().catch(() => ({}));
    throw new Error(errData.message || 'Bạn không có quyền truy cập chức năng này (403)');
  }

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    let errorMessage = data.message || `Lỗi HTTP ${response.status}`;
    if (data.errors && typeof data.errors === 'object') {
      const fieldErrors = Object.entries(data.errors)
        .map(([field, msg]) => `${field}: ${msg}`)
        .join(', ');
      errorMessage = `${errorMessage} (${fieldErrors})`;
    }
    const error: Error & { details?: Record<string, string> } = new Error(errorMessage);
    error.details = data.errors;
    throw error;
  }

  return data as T;
}

// 1. PUBLIC API
export const publicApi = {
  getSiteSettings: async (): Promise<SiteSettings> => {
    return request<SiteSettings>('/api/public/site');
  },
  getArtists: async (): Promise<Artist[]> => {
    return request<Artist[]>('/api/public/artists');
  },
};

// 2. ADMIN AUTH API
export const adminAuthApi = {
  login: async (username: string, password: string): Promise<AuthResponse> => {
    const res = await request<AuthResponse>('/api/admin/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
    if (res.token) {
      tokenStorage.set(res.token);
    }
    return res;
  },
  logout: async (): Promise<{ message: string }> => {
    try {
      const res = await request<{ message: string }>('/api/admin/auth/logout', {
        method: 'POST',
      });
      return res;
    } finally {
      tokenStorage.clear();
    }
  },
  getMe: async (): Promise<AdminUser> => {
    return request<AdminUser>('/api/admin/auth/me');
  },
};

// 3. ADMIN DASHBOARD API
export const adminDashboardApi = {
  getStats: async (): Promise<DashboardStats> => {
    return request<DashboardStats>('/api/admin/dashboard');
  },
};

// 4. ADMIN ARTISTS API
export const adminArtistsApi = {
  getAll: async (search?: string): Promise<Artist[]> => {
    const query = search ? `?search=${encodeURIComponent(search)}` : '';
    return request<Artist[]>(`/api/admin/artists${query}`);
  },
  getById: async (id: number): Promise<Artist> => {
    return request<Artist>(`/api/admin/artists/${id}`);
  },
  create: async (artist: ArtistInput): Promise<Artist> => {
    return request<Artist>('/api/admin/artists', {
      method: 'POST',
      body: JSON.stringify(artist),
    });
  },
  update: async (id: number, artist: ArtistInput): Promise<Artist> => {
    return request<Artist>(`/api/admin/artists/${id}`, {
      method: 'PUT',
      body: JSON.stringify(artist),
    });
  },
  toggleVisibility: async (id: number): Promise<Artist> => {
    return request<Artist>(`/api/admin/artists/${id}/visibility`, {
      method: 'PATCH',
    });
  },
  delete: async (id: number): Promise<{ message: string }> => {
    return request<{ message: string }>(`/api/admin/artists/${id}`, {
      method: 'DELETE',
    });
  },
  reorder: async (orders: { id: number; displayOrder: number }[]): Promise<{ message: string }> => {
    return request<{ message: string }>('/api/admin/artists/reorder', {
      method: 'PUT',
      body: JSON.stringify({ orders }),
    });
  },
};

// 5. ADMIN SITE SETTINGS API
export const adminSettingsApi = {
  getSettings: async (): Promise<SiteSettings> => {
    return request<SiteSettings>('/api/admin/site-settings');
  },
  updateSettings: async (settings: SiteSettings): Promise<SiteSettings> => {
    return request<SiteSettings>('/api/admin/site-settings', {
      method: 'PUT',
      body: JSON.stringify(settings),
    });
  },
};

// 6. ADMIN FILE UPLOAD API
export const adminUploadApi = {
  uploadImage: async (file: File): Promise<{ url: string }> => {
    const formData = new FormData();
    formData.append('file', file);
    return request<{ url: string }>('/api/admin/uploads/image', {
      method: 'POST',
      body: formData,
    });
  },
};

