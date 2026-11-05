import React, { useEffect, useState } from 'react';
import {
  Save,
  CheckCircle,
  AlertCircle,
  Loader2,
  Globe,
  Send,
  Share2,
} from 'lucide-react';
import type { SiteSettings } from '../../types';
import { adminSettingsApi } from '../../services/api';
import './AdminSiteSettingsPage.css';

export const AdminSiteSettingsPage: React.FC = () => {
  const [settings, setSettings] = useState<SiteSettings>({
    brand_name: '',
    hero_title: '',
    tagline: '',
    submit_description: '',
    submit_button_label: '',
    submit_url: '',
    copyright_text: '',
    footer_spotify_url: '',
    footer_youtube_url: '',
    footer_instagram_url: '',
    footer_tiktok_url: '',
  });

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  useEffect(() => {
    const fetchSettings = async () => {
      setLoading(true);
      try {
        const data = await adminSettingsApi.getSettings();
        setSettings((prev) => ({ ...prev, ...data }));
      } catch (err: unknown) {
        setMessage({
          type: 'error',
          text: err instanceof Error ? err.message : 'Không thể tải cấu hình',
        });
      } finally {
        setLoading(false);
      }
    };
    fetchSettings();
  }, []);

  const handleChange = (key: keyof SiteSettings, value: string) => {
    setSettings((prev) => ({ ...prev, [key]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setMessage(null);

    try {
      const updated = await adminSettingsApi.updateSettings(settings);
      setSettings(updated);
      setMessage({ type: 'success', text: 'Đã lưu cấu hình Landing Page thành công!' });
    } catch (err: unknown) {
      setMessage({
        type: 'error',
        text: err instanceof Error ? err.message : 'Không thể lưu cấu hình',
      });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="admin-page">
      <div className="admin-page-header">
        <div>
          <h1 className="admin-page-title">Cấu hình Landing Page</h1>
          <p className="admin-page-desc">
            Chỉnh sửa nội dung thương hiệu, thông điệp Hero, liên kết Demo và mạng xã hội.
          </p>
        </div>
      </div>

      {message && (
        <div
          className={`admin-notification ${
            message.type === 'success' ? 'notice-success' : 'notice-error'
          }`}
        >
          {message.type === 'success' ? <CheckCircle size={18} /> : <AlertCircle size={18} />}
          <span>{message.text}</span>
        </div>
      )}

      {loading ? (
        <div className="table-loading">Đang tải thông tin cấu hình...</div>
      ) : (
        <form onSubmit={handleSubmit} className="settings-form">
          {/* Group 1: Brand & Hero */}
          <div className="settings-card">
            <div className="settings-card-header">
              <Globe size={20} className="card-header-icon" />
              <div>
                <h2 className="card-title">Thương hiệu & Hero</h2>
                <p className="card-desc">Cấu hình thông tin chính xuất hiện ở thanh điều hướng và trang chủ</p>
              </div>
            </div>

            <div className="settings-card-body">
              <div className="form-group">
                <label htmlFor="brand_name">Tên thương hiệu (Brand Name)</label>
                <input
                  id="brand_name"
                  type="text"
                  value={settings.brand_name || ''}
                  onChange={(e) => handleChange('brand_name', e.target.value)}
                  placeholder="MELODICK RECS."
                />
              </div>

              <div className="form-group">
                <label htmlFor="hero_title">Tiêu đề lớn Hero (Hero Title)</label>
                <input
                  id="hero_title"
                  type="text"
                  value={settings.hero_title || ''}
                  onChange={(e) => handleChange('hero_title', e.target.value)}
                  placeholder="MELODICK RECS."
                />
              </div>

              <div className="form-group">
                <label htmlFor="tagline">Khẩu hiệu (Tagline)</label>
                <input
                  id="tagline"
                  type="text"
                  value={settings.tagline || ''}
                  onChange={(e) => handleChange('tagline', e.target.value)}
                  placeholder="Architects of the underground sound."
                />
              </div>
            </div>
          </div>

          {/* Group 2: Demo Submission */}
          <div className="settings-card">
            <div className="settings-card-header">
              <Send size={20} className="card-header-icon" />
              <div>
                <h2 className="card-title">Gửi Demo (Submit)</h2>
                <p className="card-desc">Cấu hình thông điệp và đường dẫn Google Forms tiếp nhận demo</p>
              </div>
            </div>

            <div className="settings-card-body">
              <div className="form-group">
                <label htmlFor="submit_description">Nội dung giới thiệu</label>
                <input
                  id="submit_description"
                  type="text"
                  value={settings.submit_description || ''}
                  onChange={(e) => handleChange('submit_description', e.target.value)}
                  placeholder="Looking for new signatures."
                />
              </div>

              <div className="form-group">
                <label htmlFor="submit_button_label">Nhãn nút gửi</label>
                <input
                  id="submit_button_label"
                  type="text"
                  value={settings.submit_button_label || ''}
                  onChange={(e) => handleChange('submit_button_label', e.target.value)}
                  placeholder="SUBMIT DEMO"
                />
              </div>

              <div className="form-group">
                <label htmlFor="submit_url">Đường dẫn Google Forms / URL nhận demo</label>
                <input
                  id="submit_url"
                  type="url"
                  value={settings.submit_url || ''}
                  onChange={(e) => handleChange('submit_url', e.target.value)}
                  placeholder="https://forms.gle/Zj4QpgXX8MuMcGhRA"
                />
                <span className="field-hint">
                  Nếu để trống hoặc không hợp lệ, nút gửi sẽ hiển thị trạng thái tạm đóng.
                </span>
              </div>
            </div>
          </div>

          {/* Group 3: Footer & Social Media */}
          <div className="settings-card">
            <div className="settings-card-header">
              <Share2 size={20} className="card-header-icon" />
              <div>
                <h2 className="card-title">Footer & Mạng xã hội</h2>
                <p className="card-desc">Bản quyền và các liên kết nền tảng ở cuối trang (chỉ hiển thị khi có link hợp lệ)</p>
              </div>
            </div>

            <div className="settings-card-body">
              <div className="form-group">
                <label htmlFor="copyright_text">Nội dung Bản quyền (Copyright)</label>
                <input
                  id="copyright_text"
                  type="text"
                  value={settings.copyright_text || ''}
                  onChange={(e) => handleChange('copyright_text', e.target.value)}
                  placeholder="© 2026 Melodick Recs. All rights reserved."
                />
              </div>

              <div className="form-grid-2">
                <div className="form-group">
                  <label htmlFor="footer_spotify_url">Spotify URL</label>
                  <input
                    id="footer_spotify_url"
                    type="url"
                    value={settings.footer_spotify_url || ''}
                    onChange={(e) => handleChange('footer_spotify_url', e.target.value)}
                    placeholder="https://open.spotify.com/..."
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="footer_youtube_url">YouTube URL</label>
                  <input
                    id="footer_youtube_url"
                    type="url"
                    value={settings.footer_youtube_url || ''}
                    onChange={(e) => handleChange('footer_youtube_url', e.target.value)}
                    placeholder="https://youtube.com/@..."
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="footer_instagram_url">Instagram URL</label>
                  <input
                    id="footer_instagram_url"
                    type="url"
                    value={settings.footer_instagram_url || ''}
                    onChange={(e) => handleChange('footer_instagram_url', e.target.value)}
                    placeholder="https://instagram.com/..."
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="footer_tiktok_url">TikTok URL</label>
                  <input
                    id="footer_tiktok_url"
                    type="url"
                    value={settings.footer_tiktok_url || ''}
                    onChange={(e) => handleChange('footer_tiktok_url', e.target.value)}
                    placeholder="https://tiktok.com/@..."
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Submit Action */}
          <div className="settings-actions">
            <button type="submit" className="btn-admin-primary" disabled={saving}>
              {saving ? (
                <>
                  <Loader2 size={18} className="spin-icon" />
                  <span>Đang lưu thay đổi...</span>
                </>
              ) : (
                <>
                  <Save size={18} />
                  <span>Lưu tất cả thay đổi</span>
                </>
              )}
            </button>
          </div>
        </form>
      )}
    </div>
  );
};
