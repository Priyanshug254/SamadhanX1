import React from 'react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { AppRoutes } from './routes/AppRoutes';
import { PresenterAssistant } from './components/demo/PresenterAssistant';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
        <PresenterAssistant />
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;
