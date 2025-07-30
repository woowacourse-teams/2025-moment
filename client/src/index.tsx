import App from '@/app/App';
import { createRoot } from 'react-dom/client';

async function enableMocking() {
  if (process.env.NODE_ENV !== 'development') {
    return;
  }

  const { worker } = await import('./mocks/browser');

  return worker.start({
    onUnhandledRequest: 'warn',
  });
}

async function startApp() {
  // await enableMocking();
  // aws test

  const rootElement = document.getElementById('root');
  if (!rootElement) {
    throw new Error('Root element not found');
  }

  const root = createRoot(rootElement);
  root.render(<App />);
}

startApp().catch(console.error);
