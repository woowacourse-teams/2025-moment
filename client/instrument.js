import * as Sentry from '@sentry/react';

const ERROR_SAMPLE_RATE = 1.0;

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
      errorSampleRate: ERROR_SAMPLE_RATE,
      maskAllText: true,
      maskAllInputs: true,
    }),
  ],
});
