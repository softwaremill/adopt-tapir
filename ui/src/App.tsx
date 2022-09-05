import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { ConfigurationFormPage } from './pages/ConfigurationFormPage';
import { EmbeddedFormPage } from './pages/EmbeddedFormPage';
import { PreviewStarterPage } from './pages/PreviewStarterPage';
import { RootPage } from './pages/RootPage';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<RootPage />}>
          <Route path="" element={<ConfigurationFormPage />} />
          <Route path="/preview-starter" element={<PreviewStarterPage />} />
        </Route>

        <Route path="/embedded-form" element={<EmbeddedFormPage />} />

        <Route path="*" element={<Navigate to="/" replace={true} />} />
      </Routes>
    </BrowserRouter>
  );
};
