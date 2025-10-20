import App from '@/app/App';
import { createRoot } from 'react-dom/client';
import '../instrument';


async function startApp() {

  const rootElement = document.getElementById('root');
  if (!rootElement) {
    throw new Error('Root element not found');
  }

  const root = createRoot(rootElement);
  root.render(<App />);
}

startApp().catch(console.error);
