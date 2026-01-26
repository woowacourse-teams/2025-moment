import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from '@shared/auth/AuthProvider';
import { AuthGuard } from '@shared/auth/AuthGuard';
import LoginPage from '@pages/LoginPage';
import DashboardPage from '@pages/DashboardPage';

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/dashboard"
          element={
            <AuthGuard>
              <DashboardPage />
            </AuthGuard>
          }
        />
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </AuthProvider>
  );
}

export default App;
