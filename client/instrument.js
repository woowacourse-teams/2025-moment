import * as Sentry from '@sentry/react';

function getEnvironmentFromDomain() {
  if (typeof window === 'undefined') return 'development'; // SSR 대응
  return window.location.hostname === 'connectingmoment.com' ? 'production' : 'development';
}

Sentry.init({
  dsn: process.env.REACT_APP_SENTRY_DSN,
  enabled: getEnvironmentFromDomain() === 'production',
  environment: getEnvironmentFromDomain(),

  integrations: [
    Sentry.replayIntegration({
      sessionSampleRate: 0,
      errorSampleRate: 1.0,
      // 개인정보 보호
      maskAllText: true,
      maskAllInputs: true,
    }),
  ],
});
