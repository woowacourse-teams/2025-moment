// client/src/shared/lib/ga/index.ts
import ReactGA from 'react-ga4';

const GA_MEASUREMENT_ID = process.env.REACT_APP_GA_ID || '';

const ALLOWED_HOSTS = new Set(['connectingmoment.com', 'www.connectingmoment.com']);
const isProdEnv = process.env.NODE_ENV === 'production';
const isAllowedHost = typeof window !== 'undefined' && ALLOWED_HOSTS.has(window.location.hostname);

let initialized = false;

export const isGAEnabled = () => isProdEnv && isAllowedHost && initialized;

export const initGA = () => {
  if (!isProdEnv || !isAllowedHost) return;
  if (initialized) return;

  if (!GA_MEASUREMENT_ID) {
    console.warn('GA_MEASUREMENT_ID is not set');
    return;
  }

  ReactGA.initialize(GA_MEASUREMENT_ID);
  initialized = true;
};

export const sendPageview = (path: string) => {
  if (!isGAEnabled()) {
    if (!isProdEnv) {
      console.debug('[GA][pageview][dev-only]', path, { title: document.title });
    }
    return;
  }

  ReactGA.send({ hitType: 'pageview', page: path, title: document.title });
};
