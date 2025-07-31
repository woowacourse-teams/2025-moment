import * as Sentry from '@sentry/react';

Sentry.init({
  dsn: process.env.REACT_APP_SENTRY_DSN,
  sendDefaultPii: true,
  enableLogs: true,
  environment: process.env.NODE_ENV,

  integrations: [],
});
