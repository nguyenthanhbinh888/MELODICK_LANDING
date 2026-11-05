import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Users, Eye, EyeOff, Sliders, ArrowRight, ShieldCheck } from 'lucide-react';
import type { DashboardStats } from '../../types';
import { adminDashboardApi } from '../../services/api';
import { useAuth } from '../../app/providers/AuthContext';
import './AdminDashboardPage.css';

export const AdminDashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchStats = async () => {
      setLoading(true);
      try {
        const data = await adminDashboardApi.getStats();
        setStats(data);
      } catch (err: unknown) {
        setError(err instanceof Error ? err.message : 'Không thể tải thống kê');
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  return (
    <div className="admin-page">
      <div className="admin-page-header">
        <div>
          <h1 className="admin-page-title">Dashboard Tổng quan</h1>
          <p className="admin-page-desc">Chào mừng trở lại, {user?.displayName || user?.username}.</p>
        </div>
        <div className="admin-status-pill">
          <ShieldCheck size={16} />
          <span>Phiên làm việc bảo mật</span>
        </div>
      </div>

      {error && <div className="admin-alert-error">{error}</div>}

      {/* Stats Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon-wrapper stat-icon-blue">
            <Users size={22} />
          </div>
          <div className="stat-content">
            <span className="stat-label">Tổng số Nghệ sĩ</span>
            <span className="stat-value">{loading ? '...' : (stats?.totalArtists ?? 0)}</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper stat-icon-green">
            <Eye size={22} />
          </div>
          <div className="stat-content">
            <span className="stat-label">Đang Công khai (Published)</span>
            <span className="stat-value">{loading ? '...' : (stats?.publishedArtists ?? 0)}</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper stat-icon-orange">
            <EyeOff size={22} />
          </div>
          <div className="stat-content">
            <span className="stat-label">Đang Ẩn (Hidden)</span>
            <span className="stat-value">{loading ? '...' : (stats?.hiddenArtists ?? 0)}</span>
          </div>
        </div>
      </div>

      {/* Shortcuts */}
      <div className="dashboard-shortcuts">
        <h2 className="section-title">Lối tắt Quản trị</h2>
        <div className="shortcuts-grid">
          <Link to="/admin/artists" className="shortcut-card">
            <div className="shortcut-info">
              <Users size={24} className="shortcut-icon" />
              <div>
                <h3 className="shortcut-heading">Quản lý Nghệ sĩ</h3>
                <p className="shortcut-text">Thêm, sửa, xóa, chỉnh sửa liên kết nền tảng và thứ tự hiển thị.</p>
              </div>
            </div>
            <ArrowRight size={18} className="shortcut-arrow" />
          </Link>

          <Link to="/admin/settings" className="shortcut-card">
            <div className="shortcut-info">
              <Sliders size={24} className="shortcut-icon" />
              <div>
                <h3 className="shortcut-heading">Cấu hình Landing Page</h3>
                <p className="shortcut-text">Tùy biến tiêu đề thương hiệu, thông điệp Hero, Demo URL và Footer.</p>
              </div>
            </div>
            <ArrowRight size={18} className="shortcut-arrow" />
          </Link>
        </div>
      </div>
    </div>
  );
};
