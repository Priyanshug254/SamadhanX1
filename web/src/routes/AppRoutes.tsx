import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from '../pages/auth/LoginPage';
import { AppLayout } from '../components/layout/AppLayout';
import { ProtectedRoute } from '../components/layout/ProtectedRoute';
import { GovernmentDashboardPage } from '../pages/government/GovernmentDashboardPage';
import { ChallengeDetailPage } from '../pages/government/ChallengeDetailPage';
import { GisMapPage } from '../pages/gis/GisMapPage';
import { InnovationHubPage } from '../pages/innovation/InnovationHubPage';
import { ProposalDetailPage } from '../pages/innovation/ProposalDetailPage';
import { IndustryCsrPage } from '../pages/partners/IndustryCsrPage';
import { NationalCommandCenterPage } from '../pages/analytics/NationalCommandCenterPage';
import { ActionCenterPage } from '../pages/governance/ActionCenterPage';
import { useAuth } from '../context/AuthContext';

const RoleBasedHomeRedirect: React.FC = () => {
  const { user } = useAuth();
  const primaryRole = user?.role || 'CITIZEN';

  if (['FACULTY', 'STUDENT', 'UNIVERSITY_ADMIN', 'RESEARCH_LAB'].includes(primaryRole)) {
    return <Navigate to="/innovation" replace />;
  }

  if (['STARTUP', 'MSME', 'INDUSTRY', 'CSR'].includes(primaryRole)) {
    return <Navigate to="/partnerships" replace />;
  }

  if (['GOVERNMENT_OFFICIAL'].includes(primaryRole)) {
    return <Navigate to="/government" replace />;
  }

  if (['SUPER_ADMIN', 'GOVERNMENT_ADMIN'].includes(primaryRole)) {
    return <Navigate to="/action-center" replace />;
  }

  return <Navigate to="/map" replace />;
};

export const AppRoutes: React.FC = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route
        path="/"
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<RoleBasedHomeRedirect />} />
        <Route path="action-center" element={<ActionCenterPage />} />
        
        {/* Government Portal & Aliases */}
        <Route path="government" element={<GovernmentDashboardPage />} />
        <Route path="government/challenges" element={<Navigate to="/government" replace />} />
        <Route path="government/challenges/:id" element={<ChallengeDetailPage />} />
        
        {/* GIS Map & Aliases */}
        <Route path="map" element={<GisMapPage />} />
        <Route path="government/gis-map" element={<GisMapPage />} />
        
        {/* Innovation & University Hub */}
        <Route path="innovation" element={<InnovationHubPage />} />
        <Route path="innovation/proposals/:id" element={<ProposalDetailPage />} />
        
        {/* Industry, MSME & CSR Partnerships */}
        <Route path="partnerships" element={<IndustryCsrPage />} />
        <Route path="industry" element={<IndustryCsrPage />} />
        
        {/* National Analytics Command Center */}
        <Route path="analytics" element={<NationalCommandCenterPage />} />
      </Route>

      <Route path="*" element={<RoleBasedHomeRedirect />} />
    </Routes>
  );
};
