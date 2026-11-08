import React, { useEffect, useState, useMemo, useRef, useCallback } from 'react';
import {
  Plus,
  Search,
  Edit2,
  Trash2,
  Eye,
  EyeOff,
  Image as ImageIcon,
  X,
  PlusCircle,
  AlertCircle,
  CheckCircle,
  Loader2,
  Upload,
  Link as LinkIcon,
  RefreshCw,
  Flame,
} from 'lucide-react';
import type { Artist, ArtistInput } from '../../types';
import { adminArtistsApi, adminUploadApi } from '../../services/api';
import './AdminArtistsPage.css';

const PLATFORM_OPTIONS = [
  'Spotify',
  'SoundCloud',
  'YouTube',
  'Instagram',
  'TikTok',
  'Apple Music',
  'Other',
];

export const AdminArtistsPage: React.FC = () => {
  const [artists, setArtists] = useState<Artist[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingArtistId, setEditingArtistId] = useState<number | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // File upload state
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [showUrlInput, setShowUrlInput] = useState(false);

  // Delete modal state
  const [deletingArtist, setDeletingArtist] = useState<Artist | null>(null);

  // Platform stats refresh state
  const [refreshingStatsIds, setRefreshingStatsIds] = useState<Record<number, boolean>>({});

  // Form state
  const [formData, setFormData] = useState<ArtistInput>({
    name: '',
    imageUrl: '',
    bio: '',
    published: true,
    displayOrder: 1,
    links: [],
  });

  const fetchArtists = useCallback(async () => {
    setLoading(true);
    try {
      const data = await adminArtistsApi.getAll(search);
      setArtists(data);
    } catch (err: unknown) {
      setMessage({
        type: 'error',
        text: err instanceof Error ? err.message : 'Lỗi khi tải danh sách nghệ sĩ',
      });
    } finally {
      setLoading(false);
    }
  }, [search]);

  useEffect(() => {
    fetchArtists();
  }, [fetchArtists]);

  const showNotification = (type: 'success' | 'error', text: string) => {
    setMessage({ type, text });
    setTimeout(() => setMessage(null), 4000);
  };

  const handleTriggerFileInput = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      showNotification('error', 'Vui lòng chọn định dạng ảnh hợp lệ (PNG, JPG, WEBP, GIF)');
      return;
    }

    if (file.size > 10 * 1024 * 1024) {
      showNotification('error', 'Kích thước file ảnh vượt quá giới hạn 10MB');
      return;
    }

    setIsUploading(true);
    try {
      const res = await adminUploadApi.uploadImage(file);
      setFormData((prev) => ({ ...prev, imageUrl: res.url }));
      showNotification('success', 'Đã tải ảnh lên thành công!');
    } catch (err: unknown) {
      showNotification(
        'error',
        err instanceof Error ? err.message : 'Tải ảnh lên thất bại. Vui lòng thử lại.'
      );
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const handleRemoveImage = () => {
    setFormData((prev) => ({ ...prev, imageUrl: '' }));
  };

  const handleRefreshStats = async (artistId: number) => {
    setRefreshingStatsIds((prev) => ({ ...prev, [artistId]: true }));
    try {
      const updatedStats = await adminArtistsApi.refreshStats(artistId);
      setArtists((prev) =>
        prev.map((a) => (a.id === artistId ? { ...a, stats: updatedStats } : a))
      );
      showNotification('success', 'Đã cập nhật chỉ số lượt xem & video hot thành công!');
    } catch (err: unknown) {
      showNotification(
        'error',
        err instanceof Error ? err.message : 'Không thể làm mới chỉ số nền tảng'
      );
    } finally {
      setRefreshingStatsIds((prev) => ({ ...prev, [artistId]: false }));
    }
  };

  const handleOpenAddModal = () => {
    setEditingArtistId(null);
    setShowUrlInput(false);
    setFormData({
      name: '',
      imageUrl: '',
      bio: '',
      published: true,
      displayOrder: artists.length + 1,
      links: [
        { platform: 'Spotify', url: '', displayOrder: 1 },
        { platform: 'SoundCloud', url: '', displayOrder: 2 },
        { platform: 'Instagram', url: '', displayOrder: 3 },
      ],
    });
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (artist: Artist) => {
    setEditingArtistId(artist.id);
    setShowUrlInput(Boolean(artist.imageUrl && artist.imageUrl.startsWith('http')));
    setFormData({
      name: artist.name,
      slug: artist.slug || '',
      imageUrl: artist.imageUrl || '',
      bio: artist.bio || '',
      published: artist.published,
      displayOrder: artist.displayOrder,
      links: (artist.links || []).map((l) => ({
        id: l.id,
        platform: l.platform,
        url: l.url || '',
        displayOrder: l.displayOrder,
      })),
    });
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingArtistId(null);
    setShowUrlInput(false);
    setIsUploading(false);
  };

  const handleAddLink = () => {
    setFormData((prev) => ({
      ...prev,
      links: [
        ...prev.links,
        {
          platform: 'Spotify',
          url: '',
          displayOrder: prev.links.length + 1,
        },
      ],
    }));
  };

  const handleRemoveLink = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      links: prev.links.filter((_, idx) => idx !== index),
    }));
  };

  const handleLinkChange = (index: number, field: 'platform' | 'url', value: string) => {
    setFormData((prev) => {
      const updated = [...prev.links];
      updated[index] = { ...updated[index], [field]: value };
      return { ...prev, links: updated };
    });
  };

  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name.trim()) {
      showNotification('error', 'Tên nghệ sĩ không được để trống');
      return;
    }

    setIsSubmitting(true);
    try {
      if (editingArtistId) {
        const updated = await adminArtistsApi.update(editingArtistId, formData);
        setArtists((prev) => prev.map((a) => (a.id === editingArtistId ? updated : a)));
        showNotification('success', `Đã cập nhật nghệ sĩ ${formData.name}`);
      } else {
        const created = await adminArtistsApi.create(formData);
        setArtists((prev) => [...prev, created]);
        showNotification('success', `Đã thêm nghệ sĩ mới ${formData.name}`);
      }
      handleCloseModal();
      await fetchArtists();
    } catch (err: unknown) {
      showNotification(
        'error',
        err instanceof Error ? err.message : 'Không thể lưu thông tin nghệ sĩ'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleToggleVisibility = async (artist: Artist) => {
    try {
      const updated = await adminArtistsApi.toggleVisibility(artist.id);
      setArtists((prev) => prev.map((a) => (a.id === artist.id ? updated : a)));
      showNotification(
        'success',
        `Nghệ sĩ ${artist.name} hiện đang ở trạng thái ${updated.published ? 'Công khai' : 'Ẩn'}`
      );
    } catch (err: unknown) {
      showNotification(
        'error',
        err instanceof Error ? err.message : 'Lỗi khi cập nhật trạng thái hiển thị'
      );
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingArtist) return;
    try {
      await adminArtistsApi.delete(deletingArtist.id);
      showNotification('success', `Đã xóa nghệ sĩ ${deletingArtist.name}`);
      setDeletingArtist(null);
      await fetchArtists();
    } catch (err: unknown) {
      showNotification('error', err instanceof Error ? err.message : 'Lỗi khi xóa nghệ sĩ');
    }
  };

  const sortedArtists = useMemo(() => {
    return [...artists].sort((a, b) => a.displayOrder - b.displayOrder);
  }, [artists]);

  return (
    <div className="admin-page">
      <div className="admin-page-header">
        <div>
          <h1 className="admin-page-title">Quản lý Nghệ sĩ</h1>
          <p className="admin-page-desc">
            Danh sách nghệ sĩ, quản lý ảnh đại diện, liên kết nền tảng và thứ tự hiển thị.
          </p>
        </div>
        <button onClick={handleOpenAddModal} className="btn-admin-primary">
          <Plus size={18} />
          <span>Thêm Nghệ sĩ</span>
        </button>
      </div>

      {message && (
        <div className={`admin-notification ${message.type === 'success' ? 'notice-success' : 'notice-error'}`}>
          {message.type === 'success' ? <CheckCircle size={18} /> : <AlertCircle size={18} />}
          <span>{message.text}</span>
        </div>
      )}

      {/* Filter and Search Bar */}
      <div className="table-controls">
        <div className="search-box">
          <Search size={18} className="search-icon" />
          <input
            type="text"
            placeholder="Tìm theo tên nghệ sĩ..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          {search && (
            <button className="clear-search-btn" onClick={() => setSearch('')}>
              <X size={16} />
            </button>
          )}
        </div>
      </div>

      {/* Artists Table */}
      <div className="table-wrapper">
        {loading ? (
          <div className="table-loading">Đang tải danh sách nghệ sĩ...</div>
        ) : sortedArtists.length === 0 ? (
          <div className="table-empty">
            <p>Không tìm thấy nghệ sĩ nào.</p>
            <button onClick={handleOpenAddModal} className="btn-admin-secondary mt-2">
              Thêm nghệ sĩ đầu tiên
            </button>
          </div>
        ) : (
          <table className="admin-table">
            <thead>
              <tr>
                <th style={{ width: '70px' }}>Thứ tự</th>
                <th style={{ width: '70px' }}>Ảnh</th>
                <th>Tên Nghệ sĩ</th>
                <th>Trạng thái</th>
                <th>Chỉ số Lượt xem & Video Hot</th>
                <th>Số liên kết</th>
                <th style={{ textAlign: 'right' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {sortedArtists.map((artist) => (
                <tr key={artist.id}>
                  <td>
                    <span className="order-badge">#{artist.displayOrder}</span>
                  </td>
                  <td>
                    <div className="table-avatar">
                      {artist.imageUrl ? (
                        <img src={artist.imageUrl} alt={artist.name} />
                      ) : (
                        <ImageIcon size={18} className="placeholder-icon" />
                      )}
                    </div>
                  </td>
                  <td>
                    <div className="artist-table-title">{artist.name}</div>
                    {artist.bio && <div className="artist-table-bio">{artist.bio}</div>}
                  </td>
                  <td>
                    <button
                      className={`status-badge-btn ${artist.published ? 'status-published' : 'status-hidden'}`}
                      onClick={() => handleToggleVisibility(artist)}
                      title="Click để đổi trạng thái ẩn/hiện"
                    >
                      {artist.published ? (
                        <>
                          <Eye size={14} />
                          <span>Công khai</span>
                        </>
                      ) : (
                        <>
                          <EyeOff size={14} />
                          <span>Đang ẩn</span>
                        </>
                      )}
                    </button>
                  </td>
                  <td>
                    <div className="admin-stats-cell">
                      {artist.stats && (artist.stats.totalViews > 0 || artist.stats.topVideo || artist.stats.followersCount > 0) ? (
                        <div className="admin-stats-info">
                          {artist.stats.totalViews > 0 && (
                            <span className="admin-views-tag" title={`Tổng lượt xem: ${artist.stats.totalViews.toLocaleString()}`}>
                              👁️ {artist.stats.formattedViews || `${artist.stats.totalViews.toLocaleString()} views`}
                            </span>
                          )}
                          {artist.stats.topVideo && (
                            <span className="admin-topvideo-tag" title={artist.stats.topVideo.title}>
                              <Flame size={12} className="admin-flame-icon" />
                              <span className="admin-topvideo-title">{artist.stats.topVideo.title}</span>
                            </span>
                          )}
                          {artist.stats.followersCount > 0 && (
                            <span className="admin-followers-tag" title={`Người theo dõi: ${artist.stats.followersCount.toLocaleString()}`}>
                              👥 {artist.stats.formattedFollowers || `${artist.stats.followersCount.toLocaleString()} followers`}
                            </span>
                          )}
                        </div>
                      ) : (
                        <span className="admin-no-stats">Chưa có chỉ số</span>
                      )}
                      <button
                        type="button"
                        onClick={() => handleRefreshStats(artist.id)}
                        disabled={refreshingStatsIds[artist.id]}
                        className="admin-refresh-stats-btn"
                        title="Đồng bộ / làm mới số liệu từ API công khai"
                      >
                        <RefreshCw size={12} className={refreshingStatsIds[artist.id] ? 'spin-icon' : ''} />
                        <span>{refreshingStatsIds[artist.id] ? 'Đang tải...' : 'Làm mới'}</span>
                      </button>
                    </div>
                  </td>
                  <td>
                    <span className="links-count-pill">
                      {(artist.links || []).filter((l) => l.url && l.url !== '#').length} nền tảng
                    </span>
                  </td>
                  <td>
                    <div className="table-actions">
                      <button
                        onClick={() => handleOpenEditModal(artist)}
                        className="action-btn edit-btn"
                        title="Chỉnh sửa"
                      >
                        <Edit2 size={16} />
                      </button>
                      <button
                        onClick={() => setDeletingArtist(artist)}
                        className="action-btn delete-btn"
                        title="Xóa nghệ sĩ"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Create / Edit Modal */}
      {isModalOpen && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <h2 className="modal-title">
                {editingArtistId ? 'Chỉnh sửa Nghệ sĩ' : 'Thêm Nghệ sĩ Mới'}
              </h2>
              <button className="modal-close-btn" onClick={handleCloseModal}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleFormSubmit} className="modal-body">
              <div className="form-grid">
                <div className="form-group">
                  <label htmlFor="artist-name">Tên Nghệ sĩ *</label>
                  <input
                    id="artist-name"
                    type="text"
                    required
                    placeholder="VD: MIRAGEMFXNK"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="artist-order">Thứ tự hiển thị (Display Order)</label>
                  <input
                    id="artist-order"
                    type="number"
                    min="1"
                    value={formData.displayOrder}
                    onChange={(e) =>
                      setFormData({ ...formData, displayOrder: parseInt(e.target.value, 10) || 1 })
                    }
                  />
                </div>
              </div>

              <div className="form-group image-upload-group">
                <label>Ảnh đại diện nghệ sĩ</label>
                <input
                  type="file"
                  ref={fileInputRef}
                  onChange={handleFileChange}
                  accept="image/png,image/jpeg,image/jpg,image/webp,image/gif"
                  style={{ display: 'none' }}
                />

                <div className="image-upload-container">
                  {formData.imageUrl ? (
                    <div className="image-preview-card">
                      <div className="preview-avatar-wrapper">
                        <img
                          src={formData.imageUrl}
                          alt="Preview"
                          className="uploaded-preview-img"
                          onError={(e) => {
                            (e.target as HTMLImageElement).src =
                              'https://placehold.co/120x120/1e293b/ffffff?text=Image';
                          }}
                        />
                      </div>
                      <div className="preview-info">
                        <div className="preview-url-text" title={formData.imageUrl}>
                          {formData.imageUrl}
                        </div>
                        <div className="preview-actions">
                          <button
                            type="button"
                            className="btn-upload-file"
                            onClick={handleTriggerFileInput}
                            disabled={isUploading}
                          >
                            {isUploading ? (
                              <>
                                <Loader2 size={14} className="spin-icon" />
                                <span>Đang tải...</span>
                              </>
                            ) : (
                              <>
                                <Upload size={14} />
                                <span>Chọn ảnh khác</span>
                              </>
                            )}
                          </button>
                          <button
                            type="button"
                            className="btn-remove-image"
                            onClick={handleRemoveImage}
                            title="Xóa ảnh hiện tại"
                          >
                            <Trash2 size={14} />
                            <span>Xóa</span>
                          </button>
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div
                      className={`upload-dropzone ${isUploading ? 'uploading' : ''}`}
                      onClick={!isUploading ? handleTriggerFileInput : undefined}
                    >
                      {isUploading ? (
                        <div className="upload-dropzone-content">
                          <Loader2 size={24} className="spin-icon text-accent" />
                          <span className="dropzone-title">Đang tải ảnh lên hệ thống...</span>
                        </div>
                      ) : (
                        <div className="upload-dropzone-content">
                          <div className="upload-icon-circle">
                            <Upload size={20} />
                          </div>
                          <div className="dropzone-details">
                            <span className="dropzone-title">Chọn ảnh từ máy tính</span>
                            <span className="dropzone-subtitle">Hỗ trợ JPG, PNG, WEBP, GIF (Tối đa 10MB)</span>
                          </div>
                        </div>
                      )}
                    </div>
                  )}

                  <div className="url-toggle-wrapper">
                    <button
                      type="button"
                      className="btn-toggle-url"
                      onClick={() => setShowUrlInput(!showUrlInput)}
                    >
                      <LinkIcon size={13} />
                      <span>{showUrlInput ? 'Ẩn ô nhập URL trực tiếp' : 'Hoặc nhập URL ảnh trực tiếp'}</span>
                    </button>

                    {showUrlInput && (
                      <input
                        id="artist-image"
                        type="url"
                        placeholder="https://example.com/artist.jpg hoặc đường dẫn ảnh..."
                        value={formData.imageUrl || ''}
                        onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
                        className="manual-url-input"
                      />
                    )}
                  </div>
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="artist-bio">Mô tả ngắn (Bio)</label>
                <textarea
                  id="artist-bio"
                  rows={2}
                  placeholder="Giới thiệu phong cách âm nhạc..."
                  value={formData.bio || ''}
                  onChange={(e) => setFormData({ ...formData, bio: e.target.value })}
                />
              </div>

              <div className="form-checkbox">
                <label>
                  <input
                    type="checkbox"
                    checked={formData.published}
                    onChange={(e) => setFormData({ ...formData, published: e.target.checked })}
                  />
                  <span>Công khai trên trang chính (Published)</span>
                </label>
              </div>

              {/* Platform links */}
              <div className="links-section">
                <div className="links-section-header">
                  <label>Liên kết Nền tảng Âm nhạc & Mạng xã hội</label>
                  <button type="button" onClick={handleAddLink} className="add-link-btn">
                    <PlusCircle size={16} />
                    <span>Thêm liên kết</span>
                  </button>
                </div>

                {formData.links.map((link, idx) => (
                  <div key={idx} className="link-row">
                    <select
                      value={link.platform}
                      onChange={(e) => handleLinkChange(idx, 'platform', e.target.value)}
                    >
                      {PLATFORM_OPTIONS.map((opt) => (
                        <option key={opt} value={opt}>
                          {opt}
                        </option>
                      ))}
                    </select>

                    <input
                      type="text"
                      placeholder="URL (VD: https://open.spotify.com/artist/...)"
                      value={link.url}
                      onChange={(e) => handleLinkChange(idx, 'url', e.target.value)}
                    />

                    <button
                      type="button"
                      onClick={() => handleRemoveLink(idx)}
                      className="remove-link-btn"
                      title="Xóa link"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                ))}
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  onClick={handleCloseModal}
                  className="btn-admin-secondary"
                  disabled={isSubmitting}
                >
                  Hủy
                </button>
                <button type="submit" className="btn-admin-primary" disabled={isSubmitting}>
                  {isSubmitting ? (
                    <>
                      <Loader2 size={16} className="spin-icon" />
                      <span>Đang lưu...</span>
                    </>
                  ) : (
                    'Lưu Nghệ sĩ'
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {deletingArtist && (
        <div className="modal-backdrop">
          <div className="modal-card modal-confirm">
            <div className="modal-header">
              <h2 className="modal-title">Xác nhận xóa nghệ sĩ</h2>
              <button className="modal-close-btn" onClick={() => setDeletingArtist(null)}>
                <X size={20} />
              </button>
            </div>
            <div className="modal-body">
              <p>
                Bạn có chắc chắn muốn xóa nghệ sĩ <strong>{deletingArtist.name}</strong> không? Toàn bộ liên kết nền tảng liên quan cũng sẽ bị xóa. Thao tác này không thể hoàn tác.
              </p>
            </div>
            <div className="modal-footer">
              <button onClick={() => setDeletingArtist(null)} className="btn-admin-secondary">
                Hủy bỏ
              </button>
              <button onClick={handleDeleteConfirm} className="btn-admin-danger">
                Xóa vĩnh viễn
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

