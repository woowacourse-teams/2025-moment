import * as Sentry from '@sentry/react';

Sentry.init({
  dsn: process.env.REACT_APP_SENTRY_DSN,
  environment: process.env.NODE_ENV,
  tracesSampleRate: 1.0,
  integrations: [
    Sentry.replayIntegration({
      sessionSampleRate: 0,
      errorSampleRate: 1.0,
      maskAllText: true,
      maskAllInputs: true,
    }),
  ],
});
