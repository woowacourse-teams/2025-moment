import { AuthProvider } from '@shared/auth/AuthProvider';
import { AppRouter } from '@app/index';

function App() {
  return (
    <AuthProvider>
      <AppRouter />
    </AuthProvider>
  );
}

export default App;
