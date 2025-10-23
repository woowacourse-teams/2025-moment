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
      // 에러 발생 시에만 Replay 기록 (비용 절약)
      sessionSampleRate: 0,
      errorSampleRate: 1.0,
      // 개인정보 보호
      maskAllText: true,
      maskAllInputs: true,
    }),
  ],

  beforeSend(event) {
    const error = event.exception?.values?.[0];
    if (!error) return event;

    const errorMessage = error.value || '';

    /* 불필요한 에러 제외 */
    // 1. 네트워크 에러 (사용자 환경 문제)
    if (
      errorMessage.includes('NetworkError') ||
      errorMessage.includes('Failed to fetch') ||
      errorMessage.includes('ERR_NETWORK')
    ) {
      return null;
    }

    // 2. 번들 로딩 실패 (일시적 문제)
    if (errorMessage.includes('Loading chunk') || errorMessage.includes('ChunkLoadError')) {
      return null;
    }

    // 3. 404 에러 (사용자 실수)
    if (event.tags?.http_status === '404') {
      return null;
    }

    return event;
  },
});
