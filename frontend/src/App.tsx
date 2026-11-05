import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './app/providers/AuthContext';
import { AppRouter } from './app/router';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRouter />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
