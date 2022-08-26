import {BrowserRouter, Navigate, Route, Routes} from 'react-router-dom';
import {RootPage} from "./pages/RootPage";
import {ConfigurationFormPage} from "./pages/ConfigurationFormPage";
import {PreviewStarterPage} from "./pages/PreviewStarterPage";
import {EmbeddedFormPage} from "./pages/EmbeddedFormPage";

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<RootPage/>}>
          <Route path="" element={<ConfigurationFormPage/>}/>
          <Route path="/preview-starter" element={<PreviewStarterPage/>}/>
        </Route>

        <Route
          path="/embedded-form"
          element={<EmbeddedFormPage/>}
        />

        <Route path="*" element={<Navigate to="/" replace={true} />} />
      </Routes>
    </BrowserRouter>
  );
};
