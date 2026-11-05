import React, { createContext, useContext, useEffect, useState } from 'react';
import type { SiteSettings } from '../../types';
import { publicApi } from '../../services/api';

interface SiteSettingsContextType {
  settings: SiteSettings;
  isLoading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

export const DEFAULT_SITE_SETTINGS: SiteSettings = {
  brand_name: 'MELODICK RECS.',
  hero_title: 'MELODICK RECS.',
  tagline: 'Architects of the underground sound.',
  submit_description: 'Looking for new signatures.',
  submit_button_label: 'SUBMIT DEMO',
  submit_url: 'https://forms.gle/Zj4QpgXX8MuMcGhRA',
  copyright_text: '© 2026 Melodick Recs. All rights reserved.',
  footer_spotify_url: '',
  footer_youtube_url: '',
  footer_instagram_url: '',
  footer_tiktok_url: '',
};

const SiteSettingsContext = createContext<SiteSettingsContextType>({
  settings: DEFAULT_SITE_SETTINGS,
  isLoading: false,
  error: null,
  refetch: async () => {},
});

export const SiteSettingsProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [settings, setSettings] = useState<SiteSettings>(DEFAULT_SITE_SETTINGS);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchSettings = async () => {
    setIsLoading(true);
    try {
      const data = await publicApi.getSiteSettings();
      setSettings((prev) => ({ ...prev, ...data }));
      setError(null);
    } catch (err: unknown) {
      console.warn('Could not fetch site settings from backend, using defaults:', err);
      setError(err instanceof Error ? err.message : 'Lỗi tải cấu hình');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchSettings();
  }, []);

  return (
    <SiteSettingsContext.Provider value={{ settings, isLoading, error, refetch: fetchSettings }}>
      {children}
    </SiteSettingsContext.Provider>
  );
};

export const useSiteSettings = () => useContext(SiteSettingsContext);

