import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { PublicLayout } from '../../layouts/PublicLayout';
import { AdminLayout } from '../../layouts/AdminLayout';
import { ProtectedRoute } from '../../components/common/ProtectedRoute';
import { HomePage } from '../../pages/public/HomePage';
import { ArtistsPage } from '../../pages/public/ArtistsPage';
import { SubmitPage } from '../../pages/public/SubmitPage';
import { AdminLoginPage } from '../../pages/admin/AdminLoginPage';
import { AdminDashboardPage } from '../../pages/admin/AdminDashboardPage';
import { AdminArtistsPage } from '../../pages/admin/AdminArtistsPage';
import { AdminSiteSettingsPage } from '../../pages/admin/AdminSiteSettingsPage';

export const AppRouter: React.FC = () => {
  return (
    <Routes>
      {/* Public Routes */}
      <Route element={<PublicLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/artists" element={<ArtistsPage />} />
        <Route path="/submit" element={<SubmitPage />} />
      </Route>

      {/* Admin Login Route (Unprotected) */}
      <Route path="/admin/login" element={<AdminLoginPage />} />

      {/* Protected Admin Routes */}
      <Route path="/admin" element={<ProtectedRoute />}>
        <Route element={<AdminLayout />}>
          <Route index element={<AdminDashboardPage />} />
          <Route path="artists" element={<AdminArtistsPage />} />
          <Route path="settings" element={<AdminSiteSettingsPage />} />
        </Route>
      </Route>

      {/* Fallback to Public Home */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};
