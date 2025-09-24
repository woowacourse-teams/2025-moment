import * as Sentry from '@sentry/react';

function getEnvironmentFromDomain() {
  if (typeof window === 'undefined') return 'development'; // SSR 대응

  const hostname = window.location.hostname;

  if (hostname === 'connectingmoment.com') {
    return 'production';
  }

  return 'development';
}

Sentry.init({
  dsn: process.env.REACT_APP_SENTRY_DSN,
  enabled: getEnvironmentFromDomain() === 'production',
  sendDefaultPii: true,
  enableLogs: true,
  environment: getEnvironmentFromDomain(),

  integrations: [],
});
